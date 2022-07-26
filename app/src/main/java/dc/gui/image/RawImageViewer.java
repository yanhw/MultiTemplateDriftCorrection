package dc.gui.image;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

import java.awt.BorderLayout;
import java.awt.Cursor;

import javax.swing.JSplitPane;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dc.model.FileListModel;

@SuppressWarnings("serial")
public class RawImageViewer extends JPanel implements ChangeListener  {
	private static final Logger logger = Logger.getLogger(RawImageViewer.class.getName());
	
	protected static int NUM_FRAME;
	private int frameNumber = 0;
    private FileListModel imgList;
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
		imagePanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(imagePanel);
		add(scrollPane, BorderLayout.CENTER);
		
		setHandlers(); 
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
		}
	}
	
	public BoundedRangeModel getRawFrameModel() {
		return movieSlider.getModel();
	}
	
    public ROIModel getROI() {
    	return imagePanel.getROI();
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
	
	public void setRawFileModel(FileListModel fileList) {
		this.imgList = fileList;
		imgList.addListDataListener(new FileModelListener());
	}
	
	private class FileModelListener implements ListDataListener {

		@Override
		public void intervalAdded(ListDataEvent e) {
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {	
		}

		@Override
		public void contentsChanged(ListDataEvent e) {
			NUM_FRAME = imgList.getSize();
			movieSlider.setMaximum(NUM_FRAME);
			updatePictureWithSlider(0);
		}
		
	}
	
	public void setPlotSelectionModel(DefaultListSelectionModel model) {
		model.addListSelectionListener(new PlotListener());
	}
	
	private class PlotListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				updatePictureWithSlider(e.getFirstIndex());
			}			
		}
		
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
    	if (imgList == null || imgList.getSize() == 0) {
    		return;
    	}
    	if (!imagePanel.updateImage(imgList.getElementAt(frameNumber).toString()));
    	// TODO give feedback for bad image
    }
    
    protected void updateZoomLevel(double zoomLevel) {
    	imagePanel.setZoomLevel(zoomLevel);
    	imagePanel.revalidate();
    }
    
}
