package dc.gui.image;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
//import java.util.logging.Logger;

public class ROIModel {
//	private static final Logger logger = Logger.getLogger(ROIModel.class.getName());
	
	public static final String ARRAY = "ROI";
	public static final String FLAG = "flag";
	public static final int TOP = 0;
	public static final int BOTTOM = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	private int[] ROI = {0,0,0,0};
	private boolean flag = false;
	
	public void setROI(int[] ROI) {
		if (ROI != null) {
			int[] old = this.ROI;
			this.ROI = ROI;
			this.flag = true;
//			logger.info("ROI changed to: " + ROI[0] + " " + ROI[1] + " " + ROI[2] + " " + ROI[3] + " " + ROI[4]);
			support.firePropertyChange(ARRAY, old, ROI);
			support.firePropertyChange(FLAG, null, flag);
		}
	}
	
	public void removeROI() {
		if (flag== false) {
			return;
		}
		flag = false;
//		logger.info("ROI changed to: " + ROI[0] + " " + ROI[1] + " " + ROI[2] + " " + ROI[3] + " " + ROI[4]);
		support.firePropertyChange(FLAG, null, flag);
	}
	
	protected boolean getFlag() {
		return this.flag;
	}
	
	protected int[] getROI() {
		return this.ROI;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	 public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		 support.addPropertyChangeListener(propertyName, listener);
	 }
}
