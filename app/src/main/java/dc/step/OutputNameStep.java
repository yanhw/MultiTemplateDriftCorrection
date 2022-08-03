package dc.step;

import java.util.logging.*;


public abstract class OutputNameStep implements ProcessStep {
	private static final Logger logger = Logger.getLogger(OutputNameStep.class.getName());
	protected String name = "output process step";

	
	protected ImageData myImage;
	
	public OutputNameStep() {
		
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public String getName() {
		return this.name;
	}
	
	public void initialise() {
		
	}
	
	public abstract void initialise(String folder, int startingIdx);
	
	protected boolean hasValidImage(ImageData image) {
		if (image == null) {
			return false;
		}
		if (image.getImage() == null) {
			return false;
		}
		logger.finer("has image");
		return true;
	}
}
