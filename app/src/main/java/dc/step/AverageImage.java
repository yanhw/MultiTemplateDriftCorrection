package dc.step;

import java.util.logging.*;

import org.bytedeco.opencv.opencv_core.Mat;

public class AverageImage implements ProcessStep {
	private static final String name = "averaging: ";
	private static final Logger logger = Logger.getLogger(AverageImage.class.getName());
	
	//~ private double[][][] imageStack = null;
	private Mat sumImage = null;
//	private ImageData myData;
	private int count = 0;
	private int step = 10;
	//~ private int init_count = 0;
	
	
	public AverageImage(int step) {
		if (step <= 0) {
			logger.severe("invalid step to average: " + String.valueOf(step));
		}
		this.step = step;
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public String getName() {
		return name+String.valueOf(step);
	}
	
	public void initialise() {
		count = 0;
		resetSumImage();
		//~ init_count = 0;
	}
	
	public ImageData run(ImageData myImage) {
		// validate input data
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
//		// initialise necessary variables for the first input
//		if (count == 0) {
//			sumImage = new double[numRow][numCol];
//			resetSumImage();
//		}
//		for (int r = 0; r < numRow; r++) {
//			for (int c = 0; c < numCol; c++) {
//				sumImage[r][c] += inputImage[r][c];
//			}
//		}
		
		updateCount();
		if (count == 0) {
			return new ImageData(sumImage);
		}
		return new ImageData("null");
	}	//end of run()
	
	private void updateCount(){
		count += 1;
		if (count == step) {
			count = 0;
		}
	}
	
	private void resetSumImage() {
//		if (sumImage == null) {
//			logger.warning("resetting sumImage without initiation");
//		} else {
//			int numRow = sumImage.length;
//			int numCol = sumImage[0].length;
//			for (int r = 0; r < numRow; r++) {
//				for (int c = 0; c < numCol; c++) {
//					sumImage[r][c] += 0;
//				}
//			}
//		}	// end of if
	}	// end of resetSumImage

	@Override
	public ProcessStep copy() {
		return new AverageImage(step);
	}
}
