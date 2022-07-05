package dc.controller;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import dc.model.*;
import dc.utils.FileSystem;

public class Movie {
	private static final Logger logger = Logger.getLogger(Movie.class.getName());
	
	private String saveDir = null;
	private List<Path> fileList;
	
	private ImageArrayReader imageReader;
	private TemplateMatchingManager templateMatching;
	private DriftManager driftManager;
	private DriftCorrectionManager driftCorrection;
	private MovieStateModel myState;
	
	public Movie() {
		logger.setLevel(Level.FINE);
		imageReader = new ImageArrayReader("png");
		templateMatching = new TemplateMatchingManager();
		driftManager = new DriftManager();
		driftCorrection = new DriftCorrectionManager();
		myState = new MovieStateModel();
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
	
	// getters and setters for GUI
	protected void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		imageReader.setFileHandler(fh);
		templateMatching.setFileHandler(fh);
		driftManager.setFileHandler(fh);
		driftCorrection.setFileHandler(fh);
	}
	
	protected void setInterruptionFlag(Flag interrupt) {
		templateMatching.setInterruptionFlag(interrupt);
		driftCorrection.setInterruptionFlag(interrupt);
	}
	
	protected MovieStateModel getMovieStateModel() {
		return myState;
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
	
	// call this method when state might be changed
	@SuppressWarnings("static-access")
	protected void checkState() {
		if (!isIOReady()) {
			logger.info("state set to: INIT");
			myState.setValue(myState.INIT);
		} else if (!isDriftReady()) {
			logger.info("state set to: TEMPLATE_MATCHING");
			myState.setValue(myState.TEMPLATE_MATCHING);
		} else if (!driftCorrectionDone()) {
			logger.info("state set to: DRIFT_EDIT");
			myState.setValue(myState.DRIFT_EDIT);
		} else {
			logger.info("state set to: DRIFT_CORRECTION");
			myState.setValue(myState.DRIFT_CORRECTION);
		}
	}
	

	
	/////////////////////////////////////////////////////////////////////
	///////////////////////////// IO state //////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	protected void setSrcDir(String folder) {
		fileList = FileSystem.getFiles(Paths.get(folder), ".png");
		if (fileList == null) {
			logger.warning("invalid inputDir :" + folder);
			return;
		}
		if (fileList.isEmpty()) {
			logger.fine("no files found in :" + folder);
			return;
		}
		if (fileList.size() < 2) {
			logger.fine("need at least 2 frames");
			return;
		}
		// note: initialise variables here, not in constructor, because the movie can change
		templateMatching.init(fileList);
		driftManager.init(fileList.size());	
		driftCorrection.init(fileList);
	}
	
	protected void setSaveDir(String folder) {
		File file = new File(folder);
		if (file.canWrite()) {
			saveDir = folder;
		} else {
			logger.info("cannot set save folder at : " + folder);
		}
	}
	
	protected List<Path> getFileList() {
		return fileList;
	}
	
	protected String getSaveFolder() {
		return saveDir;
	}
	
	private boolean isIOReady() {
		if (fileList == null ||fileList.size() <= 2) {
			logger.fine("need at least 2 frames");
			return false;
		}
		if (saveDir == null) {
			logger.fine("no saveDir");
			return false;
		}
		
		return true;
	}

	/////////////////////////////////////////////////////////////////////
	////////////////////// template matching ////////////////////////////
	/////////////////////////////////////////////////////////////////////

	protected void setSegmentFrame(int frameNumber) {
		templateMatching.setSegmentFrame(frameNumber);
	}

	protected void removeSegmentFrame(int segmentIndex) {
		templateMatching.removeSegmentFrame(segmentIndex);
	}
	
	protected boolean setTemplate(int frameNumber, int[] ROI) {
		String filename = fileList.get(frameNumber).toString();
		double[][] image = imageReader.read(filename);
		Boolean res = templateMatching.setROI(frameNumber, ROI, image);
		return res; 
	}
	
	protected void removeTemplate(int targetIndex) {
		templateMatching.removeROI(targetIndex);
	}

//	protected MovieSegment getMovieSegment(int frameNumber) {
//		return templateMatching.getSegment(frameNumber);
//	}

	protected boolean templageMatchingPreRunValidation() {
		return templateMatching.templageMatchingPreRunValidation();
	}
	
	protected void runTemplateMatching(boolean blur) {
		templateMatching.run(saveDir, blur);
	}
	

	protected void afterTemplateMatching() {
//		logger.info("at after template matching");
		driftManager.setDrifts(templateMatching.tempXDrift, templateMatching.tempYDrift);
		driftManager.saveFittedDrift(saveDir);
		saveRawDrift();
//		logger.info("end of after template matching");
	}
	
	protected int getTemplateMatchingProgress() {
		return templateMatching.getProgress();
	}
	
	private void setTemplateMatchingProgress(int num) {
		templateMatching.setProgress(num);
	}
	
	private void saveRawDrift() {
		driftManager.saveRawDrift(saveDir);
	}
	
	private boolean isDriftReady() {
		return driftManager.isDriftReady();
	}
	
	protected void setDriftCsv(String filename) {
		if (driftManager.setDrifts(filename)) {
			setTemplateMatchingProgress(100);
		}	
	}
	
	/////////////////////////////////////////////////////////////////////
	////////////////////// drift editing ////////////////////////////////
	/////////////////////////////////////////////////////////////////////
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
		if (driftManager.isReady()) {
			return true;
		}
		return false;
	}

	protected void runDriftCorrection(boolean blurFlag) {
		// TODO: customise ROI for output images
		String filename = fileList.get(0).toString();
		double[][] image = imageReader.read(filename);
		int[] ROI = new int[4];
		ROI[0] = 0;
		ROI[1] = image.length;
		ROI[2] = 0;
		ROI[3] = image[0].length;
		if (blurFlag) {
			driftCorrection.run(saveDir, getXFittedDrift(), getYFittedDrift(), ROI);
		} else {
			driftCorrection.run(saveDir, getXDrift(), getYDrift(), ROI);
		}
	}

	protected int getDriftCorrectionProgress() {
		return driftCorrection.getProgress();
	}
	
	
	protected List<String> getSaveFiles() {
		return driftCorrection.getSaveFiles();
	}

	private boolean driftCorrectionDone() {
		if (getDriftCorrectionProgress() == 100) {
			return true;
		}
		return false;
	}

}
