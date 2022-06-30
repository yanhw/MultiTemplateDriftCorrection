package dc.gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import dc.controller.Controller;
import dc.model.DriftModel;
import dc.model.DriftSectionModel;

import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import java.awt.event.ActionListener;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.InputVerifier;
import javax.swing.JSeparator;

@SuppressWarnings("serial")
public class DriftEditingPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(DriftEditingPanel.class.getName());
	
	private Controller controller;
	
	private JTable driftTable;
	private JTable driftSectionTable;
	private JButton addCuttingPointButton;
	private JButton removeCuttingPointButton;
	private JCheckBox fittingBox;
	private JButton runButton;
	
	/**
	 * Create the panel.
	 */
	public DriftEditingPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		add(splitPane, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(400, 200));
		scrollPane.setMaximumSize(new Dimension(400, 250));
		splitPane.setLeftComponent(scrollPane);
		
		driftTable = new JTable();
		scrollPane.setViewportView(driftTable);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setPreferredSize(new Dimension(400, 200));
		scrollPane_1.setMaximumSize(new Dimension(400, 250));
		splitPane.setRightComponent(scrollPane_1);
		
		driftSectionTable = new JTable();
		scrollPane_1.setRowHeaderView(driftSectionTable);
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
			}
		});
		removeCuttingPointButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.fine("triggered run btn");
				boolean falg = DriftEditingPanel.this.fittingBox.isSelected();
				DriftEditingPanel.this.controller.runDriftCorrection(falg);
			}
		});
	}
	
	protected void setDriftModel(DriftModel driftModel) {
		driftTable.setModel(driftModel);
		driftTable.removeColumn(driftTable.getColumn("fitted dx"));
		driftTable.removeColumn(driftTable.getColumn("fitted dy"));
		driftTable.removeColumn(driftTable.getColumn("weight x"));
		driftTable.removeColumn(driftTable.getColumn("weight y"));
		// https://stackoverflow.com/questions/13508851/validate-a-tables-cell-using-editors
		final InputVerifier iv = new InputVerifier() {

		    @Override
		    public boolean verify(JComponent input) {
		        JTextField field = (JTextField) input;
		        float floatValue = 0;
				try {
					floatValue = Float.parseFloat(String.valueOf(field));
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
	}
	
	protected void setDriftSectionModel(DriftSectionModel sectionModel) {
		driftSectionTable.setModel(sectionModel);
		revalidate();
	}
	
	public void updateDriftSectionTable() {
		((AbstractTableModel) driftSectionTable.getModel()).fireTableDataChanged();
	};
	
	//https://www.codejava.net/java-se/swing/how-to-scroll-jtable-row-to-visible-area-programmatically
	public void setDriftTableVisible(int frameNumber) {
		logger.info("changing table view to frame: "+frameNumber);
		int columnIndex = 0;
		boolean includeSpacing = true;
		Rectangle cellRect = driftTable.getCellRect(frameNumber, columnIndex, includeSpacing);
		driftTable.scrollRectToVisible(cellRect);
	}
}
