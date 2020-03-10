import java.io.Serializable;

/**
 * A serializable object that can contain any type of message between clients.
 */
public class Data extends Object implements Serializable {
	private String data;

	private static final long serialVersionUID = -557292914719924865L;
	
	public Data(String s) {
		this.data = s;
	}

	/**
	 * Get the data's contents
	 */
	public String getData() {
		return this.data;
	}
	
}
