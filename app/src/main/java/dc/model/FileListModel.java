package dc.model;

import java.nio.file.Path;
import java.util.List;

import javax.swing.AbstractListModel;

/*
 * using abstract model for better control of listener behaver.
 * since the file list is not expected to change for the same movie,
 * the only expected event should be contentsChanged,
 * which implies the entire list is changed.
 */
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
		fireContentsChanged(this, 0, 0);
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
