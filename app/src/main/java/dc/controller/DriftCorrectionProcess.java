package dc.controller;

import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import dc.model.BooleanModel;
import dc.step.*;

public class DriftCorrectionProcess extends Process {
	private static final Logger logger = Logger.getLogger(DriftCorrectionProcess.class.getName());
	private dc.step.DriftCorrectionStep driftCorrection;
	private ComputeFileName outputFileName;
	private BooleanModel interruptionFlag;
	private boolean overwriteFlag;
	private FileHandler fh;
	
	public DriftCorrectionProcess(BooleanModel interruptionFlag2, boolean overwriteFlag) {
		super(interruptionFlag2);
		this.interruptionFlag = interruptionFlag2;
		this.overwriteFlag = overwriteFlag;
		//read image
		addStep(new dc.step.ImageReader("png"));
		//check dimension
		addStep(new CheckDimension());
		//drift correction
		driftCorrection = new dc.step.DriftCorrectionStep();
		addStep(driftCorrection);
		//compute filename
		outputFileName = new ComputeFileName();
		addStep(outputFileName);
		//if necessary, check overwrite
		if (!overwriteFlag) {
			addStep(new CheckFileNotExist());
		}
		//save image
		addStep(new SaveImage());
		//check image is saved
		addStep(new CheckFileExist());
//		logger.fine("drift correction process created");
	}
	
	public void setFileHandler(FileHandler fh) {
		this.fh = fh;
		logger.addHandler(fh);
		for (ProcessStep step: mySteps) {
			step.setFileHandler(fh);
		}
	}

	@Override
	public Process copy() {
		DriftCorrectionProcess newProcess = new DriftCorrectionProcess(interruptionFlag, overwriteFlag);
		newProcess.setFileHandler(fh);
		return newProcess;
	}

	@Override
	public String getInputType() {
		return mySteps.get(0).getInputType();
	}
	
	public void initDriftCorrection(List<Integer> xDrift, List<Integer> yDrift, List<String> saveFiles, int padTop, int padBottom, int padLeft, int padRight, int ROI[], String folder, int startingIdx) {
		assert (xDrift.size() == yDrift.size());
		assert (xDrift.size() == saveFiles.size());
		super.initialise(folder, startingIdx);
		driftCorrection.initialise(yDrift, xDrift, padTop, padBottom, padLeft, padRight, ROI);
		outputFileName.initialise(saveFiles);
	}

}
