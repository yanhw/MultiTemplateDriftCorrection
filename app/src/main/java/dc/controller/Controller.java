package dc.controller;


import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import dc.gui.MainFrame;
import dc.model.*;

/*
 * This class serves as central controller for the app
 * Thread management is handled here. Other classes in this package can be assumed to be
 * thread safe.
 * Return type of all public methods should be void, use MainFrame to update gui
 * Exception: models for JTable are updated directly by the class that stores relevant data,
 * (hence bypass the MainFrame route)
 * 
 * commented methods are kept for future non-GUI version
 * 
 * Note: logger fileHandler is added outside constructor, so avoid log inside constructor in this package
 */
public class Controller {
	private static final Logger logger = Logger.getLogger(Controller.class.getName());
	
	private boolean isBusy = false;							// sync lock
	private SwingWorker<Void, Integer> stoppableWorker;		// multi-process worker for long processes
	private BooleanModel interrupt = new BooleanModel();					// flag for stoppableWorker to stop
	
	private Movie myMovie;
	private BoundedRangeModel myProgress;	// this cannot take over the function of boolean model due to reaction time
	private BooleanModel runningFlag;
	private TextModel myStatus;
	
	public Controller() {
		logger.setLevel(Level.FINE);
//		logger.setUseParentHandlers(true);
		myMovie = new Movie();
		myProgress = new DefaultBoundedRangeModel(0,1,0,100); 
		myStatus = new TextModel();
		runningFlag = new BooleanModel(false);
		// listeners are here (instead of inside DriftManager) for thread management purpose
		DriftModel driftModel = myMovie.getDriftModel();
		DriftSectionModel sectionModel = myMovie.getDriftSectionModel();		
		driftModel.addTableModelListener(new DriftModelListener());
		sectionModel.addTableModelListener(new DriftSectionModelListener());
		myMovie.setInterruptionFlag(interrupt);
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
		mainFrame.setProgressModel(myProgress);
		mainFrame.setStatusModel(myStatus);
		mainFrame.setRunningFlagModel(runningFlag);
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
		logger.info("controller is released");
	}
	
	/////////////////////////////////////////////////////////////////////
	/////////////////////// set advanced parameter //////////////////////
	/////////////////////////////////////////////////////////////////////
	
	public void resetParameter() {
		
	}
	
	public void setMaxNumThread() {
		
	}
	
//	public void set
	
	/////////////////////////////////////////////////////////////////////
	///////////////////////////// IO state //////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	public void setSrcDir(String folder) {
		if (isBusy) {
			return;
		}
		block("getting images from " + folder);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {	 		
				myMovie.setSrcDir(folder);

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
				myMovie.checkState();
				myStatus.setText("");
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
			myMovie.checkState();
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!res) {
					logger.warning("failed to set ROI");
					release();
					return;
				}
				//TODO update start and end frame
				myStatus.setText("");
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
		// TODO this release mechanism is bad, need to change
		block("starting template matching...");
		runningFlag.set(true);
		stoppableWorker = new SwingWorker<Void, Integer>() {
			
			@Override
			public Void doInBackground() {
				publish(0);
				
				// TODO: this thread is bad, need to find more straight forward way to update progress
				Thread temp = new Thread() {
				    public void run() {
				    	myMovie.runTemplateMatching(blur);
				    }  
				};

				temp.start();
				
//				myMovie.runTemplateMatching(blur);
				int progress = myMovie.getTemplateMatchingProgress();
//				System.out.println(progress);
				while (progress != 100) {
					try {
//						System.out.println(interrupt);
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					publish(progress);
					logger.info("progress set to: "+progress);
					progress = myMovie.getTemplateMatchingProgress();
				}
				publish(100);
				return null;     
			}
			
			@Override
	        protected void process(List<Integer> chunks) {
	            int i = chunks.get(chunks.size()-1);
	            myProgress.setValue(i);
	        }
			
			@Override
			public void done() {
				if (!interrupt.get()) {
					logger.info("finished");
					myProgress.setValue(100);
					runningFlag.set(false);
					afterTemplateMatching();
				}
				// this will be triggered when cancel button is pressed or when there is a problem
				else {
					myProgress.setValue(100);
					runningFlag.set(false);
					release();
				}
//				release();
			}
		};

		stoppableWorker.execute();
	}
	
