package dc.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class TextModel {
	public static final String TEXT = "text";
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	private String text = "";
	
	public void setText(String text) {
		if (text != null) {
			String old = this.text;
			this.text = text;
			support.firePropertyChange(TEXT, old, text);
		}
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	 public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		 support.addPropertyChangeListener(propertyName, listener);
	 }
	 
	 public String getText() {
		 return text;
	 }
}
