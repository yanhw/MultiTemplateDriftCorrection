package dc.gui;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dc.model.TextModel;


@SuppressWarnings("serial")
public class StatusPanel  extends JPanel{
	private static final Logger logger = Logger.getLogger(StatusPanel.class.getName());
	private JLabel statusLabel;
	private JProgressBar progressBar;
	private boolean isProgressBarVisible = false;
	
	public StatusPanel(int width) {
//		assert (width > 0);
		setBorder(new BevelBorder(BevelBorder.LOWERED));
		setPreferredSize(new Dimension(width, 16));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		statusLabel = new JLabel("program is ready");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		add(statusLabel);
//		add(progressBar);
		
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public void setProgessBarModel(BoundedRangeModel model) {
		progressBar.setModel(model);
		model.addChangeListener(new ProgressChangeListener());
	}
	
	public void setTextModel(TextModel model) {
		model.addPropertyChangeListener(new StatusChangeListener());
	}
	
	private class ProgressChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			BoundedRangeModel model = (BoundedRangeModel) e.getSource();
			int progress = model.getValue();
			if (progress >= 100) {
				remove(progressBar);
				revalidate();
				repaint();
				isProgressBarVisible = false;
				statusLabel.setText("");
				logger.fine("progress finished, hiding progress bar");
			} else {
				if (!isProgressBarVisible) {
					add(progressBar);
					isProgressBarVisible = true;
				}				
				statusLabel.setText("processing... "+progress+"% done");
			}
		}	
	}

	private class StatusChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String text = (String) evt.getNewValue();
			statusLabel.setText(text);
		}
		
	}

}
