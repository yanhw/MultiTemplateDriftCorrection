package dc.step;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class CheckDimension extends InterruptableStep {
	private String name = "check dimension";
	private static final Logger logger = Logger.getLogger(CheckDimension.class.getName());
	
	private int height = -1;
	private int width = -1;
	
	@Override
	public String getName() {
		return name + " " + height + " " + width;
	}

	@Override
	public void initialise() {
		height = -1;
		width = -1;
	}

	@Override
	public ImageData run(ImageData input) {
		int imgHeight = input.getImage().arrayHeight();
		int imgWidth = input.getImage().arrayWidth();
		if (height==-1) {
			height = imgHeight;
			width = imgWidth;
		} else if (height != imgHeight || width != imgWidth) {
			message = "All images should have the same dimension.\n"
					+ "detected dimension of the image sequence: height=" + height + ", width=" + width + "\n"
					+ "detected dimension of image " + input.getString() + ": height=" + imgHeight + ", width=" + imgWidth + "\n"
					+ "Terminating the process. Please check input images";
			logger.severe(message);
			interrupt();
		}
		return input;
	}

	@Override
	public ProcessStep copy() {
		return new CheckDimension();
	}

	@Override
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}

}
