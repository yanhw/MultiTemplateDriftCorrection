package dc.model;

import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import dc.controller.Controller;

@SuppressWarnings("serial")
public class DriftSectionModel extends AbstractTableModel {
	private static final Logger logger = Logger.getLogger(DriftSectionModel.class.getName());
	private Controller controller;
	
	private String[] columnNames = {"index","starting frame","ending frame","fit","fit degree"};
	private static final int INDEX = 0;
	private static final int START = 1;
	private static final int END = 2;
	private static final int FIT = 3;	//TODO: fit option for individual section
	private static final int DEGREE = 4;
	
	private List<Integer> cuttingPoints;
	private List<Integer> degrees;
	private int lastFrame;
	
	public void initialise(Controller controller, FileHandler fh) {
		logger.addHandler(fh);
		this.controller = controller;
	}
	
	public void setData(List<Integer> cuttingPoints, List<Integer> degrees, int lastFrame) {
		if (cuttingPoints == null || degrees == null) {
			return;
		}
		if (cuttingPoints.size() > 0 && cuttingPoints.get(cuttingPoints.size()-1) > lastFrame) {
			return;
		}
		if (cuttingPoints.size()+1 != degrees.size()) {
			return;
		}
		this.cuttingPoints = cuttingPoints;
		this.degrees = degrees;
		this.lastFrame = lastFrame;
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		// only degree can be edited
		if (column == DEGREE) {
			return true; 
		}
		return false;
	}
	
	@Override
	public String getColumnName(int col) {
        return columnNames[col];
    }
	
	@Override
	public int getRowCount() {
		if (degrees==null) {
			return 0;
		}
		return degrees.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		if (degrees == null) {
			return;
		}
		if (row >= getRowCount() || col >= getColumnCount()) {
			return;
		}
		
		int intValue = 0;
		try {
			intValue = Integer.parseInt(String.valueOf(value));
			logger.info("value input: " + intValue);
        } catch (ClassCastException e) {
            logger.warning("failed to render");
            return;
        } catch (NumberFormatException e) {
        	logger.info("input is not a number");
        	return;
        }
		
		switch(col) {
			case DEGREE:
				if (degrees.get(row) == intValue) {
					return;
				}
				controller.changeFitDegree(row, intValue);
				break;
			default:
				logger.warning("unknonwn col: " + col);
		}
        fireTableCellUpdated(row, col);
    }

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= getRowCount() || columnIndex >= getColumnCount()) {
			return null;
		}
		assert (cuttingPoints.size()+1 == degrees.size());
		switch (columnIndex) {
			case INDEX:
				return rowIndex;
			case START:
				if (rowIndex == 0) {
					return 0;
				}
				return cuttingPoints.get(rowIndex-1);
			case END:
				if (rowIndex == cuttingPoints.size() || cuttingPoints.size()==0) {
					return lastFrame;
				}
				return cuttingPoints.get(rowIndex);
			case FIT:
				return true;
			case DEGREE:
				return degrees.get(rowIndex);
		}
		return null;
	}
}