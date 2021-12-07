package dc.gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;

@SuppressWarnings("serial")
public class DriftEditingPanel extends JPanel {
	private JTable driftTable;
	private JTable driftSectionTable;

	/**
	 * Create the panel.
	 */
	public DriftEditingPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		add(splitPane, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(400, 200));
		scrollPane.setMaximumSize(new Dimension(400, 250));
		splitPane.setLeftComponent(scrollPane);
		
		driftTable = new JTable();
		scrollPane.setViewportView(driftTable);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setPreferredSize(new Dimension(400, 200));
		scrollPane_1.setMaximumSize(new Dimension(400, 250));
		splitPane.setRightComponent(scrollPane_1);
		
		driftSectionTable = new JTable();
		scrollPane_1.setRowHeaderView(driftSectionTable);
		splitPane.setDividerLocation(50.0);
		
		JPanel buttonPanel = new JPanel();
		add(buttonPanel, BorderLayout.EAST);
		
		JButton addCuttingPointButton = new JButton("Add Cutting Point");
		addCuttingPointButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		buttonPanel.setLayout(new GridLayout(8, 1, 0, 0));
		addCuttingPointButton.setToolTipText("set selected frame as a cutting point");
		buttonPanel.add(addCuttingPointButton);
		
		JButton removeCuttingPointButton = new JButton("New button");
		removeCuttingPointButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		buttonPanel.add(removeCuttingPointButton);
		
		JCheckBox fittingBox = new JCheckBox("use fitted drift");
		fittingBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		fittingBox.setSelected(true);
		fittingBox.setToolTipText("if selected, fitted drift will be used for drift correction. Otherwise, raw drift will be used.");
		buttonPanel.add(fittingBox);

	}

}
