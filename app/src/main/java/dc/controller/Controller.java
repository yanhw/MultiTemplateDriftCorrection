package dc.controller;


import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;

import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import dc.gui.MainFrame;
import dc.model.DriftModel;
import dc.model.DriftSectionModel;
import dc.model.MovieStateModel;

/*
 * This class serves as central controller for the app
 * Thread management is handled here. Other classes in this package can be assumed to be
 * thread safe.
 * Return type of all public methods should be void, use MainFrame to update gui
 * Exception: models for JTable are updated directly by the class that stores relevant data,
 * (hence bypass the MainFrame route)
 * 
 * Note: gui.Synchroniser is the other class that handles gui events. All events that does
 * not require approval go to Synchroniser, all events that requires approval go to Controller.
 * In other words, all events that only affect the view go to Synchroniser, all events that
 * modify the model go to Controller.
 * 
 * Note: logger fileHandler is added outside constructor, so avoid log inside constructor in this package
 */
public class Controller {
	private static final Logger logger = Logger.getLogger(Controller.class.getName());
	

//	private int state = INIT;
	
	private MainFrame myView;
	private boolean isBusy = false;					// sync lock
	private SwingWorker<Void, Integer> stoppableWorker;
	
	private Flag interrupt = new Flag();
	
	private Movie myMovie;

	public Controller() {
		logger.setLevel(Level.FINE);
//		logger.setUseParentHandlers(true);
		this.myMovie = new Movie();
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
		myView = mainFrame;
		ReadOnlyMovie movie = new ReadOnlyMovie(myMovie);
		myView.setMovie(movie);
		setDriftTableModel();
		setMovieStateModel();
	}
	
	public void setTemplateTableModel(DefaultTableModel model) {
		myMovie.setTemplateTableModel(model);
	}
	
	private void setDriftTableModel() {
		DriftModel driftModel = new DriftModel();
		DriftSectionModel sectionModel = new DriftSectionModel();
		myMovie.setDriftTableModel(driftModel, sectionModel);
		myView.setDriftModel(driftModel);
		myView.setDriftSectionModel(sectionModel);
	}
	
	private void setMovieStateModel() {
		MovieStateModel myState = new MovieStateModel();
		myMovie.setMovieStateModel(myState);
		myView.setMovieStateModel(myState);
	}
	
	// should be called before initialising a thread
	private void block(String message) {
		isBusy = true;
		myView.updateStatus(message);
		logger.info("blocking controller, " + message);
	}
	
	// should be called when a blocking thread is finished
	private void release() {
		isBusy = false;
		logger.info("controller is released");
	}
	
	// call this method when state might be changed
	private void checkState() {
		myMovie.checkState();
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
				List<Path> fileList = myMovie.getFileList();
				if (fileList == null) {
					release();
					return;
				}
				if (fileList.isEmpty()) {
					myView.updateStatus("no .png file found in " + folder);
					release();
					return;
				}
				if (fileList.size() < 2) {
					myView.updateStatus("need at least 2 images");
					release();
					return;
				}
				// success
				checkState();
				myView.setImageFileName(folder);
				myView.setRawImages(fileList);
				myView.updateStatus("");
				autoSetSaveDir(folder);
				
				if (myMovie.templageMatchingPreRunValidation()) {
					myView.setTemplateMatchingBtn(true, false);
				} else {
					myView.setTemplateMatchingBtn(false, false);
				}
				release();
			}
		};
		worker.execute();
	}
	
	
	private void autoSetSaveDir(String movieFolder) {
		assert (movieFolder != null);
		if (myMovie.getSaveFolder() != null) {
			return;
		}
		File file = new File(movieFolder);
		setSaveDir(file.getParent());
	}
	
	public void setSaveDir(String folder) {
		myMovie.setSaveDir(folder);
		
		if (myMovie.getSaveFolder() == folder) {
			checkState();
			myView.setSaveFolder(folder);
		} else {
			myView.updateStatus("it appears you cannot modify the selected folder.");
		}
	}
	

	
	
	/////////////////////////////////////////////////////////////////////
	////////////////////// template matching ////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	public void setSegmentFrame() {
		if (isBusy) {
			return;
		}
		int frameNumber = myView.getRawFrameIndex();
		myMovie.setSegmentFrame(frameNumber);
		if (myMovie.templageMatchingPreRunValidation()) {
			myView.setTemplateMatchingBtn(true, false);
		} else {
			myView.setTemplateMatchingBtn(false, false);
		}
	}
	
	public void removeSegmentFrame(int segmentIndex) {
		if (isBusy) {
			return;
		}
		myMovie.removeSegmentFrame(segmentIndex);
		if (myMovie.templageMatchingPreRunValidation()) {
			myView.setTemplateMatchingBtn(true, false);
		} else {
			myView.setTemplateMatchingBtn(false, false);
		}
	}
	
	public void setTemplate() {
		if (isBusy) {
			return;
		}
		int[] ROI = myView.getRawROI();
		if (ROI == null) {
			logger.info("ROI is not set");
			myView.updateStatus("must select a region of interest!");
			return;
		}
		
		int frameNumber = myView.getRawFrameIndex();
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
				myView.setTMImage(frameNumber);
				myView.updateStatus("");
				if (myMovie.templageMatchingPreRunValidation()) {
					myView.setTemplateMatchingBtn(true, false);
				} else {
					myView.setTemplateMatchingBtn(false, false);
				}
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
		if (myMovie.templageMatchingPreRunValidation()) {
			myView.setTemplateMatchingBtn(true, false);
		} else {
			myView.setTemplateMatchingBtn(false, false);
		}
	}

	public void runTemplateMatching(boolean blur) {
		if (isBusy) {
			cancelTemplateMatching();
			return;
		}
		interrupt.set(false);
		myView.updateStatus("validating input, please wait...");
		if (!myMovie.templageMatchingPreRunValidation()) {
			myView.updateStatus("template validation failed, check input!");
			return;
		}
		// release only in afterTemplateMatching
		// TODO this release mechanism is bad, need to change
		block("starting template matching...");
		myView.setTemplateMatchingBtn(true, true);
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
	            myView.setProgress(i);
	        }
			
			@Override
			public void done() {
				if (!interrupt.get()) {
					logger.info("finished");
					myView.setProgress(100);
					myView.setTemplateMatchingBtn(true, false);
					afterTemplateMatching();
				}
				// this will be triggered when cancel button is pressed or when there is a problem
				else {
					myView.setProgress(100);
					myView.setTemplateMatchingBtn(true, false);
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
		myView.setProgress(100);
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
				updatePlot();
				myView.updateStatus("ready to view drift");
				checkState();
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
					myView.updateStatus("failed to load csv file: " + filename);					
					release();
					return;
				}
				checkState();
				updatePlot();
				myView.updateStatus("ready to view drift");
				logger.info("finished reading csv");
				
				
				release();
			}
		};
		worker.execute();
	}
	
	
