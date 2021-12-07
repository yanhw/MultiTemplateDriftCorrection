package dc.model;

/* both startFrame and endFrame are immutable
 * startFrame and endFrame are inclusive
 * endFrame for MovieSegment1 should be the same as startFrame for
 * MovieSegment2, i.e. there should be one frame of overlapping.
 * If need to modify segmentation of movie, new MovieSegment object 
 * should be created.
 * 
 * Note: input validation is expected to be done by controller!
 */
public class MovieSegment {
	private int startFrame;
	private int endFrame;
	private int ROIFrame = -1;
	private int[] ROI;
	private double[][] template;
	private double[][] blurredTemplate;
	private int[] xLoc;
	private int[] yLoc;
	private float[] xDrift;
	private float[] yDrift;
	
	public MovieSegment(int startFrame, int endFrame) {
		assert (startFrame >= 0);
		assert (endFrame > 0);
		assert (endFrame > startFrame);
		
		this.startFrame = startFrame;
		this.endFrame = endFrame;
		int numFrame = endFrame-startFrame+1;
		xLoc = new int[numFrame];
		yLoc = new int[numFrame];
		xDrift = new float[numFrame];
		yDrift = new float[numFrame];
	}
	
	protected void setROI(int ROIFrame, int ROI[], double[][] template, double[][] blurredTemplate) {
		assert (ROIFrame >= startFrame);
		assert (ROIFrame <= endFrame);
		assert (ROI != null);
		assert (ROI.length == 4);
		assert (template != null);
		
		this.ROIFrame = ROIFrame;
		this.ROI = ROI;
		this.template = template;
		this.blurredTemplate = blurredTemplate;
	}
	
	protected void removeROI() {
		this.ROIFrame = -1;
		this.ROI = null;
		this.template = null;
		this.blurredTemplate = null;
	}
	
	protected void setLocation(int frameNumber, int x, int y) {
		assert (ROIFrame != -1);
		assert (xLoc != null);
		assert (yLoc != null);
		xLoc[frameNumber-startFrame] = x;
		yLoc[frameNumber-startFrame] = y;
	}

	protected int getStartFrame() {
		return startFrame;
	}
	
	protected int getEndFrame() {
		return endFrame;
	}
	
	protected int getROIFrame() {
		return ROIFrame;
	}
	
	protected int[] getROI() {
		return ROI;
	}
	
	protected double[][] getTemplate() {
		assert(ROIFrame != -1);
		return template;
	}
	
	protected double[][] getBlurredTemplate() {
		assert(ROIFrame != -1);
		return blurredTemplate;
	}
	
	protected int[] getXLoc() {
		return xLoc;
	}
	
	protected int[] getYLoc() {
		return yLoc;
	}
	
	protected boolean isReady() {
		if (ROI == null) {
			return false;
		}
		return true;
	}
	
	protected void setXDrift(int frameIndex, float drift) {
		assert (frameIndex >= startFrame);
		assert (frameIndex <= endFrame);
		xDrift[frameIndex-startFrame] = drift;
	}
	
	protected void setYDrift(int frameIndex, float drift) {
		assert (frameIndex >= startFrame);
		assert (frameIndex <= endFrame);
		yDrift[frameIndex-startFrame] = drift;
	}
	
	protected float[] getXDrift() {
		return xDrift;
	}
	
	protected float[] getYDrift() {
		return yDrift;
	}

}