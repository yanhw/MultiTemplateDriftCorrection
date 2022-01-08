package dc.gui_old.image;

import java.nio.file.Path;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import dc.gui_old.Synchroniser;

import java.util.logging.FileHandler;
import java.util.logging.Logger;



@SuppressWarnings("serial")
public class ImageViewer extends JPanel {
	private static final Logger logger = Logger.getLogger(ImageViewer.class.getName());
	
    RawImageViewer rawImage;
    JTabbedPane rightPanel;
    TemplateImageViewer templateImage;
    DriftViewerJFreeChart xDriftPlot;
    DriftViewerJFreeChart yDriftPlot;
    DriftCorrectedViewer correctedImage;
	
	public ImageViewer(Synchroniser sync) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
//        rawImage = new RawImageViewer(sync);
        
        templateImage = new TemplateImageViewer();
        
        xDriftPlot = new DriftViewerJFreeChart();
        xDriftPlot.setSynchroniser(sync);
        yDriftPlot = new DriftViewerJFreeChart();
        yDriftPlot.setSynchroniser(sync);
        JTabbedPane driftPlot = new JTabbedPane();
        driftPlot.addTab("x drift", xDriftPlot);
        driftPlot.addTab("y drift", yDriftPlot);
        
        correctedImage = new DriftCorrectedViewer(sync);
        
        rightPanel = new JTabbedPane();
        rightPanel.addTab("Selected Template", templateImage);
        rightPanel.addTab("Detected Drift", driftPlot);
        rightPanel.addTab("Drift Corrected Movie", correctedImage);
		rightPanel.setEnabledAt(1, false);
		rightPanel.setEnabledAt(2, false);

//        logger.fine("showing template page");
        add(rawImage);
        add(rightPanel);
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		rawImage.setFileHandler(fh);
		templateImage.setFileHandler(fh);
		xDriftPlot.setFileHandler(fh);
		yDriftPlot.setFileHandler(fh);
		correctedImage.setFileHandler(fh);
	}
	

	public void setRawImageList(List<Path> fileList) {
		rawImage.setImageList(fileList);
	}
	
	public void setRawImageFrame(int frameNumber) {
		rawImage.updatePictureWithSlider(frameNumber);
	}
	
	public int getRawFrameIndex() {
		return rawImage.getFrameIndex();
	}
	
	public int[] getRawROI() {
		return rawImage.getROI();
	}
	
	public void setTMImage(int frameNumber, int first, int last, Path path, int[] ROI) {
		templateImage.setImage(frameNumber, first, last, path, ROI);
	}
	
	public void setXDriftData(int[] xList, float[] xFittedList) {
		xDriftPlot.setData(xList, xFittedList);
	}
	
	public void setYDriftData(int[] yList, float[] yFittedList) {
		yDriftPlot.setData(yList, yFittedList);
	}
	
	public void setCorrectedImages(List<String> list) {
		correctedImage.setImageList(list);
	}
	
	public void setActiveState(int state) {
		switch (state) {
			case 0:
			case 1:
				rightPanel.setEnabledAt(0, true);
				rightPanel.setEnabledAt(1, false);
				rightPanel.setEnabledAt(2, false);
				rightPanel.setSelectedIndex(0);
				break;
			case 2:
				rightPanel.setEnabledAt(0, true);
				rightPanel.setEnabledAt(1, true);
				rightPanel.setEnabledAt(2, false);
				rightPanel.setSelectedIndex(1);
				break;
			case 3:
				rightPanel.setEnabledAt(0, true);
				rightPanel.setEnabledAt(1, true);
				rightPanel.setEnabledAt(2, true);
				rightPanel.setSelectedIndex(2);
		}
	}

	
}
