package dc.gui_old;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;


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
	
	public void setStatusLabel(String message) {
		statusLabel.setText(message);
	}
	
	protected void setProgress(int progress) {
		assert (progress >= 0);
		logger.info("progress set to: " + progress);
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
			progressBar.setValue(progress);
		}
		statusLabel.setText("processing... "+progress+"% done");
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	// not using because the thread structure in controller is too deep
	private class ProgressBarListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("progress" == evt.getPropertyName()) {
				logger.info("progress changed");
				int progress = (Integer) evt.getNewValue();
				StatusPanel.this.setProgress(progress);
			} 
		}
	} 

}
