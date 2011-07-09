/*
 * Copyright 2011  Matthew Mole <code@gairne.co.uk>, Carlos Eduardo da Silva <kaduardo@gmail.com>
 * 
 * This file is part of ahgdc.
 * 
 * ahgdc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ahgdc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ahgdc.  If not, see <http://www.gnu.org/licenses/>.
 */

package android.hgd;

import java.io.File;

import android.util.Log;

public class FileBrowser
{
	public String currentPath = "/";
	private File[] currentPathFiles = {};
	
	public FileBrowser() {
		
	}
	
	public File[] listDirectory(File f) {
		if (!f.isDirectory()) {
			return null;
		}
		return f.listFiles();
	}
	
	/**
	 * Displays the file browser widgit and prompts the user to select a file.
	 * Returns the empty string if the user cancels.
	 * 
	 * @return "" if cancelled, full pathname to file on success.
	 */
	public String chooseFile() {
		return "/mnt/sdcard/downloads/bluetooth/Baby.mp3";
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
