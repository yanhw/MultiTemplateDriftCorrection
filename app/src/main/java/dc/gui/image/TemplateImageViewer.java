package dc.gui.image;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import dc.model.FileListModel;
import dc.model.TemplateMatchingSegmentModel;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class TemplateImageViewer extends JPanel {
	private static final Logger logger = Logger.getLogger(TemplateImageViewer.class.getName());
	
	private ImagePanel imagePanel;
	private JLabel textLabel;
	
	private TemplateMatchingSegmentModel sections;
	private FileListModel fileList;
	private Slider slider;
	
	private int[] ROI = {0,0,0,0};		// keep current display status to avoid unnecessary refresh
	private boolean hasROI = false;
	private int start = 0;
	private int curr = 0;
	private int end = 0;
	/**
	 * Create the panel.
	 */
	public TemplateImageViewer() {
		setLayout(new BorderLayout(0, 0));
		
		textLabel = new JLabel("Templates will be displayed here.");
		add(textLabel, BorderLayout.NORTH);
		
		imagePanel = new ImagePanel();
		add(imagePanel);
		
		slider = new Slider();
		add(slider, BorderLayout.SOUTH);
		slider.addChangeListener(new SliderListener());
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		imagePanel.setFileHandler(fh);
	}
	
	public void setTemplateTableModel(TemplateMatchingSegmentModel model) {
		sections = model;
		sections.addTableModelListener(new SegmentModelListener());
		
	}
	
	public void setRawFileModel(FileListModel fileList) {
		this.fileList = fileList;
	}
	
	private class SegmentModelListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent e) {
			if (sections.getRowCount() != slider.getFrameNumber()) {
				slider.setMaximum(sections.getRowCount());
			}
			checkUpdate();
		}	
	}
	
	@SuppressWarnings("unused")
	private class FileModelListener implements ListDataListener {

		@Override
		public void intervalAdded(ListDataEvent e) {
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {	
		}

		@Override
		public void contentsChanged(ListDataEvent e) {
			
		}
		
	}
	
	private class SliderListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider)e.getSource();
			if (!source.getValueIsAdjusting()) {
				int sectionNumber = (int)source.getValue();
				logger.info("updating to template image: " + sectionNumber);
				checkUpdate();
			}
		}
		
	}
	
	private void checkUpdate() {
		int sliderValue = slider.getValue();
		if (sections.getRowCount() <= sliderValue) {
			logger.info("no image to display");
			return;
		}
		int start = (int) sections.getValueAt(sliderValue, TemplateMatchingSegmentModel.START_IDX);
		int end = (int) sections.getValueAt(sliderValue, TemplateMatchingSegmentModel.END_IDX);
		int keyframe = (int) sections.getValueAt(sliderValue, TemplateMatchingSegmentModel.KEY_IDX);
		boolean hasROI = (boolean) sections.getValueAt(sliderValue, TemplateMatchingSegmentModel.HAS_TEMPLATE_IDX);
		boolean sameLabel = (this.start==start && this.end==end && this.curr==keyframe && this.hasROI == hasROI);
		int[] ROI = {(int) sections.getValueAt(sliderValue, TemplateMatchingSegmentModel.TOP),
				(int) sections.getValueAt(sliderValue, TemplateMatchingSegmentModel.BOTTOM),
				(int) sections.getValueAt(sliderValue, TemplateMatchingSegmentModel.LEFT),
				(int) sections.getValueAt(sliderValue, TemplateMatchingSegmentModel.RIGHT)};
		boolean sameImage = true;
		for (int i = 0; i < 4; i++) {
			if (this.ROI[i] != ROI[i]) {
				sameImage = false;
			}
		}
		if (!hasROI) {
			ROI = null;
		}
		if (curr != keyframe || this.hasROI != hasROI) {
			sameImage = false;
		}
		if (!sameLabel) {
			updateLabel(start, end, keyframe, hasROI);
		}
		if (!sameImage) {
			setImage(keyframe, ROI);
		}
	}
	
	private void updateLabel(int start, int end, int key, boolean hasROI) {
		if (hasROI) {
			textLabel.setText("template at frame number " + key + " for movie at frame " 
			    	+ start + " to " + end);
		} else {
			textLabel.setText("no template for movie at frame " 
    		    	+ start + " to " + end);
		}
		this.start = start;
		this.end = end;
		this.curr = key;
		this.hasROI = hasROI;
	}
	
	public void setImage(int frameNumber, int[] ROI) {
    	if (ROI == null) {
    		imagePanel.updateImage(null);
    	} else {
    		Path path = fileList.getElementAt(frameNumber);
    		imagePanel.updateImage(path.toString());
	    	imagePanel.setROI(ROI[0], ROI[2], ROI[1]-ROI[0], ROI[3]-ROI[2]);
	    	
	    	logger.info("displaying " + frameNumber + " " + path.toString());
    	}
    	
    }
	
  
}
