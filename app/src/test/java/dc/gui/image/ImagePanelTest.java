package dc.gui.image;

import static org.junit.Assert.*;

import java.awt.Dimension;

import org.junit.Before;
import org.junit.Test;

//TODO not fully implemented
public class ImagePanelTest {
	private ImagePanel myPanel;
	private ROIModel myROI;
	
	private static final String filename = "src/test/resources/test_image/rodImage.png";
	private static final int imgHeight = 687;
	private static final int imgWidth = 645;
	
	@Before
	public void init() {
		myPanel = new ImagePanel();
		myROI = myPanel.getROI();
	}
	
	@Test
	public void testSetImage() {
		myPanel.updateImage(filename);
		Dimension preferredDimension = myPanel.getPreferredSize();
		assertEquals("preferred height", imgHeight, preferredDimension.height);
		assertEquals("preferred width", imgWidth, preferredDimension.width);
	}

	@Test
	public void testSetROI() {
		myROI.getFlag();
	}
}
