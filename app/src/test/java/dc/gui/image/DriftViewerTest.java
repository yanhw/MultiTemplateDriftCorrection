package dc.gui.image;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DriftViewerTest {
	private DriftViewer myViewer;
	private String direction = "x";
	
	@Before
	public void init() {
		myViewer = new DriftViewer(direction);
	}
	
	@Test
	public void test() {
		myViewer.getSelectionModel();
		fail("Not yet implemented");
	}

}
