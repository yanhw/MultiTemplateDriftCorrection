package dc.step;

import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;

import dc.controller.ImageData;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;
import static org.bytedeco.opencv.global.opencv_core.copyMakeBorder;

public class DriftCorrectionStep extends SimpleProcessStep {
	private static final String name = "drift correction";
	private static final Logger logger = Logger.getLogger(TemplateMatching.class.getName());
	
	private static final int TOP = 0;
	private static final int BOTTOM = 1;
	private static final int LEFT = 2;
	private static final int RIGHT = 3;
	
	private List<Integer> rowLoc;
	private List<Integer> colLoc;
	private int count;
	private int padTop;
	private int padBottom;
	private int padLeft;
	private int padRight;
//	private int[] ROI;
	private int height;
	private int width;

	public DriftCorrectionStep() {
		
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public void initialise(List<Integer> rowLoc, List<Integer> colLoc, int padTop, int padBottom, int padLeft, int padRight, int ROI[]) {
//		if (rowLoc == null ||)
		assert (padTop >=0);
		assert (padLeft >=0);
		assert (padRight >=0);
		assert (padBottom >=0);
		
		this.rowLoc = rowLoc;
		this.colLoc = colLoc;
		this.padTop = padTop;
		this.padBottom = padBottom;
		this.padLeft = padLeft;
		this.padRight = padRight;
//		this.ROI = ROI;
		this.height = ROI[BOTTOM]-ROI[TOP]+1+padTop+padBottom;
		this.width = ROI[RIGHT]-ROI[LEFT]+1+padLeft+padRight;
		this.count = 0;

	}

	@Override
	public ImageData run(ImageData input) {
		int idx = count;
		count += 1;
		if (!hasValidImage(input)) {
			logger.severe("invalid input image");
			return input;
		}
		Mat raw = input.getImage();
		int top = padTop-rowLoc.get(idx);
		int left = padLeft-colLoc.get(idx);
		logger.info("top: "+top+" left: "+left+" height: "+height+" width: "+width+" padTop:"+padTop+" padLeft:"+padLeft+" yDrift:"+rowLoc.get(idx)+" xDrift:"+colLoc.get(idx));
		Mat image = new Mat(height, width, CV_32F);;   
	    copyMakeBorder(raw, image, top, padTop-top+padBottom, left, padLeft-left+padRight, 0, Scalar.all(0));
//		image = raw.adjustROI(top, padTop-top, left, padLeft-left);
//		for (int r = 0; r < raw.length; r++) {
//			for (int c = 0; c < raw[0].length; c++) {
//				if (r+top>=0 && c+left>=0 && r+top<height && c+left<width) {
//					image[r+top][c+left] = raw[r][c];
//				}		
//			}
//		}
		input.setImage(image);
		return input;
	}

	@Override
	public ProcessStep copy() {
		return new DriftCorrectionStep();
	}
	
}
