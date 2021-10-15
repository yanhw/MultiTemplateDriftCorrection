package DriftCorrection.model;

import java.util.LinkedList;
import java.util.logging.*;

import DriftCorrection.process.ImageData;
import DriftCorrection.step.OutputProcessStep;
import DriftCorrection.step.ProcessStep;


public abstract class Process {
	// TODO: change protected attributes
	protected LinkedList<ProcessStep> mySteps;
	//~ private LinkedList<ProcessStep> inputSteps;
//	private LinkedList<ProcessStep> outputSteps;
	protected LinkedList<Integer> outputStepIndex;
	private static final Logger logger = Logger.getLogger(Process.class.getName());
	private Boolean interruptionFlag;
	
	public Process() {
		interruptionFlag = false;
	}
	
	public Process(Boolean interruptionFlag) {
		this.interruptionFlag = interruptionFlag;
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	// this method resets all processSteps so that it is ready for new movie
	public void initialise(String outputDir, int startingIdx) {
		for (ProcessStep myProcess: mySteps) {
			myProcess.initialise();
		}
		for (Integer idx: outputStepIndex) {
			((OutputProcessStep) mySteps.get(idx)).initialise(outputDir, startingIdx);
		}
	}

	public void run(String inputFileName) {
		logger.fine("worker thread started");
		ImageData myData = new ImageData(inputFileName);
		for (ProcessStep myProcess: mySteps) {
			if (interruptionFlag==true) {
				logger.info("process is interrupted");
				break;
			}
			myData = myProcess.run(myData);
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		}
		logger.fine("worker thread finished");
	}
	
	public abstract Process copy();
	
	public abstract String getInputType();
	
}
