package dc.model;

import static org.junit.Assert.*;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.junit.Before;
import org.junit.Test;

public class DriftModelTest {
	private DriftModel myModel;
	private DriftModelListener myListener;
	
	@Before
	public void init() {
		myModel = new DriftModel();
		myListener = new DriftModelListener();
		myModel.addTableModelListener(myListener);
	}
	
	@Test
	public void testInitData() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		assertEquals("row count", frameNumber, myModel.getRowCount());
		assertEquals("1 event", 1, myListener.count);
		assertEquals("all rows", 0, myListener.firstRow);
		assertEquals("all rows", frameNumber-1, myListener.lastRow);
		assertEquals("all col", TableModelEvent.ALL_COLUMNS, myListener.col);
		assertEquals("insertion event", TableModelEvent.INSERT, myListener.type);
	}
	
	@Test
	public void testInitDataFail() {
		int frameNumber = 1;
		myModel.initData(frameNumber);
		assertEquals("row count", 0, myModel.getRowCount());
		assertEquals("no event", 0, myListener.count);
	}
	
	@Test
	public void testSetData() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		myListener.resetCount();
		float[] dx = dummyArray(frameNumber);
		float[] dy = dummyArray(frameNumber);
		myModel.setData(dx, dy);
		assertEquals("single event", 1, myListener.count);
		assertEquals("all rows", 0, myListener.firstRow);
		assertEquals("all rows", frameNumber-1, myListener.lastRow);
		assertEquals("all col", TableModelEvent.ALL_COLUMNS, myListener.col);
		assertEquals("UPDATE event", TableModelEvent.UPDATE, myListener.type);
		for (int i = 0; i < myModel.getRowCount(); i++) {
			assertEquals("dx", dx[i], myModel.getValueAt(i, DriftModel.DX));
			assertEquals("dy", dy[i], myModel.getValueAt(i, DriftModel.DY));
			assertEquals("weightx", 1.0, myModel.getValueAt(i, DriftModel.WEIGHT_X));
			assertEquals("weighty", 1.0, myModel.getValueAt(i, DriftModel.WEIGHT_Y));
		}
	}
	
	
	@Test
	public void testSetDataFail() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		myListener.resetCount();
		float[] wrongSize = dummyArray(frameNumber-1);
		float[] rightSize = dummyArray(frameNumber);
		myModel.setData(null, null);
		assertEquals("no event", 0, myListener.count);
		myModel.setData(wrongSize, wrongSize);
		assertEquals("no event", 0, myListener.count);
		myModel.setData(null, rightSize);
		assertEquals("no event", 0, myListener.count);
		myModel.setData(wrongSize, null);
		assertEquals("no event", 0, myListener.count);
		myModel.setData(wrongSize, rightSize);
		assertEquals("no event", 0, myListener.count);
	}
	
	@Test
	public void testClear() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		myListener.resetCount();
		myModel.clear();
		assertEquals("single event", 1, myListener.count);
		assertEquals("no element", 0, myModel.getRowCount());
	}
	
	@Test
	public void testRemoveDrift() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		myListener.resetCount();
		int targetFrame = 5;
		myModel.removeDrift(targetFrame);
		assertEquals("2 events", 2, myListener.count);
		assertEquals("row", targetFrame, myListener.firstRow);
		assertEquals("col", DriftModel.WEIGHT_Y, myListener.col);
		assertEquals("UPDATE event", TableModelEvent.UPDATE, myListener.type);
		assertEquals("value to 0", 0.0, ((Number)myListener.targetContent).doubleValue(), 0.001);
	}
	
	@Test
	public void testRemoveDriftFail() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		myListener.resetCount();
		myModel.removeDrift(frameNumber);
		assertEquals("no event", 0, myListener.count);
		myModel.removeDrift(-1);
		assertEquals("no event", 0, myListener.count);
		frameNumber = 5;
		myModel.removeDrift(frameNumber);
		myListener.resetCount();
		myModel.removeDrift(frameNumber);
		assertEquals("no event", 0, myListener.count);
	}
	
	@Test
	public void testSetDrift() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		myListener.resetCount();
		int start = 3, end = 7, col = DriftModel.FITTED_DX;
		double[] value = dummyArrayDouble(end-start+1);
		myModel.setDrift(value, start, end, col);
		assertEquals("one event", 1, myListener.count);
		assertEquals("first row", start, myListener.firstRow);
		assertEquals("last row", end, myListener.lastRow);
		assertEquals("col", col, myListener.col);
		assertEquals("UPDATE event", TableModelEvent.UPDATE, myListener.type);
		for (int i = start; i <=end; i++) {
			assertEquals("updated value", value[i-start], ((Number)myModel.getValueAt(i, col)).doubleValue(), 0.001);
		}
	}
	
	@Test
	public void testSetDriftFail() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		myListener.resetCount();
		int start = 3, end = 7, col = DriftModel.FITTED_DX;
		double[] value = dummyArrayDouble(end-start+1);
		myModel.setDrift(value, -1, end, col);
		assertEquals("no event", 0, myListener.count);
		myModel.setDrift(value, start, frameNumber+2, col);
		assertEquals("no event", 0, myListener.count);
		myModel.setDrift(value, start, start-1, col);
		assertEquals("no event", 0, myListener.count);
		myModel.setDrift(value, start, end+1, col);
		assertEquals("no event", 0, myListener.count);
		myModel.setDrift(null, start, end, col);
		assertEquals("no event", 0, myListener.count);
		myModel.setDrift(value, start, end, DriftModel.DX);
		assertEquals("no event", 0, myListener.count);
		myModel.setDrift(value, start, end, DriftModel.WEIGHT_X);
		assertEquals("no event", 0, myListener.count);
	}
	
	@Test
	public void testGetRowCount() {
		assertEquals("empty model", 0, myModel.getRowCount());
		int frameNumber = 10;
		myModel.initData(frameNumber);
		assertEquals("row count", frameNumber, myModel.getRowCount());
	}
	
	@Test
	public void testSetValueAt() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		myListener.resetCount();
		int targetFrame = 5;
		int targetValue = 8;
		int targetCol = DriftModel.DX;
		myModel.setValueAt(targetValue, targetFrame, targetCol);
		assertEquals("one event", 1, myListener.count);
		assertEquals("row", targetFrame, myListener.firstRow);
		assertEquals("col", DriftModel.DX, myListener.col);
		assertEquals("UPDATE event", TableModelEvent.UPDATE, myListener.type);
		assertEquals("value to targetValue", targetValue, ((Number)myListener.targetContent).doubleValue(), 0.001);
	}
	
	@Test
	public void testSetValueAtFail() {
		int frameNumber = 10;
		myModel.initData(frameNumber);
		myListener.resetCount();
		int value = 10;
		myModel.setValueAt(value, frameNumber, DriftModel.DX);
		assertEquals("no event", 0, myListener.count);
		myModel.setValueAt(value, -1, DriftModel.DX);
		assertEquals("no event", 0, myListener.count);
		myModel.setValueAt("bad value", 0, DriftModel.DX);
		assertEquals("no event", 0, myListener.count);
		myModel.setValueAt(value, frameNumber, DriftModel.WEIGHT_X);
		assertEquals("no event", 0, myListener.count);
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
	
	private class DriftModelListener implements TableModelListener {
		public int count = 0;
		public int col;
		public int firstRow;
		public int lastRow;
		public int type;
		public Object targetContent;	//record content of first changed cell
		
		@Override
		public void tableChanged(TableModelEvent e) {
			DriftModel model = (DriftModel)e.getSource();
			count += 1;
			col = e.getColumn();
			firstRow = e.getFirstRow();
			lastRow = e.getLastRow();
			type = e.getType();
			if (firstRow != TableModelEvent.HEADER_ROW && col != TableModelEvent.ALL_COLUMNS) {
				targetContent = model.getValueAt(firstRow, col);
			}		
		}
		
		public void resetCount() {
			count = 0;
		}
		
	}

}
