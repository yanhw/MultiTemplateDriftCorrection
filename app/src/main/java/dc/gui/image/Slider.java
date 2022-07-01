package dc.gui.image;

import java.awt.Component;
import java.awt.Dimension;
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
	private JButton nextBtn;
	private JButton prevBtn;
	private JSlider imageSlider;
	private JLabel sliderLabel;
	private BoundedRangeModel model;
	
	public Slider() {
        
        model = new DefaultBoundedRangeModel(0, 0, 0, 0);
        imageSlider = new JSlider(model);
        imageSlider.setPreferredSize(new Dimension(200, 10));
        imageSlider.setPaintTicks(true);
        imageSlider.setPaintLabels(false);
        imageSlider.setBorder(
        		BorderFactory.createEmptyBorder(0,0,10,0));
        Font font = new Font("Serif", Font.ITALIC, 15);
        imageSlider.setFont(font);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(200, 40));
        
        JPanel labelGroup = new JPanel();
        labelGroup.setLayout(new BoxLayout(labelGroup, BoxLayout.X_AXIS));
        sliderLabel = new JLabel("Frame: 0/0", JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelGroup.add(sliderLabel);
        
        add(labelGroup);
        prevBtn = new JButton("<");
//        prevBtn.setMaximumSize(new Dimension(10, 10));
//        prevBtn.setPreferredSize(new Dimension(10, 10));
        add(prevBtn);
        prevBtn.addActionListener(new PrevBtnListener());
        add(imageSlider);
        addChangeListener(this);
        nextBtn = new JButton(">");
        add(nextBtn);
        nextBtn.addActionListener(new NextBtnListener());
	}
	
	public void setMaximum(int numFrame) {
		model.setMaximum(numFrame-1);
	}
	
	public int getFrameNumber() {
		return model.getMaximum()+1;
	}
	
	public int getValue() {
		return model.getValue();
	}
	
	public void addChangeListener(ChangeListener listener) {
		imageSlider.addChangeListener(listener);
	}
	
//	public int getFrameNumber() {
//		return imageSlider.getValue();
//	}
	
	public void setFrameNumber(int frameNumber) {
		if (frameNumber < 0 || frameNumber >= model.getMaximum()) {
			return;
		}
		model.setValue(frameNumber);
//		sliderLabel.setText("Frame: "+(frameNumber)+"/"+model.getMaximum());
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
        	int frameNumber = (int)source.getValue();
        	sliderLabel.setText("Frame: "+(frameNumber)+"/"+model.getMaximum());
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
			if (current < model.getMaximum()) {
				Slider.this.imageSlider.setValue(current+1);
			}
		}
	}
}
