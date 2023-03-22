package dc.controller;


import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import dc.gui.MainFrame;
import dc.model.BooleanModel;
import dc.model.DriftModel;
import dc.model.DriftSectionModel;
import dc.model.DriftUpdateStateModel;
import dc.model.TextModel;
import dc.utils.Constants;

/*
 * This class receives request from gui, and passes it to Movie class. Thread management is handled here. Other classes in this package can be assumed to be
 * thread safe.
 * Return type of all public methods should be void, use model listeners to update gui
 * 
 * commented methods are kept for future non-GUI version
 * 
 * Note: logger fileHandler is added outside constructor, so avoid log inside constructor in this package
 */
public class Controller {
	private static final Logger logger = Logger.getLogger(Controller.class.getName());
	
	private boolean isBusy = false;							// sync lock
	private BooleanModel interrupt = new BooleanModel();	// flag for stoppableWorker to stop
	private DriftUpdateStateModel driftUpdateModel = new DriftUpdateStateModel();
											// sync lock for drift plotting
	
	private Movie myMovie;					// data controller
	private BooleanModel runningFlag;		// flag for buttons that trigger long process
											// TODO: check if can merge this with isBusy
	private TextModel myStatus;				// update text in status panel

	public Controller() {
		logger.setLevel(Level.FINE);
//		logger.setUseParentHandlers(true);
		myMovie = new Movie();
		myStatus = new TextModel();
		runningFlag = new BooleanModel(false);
		// listeners are here (instead of inside DriftManager) for thread management purpose
		DriftModel driftModel = myMovie.getDriftModel();
		DriftSectionModel sectionModel = myMovie.getDriftSectionModel();		
		driftModel.addTableModelListener(new DriftModelListener());
		sectionModel.addTableModelListener(new DriftSectionModelListener());
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		myMovie.setFileHandler(fh);
	}
	
	public void setMainFrame(MainFrame mainFrame) {
		if (mainFrame == null) {
			logger.severe("recieved null mainFrame");
		}
		mainFrame.setMovieStateModel(myMovie.getMovieStateModel());
		mainFrame.setFileModels(myMovie.getRawFileList(), myMovie.getCorrectedFileList());
		mainFrame.setTemplateTableModel(myMovie.getTemplateTableModel());
		mainFrame.setDriftModel(myMovie.getDriftModel());
		mainFrame.setDriftSectionModel(myMovie.getDriftSectionModel());
		mainFrame.setFileNameModels(myMovie.getInputDirModel(), myMovie.getSaveDirModel());
		
		TextModel myWarning = new TextModel();
		BoundedRangeModel myProgress = new DefaultBoundedRangeModel(0,1,0,100); 
		myMovie.setGUIHelper(interrupt, myProgress, myWarning);
		mainFrame.setProgressModel(myProgress);
		mainFrame.setStatusModel(myStatus);
		mainFrame.setWarningModel(myWarning);
		mainFrame.setRunningFlagModel(runningFlag);
		mainFrame.setDriftUpdateModel(driftUpdateModel);
		
		AtomicInteger gaussianKernel = new AtomicInteger(Constants.DEFAULT_GAUSSIAN_KERNEL);
		AtomicInteger gaussianInteration = new AtomicInteger(Constants.DEFAULT_GAUSSIAN_ITERATION);
		AtomicInteger templateMatchingMethod = new AtomicInteger(Constants.DEFAULT_TM_METHOD);
		AtomicInteger maxThreads = new AtomicInteger(Constants.MAX_WORKER);
		AtomicInteger maxDegree = new AtomicInteger(Constants.MAX_FITTING_DEGREE);
		myMovie.setDefaultParameters(gaussianKernel, gaussianInteration, templateMatchingMethod, maxThreads, maxDegree);
		mainFrame.setDefaultParameters(gaussianKernel, gaussianInteration, templateMatchingMethod, maxThreads, maxDegree);
	}
	
	public void clearSession() {
		block("reseting...");
		if (runningFlag.get()) {
			interrupt.set(true);
		}
		driftUpdateModel.setValue(DriftUpdateStateModel.UPDATING);
		myMovie.reset();
		
		interrupt.set(false);
		runningFlag.set(false);
		release();
		myStatus.setText("session reseted. please choose image sequence");
	}

