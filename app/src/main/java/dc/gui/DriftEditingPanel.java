package dc.gui;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Cursor;

import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ButtonModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.InputVerifier;
import javax.swing.JSeparator;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;

import dc.controller.Controller;
import dc.model.BooleanModel;
import dc.model.DriftModel;
import dc.model.DriftSectionModel;


@SuppressWarnings("serial")
public class DriftEditingPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(DriftEditingPanel.class.getName());
	
	private Controller controller;
	
	private JTable driftTable;
	private JTable driftSectionTable;
	private JButton addCuttingPointButton;
	private JButton removeCuttingPointButton;
	private JCheckBox fittingBox;
	private JCheckBox overwriteBox;
	private JButton runButton;
	
	/**
	 * Create the panel.
	 */
	public DriftEditingPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		add(splitPane, BorderLayout.CENTER);
		
		JPanel leftGroup = new JPanel();
		leftGroup.setLayout(new BorderLayout(0, 0));
		leftGroup.add(new JLabel("detected drift"), BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(400, 200));
		scrollPane.setMaximumSize(new Dimension(400, 250));
		leftGroup.add(scrollPane, BorderLayout.CENTER);
		splitPane.setLeftComponent(leftGroup);

		driftTable = new JTable();
		scrollPane.setViewportView(driftTable);
		
		JPanel rightGroup = new JPanel();
		rightGroup.setLayout(new BorderLayout(0, 0));
		rightGroup.add(new JLabel("drift sections for fitting"), BorderLayout.NORTH);
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setPreferredSize(new Dimension(400, 200));
		scrollPane_1.setMaximumSize(new Dimension(400, 250));
		rightGroup.add(scrollPane_1, BorderLayout.CENTER);
		splitPane.setRightComponent(rightGroup);
		
		
		driftSectionTable = new JTable();
		scrollPane_1.setViewportView(driftSectionTable);
		splitPane.setDividerLocation(0.5);
		splitPane.setResizeWeight(0.5);
		
		JPanel buttonPanel = new JPanel();
		add(buttonPanel, BorderLayout.EAST);
		
		addCuttingPointButton = new JButton("Add Cutting Point");
		buttonPanel.setLayout(new GridLayout(8, 1, 0, 0));
		addCuttingPointButton.setToolTipText("set selected frame as a cutting point");
		buttonPanel.add(addCuttingPointButton);
		
		removeCuttingPointButton = new JButton("Remove Cutting Point");
		removeCuttingPointButton.setToolTipText("select a section to merge it with the previous section");
		buttonPanel.add(removeCuttingPointButton);
		
		fittingBox = new JCheckBox("use fitted drift");
		fittingBox.setSelected(true);
		fittingBox.setToolTipText("if selected, fitted drift will be used for drift correction. Otherwise, raw drift will be used.");
		buttonPanel.add(fittingBox);
		
		overwriteBox = new JCheckBox("overwrite exiting files");
		overwriteBox.setToolTipText("if selected, existing files in the speficied save directory might be overwrited");
		buttonPanel.add(overwriteBox);
		
		JSeparator separator = new JSeparator();
		buttonPanel.add(separator);
		
		runButton = new JButton("Run Drift Correction");
		buttonPanel.add(runButton);
		
	}
	
	
	protected void setController(Controller controller) {
		this.controller = controller;
		setHandlers();
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	private void setHandlers() {
		addCuttingPointButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] row = driftTable.getSelectedRows(); 
				if (row.length == 1) {
					int targetFrame = row[0];
					DriftEditingPanel.logger.info("adding frame " + targetFrame + "as cutting point");
					DriftEditingPanel.this.controller.addCuttingPoint(targetFrame);
				}
			}
		});
		removeCuttingPointButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] row = driftSectionTable.getSelectedRows(); 
				if (row.length == 1) {
					int targetFrame = row[0];
					DriftEditingPanel.logger.info("removing cuttingPoint at index " + targetFrame);
					DriftEditingPanel.this.controller.removeCuttingPoint(targetFrame);
				}
			}
		});
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.fine("triggered run btn");
				boolean flag = DriftEditingPanel.this.fittingBox.isSelected();
				boolean overwriteFlag = DriftEditingPanel.this.overwriteBox.isSelected();
				DriftEditingPanel.this.controller.runDriftCorrection(flag, overwriteFlag);
			}
		});
	}
	
	protected void setDriftModel(DriftModel driftModel) {
		driftTable.setModel(driftModel);
		driftTable.removeColumn(driftTable.getColumn("fitted dx"));
		driftTable.removeColumn(driftTable.getColumn("fitted dy"));
		driftTable.removeColumn(driftTable.getColumn("weight x"));
		driftTable.removeColumn(driftTable.getColumn("weight y"));
		driftTable.getTableHeader().setReorderingAllowed(false);
		
		// https://stackoverflow.com/questions/13508851/validate-a-tables-cell-using-editors
		final InputVerifier iv = new InputVerifier() {

		    @Override
		    public boolean verify(JComponent input) {
		        JTextField field = (JTextField) input;
		        String text = field.getText();
		        float floatValue = 0;
				try {
					floatValue = Float.parseFloat(text);
					logger.info("value input: " + floatValue);
				} catch (ClassCastException e) {
					logger.warning("failed to render");
					return false;
				} catch (NumberFormatException e) {
					logger.info("input is not a number: " + field);
					return false;
				}
		        return true;
		    }
		    
		    @Override
		    public boolean shouldYieldFocus(JComponent input) {
		        boolean valid = verify(input);
		        if (!valid) {
		        	logger.fine("invalid input: " + input);
		        }
		        return valid;
		    }

		};
		DefaultCellEditor editor = new DefaultCellEditor(new JTextField()) {
		    {
		        getComponent().setInputVerifier(iv);
		    }

		    @SuppressWarnings("deprecation")
			@Override
		    public boolean stopCellEditing() {
		        if (!iv.shouldYieldFocus(getComponent())) return false;
		        return super.stopCellEditing();
		    }

		    @Override
		    public JTextField getComponent() {
		        return (JTextField) super.getComponent();
		    }

		};
		driftTable.setDefaultEditor(Object.class, editor);
		
		ListSelectionModel select= driftTable.getSelectionModel();
        select.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        driftTable.addMouseMotionListener(new MouseAdapter() {
        	@Override
        	public void mouseMoved(MouseEvent e) {
        		Point panelPoint = e.getPoint();
        		int col = driftTable.columnAtPoint(panelPoint);
        		if (col == DriftModel.DX || col == DriftModel.DY) {
        			driftTable.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        		} else {
        			driftTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        		}
        	}
        });
	}
	
	protected void setDriftSectionModel(DriftSectionModel sectionModel) {
		driftSectionTable.setModel(sectionModel);
		driftSectionTable.removeColumn(driftSectionTable.getColumn("fit"));
		driftSectionTable.getTableHeader().setReorderingAllowed(false);
		
		final InputVerifier iv = new InputVerifier() {

		    @Override
		    public boolean verify(JComponent input) {
		        JTextField field = (JTextField) input;
		        String text = field.getText();
		        int intValue = 0;
				try {
					intValue = Integer.parseInt(text);
					logger.info("value input: " + intValue);
				} catch (ClassCastException e) {
					logger.warning("failed to render");
					return false;
				} catch (NumberFormatException e) {
					logger.info("input is not a number: " + text);
					return false;
				}
				if (intValue > 0 && intValue <= DriftSectionModel.MAXFITTINGDEGREE) {
					 return true;
				}
		        return false;
		    }
		    
		    @Override
		    public boolean shouldYieldFocus(JComponent input) {
		        boolean valid = verify(input);
		        if (!valid) {
		        	logger.fine("invalid input: " + input);
		        }
		        return valid;
		    }

		};
		DefaultCellEditor editor = new DefaultCellEditor(new JTextField()) {
		    {
		        getComponent().setInputVerifier(iv);
		    }

		    @SuppressWarnings("deprecation")
			@Override
		    public boolean stopCellEditing() {
		        if (!iv.shouldYieldFocus(getComponent())) return false;
		        return super.stopCellEditing();
		    }

		    @Override
		    public JTextField getComponent() {
		        return (JTextField) super.getComponent();
		    }

		};
		driftSectionTable.setDefaultEditor(Object.class, editor);
		ListSelectionModel select= driftSectionTable.getSelectionModel();
        select.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        driftSectionTable.addMouseMotionListener(new MouseAdapter() {
        	@Override
        	public void mouseMoved(MouseEvent e) {
        		Point panelPoint = e.getPoint();
        		int col = driftSectionTable.columnAtPoint(panelPoint);
        		if (col == DriftSectionModel.DEGREE) {
        			driftSectionTable.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        		} else {
        			driftSectionTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        		}
        	}
        });
	}
	
	protected void setOverwriteModel(ButtonModel model) {
		overwriteBox.setModel(model);
	}
	
	protected void setPlotSelectionModel(DefaultListSelectionModel model) {
		model.addListSelectionListener(new PlotListener());
	}
	
	private class PlotListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				setDriftTableVisible(e.getFirstIndex());
			}			
		}
		
	}
	
	//https://www.codejava.net/java-se/swing/how-to-scroll-jtable-row-to-visible-area-programmatically
	private void setDriftTableVisible(int frameNumber) {
		logger.info("changing table view to frame: "+frameNumber);
		int columnIndex = 0;
		boolean includeSpacing = true;
		Rectangle cellRect = driftTable.getCellRect(frameNumber, columnIndex, includeSpacing);
		driftTable.scrollRectToVisible(cellRect);
	}


	protected void setRunningFlagModel(BooleanModel model) {
		model.addPropertyChangeListener(new RunningFlagChangeListener());
	}
	
	private class RunningFlagChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			boolean flag = (boolean) evt.getNewValue();
			if (flag) {
				DriftEditingPanel.this.runButton.setText("Stop Drift Correction");
			} else {
				DriftEditingPanel.this.runButton.setText("Run Drift Correction");
			}
		}
		
	}
}
