import java.io.Serializable;

public class Data extends Object implements Serializable {
	private String data;

	private static final long serialVersionUID = -557292914719924865L;
	
	public Data(String s) {
		this.data = s;
	}
		
	public String getData() {
		return this.data;
	}
	
}
