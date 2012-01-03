package android.hgd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jhgdc.library.JHGDException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
				Message msg = Message.obtain();
				
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
	
}
