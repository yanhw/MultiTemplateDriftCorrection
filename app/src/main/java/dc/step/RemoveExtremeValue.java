package dc.step;

//import java.util.Arrays;
import java.util.logging.*;

import org.bytedeco.opencv.opencv_core.Mat;

import dc.controller.ImageData;

public class RemoveExtremeValue extends SimpleProcessStep {
	private static final String name = "remove extremas";
	private static final Logger logger = Logger.getLogger(RemoveExtremeValue.class.getName());
	
	private double lowRange = 0.0001;
	private double highRange = 0.9999;
	
	public RemoveExtremeValue() {
		super();
	}
	
	/* Assumption: 0 <= lowRange < highRange <= 1 */
	public RemoveExtremeValue(double lowRange, double highRange) {
		super();
		this.lowRange = lowRange;
		this.highRange = highRange;
	}

	public ImageData run(ImageData myImage) {
		if (myImage == null) {
			logger.severe("image data object is not created");
			return null;
		}
		Mat inputImage = myImage.getImage();
		if (inputImage == null) {
			logger.severe("image is not found in ImageData");
			return myImage;
		}
//		int numRow = inputImage.length;
//		int numCol = inputImage[0].length;
//		double[] arrayToSort = new double[numRow*numCol];
//		double lowLimit, highLimit;
//		for (int r = 0; r < numRow; r++) {
//			for (int c = 0; c < numCol; c++) {
//				arrayToSort[r*numCol+c] = inputImage[r][c];
//			}
//		}
//		Arrays.sort(arrayToSort);
//		lowLimit = arrayToSort[(int)(arrayToSort.length*lowRange)];
//		highLimit = arrayToSort[(int)(arrayToSort.length*highRange)];
//		if (highLimit <= lowLimit) {
//			logger.warning("bad image");
//		}
//		for (int r = 0; r < numRow; r++) {
//			for (int c = 0; c < numCol; c++) {
//				if (inputImage[r][c] >= highLimit) {
//					inputImage[r][c] = highLimit;
//				}
//				else if (inputImage[r][c] <= lowLimit) {
//					inputImage[r][c] = lowLimit;
//				}
//			}
//		}
		//~ System.out.println((int)(arrayToSort.length*this.lowRange));
		//~ System.out.println((int)(arrayToSort.length*this.highRange));
		//~ System.out.println(lowLimit);
		//~ System.out.println(highLimit);
		return myImage;
	}
	
	public String getName() {
		return name + " low range: " + lowRange + " high range: " + highRange;
	}

	@Override
	public ProcessStep copy() {
		return new RemoveExtremeValue(lowRange, highRange);
	}
}
