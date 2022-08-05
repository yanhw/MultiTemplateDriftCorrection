package dc.step;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class CheckFileNotExist extends InterruptableStep {
	private static final String name = "check file exist";
	private static final Logger logger = Logger.getLogger(CheckFileNotExist.class.getName());

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
		if (f.isFile()) {
			message = "The file " + filename + " already exists.\n"
					+ "Other files in this folder might also be overwritten in the process.\n"
					+ "If you wish to overwrite these files, select allow overwrite option.\n"
					+ "If you wish to keep existing files, change the save location or \n"
					+ "rename existing files."
					+ "Terminating the process.";
			interrupt();
			logger.severe(message);
		}
		return input;
	}

	@Override
	public ProcessStep copy() {
		return new CheckFileNotExist();
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public String getName() {
		return name;
	}
}
