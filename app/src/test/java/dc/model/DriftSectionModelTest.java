package dc.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DriftSectionModelTest {
	private DriftSectionModel myModel;
	
	@Before
	public void init() {
		myModel = new DriftSectionModel();
	}
	
	@Test
	public void testInitNormal() {
		int frameNumber = 100;
		myModel.initData(frameNumber);
		assertEquals("number of sections", 1, myModel.getRowCount());
		assertEquals("starting frame", 0, myModel.getValueAt(0, DriftSectionModel.START));
		assertEquals("ending frame", frameNumber-1, myModel.getValueAt(0, DriftSectionModel.END));
		assertEquals("default degree", DriftSectionModel.DEFAULTFITTINGDEGREE, myModel.getValueAt(0, DriftSectionModel.DEGREE));
		assertTrue("fit flag", (boolean)myModel.getValueAt(0, DriftSectionModel.FIT));
	}
	
	@Test
	public void testInitNegativeFrameNumber() {
		myModel.initData(-1);
		assertEquals("failed init", 0, myModel.getRowCount());
		
		int positive = 10;
		myModel.initData(positive);
		myModel.initData(-1);
		assertEquals("failed init", 1, myModel.getRowCount());
	}
	
	@Test
	public void testInitNewMovie() {
		int oldFrameNumber = 50;
		myModel.initData(oldFrameNumber);
		int frameNumber = 100;
		myModel.initData(frameNumber);
		assertEquals("number of sections", 1, myModel.getRowCount());
		assertEquals("starting frame", 0, myModel.getValueAt(0, DriftSectionModel.START));
		assertEquals("ending frame", frameNumber-1, myModel.getValueAt(0, DriftSectionModel.END));
		assertEquals("default degree", DriftSectionModel.DEFAULTFITTINGDEGREE, myModel.getValueAt(0, DriftSectionModel.DEGREE));
		assertTrue("fit flag", (boolean)myModel.getValueAt(0, DriftSectionModel.FIT));
	}
	
	@Test
	public void testIsEndFrame() {
		int frameNumber = 100;
		int sectionEnd = 50;
		
		assertFalse("no end frame", myModel.isEndFrame(frameNumber));
		myModel.initData(frameNumber);
		assertTrue("true end frame", myModel.isEndFrame(frameNumber-1));
		assertFalse("not end frame", myModel.isEndFrame(frameNumber));
		
		myModel.setEndFrame(sectionEnd);
		assertTrue("new end frame", myModel.isEndFrame(sectionEnd));
		myModel.removeEndFrame(1);
		assertFalse("not end frame", myModel.isEndFrame(sectionEnd));
	}
	
	@Test
	public void testgetRowNumber() {
		int frameNumber = 100;
		myModel.initData(frameNumber);
		assertEquals("first section", 0, myModel.getRowNumber(1));
		assertEquals("bad frame number", -1, myModel.getRowNumber(-1));
		assertEquals("bad frame number", -1, myModel.getRowNumber(100));
		assertEquals("bad frame number", -1, myModel.getRowNumber(1000));
		int sectionEnd = 50;
		myModel.setEndFrame(sectionEnd);
		assertEquals("first section", 0, myModel.getRowNumber(1));
		assertEquals("first section", 0, myModel.getRowNumber(sectionEnd));
		assertEquals("first section", 1, myModel.getRowNumber(sectionEnd+1));
	}
	
	@Test
	public void testgetRowNumberEmptyMovie() {
		assertEquals("un-inited movie", -1, myModel.getRowNumber(0));
		assertEquals("un-inited movie", -1, myModel.getRowNumber(1));
	}
	
	@Test
	public void testSetEndFrame() {
		int frameNumber = 100;
		myModel.initData(frameNumber);
		int sectionEnd = 50;
		assertFalse("not end frame", myModel.isEndFrame(sectionEnd));
		myModel.setEndFrame(sectionEnd);
		assertTrue("new end frame", myModel.isEndFrame(sectionEnd));
		assertEquals("number of sections", 2, myModel.getRowCount());
		assertEquals("starting frame", sectionEnd, myModel.getValueAt(1, DriftSectionModel.START));
		assertEquals("ending frame", sectionEnd, myModel.getValueAt(0, DriftSectionModel.END));
		
		// these should fail to set, hence number of sections remains at 2
		myModel.setEndFrame(0);
		assertEquals("number of sections", 2, myModel.getRowCount());
		myModel.setEndFrame(sectionEnd);
		assertEquals("number of sections", 2, myModel.getRowCount());
		myModel.setEndFrame(frameNumber-2);
		assertEquals("number of sections", 2, myModel.getRowCount());
		myModel.setEndFrame(frameNumber-1);
		assertEquals("number of sections", 2, myModel.getRowCount());
	}
	
	@Test
	public void testRemoveEndFrame() {
		int frameNumber = 100;
		myModel.initData(frameNumber);
		myModel.removeEndFrame(0);
		assertEquals("number of sections", 1, myModel.getRowCount());
		int sectionEnd = 50;
		myModel.setEndFrame(sectionEnd);
		myModel.removeEndFrame(0);
		assertEquals("number of sections", 2, myModel.getRowCount());
		myModel.removeEndFrame(3);
		assertEquals("number of sections", 2, myModel.getRowCount());
		myModel.removeEndFrame(1);
		assertEquals("number of sections", 1, myModel.getRowCount());
	}
}
