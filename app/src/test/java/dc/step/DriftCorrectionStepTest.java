package dc.step;

import static dc.utils.ImageProcessing.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bytedeco.javacpp.indexer.FloatRawIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.Before;
import org.junit.Test;

import dc.controller.ImageArrayReader;

public class DriftCorrectionStepTest {
	private DriftCorrectionStep myDriftCorrection;
	private ImageReader myReader;
	private ImageArrayReader reader;
	
	@Before
	public void init() {
		myDriftCorrection = new DriftCorrectionStep();
		myReader = new ImageReader("png");
		reader = new ImageArrayReader("png");
	}
	
	@Test
	public void test2RodImage() {
		String testFileName = "src/test/resources/test_image/rodImage.png";
		String expectedResultFileName = "src/test/resources/test_image/rodImage_pad_10_20_30_40.png";
		String expectedResultFileName2 = "src/test/resources/test_image/rodImage_pad_15_15_25_45.png";
		String outputFileName = "src/test/resources/test_image/rodImageTestOutput.png";
		List<Integer> rowLoc = new LinkedList<Integer>();
		List<Integer> colLoc = new LinkedList<Integer>();
		int padTop = 10;
		int padBottom = 20;
		int padLeft = 30;
		int padRight = 40;
		rowLoc.add(0);
		colLoc.add(0);
		rowLoc.add(-5);
		colLoc.add(5);
		int[] ROI = {0,687,0,645};
		
		myDriftCorrection.initialise(rowLoc, colLoc, padTop, padBottom, padLeft, padRight, ROI);
		// first image
		ImageData data = new ImageData(testFileName);
		myReader.run(data);
		myDriftCorrection.run(data);
		Mat resMat = data.getImage();
		assertNotNull("image should not be null", resMat);
		int numRow = resMat.arrayHeight();
		int numCol = resMat.arrayWidth();
		double[][] res = new double[numRow][numCol];
		FloatRawIndexer sI = resMat.createIndexer();
		for (int r = 0; r < numRow; r++) {
			for (int c = 0; c < numCol; c++) {
				res[r][c] = sI.get(r,c);
			}
		}
		double[][] result = reader.read(expectedResultFileName);
		assertEquals("output height", result.length, numRow);
		assertEquals("output width", result[0].length, numCol);
		if (!Arrays.deepEquals(result, res)) {
			saveImage(res, outputFileName);
			fail("output mismatch with expected image, see output file");
		}
		
		// second image
		data = new ImageData(testFileName);
		myReader.run(data);
		myDriftCorrection.run(data);
		resMat = data.getImage();
		assertNotNull("image should not be null", resMat);
		numRow = resMat.arrayHeight();
		numCol = resMat.arrayWidth();
		res = new double[numRow][numCol];
		sI = resMat.createIndexer();
		for (int r = 0; r < numRow; r++) {
			for (int c = 0; c < numCol; c++) {
				res[r][c] = sI.get(r,c);
			}
		}
		result = reader.read(expectedResultFileName2);
		assertEquals("output height", result.length, numRow);
		assertEquals("output width", result[0].length, numCol);
		if (!Arrays.deepEquals(result, res)) {
			saveImage(res, outputFileName);
			fail("output mismatch with expected image, see output file");
		}
	}

}
