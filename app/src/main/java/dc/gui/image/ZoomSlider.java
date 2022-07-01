package dc.gui.image;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.GridLayout;

/*
 * use together with ImagePanel
 * maxzoom: 10
 * minzoom 0.1
 * zoom steps: 0.1,0.2,0.5,1,2,5,10
 */

@SuppressWarnings("serial")
public class ZoomSlider extends JPanel implements ChangeListener {
	
	private JSlider slider;
	private JLabel sliderLabel;
	private BoundedRangeModel model;
	public static final double[] STEPS = {0.1,0.2,0.5,1.0,2.0,5.0,10.0};
	private int NUM_STEP = STEPS.length;
	private int defaultStep = 3;
	
	public ZoomSlider() {
		
		
		sliderLabel = new JLabel("Zoom level: "+STEPS[defaultStep], JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        setPreferredSize(new Dimension(200, 16));
        
        //Create the slider.
        model = new DefaultBoundedRangeModel(defaultStep, 1, 0, NUM_STEP-1);
        slider = new JSlider(model);
        
        slider.setPaintTicks(true);
        slider.setPaintLabels(false);
        slider.setBorder(
        		BorderFactory.createEmptyBorder(0,0,10,0));
        Font font = new Font("Serif", Font.ITALIC, 15);
        slider.setFont(font);
        setLayout(new GridLayout(0, 2, 0, 0));
        
        add(sliderLabel);
        add(slider);
        addChangeListener(this);
	}
	
	public void setMaximum(int numFrame) {
		this.NUM_STEP = numFrame;
		model.setMaximum(numFrame);
	}

	protected void addChangeListener(ChangeListener listener) {
		slider.addChangeListener(listener);
	}
	

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
        	int step = (int)source.getValue();
        	sliderLabel.setText("Zoom level: "+STEPS[step]);
        }
	}
}
