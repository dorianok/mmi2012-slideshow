package mmislideshow;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Value Object for the response data of the Google Voice Search API.
 * 
 * @author Stefan Schmidt (s.schmidt@tu-berlin.de)
 *
 */
public class ASRResult {
	
	/**
	 * <p>
	 * Status flag.
	 * </p>
	 * <ul>
	 * <li>-1: not set (set by us, does not come from the API)</li>
	 * <li>0: No error occurred</li>
	 * <li>5: Error (Probably the service couldn't process the audio data.)</li>
	 * </ul>
	 * 
	 */
	private long status = -1;
	
	/**
	 * ID of the response.
	 */
	private String id;
	
	/**
	 * All hypotheses provided. Just one at the moment, but who knows...
	 */
	private List<Hypothesis> hypotheses = new ArrayList<Hypothesis>();
	
	/**
	 * The language code used for the request.
	 */
	private String languagCode;

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Hypothesis> getHypotheses() {
		return hypotheses;
	}

	public void setHypotheses(List<Hypothesis> hypotheses) {
		this.hypotheses = hypotheses;
	}

	public String getLanguagCode() {
		return languagCode;
	}

	public void setLanguagCode(String languagCode) {
		this.languagCode = languagCode;
	}

	@Override
	public String toString() {
		return "ASRResult [status=" + status + ", id=" + id + ", hypotheses="
				+ hypotheses + ", languagCode=" + languagCode + "]";
	}

}
