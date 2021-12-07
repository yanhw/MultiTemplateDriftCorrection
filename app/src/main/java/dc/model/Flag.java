package dc.model;

public class Flag {
	private boolean flag;
	
	public Flag() {
		flag = false;
	}
	
	public Flag(boolean flag) {
		this.flag = flag;
	}
	
	public void set(boolean flag) {
		this.flag = flag;
	}
	
	public boolean get() {
		return this.flag;
	}
}
