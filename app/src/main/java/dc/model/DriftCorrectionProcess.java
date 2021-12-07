package dc.model;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import dc.step.*;

public class DriftCorrectionProcess extends Process {
	private static final Logger logger = Logger.getLogger(DriftCorrectionProcess.class.getName());
	private dc.step.DriftCorrectionStep driftCorrection;
	private SaveImage saveImage;
	private Flag interruptionFlag;
	private FileHandler fh;
	
	public DriftCorrectionProcess(Flag interruptionFlag2) {
		super(interruptionFlag2);
		this.interruptionFlag = interruptionFlag2;
		mySteps = new LinkedList<ProcessStep>();
		outputStepIndex = new LinkedList<Integer>();
		mySteps.add(new dc.step.ImageReader("png"));
		driftCorrection = new dc.step.DriftCorrectionStep();
		mySteps.add(driftCorrection);
		saveImage = new SaveImage();
		mySteps.add(saveImage);
		outputStepIndex.add(mySteps.size()-1);
//		logger.fine("dirft correction process created");
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
		DriftCorrectionProcess newProcess = new DriftCorrectionProcess(interruptionFlag);
		newProcess.setFileHandler(fh);
		return newProcess;
	}

	@Override
	public String getInputType() {
		return mySteps.get(0).getInputType();
	}
	
	public void initDriftCorrection(List<Integer> xDrift, List<Integer> yDrift, List<String> saveFiles, int padTop, int padBottom, int padLeft, int padRight, int ROI[]) {
		assert (xDrift.size() == yDrift.size());
		assert (xDrift.size() == saveFiles.size());
		driftCorrection.initialise(yDrift, xDrift, padTop, padBottom, padLeft, padRight, ROI);
		saveImage.initialise(saveFiles);
	}

}
