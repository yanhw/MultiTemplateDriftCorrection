package dc.gui.image;

import javax.swing.JPanel;

import dc.gui_old.Synchroniser;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.border.TitledBorder;

public class RawImageViewer extends JPanel {
	
	private Synchroniser sync;
	
	/**
	 * Create the panel.
	 */
	public RawImageViewer(Synchroniser sync) {
		setBorder(new TitledBorder(null, "Input Image Sequence", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.SOUTH);
		
		Slider slider = new Slider();
		splitPane.setRightComponent(slider);
		
		ZoomSlider zoomSlider = new ZoomSlider();
		splitPane.setLeftComponent(zoomSlider);
		
		ImagePanel imagePanel = new ImagePanel();
		add(imagePanel, BorderLayout.CENTER);

	}

}
