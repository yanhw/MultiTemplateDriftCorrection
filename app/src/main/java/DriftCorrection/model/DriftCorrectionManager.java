package DriftCorrection.model;

import static DriftCorrection.utils.Constants.MAX_WORKER;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import DriftCorrection.utils.FileSystem;

public class DriftCorrectionManager {
	private static final Logger logger = Logger.getLogger(DriftCorrectionManager.class.getName());
	private static final String saveFolderName = "drift_corrected_img";
	private static final String padString = "000000";		// saved image file names are padded to 6 digits
	
	private FileHandler fh;
	private Boolean interruptionFlag;
	
	private List<Path> fileList;
	private List<String> saveFileList;
	private int progress = 0;

	private int[] prevXDrift;
	private int[] prevYDrift;
	private int prevTop;
	private int prevBottom;
	private int prevLeft;
	private int prevRight;
//	private int prevROI;
	
	private List<Integer> changedList;
	
	public DriftCorrectionManager() {
		
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		this.fh = fh;
	}
	
	public void setInterruptionFlag(Boolean flag) {
		interruptionFlag = flag;
	}
	
	protected void init(List<Path> fileList) {
		this.fileList = fileList;
		this.changedList = new LinkedList<Integer>();
		prevXDrift = null;
		prevYDrift = null;
		progress = 0;
	}
	
	
	protected void run(String saveDir, float[] xRawDrift, float[] yRawDrift, int[] ROI) {
		logger.info("DriftCorrection started running...");
		progress = 0;
		// setup saveDir
		String targetFolder = FileSystem.joinPath(saveDir, saveFolderName);
		Path path = Paths.get(targetFolder);
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			logger.severe("failed to create saveDir");
			return;
		}
		
		saveFileList = getSaveFileList(targetFolder, xRawDrift.length);
		
		//  find padding
		int[] xDrift = toInteger(xRawDrift);
		int[] yDrift = toInteger(yRawDrift);
		
		int top = 0, bottom = 0, left = 0, right = 0;
		for (int idx = 0; idx < xDrift.length; idx++) {
			if (yDrift[idx] < -bottom) {
				bottom = -yDrift[idx];
			}
			if (yDrift[idx] > top) {
				top = yDrift[idx];
			}
			if (xDrift[idx] < -right) {
				right = -xDrift[idx];
			}
			if (xDrift[idx] > left) {
				left = xDrift[idx];
			}
		}
		logger.info("padding: " + top + " " + bottom + " " + left + " " + " " + right);	
		
		// organise list for changed files
		checkChangedFrames(xDrift, yDrift, top, bottom, left, right);
		if (changedList.size() == 0) {
			logger.info("no changes from previous setting");
			progress = 100;
			return;
		} else {
			logger.info("number of frames to process:" + changedList.size());
		}
		List<Path> fileSubList = new LinkedList<Path>();
		List<Integer> xSubDrift = new LinkedList<Integer>();
		List<Integer> ySubDrift = new LinkedList<Integer>();
		List<String> saveFileSubList = new LinkedList<String>();
		for (Integer idx: changedList) {
			fileSubList.add(fileList.get(idx));
			xSubDrift.add(xDrift[idx]);
			ySubDrift.add(yDrift[idx]);
			saveFileSubList.add(saveFileList.get(idx));
		}
		
		int numThread = computeThreadSize();
		
		// create process pool
		Process process = new DriftCorrectionProcess();
		process.setFileHandler(fh);
		List<Process> processPool = new ArrayList<Process>(numThread);
		for (int i = 0; i < numThread; i++) {
			processPool.add(process.copy());
		}
		
		
		int total = changedList.size();		
		int numActiveThread = numThread;	
		// split the task for threads
		if (fileSubList.size() < numThread) {
			numActiveThread = fileSubList.size();
		}

