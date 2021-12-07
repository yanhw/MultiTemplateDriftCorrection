package dc.gui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import java.awt.Dimension;
import java.awt.BorderLayout;

@SuppressWarnings("serial")
public class TemplateMatchingPanel extends JPanel {
	private JTable table;

	/**
	 * Create the panel.
	 */
	public TemplateMatchingPanel() {
		setMaximumSize(new Dimension(32767, 400));
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(400, 200));
		scrollPane.setMaximumSize(new Dimension(400, 250));
		add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.EAST);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JButton setTemplateButton = new JButton("Set Template");
		setTemplateButton.setToolTipText("set selected region as template");
		panel.add(setTemplateButton);
		
		JButton removeTemplateButton = new JButton("Remove Template");
		panel.add(removeTemplateButton);
		
		JButton setSectionButton = new JButton("Set Section");
		setSectionButton.setToolTipText("Set the current frame as the start of a section. Each section uses a different template for template matching");
		panel.add(setSectionButton);
		
		JButton removeSectionButton = new JButton("Remove Section");
		removeSectionButton.setToolTipText("Remove the selected row as a section. The removed section will be merged with previous section");
		panel.add(removeSectionButton);
		
		JSeparator separator = new JSeparator();
		panel.add(separator);
		
		JCheckBox blurCheckBox = new JCheckBox("Blur Image");
		blurCheckBox.setToolTipText("use Guassian blur for template matching");
		panel.add(blurCheckBox);
		
		JButton runButton = new JButton("Run");
		runButton.setEnabled(false);
		runButton.setToolTipText("Must set template for all sections first!");
		panel.add(runButton);
		
		JButton loadDriftButton = new JButton("Load Drift");
		loadDriftButton.setToolTipText("Load drift infomation from existing csv file");
		panel.add(loadDriftButton);

	}

}
