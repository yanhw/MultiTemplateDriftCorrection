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
			message = "Something is wrong... Input data is null... Terminating the process";
			interrupt();
			logger.severe(message);
			return input;
		}
		String filename = input.getOutputString();
		if (filename == null) {
			message = "Something is worng! \n"
					+ "Output filname for the image " + input.getString() + " is not found.\n"
					+ "Terminating the process";
			interrupt();
			logger.severe(message);
			return input;
		}
		File f = new File(filename);
		if (!f.isFile()) {
			message = "Failed to create the output file " + filename + "\n"
					+ "Terminating the process";
			interrupt();
			logger.severe(message);
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
