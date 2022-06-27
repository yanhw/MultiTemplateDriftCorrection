package dc.gui;

import java.nio.file.Path;

import dc.controller.ReadOnlyMovie;

/*****************************
 * 
 * This class synchronises the display of various GUI components
 *
 */

public class Synchroniser {

	
	private MainFrame myView;
	private ReadOnlyMovie myMovie;
	
	public Synchroniser(MainFrame view) {
		myView = view;
	}
	
	
	public void setMovie(ReadOnlyMovie movie) {
		this.myMovie = movie;
	}
	


	
	
	public void rawImageChanged(int frameNumber) {
//		switch (state) {
//			case TEMPLATE_MATCHING:
//				setTMImage(frameNumber);
//				break;
//		}
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
