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
	private String[] activities = {};
	
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
	
	public synchronized void die() {
		Looper.myLooper().quit();
		workerHandler = null;
		activities = null;
		uiThreadCallback = null;
	}
	
	public synchronized void removeActivity() {
		String[] replacement = new String[activities.length-1];
		for (int i = 1; i < activities.length; i++) {
			replacement[i-1] = activities[i];
		}
		activities = replacement;
		uiThreadCallback.notifyActive(activities);
	}
	
	public synchronized String getLatestActivity() {
		try {
			return activities[activities.length - 1];
		}
		catch (Exception e) {
			return "";
		}
	}
	
	public synchronized void addActivity(String message) {
		String[] replacement = new String[activities.length+1];
		for (int i = 0; i < activities.length; i++) {
			replacement[i] = activities[i];
		}
		replacement[activities.length] = message;
		activities = replacement;
		uiThreadCallback.notifyActive(activities);
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
		        	removeActivity();
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_IOFAIL, e.toString());
		        	return;
		        }
		        catch (JHGDException e) {
		        	removeActivity();
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_GENFAIL, e.toString());
		        	return;
		        }
				
				//Connect to new server
				try {
		        	ahgdClient.jc.connect(server.getHostname(), Integer.parseInt(server.getPort()), false);
			    }
				catch (IOException e) {
					removeActivity();
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_IOFAIL, e.toString());
		        	return;
		        }
		        catch (JHGDException e) {
		        	removeActivity();
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_GENFAIL, e.toString());
		        	return;
		        }
				
				// -----
				boolean canEncrypt = true;
				try {
		        	ahgdClient.jc.checkServerEncryption();
		        }
		        catch (IllegalStateException e) {
		        	//It's fine.
		        }
		        catch (IOException e) {
		        	canEncrypt = false;
		        }
		        catch (JHGDException e) {
		        	canEncrypt = false;
		        }
				
				//Connect to new server
				try {
					if (canEncrypt) {
						ahgdClient.jc.requestEncryption();
					}
			    }
				catch (IOException e) {
					removeActivity();
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_IOFAIL, e.toString());
		        	return;
		        }
		        catch (JHGDException e) {
		        	removeActivity();
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_GENFAIL, e.toString());
		        	return;
		        }
				
				// ---
				
				//Logging in
				try {
			    	ahgdClient.jc.login(server.getUser(), password);
		    	}
				catch (IOException e) {
					removeActivity();
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_PASSWORD_IOFAIL, e.toString());
		        	return;
		        }
		        catch (JHGDException e) {
		        	removeActivity();
		        	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_PASSWORD_GENFAIL, e.toString());
		        	return;
		        }
				
				removeActivity();
		    	uiThreadCallback.notify(ahgdConstants.THREAD_CONNECTION_SUCCESS, server.getHostname());
			}
		});
		addActivity("connect to " + server.getHostname());
	}
	
	public synchronized void getActive() {
		workerHandler.post(new Runnable() {
			public void run() {
				uiThreadCallback.notifyActive(activities);
			}
		});
	}
	
	/**
	 * This is called by the User Interface thread and sends a runnable to the worker thread to be executed.
	 * Also updates the playlist
	 * 
	 * @param server
	 * @param password
	 */
	public synchronized void uploadFile(final String filename) {
		workerHandler.post(new Runnable() {
			public void run() {
				try {
		    		ahgdClient.jc.requestQueue(new File(filename));
		    		getPlaylist();
		    	}
		    	catch (FileNotFoundException e) {
		    		removeActivity();
		    		uiThreadCallback.notify(ahgdConstants.THREAD_UPLOAD_FILENOTFOUND, e.toString());
		    		return;
		    	}
		    	catch (IllegalStateException e) {
		    		removeActivity();
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
		    		removeActivity();
		    		uiThreadCallback.notify(ahgdConstants.THREAD_UPLOAD_IOFAIL, e.toString());
		    		return;
		    	}
		    	catch (JHGDException e) {
		    		removeActivity();
		    		uiThreadCallback.notify(ahgdConstants.THREAD_UPLOAD_GENFAIL, e.toString());
		    		return;
		    	}
				
				removeActivity();
				getPlaylist();
		    	uiThreadCallback.notify(ahgdConstants.THREAD_UPLOAD_SUCCESS, "");
			}
		});
		addActivity("enqueue " + (new File(filename)).getName());
	}
	
	/**
	 * This is called by the User Interface thread and sends a runnable to the worker thread to be executed.
	 * 
	 * @param server
	 * @param password
	 */
	public synchronized void getPlaylist() {
		if (getLatestActivity().equals("retrieving playlist")) {
			return;
		}
		workerHandler.post(new Runnable() {
			public void run() {
				try {
		    		Playlist p = ahgdClient.jc.getPlaylist();
		    		uiThreadCallback.notifyPlaylist(p);
		    	}
		    	catch (IllegalArgumentException e) {
		    		removeActivity();
		    		uiThreadCallback.notify(ahgdConstants.THREAD_PLAYLIST_GENFAIL, e.toString());
		    		return;
		    	}
		    	catch (IllegalStateException e) {
		    		removeActivity();
		    		uiThreadCallback.notify(ahgdConstants.THREAD_PLAYLIST_GENFAIL, e.toString());
			    	return;
		    	}
		    	catch (IOException e) {
		    		removeActivity();
		    		uiThreadCallback.notify(ahgdConstants.THREAD_PLAYLIST_IOFAIL, e.toString());
		    		return;
		    	}
		    	catch (JHGDException e) {
		    		removeActivity();
		    		uiThreadCallback.notify(ahgdConstants.THREAD_PLAYLIST_GENFAIL, e.toString());
		    		return;
		    	}
				
				removeActivity();
		    	uiThreadCallback.notify(ahgdConstants.THREAD_PLAYLIST_SUCCESS, "");
			}
		});
		addActivity("retrieving playlist");
	}
	
	/**
	 * This is called by the User Interface thread and sends a runnable to the worker thread to be executed.
	 * 
	 * @param server
	 * @param password
	 */
	public synchronized void voteSong() {
		workerHandler.post(new Runnable() {
			public void run() {
				try {
		    		String trackID = ahgdClient.jc.getCurrentPlaying().getId();
		    		ahgdClient.jc.requestVoteOff(trackID); //even if ok, check that the vote flag is now 1
		    		getPlaylist();
		    	}
		    	catch (IllegalArgumentException e) {
		    		removeActivity();
		    		uiThreadCallback.notify(ahgdConstants.THREAD_VOTING_GENFAIL, e.toString());
		    		return;
		    	}
		    	catch (IllegalStateException e) {
		    		removeActivity();
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
		    		removeActivity();
		    		uiThreadCallback.notify(ahgdConstants.THREAD_VOTING_IOFAIL, e.toString());
		    		return;
		    	}
		    	catch (JHGDException e) {
		    		removeActivity();
		    		uiThreadCallback.notify(ahgdConstants.THREAD_VOTING_GENFAIL, e.toString());
		    		return;
		    	}
				
				removeActivity();
		    	uiThreadCallback.notify(ahgdConstants.THREAD_VOTING_SUCCESS, "");
			}
		});
		addActivity("voting off current");
	}
}
