package dc.gui_old;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import dc.process.Controller;

@SuppressWarnings("serial")
public class DriftEditingPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(DriftEditingPanel.class.getName());
	
	private Controller controller;
	private JCheckBox fitBox;
	private JButton deleteBtn;
	private JButton addCuttingBtn;
	private JButton removeCuttingBtn;
	private JTable driftTable;
	private JTable sectionTable;
	
	//TODO: shift this to model
	private DriftModel driftModel;
	private DriftSectionModel sectionModel;
	
	
	public DriftEditingPanel() {
		
		JPanel btnGroup = new JPanel();
		btnGroup.setLayout(new BoxLayout(btnGroup, BoxLayout.Y_AXIS));
		fitBox = new JCheckBox("fit polynomial");
		fitBox.setSelected(true);
		fitBox.addItemListener(new FitBoxListener());
		deleteBtn = new JButton("delete selected value");
		deleteBtn.addActionListener(new RemoveBtnListener());
		addCuttingBtn = new JButton("add selected frame as cutting point");
		addCuttingBtn.addActionListener(new AddCuttingBtnListener());
		removeCuttingBtn = new JButton("remove selected cutting point");
		removeCuttingBtn.addActionListener(new RemoveCuttingBtnListener());
		btnGroup.add(fitBox);
		btnGroup.add(addCuttingBtn);
		btnGroup.add(removeCuttingBtn);
//		btnGroup.add(deleteBtn);
		
		//https://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableDemoProject/src/components/TableDemo.java
		driftModel = new DriftModel();
		sectionModel = new DriftSectionModel();
		
	
		driftTable = new JTable(driftModel);

		driftTable.setCellSelectionEnabled(true);  
		driftTable.putClientProperty("terminateEditOnFocusLost", true);

		sectionTable = new JTable(sectionModel);
		sectionTable.setCellSelectionEnabled(true);
		sectionTable.putClientProperty("terminateEditOnFocusLost", true);

        ListSelectionModel select= driftTable.getSelectionModel();
        select.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JScrollPane scrollPane = new JScrollPane(driftTable);
		driftTable.setFillsViewportHeight(true);
		// for table size
		// https://stackoverflow.com/questions/41629778/fix-height-of-jscrollpane
		scrollPane.setMaximumSize(new Dimension(400,250));
		scrollPane.setPreferredSize(new Dimension(400, 200));
		
		JScrollPane scrollPane2 = new JScrollPane(sectionTable);
		sectionTable.setFillsViewportHeight(true);	
		scrollPane2.setMaximumSize(new Dimension(400,250));
		scrollPane2.setPreferredSize(new Dimension(400, 200));
		add(btnGroup);
		add(scrollPane);
		add(scrollPane2);
	}
	
	protected void setController(Controller controller) {
		this.controller = controller;
		controller.setDriftTableModel(driftModel, sectionModel);
		
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public void updateDriftSectionTable() {
		((AbstractTableModel) sectionTable.getModel()).fireTableDataChanged();
	};
	
	//https://www.codejava.net/java-se/swing/how-to-scroll-jtable-row-to-visible-area-programmatically
	public void setDriftTableVisible(int frameNumber) {
		logger.info("changing table view to frame: "+frameNumber);
		int columnIndex = 0;
		boolean includeSpacing = true;
		Rectangle cellRect = driftTable.getCellRect(frameNumber, columnIndex, includeSpacing);
		driftTable.scrollRectToVisible(cellRect);
	}

	
	// decides if polynomial fitting is used.
	// if set to false, fill up invalid value with linear interpolation
	private class FitBoxListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			boolean blur = DriftEditingPanel.this.fitBox.isSelected();
			DriftEditingPanel.this.controller.setFitting(blur);
		}
	}
	
	// for removing drift value at selected row
	private class RemoveBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			int[] row = driftTable.getSelectedRows(); 
			if (row.length == 1) {
				int targetFrame = row[0];
				DriftEditingPanel.logger.info("removing drift from row " + targetFrame);
				DriftEditingPanel.this.controller.removeDrift(targetFrame);
			}
		}
	}
	
	//
	private class AddCuttingBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			int[] row = driftTable.getSelectedRows(); 
			if (row.length == 1) {
				int targetFrame = row[0];
				DriftEditingPanel.logger.info("adding frame " + targetFrame + "as cutting point");
				DriftEditingPanel.this.controller.addCuttingPoint(targetFrame);
			}
		}
	}
	
	//
	private class RemoveCuttingBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			int[] row = sectionTable.getSelectedRows(); 
			if (row.length == 1) {
				int targetFrame = row[0];
				DriftEditingPanel.logger.info("removing cuttingPoint at index " + targetFrame);
				DriftEditingPanel.this.controller.removeCuttingPoint(targetFrame);
			}
		}
	}
	
//	private class DriftTableEditListener implements CellEditorListener {
//
//		@Override
//		public void editingStopped(ChangeEvent e) {
//			TableCellEditor tcl = (TableCellEditor)e.getSource();
//	        System.out.println("Row   : " + tcl.ge);
//	        System.out.println("Column: " + tcl.getColumn());
//	        System.out.println("Old   : " + tcl.getOldValue());
//	        System.out.println("New   : " + tcl.getNewValue());
//		}
//
//		@Override
//		public void editingCanceled(ChangeEvent e) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	}

	//https://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableDemoProject/src/components/TableDemo.java
	public class DriftModel extends AbstractTableModel {
		private String[] columnNames = {"frame number",
				"dx",
		"dy"};
		private float[] xDrift;
		private float[] yDrift;
		private static final int INDEX = 0;
		private static final int DX = 1;
		private static final int DY = 2;

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
					DriftEditingPanel.this.controller.changeXDrift(row, floatValue);
					break;
				case DY:
					if (yDrift[row] == floatValue) {
						return;
					}
					DriftEditingPanel.this.controller.changeYDrift(row, floatValue);
					break;
				default:
					logger.warning("unknonwn col: " + col);
			}
            fireTableCellUpdated(row, col);

        }
	}

	public class DriftSectionModel extends AbstractTableModel {
		private String[] columnNames = {"index","starting frame","ending frame","fit","fit degree"};
		private static final int INDEX = 0;
		private static final int START = 1;
		private static final int END = 2;
		private static final int FIT = 3;	//TODO: fit option for individual section
		private static final int DEGREE = 4;
		
		private List<Integer> cuttingPoints;
		private List<Integer> degrees;
		private int lastFrame;
		
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
					DriftEditingPanel.this.controller.changeFitDegree(row, intValue);
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


}
