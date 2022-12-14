package dc.utils;

import static org.bytedeco.opencv.global.opencv_imgproc.TM_CCOEFF;

public final class Constants {
	public static final String CBIS_ROOT_DIR = "/oceanstor/scratch/utkur/hongwei/";
	public static final String CBIS_HOME_DIR = "/gpfs0/home/";
	public static final String[] INPUT_FORMAT = {"png", "jpeg", "jpg", "bmp"};
	public static final String VERSION = "1.0.0";
	public static final String VERSION_CHECK_FILE = "/gpfs0/home/hongwei/codes/DriftCorrection/curr_version.txt";
	
	public static final int MAX_WORKER = 5;	// maximum number of worker to create. Placed a limit here to avoid exahusting server resource
	public static final int DEFAULT_TM_METHOD = TM_CCOEFF;
	public static final String[] TM_METHOD_LIST = {"Squared Difference", "Normaised Squared Difference", 
			"Cross Corelation", "Normalised Cross Corelation", "Cross Coefficient", "Normaised Cross Coefficient"};
	public static final int DEFAULT_GAUSSIAN_KERNEL = 5;
	public static final int DEFAULT_GAUSSIAN_ITERATION = 3;
	public static final int MAX_FITTING_DEGREE = 25;
	public static final String URI = "https://github.com/yanhw/MultiTemplateDriftCorrection/wiki";
}
