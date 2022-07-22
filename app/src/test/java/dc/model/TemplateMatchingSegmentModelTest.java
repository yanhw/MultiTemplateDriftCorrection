package dc.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TemplateMatchingSegmentModelTest {
	private TemplateMatchingSegmentModel myModel;
	
	@Before
	public void init() {
		myModel = new TemplateMatchingSegmentModel();
	}
	
	@Test
	public void testInit() {
		int movieSize = 25;
		myModel.init(movieSize);
		assertEquals("1 row", 1, myModel.getRowCount());
		checkConsistancy();
		
		myModel.setEndFrame(movieSize/2);
		assertEquals("2 row", 2, myModel.getRowCount());
		myModel.init(movieSize);
		assertEquals("1 row", 1, myModel.getRowCount());
		checkConsistancy();
	}
	
	@Test
	public void testInitFail() {
		myModel.init(0);
		assertEquals("0 row", 0, myModel.getRowCount());
		checkConsistancy();
	}
	
	private void checkConsistancy() {
		int numSegment = myModel.getRowCount();
		for (int i = 0; i < numSegment; i++) {
			double[][] template = myModel.getTemplate(i);
			double[][] blurredTemplate = myModel.getBlurredTemplate(i);
			if ((boolean) myModel.getValueAt(i, TemplateMatchingSegmentModel.HAS_TEMPLATE_IDX)) {
				int height = template.length;
				int width = template[0].length;
				int top = ((Number)myModel.getValueAt(i, TemplateMatchingSegmentModel.TOP)).intValue();
				int bottom = ((Number)myModel.getValueAt(i, TemplateMatchingSegmentModel.BOTTOM)).intValue();
				int left = ((Number)myModel.getValueAt(i, TemplateMatchingSegmentModel.LEFT)).intValue();
				int right = ((Number)myModel.getValueAt(i, TemplateMatchingSegmentModel.RIGHT)).intValue();
				assertNotNull("has template", template);
				assertEquals("template height", height, bottom-top);
				assertEquals("template width", width, right-left);
				
				height = blurredTemplate.length;
				width = blurredTemplate[0].length;
				assertNotNull("has blurred template", blurredTemplate);
				assertEquals("blurred template height", height, bottom-top);
				assertEquals("blurred template width", width, right-left);
			} else {
				assertNull("no template", template);
				assertNull("no blurred template", blurredTemplate);
			}
		}
		assertNull("template idx out of bound", myModel.getTemplate(numSegment));
		assertNull("blurred template idx out of bound", myModel.getBlurredTemplate(numSegment));
//		assertThrows("blurred template idx out of bound", IndexOutOfBoundsException.class, ()->myModel.getBlurredTemplate(numSegment));
	}

}
