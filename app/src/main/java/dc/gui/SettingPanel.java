package dc.gui;

import javax.swing.JPanel;
import javax.swing.JLayeredPane;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;

public class SettingPanel extends JPanel {

	/**
	 * Create the panel.
	 */
	public SettingPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel bottomPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) bottomPanel.getLayout();
		flowLayout.setHgap(25);
		add(bottomPanel, BorderLayout.SOUTH);
		
		JButton prevButton = new JButton("<<");
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		bottomPanel.add(prevButton);
		
		JButton nextButton = new JButton(">>");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		bottomPanel.add(nextButton);
		
		JLayeredPane stepPanel = new JLayeredPane();
		add(stepPanel, BorderLayout.CENTER);
		stepPanel.setLayout(new BorderLayout(0, 0));
		
		TemplateMatchingPanel templateMatchingPanel = new TemplateMatchingPanel();
		stepPanel.add(templateMatchingPanel);
		
	}
}
