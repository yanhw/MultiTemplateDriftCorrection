package dc.controller;

import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import dc.model.BooleanModel;
import dc.step.*;

public class TemplateMatchingProcess extends Process {
	private static final Logger logger = Logger.getLogger(TemplateMatchingProcess.class.getName());
	private boolean blur;
	private BooleanModel interruptionFlag;
	private FileHandler fh;
//	private step.TemplateMatching templateMatchingStep;
	private dc.step.TemplateMatching templateMatchingStep;
	private GaussianImage gaussianStep;
	
	public TemplateMatchingProcess(boolean blur, BooleanModel interruptionFlag2) {
		super(interruptionFlag2);
		this.interruptionFlag = interruptionFlag2;
		this.blur = blur;
		//read image
		addStep(new dc.step.ImageReader("png"));
		//check dimension
		addStep(new CheckDimension());
		//gaussian blur
		if (blur) {
			gaussianStep = new dc.step.GaussianImage();
			addStep(gaussianStep);
		}
		//template matching
		templateMatchingStep = new dc.step.TemplateMatching();
		addStep(templateMatchingStep);
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		for (ProcessStep step: mySteps) {
			step.setFileHandler(fh);
		}
		this.fh = fh;
	}
	
	public void initialise(double template[][], int gaussianKernel, 
			int gaussianIteration, int templateMatchingMethod) {
		templateMatchingStep.initialise(template);
		templateMatchingStep.init(templateMatchingMethod);
		if (blur) {
			gaussianStep.init(gaussianKernel, gaussianIteration);
		}
	}
	
	public List<Integer> getXDrift() {
		return templateMatchingStep.getColDrift();
	}
	
	public List<Integer> getYDrift() {
		return templateMatchingStep.getRowDrift();
	}

	@Override
	public Process copy() {
		TemplateMatchingProcess newProcess = new TemplateMatchingProcess(blur, interruptionFlag);
		newProcess.setFileHandler(fh);
		return newProcess;
	}

	@Override
	public String getInputType() {
		return mySteps.get(0).getInputType();
	}

}