package dc.utils;

import static org.bytedeco.opencv.global.opencv_imgproc.TM_CCOEFF;

public final class Constants {
	public static final String CBIS_ROOT_DIR = "/gpfs0/scratch/utkur/";
	public static final String CBIS_HOME_DIR = "/gpfs0/home/";
	public static final String[] INPUT_FORMAT = {"dm3", "dm4", "png"};
	public static final String VERSION = "0.2.0";
	public static final String VERSION_CHECK_FILE = "/gpfs0/home/hongwei/codes/DriftCorrection/curr_version.txt";
	
	public static final int MAX_WORKER = 5;	// maximum number of worker to create. Placed a limit here to avoid exahusting server resource
	public static final boolean AUTO_VOERWRITE = true;
	public static final int DEFAULT_TM_METHOD = TM_CCOEFF;
	public static final int DEFAULT_GAUSSIAN_KERNEL = 5;
	public static final int DEFAULT_GAUSSIAN_ITERATION = 3;
	public static final int MAX_FITTING_DEGREE = 25;
}
