package dc.gui_old;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import dc.process.Controller;

@SuppressWarnings("serial")
public class DriftCorrectionPanel extends JPanel {
	
	private static final Logger logger = Logger.getLogger(DriftCorrectionPanel.class.getName());
	private Controller controller;
	private JButton runBtn;
	
	
	public DriftCorrectionPanel() {

		JPanel btnGroup = new JPanel();
		btnGroup.setLayout(new BoxLayout(btnGroup, BoxLayout.Y_AXIS));
		runBtn = new JButton("RUN DRIFT CORRECTION");
		runBtn.addActionListener(new RunBtnListener());
		btnGroup.add(runBtn);
		add(btnGroup);
	}
	
	protected void setController(Controller controller) {
		this.controller = controller;
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	protected void toggleDriftCorrectionBtn(boolean flag) {
		if (flag) {
			runBtn.setText("RUN DRIFT CORRECTION");
		} else {
			runBtn.setText("CANCEL DRIFT CORRECTION");
		}
	}
	
	private class RunBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			logger.fine("triggered run btn");
			DriftCorrectionPanel.this.controller.runDriftCorrection();
		}
	}

}