		logger.info("number of active thread: " + numActiveThread);
		int[] procStartingIdxList = new int[numThread];
		int[] procEndingIdxList = new int[numThread];
		List<List<Path>> threadFileList = new ArrayList<List<Path>>(numActiveThread);
		List<List<Integer>> xDriftList = new ArrayList<List<Integer>>(numActiveThread);
		List<List<Integer>> yDriftList = new ArrayList<List<Integer>>(numActiveThread);
		List<List<String>> saveFileListList = new ArrayList<List<String>>(numActiveThread);
		for (int j = 0; j < numActiveThread; j++) {
			int start = j*(fileSubList.size()/numActiveThread);
			int end = (j+1)*(fileSubList.size()/numActiveThread);
			if (j == numActiveThread-1) {
				end = fileSubList.size();
			}
			procStartingIdxList[j] = start;
			procEndingIdxList[j] = end;
			threadFileList.add(j, fileSubList.subList(start, end));
			xDriftList.add(xSubDrift.subList(start, end));
			yDriftList.add(ySubDrift.subList(start, end));
			saveFileListList.add(saveFileSubList.subList(start, end));
			logger.info("process " + j + " starts at " + start + ", ends at " + (end-1));
		}

		for (int j = 0; j < numActiveThread; j++) {
			((DriftCorrectionProcess)processPool.get(j)).initDriftCorrection(xDriftList.get(j), yDriftList.get(j), saveFileListList.get(j), top, bottom, left, right, ROI);
			processPool.get(j).initialise(targetFolder, procStartingIdxList[j]);
		}

		// countDownLatch to track progress
		CountDownLatch latch = new CountDownLatch(fileSubList.size());
		logger.info("latch launched");
		// run the conversion
		ExecutorService pool = Executors.newFixedThreadPool(numActiveThread);
		WorkerThread[] workers = new WorkerThread[numActiveThread];

		for (int j = 0; j < numActiveThread; j++) {
			logger.info("starting process " + j);
			workers[j] = new WorkerThread(processPool.get(j), threadFileList.get(j), latch);
			workers[j].setFileHandler(fh);
			pool.execute(workers[j]);
		}
		while (latch.getCount()>0) {
			//System.out.println((100*(fileList.size()-latch.getCount()+done)/total) + " total: "+total+" count: " + latch.getCount()+ " done: " + done);
			progress = (int) (100*(fileSubList.size()-latch.getCount())/total);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// clean up and update attributes
		pool.shutdown();
		prevLeft = left;
		prevRight = right;
		prevTop = top;
		prevBottom = bottom;
		prevXDrift = xDrift;
		prevYDrift = yDrift;
		progress = 100;
		logger.info("drift correction master thread finished");
		return;
	}
	
	private List<String> getSaveFileList(String saveDir, int length) {
		List<String> result = new ArrayList<String>();
		for (int idx = 0; idx < length; idx++) {
			String filenum = String.valueOf(idx);
			String filename = (padString + filenum).substring(filenum.length()) + ".png";
			result.add(FileSystem.joinPath(saveDir, filename));
		}
		return result;
	}

	public int getProgress() {
		return progress;
	}
	
	public List<String> getSaveFiles() {
		return saveFileList;
	}
	
	
	private void checkChangedFrames(int[] xDrift, int[] yDrift, int top, int bottom, int left, int right) {
		changedList.clear();
		if (prevXDrift == null || top != prevTop || bottom != prevBottom ||
				left != prevLeft || right != prevRight) {
			for (int idx = 0; idx < xDrift.length; idx++) {
				changedList.add(idx);
			}
		} else {
			for (int idx = 0; idx < xDrift.length; idx++) {
				if ((prevXDrift[idx] != xDrift[idx]) || 
						(prevYDrift[idx] != yDrift[idx])) {
					changedList.add(idx);
				}
			}
		}
	}
	
	
	private int[] toInteger(float[] input) {
		int[] output = new int[input.length];
		for (int idx = 0; idx < input.length; idx++) {
			output[idx] = (int) input[idx];
		}
		return output;
	}
	

	// determine the number of threads
	private int computeThreadSize() {
		int size = Runtime.getRuntime().availableProcessors();
		size = Math.min(size, MAX_WORKER);
		return size;
	}
	
}
