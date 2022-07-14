package dc.controller;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import dc.model.DriftModel;
import dc.model.DriftSectionModel;


public class DriftManagerTest {
	private DriftManager myManager;
	private DriftModel myDrift;
	private DriftSectionModel mySection;
	
	@Before
	public void init() {
		myManager = new DriftManager();
		myDrift = new DriftModel();
		mySection = new DriftSectionModel();
		myManager.setTableModel(myDrift, mySection);
	}
	
	@Test
	public void testInit() {
		int numFrame = 10;
		myManager.init(numFrame);
		assertFalse("drift not ready", myManager.isDriftReady());
	}

}
