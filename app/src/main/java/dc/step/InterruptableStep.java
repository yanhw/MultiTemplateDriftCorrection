package dc.step;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

import dc.model.BooleanModel;

public abstract class InterruptableStep implements ProcessStep {
	private static final Logger logger = Logger.getLogger(InterruptableStep.class.getName());
	protected String name = "output process step";
	
	private BooleanModel interruptionFlag;
	
	@Override
	public String getName() {
		return name;
	}

	public void setInterruptionFlag(BooleanModel interruptionFlag) {
		this.interruptionFlag = interruptionFlag;
	}
		
	public void interrupt() {
		interruptionFlag.set(true);
	}

	@Override
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}

}
