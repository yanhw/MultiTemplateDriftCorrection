package dc.step;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.bytedeco.opencv.opencv_core.Mat;

import dc.controller.ImageData;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_core.CV_32F;

public class ImageReader implements ProcessStep{
	private String name = "image reader";
	private static final Logger logger = Logger.getLogger(ImageReader.class.getName());
	private String format;
	
	public ImageReader(String format) {
		this.format = format;
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	@Override
	public String getName() {
		return name + ": " + format;
	}
	
	@Override
	public void initialise() {
		// TODO Auto-generated method stub
		
	}

	//
	@Override
	public ImageData run(ImageData input) {
		if (input == null) {
			logger.severe("input is null");
			return input;
		}
		String filename = input.getString();
//		logger.info(filename);
		if (filename == null) {
			logger.severe("iamge address not found");
			return input;
		}
		Mat image = imread(filename,0); //CV_LOAD_IMAGE_GRAYSCALE = 0
		image.convertTo(image, CV_32F);

		input.setImage(image);
		return input;
	}

	@Override
	public ProcessStep copy() {
		return new ImageReader(format);
	}
	
	@Override
	public String getInputType() {
		return format;
	}
}
