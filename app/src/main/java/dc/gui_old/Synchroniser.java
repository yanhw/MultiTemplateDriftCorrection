package dc.gui_old;

import java.nio.file.Path;

import dc.model.ReadOnlyMovie;

/*****************************
 * 
 * This class synchronises the display of various GUI components
 *
 */

public class Synchroniser {
	
	public static final int INIT = 0;
	public static final int TEMPLATE_MATCHING = 1;
	public static final int DRIFT_EDIT = 2;
	public static final int DRIFT_CORRECTION = 3;
	public static final int DONE = 4;				// not in use
	
	private MainFrame myView;
	private ReadOnlyMovie myMovie;
	private int state;			// maybe should access this from controller
	
	public Synchroniser(MainFrame view) {
		myView = view;
	}
	
	
	public void setMovie(ReadOnlyMovie movie) {
		this.myMovie = movie;
	}
	
	
	protected void setState(int state) {
		this.state = state;
	}
	
	public void rawImageChanged(int frameNumber) {
		switch (state) {
			case TEMPLATE_MATCHING:
				setTMImage(frameNumber);
				break;
		}
	}
	
	public void tableRowSelected(int rowNumber) {
		
	}
	
	public void driftPointSelected(int frameNumber) {
		
	}
	
	public void driftCellSelected(int frameNumber) {
		
	}


	protected void setTMImage(int frameNumber) {
		int first = myMovie.getSegementStart(frameNumber);
		int last = myMovie.getSegementEnd(frameNumber);
		Path path = myMovie.getROIFrame(frameNumber);
		int[] ROI = myMovie.getROI(frameNumber);
		myView.setTMImage(frameNumber, first, last, path, ROI);
		
	}


	public void driftPlotPointSelected(int frameNumber) {
		// TODO Auto-generated method stub
		myView.setDriftTableVisible(frameNumber);
		myView.setRawImageFrame(frameNumber);
	}

	
	
}
