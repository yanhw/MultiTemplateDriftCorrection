package dc.step;

import java.util.List;
import java.util.logging.*;

import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

import dc.controller.ImageData;
import dc.utils.FileSystem;

public class SaveImage extends OutputProcessStep {
	
	private static final String name = "save image";
	private static final Logger logger = Logger.getLogger(SaveImage.class.getName());
	
	// TODO: check for overwrite
//	private boolean checkOverwrite = false;		// whether to give warning
	private boolean expectNull = false;			// this is true if there is averaging step
	private String saveDir = null;				// saving location
	private List<String> saveFiles;
	private int count = 0;						// counter for file name
	private String padString = "000000";
	
	@Override
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	@Override
	public ImageData run(ImageData myImage) {
		logger.fine(getName());
		if (myImage == null) {
			if (!expectNull) {
				logger.severe("image data object is not created");
			}
			return null;
		}
		Mat inputImage = myImage.getImage();
		if (inputImage == null) {
			logger.severe("image is not found in ImageData");
			return myImage;
		}
		if (saveDir == null) {
			logger.severe("saveDir is not initialised");
			return myImage;
		}
		
//		BufferedImage image = new BufferedImage(numCol, numRow, BufferedImage.TYPE_BYTE_GRAY);
//		WritableRaster raster = image.getRaster();
//		raster.setSamples(0, 0, numCol, numRow, 0, editedImage);
		String filename;
		if (saveFiles == null) {
			String filenum = String.valueOf(count);
			filename = (padString + filenum).substring(filenum.length()) + ".png";
			filename = FileSystem.joinPath(saveDir, filename);
		} else {
			filename = saveFiles.get(count);
		}
		imwrite(filename, inputImage);
//			ImageIO.write(image, "png", new File(filename));
		count++;
		return myImage;
	}
	
	@Override
	public void initialise(String folder, int startingIdx) {
		if (folder == null) {
			logger.warning("input folder is null");
			return;
		}
		this.saveDir = folder;
		this.count = startingIdx;
		this.saveFiles = null;
		logger.fine("initialised saveDir at: " + saveDir);
	}
	
	public void initialise(List<String> saveFiles) {
		if (saveFiles == null) {
			logger.warning("saveFiles is null");
		}
		this.saveFiles = saveFiles;
		this.count = 0;
	}
	
	public String getName() {
		return name + " saving to: " + saveDir;
	}
	
	@Override
	public ProcessStep copy() {
		return new SaveImage();
	}
}
