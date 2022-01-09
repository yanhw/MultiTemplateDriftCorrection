package dc.gui.image;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import dc.gui.Synchroniser;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class ImageViewer extends JPanel {
	private static final Logger logger = Logger.getLogger(ImageViewer.class.getName());
	
	private JTabbedPane tabbedPane;
	private TemplateImageViewer templateImage;
	private DriftCorrectedImageViewer correctedImage;
	private DriftViewer xDriftPlot;
	private DriftViewer yDriftPlot;

	private Synchroniser sync;

	/**
	 * Create the panel.
	 */
	public ImageViewer() {
		setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane);
		
		templateImage = new TemplateImageViewer();
		tabbedPane.addTab("Templates", null, templateImage, null);
		
		JTabbedPane driftPlotPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Drift Plot", null, driftPlotPane, null);
		
		xDriftPlot = new DriftViewer();
		driftPlotPane.addTab("x drift", null, xDriftPlot, null);
		
		yDriftPlot = new DriftViewer();
		driftPlotPane.addTab("y drift", null, yDriftPlot, null);
		
		correctedImage = new DriftCorrectedImageViewer();
		tabbedPane.addTab("Drift Corrected Image", null, correctedImage, null);

	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		templateImage.setFileHandler(fh);
		xDriftPlot.setFileHandler(fh);
		yDriftPlot.setFileHandler(fh);
		correctedImage.setFileHandler(fh);
	}
	
	public void setSynchroniser(Synchroniser sync) {
		this.sync = sync;
		correctedImage.setSynchroniser(sync);
	}
	
	
	public void updateTagState() {
		int state = sync.getState();
		if (state < 0) {
			logger.severe("unknown state: " + state);
		}
		logger.info("setting tab enabled upto: " + state); 
		switch (state) {
			case 0:
				tabbedPane.setEnabledAt(1, false);
				tabbedPane.setEnabledAt(2, false);
				break;
			case 1:
				tabbedPane.setEnabledAt(1, true);
				tabbedPane.setEnabledAt(2, false);
				break;
			default:
				tabbedPane.setEnabledAt(1, true);
				tabbedPane.setEnabledAt(2, true);
		}
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
				tabbedPane.setEnabledAt(0, true);
				tabbedPane.setEnabledAt(1, false);
				tabbedPane.setEnabledAt(2, false);
				tabbedPane.setSelectedIndex(0);
				break;
			case 2:
				tabbedPane.setEnabledAt(0, true);
				tabbedPane.setEnabledAt(1, true);
				tabbedPane.setEnabledAt(2, false);
				tabbedPane.setSelectedIndex(1);
				break;
			case 3:
				tabbedPane.setEnabledAt(0, true);
				tabbedPane.setEnabledAt(1, true);
				tabbedPane.setEnabledAt(2, true);
				tabbedPane.setSelectedIndex(2);
		}
	}
}
