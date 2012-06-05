package mmislideshow;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * <p>
 * The {@link ASRRequest} can be used to send a POST request to the Google Voice
 * Search service.
 * </p>
 * 
 * <p>
 * The request contains audio data of the user utterance (FLAC format, 16,000 hz
 * sample rate).
 * </p>
 * 
 * <p>
 * Finally you get a value object containing some meta-data and the recognized
 * text (scored with a confidence value).
 * </p>
 * 
 * <p>
 * At the moment we just receive one hypothesis from the service, but
 * {@link ASRRequest} would be able to handle a list of hypotheses.
 * </p>
 * 
 * <p>
 * You can get the result ( see {@link ASRResult} ) of the automatic speech
 * recognition as raw JSON string or already parsed as an value object.
 * </p>
 * 
 * @author Stefan Schmidt
 * 
 */
public class ASRRequest {
	
	/**
	 * Predefined language code of German.
	 */
	public static final String LANG_de_DE = "de-DE";
	
	/**
	 * Predefined language code of English.
	 */
	public static final String LANG_en_US = "en-US";
	
	/**
	 * Encoding information about the audio data to be send.
	 * Usually you won't change this.
	 */
	private static final String CONTENT_DESCRIPTION = "audio/x-flac; rate=16000";
	
	/**
	 * URL of the Google Voice Search service.
	 *  Usually you won't change this.
	 */
	private static final String BASE_URL = "https://www.google.com/speech-api/v1/recognize";
	
	/**
	 * The parser is used to parse the JSON part of the response. Mainly the hypotheses.
	 */
	private JSONParser parser=new JSONParser();
	
	/**
	 * The content of response as pure text (a JSON string). 
	 */
	private String responseContent;
	
	/**
	 * HTTP status of the response. Just for debugging.
	 */
	private String httpResponsStatusLine;
	
	/**
	 * Language code used for the last executed request.
	 */
	private String languageCode;
	
	/**
	 * <p>
	 * Executes POST request to the Google Voice Search service with given audio
	 * data and for the defined language.
	 * </p>
	 * 
	 * After executing this method, you can get the response via
	 * {@link #getResponseContent()} or still parsed via {@link #getAsrResult()}
	 * (recommended).
	 * 
	 * @param flacFilePath
	 *            Path to a file containg audio data in the FLAC (Free Lossless
	 *            Audio Codec) format. The data have to be encoded with a sample
	 *            rate of 16,000 hz.
	 * @param languageCode
	 *            Code of the language to be used when processing the audio
	 *            data. See {@link #LANG_de_DE} and {@link #LANG_en_US}.
	 * @throws URISyntaxException
	 *             If the constructed URI is malformed. This could be the case
	 *             if you use an obscure language code (special characters?).
	 */
	public void request(final String flacFilePath, final String languageCode) throws URISyntaxException {
		this.languageCode = languageCode;
		
		// Assemble POST request
		HttpPost post = new HttpPost(getUri(languageCode));
		FileEntity entity = new FileEntity(new File(flacFilePath), CONTENT_DESCRIPTION);
		post.setEntity(entity);

		HttpClient client = new DefaultHttpClient();

		try {
			// send POST via client, and get response
			HttpResponse response = client.execute(post);

			// store http state
			httpResponsStatusLine = response.getStatusLine().toString();

			// read response content 
			HttpEntity responeEntity = response.getEntity();
			BufferedInputStream bin = new BufferedInputStream(responeEntity.getContent());
			try {
				StringBuilder sb = new StringBuilder();
				int currentByte = bin.read();

				while(currentByte != -1) {
					sb.append((char) currentByte );
					currentByte = bin.read();
				}

				// store content
				responseContent = sb.toString();
			} finally {
				bin.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the URI to be used for the request. URL parameters are added
	 * here.
	 * 
	 * @param language
	 *            Value for the 'lang' parameter in the URI
	 * @return URI URI to be used for the request.
	 * @throws URISyntaxException
	 *             If the constructed URI is malformed. This could be the case
	 *             if you use an obscure language code (special characters?).
	 */
	private URI getUri(String language) throws URISyntaxException {
		String url = BASE_URL + "?lang=" + language + "&maxresults=6";
		return new URI(url);
	}

	/**
	 * Gets the HTTP status of the response. May be interesting when handling
	 * network connection problems.
	 * 
	 * Before calling the function, you have to execute {@link #request(String, String)}.
	 * 
	 * @return HTTP status line.
	 */
	public String getHttpResponsStatusLine() {
		return httpResponsStatusLine;
	}
	
	/**
	 * Gets the content of the Google Voice Search API response in form of a
	 * JSON string.
	 * 
	 * Before calling the function, you have to execute {@link #request(String, String)}.
	 * 
	 * @return JSON string representation of the response data.
	 */
	public String getResponseContent() {
		return responseContent;
	}
	

	/**
	 * Gets the Google Voice Search response in form of an data object. It is created when calling this 
	 * method, by parsing the previously received JSON string.
	 * 
	 * Before calling the function, you have to execute {@link #request(String, String)}.
	 * 
	 * @return Object representation of the response data.
	 * 
	 * @throws ParseException
	 */
	@SuppressWarnings("rawtypes")
	public ASRResult getAsrResult() throws ParseException {
		
		// no response -> nothing can be parsed
		if(responseContent == null) {
			throw new NullPointerException("responseContent is null. You have perform request() before.");
		}
		
		// The factory is used to create concrete instance of lists and maps
		// when parsing the JSON string.
		ContainerFactory factory = new ContainerFactory() {
			
			@Override
			public Map createObjectContainer() {
				return new HashMap();
			}
			
			@Override
			public List creatArrayContainer() {
				return new ArrayList();
			}
		};
		
		// Parse the JSON string
		Map container = (Map) parser.parse(responseContent, factory);
		
		// Copy values to the data object
		ASRResult result = new ASRResult();
		result.setId( (String) container.get("id") );
		result.setStatus( (Long) container.get("status") );
		
		for(Object hypotheseObject : (ArrayList) container.get("hypotheses")) {
			Map hypoAsMap = (Map) hypotheseObject;
			
			Hypothesis hypothese = new Hypothesis();
			hypothese.setConfidence( (Double) hypoAsMap.get("confidence") );
			hypothese.setUtterance( (String) hypoAsMap.get("utterance") );
			
			result.getHypotheses().add(hypothese);
		}
		
		// finally set the used language code.
		result.setLanguagCode(this.languageCode);
		
		return result;
	}
	
	/**
	 * The main method just demonstrates how to use the ASRRequest class.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ASRRequest asrRequest = new ASRRequest();
		
		// Some example audio files. Modify the paths according 
		// to your environment, when testing with main().  
		// String file = "c:\\users\\stefan\\Hallo.flac";
		 String file = "c:\\users\\stefan\\das_pferd_frisst_keinen_gurkensalat.flac";
		// String file = "c:\\users\\stefan\\rollo_hoch.flac";
		// String file = "c:\\users\\stefan\\bitte_den_link_zu_bertolt_brecht_oeffnen.flac";
		
		try {
			asrRequest.request(file, LANG_de_DE);
			
			// get ASR result as String (pure content of the response)
			System.out.println("JSON string:\n" + asrRequest.getResponseContent());
			
			// get ASR result as value object
			ASRResult asrResult = asrRequest.getAsrResult();
			System.out.println("Data object:\n" + asrResult.toString());
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
