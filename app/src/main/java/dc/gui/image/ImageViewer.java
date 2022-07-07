package dc.gui.image;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dc.model.DriftModel;
import dc.model.MovieStateModel;
import dc.model.RawFileModel;
import dc.model.TemplateMatchingSegmentModel;

import java.awt.BorderLayout;
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
		
		xDriftPlot = new DriftViewer("x");
		driftPlotPane.addTab("x drift", null, xDriftPlot, null);
		
		yDriftPlot = new DriftViewer("y");
		driftPlotPane.addTab("y drift", null, yDriftPlot, null);
		
		correctedImage = new DriftCorrectedImageViewer();
		tabbedPane.addTab("Drift Corrected Image", null, correctedImage, null);
		tabbedPane.setEnabledAt(1, false);
		tabbedPane.setEnabledAt(2, false);
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		templateImage.setFileHandler(fh);
		xDriftPlot.setFileHandler(fh);
		yDriftPlot.setFileHandler(fh);
		correctedImage.setFileHandler(fh);
	}
	
	public void setTemplateTableModel(TemplateMatchingSegmentModel model) {
		templateImage.setTemplateTableModel(model);
	}
	
	public void setDriftDataModel(DriftModel model) {
		xDriftPlot.setDriftModelListener(model);
		yDriftPlot.setDriftModelListener(model);
	}
	
	public void setRawFileModel(RawFileModel fileList) {
		templateImage.setRawFileModel(fileList);
	}
	
	public DefaultListSelectionModel getXSelectionModel() {
		return xDriftPlot.getSelectionModel();
	}
	
	public DefaultListSelectionModel getYSelectionModel() {
		return yDriftPlot.getSelectionModel();
	}
	
	public void setCorrectedImages(List<String> list) {
		correctedImage.setImageList(list);
	}
	
	public void addMovieStateModelListener(MovieStateModel model) {
		model.addChangeListener(new StateChangeListener());
	}
	
	private class StateChangeListener implements ChangeListener{

		@Override
		public void stateChanged(ChangeEvent e) {
			MovieStateModel source = (MovieStateModel) e.getSource();
			int state = (int)source.getValue();
			logger.info("setting tabbedPane state: " + state);
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
	
}
