package android.hgd.jobs;
import java.io.IOException;

import jhgdc.library.JHGDException;
import android.hgd.ServerDetails;
import android.hgd.ahgdClient;
import android.hgd.ahgdConstants;
import android.os.Handler;
import android.os.Message;


public class ConnectionThread extends Thread {

	private Handler uiThread;
	private ServerDetails currentJob;
	private String password;
	
	public ConnectionThread(Handler uiThread, ServerDetails currentJob, String password) {
		this.uiThread = uiThread;
		this.currentJob = currentJob;
		this.password = password;
	}
	
	public void run() {
		Message msg = Message.obtain();
		
		//Disconnect from previous server
		try {
        	ahgdClient.jc.disconnect(true);
        }
        catch (IllegalStateException e) {
        	//It's fine.
        }
        catch (IOException e) {
        	msg.arg1 = ahgdConstants.THREAD_CONNECTION_IO_FAIL;
        	uiThread.sendMessage(msg);
        	return;
        }
        catch (JHGDException e) {
        	msg.arg1 = ahgdConstants.THREAD_CONNECTION_JHGDC_FAIL;
        	uiThread.sendMessage(msg);
        	return;
        }
		
		//Connect to new server
		try {
        	ahgdClient.jc.connect(currentJob.getHostname(), Integer.parseInt(currentJob.getPort()));
	        //currentServer.setText(currentJob.toString());
	        //this.connectedTo = server;
	    }
		catch (IOException e) {
        	msg.arg1 = ahgdConstants.THREAD_CONNECTION_IO_FAIL;
        	uiThread.sendMessage(msg);
        	return;
        }
        catch (JHGDException e) {
        	msg.arg1 = ahgdConstants.THREAD_CONNECTION_JHGDC_FAIL;
        	uiThread.sendMessage(msg);
        	return;
        }
		
		//Logging in
		try {
	    	ahgdClient.jc.login(currentJob.getUser(), password);
    	}
		catch (IOException e) {
        	msg.arg1 = ahgdConstants.THREAD_CONNECTION_PWIO_FAIL;
        	uiThread.sendMessage(msg);
        	return;
        }
        catch (JHGDException e) {
        	msg.arg1 = ahgdConstants.THREAD_CONNECTION_PWJHGDC_FAIL;
        	uiThread.sendMessage(msg);
        	return;
        }
		
		msg.arg1 = ahgdConstants.THREAD_CONNECTION_SUCCESS;
    	uiThread.sendMessage(msg);
	}

}
