package dc.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class BooleanModel {
	public static final String TEXT = "flag";
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	private boolean flag;
	
	public BooleanModel() {
		flag = false;
	}
	
	public BooleanModel(boolean flag) {
		this.flag = flag;
	}
	
	public void set(boolean flag) {
		boolean old = this.flag;
		this.flag = flag;
		support.firePropertyChange(TEXT, old, flag);
	}
	
	public boolean get() {
		return this.flag;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	 public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		 support.addPropertyChangeListener(propertyName, listener);
	 }
}