//	/////////////////////////////////////////////////////////////////////
//	//////////////////////// edit drift /////////////////////////////////
//	/////////////////////////////////////////////////////////////////////
	
	
	public void removeDrift(int frameNumber) {
		if (isBusy) {
			return;
		}
	}
	
	public void changeXDrift(int frameNumber, float newVal) {
		if (isBusy) {
			return;
		}
		myMovie.setXDrift(frameNumber, newVal);
		updateXPlot();
//		logger.info("aaaaaaa");
	}
	
	public void changeYDrift(int frameNumber, float newVal) {
		if (isBusy) {
			return;
		}
		myMovie.setYDrift(frameNumber, newVal);
		updateYPlot();
	}
	
	///////////////// fitting//////////////////////////////////
	
	public void changeFitDegree(int row, int intValue) {
		if (isBusy) {
			return;
		}
		myMovie.setFitDegree(row, intValue);
		updatePlot();
	}
	
	public void addCuttingPoint(int frameNumber) {
		if (isBusy) {
			return;
		}
		myMovie.addCuttingPoint(frameNumber);
		myView.updateDriftSectionTable();
		updatePlot();
	}
	
	public void removeCuttingPoint(int sectionIndex) {
		if (isBusy) {
			return;
		}
		myMovie.removeCuttingPoint(sectionIndex);
		myView.updateDriftSectionTable();
		updatePlot();
	}


	///////////////// plot////////////////////
	private void updatePlot() {
		updateXPlot();
		updateYPlot();
	}
	
	private void updateXPlot() {
		float[] xRawList = myMovie.getXDrift();
		float[] xFittedList = myMovie.getXFittedDrift();
		
		assert (xRawList != null);
		
		assert (xFittedList != null);
		
		int[] xList = new int[xRawList.length];
		
		for (int idx = 0; idx < xRawList.length; idx++) {
			xList[idx] = (int) xRawList[idx];
		}
		myView.setXDriftData(xList, xFittedList);
	}
	
	private void updateYPlot() {
		float[] yRawList = myMovie.getYDrift();
		float[] yFittedList = myMovie.getYFittedDrift();
		assert (yRawList != null);
		assert (yFittedList != null);
		int[] yList = new int[yRawList.length];
		for (int idx = 0; idx < yRawList.length; idx++) {
			yList[idx] = (int) yRawList[idx];
		}
		myView.setYDriftData(yList, yFittedList);
	}
	


	/////////////////////////////////////////////////////////////////////
	////////////////////// Drift correction ////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	public void runDriftCorrection(boolean blurFlag) {
		if (isBusy) {
			cancelDriftCorrection();
			return;
		}
		interrupt.set(false);
		myView.updateStatus("validating input, please wait...");
		if (!myMovie.driftCorrectionPreRunValidation()) {
			myView.updateStatus("drift correction failed, check input!");
			return;
		}
		// release only in afterTemplateMatching
		// TODO this release mechanism is bad, need to change
		// TODO handle interruption
		block("starting drift correction...");
		myView.toggleDriftCorrectionBtn(false);
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
	            myView.setProgress(i);
	        }
			
			@Override
			public void done() {
				if (!interrupt.get()) {
					logger.info("finished");
					myView.setCorrectedImages(myMovie.getSaveFiles());
				}
				checkState();
				myView.setProgress(100);
				myView.toggleDriftCorrectionBtn(true);
				release();

			}
		};
		stoppableWorker.execute();
	}

	private void cancelDriftCorrection() {
		logger.info("drift correction cancelled");
		stoppableWorker.cancel(true);
		myView.setProgress(100);
		
	}

}
