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

public final class ahgdConstants {

	public static final boolean AHGD_SUCCESS = true;
	public static final boolean AHGD_FAIL = false;
	
	public static final int THREAD_CONNECTION_SUCCESS = 0;
	public static final int THREAD_CONNECTION_IO_FAIL = 1;
	public static final int THREAD_CONNECTION_JHGDC_FAIL = 2;
	public static final int THREAD_CONNECTION_PWIO_FAIL = 3;
	public static final int THREAD_CONNECTION_PWJHGDC_FAIL = 4;
	
	public static final int THREAD_UPLOAD_SUCCESS = 10;
	public static final int THREAD_UPLOAD_FILENOTFOUND = 11;
	public static final int THREAD_UPLOAD_ISE_NOTCONNECTED = 12;
	public static final int THREAD_UPLOAD_ISE_NOTAUTH = 13;
	public static final int THREAD_UPLOAD_FAIL = 14;
	public static final int THREAD_UPLOAD_IO = 15;
	public static final int THREAD_UPLOAD_JHGDC = 16;
}
