package DriftCorrection.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

//import javax.swing.JFileChooser;
import javax.swing.JFrame;
//import javax.swing.JPanel;

import DriftCorrection.gui.image.ImageViewer;
import DriftCorrection.model.ReadOnlyMovie;
import DriftCorrection.process.Controller;


@SuppressWarnings("serial")
public class MainFrame  extends JFrame{
	private static final Logger logger = Logger.getLogger(MainFrame.class.getName());
	private Synchroniser sync;
	private StatusPanel statusPanel;
	private ImageViewer imagePanel;
	private SettingPanel settingPanel;

	public MainFrame(Controller controller, FileHandler fh) {
		logger.setLevel(Level.FINE);
		logger.addHandler(fh);
		logger.info("creating MainFrame...");
		
		sync = new Synchroniser(this);
		settingPanel = new SettingPanel(controller);
		imagePanel = new ImageViewer(sync);
		statusPanel = new StatusPanel(controller, this.getWidth());
		settingPanel.setFileHandler(fh);
		imagePanel.setFileHandler(fh);
		statusPanel.setFileHandler(fh);
		
		controller.setMainFrame(this);	
		
		// top level container
		Container cp = getContentPane();		
		cp.setLayout(new BorderLayout());
		cp.add(settingPanel, BorderLayout.NORTH);
		cp.add(imagePanel, BorderLayout.CENTER);
		cp.add(statusPanel, BorderLayout.SOUTH);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Drift Correction");
		this.setSize(1200,1000);
		this.setVisible(true);
		logger.info("MainFrame is ready...");
		this.statusPanel.setStatusLabel("Ready...");
	}
	
	public void setMovie(ReadOnlyMovie movie) {
		sync.setMovie(movie);
	}
	
	//////////// setting panel
	public void updateView(int state) {
		sync.setState(state);
		settingPanel.updateView(state);
		imagePanel.setActiveState(state);
	}
	
	//////////// IO panel
	public void setImageFileName(String filename) {
		settingPanel.setInputFile(filename);
	}
	
	public void setSaveFolder(String folderName) {
		settingPanel.setOutputFile(folderName);
	}
	
	//////////// template matching
	public void toggleTemplateMatchingBtn(boolean flag) {
		settingPanel.toggleTemplateMatchingBtn(flag);
	}
	
	/////////// drift editing
	public void updateDriftSectionTable() {
		settingPanel.updateDriftSectionTable();
	}
	

	public void setDriftTableVisible(int frameNumber) {
		settingPanel.setDriftTableVisible(frameNumber);
	}
	
	////////// drift correction
	public void toggleDriftCorrectionBtn(boolean flag) {
		settingPanel.toggleDriftCorrectionBtn(flag);
	}
	
	/////////// raw Image
	public void setRawImages(List<Path> fileList) {
		imagePanel.setRawImageList(fileList);
	}
	
	public void setRawImageFrame(int frameNumber) {
		imagePanel.setRawImageFrame(frameNumber);
	}
	
	public int getRawFrameIndex() {
		return imagePanel.getRawFrameIndex();
	}
	
	public int[] getRawROI() {
		return imagePanel.getRawROI();
	}
	
	////////// right image panel
	public void setTMImage(int frameNumber, int first, int last, Path path, int[] ROI) {
		imagePanel.setTMImage(frameNumber, first, last, path, ROI);
	}
	
	public void setTMImage(int frameNumber) {
		sync.setTMImage(frameNumber);
	}
//	
//	protected void removeTMImage(int frameNumber) {
//		imagePanel.removeTMImage();
//	}
	
	public void setXDriftData(int[] xList, float[] xFittedList) {
		imagePanel.setXDriftData(xList, xFittedList);
	}
	public void setYDriftData(int[] yList, float[] yFittedList) {
		imagePanel.setYDriftData(yList, yFittedList);
	}
	
	public void setCorrectedImages(List<String> list) {
		imagePanel.setCorrectedImages(list);
	}
	
	////////// status panel
	public void updateStatus(String message) {
		statusPanel.setStatusLabel(message);
	}
	
	public void setProgress(int num) {
		statusPanel.setProgress(num);
	}


//	
//	public void setProgress(int progress) {
//		statusPanel.setProgress(progress);
//	}
//	

	
}
