package dc.gui.image;



import static org.junit.Assert.assertEquals;

import javax.swing.DefaultListSelectionModel;

import org.jfree.data.xy.XYSeries;
import org.junit.Before;
import org.junit.Test;

import dc.model.DriftModel;

// these tests are focused on listener for DriftModel
public class DriftViewerTest {
	private static final double EPSILON = 0.01;
	
	private DriftViewer myViewer;
	private DriftModel myModel;
	private DefaultListSelectionModel mySelection;
	private XYSeries rawDrift;
	private XYSeries fittedDrift;
	private String direction = "x";
	
	// deprecated methods in DriftViewer are for this test only
	@SuppressWarnings("deprecation")
	@Before
	public void init() {
		myViewer = new DriftViewer(direction);
		myModel = new DriftModel();
		myViewer.setDriftModelListener(myModel);
		mySelection = myViewer.getSelectionModel();
		rawDrift = myViewer.getRawDrift();
		fittedDrift = myViewer.getFittedDrift();
	}
	
	@Test
	public void testInit() {
		int firstMovieSize = 10;
		myModel.initData(firstMovieSize);
		checkConsistency();
	}
	
	@Test
	public void testReInit() {
		int firstMovieSize = 10, secondMovieSize = 20;
		myModel.initData(firstMovieSize);
		myModel.initData(secondMovieSize);
		checkConsistency();
	}
	
	@Test
	public void testResetSession() {
		int firstMovieSize = 10;
		myModel.initData(firstMovieSize);
		myModel.clear();
		checkConsistency();
	}
	
	@Test
	public void testSetData() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		float[] dx = dummyArray(frameNumber);
		float[] dy = dummyArray(frameNumber);
		myModel.setData(dx, dy);
		checkConsistency();
	}
	
	@Test
	public void testRemoveDrift() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		int targetFrame = 5;
		myModel.removeDrift(targetFrame);
		checkConsistency();
	}
	
	@Test
	public void testSetDrift() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		int start = 3, end = 7, col = DriftModel.FITTED_DX;
		double[] value = dummyArrayDouble(end-start+1);
		myModel.setDrift(value, start, end, col);
		checkConsistency();
	}
	
	@Test
	public void testSetValueAt() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		int targetFrame = 5;
		int targetValue = 8;
		int targetCol = DriftModel.DX;
		myModel.setValueAt(targetValue, targetFrame, targetCol);
		checkConsistency();
	}
	
	//TODO
	public void testSelection() {
		mySelection.getLeadSelectionIndex();
	}
	
	private void checkConsistency() {
		int movieSize = myModel.getRowCount();
		int plotSize = rawDrift.getItemCount();
		int plotSize2 = fittedDrift.getItemCount();
		assertEquals("compare movie size", movieSize, plotSize);
		assertEquals("compare movie size", movieSize, plotSize2);
		for (int i = 0; i < movieSize; i++) {
			double dx = ((Number) myModel.getValueAt(i, DriftModel.DX)).doubleValue();
			double fittedDx = ((Number) myModel.getValueAt(i, DriftModel.FITTED_DX)).doubleValue();
			int xIdx = rawDrift.indexOf(i);
			double plottedDx = rawDrift.getY(xIdx).doubleValue();
			int fittedXIdx = fittedDrift.indexOf(i);
			double plottedFittedDx = fittedDrift.getY(fittedXIdx).doubleValue();
			assertEquals("raw plot idx", i, xIdx);
			assertEquals("fitted plot idx", i, fittedXIdx);
			assertEquals("raw plot value", dx, plottedDx, EPSILON);
			assertEquals("fitted plot value", fittedDx, plottedFittedDx, EPSILON);
		}
	}
	
	private float[] dummyArray(int size) {
		float[] array = new float[size];
		for (int i = 0; i < size; i++) {
			array[i] = (float) i;
		}
		return array;
	}
	
	private double[] dummyArrayDouble(int size) {
		double[] array = new double[size];
		for (int i = 0; i < size; i++) {
			array[i] = (double) i;
		}
		return array;
	}
}
