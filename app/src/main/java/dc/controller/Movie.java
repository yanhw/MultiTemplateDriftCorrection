package dc.controller;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoundedRangeModel;

import dc.model.*;
import dc.utils.Constants;
import dc.utils.FileSystem;

/*
 *  This is centre controller for data model. It manages interaction and data flow between
 *  template matching, drift editing and drift correction.
 */

public class Movie {
	private static final Logger logger = Logger.getLogger(Movie.class.getName());
	
	private TemplateMatchingManager templateMatching;
	private DriftManager driftManager;
	private DriftCorrectionManager driftCorrection;
	
	private FileListModel fileList;
	private MovieStateModel myState;
	private TextModel inputDir;
	private TextModel saveDir;
	private TextModel myWarning;
	private AtomicInteger maxThreads;
	
	public Movie() {
		logger.setLevel(Level.FINE);
		templateMatching = new TemplateMatchingManager();
		driftManager = new DriftManager();
		driftCorrection = new DriftCorrectionManager();
		
		myState = new MovieStateModel();
		fileList = new FileListModel();
		inputDir = new TextModel();
		saveDir = new TextModel();
		driftCorrection.setSaveDir(saveDir);
		@SuppressWarnings("serial")
		TemplateMatchingSegmentModel templateMatchingSegmentModel = new TemplateMatchingSegmentModel() {
			@Override
			public boolean isCellEditable(int row, int column) {       
				return false;
			}
		};
		templateMatching.setTableModel(templateMatchingSegmentModel);
		DriftModel driftModel = new DriftModel();
		DriftSectionModel sectionModel = new DriftSectionModel();
		driftManager.setTableModel(driftModel, sectionModel);
	}
	
