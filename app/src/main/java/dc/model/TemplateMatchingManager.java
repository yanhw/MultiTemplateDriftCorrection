package dc.model;

import static dc.utils.Constants.MAX_WORKER;

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

import javax.swing.table.DefaultTableModel;

import dc.step.GaussianImage;

public class TemplateMatchingManager {
	private static final Logger logger = Logger.getLogger(TemplateMatchingManager.class.getName());
//	private static final int MAX_SEGMENT = 100;		// upper limit for number of key frames
	
	private static final int TOP = 0;
	private static final int BOTTOM = 1;
	private static final int LEFT = 2;
	private static final int RIGHT = 3;
	
	private FileHandler fh;
	private Flag interruptionFlag;
	
	private List<Path> fileList;
	private List<MovieSegment> movieSegments;
	private List<Integer> keyFrameList;	//TODO: should remove this in the future
	private DefaultTableModel model;	//TODO: use this to replace movieSegments? or maybe integrate these two together
	private GaussianImage gaussianFilter;
	//TODO monitor the changes and template match only changed sections
	
	/*
	 *  progress monitors if template matching is done.
	 *  it is changed by SwingWorker when performing template matching
	 *  it is set to 100 if external csv file is used
	 */
	private int progress = 0;
	
	public TemplateMatchingManager() {
		this.gaussianFilter = new GaussianImage(5,3);
	}
	
	public void setFileHandler(FileHandler fh) {
		this.fh = fh;
		logger.addHandler(fh);
		gaussianFilter.setFileHandler(fh);
	}

	public void setInterruptionFlag(Flag interrupt) {
		interruptionFlag = interrupt;
	}
	
	public void setTableModel(DefaultTableModel model) {
		this.model = model;
		model.addColumn("index");
		model.addColumn("frist frame");
		model.addColumn("last frame");
		model.addColumn("template frame");
		model.addColumn("has template");
//		model.addRow(new Object[] {"1","","",""});
	}
	
	public void init(List<Path> fileList) {
		this.fileList = fileList;
		movieSegments = new LinkedList<MovieSegment>();
		keyFrameList = new LinkedList<Integer>();
		progress = 0;
		movieSegments.add(new MovieSegment(0, fileList.size()-1));
		refreshTable();
	}
	
	protected void setSegmentFrame(int frameNumber) {
		if (frameNumber <= 0) {
			return;
		}
		if (frameNumber >= fileList.size()-1) {
			return;
		}
		if (keyFrameList.contains(frameNumber)) {
			return;
		}
		int idx = 0;
		for (int i: keyFrameList) {
			if (i > frameNumber) {
				break;
			}
			idx++;
		}
		MovieSegment target = movieSegments.remove(idx);
		int starting = target.getStartFrame();
		int ending = target.getEndFrame();
		MovieSegment first = new MovieSegment(starting, frameNumber);
		MovieSegment second = new MovieSegment(frameNumber, ending);
		if (target.isReady()) {
			int ROIFrame = target.getROIFrame();
			if (ROIFrame < frameNumber) {
				first.setROI(ROIFrame, target.getROI(), target.getTemplate(), target.getBlurredTemplate());
			} else {
				second.setROI(ROIFrame, target.getROI(), target.getTemplate(), target.getBlurredTemplate());
			}
		}
		movieSegments.add(idx, first);
		movieSegments.add(idx+1, second);
		keyFrameList.add(idx, frameNumber);
		logger.info("set key frame at: " + frameNumber);
		refreshTable();
		assert (keyFrameList.size() == movieSegments.size()-1);
	}
	
	protected void removeSegmentFrame(int segmentIndex) {
		// first segment cannot be removed
		if (segmentIndex == 0) {
			return;
		}
		if (segmentIndex >= movieSegments.size()) {
			return;
		}
		MovieSegment target = movieSegments.get(segmentIndex);
		MovieSegment previous = movieSegments.get(segmentIndex-1);		
		int starting = previous.getStartFrame();
		int ending = target.getEndFrame();
		MovieSegment merge = new MovieSegment(starting, ending);
		if (previous.isReady()) {
			merge.setROI(previous.getROIFrame(), previous.getROI(), previous.getTemplate(), previous.getBlurredTemplate());
		} else if (target.isReady()) {
			merge.setROI(target.getROIFrame(), target.getROI(), target.getTemplate(), target.getBlurredTemplate());
		}
		movieSegments.remove(previous);
		movieSegments.remove(target);
		movieSegments.add(segmentIndex-1,merge);
		keyFrameList.remove(segmentIndex-1);
		refreshTable();
		assert (keyFrameList.size()+1 == movieSegments.size());
	}
	
