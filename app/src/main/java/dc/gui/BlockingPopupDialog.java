package dc.gui;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.FlowLayout;

// https://stackoverflow.com/questions/20113480/joptionpane-internal-dialog-without-title-bar-or-close-button
@SuppressWarnings("serial")
public class BlockingPopupDialog extends JDialog {
	
	private JLabel messageLabel = new JLabel("This is a message");

    public BlockingPopupDialog(String text) {
        super(new JFrame(), true);
        messageLabel.setText(text);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setModal(false);
        add(messageLabel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public void updateText(String text) {
    	messageLabel.setText(text);
    	revalidate();
    	repaint();
    }
}
