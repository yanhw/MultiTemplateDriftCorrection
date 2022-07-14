package dc.model;

import java.nio.file.Path;
import java.util.List;

import javax.swing.AbstractListModel;

@SuppressWarnings("serial")
public class FileListModel extends AbstractListModel<Path>{
	
	private List<Path> fileList;
	
	public FileListModel() {

	}
	
	public void setFiles(List<Path> fileList) {
		if (fileList == null || fileList.size() < 2) {
			return;
		}
		this.fileList = fileList;
		fireContentsChanged(fileList, 0, 0);
	}
	
	public void clearFiles() {
		fileList = null;
	}
	
	@Override
	public int getSize() {
		if (fileList == null) {
			return 0;
		}
		return fileList.size();
	}

	@Override
	public Path getElementAt(int index) {
		if (fileList == null || index < 0 || index >= fileList.size()) {
			return null;
		}
		return fileList.get(index);
	}

}
