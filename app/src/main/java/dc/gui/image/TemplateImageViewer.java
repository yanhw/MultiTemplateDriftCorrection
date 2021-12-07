package dc.gui.image;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public class TemplateImageViewer extends JPanel {

	/**
	 * Create the panel.
	 */
	public TemplateImageViewer() {
		setLayout(new BorderLayout(0, 0));
		
		ImagePanel imagePanel = new ImagePanel();
		add(imagePanel);
		
		Slider slider = new Slider();
		add(slider, BorderLayout.SOUTH);

	}

}