	private void cancelTemplateMatching() {
		interrupt.set(true);
		logger.info("template matching cancelled");
		myProgress.setValue(100);
	}

	private void afterTemplateMatching() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {	 		
				myMovie.afterTemplateMatching();
				return null;     
			}
			@Override
			public void done() {
				myStatus.setText("ready to view drift");
				myMovie.checkState();
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
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				myMovie.setDriftCsv(filename);
				return null;     
			}
			@Override
			public void done() {
				
				if (myMovie.getTemplateMatchingProgress() != 100) {
					logger.info("failed to reading csv");
					myStatus.setText("failed to load csv file: " + filename);					
					release();
					return;
				}
				myMovie.checkState();
				myStatus.setText("ready to view drift");
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
//			DriftModel model = (DriftModel)e.getSource();
			int col = e.getColumn();
			int direction;
			switch(col) {
				case DriftModel.FITTED_DX:
				case DriftModel.FITTED_DY:
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
			fitDrift(start, end, direction);
		}
		
	}
	
	private class DriftSectionModelListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent e) {
			if (e.getType() == TableModelEvent.DELETE) {
				return;
			}
			DriftSectionModel model = (DriftSectionModel)e.getSource();
			int direction = DriftManager.FITBOTH;

			int startRow = e.getFirstRow();
			int endRow = e.getLastRow();
			int start = (int) model.getValueAt(startRow, DriftSectionModel.START);
			int end = (int) model.getValueAt(endRow, DriftSectionModel.END);
			logger.info("drift section table changed: " + start + " " + end + " " + direction);
			fitDrift(start, end, direction);
		}
		
	}
	
	// allowing to run when controller is "blocked"
	private void fitDrift(int start, int end, int direction) {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
//				logger.info("fitDrift worker running");
//				if (myMovie.isDriftReady()) {
//					return null;
//				}
				myMovie.fitDrift(start, end, direction);
//				logger.info("fitDrift worker finishing");
				return null;     
			}
			@Override
			public void done() {
				myStatus.setText("");
				myMovie.checkState();
			}
		};
		worker.execute();
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
		if (isBusy) {
			return;
		}
		myMovie.addCuttingPoint(frameNumber);
	}
	
	public void removeCuttingPoint(int sectionIndex) {
		if (isBusy) {
			return;
		}
		myMovie.removeCuttingPoint(sectionIndex);
	}
	

	///////////////// plot////////////////////



	/////////////////////////////////////////////////////////////////////
	////////////////////// Drift correction ////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	public void runDriftCorrection(boolean blurFlag) {
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
		// TODO this release mechanism is bad, need to change
		// TODO handle interruption
		block("starting drift correction...");
		runningFlag.set(true);
		SwingWorker<Void, Integer> stoppableWorker = new SwingWorker<Void, Integer>() {
			@Override
			public Void doInBackground() {
				publish(0);
				
				// TODO: this thread is bad, need to find more straight forward way to update progress
				Thread temp = new Thread() {
				    public void run() {
				    	myMovie.runDriftCorrection(blurFlag);
				    }  
				};

				temp.start();
				
				int progress = myMovie.getDriftCorrectionProgress();
				while (progress != 100) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					publish(progress);
					progress = myMovie.getDriftCorrectionProgress();
				}
				publish(100);
				return null;     
			}
			
			@Override
	        protected void process(List<Integer> chunks) {
	            int i = chunks.get(chunks.size()-1);
	            myProgress.setValue(i);
	        }
			
			@Override
			public void done() {
				if (!interrupt.get()) {
					logger.info("finished");
				}
				myMovie.checkState();
				myProgress.setValue(100);
				runningFlag.set(false);
				release();

			}
		};
		stoppableWorker.execute();
	}

	private void cancelDriftCorrection() {
		logger.info("drift correction cancelled");
		stoppableWorker.cancel(true);
		runningFlag.set(false);
		myProgress.setValue(100);
		
	}

}
