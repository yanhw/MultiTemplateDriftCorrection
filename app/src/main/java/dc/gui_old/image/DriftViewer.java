package dc.gui_old.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
//import javax.swing.UIManager;


 // replaced by JfreeChart version
@Deprecated
@SuppressWarnings("serial")
public class DriftViewer extends JPanel {

	private int[] idxList;
	private int[] dataList;
	private float[] fittedDataList;
	private boolean[] isSelected;
    private int margin=50;
    private int radius = 2;
    private int maxX;
    private int maxY;
    private int minX;
    private int minY;
    
    private boolean hasFirstPoint = false;
    private Point firstPoint;
//    private Point secondPoint;
    
    // ROI in coordinate
    private boolean hasROI = false;
    private double left;
    private double top;
    private double right;
    private double bottom;

   
	public DriftViewer() {
		
		this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point panelPoint = e.getPoint();
//                Point piexelLocation = getPixelLocation(panelPoint);
                setPressedPoint(panelPoint);
//                System.out.println("You clicked at " + panelPoint + " which is relative to the image " + imgContext);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                Point panelPoint = e.getPoint();
//                Point piexelLocation = getPixelLocation(panelPoint);
                setReleasedPoint(panelPoint);
//                System.out.println("You clicked at " + panelPoint + " which is relative to the image " + imgContext);
            }
        });
        
        this.addMouseMotionListener(new MouseAdapter() {
        	@Override
        	public void mouseDragged(MouseEvent e) {
        		Point panelPoint = e.getPoint();
//        		Point piexelLocation = getPixelLocation(panelPoint);
          
        		setMovingPoint(panelPoint);
        	}
        });
	}
	
	
