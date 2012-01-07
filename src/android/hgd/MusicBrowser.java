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
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;

public class MusicBrowser {
	
	public static final int NO_ACTION = 0;
	public static final int VALID_TO_UPLOAD = 1;
	public static final int DIRECTORY = 2;
	
	private String selectedArtist = null;
	private String selectedAlbum = null;
	private String selectedTitle = null;
	private String selectedPath = null;
	
	private static final String[] columns = {android.provider.MediaStore.Audio.Media._ID, android.provider.MediaStore.Audio.Media.TITLE, android.provider.MediaStore.Audio.Media.ALBUM, android.provider.MediaStore.Audio.Media.ARTIST};
	private static final String[] albumOnly = {"album"};//{android.provider.MediaStore.Audio.Media.ALBUM};
	private static final String[] artistOnly = {"artist"};//{android.provider.MediaStore.Audio.Media.ARTIST};
	private static final String[] titlesOnly = {"title"};//{android.provider.MediaStore.Audio.Media.TITLE};
	private static final String[] idOnly = {"_id"};//{android.provider.MediaStore.Audio.Media._ID};

	private ContentResolver cr;
	
	public MusicBrowser(ContentResolver contentResolver) {
		cr = contentResolver;
	}
	
	public long getID(String artist, String album, String title)
	{
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		if (album != null && artist != null && title != null) {
			String[] params = {artist, album, title};
			Cursor cursor = cr.query(uri, idOnly, "artist=? AND album=? AND title=?", params, "_id ASC");
			if (cursor == null) {
				return 0;
			}
			else if (!cursor.moveToFirst()) {
				return 0;
			}
			else {
				return cursor.getLong(cursor.getColumnIndex("_id"));
			}
		}
		return 0;
	}
	
	public String getPath(String artist, String album, String title)
	{
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		if (album != null && artist != null && title != null) {
			String[] params = {artist, album, title};
			Cursor cursor = cr.query(uri, new String[] {"_data"}, "artist=? AND album=? AND title=?", params, "_data ASC");
			if (cursor == null) {
				return "";
			}
			else if (!cursor.moveToFirst()) {
				return "";
			}
			else {
				return cursor.getString(cursor.getColumnIndex("_data"));
			}
		}
		return "";
	}
	
	public ArrayList<String> queryMusicAPI(String artist, String album) {
		ArrayList<String> results = new ArrayList<String>();
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		if (album != null && artist != null) {
			results.add("..");
			String[] params = {artist, album};
			Cursor cursor = cr.query(uri, titlesOnly, "artist=? AND album=?", params, "title ASC");
			int test = cursor.getCount();
			if (cursor == null) {
				return results;
			}
			else if (!cursor.moveToFirst()) {
				return results;
			}
			else {
				do {
					String res = cursor.getString(cursor.getColumnIndex("title"));
					if (!(results.contains(res))) {
						results.add(res);
					}
				} while (cursor.moveToNext());
			}
		}
		else if (artist != null) {
			results.add("..");
			String[] params = {artist};
			Cursor cursor = cr.query(uri, albumOnly, "artist=?", params, "album ASC");
			int test = cursor.getCount();
			if (cursor == null) {
				return results;
			}
			else if (!cursor.moveToFirst()) {
				return results;
			}
			else {
				do {
					String res = cursor.getString(cursor.getColumnIndex("album"));
					if (!(results.contains(res))) {
						results.add(res);
					}
				} while (cursor.moveToNext());
			}
		}
		else {
			Cursor cursor = cr.query(uri, artistOnly, null, null, "artist ASC");
			if (cursor == null) {
				return results;
			}
			else if (!cursor.moveToFirst()) {
				return results;
			}
			else {
				do {
					String res = cursor.getString(cursor.getColumnIndex("artist"));
					if (!(results.contains(res))) {
						results.add(res);
					}
				} while (cursor.moveToNext());
			}
		}
		return results;
	}
	
	/**
	 * Return a list to display to the user.
	 * 
	 * If we are currently at the top level, display all artists.
	 * If we have selected an artist, display all albums.
	 * If we have selected an album, display all songs.
	 * 
	 * We assume that selectedAlbum can only be set if selectedArtist is not null.
	 * 
	 * Prepend a ".." to mean go back.
	 * 
	 * @return The contents of the current directory.
	 */
	public String[] getFilelist() {
		ArrayList<String> list = queryMusicAPI(selectedArtist, selectedAlbum);
		return list.toArray(new String[0]);
	}
	//handle .. click
	public int update(String clicked) {
		if (selectedAlbum != null && selectedArtist != null) {
			if (clicked.equals("..")) {
				selectedAlbum = null;
				return DIRECTORY;
			}
			selectedTitle = clicked;
			selectedPath = getPath(selectedArtist, selectedAlbum, selectedTitle); //ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, getID(selectedArtist, selectedAlbum, selectedTitle)).;
			return VALID_TO_UPLOAD;
		}
		else if (selectedArtist != null) {
			if (clicked.equals("..")) {
				selectedArtist = null;
				return DIRECTORY;
			}
			selectedAlbum = clicked;
			return DIRECTORY;
		}
		else {
			selectedArtist = clicked;
			return DIRECTORY;
		}
	}

	public String getPathToFile() {
		//if null, could cause problems
		return selectedPath;
	}
}
