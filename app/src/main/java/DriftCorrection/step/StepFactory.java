package DriftCorrection.step;

import java.util.logging.*;

// factory method to generate process steps
public class StepFactory {
	private static final Logger logger = Logger.getLogger(StepFactory.class.getName());
	
	
	public static ProcessStep createInputStep(String fileType) {
		if (fileType == null) {
			logger.severe("null filetye");
			return null;
		}
		switch (fileType) {
			case "png":
			case "jpeg":
			case "bmp":
				// to implement
				return null;
			default:
				logger.severe("unregonised input format: " + fileType);
				return null;
		}
	}
	
	public static ProcessStep createStep(String name) {
		return null;
	}
	
	public static ProcessStep createStep(String name, int value) {
		return null;
	}
	
	public static ProcessStep createStep(String name, int value, int value2) {
		return null;
	}
	
}
