package dc.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import dc.step.*;

public class TemplateMatchingProcess extends Process {
	private static final Logger logger = Logger.getLogger(TemplateMatchingProcess.class.getName());
	private boolean blur;
	private Flag interruptionFlag;
	private FileHandler fh;
//	private step.TemplateMatching templateMatchingStep;
	private dc.step.TemplateMatching templateMatchingStep;
	
	public TemplateMatchingProcess(boolean blur, Flag interruptionFlag2) {
		super(interruptionFlag2);
		this.interruptionFlag = interruptionFlag2;
//		System.out.println(interruptionFlag.get());
		this.blur = blur;
		mySteps = new LinkedList<ProcessStep>();
		outputStepIndex = new LinkedList<Integer>();
		mySteps.add(new dc.step.ImageReader("png"));
		if (blur) {
			mySteps.add(new dc.step.GaussianImage(5, 3));
		}
//		templateMatchingStep = new step.TemplateMatching();
		templateMatchingStep = new dc.step.TemplateMatching();
		mySteps.add(templateMatchingStep);
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		for (ProcessStep step: mySteps) {
			step.setFileHandler(fh);
		}
		this.fh = fh;
	}
	
	public void setTemplate(double template[][]) {
		templateMatchingStep.initialise(template);
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