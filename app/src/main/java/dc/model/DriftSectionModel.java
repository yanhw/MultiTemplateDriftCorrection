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
	public static final int DEGREE = 3;
	public static final int FIT = 4;	//TODO: fit option for individual section
	
	protected static final int DEFAULTFITTINGDEGREE = 5;
	
	
	public DriftSectionModel() {
		addColumn("index");
		addColumn("starting frame");
		addColumn("ending frame");
		addColumn("fit degree");
		addColumn("fit");
	}
	
	public void initialise(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public void initData(int frameNumber) {
		if (frameNumber <= 0) {
			return;
		}
		setRowCount(0);
		addRow(new Object[] {1, 0, frameNumber-1, DEFAULTFITTINGDEGREE, true});
	}
	

	public void clear() {
		setRowCount(0);
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
		assert getRowCount() > 0;
		return (int)getValueAt(getRowCount()-1, END)+1;
	}
	
	public int getRowNumber(int frameNumber) {
		if (frameNumber < 0 || getRowCount()==0 || frameNumber >= getMovieSize()) {
			return -1;
		}
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
		if (isEndFrame(frameNumber)) {
			return;
		}
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
		int degree = DEFAULTFITTINGDEGREE;
		try {
			degree = (int)getValueAt(targetIdx, DEGREE);
		} catch (ClassCastException e) {
			logger.warning("bad degree: " + getValueAt(targetIdx, DEGREE));
		}
		
		insertRow(targetIdx+1, new Object[] {targetIdx+2, frameNumber, ending, degree, flag});
		
		logger.info("set end frame at: " + frameNumber);
	}
	
	public void removeEndFrame(int segmentIndex) {
		// first segment cannot be removed
		if (segmentIndex <= 0 || segmentIndex >= getRowCount()) {
			return;
		}
		int prevIndex = segmentIndex-1;
		int ending = (int)getValueAt(segmentIndex, END);
		setValueAt(ending, prevIndex, END);
		removeRow(segmentIndex);
	}
	
	@Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case INDEX:
            case START:
            case END:
            case DEGREE:
                return Integer.class;
            case FIT:
                return Boolean.class;
            default:
                return null;
        }
    }
	
	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == DEGREE || column == FIT) {
			return true; 
		}
		return false;
	}

	public void setMaxDegree(int degree) {
		for (int i=0; i < getRowCount(); i++) {
			if ((int)getValueAt(i, DEGREE) > degree) {
				setValueAt(degree, i, DEGREE);
			}
		}
	}

}