package dc.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;

import dc.App;
import dc.controller.Controller;
import dc.model.BooleanModel;
import dc.utils.Constants;

@SuppressWarnings("serial")
public class DCMenuBar extends JMenuBar {
	private static final Logger logger = Logger.getLogger(DCMenuBar.class.getName());
	
	private final String PROP_SHOW_TOOLTEXT = "show tooltip text";
	private final String PROP_GAUSSIAN_KERNEL = "gaussian kernel";
	private final String PROP_GAUSSIAN_ITERATION = "gaussian iteration";
	private final String PROP_TM_METHOD = "template matching method";
	private final String PROP_MAX_THREAD = "max thread number";
	private final String PROP_MAX_DEGREE = "max fitting degree";
	
	private final JMenu helpMenu = new JMenu("Help");
	private final JMenuItem howToItem = new JMenuItem("How to use");
	private final JCheckBoxMenuItem tooltipItem = new JCheckBoxMenuItem("Show tooltip text");
	private final JMenu projectMenu = new JMenu("Project");
	private final JMenu advancedSettingMenu = new JMenu("Advanced Setting");
	private final JMenuItem gaussianMenu = new JMenuItem("Gaussian Option");
	private final JMenuItem templateMatchingMenu = new JMenuItem("Template Matching Option");
	private final JMenuItem numThreadMenu = new JMenuItem("Maximun Number of Threads");
	private final JMenuItem maxDegreeMenu = new JMenuItem("Maximum Fitting Degree");
	private final JMenuItem clearAllItem = new JMenuItem("Clear Current Session");
	private final JMenuItem resetAllToDefault = new JMenuItem("Reset Advanced Options to Default Value");
	
	private Controller controller;
	private JRootPane rootPane;

	private AtomicInteger gaussianKernel;
	private AtomicInteger gaussianInteration;
	private AtomicInteger templateMatchingMethod;
	private AtomicInteger maxThreads;
	private AtomicInteger maxDegree;
	
