package dc.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

import dc.model.DriftModel;
import dc.model.DriftSectionModel;
import dc.utils.FileSystem;

/* 
 * always try to fit data with polynomial here, decides which to use in Movie
 * 
 * commented methods are kept for future non-gui version
 */
public class DriftManager {
	private static final Logger logger = Logger.getLogger(DriftManager.class.getName());
	public static final int FITX = 0;
	public static final int FITY = 1;
	public static final int FITBOTH = 2;

	private DriftModel driftModel;
	private DriftSectionModel sectionModel;
	
	private boolean isDriftReady;
	
	public DriftManager() {
		
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		
	}
	
	protected void setTableModel(DriftModel driftModel, DriftSectionModel sectionModel) {
		assert (driftModel != null);
		assert (sectionModel != null);
		this.driftModel = driftModel;
		this.sectionModel = sectionModel;
	}	
	
	// initialise variables here because the movie can change
	protected void init(int numFrame) {
		assert (numFrame >= 2);
		this.isDriftReady = false;
		driftModel.initData(numFrame);
		sectionModel.initData(numFrame);
	}
	
	
	
////////////////////set drift///////////////////////
	
	/*
	 *  required csv file structure:
	 *  	Slice, dX,  dY
	 *  	0,     num, num
	 *  	...
	 *  
	 *  num can be a integer or empty
	 *  
	 *  This should not set xDrift and yDrift directly,
	 *  call the default method instead.
	 */
	protected boolean setDrifts(String filename) {
		if (filename == null) {
			logger.info("null input");
			return false;
		}
		if (!filename.endsWith(".csv")) {
			logger.info("wrong format: " + filename);
			return false;
		}
		float[] tempX = new float[driftModel.getRowCount()];
		float[] tempY = new float[driftModel.getRowCount()];
		try (Scanner scanner = new Scanner(new File(filename));) {
		    if (!scanner.hasNextLine()) {
		    	logger.info("empty file!");
		    	return false;
		    }
		    String firstLine = scanner.nextLine();
		    if (!firstLine.equals("Slice,dX,dY")) {
		    	logger.info("invalid csv file header: " + firstLine);
		    	return false;
		    }
		    int idx = 0;
			while (scanner.hasNextLine() && idx < driftModel.getRowCount()) {
		    	String data = scanner.nextLine();
		    	StringTokenizer tokenizer = new StringTokenizer(data, ",");
		    	if (tokenizer.countTokens() != 3) {
		    		logger.info("invalid format at line " + idx + " : " + data);
		    		return false;
		    	}
		    	int targetIdx = Integer.parseInt(tokenizer.nextToken());
		    	if (targetIdx != idx) {
		    		logger.info("line mismatch, expecting: " + idx + ", actual:" + targetIdx);
		    		return false;
		    	}
		    	tempX[idx] = Float.parseFloat(tokenizer.nextToken());
		    	tempY[idx] = Float.parseFloat(tokenizer.nextToken());
		    	//TODO: follow target index
		    	idx++;
		    }
			logger.info("last line recorded: " + idx);
		} catch (FileNotFoundException e) {
			logger.info(filename + " is not found");
			e.printStackTrace();
			return false;
		}
		setDrifts(tempX, tempY);
		return true;
	}
	
	// default entry point to initialise drifts, if succeed, change isDriftReady to true, and it always stays true unless reset movie
	protected void setDrifts(float[] x, float[] y) {
		assert (x != null);
		assert (y != null);
		assert (x.length == y.length);
		assert (x.length == driftModel.getRowCount());
		driftModel.setData(x, y);
		isDriftReady = true;
		logger.info("ready: " + isDriftReady);
	}

//	protected void setDrifts(int frameNumber, float x, float y) {
//		assert (frameNumber >= 0);
//		assert (frameNumber < driftModel.getRowCount());
//		driftModel.setValueAt(x, frameNumber, DriftModel.DX);
//		driftModel.setValueAt(y, frameNumber, DriftModel.DY);
//	}
//	
//	protected void setXDrift(int frameNumber, float newVal) {
//		assert (frameNumber >= 0);
//		assert (frameNumber < driftModel.getRowCount());
//		driftModel.setValueAt(newVal, frameNumber, DriftModel.DX);
//		logger.info("xDrift at frame " + frameNumber + " is changed to " + newVal);
//	}
//	
//	protected void setYDrift(int frameNumber, float newVal) {
//		assert (frameNumber >= 0);
//		assert (frameNumber < driftModel.getRowCount());
//		driftModel.setValueAt(newVal, frameNumber, DriftModel.DY);
//		logger.info("yDrift at frame " + frameNumber + " is changed to " + newVal);
//	}
//	
//	protected void removeDrift(int frameNumber) {
//		assert (frameNumber >= 0);
//		assert (frameNumber < driftModel.getRowCount());
//		driftModel.removeDrift(frameNumber);
//	}


