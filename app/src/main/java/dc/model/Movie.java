package dc.model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.DefaultTableModel;

import dc.gui_old.DriftEditingPanel.DriftModel;
import dc.gui_old.DriftEditingPanel.DriftSectionModel;
import dc.utils.FileSystem;

public class Movie {
	private static final Logger logger = Logger.getLogger(Movie.class.getName());
	
	public static final int INIT = 0;
	public static final int TEMPLATE_MATCHING = 1;
	public static final int DRIFT_EDIT = 2;
	public static final int DRIFT_CORRECTION = 3;
	public static final int DONE = 4;				// not in use
	private int state = INIT;
	
	private boolean useFittedDrift = true;
	private String saveDir = null;
	private List<Path> fileList;
	
	private ImageArrayReader imageReader;
	private TemplateMatchingManager templateMatching;
	private DriftManager driftManager;
	private DriftCorrectionManager driftCorrection;
	
	public Movie() {
		logger.setLevel(Level.FINE);
		this.imageReader = new ImageArrayReader("png");
		this.templateMatching = new TemplateMatchingManager();
		this.driftManager = new DriftManager();
		this.driftCorrection = new DriftCorrectionManager();
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		imageReader.setFileHandler(fh);
		templateMatching.setFileHandler(fh);
		driftManager.setFileHandler(fh);
		driftCorrection.setFileHandler(fh);
	}
	
	public void setInterruptionFlag(Flag interrupt) {
		templateMatching.setInterruptionFlag(interrupt);
		driftCorrection.setInterruptionFlag(interrupt);
	}
	
	public void setTemplateTableModel(DefaultTableModel model) {
		templateMatching.setTableModel(model);
	}
	

	public void setDriftTableModel(DriftModel model, DriftSectionModel sectionModel) {
		driftManager.setTableModel(model, sectionModel);
	}
	
	public void advanceState() {
		switch (state) {
			case INIT:
				if (isIOReady()) {
					this.state++;
				}
				break;
			case TEMPLATE_MATCHING:
				if (isDriftReady()) {
					this.state++;
				}
				break;
			case DRIFT_EDIT:
				if (isDriftReady()) {
					this.state++;
				}
				break;
			case DRIFT_CORRECTION:
				
				break;
			default:
				logger.severe("unkown state: " + state);
		}
		
	}
	
	public void previousState() {
		switch (state) {
			case INIT:
				logger.info("no previous state");
				return;
			case TEMPLATE_MATCHING:
				this.state--;
				break;
			case DRIFT_EDIT:
				this.state--;
				break;
			case DRIFT_CORRECTION:
				this.state--;
				break;
			default:
				logger.severe("unkown state: " + state);
		}
	}
	
	public int getState() {
		return state;
	}
	
	
	/////////////////////////////////////////////////////////////////////
	///////////////////////////// IO state //////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	public void setSrcDir(String folder) {
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
	
	public void setSaveDir(String folder) {
		File file = new File(folder);
		if (file.canWrite()) {
			saveDir = folder;
		} else {
			logger.info("cannot set save folder at : " + folder);
		}
	}
	
	public List<Path> getFileList() {
		return fileList;
	}
	
	public String getSaveFolder() {
		return saveDir;
	}
	
	private boolean isIOReady() {
		if (fileList == null ||fileList.size() <= 2) {
			return false;
		}
		if (saveDir == null) {
			return false;
		}
		
		return true;
	}

	/////////////////////////////////////////////////////////////////////
	////////////////////// template matching ////////////////////////////
	/////////////////////////////////////////////////////////////////////

	public void setSegmentFrame(int frameNumber) {
		templateMatching.setSegmentFrame(frameNumber);
	}

	public void removeSegmentFrame(int segmentIndex) {
		templateMatching.removeSegmentFrame(segmentIndex);
	}
	
	public boolean setTemplate(int frameNumber, int[] ROI) {
		String filename = fileList.get(frameNumber).toString();
		double[][] image = imageReader.read(filename);
		Boolean res = templateMatching.setROI(frameNumber, ROI, image);
		return res; 
	}
	
	public void removeTemplate(int targetIndex) {
		templateMatching.removeROI(targetIndex);
	}

	protected MovieSegment getMovieSegment(int frameNumber) {
		return templateMatching.getSegment(frameNumber);
	}

	public boolean templageMatchingPreRunValidation() {
		return templateMatching.templageMatchingPreRunValidation();
	}
	
	public void runTemplateMatching(boolean blur) {
		templateMatching.run(saveDir, driftManager.getXDrift(), driftManager.getYDrift(), blur);
	}
	

	public void afterTemplateMatching() {
//		logger.info("at after template matching");
		driftManager.fitDrift(driftManager.FITBOTH);
		driftManager.saveFittedDrift(saveDir);
		saveRawDrift();
//		logger.info("end of after template matching");
	}
	
	public int getTemplateMatchingProgress() {
		return templateMatching.getProgress();
	}
	
	private void setTemplateMatchingProgress(int num) {
		templateMatching.setProgress(num);
	}
	
	private void saveRawDrift() {
		driftManager.saveRawDrift(saveDir);
	}
	
	private boolean isDriftReady() {
		if (templateMatching.getProgress() == 100) {
			return true;
		}
		return false;
	}
	
	public void setDriftCsv(String filename) {
		if (driftManager.setDrifts(filename)) {
			driftManager.fitDrift(driftManager.FITBOTH);
			setTemplateMatchingProgress(100);
		}	
	}
	
	/////////////////////////////////////////////////////////////////////
	////////////////////// drift editing ////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	public float[] getXDrift() {
		return driftManager.getXDrift();
	}
	
	public float[] getYDrift() {
		return driftManager.getYDrift();
	}
	
	public float[] getXFittedDrift() {
		return driftManager.getFittedXDrift();
	}
	
	public float[] getYFittedDrift() {
		return driftManager.getFittedYDrift();
	}
	
	public void setFitting(boolean fit) {
		useFittedDrift = fit;
		logger.info("fitting set to: " + fit);
	}
	
	public void setFitDegree(int sectionIndex, int degree) {
		driftManager.setFitDegree(sectionIndex, degree);
	}
	
	public void setXDrift(int frameNumber, float newVal) {
		driftManager.setXDrift(frameNumber, newVal);
	}
	
	public void setYDrift(int frameNumber, float newVal) {
		driftManager.setYDrift(frameNumber, newVal);
	}
	
	public void addCuttingPoint(int frameNumber) {
		driftManager.addCuttingPoint(frameNumber);
	}
	
	public void removeCuttingPoint(int sectionIndex) {
		driftManager.removeCuttingPoint(sectionIndex);
	}
	
	/////////////////////////////////////////////////////////////////////
	////////////////////// drift correction /////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	public boolean driftCorrectionPreRunValidation() {
		if (driftManager.isReady()) {
			return true;
		}
		return false;
	}

	public void runDriftCorrection() {
		// TODO: customise ROI
		String filename = fileList.get(0).toString();
		double[][] image = imageReader.read(filename);
		int[] ROI = new int[4];
		ROI[0] = 0;
		ROI[1] = image.length;
		ROI[2] = 0;
		ROI[3] = image[0].length;
		if (useFittedDrift) {
			driftCorrection.run(saveDir, getXFittedDrift(), getYFittedDrift(), ROI);
		} else {
			driftCorrection.run(saveDir, getXDrift(), getYDrift(), ROI);
		}
	}

	public int getDriftCorrectionProgress() {
		return driftCorrection.getProgress();
	}
	
	
	public List<String> getSaveFiles() {
		return driftCorrection.getSaveFiles();
	}



}
