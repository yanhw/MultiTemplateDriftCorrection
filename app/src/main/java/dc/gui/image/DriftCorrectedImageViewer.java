package dc.gui.image;

import javax.swing.JPanel;
import javax.swing.JSlider;

import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import java.awt.Color;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.border.EtchedBorder;

@SuppressWarnings("serial")
public class DriftCorrectedImageViewer extends JPanel implements ChangeListener  {
	private static final Logger logger = Logger.getLogger(DriftCorrectedImageViewer.class.getName());
	
	protected static int NUM_FRAME;
	private int frameNumber = 0;
	private List<String> imgList;
	private Slider slider;
	private ZoomSlider zoomSlider;
	private ImagePanel imagePanel;
	
	/**
	 * Create the panel.
	 */
	public DriftCorrectedImageViewer() {
		setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.SOUTH);
		
		slider = new Slider();
		splitPane.setRightComponent(slider);
		
		zoomSlider = new ZoomSlider();
		splitPane.setLeftComponent(zoomSlider);
		
		imagePanel = new ImagePanel();
		add(imagePanel, BorderLayout.CENTER);
		
		setHandlers();
	}
	
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		imagePanel.setFileHandler(fh);
	}
	
	private void setHandlers() {
		slider.addChangeListener(this);

		zoomSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if (!source.getValueIsAdjusting()) {
					int zoomStep = (int)source.getValue();
					double zoomLevel = ZoomSlider.STEPS[zoomStep];
					logger.info("changing zoom level to: " + zoomLevel);
					updateZoomLevel(zoomLevel);
				}
			}

		});
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
		if (!source.getValueIsAdjusting()) {
			frameNumber = (int)source.getValue();
			logger.info("updating to image: " + frameNumber);
			updatePicture(frameNumber);
		}
	}
	
	/** Update the label to display the image for the current frame. */
	protected void updatePicture(int frameNumber) {
		if (imgList == null) {
			return;
		}
		if (!imagePanel.updateImage(imgList.get(frameNumber)));
		// TODO give feedback for bad image
	}
	
	protected void updateZoomLevel(double zoomLevel) {
    	imagePanel.setZoomLevel(zoomLevel);
    }
	
	public void setImageList(List<String> list) {
		this.imgList = list;
		NUM_FRAME = list.size();
		slider.setMaximum(NUM_FRAME);
		updatePicture(0);
	}
}
