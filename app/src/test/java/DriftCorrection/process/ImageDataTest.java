package DriftCorrection.process;

import static org.junit.Assert.*;

import org.junit.Test;


public class ImageDataTest {

	@Test
	public void testSetString() {
		String filename = "randomtext";
		ImageData data = new ImageData(filename);
		assertNull("there should be no image", data.getImage());
	}

}
