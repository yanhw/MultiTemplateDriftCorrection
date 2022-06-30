package dc.model;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.table.DefaultTableModel;

import dc.controller.Controller;

//https://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableDemoProject/src/components/TableDemo.java
@SuppressWarnings("serial")
public class DriftModel extends DefaultTableModel {
	private static final Logger logger = Logger.getLogger(DriftModel.class.getName());
	private Controller controller;

	public static final int INDEX = 0;
	public static final int DX = 1;
	public static final int DY = 2;
	public static final int FITTED_DX = 3;
	public static final int FITTED_DY = 4;
	public static final int WEIGHT_X = 5;
	public static final int WEIGHT_Y = 6;
	
	public DriftModel() {
		addColumn("frame number");
		addColumn("dx");
		addColumn("dy");
		addColumn("fitted dx");
		addColumn("fitted dy");
		addColumn("weight x");
		addColumn("weight y");
	}
	
	public void initialise(Controller controller, FileHandler fh) {
		logger.addHandler(fh);
		this.controller = controller;
	}
	
	public void initData(int movieSize) {
		setRowCount(0);
		for (int i = 0; i < movieSize; i++) {
			addRow(new Object[] {i, 0.0,0.0,0.0,0.0,1.0,1.0});
		}
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
		logger.info("setting raw data for driftModel");
		for (int i = 0; i < getRowCount(); i++) {
			setValueAt((float)xDrift[i], i, DX);
			setValueAt((float)yDrift[i], i, DY);
			setValueAt((double)1.0, i, WEIGHT_X);
			setValueAt((double)1.0, i, WEIGHT_Y);
		}
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
	
	
//
//	@Override
//	public int getColumnCount() {
//		return 3;
//	}


//
//	@Override
//	public void setValueAt(Object value, int row, int col) {
//
//		if (row >= getRowCount()) {
//			return;
//		}
//
//		float floatValue = 0;
//		try {
//			floatValue = Float.parseFloat(String.valueOf(value));
//			logger.info("value input: " + floatValue);
//		} catch (ClassCastException e) {
//			logger.warning("failed to render");
//			return;
//		} catch (NumberFormatException e) {
//			logger.info("input is not a number: " + value);
//			return;
//		}
//
//		switch(col) {
//		case DX:
//			if (xDrift[row] == floatValue) {
//				return;
//			}
//			controller.changeXDrift(row, floatValue);
//			break;
//		case DY:
//			if (yDrift[row] == floatValue) {
//				return;
//			}
//			controller.changeYDrift(row, floatValue);
//			break;
//		default:
//			logger.warning("unknonwn col: " + col);
//		}
//		fireTableCellUpdated(row, col);
//
//	}
}