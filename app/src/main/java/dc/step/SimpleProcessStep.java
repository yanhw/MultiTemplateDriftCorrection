package dc.step;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

import dc.controller.ImageData;

public abstract class SimpleProcessStep implements ProcessStep {
	protected String name = "simple process step";
	protected static final Logger logger = Logger.getLogger(SimpleProcessStep.class.getName());
	
	protected ImageData myImage;
	
	public SimpleProcessStep() {
		
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public String getName() {
		return this.name;
	}
	
	public void initialise() {
	}
	
	protected boolean hasValidImage(ImageData image) {
		if (image == null) {
			return false;
		}
		if (image.getImage() == null) {
			return false;
		}
		return true;
	}
}
