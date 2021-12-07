package dc.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import dc.gui.image.RawImageViewer;
import dc.gui_old.Synchroniser;
import javax.swing.JSplitPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import dc.gui.image.ImageViewer;
import java.awt.Dimension;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	private JPanel contentPane;
	private final StatusPanel statusPanel = new StatusPanel(0);
	private final JSplitPane splitPane = new JSplitPane();
	private final SettingPanel settingPanel = new SettingPanel();
	private final JMenuBar menuBar = new JMenuBar();
	private final JMenu helpMenu = new JMenu("Help");
	private final JMenuItem howToItem = new JMenuItem("How to use");
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
		setPreferredSize(new Dimension(1200, 1000));
		setSize(new Dimension(1200, 1000));
		setTitle("Drift Correction");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		
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
		
		RawImageViewer rawImageViewer = new RawImageViewer((Synchroniser) null);
		splitPane.setLeftComponent(rawImageViewer);
		
		splitPane.setRightComponent(imageViewer);
		splitPane.setDividerLocation(0.5);
		
		contentPane.add(settingPanel, BorderLayout.NORTH);
	}

}
