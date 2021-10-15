package DriftCorrection.utils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageProcessing{
	public static void saveImage(double[][] inputImage, String filename) {
		int numRow = inputImage.length;
		int numCol = inputImage[0].length;
		int[] editedImage = new int[numRow*numCol];
		int temp;
		for (int r = 0; r < numRow; r++) {
			for (int c = 0; c < numCol; c++) {
				temp = (int)inputImage[r][c];
//				System.out.print(temp);
				// ensures no over/under flow of value
				if (temp < 0) {
					editedImage[r*numCol+c] = 0;
				} else if (temp > 255) {
					editedImage[r*numCol+c] = 255;
				} else {
					editedImage[r*numCol+c] = temp;
				}
			}
		}
		BufferedImage image = new BufferedImage(numCol, numRow, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = image.getRaster();
		raster.setSamples(0, 0, numCol, numRow, 0, editedImage);
		try {
			ImageIO.write(image, "png", new File(filename));
		} catch (IOException e) {
//			logger.severe("failed to save image");
		}
	}
}