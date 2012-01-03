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

public class ServerDetails {
	private String hostname;
	private String port;
	private String user;
	private String password;
	
	public ServerDetails(String hostname, String port, String user, String password) {
		this.hostname = hostname;
		this.port = port;
		this.user = user;
		this.password = password;
	}
	
	public ServerDetails(String hostname, String port, String user) {
		this.hostname = hostname;
		this.port = port;
		this.user = user;
		this.password = "";
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public static ServerDetails toServerDetails(String serialisedServerDetails) {
		String user = serialisedServerDetails.split("@")[0];
		//String user = serialisedServerDetails.split("@")[0].split(":")[0];
		//String password = serialisedServerDetails.split("@")[0].split(":")[1];
		String hostname = serialisedServerDetails.split("@")[1].split(":")[0];
		String port = serialisedServerDetails.split("@")[1].split(":")[1];
		
		return new ServerDetails(hostname, port, user);//, password);
	}
	
	public String getHostname() {
		return this.hostname;
	}
	
	public String getPort() {
		return this.port;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String toString() {
		//return getUser() + ":" + getPassword() + "@" + getHostname() + ":" + getPort();
		return getUser() + "@" + getHostname() + ":" + getPort();
	}
}
