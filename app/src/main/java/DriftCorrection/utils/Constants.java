package DriftCorrection.utils;

public final class Constants {
	public static final String CBIS_ROOT_DIR = "/gpfs0/scratch/utkur/";
	public static final String CBIS_HOME_DIR = "/gpfs0/home/";
	public static final String[] INPUT_FORMAT = {"dm3", "dm4", "png"};
	public static final String VERSION = "0.1.0";
	public static final String VERSION_CHECK_FILE = "/gpfs0/home/hongwei/codes/DriftCorrection/curr_version.txt";
	public static final int MAX_WORKER = 5;	// maximum number of worker to create. Placed a limit here to avoid exahusting server resource
}
