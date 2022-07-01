package dc.model;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;


//https://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableDemoProject/src/components/TableDemo.java
@SuppressWarnings("serial")
public class DriftModel extends AbstractTableModel {
	private static final Logger logger = Logger.getLogger(DriftModel.class.getName());

	public static final int INDEX = 0;
	public static final int DX = 1;
	public static final int DY = 2;
	public static final int FITTED_DX = 3;
	public static final int FITTED_DY = 4;
	public static final int WEIGHT_X = 5;
	public static final int WEIGHT_Y = 6;
	private static final String[] COLUMN_NAMES = {"frame number", "dx", "dy", "fitted dx", "fitted dy", "weight x", "weight y"};
	
	private Object[][] dataVector;
	
	
	public DriftModel() {
	}
	
	public void initialise(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public void initData(int movieSize) {
		dataVector = new Object[movieSize][getColumnCount()];
		for (int i = 0; i < movieSize; i++) {
			dataVector[i][INDEX] = i;
			dataVector[i][DX] = (float)0.0;
			dataVector[i][DY] = (float)0.0;
			dataVector[i][FITTED_DX] = (float)0.0;
			dataVector[i][FITTED_DY] = (float)0.0;
			dataVector[i][WEIGHT_X] = (double)1.0;
			dataVector[i][WEIGHT_Y] = (double)1.0;
		}
		fireTableRowsInserted(0, getRowCount()-1);
	}
	
	public void setData(float[] xDrift, float[] yDrift) {
		if (xDrift == null && yDrift == null) {
			logger.info("setting null array as drift");
			return;
		} else if (xDrift == null || yDrift == null) {
			logger.warning("setting inconsistant drift array");
			return;
		}
		if (xDrift.length != yDrift.length) {
			logger.severe("drift lengths mismatch: " + xDrift.length + " " + yDrift.length);
			return;
		}
		if (xDrift.length != getRowCount()) {
			logger.warning("size mismatch!");
			return;
		}
		
		logger.info("setted raw data for driftModel "+getRowCount() + " rows");
		for (int i = 0; i < getRowCount(); i++) {
			dataVector[i][DX] = (float)xDrift[i];
			dataVector[i][DY] = (float)yDrift[i];
			dataVector[i][WEIGHT_X] = (double)1.0;
			dataVector[i][WEIGHT_Y] = (double)1.0;
		}
		fireTableRowsUpdated(0, getRowCount()-1);
	}
	
	public void removeDrift(int frameNumber) {
		if (frameNumber < 0 || frameNumber >= getRowCount()) {
			return;
		}
		dataVector[frameNumber][FITTED_DX] = (double)0.0;
		dataVector[frameNumber][FITTED_DY] = (double)0.0;
		dataVector[frameNumber][WEIGHT_X] = (double)0.0;
		dataVector[frameNumber][WEIGHT_Y] = (double)0.0;
		fireTableCellUpdated(frameNumber, WEIGHT_X);
		fireTableCellUpdated(frameNumber, WEIGHT_Y);
	}
	
	public void setDrift(double[] value, int start, int end, int col) {
		for (int i = start; i <= end; i++) {
			dataVector[i][col] = value[i-start];
		}
		TableModelEvent e = new TableModelEvent(this, start, end, col);
		fireTableChanged(e);
	}
	
	@Override
	public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }
	
	@Override
	public boolean isCellEditable(int row, int column) {
		// first column is frame index, this cannot be edited
		if (column >= 1) {
			return true; 
		}
		return false;
	}

	@Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case INDEX:
                return Integer.class;
            case DX:
            case DY:
            case FITTED_DX:
            case FITTED_DY:
                return Float.class;
            case WEIGHT_X:
            case WEIGHT_Y:
                return Double.class;
            default:
                return null;
        }
    }

	@Override
	public int getRowCount() {
		if (dataVector == null) {
			return 0;
		}
		return dataVector.length;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return dataVector[rowIndex][columnIndex];
	}


	@Override
	// this handles only DX and DY
	public void setValueAt(Object value, int row, int col) {

		if (row >= getRowCount()) {
			return;
		}

		float floatValue = 0;
		try {
			floatValue = Float.parseFloat(String.valueOf(value));
			logger.info("value input: " + floatValue);
		} catch (ClassCastException e) {
			logger.warning("failed to render");
			return;
		} catch (NumberFormatException e) {
			logger.info("input is not a number: " + value);
			return;
		}

		switch(col) {
		case DX:
			if (((Number) dataVector[row][DX]).floatValue() == floatValue) {
				return;
			}
			dataVector[row][DX] = floatValue;
			break;
		case DY:
			if (((Number) dataVector[row][DY]).floatValue() == floatValue) {
				return;
			}
			dataVector[row][DY] = floatValue;
			break;
		default:
			logger.warning("invalid col: " + col);
		}
		fireTableCellUpdated(row, col);

	}
}