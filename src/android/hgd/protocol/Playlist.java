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

package android.hgd.protocol;

import java.util.ArrayList;

/**
 * A class representing a Playlist AT THE POINT OF INSTANTIATION.
 * If data obtained by a call to libjhgdc is left for a large period of time
 * then a Playlist object created from it, the Playlist will be out of date.
 */
public class Playlist {

	private ArrayList<PlaylistItem> items;
	
	/**
	 * @author Matthew Mole
	 * @param items An already instantiated ArrayList of PlaylistItems.
	 */
	public Playlist(ArrayList<PlaylistItem> items) {
		this.items = items;
	}
	
	/**
	 * Parse input obtained from HGDClient.requestPlaylist() and return a populated Playlist.
	 * 
	 * @author Matthew Mole
	 * @param inputs An array of expected format: <track-id>|<filename>|<artist>|<title>|<user>
	 * @return A Playlist object instantiated with an ArrayList of PlaylistItems, parsed from the given input
	 */
	public static Playlist getPlaylist(String[] inputs) throws IllegalArgumentException  {
		ArrayList<PlaylistItem> items = new ArrayList<PlaylistItem>();
		
		for (String input : inputs) {
			if (input.split("|").length == 5) {
				items.add(new PlaylistItem(input.split("|")[0], input.split("|")[1], input.split("|")[2], input.split("|")[3], input.split("|")[4]));
			}
			else {
				throw new IllegalArgumentException("input incorrect format");
			}
		}
		
		return new Playlist(items);
	}
	
	/**
	 * @author Matthew Mole
	 * @return the ArrayList of PlaylistItems in the playlist at the time of instantiation.
	 */
	public ArrayList<PlaylistItem> getItems() {
		return this.items;
	}
	
	/**
	 * @author Matthew Mole
	 * @return True if the playlist is empty.
	 */
	public boolean isEmpty() {
		return this.items.size() == 0;
	}
}
