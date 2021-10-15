package DriftCorrection.gui.image;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import DriftCorrection.gui.Synchroniser;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

// for testing
import DriftCorrection.utils.FileSystem;

@SuppressWarnings({ "serial", "unused" })
public class RawImageViewer extends JPanel implements ChangeListener {
	private static final Logger logger = Logger.getLogger(RawImageViewer.class.getName());
	
	protected static int NUM_FRAME;
	private int frameNumber = 0;
    private List<Path> imgList;
    private Slider movieSlider;
    private ZoomSlider zoomSlider;
    //This label uses ImageIcon to show the frames.
    private ImagePanel imagePanel;
    private Synchroniser sync;
//    BufferedImage image;
	
	public RawImageViewer(Synchroniser sync) {
		this.sync = sync;
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
        
        //Create the label that displays the animation.
        imagePanel = new ImagePanel(); 
        imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point panelPoint = e.getPoint();
                Point piexelLocation = imagePanel.getPixelLocation(panelPoint);
                imagePanel.setPoint(piexelLocation);
//                System.out.println("You clicked at " + panelPoint + " which is relative to the image " + imgContext);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                Point panelPoint = e.getPoint();
                Point piexelLocation = imagePanel.getPixelLocation(panelPoint);
                imagePanel.setSecPoint(piexelLocation);
//                System.out.println("You clicked at " + panelPoint + " which is relative to the image " + imgContext);
            }
        });
        
        imagePanel.addMouseMotionListener(new MouseAdapter() {
        	@Override
        	public void mouseDragged(MouseEvent e) {
        		Point panelPoint = e.getPoint();
        		Point piexelLocation = imagePanel.getPixelLocation(panelPoint);
          
        		imagePanel.setSecPoint(piexelLocation);
        	}
        });
        
//        updatePicture(0); //display first frame
        
        //Put everything together.
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        add(scrollPane);
        
        movieSlider = new Slider();
        movieSlider.addChangeListener(this);
        add(movieSlider);
        
        zoomSlider = new ZoomSlider();
        zoomSlider.addChangeListener(new ZoomSliderListener());
        add(zoomSlider);
        
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));  
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
	
	public void setImageList(List<Path> imgList) {
		this.imgList = imgList;
		NUM_FRAME = imgList.size();
		movieSlider.setMaximum(NUM_FRAME);
	}
	
	protected void updatePictureWithSlider(int frameNumber) {
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
    
    protected int[] getROI() {
    	return imagePanel.getROI();
    }
    
    public int getFrameIndex() {
    	return frameNumber;
    }
    
    
    private class ZoomSliderListener implements ChangeListener {

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
    	
    }

//    /**
//     * Create the GUI and show it.  For thread safety,
//     * this method should be invoked from the
//     * event-dispatching thread.
//     */
//    private static void createAndShowGUI() {
//        //Create and set up the window.
//        JFrame frame = new JFrame("ImageTest");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        Path movieDir = Paths.get("Z:\\hongwei\\concaved\\190121\\03\\drift_corrected_average_5");
//		List<Path> imgList = FileSystem.getFiles(movieDir , "png");
//        RawImageViewer imageViewer = new RawImageViewer();
//        imageViewer.setImageList(imgList);
//        //Add content to the window.
//        frame.add(imageViewer, BorderLayout.CENTER);
//
//        //Display the window.
//        frame.pack();
//        frame.setVisible(true);
//        imageViewer.updatePicture(0); 
//    }
//    
//    
//    public static void main(String[] args) {
//        /* Turn off metal's use of bold fonts */
//        UIManager.put("swing.boldMetal", Boolean.FALSE);
//        
//        
//        //Schedule a job for the event-dispatching thread:
//        //creating and showing this application's GUI.
//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                createAndShowGUI();
//            }
//        });
//    }
}
