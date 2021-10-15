package DriftCorrection.gui.image;

import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class TemplateImageViewer  extends JPanel{
	private static final Logger logger = Logger.getLogger(TemplateImageViewer.class.getName());
	
    private ImagePanel imagePanel;
    private JLabel textLabel;
    
    public TemplateImageViewer() {
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	this.textLabel = new JLabel("template is not set.");
    	this.imagePanel = new ImagePanel();
    	JScrollPane scrollPane = new JScrollPane(this.imagePanel);
    	add(textLabel);
    	add(scrollPane);
    }
    
    public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		imagePanel.setFileHandler(fh);
	}
    
    public void setImage(int frameNumber, int first, int last, Path path, int[] ROI) {
    	if (ROI == null) {
    		textLabel.setText("no template for movie at frame " 
    		    	+ first + " to " + last);
    	} else {
    		// TODO: do not update if the image is the same
    		imagePanel.updateImage(path.toString());
	    	imagePanel.setROI(ROI[0], ROI[2], ROI[1]-ROI[0], ROI[3]-ROI[2]);
	    	textLabel.setText("template at frame number " + frameNumber + " for movie at frame " 
	    	+ first + " to " + last);
	    	logger.info("displaying " + frameNumber + " " + path.toString() + " "+ ROI);
    	}
    	
    }
	
    public void setImage(int frameNumber) {
    	
    }
    
    public void removeImage(int indexNumber) {
    	
    }
    

}
