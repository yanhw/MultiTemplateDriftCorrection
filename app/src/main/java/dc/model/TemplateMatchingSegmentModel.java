package dc.model;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class TemplateMatchingSegmentModel extends DefaultTableModel{
	private static final Logger logger = Logger.getLogger(TemplateMatchingSegmentModel.class.getName());
	
	public static final int IDX_IDX = 0;
	public static final int START_IDX = 1;
	public static final int END_IDX = 2;
	public static final int KEY_IDX = 3;
	public static final int HAS_TEMPLATE_IDX = 4;
	public static final int TOP = 5;
	public static final int BOTTOM = 6;
	public static final int LEFT = 7;
	public static final int RIGHT = 8;
	private static final int ROI_TOP = 0;
	private static final int ROI_BOTTOM = 1;
	private static final int ROI_LEFT = 2;
	private static final int ROI_RIGHT = 3;
	
	private List<double[][]> templates = new LinkedList<double[][]>();
	private List<double[][]> blurredTemplates = new LinkedList<double[][]>();
	
	public TemplateMatchingSegmentModel() {
		addColumn("index");
		addColumn("frist frame");
		addColumn("last frame");
		addColumn("template frame");
		addColumn("has template");
		
		addColumn("top");
		addColumn("bottom");
		addColumn("left");
		addColumn("right");
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public void init(int movieSize) {
		setRowCount(0);
		templates.clear();
		blurredTemplates.clear();
		addRow(new Object[] {1, 0, movieSize-1, 0, false, 0,0,0,0});
		templates.add(null);
		blurredTemplates.add(null);
	}
	
	public boolean isEndFrame(int frameNumber) {
		for (int i = 0; i < getRowCount(); i++) {
			if ((int)getValueAt(i, END_IDX) == frameNumber) {
				return true;
			}
		}
		return false;
	}
	
	private int getMovieSize() {
		return (int)getValueAt(getRowCount()-1, END_IDX);
	}
	
	private int getRowNumber(int frameNumber) {
		assert frameNumber >= 0;
		assert frameNumber < getMovieSize();
		int i;
		for (i=0; i < getRowCount(); i++) {
			if ((int)getValueAt(i, END_IDX) > frameNumber) {
				return i;
			}
		}
		assert false;
		return i;
	}
	
	public void setEndFrame(int frameNumber) {
		assert !isEndFrame(frameNumber);
		if (frameNumber <= 0) {
			return;
		}
		// the last segment must have at least 2 frames
		if (frameNumber >= getMovieSize()-1) {
			return;
		}
		int targetIdx = getRowNumber(frameNumber);
		int ending = (int)getValueAt(targetIdx, END_IDX);
		setValueAt(frameNumber, targetIdx, END_IDX);
		insertRow(targetIdx+1, new Object[] {targetIdx+2, frameNumber, ending, 0, false, 0,0,0,0});
		templates.add(targetIdx+1, null);
		blurredTemplates.add(targetIdx+1, null);
		if ((boolean)getValueAt(targetIdx, HAS_TEMPLATE_IDX)) {
			int ROIFrame = (int)getValueAt(targetIdx, KEY_IDX);
			if (ROIFrame >= frameNumber) {
				int[] ROI = {(int)getValueAt(targetIdx, TOP), (int)getValueAt(targetIdx, BOTTOM),
						(int)getValueAt(targetIdx, LEFT), (int)getValueAt(targetIdx, RIGHT)};
				setROI((int)getValueAt(targetIdx, KEY_IDX), ROI, templates.get(targetIdx), blurredTemplates.get(targetIdx));
				removeROI(targetIdx);
			}
		}
		logger.info("set end frame at: " + frameNumber);
		assert (getRowCount() == templates.size());
		assert (getRowCount() == blurredTemplates.size());
	}
	
	public void removeEndFrame(int segmentIndex) {
		// first segment cannot be removed
		if (segmentIndex == 0) {
			return;
		}
		if (segmentIndex >= getRowCount()) {
			return;
		}
		int prevIndex = segmentIndex-1;
		int ending = (int)getValueAt(segmentIndex, END_IDX);
		setValueAt(ending, prevIndex, END_IDX);
		if (!(boolean)getValueAt(prevIndex, HAS_TEMPLATE_IDX) && (boolean)getValueAt(segmentIndex, HAS_TEMPLATE_IDX)) {
			int[] ROI = {(int)getValueAt(segmentIndex, TOP), (int)getValueAt(segmentIndex, BOTTOM),
					(int)getValueAt(segmentIndex, LEFT), (int)getValueAt(segmentIndex, RIGHT)};
			setROI((int)getValueAt(segmentIndex, KEY_IDX), ROI, templates.get(segmentIndex), blurredTemplates.get(segmentIndex));
		}
		removeRow(segmentIndex);
		templates.remove(segmentIndex);
		blurredTemplates.remove(segmentIndex);
		assert (getRowCount() == templates.size());
		assert (getRowCount() == blurredTemplates.size());
	}
	
	public void setROI(int frameNumber, int ROI[], double[][] image, double[][] blurredTemplate) {
		int segmentIndex = getRowNumber(frameNumber);
		
		setValueAt(true, segmentIndex, HAS_TEMPLATE_IDX);
		setValueAt(frameNumber, segmentIndex, KEY_IDX);
		setValueAt(ROI[ROI_TOP], segmentIndex, TOP);
		setValueAt(ROI[ROI_BOTTOM], segmentIndex, BOTTOM);
		setValueAt(ROI[ROI_LEFT], segmentIndex, LEFT);
		setValueAt(ROI[ROI_RIGHT], segmentIndex, RIGHT);
		templates.set(segmentIndex, image);
		blurredTemplates.set(segmentIndex, blurredTemplate);
	}
	
	public void removeROI(int segmentIndex) {
		if (segmentIndex >= getRowCount()) {
			return;
		}
		setValueAt(false, segmentIndex, HAS_TEMPLATE_IDX);
		templates.set(segmentIndex, null);
		blurredTemplates.set(segmentIndex, null);
	}
	
	public double[][] getTemplate(int idx) {
		if (idx < 0 || idx >= templates.size()) {
			logger.info("invalid index: " + idx);
			return null;
		}
		return templates.get(idx);
	}
	
	public double[][] getBlurredTemplate(int idx) {
		if (idx < 0 || idx >= blurredTemplates.size()) {
			logger.info("invalid index: " + idx);
			return null;
		}
		return blurredTemplates.get(idx);
	}

	public boolean isReady() {
		for (int i = 0; i < getRowCount(); i++) {
			if (!(boolean)getValueAt(i, HAS_TEMPLATE_IDX)) {
				return false;
			}
		}
		return true;
	}
}