	// should be called before initialising a thread
	private void block(String message) {
		isBusy = true;
		myStatus.setText(message);
		logger.info("blocking controller, " + message);
	}
	
	// should be called when a blocking thread is finished
	private void release() {
		isBusy = false;
		if (driftUpdateModel.getValue() == DriftUpdateStateModel.UPDATING) {
			driftUpdateModel.setValue(DriftUpdateStateModel.NEED_CHECK);
		}
		logger.info("controller is released");
	}
	
	/////////////////////////////////////////////////////////////////////
	/////////////////////// set advanced parameter //////////////////////
	/////////////////////////////////////////////////////////////////////
	
	public void resetParameter() {
		myMovie.resetDefaultParameters();
	}
	
	public void setMaxWorkerThread(int number) {
		myMovie.setMaxWorkerThread(number);
	}
	
	public void setGaussianKernel(int size, int iteration) {
		if (isBusy) {
			myStatus.setText("the program is busy, no change is made, please for current process to finish");
			return;
		}
		block("changing setting for gaussian parameter...");
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {	 		
				myMovie.setGaussianOption(size, iteration);
				return null;     
			}
			@Override
			public void done() {
				myStatus.setText("");
				release();
			}
		};
		worker.execute();
	}
	
	public void setTMMethod(int method) {
		myMovie.setTMMethod(method);
	}
	
	public void setMaxFittingDegree(int degree) {
		myMovie.setMaxFittingDegree(degree);
	}

	
	/////////////////////////////////////////////////////////////////////
	///////////////////////////// IO state //////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	public void setSrcDir(String folder, String filetype) {
		if (isBusy) {
			return;
		}
		block("getting images from " + folder);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {	 		
				myMovie.setSrcDir(folder, filetype);
				return null;     
			}
			@Override
			public void done() {
				
				if (myMovie.getRawFileList().getSize() < 2) {
					myStatus.setText("need at least 2 png images in the folder");
					release();
					return;
				}
				// success
				myStatus.setText("please select templates in the input image sequence");
				autoSetSaveDir(folder);
				
				release();
			}
		};
		worker.execute();
	}
	
	
	private void autoSetSaveDir(String movieFolder) {
		assert (movieFolder != null);
		if (myMovie.getSaveFolder().getText().length() != 0) {
			return;
		}
		File file = new File(movieFolder);
		setSaveDir(file.getParent());
	}
	
	public void setSaveDir(String folder) {
		if (myMovie.setSaveDir(folder)) {
			myStatus.setText("data will be saved at: " + folder);
		} else {
			myStatus.setText("it appears you cannot modify the selected folder.");
		}
	}
	

	
	
	/////////////////////////////////////////////////////////////////////
	////////////////////// template matching ////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	public void setSegmentFrame(int frameNumber) {
		if (isBusy) {
			return;
		}
		myMovie.setSegmentFrame(frameNumber);
	}
	
	public void removeSegmentFrame(int segmentIndex) {
		if (isBusy) {
			return;
		}
		myMovie.removeSegmentFrame(segmentIndex);
	}
	
	public void setTemplate(int frameNumber, int[] ROI, boolean hasROI) {
		if (isBusy) {
			return;
		}
		if (!hasROI) {
			logger.info("ROI is not set");
			myStatus.setText("must select a region of interest!");
			return;
		}
		
		block("setting key frame, please wait...");
		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
			@Override
			public Boolean doInBackground() {
				boolean res = myMovie.setTemplate(frameNumber, ROI);
				return res; 
			}
			@Override
			public void done() {
				boolean res = false;
				try {
					res = get();
				} catch (InterruptedException e) {
					logger.warning("interrupted! failed to set ROI");
					e.printStackTrace();
				} catch (ExecutionException e) {
					logger.severe("execution exception! failed to set ROI");
					logger.severe(e.getCause().getMessage());
					e.printStackTrace();
				}
				if (!res) {
					logger.warning("failed to set ROI");
					myStatus.setText("failed to set template at frame " + frameNumber);
					release();
					return;
				}
				myStatus.setText("new template set at frame " + frameNumber);
				release();
			}
		};
		worker.execute();
	}
	
	public void removeTemplate(int targetIndex) {
		if (isBusy) {
			return;
		}
		myMovie.removeTemplate(targetIndex);
	}

	public void runTemplateMatching(boolean blur) {
		if (isBusy) {
			cancelTemplateMatching();
			return;
		}
		interrupt.set(false);
		myStatus.setText("validating input, please wait...");
		if (!myMovie.templageMatchingPreRunValidation()) {
			myStatus.setText("template validation failed, check input!");
			return;
		}
		// release only in afterTemplateMatching
		block("starting template matching...");
		driftUpdateModel.setValue(DriftUpdateStateModel.UPDATING);
		runningFlag.set(true);
		SwingWorker<Void, Void> stoppableWorker = new SwingWorker<Void, Void>() {
			
			@Override
			public Void doInBackground() {		
				myMovie.runTemplateMatching(blur);
				return null;     
			}
			
			@Override
			public void done() {
				if (!interrupt.get()) {
					logger.info("finished");
					runningFlag.set(false);
					afterTemplateMatching();
				}
				// this will be triggered when cancel button is pressed or when there is a problem
				else {
					runningFlag.set(false);
					logger.info("exiting template matching due to interruption");
					myStatus.setText("template matching stopped");
					release();
				}
			}
		};

		stoppableWorker.execute();
	}
	
	private void cancelTemplateMatching() {
		interrupt.set(true);
		logger.info("template matching cancelled");
		myStatus.setText("stopping...");
	}

	private void afterTemplateMatching() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {	 
//				driftUpdateModel.setValue(DriftUpdateStateModel.UPDATING);
				myMovie.afterTemplateMatching();
				return null;     
			}
			@Override
			public void done() {
//				driftUpdateModel.setValue(DriftUpdateStateModel.NEED_CHECK);
//				myStatus.setText("checking update for drift plot");
				release();
			}
		};
		worker.execute();
	}

	public void setDriftCsv(String filename) {
		if (isBusy) {
			return;
		}
		block("reading drift from file: " + filename);
		driftUpdateModel.setValue(DriftUpdateStateModel.UPDATING);
		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
			@Override
			public Boolean doInBackground() {
//				driftUpdateModel.setValue(DriftUpdateStateModel.UPDATING);
				Boolean res = myMovie.setDriftCsv(filename);
				return res;     
			}
			@Override
			public void done() {
				boolean res = false;
				try {
					res = get();
				} catch (InterruptedException e) {
					logger.warning("interrupted! failed to read drift from csv");
					e.printStackTrace();
				} catch (ExecutionException e) {
					logger.severe("execution exception! failed to read drift from csv");
					e.printStackTrace();
				}
				if (!res) {
					logger.info("failed to reading csv");
					myStatus.setText("failed to load csv file: " + filename);					
					release();
					return;
				}
//				driftUpdateModel.setValue(DriftUpdateStateModel.NEED_CHECK);
//				myStatus.setText("checking update for drift plot");
				logger.info("finished reading csv");			
				
				release();
			}
		};
		worker.execute();
	}
	
	
