package dc.controller;

import java.util.LinkedList;
import java.util.logging.*;

import dc.model.BooleanModel;
import dc.step.ImageData;
import dc.step.InterruptableStep;
import dc.step.OutputNameStep;
import dc.step.ProcessStep;

/*
 * This abstract class is designed to perform customised image processing steps
 * for a given image sequence, and it assumes that user uses multi-threading
 * 
 * When implementing concrete process, first call super() in the constructor, then
 * add desired process steps in mySteps in the sequence that they should be performed
 * in each image, additionally:
 * 		1. ImageReader should always be the first step, and it should be interruptableStep
 * 		2. after imageReader, it's usually good to perform checkDimension to ensure
 * 			input image are in the same dimension. it is also an interruptableStep
 * 		3. add index of all steps that expects exception in interruptableStepIndex,
 * 			the interrutionFlag halts the process in all threads
 * 		4. before every saveImage, there should be outputNameStep, and the index of 
 * 			outputNameStep should be in outputStepIndex
 */
public abstract class Process {
	private static final Logger logger = Logger.getLogger(Process.class.getName());
	protected LinkedList<ProcessStep> mySteps;
	protected LinkedList<Integer> interruptableStepIndex;
	protected LinkedList<Integer> outputStepIndex;
	private BooleanModel interruptionFlag = new BooleanModel();
	private String message = null;
	
	// this flag is used to stop the process. it can be replaced by 
	// a common flag that is shared with gui.
	public Process(BooleanModel interruptionFlag) {
		this.interruptionFlag = interruptionFlag;
		mySteps = new LinkedList<ProcessStep>();
		interruptableStepIndex = new LinkedList<Integer>();
		outputStepIndex = new LinkedList<Integer>();
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	// this method resets all processSteps so that it is ready for new movie
	public void initialise(String outputDir, int startingIdx) {
		for (ProcessStep myProcess: mySteps) {
			myProcess.initialise();
		}
		for (Integer idx: interruptableStepIndex) {
			((InterruptableStep) mySteps.get(idx)).setInterruptionFlag(interruptionFlag);
			((InterruptableStep) mySteps.get(idx)).resetMessage();
		}
		message = null;
		for (Integer idx: outputStepIndex) {
			((OutputNameStep) mySteps.get(idx)).initialise(outputDir, startingIdx);
		}
	}
	
	public void addStep(ProcessStep step) {
		mySteps.add(step);
		if (step instanceof InterruptableStep) {
			interruptableStepIndex.add(mySteps.size()-1);
		}
		if (step instanceof OutputNameStep) {
			outputStepIndex.add(mySteps.size()-1);
		}
	}
	
	public void run(String inputFileName) {
		logger.fine("worker thread started");
		ImageData myData = new ImageData(inputFileName);
		for (ProcessStep myStep: mySteps) {
			myData = myStep.run(myData);
			// interruptionFlag can change in interruptableStep, or due to external interruption
			if (interruptionFlag.get()) {
				logger.info("process is interrupted");
				if (myStep instanceof InterruptableStep) {
					String message = ((InterruptableStep) myStep).getMessage();
					if (message != null) {
						this.message = message;
					}
				}		
				break;
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
			System.gc();
		}
		if (!interruptionFlag.get()) {
			logger.fine("worker thread finished");
		}
	}
	
	public abstract Process copy();
	
	public abstract String getInputType();
	
	public String getMessage() {
		return message;
	}
}
