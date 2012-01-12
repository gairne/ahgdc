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

public final class ahgdConstants {

	public static final boolean AHGD_SUCCESS = true;
	public static final boolean AHGD_FAIL = false;
	
	public static final int THREAD_CONNECTION_SUCCESS = 0;
	public static final int THREAD_CONNECTION_IOFAIL = 1;
	public static final int THREAD_CONNECTION_GENFAIL = 2;
	public static final int THREAD_CONNECTION_PASSWORD_IOFAIL = 3;
	public static final int THREAD_CONNECTION_PASSWORD_GENFAIL = 4;
	
	public static final int THREAD_UPLOAD_SUCCESS = 10;
	public static final int THREAD_UPLOAD_FILENOTFOUND = 11;
	public static final int THREAD_UPLOAD_NOTCONNECTED = 12;
	public static final int THREAD_UPLOAD_NOTAUTH = 13;
	public static final int THREAD_UPLOAD_GENFAIL = 14;
	public static final int THREAD_UPLOAD_IOFAIL = 15;
	
	public static final int THREAD_VOTING_GENFAIL = 20;
	public static final int THREAD_VOTING_NOTCONNECTED = 21;
	public static final int THREAD_VOTING_NOTAUTH = 22;
	public static final int THREAD_VOTING_IOFAIL = 23;
	public static final int THREAD_VOTING_SUCCESS = 24;
	
	public static final int THREAD_PLAYLIST_GENFAIL = 30;
	public static final int THREAD_PLAYLIST_IOFAIL = 31;
	public static final int THREAD_PLAYLIST_SUCCESS = 32;
	
	public static final int SERVER_DETAILS = 1000;
	public static final String SERVER_DATA = "SERVER_DATA";
	public static final int UPDATE_SETTINGS = 1001;
	public static final String UPDATED_SETTINGS = "UPDATED_SETTINGS";
}
