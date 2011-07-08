package android.hgd;

import java.io.File;

import android.util.Log;

public class FileBrowser
{
	private String currentPath = "/";
	private File[] currentPathFiles = {};
	
	public FileBrowser() {
		
	}
	
	public File[] listDirectory(File f) {
		if (!f.isDirectory()) {
			return null;
		}
		return f.listFiles();
	}
	
	//THings can be directories, but return null. perhaps if there are security issues.
	public File[] listDirectory() {
		Log.i("", currentPath);
		File f = new File(currentPath);
		Log.i("", ""+(f==null));
		Log.i("", ""+f.isAbsolute());
		Log.i("", ""+f.isDirectory());
		Log.i("", ""+(f.listFiles()==null));
		Log.i("", ""+(f.list()==null));
		Log.i("", ""+f.listFiles().length);
		return f.listFiles();
	}
	
	public boolean contains(File[] f, String match) {
		for (String s : toStringArray(f)) {
			if (s.equals(match)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean changeDirectory(String directory) {
		File[] listing = listDirectory(new File(currentPath));
		if (contains(listing, directory) && (new File(currentPath + "/" + directory).isDirectory())) {
			currentPath = currentPath + directory + "/";
			return true;
		}
		return false;
	}
	
	public static String[] toStringArray(File[] fs) {
		String[] res = new String[fs.length];
		for (int i = 0; i < fs.length; i++) {
			res[i] = fs[i].getName();
		}
		return res;
	}
	
	public boolean isValidToUpload(File f) {
		return (f.canRead() && f.exists() && f.isFile());
	}
}
