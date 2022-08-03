package dc.step;

import java.util.logging.*;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.indexer.FloatRawIndexer;
//import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;
import static org.bytedeco.opencv.global.opencv_imgproc.GaussianBlur;

public class GaussianImage extends SimpleProcessStep {
	private static final String name = "gaussian";
	private static final Logger logger = Logger.getLogger(GaussianImage.class.getName());
	
	private int kernel = 5;
	private int iteration = 3;
	
	public GaussianImage() {
		super();
	}
	
	
	public GaussianImage(int kernel, int iteration) {
		super();
		this.kernel = kernel;
		this.iteration = iteration;
	}
	
	@Override
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
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
		Mat out = new Mat();
		gaussian(inputImage, out, kernel, iteration);
		
		return myImage;
	}	//end of run()
	
	public void gaussian(Mat input, Mat output, int kernel, int iteration) {
		for (int i = 0; i < iteration; i++) {
			GaussianBlur(input, output, new Size(kernel, kernel), 0);
			input = output;
		}
		
	}
	
	public String getName() {
		return name + " kernel: " + kernel + " iteration: " + iteration;
	}


	@Override
	public ProcessStep copy() {
		return new GaussianImage(kernel, iteration);
	}

	// 
	public void gaussian(double[][] image, int i, int j) {
		int tRow = image.length;
		int tCol = image[0].length;
		Mat input = new Mat(tRow, tCol, CV_32F);
		FloatIndexer indexer = input.createIndexer();
		for (int r = 0; r < tRow; r++) {
			for (int c = 0; c < tCol; c++) {
				indexer.put(r, c,  (float) image[r][c]);
			}
		}
		Mat output = new Mat(tRow, tCol, CV_32F);
		gaussian(input, output, kernel, 1);
		FloatRawIndexer sI = output.createIndexer();
		for (int r = 0; r < tRow; r++) {
			for (int c = 0; c < tCol; c++) {
				image[r][c] = sI.get(r,c);
			}
		}
	}
}