	public DCMenuBar() {
		add(projectMenu);
		projectMenu.add(clearAllItem);	
		projectMenu.add(advancedSettingMenu);
		advancedSettingMenu.add(gaussianMenu);
		advancedSettingMenu.add(templateMatchingMenu);
		advancedSettingMenu.add(numThreadMenu);
		advancedSettingMenu.add(maxDegreeMenu);
		advancedSettingMenu.add(resetAllToDefault);
		
		add(helpMenu);
		helpMenu.add(howToItem);
		helpMenu.add(tooltipItem);
		boolean flag = Boolean.parseBoolean(App.prop.getProperty(PROP_SHOW_TOOLTEXT, ""+true));
		tooltipItem.setSelected(flag);
		tooltipItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ToolTipManager.sharedInstance().setEnabled(tooltipItem.isSelected());
				App.prop.setProperty(PROP_SHOW_TOOLTEXT, ""+flag);
			}
		});
	}

	protected void setController(Controller controller, JRootPane rootPane) {
		this.controller = controller;
		this.rootPane = rootPane;
		clearAllItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int reply = JOptionPane.showOptionDialog(DCMenuBar.this.rootPane, "Clear current session? All unsaved data will be lost.",
						"Clear Session", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
						null, null, null);
				if (reply == JOptionPane.OK_OPTION) {
					DCMenuBar.this.controller.clearSession();
				}
			}
		});
		
		howToItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String uri = Constants.URI;
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					try {
						Desktop.getDesktop().browse(new URI(uri));
					} catch (IOException | URISyntaxException e1) {
						logger.info("error occured when opening the wiki page");
						logger.warning(e1.getStackTrace().toString());
						String text = "Failed to access web browser,\n"
								+ "please use the following url to view user manual:\n"
								+ uri;
						JOptionPane.showMessageDialog(DCMenuBar.this.rootPane, text);
						e1.printStackTrace();
					}
				} else {
					logger.info("browser not supported, showing help url dialog");
					String text = "Web browser is not detected in your system,\n"
							+ "please use the following url to view user manual:\n"
							+ uri;
					JOptionPane.showMessageDialog(DCMenuBar.this.rootPane, text);
				}
			}
		});
		
		gaussianMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextField kernelSize = new JTextField(gaussianKernel.toString());
				JTextField iterations = new JTextField(gaussianInteration.toString());
				final JComponent[] inputs = new JComponent[] {
						new JLabel("Set parameters for the gaussian filter, this filter is used to blur images during template matching."),
				        new JLabel("Kernel Size"),
				        kernelSize,
				        new JLabel("Number of Iterations"),
				        iterations
				};
				int reply = JOptionPane.showConfirmDialog(null,inputs, "Gaussian Filter",JOptionPane.PLAIN_MESSAGE);
				if (reply == JOptionPane.OK_OPTION) {
					int size, iteration;
					try {
						size = Integer.parseInt(kernelSize.getText());
						iteration = Integer.parseInt(iterations.getText());
					} catch (NumberFormatException exp) {
						String text = "Invalid input!\n"
								+ "Number of iterations must be a positive integer!\n"
								+ "Guassian kernel size must be an odd positive integer!";
						JOptionPane.showMessageDialog(DCMenuBar.this.rootPane, text);
						return;
					}
					DCMenuBar.this.controller.setGaussianKernel(size, iteration);
					App.prop.setProperty(PROP_GAUSSIAN_KERNEL, gaussianKernel.toString());
					App.prop.setProperty(PROP_GAUSSIAN_ITERATION, gaussianInteration.toString());
				}
			}
		});
		
		templateMatchingMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> methodList = new JComboBox<String>(Constants.TM_METHOD_LIST);
				methodList.setSelectedIndex(templateMatchingMethod.get());
				final JComponent[] inputs = new JComponent[] {
						new JLabel("Set parameters for the gaussian filter, this filter is used to blur images during template matching."),
				        new JLabel("Template Matching Option"),
				        methodList
				};
				int reply = JOptionPane.showConfirmDialog(null,inputs, "Gaussian Filter",JOptionPane.PLAIN_MESSAGE);
				if (reply == JOptionPane.OK_OPTION) {
					int method = methodList.getSelectedIndex();
					DCMenuBar.this.controller.setTMMethod(method);
					App.prop.setProperty(PROP_TM_METHOD, templateMatchingMethod.toString());
				}
			}
		});
		
		numThreadMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextField numThread = new JTextField(maxThreads.toString());
				final JComponent[] inputs = new JComponent[] {
						new JLabel("Set maximum number of threads created for parallelisable processes."),
				        new JLabel("Maximum thread number"),
				        numThread
				};
				int reply = JOptionPane.showConfirmDialog(null,inputs, "Maximum Threads",JOptionPane.PLAIN_MESSAGE);
				if (reply == JOptionPane.OK_OPTION) {
					int number;
					try {
						number = Integer.parseInt(numThread.getText());
					} catch (NumberFormatException exp) {
						String text = "Invalid input: " + numThread.getText() + "\n"
								+ "Number of threads must be a positive integer!\n";
						JOptionPane.showMessageDialog(DCMenuBar.this.rootPane, text);
						return;
					}
					DCMenuBar.this.controller.setMaxWorkerThread(number);
					App.prop.setProperty(PROP_MAX_THREAD, maxThreads.toString());
				}
			}
		});
		
		maxDegreeMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextField maxDegreeField = new JTextField(maxDegree.toString());
				final JComponent[] inputs = new JComponent[] {
						new JLabel("Set maximum allowed degree for polynomial fitting."),
				        new JLabel("Maximum fitting degree"),
				        maxDegreeField
				};
				int reply = JOptionPane.showConfirmDialog(null,inputs, "Maximum Fitting Degree",JOptionPane.PLAIN_MESSAGE);
				if (reply == JOptionPane.OK_OPTION) {
					int degree;
					try {
						degree = Integer.parseInt(maxDegreeField.getText());
					} catch (NumberFormatException exp) {
						String text = "Invalid input: " + maxDegreeField.getText() + "\n"
								+ "Fitting degree must be a positive integer!\n";
						JOptionPane.showMessageDialog(DCMenuBar.this.rootPane, text);
						return;
					}
					DCMenuBar.this.controller.setMaxFittingDegree(degree);
					App.prop.setProperty(PROP_MAX_DEGREE, maxDegree.toString());
				}
			}
		});
		
		resetAllToDefault.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DCMenuBar.this.controller.resetParameter();
			}
		});
	}

	protected void setDefaultParameters(AtomicInteger gaussianKernel2, AtomicInteger gaussianInteration2, AtomicInteger templateMatchingMethod2,
			AtomicInteger maxThreads2, AtomicInteger maxDegree2) {
		this.gaussianKernel = gaussianKernel2;
		this.gaussianInteration = gaussianInteration2;
		this.templateMatchingMethod = templateMatchingMethod2;
		this.maxThreads = maxThreads2;
		this.maxDegree = maxDegree2;
		// use controller method to try to update setting from prev session,
		// if succeed, parameter value will sync with the value in prop
		// if fails, value in prop is ignored, and will become sync when user
		// changes the setting
		if (App.prop.getProperty(PROP_GAUSSIAN_KERNEL) != null && 
				App.prop.getProperty(PROP_GAUSSIAN_ITERATION) != null) {
			int kernel = Integer.parseInt(App.prop.getProperty(PROP_GAUSSIAN_KERNEL));
			int iteration = Integer.parseInt(App.prop.getProperty(PROP_GAUSSIAN_ITERATION));
			controller.setGaussianKernel(kernel, iteration);
		}
		if (App.prop.getProperty(PROP_TM_METHOD) != null) {
			int method = Integer.parseInt(App.prop.getProperty(PROP_TM_METHOD));
			controller.setTMMethod(method);
		}
		if (App.prop.getProperty(PROP_MAX_THREAD) != null) {
			int thread = Integer.parseInt(App.prop.getProperty(PROP_MAX_THREAD));
			controller.setMaxWorkerThread(thread);
		}
		if (App.prop.getProperty(PROP_MAX_DEGREE) != null) {
			int degree = Integer.parseInt(App.prop.getProperty(PROP_MAX_DEGREE));
			controller.setMaxFittingDegree(degree);
		}
	}
	
	private void setEditingEnabled(boolean flag) {
		clearAllItem.setEnabled(flag);
		gaussianMenu.setEnabled(flag);
		templateMatchingMenu.setEnabled(flag);
		numThreadMenu.setEnabled(flag);
		maxDegreeMenu.setEnabled(flag);
		resetAllToDefault.setEnabled(flag);
	}
	
	protected void setRunningFlagModel(BooleanModel model) {
		model.addPropertyChangeListener(new RunningFlagChangeListener());
	}
	
	private class RunningFlagChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			boolean flag = (boolean) evt.getNewValue();
			DCMenuBar.this.setEditingEnabled(!flag);
		}

	}
	
}
