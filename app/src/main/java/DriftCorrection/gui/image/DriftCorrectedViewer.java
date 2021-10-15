package DriftCorrection.gui.image;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import DriftCorrection.gui.Synchroniser;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class DriftCorrectedViewer extends JPanel implements ChangeListener {
	private static final Logger logger = Logger.getLogger(DriftCorrectedViewer.class.getName());

	protected static int NUM_FRAME;
	private int frameNumber = 0;
	private List<String> imgList;
	private Slider mySlider;
	//This label uses ImageIcon to show the frames.
	private ImagePanel imagePanel;
	private Synchroniser sync;
	//	    BufferedImage image;

	public DriftCorrectedViewer(Synchroniser sync) {
		this.sync = sync;

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));


		//Create the label that displays the animation.
		imagePanel = new ImagePanel(); 
		imagePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Point panelPoint = e.getPoint();
				Point piexelLocation = imagePanel.getPixelLocation(panelPoint);
				imagePanel.setPoint(piexelLocation);
				//	                System.out.println("You clicked at " + panelPoint + " which is relative to the image " + imgContext);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				Point panelPoint = e.getPoint();
				Point piexelLocation = imagePanel.getPixelLocation(panelPoint);
				imagePanel.setSecPoint(piexelLocation);
				//	                System.out.println("You clicked at " + panelPoint + " which is relative to the image " + imgContext);
			}
		});

		imagePanel.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				Point panelPoint = e.getPoint();
				Point piexelLocation = imagePanel.getPixelLocation(panelPoint);

				imagePanel.setSecPoint(piexelLocation);
			}
		});

		//	        updatePicture(0); //display first frame

		//Put everything together.
		JScrollPane scrollPane = new JScrollPane(imagePanel);
		add(scrollPane);

		mySlider = new Slider();
		mySlider.addChangeListener(this);
		add(mySlider);

		setBorder(BorderFactory.createEmptyBorder(10,10,10,10));  
	}

	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		imagePanel.setFileHandler(fh);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
		if (!source.getValueIsAdjusting()) {
			frameNumber = (int)source.getValue();
			logger.info("updating to image: " + frameNumber);
			updatePicture(frameNumber);
			sync.rawImageChanged(frameNumber);
		}
	}

	public void setImageList(List<String> list) {
		this.imgList = list;
		NUM_FRAME = list.size();
		mySlider.setMaximum(NUM_FRAME);
	}



	/** Update the label to display the image for the current frame. */
	protected void updatePicture(int frameNumber) {
		if (!imagePanel.updateImage(imgList.get(frameNumber)));
		// TODO give feedback for bad image
	}

	public int[] getROI() {
		return imagePanel.getROI();
	}

	public int getFrameIndex() {
		return frameNumber;
	}


}
