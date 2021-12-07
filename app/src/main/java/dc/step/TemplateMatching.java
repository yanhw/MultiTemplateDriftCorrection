package dc.step;

import java.util.ArrayList;
import org.bytedeco.javacpp.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Size;

import dc.process.ImageData;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import static org.bytedeco.opencv.global.opencv_imgproc.TM_CCOEFF;
import static org.bytedeco.opencv.global.opencv_imgproc.matchTemplate;
import static org.bytedeco.opencv.global.opencv_core.minMaxLoc;
import static org.bytedeco.opencv.global.opencv_core.CV_32F;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class TemplateMatching extends SimpleProcessStep {
	
	private static final String name = "template matching";
	private static final Logger logger = Logger.getLogger(TemplateMatching.class.getName());
	
	private Mat template;
	private List<Integer> rowLoc;
	private List<Integer> colLoc;
	
	public TemplateMatching(double[][] template) {
		if (template == null) {
			logger.severe("initialised with null template");
		}
		setTemplate(template);
	}
	
	public TemplateMatching() {
		
	}
	
	public TemplateMatching(Mat template2) {
		this.template = template2;
	}
	
	@Override
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	private void setTemplate(double[][] template) {
		int tRow = template.length;
		int tCol = template[0].length;
		this.template = new Mat(tRow, tCol, CV_32F);
		FloatIndexer indexer = this.template.createIndexer();
		for (int r = 0; r < tRow; r++) {
			for (int c = 0; c < tCol; c++) {
				indexer.put(r, c,  (float) template[r][c]);
			}
		}
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public ImageData run(ImageData input) {
		// validate input data
		if (input == null) {
			logger.severe("image data object is not created");
			return null;
		}

		Mat inputImage = input.getImage();
		Size size = new Size(inputImage.cols()-template.cols()+1, inputImage.rows()-template.rows()+1);
		Mat result = new Mat(size, CV_32F);
		matchTemplate(inputImage, template, result, TM_CCOEFF);

		DoublePointer minVal= new DoublePointer();
		DoublePointer maxVal= new DoublePointer();
		Point min = new Point();
		Point max = new Point();
		minMaxLoc(result, minVal, maxVal, min, max, null);
		rowLoc.add(max.y());
		colLoc.add(max.x());
		
		logger.info("proc frame: " + rowLoc.size() + ", drift:" + max.x() + " " + max.y());
		// for debugging
//		NormaliseImage normaliser = new NormaliseImage();
//		SaveImage saver = new SaveImage();
//		saver.initialise("Z:/hongwei/converter_test", 0);
//		ImageData data = new ImageData(imageNormed);
//		data = normaliser.run(data);
//		saver.run(data);
//		data.setImage(templateNormed);
//		data = normaliser.run(data);
//		saver.run(data);
//		data.setImage(res);
//		data = normaliser.run(data);
//		saver.run(data);
		return input;
	}
	

	public void initialise(double[][] template) {
		setTemplate(template);
		initialise();
	}
	
	@Override
	public void initialise() {
		
		rowLoc = new ArrayList<Integer>();
		colLoc = new ArrayList<Integer>();
	}
	
	public List<Integer> getRowDrift() {
		return rowLoc;
	}
	
	public List<Integer> getColDrift() {
		return colLoc;
	}
	
	@Override
	public ProcessStep copy() {
		return new TemplateMatching(template);
	}

}
