package engine.easy.util;

import java.io.File;

public class Util {

	public static String getPath(String path) {
		return null;
	}

	public static String getFileExtension(File file) {
		if (file != null && file.isFile()) {
			Filename fileName = new Util.Filename(file.getPath(), '/', '.');
			return fileName.extension();
		}
		
		return "";
	}
	
	static class Filename {
		private String fullPath;
		private char pathSeparator, extensionSeparator;

		public Filename(String str, char sep, char ext) {
			fullPath = str;
			pathSeparator = sep;
			extensionSeparator = ext;
		}

		public String extension() {
			int dot = fullPath.lastIndexOf(extensionSeparator);
			return fullPath.substring(dot + 1);
		}

		public String filename() { // gets filename without extension
			int dot = fullPath.lastIndexOf(extensionSeparator);
			int sep = fullPath.lastIndexOf(pathSeparator);
			return fullPath.substring(sep + 1, dot);
		}

		public String path() {
			int sep = fullPath.lastIndexOf(pathSeparator);
			return fullPath.substring(0, sep);
		}
	}
}
