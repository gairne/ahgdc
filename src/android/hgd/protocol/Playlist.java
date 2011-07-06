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

public class Playlist {

	private ArrayList<PlaylistItem> items;
	
	public Playlist(ArrayList<PlaylistItem> items) {
		this.items = items;
	}
	
	/*
	 * Parse input of the format:
	 * [<track-id>|<filename>|<artist>|<title>|<user>]
	 * obtained from HGDClient.requestPlaylist()
	 * and return a populated Playlist
	 */
	public static Playlist getPlaylist(String[] inputs) {
		ArrayList<PlaylistItem> items = new ArrayList<PlaylistItem>();
		
		for (String s : inputs) {
			String input = "";
			try {
				items.add(new PlaylistItem(input.split("|")[0], input.split("|")[1], input.split("|")[2], input.split("|")[3], input.split("|")[4]));
			}
			catch (Exception e) {
				//
			}
		}
		
		return new Playlist(items);
	}
	
	public ArrayList<PlaylistItem> getItems() {
		return this.items;
	}
}
