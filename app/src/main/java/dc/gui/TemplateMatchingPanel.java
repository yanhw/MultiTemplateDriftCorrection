package dc.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import dc.controller.Controller;
import dc.gui.image.ROIModel;
import dc.model.BooleanModel;
import dc.model.TemplateMatchingSegmentModel;
import dc.utils.Constants;

@SuppressWarnings("serial")
public class TemplateMatchingPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(TemplateMatchingPanel.class.getName());
	private static final String SET_TEMPLATE_TEXT = "set selected region as template";
	private static final String CANNOT_SET_TEMPLATE_TEXT = "need to select template by dragging on input image";
	private static final String REMOVE_TEMPLATE_TEXT = "remove template from the selected segment";
	private static final String CANNOT_REMOVE_TEMPLATE_TEXT = "no template in the selected segment";
	private static final String SET_SECTION_TEXT = "set the currently displayed frame as the start of a segment. Each segment uses a different template for template matching";
	private static final String CANNOT_SET_SECTION_TEXT = "the currently displayed frame is already the first frame of a segment";
	private static final String REMOVE_SECTION_TEXT = "Remove the selected row as a segment. The removed segment will be merged with previous segment";
	private static final String CANNOT_REMOVE_SECTION_TEXT = "the first segment cannot be removed";
	private static final String RUN_TEXT = "click to run template matching";
	private static final String CANCEL_TEXT = "click to cancel template matching";
	private static final String CANNOT_RUN_TEXT = "need to set template for all segments before running";
	
	private Controller controller;
	
	private final JTable table = new JTable();
	private final JButton setTemplateButton = new JButton("Set Template");
	private final JButton removeTemplateButton = new JButton("Remove Template");
	private final JButton setSectionButton = new JButton("Set Segment");
	private final JButton removeSectionButton = new JButton("Remove Segment");
	private final JCheckBox blurCheckBox = new JCheckBox("Blur Image");
	private final JButton runButton = new JButton("Run");
	private final JButton loadDriftButton = new JButton("Load Drift");
	private DnDLayerUI layerUI;
	private JLayer<JPanel> myLayer;
	
