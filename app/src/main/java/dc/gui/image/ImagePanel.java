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
	private BufferedImage rawImage;
	private Image image;
	private int imgHeight, imgWidth;
	private int top, left, height, width;
	private Point first;
	public static final double MAXZOOM = 10.0;
	public static final double MINZOOM = 0.1;
	private double zoomLevel = 1.0;
	private ROIModel ROI;
	
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
	
	public ROIModel getROI() {
		return ROI;
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (image == null) {
			return super.getPreferredSize();
		}
		return new Dimension(imgWidth, imgHeight);
	}
	
	protected void setZoomLevel(double newLevel) {
		assert (newLevel >= MINZOOM);
		assert (newLevel <= MAXZOOM);
		if (newLevel == zoomLevel) {
			return;
		}
		zoomLevel = newLevel;
		if (rawImage == null) {
			return;
		}
		scaleImage();
		updateImage();
	}
	
	private void scaleImage() {
		imgHeight = rawImage.getHeight();
		imgWidth = rawImage.getWidth();
		imgHeight = (int) (imgHeight*zoomLevel);
		imgWidth = (int) (imgWidth*zoomLevel);
		image = rawImage.getScaledInstance(imgWidth, imgHeight, Image.SCALE_FAST);
//		System.out.println(left + " " + top + " " + width + " " + height + " " + zoomLevel);
		top = (int) (ROI.getROI()[ROIModel.TOP]*zoomLevel);
		left = (int) (ROI.getROI()[ROIModel.LEFT]*zoomLevel);
		height = (int) ((ROI.getROI()[ROIModel.BOTTOM]-ROI.getROI()[ROIModel.TOP])*zoomLevel);
		width = (int) ((ROI.getROI()[ROIModel.RIGHT]-ROI.getROI()[ROIModel.LEFT])*zoomLevel);
	}
	
	protected Point getImageLocation() {
		Point p = null;
		if (image != null) {
			int x = (getWidth() - imgWidth)/2;
			int y = (getHeight() - imgHeight)/2;
			p = new Point(x, y);
//			System.out.println("getImageLocation"+p.x + " " + p.y);
		}
		return p;
	}
	
	public Point getPixelLocation(Point p) {
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
	
	public void setPoint(Point first) {
		this.first = first;
//		System.out.println("first: " + first);
	}
	
	public void setSecPoint(Point second) {
		if (image == null) {
			return;
		}
//		System.out.println("second: " + second);
		if (first.x==second.x || first.y==second.y) {
			ROI.removeROI();
		} else {
			top = Math.min(first.y, second.y);
			left = Math.min(first.x, second.x);
			// restrict points, they must be inside the image
			top = Math.max(top, 0);
			left = Math.max(left, 0);

			height = (Math.max(first.y, second.y)-top);
			width = (Math.max(first.x, second.x)-left);			
			height = Math.min(height, imgHeight-top);
			width = Math.min(width, imgWidth-left);
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
		updateImage();
	}
	
	protected void setROI(int top, int left, int height, int width) {
		int[] array = {top, top+height, left, left+width};
		ROI.setROI(array);
		scaleImage();
		this.updateImage();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			Point p = getImageLocation();
			g.drawImage(image,  p.x,  p.y,  this);
			if (ROI.getFlag()) {
				g.setColor(Color.RED);
				Point p2 = computePanelLocation(left, top);
//				System.out.println(p2.x + " " + p2.y);
//				System.out.println(left + " " + top + " " + width + " " + height);
				g.drawRect(p2.x, p2.y, width, height);
			}
		} else {
			g.setColor(getBackground());
			g.drawRect(0,0,imgHeight, imgWidth);
		}
	}
	
	public boolean updateImage(String filename) {
		if (filename == null) {
			rawImage = null;
		} else {
			try {
	    		rawImage = ImageIO.read(new File(filename));
			} catch(IOException e) {
				logger.warning("failed to open image:" + filename);
			}		
			scaleImage();
		}
		repaint();
		if (image != null) {
			
			return true;
		}
		return false;
	}
	
	private void updateImage() {
		//TODO
		//???? i think this assertion should not be here, or maybe this method is not necessary
//		assert(hasROI);
		repaint();
	}
	
	protected int getImageHeight() {
		return rawImage.getHeight();
	}
	
	protected int getImageWidth() {
		return rawImage.getWidth();
	}
}