package dc.gui_old;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import dc.controller.Controller;

@SuppressWarnings("serial")
public class SettingPanel  extends JPanel {
	private static final Logger logger = Logger.getLogger(SettingPanel.class.getName());
	private Controller controller;
	//TODO remove this.state
	@SuppressWarnings("static-access")
	private int state = controller.INIT;
	
	private JTabbedPane tabbedPane;
	private IOPanel ioPanel;
	private TemplateMatchingPanel templateMatchingPanel;
	private DriftEditingPanel driftEditingPanel;
	private DriftCorrectionPanel driftCorrectionPanel;
	private JButton prevBtn;
	private JButton nextBtn;
	
	public SettingPanel() {
		logger.setLevel(Level.FINE);
		
		ioPanel = new IOPanel();
		templateMatchingPanel = new TemplateMatchingPanel();
		driftEditingPanel = new DriftEditingPanel();
		driftCorrectionPanel = new DriftCorrectionPanel();
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Input/Output", ioPanel);
		tabbedPane.addTab("Template Matching", templateMatchingPanel);
		tabbedPane.addTab("Drift Editing", driftEditingPanel);
		tabbedPane.addTab("Drift Correction", driftCorrectionPanel);
		tabbedPane.setEnabledAt(1, false);
		tabbedPane.setEnabledAt(2, false);
		tabbedPane.setEnabledAt(3, false);
		
		
		JPanel btnPanel = new JPanel();
		
		prevBtn = new JButton("BACK");
		prevBtn.addActionListener(new BtnListener());
		nextBtn = new JButton("NEXT");
		nextBtn.addActionListener(new BtnListener());
		
		btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
		btnPanel.add(prevBtn);
		btnPanel.add(nextBtn);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(tabbedPane);
		this.add(btnPanel);
	}
	
	public void setController(Controller controller) {
		this.controller = controller;
		ioPanel.setController(controller);
		templateMatchingPanel.setController(controller);
		driftEditingPanel.setController(controller);
		driftCorrectionPanel.setController(controller);
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		ioPanel.setFileHandler(fh);
		templateMatchingPanel.setFileHandler(fh);
		driftEditingPanel.setFileHandler(fh);
		driftCorrectionPanel.setFileHandler(fh);
	}
	
	@SuppressWarnings("static-access")
	protected void updateView(int state) {
		if (state < controller.INIT || state > controller.DONE) {
			logger.severe("unknown controller state: " + state);
			return;
		}
		if (state == this.state) {
			logger.warning("view state not chaged: " + state);
			return;
		}
		if (this.state >= controller.DRIFT_CORRECTION) {
			nextBtn.setText("NEXT");
		}
		tabbedPane.setEnabledAt(this.state, false);
		this.state = state;
		tabbedPane.setEnabledAt(this.state, true);
		tabbedPane.setSelectedIndex(this.state);
		if (this.state >= controller.DRIFT_CORRECTION) {
			nextBtn.setText("");
		}
//		if (state == controller.INIT) {
//			
//		} else if (state == controller.CENTER) {
//			
//		} else if (state == controller.MASK_THRES) {
//			
//		} else if (state == controller.MASK_DILA) {
//			
//		} else if (state == controller.RANGE) {
//			
//		} else {
//			
//		}
		logger.info("changed view state to: " + state);
	}
	
	
	protected void setInputFile(String filename) {
		assert (filename != null);
		ioPanel.setInputFile(filename);
	}
	
	protected void setOutputFile(String folderName) {
		assert (folderName != null);
		ioPanel.setOutputFile(folderName);
	}
	
	protected void setTemplateMatchingBtnText(boolean flag) {
		templateMatchingPanel.setRunBtnText(flag);
	}
	
	protected void updateDriftSectionTable() {
		driftEditingPanel.updateDriftSectionTable();
	}	
	
	protected void setDriftTableVisible(int frameNumber) {
		driftEditingPanel.setDriftTableVisible(frameNumber);
	}
	
	protected void toggleTemplateMatchingBtn(boolean flag) {
		templateMatchingPanel.setRunBtnText(flag);
	}
	
	public void toggleDriftCorrectionBtn(boolean flag) {
		driftCorrectionPanel.toggleDriftCorrectionBtn(flag);
	}
	
	private class BtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			String label = ((JButton) evt.getSource()).getText();
			if (label == "BACK") {
//				SettingPanel.this.controller.previousState();
			} else {
//				SettingPanel.this.controller.advanceState();
			}
			
		}	// end of method
	}	// end of class





}
