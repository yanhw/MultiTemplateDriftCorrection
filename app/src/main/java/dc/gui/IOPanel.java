package dc.gui;

import java.awt.FlowLayout;
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
import java.awt.event.KeyEvent;
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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;

import dc.controller.Controller;
import dc.model.TextModel;

@SuppressWarnings("serial")
public class IOPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(MainFrame.class.getName());
	private Controller controller;
	private JFileChooser fileChooser;
	private JLabel inputFilename;
	private JLabel outputFilename;
	private JButton inputBtn;
	private JButton outputBtn;
	private JCheckBox overwriteBox;
	private DnDLayerUI layerUI;
	private JLayer<JPanel> myLayer;
	
	public IOPanel() {
		logger.setLevel(Level.FINE);
		fileChooser = new JFileChooser();

		// input
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		inputBtn = new JButton("choose directory");
		inputBtn.setToolTipText("choose the image sequence or drag folder here");
		inputPanel.add(inputBtn);
		JLabel inputLabel = new JLabel("image directory:", JLabel.LEFT);
		inputPanel.add(inputLabel);
		inputFilename = new JLabel();
		inputPanel.add(inputFilename);

		overwriteBox = new JCheckBox("overwrite existing files");
		overwriteBox.setMnemonic(KeyEvent.VK_O);
		overwriteBox.setSelected(true);
		overwriteBox.setEnabled(false);
		
		// output
		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		outputBtn = new JButton("choose directory");
		outputBtn.setToolTipText("choose the saving location");
		outputPanel.add(outputBtn);
		JLabel outputLabel = new JLabel("output directory:", JLabel.LEFT);
		outputPanel.add(outputLabel);
		outputFilename = new JLabel();
		outputPanel.add(outputFilename);
		
		setLayout(new GridLayout(0, 1, 0, 0));
		
		add(inputPanel);
		add(outputPanel);
		//TODO: overwrite warning
//		add(overwriteBox);
		
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
	
	protected void setFileNameModels(TextModel inputFileName, TextModel outputFileName) {
		inputFileName.addPropertyChangeListener(new FileNameListener(this.inputFilename));
		outputFileName.addPropertyChangeListener(new FileNameListener(this.outputFilename));
	}
	
	private class FileNameListener implements PropertyChangeListener {
		private JLabel label;
		
		public FileNameListener(JLabel label) {
			this.label = label;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String text = (String) evt.getNewValue();
			label.setText(text);
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
				fileChooser.setSelectedFile(new File("/gpfs0/scratch/utkur/"));
			}
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setDragEnabled(false);
			int returnVal = fileChooser.showOpenDialog(IOPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				String file = fileChooser.getSelectedFile().getPath();
				
				// pass to controller for checking and generate output path
				IOPanel.this.controller.setSrcDir(file);
				
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
	                        	IOPanel.this.controller.setSrcDir(name);
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
