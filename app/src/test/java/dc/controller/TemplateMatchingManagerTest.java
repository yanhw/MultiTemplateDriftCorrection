package dc.controller;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import dc.model.TemplateMatchingSegmentModel;

public class TemplateMatchingManagerTest {
	private TemplateMatchingManager myManager;
	private TemplateMatchingSegmentModel model;
	
	@Before
	public void init() {
		myManager = new TemplateMatchingManager();
		model = new TemplateMatchingSegmentModel();
		myManager.setTableModel(model);
	}
	
	@Test
	public void testSetROI() {
		String testFileName = "src/test/resources/test_image/rodImage.png";
//		String expectedResultFileName = "src/test/resources/test_image/rodImage_320_430_110_360.png";
//		String outputFileName = "src/test/resources/test_image/rodImageTestOutput.png";
		int[] ROI = {320,569,110,359};
		

//		double[][] result = reader.read(expectedResultFileName);
//		System.out.println(result[0][0]);
		List<Path> fileList = new LinkedList<Path>();
		fileList.add(Paths.get(testFileName));
		fileList.add(Paths.get(testFileName));	// Template matching manager requires at least 2 images
		myManager.init(fileList);
		
		boolean res = myManager.setROI(0, ROI);
		assertTrue("fail to set ROI", res);
		
//		MovieSegment segment = myManager.getSegment(0);
//		double[][] template = segment.getTemplate();
//		assertNotNull("template is null", template);
//		assertEquals("template height", template.length, result.length);
//		assertEquals("template width", template[0].length, result[0].length);
//		if (!Arrays.deepEquals(result, template)) {
//			saveImage(template, outputFileName);
//			fail("template mismatch with expected image, see output file");
//		}
//		
//		int[] resROI = segment.getROI();
//		assertArrayEquals("ROI mismatch", ROI, resROI);
		
	}

}
