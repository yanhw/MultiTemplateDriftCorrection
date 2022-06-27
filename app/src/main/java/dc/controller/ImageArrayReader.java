package dc.controller;


import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.imageio.ImageIO;


public class ImageArrayReader{
	private String name = "image reader";
	private static final Logger logger = Logger.getLogger(ImageArrayReader.class.getName());
	private String format;
	
	public ImageArrayReader(String format) {
		this.format = format;
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public String getName() {
		return name + ": " + format;
	}
	
	public double[][] read(String filename) {
		if (filename == null) {
			logger.severe("iamge address not found");
			return null;
		}
		BufferedImage img = null;
		Raster raster = null;
		try {
			img = ImageIO.read(new File(filename));
			raster = img.getData();
		} catch(IOException e) {
			logger.severe("failed to open image:" + filename);
		}
		int cols = img.getWidth();
		int rows = img.getHeight();
		double[][] image = new double[rows][cols];
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
//				image[r][c] = img.getRGB(c, r);
				image[r][c] = raster.getSample(c, r, 0);
			}
		}
		return image;
	}


	public String getInputType() {
		return format;
	}
}
