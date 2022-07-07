package dc.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.PlainDocument;

import dc.gui.image.RawImageViewer;
import dc.model.DriftModel;
import dc.model.DriftSectionModel;
import dc.model.MovieStateModel;
import dc.model.RawFileModel;
import dc.model.TemplateMatchingSegmentModel;

import javax.swing.JSplitPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import dc.controller.Controller;
import dc.gui.image.ImageViewer;
import java.awt.Dimension;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	
	private static final Logger logger = Logger.getLogger(MainFrame.class.getName());
	
	private JPanel contentPane;
	private final StatusPanel statusPanel = new StatusPanel(0);
	private final JSplitPane splitPane = new JSplitPane();
	private final SettingPanel settingPanel = new SettingPanel();
	private final JMenuBar menuBar = new JMenuBar();
	private final JMenu helpMenu = new JMenu("Help");
	private final JMenuItem howToItem = new JMenuItem("How to use");
	private final RawImageViewer rawImageViewer = new RawImageViewer();
	private final ImageViewer imageViewer = new ImageViewer();
	private final JMenu projectMenu = new JMenu("Project");
	private final JMenuItem advancedSettingItem = new JMenuItem("Advanced Setting...");
	private final JMenuItem clearAllItem = new JMenuItem("Clear All");

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
		
		menuBar.add(projectMenu);
		
		projectMenu.add(clearAllItem);
		
		projectMenu.add(advancedSettingItem);
		
		menuBar.add(helpMenu);
		
		helpMenu.add(howToItem);
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
		
		settingPanel.setController(controller);
		controller.setMainFrame(this);
		
		this.setVisible(true);
	}
	
	// model setters to be set by controller, these links data to view
	public void setMovieStateModel(MovieStateModel model) {
		settingPanel.addMovieStateModelListener(model);
		imageViewer.addMovieStateModelListener(model);
	}
	
	public void setTemplateTableModel(TemplateMatchingSegmentModel model) {
		settingPanel.setTemplateTableModel(model);
		imageViewer.setTemplateTableModel(model);
	}
	
	public void setRawFileModel(RawFileModel fileList) {
		rawImageViewer.setRawFileModel(fileList);
		imageViewer.setRawFileModel(fileList);
	}
	
	public void setFileNameModels(PlainDocument inputFileName, PlainDocument outputFileName) {
		settingPanel.setFileNameModels(inputFileName, outputFileName);
	}
	
	public void setDriftModel(DriftModel driftModel) {
		settingPanel.setDriftModel(driftModel);
		imageViewer.setDriftDataModel(driftModel);
	}
	
	public void setDriftSectionModel(DriftSectionModel sectionModel) {
		settingPanel.setDriftSectionModel(sectionModel);
	}
	
	// model setters between view components, these sync view
	private void addRawFrameChangeListener() {
		BoundedRangeModel model = rawImageViewer.getRawFrameModel();
		settingPanel.setRawFrameModel(model);
	}
	
	private void addPlotSelectionListener() {
		DefaultListSelectionModel xModel = imageViewer.getXSelectionModel();
		DefaultListSelectionModel yModel = imageViewer.getXSelectionModel();
		rawImageViewer.setPlotSelectionModel(xModel);
		rawImageViewer.setPlotSelectionModel(yModel);
		settingPanel.setPlotSelectionModel(xModel);
		settingPanel.setPlotSelectionModel(yModel);
	}
	
	//////////// template matching
	public void setTemplateMatchingBtn(boolean enableFlag, boolean runFlag) {
		settingPanel.setTemplateMatchingBtn(enableFlag, runFlag);
	}
	

	////////// drift correction
	public void toggleDriftCorrectionBtn(boolean flag) {
		settingPanel.toggleDriftCorrectionBtn(flag);
	}
	
	/////////// raw Image
	
	public int[] getRawROI() {
		return rawImageViewer.getROI();
	}
	
	////////// right image panel
	
	
	public void setCorrectedImages(List<String> list) {
		imageViewer.setCorrectedImages(list);
	}
	
	////////// status panel
	public void updateStatus(String message) {
		statusPanel.setStatusLabel(message);
	}
	
	public void setProgress(int num) {
		statusPanel.setProgress(num);
	}


}
