package dc.gui.image;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;  
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.ChartEntity;
//import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
//import org.jfree.data.xy.XYDataItem;
//import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import dc.gui.Synchroniser;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
//import javax.swing.UIManager;


//https://zetcode.com/java/jfreechart/
@SuppressWarnings("serial")
public class DriftViewer extends JPanel {
	private static final Logger logger = Logger.getLogger(DriftViewer.class.getName());
	private XYSeries rawDrift;
	private XYSeries fittedDrift;
	private Synchroniser mySynchroniser;
	
	private int[] idxList;
	public DriftViewer() {
        rawDrift = new XYSeries("raw drift");
        fittedDrift = new XYSeries("fitted drift");
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(rawDrift);
        dataset.addSeries(fittedDrift);
        
        // Create chart  
        JFreeChart chart = ChartFactory.createXYLineChart(  
            "", // Chart title  
            "Frame", // X-Axis Label  
            "Detected Drift (pixel)", // Y-Axis Label  
            dataset, PlotOrientation.VERTICAL, true, true, false  
            );  
        XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesPaint(1, Color.RED);
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinesVisible(false);
        plot.setDomainGridlinesVisible(false);

        chart.getLegend().setFrame(BlockBorder.NONE);
        ChartPanel panel = new ChartPanel(chart);
        panel.addChartMouseListener(new MyChartMouseListener());
        
        this.setLayout(new BorderLayout());
        add(panel);
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	protected void setSynchroniser(Synchroniser synchroniser) {
		if (synchroniser == null) {
			return;
		}
		mySynchroniser = synchroniser;
	}
	
	protected void setData(int[] xList, float[] xFittedList) {
		assert (xList.length == xFittedList.length);
		int numFrame = xList.length;
		
		rawDrift.clear();
		fittedDrift.clear();
		for (int i = 0; i < numFrame; i++) {
			rawDrift.add(i, xList[i]);
			fittedDrift.add(i, xFittedList[i]);
		}
		
		this.idxList = new int[xList.length];
		for (int i = 0; i < xList.length; i++) {
			this.idxList[i] = i;
		}
	}
	
	//https://www.jfree.org/forum/viewtopic.php?t=27383
	private class MyChartMouseListener implements ChartMouseListener {

		@Override
		public void chartMouseClicked(ChartMouseEvent event) {
			// TODO Auto-generated method stub
			ChartEntity entity = event.getEntity();
			if (entity == null) {
				return;
			}
			if (entity instanceof XYItemEntity) {
				// Get entity details
	//			String tooltip = ((XYItemEntity)entity).getToolTipText();
//				XYDataset dataset = ((XYItemEntity)entity).getDataset();
//				int seriesIndex = ((XYItemEntity)entity).getSeriesIndex();
				int item = ((XYItemEntity)entity).getItem();

				// You have the dataset the data point belongs to, the index of the series in that dataset of the data point, and the specific item index in the series of the data point.
//				XYSeries series = ((XYSeriesCollection)dataset).getSeries(seriesIndex);
//				XYDataItem xyItem = series.getDataItem(item);	
//				System.out.println(item);
				if (mySynchroniser != null) {
					logger.info("selected frame: " + item + " on drift plot.");
					mySynchroniser.driftPlotPointSelected(item);
				}
			}
			
		}

		@Override
		public void chartMouseMoved(ChartMouseEvent event) {
			// TODO Auto-generated method stub
			
		}
		
	}

	// for testing
	private void createDataset() {   
		  int[] xList = new int[100];
		  float[] yList = new float[100];
		  for (int i = 0; i < 100; i++) {
			  xList[i] = i;
			  yList[i] = i*i-10;
		  }
		  setData(xList, yList);
	}

	
	// for testing
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("PlotTest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DriftViewer imageViewer = new DriftViewer();
        imageViewer.createDataset();
                
        //Add content to the window.
        frame.add(imageViewer, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setSize(800,400);
        frame.setLocation(200,200);
        frame.setVisible(true);
//        imageViewer.updatePicture(0); 
        
    }
    
    // for testing
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
