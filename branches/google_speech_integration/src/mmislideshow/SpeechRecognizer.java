package mmislideshow;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.json.simple.parser.ParseException;

public class SpeechRecognizer {

	private AudioFormat audioFormat;
	private TargetDataLine targetDataLine;
	private static String WAVE_FILE = "input.wav";
	private static String FLAC_FILE = "input.flac";
	private String flacPath = "/opt/local/bin/flac";

	private AudioFormat getaudioFormat() {
		float sampleRate = 16000.0F; // 8000,11025,16000,22050,44100
		int sampleSizeInBits = 16; // 8,16
		int channels = 1; // 1,2
		boolean signed = true; // true,false
		boolean bigEndian = false; // true,false

		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
				bigEndian);
	}

	protected void startCaptureAudio() {
		audioFormat = getaudioFormat();
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class,
				audioFormat);
		try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);

			// start the capture thread
			CaptureThread thread = new CaptureThread();
			new Thread(thread).start();

		} catch (LineUnavailableException e) {
			System.out
					.println("An error occured while initalizing the audio device:");
			e.printStackTrace();
		}

	}

	protected void createFlac() {

		ProcessBuilder processBuilder = new ProcessBuilder(flacPath, "-f",
				WAVE_FILE);

		try {
			processBuilder.start();
		} catch (IOException e) {
			System.out.println("An error occured while encoding to FLAC: ");
			e.printStackTrace();
		}

	}

	protected List<String> recognizeSpeech() {
		ArrayList<String> result = new ArrayList<String>();		
		
		ASRRequest request = null;

		request = new ASRRequest();
		
		String requestLanguageCode = ASRRequest.LANG_en_US;

		try {
			// request at recognizer
			request.request(FLAC_FILE, requestLanguageCode);
			ASRResult asrResult = request.getAsrResult();

			for (Hypothesis hypothesis : asrResult.getHypotheses()) {
				result.add(hypothesis.getUtterance());
			}
			
			return result;
			// show prepared result in GUI
//			textResult.setText(resultText.toString());
		} catch (URISyntaxException e) {
			System.out.println("An error occured when requesting the Google Speech-API: ");
			e.printStackTrace();
		} catch (ParseException e) {
			System.out.println("An error occured when parsing the result of the ASR: ");
			e.printStackTrace();
		}
		return null;
	}

	protected void stopCaptureAudio() {
		// stop capturing
		targetDataLine.stop();
		targetDataLine.close();
		
		createFlac();
	}
	
	

	class CaptureThread implements Runnable {

		@Override
		public void run() {
			AudioFileFormat.Type audioType = AudioFileFormat.Type.WAVE;
			File outputFile = new File(WAVE_FILE);

			try {
				targetDataLine.open(audioFormat);
				targetDataLine.start();

				AudioSystem.write(new AudioInputStream(targetDataLine),
						audioType, outputFile);

			} catch (LineUnavailableException e) {
				System.out
						.println("An error occured while opening the audio device: ");
				e.printStackTrace();
			} catch (IOException e) {
				System.out
						.println("An error occured when writing the output file: ");
				e.printStackTrace();
			}
		}
	}
}
