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

public class PlaylistItem {

	private String trackId, filename, artist, title, user;
	
	public PlaylistItem(String trackId, String filename, String artist, String title, String user) {
		this.trackId = trackId;
		this.filename = filename;
		this.artist = artist;
		this.title = title;
		this.user = user;
	}
	
	/*
	 * Parse input of the format:
	 * ok|<playing?>[|<track-id>|<filename>|<artist>|<title>|<user>]
	 * obtained from HGDClient.requestNowPlaying()
	 * and return a populated single entry playlist
	 */
	public static PlaylistItem getPlaylistItem(String input) {
		try {
			return new PlaylistItem(input.split("|")[2], input.split("|")[3], input.split("|")[4], input.split("|")[5], input.split("|")[6]);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public String getId() {
		return this.trackId;
	}
	
	public int getIntegerId() {
		try {
			return Integer.parseInt(this.trackId);
		}
		catch (Exception e) {
			//Not a number
			return -1;
		}
	}
	
	public String getFilename() {
		return this.filename;
	}
	
	public String getArtist() {
		return this.artist;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getUser() {
		return this.user;
	}
}
