package dc.step;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class CheckFileExist extends InterruptableStep {
	private static final String name = "check file exist";
	private static final Logger logger = Logger.getLogger(CheckFileExist.class.getName());

	@Override
	public void initialise() {
		
	}
	
	@Override
	public ImageData run(ImageData input) {
		if (input == null) {
			logger.severe("empty input imageData");
			interrupt();
			return input;
		}
		String filename = input.getOutputString();
		if (filename == null) {
			logger.severe("empty output filename");
			interrupt();
			return input;
		}
		File f = new File(filename);
		if (!f.isFile()) {
			logger.severe("file not created: " + filename);
			interrupt();
		}
		return input;
	}

	@Override
	public ProcessStep copy() {
		return new CheckFileExist();
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public String getName() {
		return name;
	}
}
