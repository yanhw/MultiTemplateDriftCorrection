package dc.gui.image;

import javax.swing.JPanel;
import javax.swing.JSlider;

import dc.gui.Synchroniser;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class RawImageViewer extends JPanel implements ChangeListener  {
	private static final Logger logger = Logger.getLogger(RawImageViewer.class.getName());
	private Synchroniser sync;
	
	protected static int NUM_FRAME;
	private int frameNumber = 0;
    private List<Path> imgList;
    private Slider movieSlider;
    private ZoomSlider zoomSlider;
	private ImagePanel imagePanel;
	
	
	/**
	 * Create the panel.
	 */
	public RawImageViewer() {
		setBorder(new TitledBorder(null, "Input Image Sequence", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.SOUTH);
		
		movieSlider = new Slider();
		splitPane.setRightComponent(movieSlider);
		
		zoomSlider = new ZoomSlider();
		splitPane.setLeftComponent(zoomSlider);
		
		imagePanel = new ImagePanel();
		add(imagePanel, BorderLayout.CENTER);
		
		setHandlers(); 
	}
	
	public void setSynchroniser(Synchroniser sync) {
		this.sync = sync;
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		imagePanel.setFileHandler(fh);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
		if (!source.getValueIsAdjusting()) {
			frameNumber = (int)source.getValue();
			logger.info("updating to image: " + frameNumber);
			updatePicture(frameNumber);
			sync.rawImageChanged(frameNumber);
		}
	}
	
	private void setHandlers() {
		imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point panelPoint = e.getPoint();
                Point piexelLocation = imagePanel.getPixelLocation(panelPoint);
                if (piexelLocation != null) {
                	imagePanel.setPoint(piexelLocation);
//                	System.out.println("You clicked at " + panelPoint + " which is relative to the image " + imgContext);
                }
                
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                Point panelPoint = e.getPoint();
                Point piexelLocation = imagePanel.getPixelLocation(panelPoint);
                if (piexelLocation != null) {
                	imagePanel.setSecPoint(piexelLocation);
//                System.out.println("You clicked at " + panelPoint + " which is relative to the image " + imgContext);
                }
            }
        });
        
        imagePanel.addMouseMotionListener(new MouseAdapter() {
        	@Override
        	public void mouseDragged(MouseEvent e) {
        		Point panelPoint = e.getPoint();
        		Point piexelLocation = imagePanel.getPixelLocation(panelPoint);
        		if (piexelLocation != null) {
        			imagePanel.setSecPoint(piexelLocation);
        		}
        	}
        });
        
        movieSlider.addChangeListener(this);
        
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
	
	public void setImageList(List<Path> imgList) {
		this.imgList = imgList;
		NUM_FRAME = imgList.size();
		movieSlider.setMaximum(NUM_FRAME);
		updatePictureWithSlider(0);
	}
	
	public void updatePictureWithSlider(int frameNumber) {
		if (frameNumber < 0 || frameNumber >= NUM_FRAME) {
			return;
		}
		
		updatePicture(frameNumber);
		movieSlider.setFrameNumber(frameNumber);
	}
	
	/** Update the label to display the image for the current frame. */
    protected void updatePicture(int frameNumber) {
    	if (!imagePanel.updateImage(imgList.get(frameNumber).toString()));
    	// TODO give feedback for bad image
    }
    
    protected void updateZoomLevel(double zoomLevel) {
    	imagePanel.setZoomLevel(zoomLevel);
    }
    
    public int[] getROI() {
    	return imagePanel.getROI();
    }
    
    public int getFrameIndex() {
    	return frameNumber;
    }
}
