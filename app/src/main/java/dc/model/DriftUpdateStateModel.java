package dc.model;

import javax.swing.DefaultBoundedRangeModel;

@SuppressWarnings("serial")
public class DriftUpdateStateModel extends DefaultBoundedRangeModel {
	public static final int OK = 0;
	public static final int UPDATING = 1;
	public static final int NEED_CHECK = 2;
	
	public DriftUpdateStateModel() {
		super(OK,1,OK,NEED_CHECK+1);	//value, extent, min, max
	}
}
