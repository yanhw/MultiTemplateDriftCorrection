package dc.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
 */
public class DriftManager {
	private static final Logger logger = Logger.getLogger(DriftManager.class.getName());
	private static final int DEFAULTFITTINGDEGREE = 5;
	public final int MAXFITTINGDEGREE = 25;
	public final int FITX = 0;
	public final int FITY = 1;
	public final int FITBOTH = 2;
	private float[] xDrift;
	private float[] yDrift;
	private double[] xWeight;
	private double[] yWeight;
	private float[] xFitted;
	private float[] yFitted;
	private List<Integer> cuttingPoints;
	private List<Integer> degrees;

	private DriftModel driftModel;
	private DriftSectionModel sectionModel;
	
	private boolean isDriftReady;
	
	public DriftManager() {
		
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
		
	}
	
	// initialise variables here because the movie can change
	protected void init(int numFrame) {
		assert (numFrame >= 2);
		this.cuttingPoints = new LinkedList<Integer>();
		this.degrees = new LinkedList<Integer>();
		this.degrees.add(DEFAULTFITTINGDEGREE);
		this.xDrift = new float[numFrame];
		this.yDrift = new float[numFrame];
		this.xWeight = new double[numFrame];
		this.yWeight = new double[numFrame];
		this.xFitted = new float[numFrame];
		this.yFitted = new float[numFrame];
		this.isDriftReady = false;
		
//		setDrifts(xDrift, yDrift);
		sectionModel.setData(cuttingPoints, degrees, numFrame);
	}
	