//	/////////////////////////////////////////////////////////////////////
//	//////////////////////// edit drift /////////////////////////////////
//	/////////////////////////////////////////////////////////////////////
	
	private class DriftModelListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent e) {
			// do nothing if rows are deleted
			if (e.getType() == TableModelEvent.DELETE) {
				return;
			} 
			boolean flag = false;
			if (driftUpdateModel.getValue() == DriftUpdateStateModel.UPDATING) {
				flag = true;
			} else {
				driftUpdateModel.setValue(DriftUpdateStateModel.UPDATING);
			}
			
//			DriftModel model = (DriftModel)e.getSource();
			int col = e.getColumn();
			int direction;
			switch(col) {
				case DriftModel.FITTED_DX:
				case DriftModel.FITTED_DY:
					if (!flag) {
						driftUpdateModel.setValue(DriftUpdateStateModel.NEED_CHECK);
					}
					
					return;
				case DriftModel.DX:
				case DriftModel.WEIGHT_X:
					direction = DriftManager.FITX;
					break;
				case DriftModel.DY:
				case DriftModel.WEIGHT_Y:
					direction = DriftManager.FITY;
					break;
				default:
					direction = DriftManager.FITBOTH;
			}
			int start = e.getFirstRow();
			int end = e.getLastRow();
			logger.info("drift table changed: " + start + " " + end + " " + direction);
			myMovie.fitDrift(start, end, direction);
			if (!flag) {
				driftUpdateModel.setValue(DriftUpdateStateModel.NEED_CHECK);
			}
			
		}
		
	}
	
	private class DriftSectionModelListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent e) {
			boolean flag = false;
			if (driftUpdateModel.getValue() == DriftUpdateStateModel.UPDATING) {
				flag = true;
			} else {
				driftUpdateModel.setValue(DriftUpdateStateModel.UPDATING);
			}
			if (e.getType() == TableModelEvent.DELETE) {
				if (!flag) {
					driftUpdateModel.setValue(DriftUpdateStateModel.NEED_CHECK);
				}
				return;
			}
			DriftSectionModel model = (DriftSectionModel)e.getSource();
			int direction = DriftManager.FITBOTH;
			int startRow = e.getFirstRow();
			int endRow = e.getLastRow();
			// when first row is added, this is from init(), don't call fitDrift because DriftModel is not ready
			if (e.getType() == TableModelEvent.INSERT && endRow == 0) {
				if (!flag) {
					driftUpdateModel.setValue(DriftUpdateStateModel.NEED_CHECK);
				}
				return;
			}
			
			int start = (int) model.getValueAt(startRow, DriftSectionModel.START);
			int end = (int) model.getValueAt(endRow, DriftSectionModel.END);
			logger.info("drift section table changed: " + start + " " + end + " " + direction);
			myMovie.fitDrift(start, end, direction);
			if (!flag) {
				driftUpdateModel.setValue(DriftUpdateStateModel.NEED_CHECK);
			}
		}
		
	}
	
