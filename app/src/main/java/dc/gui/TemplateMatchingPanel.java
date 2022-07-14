package dc.gui;

import javax.swing.JPanel;
import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import dc.controller.Controller;
import dc.gui.image.ROIModel;
import dc.model.BooleanModel;
import dc.model.TemplateMatchingSegmentModel;

import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import java.awt.GridLayout;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class TemplateMatchingPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(TemplateMatchingPanel.class.getName());
	private Controller controller;
	
	private JTable table;
	private JButton setTemplateButton;
	private JButton removeTemplateButton;
	private JButton setSectionButton;
	private JButton removeSectionButton;
	private JCheckBox blurCheckBox;
	private JButton runButton;
	private JButton loadDriftButton;
	
//	private TemplateMatchingSegmentModel model;
	private int currentFrame = 0;
	private boolean hasROI = false;
	private int[] ROI = {0,0,0,0};
	
	private boolean runningFlag = false;
	private boolean enableFlag = false;
	
	/**
	 * Create the panel.
	 */
	public TemplateMatchingPanel() {
		setMaximumSize(new Dimension(32767, 400));
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(400, 200));
		scrollPane.setMaximumSize(new Dimension(400, 250));
		add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.EAST);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		setTemplateButton = new JButton("Set Template");
		setTemplateButton.setToolTipText("set selected region as template");
		panel.add(setTemplateButton);
		
		removeTemplateButton = new JButton("Remove Template");
		panel.add(removeTemplateButton);
		
		setSectionButton = new JButton("Set Section");
		setSectionButton.setToolTipText("Set the current frame as the start of a section. Each section uses a different template for template matching");
		panel.add(setSectionButton);
		
		removeSectionButton = new JButton("Remove Section");
		removeSectionButton.setToolTipText("Remove the selected row as a section. The removed section will be merged with previous section");
		panel.add(removeSectionButton);
		
		JSeparator separator = new JSeparator();
		panel.add(separator);
		
		blurCheckBox = new JCheckBox("Blur Image");
		blurCheckBox.setToolTipText("use Guassian blur for template matching");
		panel.add(blurCheckBox);
		
		runButton = new JButton("Run");
		runButton.setEnabled(false);
		runButton.setToolTipText("Must set template for all sections first!");
		panel.add(runButton);
		
		loadDriftButton = new JButton("Load Drift");
		loadDriftButton.setToolTipText("Load drift infomation from existing csv file");
		panel.add(loadDriftButton);

	}
	
	protected void setController(Controller controller) {
		this.controller = controller;

	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public void setTableModel(TemplateMatchingSegmentModel model) {
//		this.model = model;
		table.setCellSelectionEnabled(true);  
		// don't do this. this disables selection, overwrite isCellEditable instead
//		table.setEnabled(false);	
        ListSelectionModel select= table.getSelectionModel();
        select.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        select.addListSelectionListener(new TableSelectionListener());  

		table.setFillsViewportHeight(true);
		table.setModel(model);
		table.removeColumn(table.getColumn("top"));
		table.removeColumn(table.getColumn("bottom"));
		table.removeColumn(table.getColumn("left"));
		table.removeColumn(table.getColumn("right"));
		setHandlers();
		model.addTableModelListener(new DriftTableListener());
	}
	
	private void setHandlers() {
		setTemplateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TemplateMatchingPanel.logger.info("setting template " + hasROI);
				TemplateMatchingPanel.this.controller.setTemplate(currentFrame, ROI, hasROI);
			}
		});
		removeTemplateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] row = table.getSelectedRows(); 
				if (row.length == 1) {
					int targetSegment = row[0];
					TemplateMatchingPanel.logger.info("removing ROI from row " + targetSegment);
					TemplateMatchingPanel.this.controller.removeTemplate(targetSegment);
				}
			}
		});
		setSectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TemplateMatchingPanel.this.controller.setSegmentFrame(currentFrame);
			}
		});
		removeSectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] row = table.getSelectedRows(); 
				if (row.length == 1) {
					int targetSegment = row[0];
					TemplateMatchingPanel.logger.info("removing selected row " + targetSegment);
					TemplateMatchingPanel.this.controller.removeSegmentFrame(targetSegment);
				}
			}
		});
		
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean blur = TemplateMatchingPanel.this.blurCheckBox.isSelected();
				TemplateMatchingPanel.this.controller.runTemplateMatching(blur);
			}
		});
		loadDriftButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				// TODO: temp solution, need to change before formal version
				Path path = Paths.get("G:\\DriftCorrection\\app\\src\\test\\resources\\");
				if (Files.exists(path)) {
					fileChooser.setSelectedFile(new File("G:\\DriftCorrection\\app\\src\\test\\resources\\"));
				}
				else {
					fileChooser.setSelectedFile(new File("/gpfs0/scratch/utkur/"));
				}
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setDragEnabled(false);
				int returnVal = fileChooser.showOpenDialog(TemplateMatchingPanel.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String file = fileChooser.getSelectedFile().getPath();
					
					TemplateMatchingPanel.this.controller.setDriftCsv(file);
				}	// end of APPROVAL_OPTION
				
			}
		});
	}
	
	
	private class DriftTableListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent e) {
			TemplateMatchingSegmentModel model = (TemplateMatchingSegmentModel) table.getModel();
			for (int i = 0; i< model.getRowCount(); i++) {
				if (!(boolean) model.getValueAt(i, TemplateMatchingSegmentModel.HAS_TEMPLATE_IDX)) {
					enableFlag = false;
					TemplateMatchingPanel.this.setRunBtn();
					return;
				}
			}
			enableFlag = true;
			TemplateMatchingPanel.this.setRunBtn();
		}
		
	}
	

	protected void setRawFrameModel(BoundedRangeModel model) {
		model.addChangeListener(new RawImageListener());
	}
	
	private class RawImageListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			BoundedRangeModel source = (BoundedRangeModel)e.getSource();
	        int frameNumber = (int)source.getValue();
	        currentFrame = frameNumber;
		}
		
	}
	
	public void setROIModel(ROIModel model) {
		model.addPropertyChangeListener(new ROIChangeListener());
	}
	
	private class ROIChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String name = evt.getPropertyName();
			switch (name) {
				case ROIModel.ARRAY:
					int[] ROI = (int[]) evt.getNewValue();
					TemplateMatchingPanel.this.ROI = ROI;
					break;
				case ROIModel.FLAG:
					boolean flag = (boolean) evt.getNewValue();
					TemplateMatchingPanel.this.hasROI = flag;
			}
		}
		
	}
	
	public void setRunningFlagModel(BooleanModel model) {
		model.addPropertyChangeListener(new RunningFlagChangeListener());
	}

	private class RunningFlagChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			boolean flag = (boolean) evt.getNewValue();
			TemplateMatchingPanel.this.runningFlag = flag;
			TemplateMatchingPanel.this.setRunBtn();
		}

	}
	
	
	private void setRunBtn() {
		if (enableFlag) {
			runButton.setEnabled(true);
			if (!runningFlag) {
				runButton.setText("RUN");
				runButton.setToolTipText("click to run template matching");
			} else {
				runButton.setText("STOP");
				runButton.setToolTipText("click to cancel template matching");
			}
		} else {
			runButton.setEnabled(false);
			runButton.setText("RUN");
			runButton.setToolTipText("need to set template for all segments before running");
		}
	}



}
