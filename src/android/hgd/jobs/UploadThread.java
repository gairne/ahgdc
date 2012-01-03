package android.hgd.jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jhgdc.library.JHGDException;
import android.hgd.ahgdClient;
import android.hgd.ahgdConstants;
import android.os.Handler;
import android.os.Message;

public class UploadThread extends Thread {

	private Handler uiThread;
	private String filename;
	
	public UploadThread(Handler uiThread, String filename) {
		this.uiThread = uiThread;
		this.filename = filename;
	}
	
	public void run() {
		Message msg = Message.obtain();
		
		try {
    		ahgdClient.jc.requestQueue(new File(filename));
    	}
    	catch (FileNotFoundException e) {
    		msg.arg1 = ahgdConstants.THREAD_UPLOAD_FILENOTFOUND;
    		uiThread.sendMessage(msg);
    		return;
    	}
    	catch (IllegalStateException e) {
    		if (e.getMessage().equals("Client not connected")) {
    			msg.arg1 = ahgdConstants.THREAD_UPLOAD_ISE_NOTCONNECTED;
        		uiThread.sendMessage(msg);
        		return;
    		}
    		else if (e.getMessage().equals("Client not authenticated")) {
    			msg.arg1 = ahgdConstants.THREAD_UPLOAD_ISE_NOTAUTH;
        		uiThread.sendMessage(msg);
        		return;
    		}
    		else {
    			msg.arg1 = ahgdConstants.THREAD_UPLOAD_FAIL;
        		uiThread.sendMessage(msg);
        		return;
    		}
    	}
    	catch (IOException e) {
    		msg.arg1 = ahgdConstants.THREAD_UPLOAD_IO;
    		uiThread.sendMessage(msg);
    		return;
    	}
    	catch (JHGDException e) {
    		msg.arg1 = ahgdConstants.THREAD_UPLOAD_JHGDC;
    		uiThread.sendMessage(msg);
    		return;
    	}
		
		msg.arg1 = ahgdConstants.THREAD_UPLOAD_SUCCESS;
    	uiThread.sendMessage(msg);
	}

}
