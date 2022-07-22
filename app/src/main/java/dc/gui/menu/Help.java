package dc.gui.menu;

import java.awt.Dialog;
import java.awt.Window;

import javax.swing.JDialog;

@SuppressWarnings("serial")
public class Help extends JDialog {
	
	public Help(Window parent) {
		super(parent, "\"Help\"", Dialog.ModalityType.MODELESS);
	}
}
