package dc.utils;

import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.io.IOException;

// static methods that handles file system operations
public class FileSystem {
	
	public static List<Path> getFiles(Path movieDir, String format) {
		 List<Path> fileWithName = null;
		 try {
		 fileWithName = Files.walk(movieDir)
            .filter(s -> s.toString().endsWith(format))
            .map(Path::toAbsolutePath).sorted().collect(Collectors.toList());
		} catch(IOException e) {
			
		}
		for (Path name: fileWithName) {
			if (Files.isDirectory(name)) {
//				System.out.println("invalid name: " + name);
				fileWithName.remove(name);
//				System.out.println("removed");
			}
		}
		return fileWithName;
	}
	
	
	public static String findCommomDir(String srcDirList[]) {
		int numDir = srcDirList.length;
		if (numDir == 0) {
			return null;
		}
		//~ if (numDir == 1) {
			//~ return srcDirList[0];
		//~ }
		String commonDir = srcDirList[0];
		for (int i = 1; i < numDir; i++) {
			commonDir = greatestCommonPrefix(commonDir, srcDirList[i]);
		}
		// make sure commonDir is actually a parent dir
		
		return commonDir;
	}
	
	public static String joinPath(String rootString, String subString) {
		Path root = Paths.get(rootString);
		Path full = root.resolve(subString);
		return full.toString();
	}
	
	private static String greatestCommonPrefix(String a, String b) {
		int minLength = Math.min(a.length(), b.length());
		for (int i = 0; i < minLength; i++) {
			if (a.charAt(i) != b.charAt(i)) {
				return a.substring(0, i);
			}
		}
		return a.substring(0, minLength);
	}
}
