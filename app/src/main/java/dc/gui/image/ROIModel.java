package dc.gui.image;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
//import java.util.logging.Logger;

public class ROIModel {
//	private static final Logger logger = Logger.getLogger(ROIModel.class.getName());
	
	public static final String TEXT = "ROI";
	public static final int TOP = 0;
	public static final int BOTTOM = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;
	public static final int FLAG = 4;
	public static final int HAS_ROI = 1;
	public static final int NO_ROI = -1;
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	private int[] ROI = {0,0,0,0,NO_ROI};
	
	public void setROI(int[] ROI) {
		if (ROI != null) {
			int[] old = this.ROI;
			this.ROI = ROI;
//			logger.info("ROI changed to: " + ROI[0] + " " + ROI[1] + " " + ROI[2] + " " + ROI[3] + " " + ROI[4]);
			support.firePropertyChange(TEXT, old, ROI);
		}
	}
	
	public void removeROI() {
		ROI[FLAG] = NO_ROI;
//		logger.info("ROI changed to: " + ROI[0] + " " + ROI[1] + " " + ROI[2] + " " + ROI[3] + " " + ROI[4]);
		support.firePropertyChange(TEXT, null, ROI);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	 public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		 support.addPropertyChangeListener(propertyName, listener);
	 }
}