	protected float[] getXDrift() {
		float[] drift = new float[driftModel.getRowCount()];
		for (int i = 0; i < drift.length; i++) {
			drift[i] = ((Number) driftModel.getValueAt(i, DriftModel.DX)).floatValue();
		}
		return drift;
	}
	
	protected float[] getYDrift() {
		float[] drift = new float[driftModel.getRowCount()];
		for (int i = 0; i < drift.length; i++) {
			drift[i] = ((Number) driftModel.getValueAt(i, DriftModel.DY)).floatValue();
		}
		return drift;
	}
	
	protected float[] getFittedXDrift() {
		float[] drift = new float[driftModel.getRowCount()];
		for (int i = 0; i < drift.length; i++) {
			drift[i] = ((Number) driftModel.getValueAt(i, DriftModel.FITTED_DX)).floatValue();
		}
		return drift;
	}
	
	protected float[] getFittedYDrift() {
		float[] drift = new float[driftModel.getRowCount()];
		for (int i = 0; i < drift.length; i++) {
			drift[i] = ((Number) driftModel.getValueAt(i, DriftModel.FITTED_DY)).floatValue();
		}
		return drift;
	}
	
	private double[] getXWeight() {
		double[] drift = new double[driftModel.getRowCount()];
		for (int i = 0; i < drift.length; i++) {
			drift[i] = ((Double) driftModel.getValueAt(i, DriftModel.WEIGHT_X)).doubleValue();
		}
		return drift;
	}
	
	private double[] getYWeight() {
		double[] drift = new double[driftModel.getRowCount()];
		for (int i = 0; i < drift.length; i++) {
			drift[i] = ((Double) driftModel.getValueAt(i, DriftModel.WEIGHT_Y)).doubleValue();
		}
		return drift;
	}
	
	protected boolean isReady() {
		return true;
	}
	
	//////////////////// drift section///////////////////////
	
	// cutting point belongs to the second section only,
	// so the first frame cannot be a cutting point
	protected void addCuttingPoint(int frameNumber) {
		assert (frameNumber >= 0);
		assert (frameNumber < driftModel.getRowCount());
		if (sectionModel.isEndFrame(frameNumber)) {
			logger.info(frameNumber + " is already a cutting point");
			return;
		}
		sectionModel.setEndFrame(frameNumber);
	}
	
	protected void removeCuttingPoint(int sectionIndex) {
		if (sectionModel.getRowCount() == 1) {
			logger.info("must have at least 1 section");
			return;
		}
		//TODO: when sectionIndex == 0, set to 1?
		if (sectionIndex <= 0) {
			logger.info("invalid sectionIndex: " + sectionIndex);
			return;
		}
		if (sectionIndex >= sectionModel.getRowCount()) {
			logger.info("section index: " + sectionIndex + " total number of sections: " + sectionModel.getRowCount());
			return;
		}
		sectionModel.removeEndFrame(sectionIndex);
	}
	
	
//	protected void setFitDegree(int sectionIndex, int degree) {
//		if (sectionIndex >= sectionModel.getRowCount() || sectionIndex < 0) {
//			logger.warning("invalid sectionIndex:" + sectionIndex);
//			return;
//		}
//		if (degree <= 0 || degree > DriftSectionModel.MAXFITTINGDEGREE) {
//			logger.info("invalid degree: " + degree);
//			return;
//		}
//		sectionModel.setValueAt(degree, sectionIndex, DriftSectionModel.DEGREE);
//	}
	
	////////////////////drift fitting///////////////////////
	
	
//	
//	protected void fitDrift(int directionOption) {
//		int start, end;
//		for (int i = 0; i < cuttingPoints.size(); i++) {
//			fitDrift(degrees.get(i), start, cuttingPoints.get(i), directionOption);
//			start = cuttingPoints.get(i);
//		}
//		if (start < driftModel.getRowCount()-1) {
//			fitDrift(degrees.get(cuttingPoints.size()), start, driftModel.getRowCount()-1, directionOption);
//		}
//		isDriftReady = true;
//		logger.info("drift fitting done");
//	}
//	
	
