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
	private boolean hasROI = false;
	private int rawTop, rawLeft, rawHeight, rawWidth;
	private int top, left, height, width;
	private Point first;
	public static final double MAXZOOM = 10.0;
	public static final double MINZOOM = 0.1;
	private double zoomLevel = 1.0;
	
	public ImagePanel() {
		setAlignmentX(Component.CENTER_ALIGNMENT);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(10,10,10,10)));
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
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
		scaleImage();
		updateImage();
	}
	
	private void scaleImage() {
		imgHeight = rawImage.getHeight();
		imgWidth = rawImage.getWidth();
		imgHeight = (int) (imgHeight*zoomLevel);
		imgWidth = (int) (imgWidth*zoomLevel);
		image = rawImage.getScaledInstance(imgWidth, imgHeight, Image.SCALE_FAST);
		top = (int) (rawTop*zoomLevel);
		left = (int) (rawLeft*zoomLevel);
		height = (int) (rawHeight*zoomLevel);
		width = (int) (rawWidth*zoomLevel);
	}
	
	protected Point getImageLocation() {
		Point p = null;
		if (image != null) {
			int x = (getWidth() - imgWidth)/2;
			int y = (getHeight() - imgHeight)/2;
			p = new Point(x, y);
		}
		return p;
	}
	
	public Point getPixelLocation(Point p) {
		Point imageLocation = getImageLocation();
		Point pixelLocation = new Point(p.x-imageLocation.x, p.y-imageLocation.y);
		return pixelLocation;
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
			hasROI = false;
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
			if (height<0 || width < 0) {
				hasROI = false;
			} else {
				hasROI = true;
			}
			
		}
		updateImage();
	}
	
	protected void setROI(int top, int left, int height, int width) {
		this.rawTop = top;
		this.rawLeft = left;
		this.rawHeight = height;
		this.rawWidth = width;
		scaleImage();
		this.hasROI = true;
		this.updateImage();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			Point p = getImageLocation();
			g.drawImage(image,  p.x,  p.y,  this);
			if (hasROI) {
				g.setColor(Color.RED);
				Point p2 = computePanelLocation(left, top);
				g.drawRect(p2.x, p2.y, width, height);
			}
		}
	}
	
	public boolean updateImage(String filename) {
		try {
    		rawImage = ImageIO.read(new File(filename));
		} catch(IOException e) {
			logger.severe("failed to open image:" + filename);
		}		
		scaleImage();
		if (image != null) {
			repaint();
			return true;
		}
		return false;
		//TODO: show something different if failed to open image
	}
	
	private void updateImage() {
		//???? i think this assertion should not be here, or maybe this method is not necessary
//		assert(hasROI);
		repaint();
	}
	
	public int[] getROI() {
		if (!hasROI) {
			return null;
		}
		int[] array = {(int) (top/zoomLevel), 
						(int) ((top+height)/zoomLevel), 
						(int) (left/zoomLevel), 
						(int) ((left+width)/zoomLevel)};
		return array;
	}
}