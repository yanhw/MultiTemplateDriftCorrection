package dc.model;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import dc.controller.Controller;

//https://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableDemoProject/src/components/TableDemo.java
@SuppressWarnings("serial")
public class DriftModel extends AbstractTableModel {
	private static final Logger logger = Logger.getLogger(DriftModel.class.getName());
	private Controller controller;
	
	private String[] columnNames = {"frame number",
									"dx",
									"dy"};
	private float[] xDrift;
	private float[] yDrift;
	private static final int INDEX = 0;
	private static final int DX = 1;
	private static final int DY = 2;
	
	public void initialise(Controller controller, FileHandler fh) {
		logger.addHandler(fh);
		this.controller = controller;
	}

	public void setData(float[] xDrift, float[] yDrift) {
		if (xDrift == null && yDrift == null) {
			this.xDrift = xDrift;
			this.yDrift = yDrift;
			logger.fine("setting null array as drift");
			return;
		} else if (xDrift == null || yDrift == null) {
			logger.warning("setting inconsistant drift array");
			return;
		}
		if (xDrift.length != yDrift.length) {
			logger.severe("drift lengths mismatch: " + xDrift.length + " " + yDrift.length);
			return;
		}
		logger.info("setting data for driftModel");
		this.xDrift = xDrift;
		this.yDrift = yDrift;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
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
	public int getRowCount() {
		if (xDrift == null) {
			return 0;
		}
		return xDrift.length;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (xDrift == null) {
			return null;
		}
		if (rowIndex >= xDrift.length) {
			return null;
		}
		switch (columnIndex) {
		case INDEX:
			return rowIndex;
		case DX:
			return xDrift[rowIndex];
		case DY:
			return yDrift[rowIndex];
		default:
			return null;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (xDrift == null) {
			return;
		}
		if (row >= xDrift.length) {
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
			logger.info("input is not a number");
			return;
		}

		switch(col) {
		case DX:
			// this is not necessary, but improves performance, handled here because it is considered as GUI event
			if (xDrift[row] == floatValue) {
				return;
			}
			controller.changeXDrift(row, floatValue);
			break;
		case DY:
			if (yDrift[row] == floatValue) {
				return;
			}
			controller.changeYDrift(row, floatValue);
			break;
		default:
			logger.warning("unknonwn col: " + col);
		}
		fireTableCellUpdated(row, col);

	}
}