package dc.gui.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

@SuppressWarnings("serial")
class ImagePanel extends JPanel {
	private static final Logger logger = Logger.getLogger(ImagePanel.class.getName());
	private BufferedImage rawImage;				// raw image to display, this can be null
	private Image image;						// scaled version of the image
	private Point first;						// ref point for ROI selection
	private double zoomLevel = 1.0;				// current scale ratio
	private ROIModel ROI;						// ROI model, which notifies listeners
	
	public ImagePanel() {
		setAlignmentX(Component.CENTER_ALIGNMENT);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(10,10,10,10)));
        ROI = new ROIModel();
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	protected ROIModel getROI() {
		assert ROI != null;
		return ROI;
	}
	
	// necessary to override this method for scroll bars to function properly
	@Override
	public Dimension getPreferredSize() {
		if (image == null) {
			return super.getPreferredSize();
		}
		return new Dimension((int)(getImageWidth()*zoomLevel), (int)(getImageHeight()*zoomLevel));
	}
	
	////////////////////////////////////////////////////////////
	// set image to display
	////////////////////////////////////////////////////////////
	
	protected boolean updateImage(String filename) {
		if (filename == null) {
			rawImage = null;
			image = null;
			ROI.removeROI();
			logger.info("setting null image");
		} else {
			try {
	    		rawImage = ImageIO.read(new File(filename));
			} catch(IOException e) {
				logger.warning("failed to open image:" + filename);
			}			
		}
		scaleImage();
		repaint();
		return (image != null);
	}
	
	protected int getImageHeight() {
		return rawImage.getHeight();
	}
	
	protected int getImageWidth() {
		return rawImage.getWidth();
	}
	////////////////////////////////////////////////////////////////////
	// custom drawing
	////////////////////////////////////////////////////////////////////
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			Point p = getImageLocation();
			g.drawImage(image,  p.x,  p.y,  this);
			if (ROI.getFlag()) {
				g.setColor(Color.RED);
				Point p2 = computePanelLocation(getROILeft(), getROITop());
				//System.out.println(p2.x + " " + p2.y);
				g.drawRect(p2.x, p2.y, getROIWidth(), getROIHeight());
			}
		} else {
			g.setColor(getBackground());
			g.fillRect(0,0,getHeight(), getWidth());
		}
	}
	///////////////////////////////////////////////////////////////////////
	// zoom image
	///////////////////////////////////////////////////////////////////////
	
	protected void setZoomLevel(double newLevel) {
		if ((newLevel < ZoomSlider.STEPS[0]) || (newLevel > ZoomSlider.STEPS[ZoomSlider.STEPS.length-1])) {
			logger.warning("bad zoom level: " + newLevel);
			return;
		}
		if (newLevel == zoomLevel) {
			return;
		}
		zoomLevel = newLevel;
		if (rawImage == null) {
			return;
		} else {
			scaleImage();
		}
		repaint();
	}
	
	private void scaleImage() {
		if (rawImage == null) {
			return;
		}
		int imgHeight = (int) (rawImage.getHeight()*zoomLevel);
		int imgWidth = (int) (rawImage.getWidth()*zoomLevel);
		image = rawImage.getScaledInstance(imgWidth, imgHeight, Image.SCALE_FAST);
	}
	
	// the following getters returns scaled version of ROI
	private int getROITop() {
		return (int) (ROI.getROI()[ROIModel.TOP]*zoomLevel);
	}
	
	private int getROILeft() {
		return (int) (ROI.getROI()[ROIModel.LEFT]*zoomLevel);
	}
	
	private int getROIHeight() {
		return (int) ((ROI.getROI()[ROIModel.BOTTOM]-ROI.getROI()[ROIModel.TOP])*zoomLevel);
	}
	
	private int getROIWidth() {
		return (int) ((ROI.getROI()[ROIModel.RIGHT]-ROI.getROI()[ROIModel.LEFT])*zoomLevel);
	}
	
	
	/////////////////////////////////////////////////////////////////////
	// ROI selection
	/////////////////////////////////////////////////////////////////////
	// helper method for getPixelLocation
	private Point getImageLocation() {
		Point p = null;
		if (image != null) {
			int x = (getWidth() - rawImage.getWidth())/2;
			int y = (getHeight() - rawImage.getHeight())/2;
			p = new Point(x, y);
//			System.out.println("getImageLocation"+p.x + " " + p.y);
		}
		return p;
	}
	
	protected Point getPixelLocation(Point p) {
		Point imageLocation = getImageLocation();
		if (imageLocation != null) {
			Point pixelLocation = new Point(p.x-imageLocation.x, p.y-imageLocation.y);
			return pixelLocation;
		}
		return null;
	}
	
	private Point computePanelLocation(int x, int y) {
		Point imageLocation = getImageLocation();
		Point panelLocation = new Point(x+imageLocation.x, y+imageLocation.y);
		return panelLocation;
	}
	
	protected void setPoint(Point first) {
		this.first = first;
//		System.out.println("first: " + first);
	}
	
	protected void setSecPoint(Point second) {
		if (image == null) {
			return;
		}
//		System.out.println("second: " + second);
		if (first.x==second.x || first.y==second.y) {
			ROI.removeROI();
		} else {
			int top = Math.min(first.y, second.y);
			int left = Math.min(first.x, second.x);
			// restrict points, they must be inside the image
			top = Math.max(top, 0);
			left = Math.max(left, 0);

			int height = (Math.max(first.y, second.y)-top);
			int width = (Math.max(first.x, second.x)-left);			
			height = Math.min(height, rawImage.getHeight()-top);
			width = Math.min(width, rawImage.getWidth()-left);
			int rawTop = (int) (top/zoomLevel);
			int rawHeight = (int) ((height)/zoomLevel); 
			int rawLeft = (int) (left/zoomLevel);
			int rawWidth = (int) ((width)/zoomLevel);
			
			if (rawHeight<= 0 || rawWidth <= 0) {
				ROI.removeROI();
			} else {
				int[] array = {rawTop, rawTop+rawHeight, rawLeft, rawLeft+rawWidth};
				ROI.setROI(array);
			}
			
		}
		repaint();
	}
	
	protected void setROI(int top, int left, int height, int width) {
		int[] array = {top, top+height, left, left+width};
		ROI.setROI(array);
		scaleImage();
		repaint();
	}
}