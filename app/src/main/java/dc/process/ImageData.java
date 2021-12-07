package dc.process;

import org.bytedeco.opencv.opencv_core.Mat;

// wrapper class for input and output variable of a process step
public class ImageData {
	private String inputString;
	private Mat image;

	public ImageData(String inputString){
		this.inputString = inputString;
	}	
	
	public ImageData(Mat image){
		this.image = image;
	}

	public String getString(){
		return inputString;
	}
	
	public void setString(String string) {
		inputString = string;
	}
	
	public void setImage(Mat image) {
		this.image = image;
	}
	
	public Mat getImage(){
		return image;
	}

}