	// getters and setters for GUI init
	protected void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		templateMatching.setFileHandler(fh);
		driftManager.setFileHandler(fh);
		driftCorrection.setFileHandler(fh);
	}
	
	protected void setGUIHelper(BooleanModel interrupt, BoundedRangeModel progress, TextModel myWarning) {
		this.myWarning = myWarning;
		templateMatching.setInterruptionFlag(interrupt);
		driftCorrection.setInterruptionFlag(interrupt);
		templateMatching.setProgressModel(progress);
		driftCorrection.setProgressModel(progress);
		templateMatching.setWarningModel(myWarning);
		driftManager.setWarningModel(myWarning);
		driftCorrection.setWarningModel(myWarning);
	}
	
	protected void setDefaultParameters(AtomicInteger gaussianKernel, AtomicInteger gaussianInteration, AtomicInteger templateMatchingMethod,
			AtomicInteger maxThreads2, AtomicInteger maxDegree) {
		this.maxThreads = maxThreads2;
		templateMatching.setDefaultParameters(gaussianKernel, gaussianInteration, templateMatchingMethod, maxThreads2);
		driftManager.setDefaultParameters(maxDegree);
		driftCorrection.setDefaultParameters(maxThreads2);
	}
	
	protected void setMaxWorkerThread(int number) {
		if (number < 0) {
			logWarning("Invalid input: " + number + "\n"
					+ "Number of threads must be a positive integer!\n");
			return;
		}
		maxThreads.set(number);
	}
	
	protected MovieStateModel getMovieStateModel() {
		return myState;
	}
	
	protected FileListModel getRawFileList() {
		return fileList;
	}
	
	protected FileListModel getCorrectedFileList() {
		return driftCorrection.getSaveListModel();
	}
	
	protected TemplateMatchingSegmentModel getTemplateTableModel() {
		return templateMatching.getTableModel();
	}
	
	protected DriftModel getDriftModel() {
		return driftManager.getDriftModel();
	}
	
	protected DriftSectionModel getDriftSectionModel() {
		return driftManager.getDriftSectionModel();
	}
	
	protected TextModel getSaveDirModel() {
		return saveDir;
	}
	
	protected TextModel getInputDirModel() {
		return inputDir;
	}
	
	// call this method when state might be changed
	private void checkState() {
		if (!isIOReady()) {
			logger.info("state set to: INIT");
			myState.setValue(MovieStateModel.INIT);
		} else if (!isDriftReady()) {
			logger.info("state set to: TEMPLATE_MATCHING");
			myState.setValue(MovieStateModel.TEMPLATE_MATCHING);
		} else if (!driftCorrectionDone()) {
			logger.info("state set to: DRIFT_EDIT");
			myState.setValue(MovieStateModel.DRIFT_EDIT);
		} else {
			logger.info("state set to: DRIFT_CORRECTION");
			myState.setValue(MovieStateModel.DRIFT_CORRECTION);
		}
	}
	

	protected void reset() {
		fileList.clearFiles();
		saveDir.setText("");
		inputDir.setText("");
		templateMatching.reset();
		driftManager.reset();
		driftCorrection.reset();
		checkState();
	}
	
	private void logWarning(String message) {
		if (myWarning != null) {
			myWarning.setText(message);
		}
		logger.info(message);	//info because this is well handled
	}
	/////////////////////////////////////////////////////////////////////
	///////////////////////////// IO state //////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	protected void setSrcDir(String folder, String filetype) {
		boolean flag = false;
		for (int i = 0; i < Constants.INPUT_FORMAT.length; i++) {
			if (Constants.INPUT_FORMAT[i].equals(filetype)) {
				flag = true;
			}
		}
		if (!flag) {
			logger.severe("invalid filetype :" + filetype);
			return;
		}
		filetype = "." + filetype;
		List<Path> fileList = FileSystem.getFiles(Paths.get(folder), filetype);
		if (fileList == null) {
			logWarning("invalid inputDir :" + folder);
			return;
		}
		if (fileList.isEmpty()) {
			logWarning("no image files of type " + filetype + " found in :" + folder);
			return;
		}
		if (fileList.size() < 2) {
			logWarning("input folder must have at least 2 images of type " + filetype);
			return;
		}
		logger.fine("setting new folder and init the movie");
		this.fileList.setFiles(fileList);
		inputDir.setText(folder);
		
		// note: initialise variables here, not in constructor, because the movie can change
		templateMatching.init(fileList);
		driftManager.init(fileList.size());	
		driftCorrection.init(fileList);
		checkState();
	}
	
	protected boolean setSaveDir(String folder) {
		File file = new File(folder);
		if (file.canWrite()) {
			saveDir.setText(folder);
		} else {
			logWarning("Cannot set save folder at : " + folder +".\n"
					+ "It appears you cannot modify this folder");
			return false;
		}
		checkState();
		return true;
	}
	
	protected TextModel getInputFolder() {
		return inputDir;
	}
	
	protected TextModel getSaveFolder() {
		return saveDir;
	}
	
	private boolean isIOReady() {
		if (fileList == null ||fileList.getSize() <= 2) {
			logger.fine("need a movie with at least 2 frames");
			return false;
		}
		if (saveDir.getText().length() == 0) {
			logger.fine("no saveDir");
			return false;
		}
		return true;
	}

	/////////////////////////////////////////////////////////////////////
	////////////////////// template matching ////////////////////////////
	/////////////////////////////////////////////////////////////////////
	protected void setTMMethod(int method) {
		templateMatching.setTMMethod(method);
	}

	protected void setGaussianOption(int size, int iteration) {
		templateMatching.setGaussianOption(size, iteration);
	}

	
	protected void setSegmentFrame(int frameNumber) {
		templateMatching.setSegmentFrame(frameNumber);
	}

	protected void removeSegmentFrame(int segmentIndex) {
		templateMatching.removeSegmentFrame(segmentIndex);
	}
	
	protected boolean setTemplate(int frameNumber, int[] ROI) {
		Boolean res = templateMatching.setROI(frameNumber, ROI);
		return res; 
	}
	
	protected void removeTemplate(int targetIndex) {
		templateMatching.removeROI(targetIndex);
	}

	protected boolean templageMatchingPreRunValidation() {
		return templateMatching.templageMatchingPreRunValidation();
	}
	
	protected void runTemplateMatching(boolean blur) {
		assert templageMatchingPreRunValidation();
		templateMatching.run(saveDir.getText(), blur);
	}
	

	protected void afterTemplateMatching() {
//		logger.info("at after template matching");
		driftManager.setDrifts(templateMatching.tempXDrift, templateMatching.tempYDrift);
		driftManager.saveFittedDrift(saveDir.getText());
		saveRawDrift();
		checkState();
//		logger.info("end of after template matching");
	}
	
	private void saveRawDrift() {
		driftManager.saveRawDrift(saveDir.getText());
	}
	
	private boolean isDriftReady() {
		return driftManager.isDriftReady();
	}
	
	protected boolean setDriftCsv(String filename) {
		if (driftManager.setDrifts(filename)) {
			checkState();
			return true;
		}	
		return false;
	}
	
	/////////////////////////////////////////////////////////////////////
	////////////////////// drift editing ////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	protected void setMaxFittingDegree(int degree) {
		driftManager.setMaxDegree(degree);
	}
	
	// setters in this section is for testing and for non GUI mode
	protected float[] getXDrift() {
		return driftManager.getXDrift();
	}
	
	protected float[] getYDrift() {
		return driftManager.getYDrift();
	}
	
	protected float[] getXFittedDrift() {
		return driftManager.getFittedXDrift();
	}
	
	protected float[] getYFittedDrift() {
		return driftManager.getFittedYDrift();
	}
	
//	public void setFitDegree(int sectionIndex, int degree) {
//		driftManager.setFitDegree(sectionIndex, degree);
//	}
//	
//	public void setXDrift(int frameNumber, float newVal) {
//		driftManager.setXDrift(frameNumber, newVal);
//	}
//	
//	public void setYDrift(int frameNumber, float newVal) {
//		driftManager.setYDrift(frameNumber, newVal);
//	}
	
	protected void addCuttingPoint(int frameNumber) {
		driftManager.addCuttingPoint(frameNumber);
	}
	
	protected void removeCuttingPoint(int sectionIndex) {
		driftManager.removeCuttingPoint(sectionIndex);
	}
	
	protected void fitDrift(int start, int end, int directionOption) {
		driftManager.fitDrift(start, end, directionOption);
	}
	
	/////////////////////////////////////////////////////////////////////
	////////////////////// drift correction /////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	protected boolean driftCorrectionPreRunValidation() {
		if (driftManager.isDriftReady()) {
			return true;
		}
		return false;
	}

	protected void runDriftCorrection(boolean blurFlag, boolean overwriteFlag) {
		assert fileList.getSize() > 0;
		if (blurFlag) {
			driftCorrection.run(getXFittedDrift(), getYFittedDrift(), overwriteFlag);
		} else {
			driftCorrection.run(getXDrift(), getYDrift(), overwriteFlag);
		}
	}

	
	protected void afterDriftCorrection() {
		checkState();
	}

	private boolean driftCorrectionDone() {
		if (driftCorrection.isDone()) {
			return true;
		}
		return false;
	}

}
