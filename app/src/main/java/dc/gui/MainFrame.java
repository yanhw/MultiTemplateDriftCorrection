package dc.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dc.App;
import dc.controller.Controller;
import dc.gui.image.ImageViewer;
import dc.gui.image.RawImageViewer;
import dc.model.BooleanModel;
import dc.model.DriftModel;
import dc.model.DriftSectionModel;
import dc.model.DriftUpdateStateModel;
import dc.model.FileListModel;
import dc.model.MovieStateModel;
import dc.model.TemplateMatchingSegmentModel;
import dc.model.TextModel;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	
	private static final Logger logger = Logger.getLogger(MainFrame.class.getName());
	
	private static final String PROP_WINDOW_WIDTH = "window width";
	private static final String PROP_WINDOW_HEIGHT = "window height";
	private static final String PROP_X_LOC = "window x";
	private static final String PROP_Y_LOC = "window y";
	private static final int DEFAULT_HEIGHT = 1000;
	private static final int DEFAULT_WIDTH = 1200;
	private static final int DEFAULT_X = 100;
	private static final int DEFAULT_Y = 100;
	
	private JPanel contentPane;
	private final StatusPanel statusPanel = new StatusPanel(0);
	private final JSplitPane splitPane = new JSplitPane();
	private final SettingPanel settingPanel = new SettingPanel();
	private final RawImageViewer rawImageViewer = new RawImageViewer();
	private final ImageViewer imageViewer = new ImageViewer();
	private final DCMenuBar menuBar = new DCMenuBar();
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		int preferedWidth = Integer.parseInt(App.prop.getProperty(PROP_WINDOW_WIDTH, ""+DEFAULT_WIDTH));
		int preferedHeight = Integer.parseInt(App.prop.getProperty(PROP_WINDOW_HEIGHT, ""+DEFAULT_HEIGHT));
		setPreferredSize(new Dimension(preferedWidth, preferedHeight));
		setSize(new Dimension(preferedWidth, preferedHeight));
		setTitle("Drift Correction");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		int window_x = Integer.parseInt(App.prop.getProperty(PROP_X_LOC, ""+DEFAULT_X));
		int window_y = Integer.parseInt(App.prop.getProperty(PROP_Y_LOC, ""+DEFAULT_Y));
		setBounds(window_x, window_y, preferedWidth, preferedHeight);
		
		setJMenuBar(menuBar);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.add(statusPanel, BorderLayout.SOUTH);
		
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		splitPane.setLeftComponent(rawImageViewer);
		
		splitPane.setRightComponent(imageViewer);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(0.5);
		splitPane.setResizeWeight(0.5);
		
		contentPane.add(settingPanel, BorderLayout.NORTH);
		addComponentListener(new FrameDisplayListener());		
		addWindowListener(new CloseBtnListener());
	}
	
	public void initialise(Controller controller, FileHandler fh) {
		logger.setLevel(Level.FINE);
		logger.addHandler(fh);
		settingPanel.setFileHandler(fh);
		rawImageViewer.setFileHandler(fh);
		imageViewer.setFileHandler(fh);
		statusPanel.setFileHandler(fh);
		
		addRawFrameChangeListener();
		addPlotSelectionListener();
		addROIListener();
		
		settingPanel.setController(controller);
		menuBar.setController(controller, rootPane);
		controller.setMainFrame(this);
		
		this.setVisible(true);
	}
	
	// model setters to be set by controller, these links data to view
	public void setWarningModel(TextModel model) {
		model.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String text = (String) evt.getNewValue();
				if (text != "") {
					JOptionPane.showMessageDialog(rootPane, text);
					TextModel model = (TextModel) evt.getSource();
					model.setText("");
				}
			}
		});
	}
	
	public void setMovieStateModel(MovieStateModel model) {
		settingPanel.addMovieStateModelListener(model);
		imageViewer.addMovieStateModelListener(model);
	}
	
	public void setTemplateTableModel(TemplateMatchingSegmentModel model) {
		settingPanel.setTemplateTableModel(model);
		imageViewer.setTemplateTableModel(model);
	}
	
	public void setFileModels(FileListModel rawFileList, FileListModel correctedFileList) {
		rawImageViewer.setRawFileModel(rawFileList);
		imageViewer.setRawFileModel(rawFileList);
		imageViewer.setDriftCorrectedFileModel(correctedFileList);
	}
	
	public void setFileNameModels(TextModel inputFileName, TextModel outputFileName) {
		settingPanel.setFileNameModels(inputFileName, outputFileName);
	}
	
	public void setDriftModel(DriftModel driftModel) {
		settingPanel.setDriftModel(driftModel);
		imageViewer.setDriftDataModel(driftModel);
	}
	
	public void setDriftSectionModel(DriftSectionModel sectionModel) {
		settingPanel.setDriftSectionModel(sectionModel);
	}
	
	public void setProgressModel(BoundedRangeModel model) {
		statusPanel.setProgessBarModel(model);
	}
	
	public void setStatusModel(TextModel model) {
		rawImageViewer.setStatusModel(model);
		statusPanel.setTextModel(model);
	}
	
	public void setRunningFlagModel(BooleanModel model) {
		menuBar.setRunningFlagModel(model);
		settingPanel.setRunningFlagModel(model);
	}
	
	public void setDefaultParameters(AtomicInteger gaussianKernel, AtomicInteger gaussianInteration, AtomicInteger templateMatchingMethod,
			AtomicInteger maxThreads, AtomicInteger maxDegree) {
		menuBar.setDefaultParameters(gaussianKernel, gaussianInteration, templateMatchingMethod, maxThreads, maxDegree);
		settingPanel.setMaxDegree(maxDegree);
	}
	
	
	// model setters between view components, these sync view
	private void addRawFrameChangeListener() {
		BoundedRangeModel model = rawImageViewer.getRawFrameModel();
		settingPanel.setRawFrameModel(model);
	}
	
	private void addPlotSelectionListener() {
		DefaultListSelectionModel xModel = imageViewer.getXSelectionModel();
		DefaultListSelectionModel yModel = imageViewer.getYSelectionModel();
		rawImageViewer.setPlotSelectionModel(xModel);
		rawImageViewer.setPlotSelectionModel(yModel);
		settingPanel.setPlotSelectionModel(xModel);
		settingPanel.setPlotSelectionModel(yModel);
	}
	
	private void addROIListener() {
		settingPanel.setROIModel(rawImageViewer.getROI());
		statusPanel.setROIModel(rawImageViewer.getROI());
	}
	
	public void setDriftUpdateModel(DriftUpdateStateModel driftUpdateModel) {
		driftUpdateModel.addChangeListener(new StateChangeListener());
	}

	private class StateChangeListener implements ChangeListener{
		private BlockingPopupDialog myDialog;
		@Override
		public void stateChanged(ChangeEvent e) {
			DriftUpdateStateModel source = (DriftUpdateStateModel) e.getSource();
			int state = (int)source.getValue();
			logger.info("drift plot state change: " + state);
			switch (state) {
				case DriftUpdateStateModel.OK:
					break;
				case DriftUpdateStateModel.UPDATING:
					myDialog = new BlockingPopupDialog("updating drift information, please wait...");
					break;
				case DriftUpdateStateModel.NEED_CHECK:
					myDialog.updateText("checking update for drift plot, please wait...");
					imageViewer.updateDrift();
					myDialog.dispose();
					revalidate();
					repaint();
					source.setValue(DriftUpdateStateModel.OK);
			}	
		}
	}
	
	private class FrameDisplayListener implements ComponentListener {

		@Override
		public void componentResized(ComponentEvent e) {
			int height = MainFrame.this.getHeight();
			int width = MainFrame.this.getWidth();
			App.prop.setProperty(PROP_WINDOW_HEIGHT, ""+height);
			App.prop.setProperty(PROP_WINDOW_WIDTH, ""+width);
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			int x = MainFrame.this.getX();
			int y = MainFrame.this.getY();
			App.prop.setProperty(PROP_X_LOC, ""+x);
			App.prop.setProperty(PROP_Y_LOC, ""+y);
		}

		@Override
		public void componentShown(ComponentEvent e) {
		}

		@Override
		public void componentHidden(ComponentEvent e) {
		}
		
	}
	
	private class CloseBtnListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e){
			FileOutputStream out;
			try {
				out = new FileOutputStream(App.userHome + File.separator +  App.PROP_FILE);
				App.prop.store(out, "---No Comment---");
				out.close();
			} catch (IOException e1) {
				logger.info("failed to save property file");
			}
			
			e.getWindow().dispose();
		}
	}
}
