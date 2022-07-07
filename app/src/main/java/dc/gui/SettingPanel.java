package dc.gui;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.PlainDocument;

import dc.controller.Controller;
import dc.model.DriftModel;
import dc.model.DriftSectionModel;
import dc.model.MovieStateModel;
import dc.model.TemplateMatchingSegmentModel;

import java.awt.BorderLayout;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;

@SuppressWarnings("serial")
public class SettingPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(SettingPanel.class.getName());
	private MovieStateModel state;		// state that GUI is displaying
	
	private JButton prevButton;
	private JButton nextButton;
	private JPanel stepPanel;
	private IOPanel ioPanel;
	private TemplateMatchingPanel templateMatchingPanel;
	private DriftEditingPanel driftEditingPanel;
	private int viewState;
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
		ioPanel.setController(controller);
		templateMatchingPanel.setController(controller);
		driftEditingPanel.setController(controller);
		
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		ioPanel.setFileHandler(fh);
		templateMatchingPanel.setFileHandler(fh);
		driftEditingPanel.setFileHandler(fh);
	}
	
	
	private void setHandlers() { 
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				assert state.getValue() != MovieStateModel.INIT;
				viewState--;
				updateView();
				setButtons();
			}
		});
		
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				assert viewState < state.getValue();
				viewState++;
				updateView();
				setButtons();
			}
		});
	}
	
	public void addMovieStateModelListener(MovieStateModel model) {
		state = model;
		model.addChangeListener(new StateChangeListener());
		viewState = model.getValue();
		
		setHandlers();
		setButtons(); 
	}
	
	private class StateChangeListener implements ChangeListener{

		@Override
		public void stateChanged(ChangeEvent e) {
			MovieStateModel source = (MovieStateModel) e.getSource();
			int state = (int)source.getValue();
			switch (state) {
				case 0:
				case 1:
				case 2:
					viewState = state;
					break;
				case 3:
					viewState = 2;		// do not have view for model state3
			}
			updateView();
			setButtons();
		}
	}

	private void updateView() {
		stepPanel.removeAll();
		if (viewState == MovieStateModel.INIT) {
			stepPanel.add(ioPanel);
		} else if (viewState == MovieStateModel.TEMPLATE_MATCHING) {
			stepPanel.add(templateMatchingPanel);
		} else {
			stepPanel.add(driftEditingPanel);
		}
		stepPanel.revalidate();
		stepPanel.repaint();
		logger.info("changed view state to: " + state);
	}
	
	private void setButtons() {
		assert viewState <= state.getValue();
		assert viewState >= 0;
		// not using switch because switch conditions must be constant
		if (viewState == MovieStateModel.INIT) {
			prevButton.setToolTipText("There is no previous step.");
			if (state.getValue() > viewState) {
				nextButton.setToolTipText("must set movie and save locations before continue");
			} else {
				nextButton.setToolTipText("ready to set templates");
			}		
		} else if (viewState == MovieStateModel.TEMPLATE_MATCHING) {
			prevButton.setToolTipText("Go back to movie setting.");
			if (state.getValue() > viewState) {
				nextButton.setToolTipText("must perform template matching before continue");
			} else {
				nextButton.setToolTipText("ready to view drift");
			}
		} else if (viewState >= MovieStateModel.DRIFT_EDIT) {
			prevButton.setToolTipText("Go back to template matching.");
			nextButton.setToolTipText("This is the last step.");
		} else {
			logger.severe("unknown state: " + viewState);
		}
		if (viewState == MovieStateModel.INIT) {
			prevButton.setEnabled(false);
		} else {
			prevButton.setEnabled(true);
		}
		if (viewState >= state.getValue()) {
			nextButton.setEnabled(false);
		} else {
			nextButton.setEnabled(true);
		}
	}
	
	protected void setFileNameModels(PlainDocument inputFileName, PlainDocument outputFileName) {
		ioPanel.setFileNameModels(inputFileName, outputFileName);
	}

	protected void setTemplateTableModel(TemplateMatchingSegmentModel model) {
		templateMatchingPanel.setTableModel(model);
	}
	
	protected void setPlotSelectionModel(DefaultListSelectionModel model) {
		driftEditingPanel.setPlotSelectionModel(model);
	}
	
	protected void setDriftModel(DriftModel driftModel) {
		driftEditingPanel.setDriftModel(driftModel);
	}
	
	protected void setDriftSectionModel(DriftSectionModel sectionModel) {
		driftEditingPanel.setDriftSectionModel(sectionModel);
	}
	
	protected void setRawFrameModel(BoundedRangeModel model) {
		templateMatchingPanel.setRawFrameModel(model);
	}
	
	
	protected void setTemplateMatchingBtn(boolean enableFlag, boolean runFlag) {
		templateMatchingPanel.setRunBtn(enableFlag, runFlag);
	}
	
	public void toggleDriftCorrectionBtn(boolean flag) {
		
	}



}