//	private TemplateMatchingSegmentModel model;
	private int currentFrame = 0;
	private boolean hasROI = false;
	private int[] ROI = {0,0,0,0};
	
	private int selectedRow = 0;			// the selected row in table
	
	private boolean runningFlag = false;	// if template matching is already running
	private boolean enableFlag = false;		// if running condition for template matching is met
	
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
		
		scrollPane.setViewportView(table);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.EAST);
		panel.setLayout(new GridLayout(0, 1, 0, 0));

		setTemplateButton.setToolTipText(CANNOT_SET_TEMPLATE_TEXT);
		setTemplateButton.setEnabled(false);
		panel.add(setTemplateButton);

		removeTemplateButton.setEnabled(false);
		removeTemplateButton.setToolTipText(CANNOT_REMOVE_TEMPLATE_TEXT);
		panel.add(removeTemplateButton);
		
		setSectionButton.setEnabled(false);
		setSectionButton.setToolTipText(CANNOT_SET_SECTION_TEXT);
		panel.add(setSectionButton);
		
		removeSectionButton.setEnabled(false);
		removeSectionButton.setToolTipText(CANNOT_REMOVE_SECTION_TEXT);
		panel.add(removeSectionButton);

		panel.add(new JSeparator());
		
		blurCheckBox.setToolTipText("use Guassian blur for template matching");
		panel.add(blurCheckBox);
		
		runButton.setEnabled(false);
		runButton.setToolTipText(CANNOT_RUN_TEXT);
		panel.add(runButton);
		
		loadDriftButton.setToolTipText("Load drift infomation from existing csv file. Click to choose file or drag a file here");
		panel.add(loadDriftButton);
		
		layerUI = new DnDLayerUI();
		myLayer = new JLayer<JPanel>(this, layerUI);
	}
	
	protected JLayer<JPanel> wrapLayer() {
		return myLayer;
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
		table.getTableHeader().setReorderingAllowed(false);
		setHandlers();
		model.addTableModelListener(new DriftTableListener());
		select.addListSelectionListener(new DriftTableSelectionListener());
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
					fileChooser.setSelectedFile(new File(Constants.CBIS_ROOT_DIR));
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
		
		@SuppressWarnings("unused")
		DropTarget dt = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new CsvDropTargetListener(), true);
	}
	
	protected void setRawFrameModel(BoundedRangeModel model) {
		model.addChangeListener(new RawImageListener());
	}
	
	protected void setROIModel(ROIModel model) {
		model.addPropertyChangeListener(new ROIChangeListener());
	}
	
	public void setRunningFlagModel(BooleanModel model) {
		model.addPropertyChangeListener(new RunningFlagChangeListener());
	}
	
	private void setSetTemplateBtn() {
		if (hasROI) {
			setTemplateButton.setToolTipText(SET_TEMPLATE_TEXT);
			setTemplateButton.setEnabled(true);
		} else {
			setTemplateButton.setToolTipText(CANNOT_SET_TEMPLATE_TEXT);
			setTemplateButton.setEnabled(false);
		}
	}
	

	private void setRemoveTemplateBtn() {
		TemplateMatchingSegmentModel model = (TemplateMatchingSegmentModel) table.getModel();
		// must check the size of model first because the model might be changing
		if (model.getRowCount() <= selectedRow || (boolean) model.getValueAt(selectedRow, TemplateMatchingSegmentModel.HAS_TEMPLATE_IDX)) {
			removeTemplateButton.setEnabled(true);
			removeTemplateButton.setToolTipText(REMOVE_TEMPLATE_TEXT);
		} else {
			removeTemplateButton.setEnabled(false);
			removeTemplateButton.setToolTipText(CANNOT_REMOVE_TEMPLATE_TEXT);
		}
	}
	
	private void setAddSectionBtn() {
		boolean flag = true;
		TemplateMatchingSegmentModel model = (TemplateMatchingSegmentModel) table.getModel();
		for (int i = 0; i< model.getRowCount(); i++) {
			if ((int) model.getValueAt(i, TemplateMatchingSegmentModel.START_IDX) == currentFrame) {
				flag = false;
				break;
			}
		}
		
		if (flag) {
			setSectionButton.setEnabled(true);
			setSectionButton.setToolTipText(SET_SECTION_TEXT);
		} else {
			setSectionButton.setEnabled(false);
			setSectionButton.setToolTipText(CANNOT_SET_SECTION_TEXT);
		}
	}

	private void setRemoveSectionBtn() {
		if (selectedRow == 0 || selectedRow == -1) {
			removeSectionButton.setEnabled(false);
			removeSectionButton.setToolTipText(CANNOT_REMOVE_SECTION_TEXT);
		} else {
			removeSectionButton.setEnabled(true);
			removeSectionButton.setToolTipText(REMOVE_SECTION_TEXT);
		}
	}
	
	private void setRunBtn() {
		if (enableFlag) {
			runButton.setEnabled(true);
			if (!runningFlag) {
				runButton.setText("RUN");
				runButton.setToolTipText(RUN_TEXT);
			} else {
				runButton.setText("STOP");
				runButton.setToolTipText(CANCEL_TEXT);
			}
		} else {
			runButton.setEnabled(false);
			runButton.setText("RUN");
			runButton.setToolTipText(CANNOT_RUN_TEXT);
		}
	}
	
	private class DriftTableSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			int selected = TemplateMatchingPanel.this.table.getSelectedRow();
			TemplateMatchingPanel.this.selectedRow = selected;
			TemplateMatchingPanel.this.setRemoveTemplateBtn();
			TemplateMatchingPanel.this.setRemoveSectionBtn();
		}
	}
	
	private class DriftTableListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent e) {
			TemplateMatchingSegmentModel model = (TemplateMatchingSegmentModel) table.getModel();
			for (int i = 0; i< model.getRowCount(); i++) {
				if (!(boolean) model.getValueAt(i, TemplateMatchingSegmentModel.HAS_TEMPLATE_IDX)) {
					TemplateMatchingPanel.this.enableFlag = false;
					TemplateMatchingPanel.this.setRunBtn();
					TemplateMatchingPanel.this.setRemoveTemplateBtn();
					TemplateMatchingPanel.this.setRemoveSectionBtn();
					TemplateMatchingPanel.this.setAddSectionBtn();
					return;
				}
			}
			TemplateMatchingPanel.this.enableFlag = true;
			TemplateMatchingPanel.this.setRunBtn();
			TemplateMatchingPanel.this.setRemoveTemplateBtn();
			TemplateMatchingPanel.this.setRemoveSectionBtn();
			TemplateMatchingPanel.this.setAddSectionBtn();
		}
		
	}
	
	private class RawImageListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			BoundedRangeModel source = (BoundedRangeModel)e.getSource();
	        int frameNumber = (int)source.getValue();
	        TemplateMatchingPanel.this.currentFrame = frameNumber;
	        TemplateMatchingPanel.this.setAddSectionBtn();
		}
		
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
					TemplateMatchingPanel.this.setSetTemplateBtn();
			}
		}
		
	}

	private class RunningFlagChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			boolean flag = (boolean) evt.getNewValue();
			TemplateMatchingPanel.this.runningFlag = flag;
			TemplateMatchingPanel.this.setRunBtn();
		}

	}


	private class CsvDropTargetListener implements DropTargetListener {
			
		@SuppressWarnings("rawtypes")
		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			boolean flag = false;
			Transferable t = dtde.getTransferable();
			if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				try {
                    Object td = t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (td instanceof List && ((List) td).size() == 1) {
                    	Object value = ((List) td).get(0);
	                    if (value instanceof File) {
	                    	File file = (File) value;
	                        String name = file.getName();
	                        if (name.endsWith(".csv")) {
	                        	flag = true;
	                        }
                    	}
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
			}
			if (flag) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
                layerUI.setIsDragging(true);
            } else {
                dtde.rejectDrag();
                layerUI.setIsDragging(false);
            }
            repaint();
            myLayer.repaint();
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
			layerUI.setIsDragging(false);
			myLayer.repaint();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void drop(DropTargetDropEvent dtde) {
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			Transferable t = dtde.getTransferable();
			if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				try {
                    Object td = t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (td instanceof List && ((List) td).size() == 1) {
                    	Object value = ((List) td).get(0);
	                    if (value instanceof File) {
	                    	File file = (File) value;
	                        String name = file.getPath();
	                        if (name.endsWith(".csv")) {
	                        	TemplateMatchingPanel.this.controller.setDriftCsv(name);
	                        }
                    	}
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
			}
			layerUI.setIsDragging(false);
			myLayer.repaint();
		}
		
	}

}
