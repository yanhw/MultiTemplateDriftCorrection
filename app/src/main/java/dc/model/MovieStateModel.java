package dc.model;

import javax.swing.DefaultBoundedRangeModel;

@SuppressWarnings("serial")
public class MovieStateModel extends DefaultBoundedRangeModel{
	public static final int INIT = 0;
	public static final int TEMPLATE_MATCHING = 1;
	public static final int DRIFT_EDIT = 2;
	public static final int DRIFT_CORRECTION = 3;
	
	public MovieStateModel() {
		super(INIT,1,INIT,DRIFT_CORRECTION+1);	//value, extent, min, max
	}
}
