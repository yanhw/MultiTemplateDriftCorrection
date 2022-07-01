package dc.model;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class DriftSectionModel extends DefaultTableModel {
	private static final Logger logger = Logger.getLogger(DriftSectionModel.class.getName());
//	private String[] columnNames = {"index","starting frame","ending frame","fit","fit degree"};
	public static final int INDEX = 0;
	public static final int START = 1;
	public static final int END = 2;
	public static final int FIT = 3;	//TODO: fit option for individual section
	public static final int DEGREE = 4;
	
	private static final int DEFAULTFITTINGDEGREE = 5;
	public static final int MAXFITTINGDEGREE = 25;

	
	public DriftSectionModel() {
		addColumn("index");
		addColumn("starting frame");
		addColumn("ending frame");
		addColumn("fit");
		addColumn("fit degree");
	}
	
	public void initialise(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public void initData(int frameNumber) {
		setRowCount(0);
		addRow(new Object[] {1, 0, frameNumber-1, true, DEFAULTFITTINGDEGREE});
	}
	
	public boolean isEndFrame(int frameNumber) {
		for (int i = 0; i < getRowCount(); i++) {
			if ((int)getValueAt(i, END) == frameNumber) {
				return true;
			}
		}
		return false;
	}
	
	private int getMovieSize() {
		return (int)getValueAt(getRowCount()-1, END);
	}
	
	public int getRowNumber(int frameNumber) {
		assert frameNumber >= 0;
		assert frameNumber <= getMovieSize();
		int i;
		for (i=0; i < getRowCount(); i++) {
			if ((int)getValueAt(i, END) >= frameNumber) {
				return i;
			}
		}
		assert false;
		return i;
	}
	
	public void setEndFrame(int frameNumber) {
		assert !isEndFrame(frameNumber);
		if (frameNumber <= 0) {
			return;
		}
		// the last segment must have at least 2 frames
		if (frameNumber >= getMovieSize()-1) {
			return;
		}
		int targetIdx = getRowNumber(frameNumber);
		int ending = (int)getValueAt(targetIdx, END);
		setValueAt(frameNumber, targetIdx, END);
		boolean flag = (boolean) getValueAt(targetIdx, FIT);
		int degree = (int)getValueAt(targetIdx, DEGREE);
		insertRow(targetIdx+1, new Object[] {targetIdx+2, frameNumber, ending, flag, degree});
		
		logger.info("set end frame at: " + frameNumber);
	}
	
	public void removeEndFrame(int segmentIndex) {
		// first segment cannot be removed
		if (segmentIndex == 0) {
			return;
		}
		if (segmentIndex >= getRowCount()) {
			return;
		}
		int prevIndex = segmentIndex-1;
		int ending = (int)getValueAt(segmentIndex, END);
		setValueAt(ending, prevIndex, END);
		removeRow(segmentIndex);
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		// only degree can be edited
		if (column == DEGREE) {
			return true; 
		}
		return false;
	}

}