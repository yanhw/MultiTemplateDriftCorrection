package DriftCorrection.gui.image;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class Slider extends JPanel implements ChangeListener {
	private int numFrame=2;
	private JButton nextBtn;
	private JButton prevBtn;
	private JSlider imageSlider;
	private JLabel sliderLabel;
	private BoundedRangeModel model;
	
	public Slider() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		JPanel labelGroup = new JPanel();
		labelGroup.setLayout(new BoxLayout(labelGroup, BoxLayout.X_AXIS));
		prevBtn = new JButton("previous frame");
		prevBtn.addActionListener(new PrevBtnListener());
		sliderLabel = new JLabel("Frame: 0/"+numFrame, JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextBtn = new JButton("next frame");
        nextBtn.addActionListener(new NextBtnListener());
        labelGroup.add(prevBtn);
        labelGroup.add(sliderLabel);
        labelGroup.add(nextBtn);
        
        model = new DefaultBoundedRangeModel(0, 1, 0, numFrame);
        imageSlider = new JSlider(model);
        imageSlider.setPaintTicks(true);
        imageSlider.setPaintLabels(false);
        imageSlider.setBorder(
        		BorderFactory.createEmptyBorder(0,0,10,0));
        Font font = new Font("Serif", Font.ITALIC, 15);
        imageSlider.setFont(font);
        
        add(labelGroup);
        add(imageSlider);
        addChangeListener(this);
	}
	
	public void setMaximum(int numFrame) {
		this.numFrame = numFrame;
		model.setMaximum(numFrame);
	}

	public void addChangeListener(ChangeListener listener) {
		imageSlider.addChangeListener(listener);
	}
	
	public int getFrameNumber() {
		return imageSlider.getValue();
	}
	
	public void setFrameNumber(int frameNumber) {
		if (frameNumber < 0 || frameNumber >= numFrame) {
			return;
		}
		model.setValue(frameNumber);
		sliderLabel.setText("Frame: "+(frameNumber)+"/"+numFrame);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
        	int frameNumber = (int)source.getValue();
        	sliderLabel.setText("Frame: "+(frameNumber)+"/"+numFrame);
        }
	}
	
	private class PrevBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			int current = Slider.this.imageSlider.getValue();
			if (current > 0) {
				Slider.this.imageSlider.setValue(current-1);
			}
		}
	}
	
	private class NextBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			int current = Slider.this.imageSlider.getValue();
			if (current < numFrame-1) {
				Slider.this.imageSlider.setValue(current+1);
			}
		}
	}
}
