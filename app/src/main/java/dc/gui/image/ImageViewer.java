package dc.gui.image;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

public class ImageViewer extends JPanel {

	/**
	 * Create the panel.
	 */
	public ImageViewer() {
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane);
		
		TemplateImageViewer templateImageViewer = new TemplateImageViewer();
		tabbedPane.addTab("Templates", null, templateImageViewer, null);

	}

}
