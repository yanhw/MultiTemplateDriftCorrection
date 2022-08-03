package dc.step;

import java.util.List;
import java.util.logging.Logger;

import dc.utils.FileSystem;

public class ComputeFileName extends OutputNameStep {
	private static final String name = "output file name";
	private static final Logger logger = Logger.getLogger(ComputeFileName.class.getName());
	
	private String saveDir = null;				// saving location
	private List<String> saveFiles;
	private int count = 0;						// counter for file name
	private String padString = "000000";

	public void initialise(String folder, int startingIdx) {
		if (folder == null) {
			logger.warning("input folder is null");
			return;
		}
		this.saveDir = folder;
		this.count = startingIdx;
		this.saveFiles = null;
		logger.fine("initialised saveDir at: " + saveDir);
	}
	
	public void initialise(List<String> saveFiles) {
		if (saveFiles == null) {
			logger.warning("saveFiles is null");
		}
		this.saveFiles = saveFiles;
		this.count = 0;
	}

	@Override
	public ImageData run(ImageData input) {
		if (saveDir == null && saveFiles == null) {
			logger.severe("saving option is not initialised");
			return myImage;
		}
		String filename;
		if (saveFiles == null) {
			String filenum = String.valueOf(count);
			filename = (padString + filenum).substring(filenum.length()) + ".png";
			filename = FileSystem.joinPath(saveDir, filename);
		} else {
			filename = saveFiles.get(count);
		}
		input.setOutputString(filename);
		count++;
		return input;
	}

	@Override
	public ProcessStep copy() {
		return new ComputeFileName();
	}
	
	public String getName() {
		return name + " saving to: " + saveDir;
	}
}