//	
//	public void setDataTest(int[] xList, int[] yList) {
//		xList = new int[100];
//		yList = new int[100];
//		isSelected = new boolean[100];
//		for (int i = 0; i < 100; i++) {
//			xList[i] = i;
//			yList[i] = (int) ((500*Math.sin((float)i/10))+100);
//			isSelected[i] = false;
//		}
//		
//		updateExtrema();
//	}
	
	public void setData(int[] xList, float[] xFittedList) {
//		assert (xList.length == yList.length);
		this.dataList = xList;
		this.fittedDataList = xFittedList;
		isSelected = new boolean[xList.length];
		this.idxList = new int[xList.length];
		for (int i = 0; i < xList.length; i++) {
			this.idxList[i] = i;
		}
		updateExtrema();
	}
	
	private void setPressedPoint(Point point) {
		assert (point != null);
		firstPoint = point;
		hasFirstPoint = true;
	}
	
	private void setReleasedPoint(Point point) {
		assert (hasFirstPoint);
		assert (point != null);
		hasFirstPoint = false;
		if (point.x==firstPoint.x || point.y==firstPoint.y) {
			hasROI = false;
		}
		updateSelectedPoint();
		repaint();
	}
	
	private void setMovingPoint(Point point) {
		assert (hasFirstPoint);
		assert (point != null);
		if (point.x==firstPoint.x || point.y==firstPoint.y) {
			hasROI = false;
		} else {
			top = Math.min(point.y, firstPoint.y);
			left = Math.min(point.x, firstPoint.x);
			bottom = Math.max(point.y, firstPoint.y);
			right = Math.max(point.x, firstPoint.x);
			top = yPointToCoordinate(top);
			bottom = yPointToCoordinate(bottom);
			left = xPointToCoordinate(left);
			right = xPointToCoordinate(right);
			if (top <= minY || bottom >= maxY || left >= maxX || right <= minX) {
				hasROI = false;
			} else {
				top = Math.min(top, maxY);
				bottom = Math.max(bottom, minY);
				left = Math.max(left, minX);
				right = Math.min(right, maxX);
				
				hasROI = true;
			}		
		}
		updateSelectedPoint();
		repaint();
	}
	
	private void updateSelectedPoint() {
		if (hasROI) {
			assert (left < right);
			assert (top > bottom);
			// get x range
			int start = (int) Math.ceil(left);
			int end = (int) Math.floor(right);
			if (start < 0) {
				start = 0;
			}
			if (end > idxList.length-1) {
				end = idxList.length-1;
			}
//			System.out.println("start: " + start + ". end: " + end + ". left: " + left + ". right: " + right);
//			System.out.println("top: " + top + ". bottom: " + bottom);
			// left
			for (int i = 0; i < start; i++) {
				isSelected[i] = false;
			}
			// target
			for (int i = start; i < end+1; i++) {
				if (dataList[i] <= top && dataList[i] >= bottom) {
					isSelected[i] = true;
				} else {
					isSelected[i] = false;
				}
			}
			// right
			for (int i = end+1; i < idxList.length; i++) {
				isSelected[i] = false;
			}
		} 
		// No ROI, un-select all points
		else {
			for (int i = 0; i < idxList.length; i++) {
				isSelected[i] = false;
			}
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g1=(Graphics2D)g;
        g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int width=getWidth();
        int height=getHeight();
        
        // draw axis
        g1.draw(new Line2D.Double(margin,margin,margin,height-margin));
        g1.draw(new Line2D.Double(margin,height-margin,width-margin,height-margin));
        // draw points
        g1.setPaint(Color.BLUE);
        for(int i=0;i<idxList.length;i++){
        	if (!isSelected[i]) {
        		 double x = xCoordinateToPoint(idxList[i]);
        		 double y = yCoordinateToPoint(dataList[i]);
        		 g1.fill(new Ellipse2D.Double(x-radius,y-radius,2*radius,2*radius));
        	}
        }
        g1.setPaint(Color.GREEN);
        for(int i=0;i<idxList.length;i++){
        	if (isSelected[i]) {
        		double x = xCoordinateToPoint(idxList[i]);
        		double y = yCoordinateToPoint(dataList[i]);
        		g1.fill(new Ellipse2D.Double(x-radius,y-radius,2*radius,2*radius));
        	}
        }
        // draw fitted points
        g1.setPaint(Color.CYAN);
        for(int i=0;i<idxList.length;i++){
        	if (!isSelected[i]) {
        		 double x = xCoordinateToPoint(idxList[i]);
        		 double y = yCoordinateToPoint((int) fittedDataList[i]);
        		 g1.fill(new Ellipse2D.Double(x-radius,y-radius,2*radius,2*radius));
        	}
        }
        // draw ROI
        if (hasROI) {
        	g.setColor(Color.RED);
        	int x1 = (int)xCoordinateToPoint((int)left);
        	int x2 = (int)xCoordinateToPoint((int)right);
        	int y1 = (int)yCoordinateToPoint((int)top);
        	int y2 = (int)yCoordinateToPoint((int)bottom);
        	g.drawRect(x1, y1, (x2-x1), (y2-y1));
        }
        
	}
	
	private void updateExtrema() {
		assert (idxList.length == dataList.length);
		for (int i = 0; i < idxList.length; i++) {
			if (dataList[i] > maxY) {
				maxY = dataList[i];
			} else if (dataList[i] < minY) {
				minY = dataList[i];
			}
		}
		
		minX = 0;
		maxX = idxList.length-1;
	}
	
	private double xPointToCoordinate(double x) {
		double xUnit = (double)(getWidth()-2*margin)/(idxList.length-1);
		return (x-margin)/xUnit;
	}
	
	private double yPointToCoordinate(double y) {
		int height = getHeight();
		double yUnit = (double)(height-2*margin)/(maxY-minY);
		return (height-margin-y)/yUnit+minY;
	}
	
	private double xCoordinateToPoint(int x) {
		double xUnit = (double)(getWidth()-2*margin)/(idxList.length-1);
		return margin+xUnit*x;
	}
	
	private double yCoordinateToPoint(int y) {
		int height = getHeight();
		double yUnit = (double)(height-2*margin)/(maxY-minY);
		return height-margin-yUnit*(y-minY);
	}

	
	// for testing
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("PlotTest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DriftViewer imageViewer = new DriftViewer();
                
        //Add content to the window.
        frame.add(imageViewer, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setSize(800,400);
        frame.setLocation(200,200);
        frame.setVisible(true);
//        imageViewer.updatePicture(0); 
        
    }
    
    
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
