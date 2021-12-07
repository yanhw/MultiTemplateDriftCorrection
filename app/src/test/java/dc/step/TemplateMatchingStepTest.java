package dc.step;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import dc.model.ImageArrayReader;
import dc.process.ImageData;
import dc.step.ImageReader;
import dc.step.TemplateMatching;

public class TemplateMatchingStepTest {
	private TemplateMatching myTemplateMatcher;
	private ImageReader myImageReader;
	
	@Before
	public void init() {
		myTemplateMatcher = new TemplateMatching();
		myImageReader = new ImageReader("png");
	}
	
	@Test
	public void testNormalImage() {
		int top = 361;
		int height = 40;
		int left = 73;
		int width = 80;
		String filename = "Y:/hongwei/codes/image_tool/data/test image/000001.png";
		ImageData data = new ImageData(filename);
		myImageReader.run(data);
		ImageArrayReader reader = new ImageArrayReader("png");
		double[][] raw = reader.read(filename);
		
		double[][] template = new double[height][width];
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				template[r][c] = raw[r+top][c+left];
			}
		}
		myTemplateMatcher.initialise(template);
		myTemplateMatcher.run(data);
		List<Integer> rowResult = myTemplateMatcher.getRowDrift();
		List<Integer> colResult = myTemplateMatcher.getColDrift();
		assertEquals("row error in test image 000001.png", top, (int)rowResult.get(0));
		assertEquals("col error in test image 000001.png", left, (int)colResult.get(0));
	}

}
