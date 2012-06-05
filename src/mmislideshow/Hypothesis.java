package mmislideshow;

/**
 * Value Object for a hypothesis. 
 * 
 * @author Stefan Schmidt (s.schmidt@tu-berlin.de)
 *
 */
public class Hypothesis {
	
	/**
	 * Recognized text.
	 */
	private String utterance;
	
	/**
	 * Confidence value of the utterance. Between 0.0 and 1.0.
	 */
	private double confidence;

	public String getUtterance() {
		return utterance;
	}

	public void setUtterance(String utterance) {
		this.utterance = utterance;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	@Override
	public String toString() {
		return "Hypothese [utterance=" + utterance + ", confidence="
				+ confidence + "]";
	}

}
