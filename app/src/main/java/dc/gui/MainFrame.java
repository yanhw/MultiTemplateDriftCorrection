package dc.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

import dc.controller.Controller;
import dc.gui.image.ImageViewer;
import dc.gui.image.RawImageViewer;
import dc.model.BooleanModel;
import dc.model.DriftModel;
import dc.model.DriftSectionModel;
import dc.model.FileListModel;
import dc.model.MovieStateModel;
import dc.model.TemplateMatchingSegmentModel;
import dc.model.TextModel;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	
	private static final Logger logger = Logger.getLogger(MainFrame.class.getName());
	
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
		int preferedWidth = 1200;
		int preferedHeight = 1000;
		setPreferredSize(new Dimension(preferedWidth, preferedHeight));
		setSize(new Dimension(preferedWidth, preferedHeight));
		setTitle("Drift Correction");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, preferedWidth, preferedHeight);
		
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
		statusPanel.setTextModel(model);
	}
	
	public void setRunningFlagModel(BooleanModel model) {
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

	
}
