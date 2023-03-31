package dc.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JTextField;

import dc.controller.Controller;
import dc.model.TextModel;
import dc.utils.Constants;

@SuppressWarnings("serial")
public class IOPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(MainFrame.class.getName());
	private static final String NO_FOLDER_TEXT = "no folder is chosen";
	private static final int FOLDER_FIELD_HEIGHT = 25;
	private static final int FOLDER_FIELD_WIDTH = 450;
	
	private Controller controller;
	private final JFileChooser fileChooser = new JFileChooser();
	private final JTextField inputFilename = new JTextField(NO_FOLDER_TEXT);
	private final JTextField outputFilename = new JTextField(NO_FOLDER_TEXT);
	private final JButton inputBtn = new JButton("Browse");
	private final JButton outputBtn = new JButton("Browse");
	private final JComboBox<String> typeList = new JComboBox<String>(Constants.INPUT_FORMAT);
	private final JCheckBox overwriteBox;
	private DnDLayerUI layerUI;
	private JLayer<JPanel> myLayer;
	
	public IOPanel() {
		logger.setLevel(Level.FINE);
		setLayout(new GridBagLayout());
		
		// input
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		JLabel inputLabel = new JLabel("choose input image folder:", JLabel.LEFT);
		add(inputLabel, c);
		
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		inputFilename.setEnabled(false);
		inputFilename.setMinimumSize(new Dimension(FOLDER_FIELD_WIDTH,FOLDER_FIELD_HEIGHT));
		inputFilename.setPreferredSize(new Dimension(FOLDER_FIELD_WIDTH,FOLDER_FIELD_HEIGHT));
		add(inputFilename, c);

		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		inputBtn.setToolTipText("browse the input image sequence folder or drag folder here");
		add(inputBtn, c);
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_START;
		JLabel filetypeLabel = new JLabel("choose input file type");
		add(filetypeLabel, c);
		
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_START;
		add(typeList, c);
		
		// output
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_START;
		JLabel outputLabel = new JLabel("output directory:", JLabel.LEFT);
		add(outputLabel, c);
		
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		outputFilename.setMinimumSize(new Dimension(FOLDER_FIELD_WIDTH,FOLDER_FIELD_HEIGHT));
		outputFilename.setPreferredSize(new Dimension(FOLDER_FIELD_WIDTH,FOLDER_FIELD_HEIGHT));
		outputFilename.setEnabled(false);
		add(outputFilename, c);
		
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 2;
		outputBtn.setToolTipText("choose the saving location");
		add(outputBtn, c);
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.LINE_START;
		overwriteBox = new JCheckBox("overwrite existing files in output folder");
		overwriteBox.setToolTipText("if selected, existing files in the speficied save folder might be overwritten");
		add(overwriteBox, c);
		
		layerUI = new DnDLayerUI();
		myLayer = new JLayer<JPanel>(this, layerUI);
	}
	
	protected JLayer<JPanel> wrapLayer() {
		return myLayer;
	}
	
	protected void setController(Controller controller) {
		this.controller = controller;
		setHandlers();
	}
	
	private void setHandlers() {
		inputBtn.addActionListener(new InputBtnListener());
		outputBtn.addActionListener(new OutputBtnListener());
		
		@SuppressWarnings("unused")
		DropTarget dt = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new InputDropTargetListener(), true);
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	protected void setOverwriteModel(ButtonModel model) {
		overwriteBox.setModel(model);
	}
	
	protected void setFileNameModels(TextModel inputFileName, TextModel outputFileName) {
		inputFileName.addPropertyChangeListener(new FileNameListener(this.inputFilename));
		outputFileName.addPropertyChangeListener(new FileNameListener(this.outputFilename));
	}
	
	private void setSrcDir(String folder) {
		String filetype = (String) typeList.getSelectedItem();
		controller.setSrcDir(folder, filetype);
	}
	
	private class FileNameListener implements PropertyChangeListener {
		private JTextField label;
		
		public FileNameListener(JTextField label) {
			this.label = label;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String text = (String) evt.getNewValue();
			if (text.length()==0) {
				label.setText(NO_FOLDER_TEXT);
			} else {
				label.setText(text);
			}		
		}
	}
	
	private class InputBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			// TODO: temp solution, need to change before formal version
			Path path = Paths.get("G:\\DriftCorrection\\app\\src\\test\\resources\\");
			if (Files.exists(path)) {
				fileChooser.setSelectedFile(new File("G:\\DriftCorrection\\app\\src\\test\\resources\\"));
			}
			else {
				fileChooser.setSelectedFile(new File(Constants.CBIS_ROOT_DIR));
			}
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setDragEnabled(false);
			int returnVal = fileChooser.showOpenDialog(IOPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				String file = fileChooser.getSelectedFile().getPath();
				// pass to controller for checking and generate output path
				IOPanel.this.setSrcDir(file);
				
			}	// end of APPROVAL_OPTION
		}	// end of method
	}	// end of class
	
	private class OutputBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			fileChooser.setSelectedFile(new File(""));
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setDragEnabled(false);
			int returnVal = fileChooser.showOpenDialog(IOPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				String file = fileChooser.getSelectedFile().getPath();
				
				// pass to controller for checking and generate output path
				IOPanel.this.controller.setSaveDir(file);
				
			}	// end of APPROVAL_OPTION
		}	// end of method
	}	// end of class
	
	private class InputDropTargetListener implements DropTargetListener {
		
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
	                        if (file.isDirectory()) {
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
	                        if (file.isDirectory()) {
	                        	IOPanel.this.setSrcDir(name);
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
