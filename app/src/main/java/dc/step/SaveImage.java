package dc.step;

import java.util.logging.*;

import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;


public class SaveImage extends SimpleProcessStep {
	
	private static final String name = "save image";
	private static final Logger logger = Logger.getLogger(SaveImage.class.getName());
	
	// TODO: separate filename and save image, check for overwrite can be done at filename step
//	private boolean checkOverwrite = false;		// whether to give warning
	private boolean expectNull = false;			// this is true if there is averaging step
	
	@Override
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	@Override
	public ImageData run(ImageData myImage) {
		logger.fine(getName());
		if (myImage == null) {
			if (!expectNull) {
				logger.severe("image data object is not created");
			}
			return null;
		}
		Mat inputImage = myImage.getImage();
		if (inputImage == null) {
			logger.severe("image is not found in ImageData");
			return myImage;
		}
		String filename = myImage.getOutputString();
		if (filename == null) {
			logger.severe("output filename is not found in ImageData");
			return myImage;
		}
		imwrite(filename, inputImage);
		return myImage;
	}
	
	
	public String getName() {
		return name;
	}
	
	@Override
	public ProcessStep copy() {
		return new SaveImage();
	}
}
