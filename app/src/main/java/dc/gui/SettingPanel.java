package dc.gui;

import javax.swing.JPanel;

import dc.model.DriftModel;
import dc.model.DriftSectionModel;
import dc.process.Controller;

import javax.swing.JLayeredPane;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;

public class SettingPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(SettingPanel.class.getName());
	private Controller controller;
	private Synchroniser sync;
	private int state = controller.INIT;		// state that GUI is displaying
	
	JButton prevButton;
	JButton nextButton;
	private JPanel stepPanel;
	private IOPanel ioPanel;
	private TemplateMatchingPanel templateMatchingPanel;
	private DriftEditingPanel driftEditingPanel;
//	private DriftCorrectionPanel driftCorrectionPanel;
	
	/**
	 * Create the panel.
	 */
	public SettingPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel bottomPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) bottomPanel.getLayout();
		flowLayout.setHgap(25);
		add(bottomPanel, BorderLayout.SOUTH);
		
		prevButton = new JButton("<<");
		bottomPanel.add(prevButton);
		
		nextButton = new JButton(">>");
		bottomPanel.add(nextButton);
		
		stepPanel = new JPanel();
		add(stepPanel, BorderLayout.CENTER);
		stepPanel.setLayout(new BorderLayout(0, 0));
		
		ioPanel = new IOPanel();
		templateMatchingPanel = new TemplateMatchingPanel();
		driftEditingPanel = new DriftEditingPanel();
		stepPanel.add(ioPanel);
	}
	
	
	public void setController(Controller controller) {
		this.controller = controller;
		ioPanel.setController(controller);
		templateMatchingPanel.setController(controller);
		driftEditingPanel.setController(controller);
		
		setHandlers();
		setButtons(); 
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		ioPanel.setFileHandler(fh);
		templateMatchingPanel.setFileHandler(fh);
		driftEditingPanel.setFileHandler(fh);
	}
	
	public void setSynchroniser(Synchroniser sync) {
		this.sync = sync;
	}
	
	private void setHandlers() { 
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				assert state > 0;
				state--;
				updateView();
				setButtons();
			}
		});
		
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int modelState = sync.getState();
				assert state < modelState;
				state++;
				updateView();
				setButtons();
			}
		});
	}
	
	
	@SuppressWarnings("static-access")
	private void updateView() {
		stepPanel.removeAll();
		if (state == controller.INIT) {
			stepPanel.add(ioPanel);
		} else if (state == controller.TEMPLATE_MATCHING) {
			stepPanel.add(templateMatchingPanel);
		} else {
			stepPanel.add(driftEditingPanel);
		}
		stepPanel.revalidate();
		logger.info("changed view state to: " + state);
	}
	
	@SuppressWarnings("static-access")
	protected void setButtons() {
		assert state <= sync.getState();
		assert state >= 0;
		// not using switch because switch conditions must be constant
		if (state == controller.INIT) {
			prevButton.setToolTipText("There is no previous step.");
			if (sync.getState() > state) {
				nextButton.setToolTipText("must set movie and save locations before continue");
			} else {
				nextButton.setToolTipText("ready to set templates");
			}		
		} else if (state == controller.TEMPLATE_MATCHING) {
			prevButton.setToolTipText("Go back to movie setting.");
			if (sync.getState() > state) {
				nextButton.setToolTipText("must perform template matching before continue");
			} else {
				nextButton.setToolTipText("ready to view drift");
			}
		} else if (state == controller.DRIFT_EDIT) {
			prevButton.setToolTipText("Go back to template matching.");
			nextButton.setToolTipText("This is the last step.");
		} else {
			logger.severe("unknown state: " + state);
		}
		if (state == controller.INIT) {
			prevButton.setEnabled(false);
		} else {
			prevButton.setEnabled(true);
		}
		if (state == sync.getState()) {
			nextButton.setEnabled(false);
		} else {
			nextButton.setEnabled(true);
		}
	}
	
	
	protected void setInputFile(String filename) {
		assert (filename != null);
		ioPanel.setInputFile(filename);
	}
	
	protected void setOutputFile(String folderName) {
		assert (folderName != null);
		ioPanel.setOutputFile(folderName);
	}
	
	protected void setDriftModel(DriftModel driftModel) {
		driftEditingPanel.setDriftModel(driftModel);
	}
	
	protected void setDriftSectionModel(DriftSectionModel sectionModel) {
		driftEditingPanel.setDriftSectionModel(sectionModel);
	}
	
	protected void updateDriftSectionTable() {
		driftEditingPanel.updateDriftSectionTable();
	}	
	
	protected void setDriftTableVisible(int frameNumber) {
		driftEditingPanel.setDriftTableVisible(frameNumber);
	}
	
	protected void setTemplateMatchingBtn(boolean enableFlag, boolean runFlag) {
		templateMatchingPanel.setRunBtn(enableFlag, runFlag);
	}
	
	public void toggleDriftCorrectionBtn(boolean flag) {
		
	}


}