	protected void fitDrift(int start, int end, int directionOption) {
		assert start >= 0 && start <= end && end < driftModel.getRowCount();
		assert directionOption == FITX || directionOption == FITY || directionOption == FITBOTH;
		int startSection = sectionModel.getRowNumber(start);
		int endSection = sectionModel.getRowNumber(end);
		for (int i = startSection; i <= endSection; i++) {
			int startFrame = ((Number) sectionModel.getValueAt(i, DriftSectionModel.START)).intValue();
			int endFrame = (int) sectionModel.getValueAt(i, DriftSectionModel.END);
			if (endFrame == startFrame) {
				logger.warning("1 frame section: " + startFrame + " " + i);
				continue;
			}
			int degree = (int) sectionModel.getValueAt(i, DriftSectionModel.DEGREE);
			fitDrift(degree, startFrame, endFrame, directionOption);
		}
	}
	
	
	// fit a segment
	private void fitDrift(int degree, int start, int end, int directionOption) {
		assert degree > 0 && degree <= DriftSectionModel.MAXFITTINGDEGREE;
		assert start >= 0 && start < end && end < driftModel.getRowCount();
		assert directionOption == FITX || directionOption == FITY || directionOption == FITBOTH;
		logger.info("fitting drift for segment " + start + " to " + end + " with degree " + degree);
		double[] fitted;
		double diff;
		if (directionOption == FITX || directionOption == FITBOTH) {
			float[] xDrift = getXDrift();
			double[] xWeight = getXWeight();
			fitted = fitDrift(degree, start, end, xDrift, xWeight);
			diff = xDrift[start] - fitted[0];		
			for (int i = start; i <= end; i++) {
				fitted[i-start] -= diff;
			}
			driftModel.setDrift(fitted, start, end, DriftModel.FITTED_DX);
		}
		if (directionOption == FITY || directionOption == FITBOTH) {
			float[] yDrift = getYDrift();
			double[] yWeight = getYWeight();
			fitted = fitDrift(degree, start, end, yDrift, yWeight);
			diff = yDrift[start] - fitted[0];
			for (int i = start; i <= end; i++) {
				fitted[i-start] -= diff;
			}
			driftModel.setDrift(fitted, start, end, DriftModel.FITTED_DY);
		}
	}
	
	// fit x or y
	private double[] fitDrift(int degree, int start, int end, float rawDrift[], double[] weights) {
		Collection<WeightedObservedPoint> points = new ArrayList<>();
		for (int i = start; i <= end; i++) {
//			logger.info("" + weights[i]);
			points.add(new WeightedObservedPoint(weights[i], i, rawDrift[i]));
		}
	
		PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
		double[] coeff = fitter.fit(points);
		PolynomialFunction function = new PolynomialFunction(coeff);
		double[] fitted = new double[end-start+1];
		for (int i = start; i <= end; i++) {
			fitted[i-start] = function.value(i);
		}
		return fitted;
	}
	
	///////////////// csv saving////////////////////////////////
	protected void saveRawDrift(String saveDir) {
		assert (saveDir != null);
		assert (isDriftReady);
		String filename = FileSystem.joinPath(saveDir, "drift.csv");
		saveCsv(filename, getXDrift(), getYDrift());
	}
	
	protected void saveFittedDrift(String saveDir) {
		assert (saveDir != null);
		assert (isDriftReady);
		String filename = FileSystem.joinPath(saveDir, "fitted_drift.csv");
		saveCsv(filename, getFittedXDrift(), getFittedYDrift());
	}
	
	private void saveCsv(String filename, float[] xArray, float[] yArray) {
		File file = new File(filename);
		try (PrintWriter writer = new PrintWriter(file)) {
			StringBuilder sb = new StringBuilder();
			sb.append("Slice");
			sb.append(',');
			sb.append("dX");
			sb.append(',');
			sb.append("dY");
			sb.append('\n');
			for (int idx = 0; idx < xArray.length; idx++) {
				sb.append(String.valueOf(idx));
				sb.append(',');
				sb.append(String.valueOf(xArray[idx]));
				sb.append(',');
				sb.append(String.valueOf(yArray[idx]));
				sb.append('\n');
			}
			writer.write(sb.toString());
			logger.fine("created: " + filename);
		} catch (FileNotFoundException  e) {
			logger.severe("failed to write to " + filename);
		}
	}

	public boolean isDriftReady() {
		return isDriftReady;
	}
	
	

}