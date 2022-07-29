package dc.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.LayerUI;

@SuppressWarnings("serial")
public class DnDLayerUI extends LayerUI<JPanel> {
	private boolean isDragging = false;
	
	public void setIsDragging(boolean flag) {
		isDragging = flag;
	}
	
	@Override
	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);
		if (isDragging) {
			Graphics2D g2 = (Graphics2D) g.create();
			int w = c.getWidth();
			int h = c.getHeight();
			g2.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, .3f));
			g2.setColor(Color.YELLOW);
			g2.fillRect(0, 0, w, h);
			g2.dispose();
		}
		
	}
}
