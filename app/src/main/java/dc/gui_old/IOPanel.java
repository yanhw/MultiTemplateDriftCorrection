package dc.gui_old;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dc.process.Controller;

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
	
	
	public IOPanel() {
		logger.setLevel(Level.FINE);
		fileChooser = new JFileChooser();
		
		// input
		JPanel inputPanel = new JPanel();
		JLabel inputLabel = new JLabel("image directory:", JLabel.LEFT);
		inputPanel.add(inputLabel);
		inputFilename = new JLabel();
		inputPanel.add(inputFilename);
		inputBtn = new JButton("choose directory");
		inputBtn.addActionListener(new InputBtnListener());
		inputPanel.add(inputBtn);
		
		// output
		JPanel outputPanel = new JPanel();
		JLabel outputLabel = new JLabel("output directory:", JLabel.LEFT);
		outputPanel.add(outputLabel);
		outputFilename = new JLabel();
		outputPanel.add(outputFilename);
		outputBtn = new JButton("choose directory");
		outputBtn.addActionListener(new OutputBtnListener());
		outputPanel.add(outputBtn);
		
		overwriteBox = new JCheckBox("overwrite existing files");
		overwriteBox.setMnemonic(KeyEvent.VK_O);
		overwriteBox.setSelected(true);
		overwriteBox.setEnabled(false);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(inputPanel);
		add(outputPanel);
//		add(overwriteBox);
	}
	
	protected void setController(Controller controller) {
		this.controller = controller;
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	protected void setInputFile(String filename) {
		inputFilename.setText(filename);
	}
	
	protected void setOutputFile(String filename) {
		outputFilename.setText(filename);
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
}
