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

import dc.model.DriftModel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
//import javax.swing.UIManager;


//https://zetcode.com/java/jfreechart/
@SuppressWarnings("serial")
public class DriftViewer extends JPanel {
	private static final Logger logger = Logger.getLogger(DriftViewer.class.getName());
	private XYSeries rawDrift;
	private XYSeries fittedDrift;
	
	private int rawIdx;
	private int fittedIdx;
	
	private DefaultListSelectionModel selection;
	
	public DriftViewer(String direction) {
		if (direction.equals("x")) {
			rawIdx = DriftModel.DX;
			fittedIdx = DriftModel.FITTED_DX;
		} else {
			rawIdx = DriftModel.DY;
			fittedIdx = DriftModel.FITTED_DY;
		}
		
        rawDrift = new XYSeries("raw drift");
        fittedDrift = new XYSeries("fitted drift");
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(rawDrift);
        dataset.addSeries(fittedDrift);
        
        selection = new DefaultListSelectionModel();
        
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
	
	protected void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	
	protected void setDriftModelListener(DriftModel model) {
		model.addTableModelListener(new DriftModelListener());
	}
	
	protected DefaultListSelectionModel getSelectionModel() {
		return selection;
	}
	
	private class DriftModelListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent e) {
			DriftModel model = (DriftModel)e.getSource();
			int col = e.getColumn();
			if (col == rawIdx || col == TableModelEvent.ALL_COLUMNS) {
				switch (e.getType()) {
				
					case TableModelEvent.INSERT:
						int start = e.getFirstRow();
						int end = e.getLastRow();
						for (int i = start; i <= end; i++) {
							rawDrift.add((double)i, (Number) model.getValueAt(i, rawIdx));
						}
						break;
					case TableModelEvent.DELETE:
						rawDrift.clear();
						break;
					case TableModelEvent.UPDATE:
						int start1 = e.getFirstRow();
						int end1 = e.getLastRow();
						rawDrift.delete(start1, end1);
						
						for (int i = start1; i <= end1; i++) {
//							rawDrift.remove((Number) i);
							rawDrift.add((double)i, (Number) model.getValueAt(i, rawIdx));
						}
						break;
				}
			}
			if (col == fittedIdx || col == TableModelEvent.ALL_COLUMNS) {
				switch (e.getType()) {
					case TableModelEvent.INSERT:
						int start = e.getFirstRow();
						int end = e.getLastRow();
						for (int i = start; i <= end; i++) {
//							logger.info("add " + i + " " + model.getValueAt(i, fittedIdx));
							fittedDrift.add(i, (Number) model.getValueAt(i, fittedIdx));
						}
						break;
					case TableModelEvent.DELETE:
						fittedDrift.clear();
						break;
					case TableModelEvent.UPDATE:
						int start1 = e.getFirstRow();
						int end1 = e.getLastRow();
						logger.info("delete " + start1 + " " + end1);
						fittedDrift.delete(start1, end1);
						for (int i = start1; i <= end1; i++) {
//							logger.info("update " + i + " " + model.getValueAt(i, fittedIdx));
//							fittedDrift.remove((Number) i);
							fittedDrift.add(i, (Number) model.getValueAt(i, fittedIdx));
						}
						break;
				}
			}
		}
		
	}
	
	//https://www.jfree.org/forum/viewtopic.php?t=27383
	private class MyChartMouseListener implements ChartMouseListener {

		@Override
		public void chartMouseClicked(ChartMouseEvent event) {
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
				logger.info("selected frame: " + item + " on drift plot.");
				selection.setLeadSelectionIndex(item);
			}
			
		}

		@Override
		public void chartMouseMoved(ChartMouseEvent event) {
		}
		
	}
	
	
	// getters for debugging
	@Deprecated
	protected XYSeries getRawDrift() {
		return rawDrift;
	}
	@Deprecated
	protected XYSeries getFittedDrift() {
		return fittedDrift;
	}
}
