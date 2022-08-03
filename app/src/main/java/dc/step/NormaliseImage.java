package dc.step;

import java.util.logging.*;

import org.bytedeco.opencv.opencv_core.Mat;
//import static org.bytedeco.opencv.global.opencv_core.NORM_MINMAX;
//import static org.bytedeco.opencv.global.opencv_core.CV_32F;
//import static org.bytedeco.opencv.global.opencv_core.normalize;

public class NormaliseImage extends SimpleProcessStep {
	private static final String name = "normalise";
	private static final Logger logger = Logger.getLogger(NormaliseImage.class.getName());
	
	private double min = 0;
	private double max = 255;
	
	public NormaliseImage() {
		super();
	}
	
	
	public NormaliseImage(double min, double max) {
		super();
		this.min = min;
		this.max = max;
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
//		normalize(inputImage, inputImage, min, max, NORM_MINMAX, CV_32F);
//		int numRow = inputImage.length;
//		int numCol = inputImage[0].length;
//		double lowLimit = 999999.9, highLimit = -999999.9, diff, range;
//		for (int r = 0; r < numRow; r++) {
//			for (int c = 0; c < numCol; c++) {
//				if (inputImage[r][c] > highLimit) {
//					highLimit = inputImage[r][c];
//				} else if (inputImage[r][c] < lowLimit) {
//					lowLimit = inputImage[r][c];
//				}
//			}
//		}
//		diff = highLimit - lowLimit;
//		range = max-min;
//		if (diff != 0) {
//			for (int r = 0; r < numRow; r++) {
//				for (int c = 0; c < numCol; c++) {
//					inputImage[r][c] = range*(inputImage[r][c]-lowLimit)/diff;
//				}
//			}
//		} else {
//			for (int r = 0; r < numRow; r++) {
//				for (int c = 0; c < numCol; c++) {
//					inputImage[r][c] = min;
//				}
//			}
//		}
		return myImage;
	}	//end of run()
	
	public String getName() {
		return name + " min: " + min + " max: " + max;
	}


	@Override
	public ProcessStep copy() {
		return new NormaliseImage(min, max);
	}
}