//	
//	public void removeDrift(int frameNumber) {
//		if (isBusy) {
//			return;
//		}
//	}
//	
//	public void changeXDrift(int frameNumber, float newVal) {
//		if (isBusy) {
//			return;
//		}
//		myMovie.setXDrift(frameNumber, newVal);
//	}
//	
//	public void changeYDrift(int frameNumber, float newVal) {
//		if (isBusy) {
//			return;
//		}
//		myMovie.setYDrift(frameNumber, newVal);
//	}
	
	///////////////// fitting//////////////////////////////////
	
//	public void changeFitDegree(int row, int intValue) {
//		if (isBusy) {
//			return;
//		}
//		myMovie.setFitDegree(row, intValue);
//
//	}
	
	public void addCuttingPoint(int frameNumber) {
		driftUpdateModel.setValue(DriftUpdateStateModel.UPDATING);
		if (isBusy) {
			return;
		}
		block("adding cutting point...");
		myMovie.addCuttingPoint(frameNumber);
		release();
	}
	
	public void removeCuttingPoint(int sectionIndex) {
		driftUpdateModel.setValue(DriftUpdateStateModel.UPDATING);
		if (isBusy) {
			return;
		}
		block("removing cutting point...");
		myMovie.removeCuttingPoint(sectionIndex);
		release();
	}


	/////////////////////////////////////////////////////////////////////
	////////////////////// Drift correction ////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	public void runDriftCorrection(boolean blurFlag, boolean overwriteFlag) {
		if (isBusy) {
			cancelDriftCorrection();
			return;
		}
		interrupt.set(false);
		myStatus.setText("validating input, please wait...");
		if (!myMovie.driftCorrectionPreRunValidation()) {
			myStatus.setText("drift correction failed, check input!");
			return;
		}
		// release only in afterTemplateMatching
		block("starting drift correction...");
		runningFlag.set(true);
		SwingWorker<Void, Void> stoppableWorker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				myMovie.runDriftCorrection(blurFlag, overwriteFlag);
				return null;
			}
			
			@Override
			public void done() {
				if (!interrupt.get()) {
					myStatus.setText("drift correction finished");
					logger.info("finished");
				}
				myMovie.afterDriftCorrection();
				runningFlag.set(false);
				release();

			}
		};
		stoppableWorker.execute();
	}

	private void cancelDriftCorrection() {
		logger.info("drift correction cancelled");
		interrupt.set(true);
		runningFlag.set(false);
		
	}

}
