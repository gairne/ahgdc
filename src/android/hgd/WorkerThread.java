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
import java.io.FileNotFoundException;
import java.io.IOException;

import jhgdc.library.JHGDException;
import jhgdc.library.Playlist;

import android.os.Handler;
import android.os.Looper;

public class WorkerThread extends Thread {
	private ThreadListener uiThreadCallback;
	private Handler workerHandler;
	
	public WorkerThread(ThreadListener uiThreadCallback) {
		this.uiThreadCallback = uiThreadCallback;
	}
	
	@Override
	public void run() {
		try {
			Looper.prepare();
			
			//The worker thread receives work by receiving runnables through this handler.
			workerHandler = new Handler();
			
			Looper.loop();
		} catch (Throwable t) {
			
		} 
	}
	
	/**
	 * This is called by the User Interface thread and sends a runnable to the worker thread to be executed.
	 * 
	 * @param server
	 * @param password
	 */
	public synchronized void connectToServer(final ServerDetails server, final String password) {
		workerHandler.post(new Runnable() {
			public void run() {
				//Disconnect from previous server
				try {
		        	ahgdClient.jc.disconnect(true);
		        }
		        catch (IllegalStateException e) {
		        	//It's fine.
		        }
		        catch (IOException e) {
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_IOFAIL, e.toString());
		        	return;
		        }
		        catch (JHGDException e) {
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_GENFAIL, e.toString());
		        	return;
		        }
				
				//Connect to new server
				try {
		        	ahgdClient.jc.connect(server.getHostname(), Integer.parseInt(server.getPort()));
			    }
				catch (IOException e) {
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_IOFAIL, e.toString());
		        	return;
		        }
		        catch (JHGDException e) {
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_GENFAIL, e.toString());
		        	return;
		        }
				
				//Logging in
				try {
			    	ahgdClient.jc.login(server.getUser(), password);
		    	}
				catch (IOException e) {
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_PASSWORD_IOFAIL, e.toString());
		        	return;
		        }
		        catch (JHGDException e) {
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_PASSWORD_GENFAIL, e.toString());
		        	return;
		        }
				
		    	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_SUCCESS, server.getHostname());
			}
		});
	}
	
	/**
	 * This is called by the User Interface thread and sends a runnable to the worker thread to be executed.
	 * 
	 * @param server
	 * @param password
	 */
	public synchronized void uploadFile(final String filename) {
		workerHandler.post(new Runnable() {
			public void run() {
				try {
		    		ahgdClient.jc.requestQueue(new File(filename));
		    	}
		    	catch (FileNotFoundException e) {
		    		uiThreadCallback.notify(ahgdConstants.THREAD_UPLOAD_FILENOTFOUND, e.toString());
		    		return;
		    	}
		    	catch (IllegalStateException e) {
		    		if (e.getMessage().equals("Client not connected")) {
		        		uiThreadCallback.notify(ahgdConstants.THREAD_UPLOAD_NOTCONNECTED, e.toString());
		        		return;
		    		}
		    		else if (e.getMessage().equals("Client not authenticated")) {
		        		uiThreadCallback.notify(ahgdConstants.THREAD_UPLOAD_NOTAUTH, e.toString());
		        		return;
		    		}
		    		else {
		        		uiThreadCallback.notify(ahgdConstants.THREAD_UPLOAD_GENFAIL, e.toString());
		        		return;
		    		}
		    	}
		    	catch (IOException e) {
		    		uiThreadCallback.notify(ahgdConstants.THREAD_UPLOAD_IOFAIL, e.toString());
		    		return;
		    	}
		    	catch (JHGDException e) {
		    		uiThreadCallback.notify(ahgdConstants.THREAD_UPLOAD_GENFAIL, e.toString());
		    		return;
		    	}
				
		    	uiThreadCallback.notify(ahgdConstants.THREAD_UPLOAD_SUCCESS, "");
			}
		});
	}
	
	/**
	 * This is called by the User Interface thread and sends a runnable to the worker thread to be executed.
	 * 
	 * @param server
	 * @param password
	 */
	public synchronized void getPlaylist() {
		workerHandler.post(new Runnable() {
			public void run() {
				try {
		    		Playlist p = ahgdClient.jc.getPlaylist();
		    		uiThreadCallback.notifyPlaylist(p);
		    	}
		    	catch (IllegalArgumentException e) {
		    		uiThreadCallback.notify(ahgdConstants.THREAD_PLAYLIST_GENFAIL, e.toString());
		    		return;
		    	}
		    	catch (IllegalStateException e) {
		    		uiThreadCallback.notify(ahgdConstants.THREAD_PLAYLIST_GENFAIL, e.toString());
			    	return;
		    	}
		    	catch (IOException e) {
		    		uiThreadCallback.notify(ahgdConstants.THREAD_PLAYLIST_IOFAIL, e.toString());
		    		return;
		    	}
		    	catch (JHGDException e) {
		    		uiThreadCallback.notify(ahgdConstants.THREAD_PLAYLIST_GENFAIL, e.toString());
		    		return;
		    	}
				
		    	uiThreadCallback.notify(ahgdConstants.THREAD_PLAYLIST_SUCCESS, "");
			}
		});
	}
	
	/**
	 * This is called by the User Interface thread and sends a runnable to the worker thread to be executed.
	 * 
	 * @param server
	 * @param password
	 */
	public synchronized void voteSong(final String trackID) {
		workerHandler.post(new Runnable() {
			public void run() {
				try {
		    		//TODO: check trackId is valid
		    		ahgdClient.jc.requestVoteOff(trackID);
		    	}
		    	catch (IllegalArgumentException e) {
		    		uiThreadCallback.notify(ahgdConstants.THREAD_VOTING_GENFAIL, e.toString());
		    		return;
		    	}
		    	catch (IllegalStateException e) {
		    		if (e.getMessage().equals("Client not connected")) {
		    			uiThreadCallback.notify(ahgdConstants.THREAD_VOTING_NOTCONNECTED, e.toString());
			    		return;
		    		}
		    		else if (e.getMessage().equals("Client not authenticated")) {
		    			uiThreadCallback.notify(ahgdConstants.THREAD_VOTING_NOTAUTH, e.toString());
			    		return;
		    		}
		    		else {
		    			uiThreadCallback.notify(ahgdConstants.THREAD_VOTING_GENFAIL, e.toString());
			    		return;
		    		}
		    	}
		    	catch (IOException e) {
		    		uiThreadCallback.notify(ahgdConstants.THREAD_VOTING_IOFAIL, e.toString());
		    		return;
		    	}
		    	catch (JHGDException e) {
		    		uiThreadCallback.notify(ahgdConstants.THREAD_VOTING_GENFAIL, e.toString());
		    		return;
		    	}
				
		    	uiThreadCallback.notify(ahgdConstants.THREAD_VOTING_SUCCESS, "");
			}
		});
	}
}
