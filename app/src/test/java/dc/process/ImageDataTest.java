package dc.process;

import static org.junit.Assert.*;

import org.junit.Test;

import dc.controller.ImageData;


public class ImageDataTest {

	@Test
	public void testSetString() {
		String filename = "randomtext";
		ImageData data = new ImageData(filename);
		assertNull("there should be no image", data.getImage());
	}

}