	protected void setTableModel(DriftModel driftModel, DriftSectionModel sectionModel) {
		assert (driftModel != null);
		assert (sectionModel != null);
		this.driftModel = driftModel;
		this.sectionModel = sectionModel;
	}
	
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
		float[] tempX = new float[xDrift.length];
		float[] tempY = new float[xDrift.length];
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
			while (scanner.hasNextLine() && idx < xDrift.length) {
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
	// TODO: set x and y flags together with drifts, flags determined during template matching
	protected void setDrifts(float[] x, float[] y) {
		assert (x != null);
		assert (y != null);
		assert (x.length == y.length);
		assert (x.length == xDrift.length);
		this.xDrift = x;
		this.yDrift = y;
		driftModel.setData(x, y);
		for (int i = 0; i < xDrift.length; i++) {
			xWeight[i] = 1;
			yWeight[i] = 1;
		}
		isDriftReady = true;
	}

	protected void setDrifts(int frameNumber, float x, float y) {
		assert (frameNumber >= 0);
		assert (frameNumber < xDrift.length);
		this.xDrift[frameNumber] = x;
		this.yDrift[frameNumber] = y;
	}
	
	protected void setXDrift(int frameNumber, float newVal) {
		assert (frameNumber >= 0);
		assert (frameNumber < xDrift.length);
		xDrift[frameNumber] = newVal;
		logger.info("xDrift at frame " + frameNumber + " is changed to " + newVal);
		fitDrift(FITX);
	}
	
	protected void setYDrift(int frameNumber, float newVal) {
		assert (frameNumber >= 0);
		assert (frameNumber < xDrift.length);
		yDrift[frameNumber] = newVal;
		logger.info("yDrift at frame " + frameNumber + " is changed to " + newVal);
		fitDrift(FITY);
	}
	
	protected void removeDrift(int frameNumber) {
		assert (frameNumber >= 0);
		assert (frameNumber < xDrift.length);
		driftModel.setValueAt(0, frameNumber, 1);
		driftModel.setValueAt(0, frameNumber, 2);
		xWeight[frameNumber] = 0;
		yWeight[frameNumber] = 0;
	}


	protected float[] getXDrift() {
		return xDrift;
	}
	
	protected float[] getYDrift() {
		return yDrift;
	}
	
	protected float[] getFittedXDrift() {
		return xFitted;
	}
	
	protected float[] getFittedYDrift() {
		return yFitted;
	}
	
	protected boolean isReady() {
		return true;
	}
	
	//////////////////// drift fitting///////////////////////
	
	// cutting point belongs to the second section only,
	// so the first frame cannot be a cutting point
	protected void addCuttingPoint(int frameNumber) {
		assert (frameNumber >= 0);
		assert (frameNumber < xDrift.length);
		assert(degrees.size() == cuttingPoints.size()+1);
		if (cuttingPoints.contains(frameNumber)) {
			logger.info(frameNumber + " is already a cutting point");
			return;
		}

		int index = cuttingPoints.size();
		for (int i = 0; i < cuttingPoints.size(); i++) {
			if (cuttingPoints.get(i) > frameNumber) {
				index = i;
			}
		}
		int degree = degrees.get(index);
		cuttingPoints.add(index, frameNumber);
		degrees.add(degrees.get(index));
		int start = 0, end = 0;
		// fit first section
		if (index == 0) {
			start = 0;
		} else {
			start = cuttingPoints.get(index-1);
		}
		end = frameNumber;
		degree = degrees.get(index);
		fitDrift(degree, start, end, FITBOTH);
		// fit second section
		start = frameNumber;
		if (index == cuttingPoints.size()-1) {
			end = xDrift.length-1;
		} else {
			end = cuttingPoints.get(index+1);
		}
		degree = degrees.get(index+1);
		fitDrift(degree, start, end, FITBOTH);
		assert(degrees.size() == cuttingPoints.size()+1);
	}
	
	protected void removeCuttingPoint(int sectionIndex) {
		assert(degrees.size() == cuttingPoints.size()+1);
		if (cuttingPoints.size() == 0) {
			logger.info("must have at least 1 section");
			return;
		}
		//TODO: when sectionIndex == 0, set to 1?
		if (sectionIndex <= 0) {
			logger.info("invalid sectionIndex: " + sectionIndex);
			return;
		}
		if (sectionIndex >= degrees.size()) {
			logger.info("section index: " + sectionIndex + " total number of sections: " + degrees.size());
			return;
		}
		cuttingPoints.remove(sectionIndex-1);
		degrees.remove(sectionIndex-1);
		int start = 0, end = xDrift.length-1, degree = 0;
		if (sectionIndex > 1) {
			start = cuttingPoints.get(sectionIndex-1);
		}
		if (sectionIndex < cuttingPoints.size()-1) {
			end = cuttingPoints.get(sectionIndex);
		}
		degree = degrees.get(sectionIndex-1);
		fitDrift(degree, start, end, FITBOTH);

		assert(degrees.size() == cuttingPoints.size()+1);
	}
	
	
	protected void setFitDegree(int sectionIndex, int degree) {
		if (sectionIndex >= degrees.size() || sectionIndex < 0) {
			logger.warning("invalid sectionIndex:" + sectionIndex);
			return;
		}
		if (degree <= 0 || degree > MAXFITTINGDEGREE) {
			logger.info("invalid degree: " + degree);
			return;
		}
		degrees.set(sectionIndex, degree);
		int start = 0;
		int end = xDrift.length-1;
		if (sectionIndex != 0) {
			start = cuttingPoints.get(sectionIndex-1);
		}
		if (sectionIndex < cuttingPoints.size()) {
			end = cuttingPoints.get(sectionIndex);
		}
		fitDrift(degree, start, end, FITBOTH);
	}
	
	protected void fitDrift(int directionOption) {
		assert(degrees.size() == cuttingPoints.size()+1);
		int start = 0;
		for (int i = 0; i < cuttingPoints.size(); i++) {
			fitDrift(degrees.get(i), start, cuttingPoints.get(i), directionOption);
			start = cuttingPoints.get(i);
		}
		if (start < xDrift.length-1) {
			fitDrift(degrees.get(cuttingPoints.size()), start, xDrift.length-1, directionOption);
		}
		logger.info("drift fitting done");
	}
	
	// fit a segment
	private void fitDrift(int degree, int start, int end, int directionOption) {
		logger.info("fitting drift for segment " + start + " to " + end + " with degree " + degree);
		double[] fitted;
		double diff;
		if (directionOption == FITX || directionOption == FITBOTH) {
			fitted = fitDrift(degree, start, end, xDrift, xWeight);
			diff = xDrift[start] - fitted[0];
			for (int i = start; i <= end; i++) {
				xFitted[i] = (float) (fitted[i-start] - diff);
			}
		}
		if (directionOption == FITY || directionOption == FITBOTH) {
			fitted = fitDrift(degree, start, end, yDrift, yWeight);
			diff = yDrift[start] - fitted[0];
			for (int i = start; i <= end; i++) {
				yFitted[i] = (float) (fitted[i-start] - diff);
			}
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
		assert (xDrift != null && yDrift != null);
		String filename = FileSystem.joinPath(saveDir, "drift.csv");
		saveCsv(filename, xDrift, yDrift);
	}
	
	protected void saveFittedDrift(String saveDir) {
		assert (saveDir != null);
		assert (xFitted != null && yFitted != null);
		String filename = FileSystem.joinPath(saveDir, "fitted_drift.csv");
		saveCsv(filename, xFitted, yFitted);
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