	/* warning: cannot set ROI at the last frame of a segment, unless it
	 * is the only segment. (see getSegment method)
	 * 
	 */
	protected boolean setROI(int frameNumber, int ROI[], double[][] image) {
		if (frameNumber < 0) {
			logger.warning("invalid frameNumber: " + frameNumber);
			return false;
		}
		if (frameNumber >= fileList.size()-1) {
			logger.warning("invalid frameNumber: " + frameNumber);
			return false;
		}
		
		if (image == null) {
			logger.info("null image");
			return false;
		}
		if (ROI.length != 4) {
			logger.warning("ROI is not valid: " + ROI);
			return false;
		}
		if (ROI[BOTTOM] <= ROI[TOP] || ROI[RIGHT] <= ROI[LEFT]) {
			logger.warning("ROI is not valid: " + ROI);
			return false;
		}
		if (ROI[TOP] < 0 || ROI[LEFT] < 0) {
			logger.warning("ROI is not valid: " + ROI);
			return false;
		}
		if (image.length <= ROI[BOTTOM] || image[0].length <= ROI[RIGHT]) {
			logger.info("ROI not compatible with image. ROI = " + ROI);
			return false;
		}
		int height = ROI[BOTTOM]-ROI[TOP]+1;
		int width = ROI[RIGHT]-ROI[LEFT]+1;
		double[][] template = new double[height][width];
		double[][] blurredTemplate = new double[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				template[i][j] = image[i+ROI[TOP]][j+ROI[LEFT]];
			}
		}
		gaussianFilter.gaussian(image, 5, 3);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				blurredTemplate[i][j] = image[i+ROI[TOP]][j+ROI[LEFT]];
			}
		}
		logger.info("setting ROI");
		getSegment(frameNumber).setROI(frameNumber, ROI, template, blurredTemplate);
		refreshTable();
		return true;
	}
	
	protected void removeROI(int segmentIndex) {
		if (segmentIndex >= movieSegments.size()) {
			return;
		}
		MovieSegment target = movieSegments.get(segmentIndex);
		target.removeROI();
		refreshTable();
	}
	
	private void refreshTable() {
		model.setRowCount(0);
		int count = 1;
		for (MovieSegment segment: movieSegments) {
			int startingIdx = segment.getStartFrame();
			int endingIdx = segment.getEndFrame();
			int templateFrame = segment.getROIFrame();
			if (templateFrame == -1) {
				model.addRow(new Object[] {count, startingIdx, endingIdx, "", false});
			} else {
				model.addRow(new Object[] {count, startingIdx, endingIdx, templateFrame, true});
			}
			count++;
		}
	}
	
	protected void run(String saveDir, float[] tempXDrift, float[] tempYDrift, boolean blur) {
		logger.info("TemplateMatching started running...");
		logger.info("blur image: " + blur);
		progress = 0;
		List<Integer> templateXList = new LinkedList<Integer>();
		List<Integer> templateYList = new LinkedList<Integer>();
		List<Integer> startingIdx = new LinkedList<Integer>();
		List<Integer> endingIdx = new LinkedList<Integer>();
		List<double[][]> templates = new LinkedList<double[][]>();
//		List<Integer> indexIdx = new LinkedList<Integer>();
		for (MovieSegment segment: movieSegments) {
			templateXList.add(segment.getROI()[LEFT]);
			templateYList.add(segment.getROI()[TOP]);
			startingIdx.add(segment.getStartFrame());
			endingIdx.add(segment.getEndFrame());
			if (!blur) {
				templates.add(segment.getTemplate());
			} else {
				templates.add(segment.getBlurredTemplate());
			}
		}
		
		int numThread = computeThreadSize();
		logger.fine("total threads: " + numThread);
		// create process pool
		Process process = new TemplateMatchingProcess(blur, interruptionFlag);
		process.setFileHandler(fh);
		List<Process> processPool = new ArrayList<Process>(numThread);
		for (int i = 0; i < numThread; i++) {
			processPool.add(process.copy());
		}
		logger.info("process pool created");
		// setup saveDir
		Path path = Paths.get(saveDir);
		try {
			Files.createDirectories(path);
			logger.info(saveDir + " is created");
		} catch (IOException e) {
			logger.severe("failed to create saveDir");
			return;
		}
		

		int total = computeTotalFrameNumber(startingIdx,endingIdx);
		int done = 0;
		

		// i for movie segment, j for process index in processPool
		for (int i = 0; i < startingIdx.size(); i++) {
//			int templateX = templateXList.get(i);
//			int templateY = templateYList.get(i);
			
			int numActiveThread = numThread;
			// get files
			List<Path> fileList = this.fileList.subList(startingIdx.get(i), endingIdx.get(i)+1);
			
			// split the task for threads
			if (fileList.size() < numThread) {
				numActiveThread = fileList.size();
			}
			
			
			
			logger.info("number of active thread: " + numActiveThread);
			int[] procStartingIdxList = new int[numThread];
			int[] procEndingIdxList = new int[numThread];
			List<List<Path>> threadFileList = new ArrayList<List<Path>>(numActiveThread);
			for (int j = 0; j < numActiveThread; j++) {
				int start = j*(fileList.size()/numActiveThread);
				int end = (j+1)*(fileList.size()/numActiveThread);
				if (j == numActiveThread-1) {
					end = fileList.size();
				}
				procStartingIdxList[j] = start;
				procEndingIdxList[j] = end;
				threadFileList.add(j, fileList.subList(start, end));
			}
			
			for (int j = 0; j < numActiveThread; j++) {
				((TemplateMatchingProcess)processPool.get(j)).setTemplate(templates.get(i));
				// no saving involved, so not important
				processPool.get(j).initialise(saveDir, procStartingIdxList[j]);
			}
			
			// countDownLatch to track progress
			CountDownLatch latch = new CountDownLatch(fileList.size());
			logger.info("latch launched");
			// run the conversion
			ExecutorService pool = Executors.newFixedThreadPool(numActiveThread);
			WorkerThread[] workers = new WorkerThread[numActiveThread];
			
			for (int j = 0; j < numActiveThread; j++) {
				logger.info("starting process " + j + " for segment " + i);
				workers[j] = new WorkerThread(processPool.get(j), threadFileList.get(j), latch);
				workers[j].setFileHandler(fh);
				pool.execute(workers[j]);
			}
			while (latch.getCount()>0) {
//				System.out.println("progress: "+(100*(fileList.size()-latch.getCount()+done)/total) + " total: "+total+" count: " + latch.getCount()+ " done: " + done);
				progress = (int) (100*(fileList.size()-latch.getCount()+done)/total);
//				setProgress(prog)
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logger.info("process interrupted");
					e.printStackTrace();
				}
			}
			if (interruptionFlag.get()) {
				progress = 100;
				pool.shutdown();
				logger.info("process interrupted");
				return;
			}
			float xDiff = 0, yDiff = 0;
			for (int j = 0; j < numActiveThread; j++) {
				List<Integer> xDrift = ((TemplateMatchingProcess) processPool.get(j)).getXDrift();
				List<Integer> yDrift = ((TemplateMatchingProcess) processPool.get(j)).getYDrift();
				int frameStartingIdx = startingIdx.get(i)+procStartingIdxList[j];
				int frameEndingIdx = startingIdx.get(i)+procEndingIdxList[j];
				if (j==0) {
					xDiff = xDrift.get(0) - tempXDrift[startingIdx.get(i)];
					yDiff = yDrift.get(0) - tempYDrift[startingIdx.get(i)];
//					xDiff = xDrift.get(0) - templateX;
//					yDiff = yDrift.get(0) - templateY;
				}
//				System.out.println("x and y diff:" + xDiff + " " + yDiff);
				for (int k = frameStartingIdx; k < frameEndingIdx; k++) {
					tempXDrift[k] = xDrift.get(k-frameStartingIdx)-xDiff;
					tempYDrift[k] = yDrift.get(k-frameStartingIdx)-yDiff;
//					System.out.println("idx: "+k+" x drift: " + xArray[k] + " y drift: " + yArray[k]);
				}			
			}
			done += fileList.size();
			pool.shutdown();
		}
		progress = 100;
		assert (tempXDrift[0] == 0);
		assert (tempYDrift[0] == 0);
		logger.info("template matching master thread finished");
		return;
	}
	
	// warning: if the frameNumber is at intersection of two MovieSegments
	// it returns the first MovieSegment
	protected MovieSegment getSegment(int frameNumber) {
		assert (frameNumber >= 0);
		assert (keyFrameList.size()+1 == movieSegments.size());
		
		
		int idx = 0;
		for (int i: keyFrameList) {
			if (i > frameNumber) {
				break;
			}
			idx++;
		}
		return movieSegments.get(idx);
	}
	
	protected boolean templageMatchingPreRunValidation() {
		for (MovieSegment segment: movieSegments) {
			if (!segment.isReady()) {
				logger.info("rejected because ROI is not set bewteen " + segment.getStartFrame() + " and " + segment.getEndFrame());
				return false;
			}
		}
		logger.info("passed pre-run validation");
		return true;
	}
	
	protected int getProgress() {
		return progress;
	}
	
	// for template matching process, total frame number might be less that movie size
	private int computeTotalFrameNumber(List<Integer> startingIdx, List<Integer> endingIdx) {
		int count = 0;
		for (int i = 0; i < startingIdx.size(); i++) {
			count += endingIdx.get(i) - startingIdx.get(i) + 1;
		}
		return count;
	}
	
	// determine the number of threads
	private int computeThreadSize() {
		int size = Runtime.getRuntime().availableProcessors();
		size = Math.min(size, MAX_WORKER);
		return size;
	}
	
	// this method is used when external csv drift file is loaded
	protected void setProgress(int num) {
		assert(num >= 0);
		assert(num <= 100);
		logger.info("setting progress to " + num);
		this.progress = num;
	}
}
