package dc.step;

import java.util.logging.FileHandler;

public interface ProcessStep {
	public String getName();
	public void initialise();
	public ImageData run(ImageData input);
	public ProcessStep copy();
	public void setFileHandler(FileHandler fh);
	
	public default String getInputType() {
		return null;
	}
}
