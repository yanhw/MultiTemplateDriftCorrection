package dc.gui.image;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import dc.model.TemplateMatchingSegmentModel;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class TemplateImageViewer extends JPanel {
	private static final Logger logger = Logger.getLogger(TemplateImageViewer.class.getName());
	
	private ImagePanel imagePanel;
	private JLabel textLabel;
	
	private TemplateMatchingSegmentModel sections;
	private List<Path> fileList;
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
		sections.addTableModelListener(new ModelListener());
		
	}
	
	public void setFileList(List<Path> fileList) {
		this.fileList = fileList;
	}
	
	private class ModelListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent e) {
			if (sections.getRowCount() != slider.getFrameNumber()) {
				slider.setMaximum(sections.getRowCount());
			}
			checkUpdate();
		}
		
	}
	
	private class SliderListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider)e.getSource();
			if (!source.getValueIsAdjusting()) {
				int sectionNumber = (int)source.getValue();
				logger.info("updating to template image: " + sectionNumber);
				updateDisplay(sectionNumber);
			}
		}
		
	}
	
	private void checkUpdate() {
		int sliderValue = slider.getValue();
		
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
	
	private void updateDisplay(int sectionNumber) {
		if (sections.getRowCount()==0) {
			return;
		}
		int start = (int) sections.getValueAt(sectionNumber, TemplateMatchingSegmentModel.START_IDX);
		int end = (int) sections.getValueAt(sectionNumber, TemplateMatchingSegmentModel.END_IDX);
		int keyframe = (int) sections.getValueAt(sectionNumber, TemplateMatchingSegmentModel.KEY_IDX);
		boolean hasROI = (boolean) sections.getValueAt(sectionNumber, TemplateMatchingSegmentModel.HAS_TEMPLATE_IDX);
		int[] ROI = {(int) sections.getValueAt(sectionNumber, TemplateMatchingSegmentModel.TOP),
				(int) sections.getValueAt(sectionNumber, TemplateMatchingSegmentModel.BOTTOM),
				(int) sections.getValueAt(sectionNumber, TemplateMatchingSegmentModel.LEFT),
				(int) sections.getValueAt(sectionNumber, TemplateMatchingSegmentModel.RIGHT)};
		updateLabel(start, end, keyframe, hasROI);
		setImage(keyframe, ROI);
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
    		Path path = fileList.get(frameNumber);
    		imagePanel.updateImage(path.toString());
	    	imagePanel.setROI(ROI[0], ROI[2], ROI[1]-ROI[0], ROI[3]-ROI[2]);
	    	
	    	logger.info("displaying " + frameNumber + " " + path.toString() + " "+ ROI);
    	}
    	
    }
	
  
}
