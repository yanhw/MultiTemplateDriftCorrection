/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package dc;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import static dc.utils.Constants.CBIS_HOME_DIR;
import static dc.utils.Constants.VERSION;
import static dc.utils.Constants.VERSION_CHECK_FILE;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.*;

import dc.controller.Controller;
import dc.gui.MainFrame;

public class App {
	
	private static final Logger logger = Logger.getLogger(App.class.getName());
	public static final String PROP_FILE = "DriftCorrection.settings";
	public static final Properties prop = new Properties();
	
	public App() {	
		// check environment
		String currDir = System.getProperty("user.dir");
		
		FileHandler fh = null;  
		
		try {  

			// This block configure the logger with handler and formatter  
			fh = new FileHandler(currDir + "/DriftCorrection.log");  
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();  
			fh.setFormatter(formatter);  

			// the following statement is used to log any messages  
			logger.info("---------------New session----------------");  

		} catch (SecurityException e) {  
			e.printStackTrace();  
		} catch (IOException e) {  
			e.printStackTrace();  
		}  
		
		if (currDir.substring(0, 10).equals(CBIS_HOME_DIR.substring(0, 10))) {
			String latestVersion = readLatestVersion();
			if (isOutdated(VERSION, latestVersion)) {
				logger.severe("current version is too old, please get newer version from home/hongwei/codes/DriftCorrection/");
				System.exit(0);
			}
		}
		
		logger.setLevel(Level.FINE);
		logger.info("starting the program...");
		logger.info("running at: " + currDir);
		logger.info("number of processors: " + Runtime.getRuntime().availableProcessors());
		
		setProperties();
		
		// set look and feel
		try {
		    UIManager.setLookAndFeel( new FlatLightLaf() );
		} catch( Exception ex ) {
		    System.err.println( "Failed to initialize LaF" );
		}
		
		Controller controller = new Controller();
		controller.setFileHandler(fh);
		MainFrame mf = new MainFrame();
		mf.initialise(controller, fh);
	}
	
	private void setProperties() {
		try {
			prop.load(new FileInputStream(PROP_FILE));
		} catch (FileNotFoundException e) {
			logger.info("property file not found");
//			e.printStackTrace();
		} catch (IOException e) {
			logger.info("failed to read property file");
//			e.printStackTrace();
		}
	}
	
	private String readLatestVersion() {
		String line = VERSION;
		try(BufferedReader br = new BufferedReader(new FileReader(VERSION_CHECK_FILE))) {
		    line = br.readLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}
	
    private boolean isOutdated(String current, String latest) {
    	StringTokenizer currentTokenizer = new StringTokenizer(current, ".");
    	StringTokenizer latestTokenizer = new StringTokenizer(latest, ".");
    	String curr = currentTokenizer.nextToken();
    	String late = latestTokenizer.nextToken();
    	if (Integer.parseInt(curr) < Integer.parseInt(late)) {
    		return true;
    	}
    	curr = currentTokenizer.nextToken();
    	late = latestTokenizer.nextToken();
    	if (Integer.parseInt(curr) < Integer.parseInt(late)) {
    		return true;
    	}
    	return false;
    }
	
    public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new App();
			}
		});	
		
		
	}
}
