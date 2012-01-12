/*
 * Copyright 2012  Matthew Mole <code@gairne.co.uk>, Carlos Eduardo da Silva <kaduardo@gmail.com>
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

public class FileBrowser extends Browser
{
	private String currentPath = "/";
	private String lastClicked;
	
	/**
	 * Return a list of all the files and directories in the current directory (currentPath).
	 * Only the names are added to the string array, not the full path.
	 * Prepend a parent directory (..) to the top of the list.
	 * 
	 * @return The contents of the current directory.
	 */
	@Override
	public String[] getFilelist() {
		File[] files = (new File(currentPath)).listFiles();
		if (files == null) {
			
		}
		String[] res = new String[(files.length+1)];
		res[0] = "..";
		
		for (int i = 1; i <= files.length; i++) {
			res[i] = files[i-1].getName();
		}
		
		return res;
	}
	
	@Override
	public void reset() {
		currentPath = "/";
	}
	
	@Override
	public String getPath() {
		return lastClicked;
	}
	
	public boolean isValidToUpload(File f) {
		return (f.canRead() && f.exists() && f.isFile());
	}
	
	@Override
	public int update(String itemClicked) {
		String fullPath = currentPath + "/" + itemClicked;
		if (itemClicked.equals("..")) {
			String parent = (new File(currentPath)).getParent();
			if (parent == null) {
				return NO_ACTION;
			}
			if (new File(parent) != null) {
				currentPath = parent + "/";
				return DIRECTORY;
			}
		}
		if ((new File(fullPath)) == null || ((new File(fullPath)).isDirectory() && (new File(fullPath)).listFiles() == null)) {
			return NO_ACTION;
		}
		else if ((new File(fullPath)).isDirectory()) {
			currentPath = currentPath + itemClicked + "/";
			return DIRECTORY;
		}
		else if (isValidToUpload(new File(fullPath))) {
			lastClicked = currentPath + "/" + itemClicked;
			return VALID_TO_UPLOAD;
		}
		return NO_ACTION;
	}

}
