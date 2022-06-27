package dc.gui_old;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import dc.controller.Controller;

@SuppressWarnings("serial")
public class TemplateMatchingPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(TemplateMatchingPanel.class.getName());
	private Controller controller;
	
	private JCheckBox blurBtn;
	
	private JButton addKeyFrameBtn;
	private JButton removeKeyFrameBtn;
	private JButton setBtn;
	private JButton removeBtn;
	private JButton runBtn;
	private JButton loadBtn;
//	private int selected;
	private JTable table;
	// TODO: shift this to other place
	DefaultTableModel model;
	
	public TemplateMatchingPanel() {
		
		JPanel optionGroup = new JPanel();
		optionGroup.setLayout(new BoxLayout(optionGroup, BoxLayout.Y_AXIS));
		blurBtn = new JCheckBox("blur image");
		blurBtn.setSelected(false);
		optionGroup.add(blurBtn);
		
		JPanel btnGroup = new JPanel();
		btnGroup.setLayout(new BoxLayout(btnGroup, BoxLayout.Y_AXIS));
		addKeyFrameBtn = new JButton("set as start of segment");
		addKeyFrameBtn.addActionListener(new AddKeyFrameBtnListener());
		removeKeyFrameBtn = new JButton("remove as start of segment");
		removeKeyFrameBtn.addActionListener(new RemoveKeyFrameBtnListener());
		setBtn = new JButton("set as template");
		setBtn.addActionListener(new AddBtnListener());
		removeBtn = new JButton("remove template");
		removeBtn.addActionListener(new RemoveBtnListener());
		runBtn = new JButton("start template matching");
		runBtn.addActionListener(new RunBtnListener());
		loadBtn = new JButton("load existing drift file");
		loadBtn.addActionListener(new LoadBtnListener());
		btnGroup.add(addKeyFrameBtn);
		btnGroup.add(removeKeyFrameBtn);
		btnGroup.add(setBtn);
		btnGroup.add(removeBtn);
		btnGroup.add(runBtn);
		btnGroup.add(loadBtn);
		
		model = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {       
				return false; // or a condition at your choice with row and column
			}
		};
		
		table = new JTable(model);
		table.setCellSelectionEnabled(true);  
		// don't do this. this disables selection, overwrite isCellEditable instead
//		table.setEnabled(false);	
        ListSelectionModel select= table.getSelectionModel();
        select.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        select.addListSelectionListener(new TableSelectionListener());  
		
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		// for table size
		// https://stackoverflow.com/questions/41629778/fix-height-of-jscrollpane
		scrollPane.setMaximumSize(new Dimension(400,250));
		scrollPane.setPreferredSize(new Dimension(400, 200));
		add(optionGroup);
		add(btnGroup);
		add(scrollPane);
	}

	protected void setController(Controller controller) {
		this.controller = controller;
		controller.setTemplateTableModel(model);
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}
	
	public void setRunBtnText(boolean flag) {
		if (flag) {
			runBtn.setText("start template matching");
		} else {
			runBtn.setText("stop template matching");
		}
	}
	
	//this is just for testing
	@SuppressWarnings("unused")
	private class TableSelectionListener implements ListSelectionListener {  
		public void valueChanged(ListSelectionEvent e) {  
			int Data = 0;  
			int[] row = table.getSelectedRows();  
			int[] columns = table.getSelectedColumns();  
			for (int i = 0; i < row.length; i++) {  
				for (int j = 0; j < columns.length; j++) {  
					Data = (int) table.getValueAt(row[i], columns[j]);  
				}
			}  
			logger.info("Table element selected is: " + Data);    
		}       
	}
	
	// add the frame currently displaying as the start of a segment
	private class AddKeyFrameBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			TemplateMatchingPanel.this.controller.setSegmentFrame();
		}
	}
	
	private class RemoveKeyFrameBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			
			int[] row = table.getSelectedRows(); 
			if (row.length == 1) {
				int targetSegment = row[0];
				TemplateMatchingPanel.logger.info("removing selected row " + targetSegment);
				TemplateMatchingPanel.this.controller.removeSegmentFrame(targetSegment);
			}
		}
	}
		
	private class AddBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			TemplateMatchingPanel.logger.info("setting template");
			TemplateMatchingPanel.this.controller.setTemplate();
		}
	}
	
	private class RemoveBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			int[] row = table.getSelectedRows(); 
			if (row.length == 1) {
				int targetSegment = row[0];
				TemplateMatchingPanel.logger.info("removing ROI from row " + targetSegment);
				TemplateMatchingPanel.this.controller.removeTemplate(targetSegment);
			}
		}
	}
	
	private class RunBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			boolean blur = TemplateMatchingPanel.this.blurBtn.isSelected();
			TemplateMatchingPanel.this.controller.runTemplateMatching(blur);
		}
	}
	
	private class LoadBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			JFileChooser fileChooser = new JFileChooser();
			// TODO: temp solution, need to change before formal version
			Path path = Paths.get("G:\\DriftCorrection\\app\\src\\test\\resources\\");
			if (Files.exists(path)) {
				fileChooser.setSelectedFile(new File("G:\\DriftCorrection\\app\\src\\test\\resources\\"));
			}
			else {
				fileChooser.setSelectedFile(new File("/gpfs0/scratch/utkur/"));
			}
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setDragEnabled(false);
			int returnVal = fileChooser.showOpenDialog(TemplateMatchingPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String file = fileChooser.getSelectedFile().getPath();
				
				TemplateMatchingPanel.this.controller.setDriftCsv(file);
			}	// end of APPROVAL_OPTION
			
		}
	}
}
