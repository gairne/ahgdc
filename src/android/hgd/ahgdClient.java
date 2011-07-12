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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import android.hgd.ahgdConstants;

import jhgdc.library.HGDClient;
import jhgdc.library.HGDConsts;
import jhgdc.library.JHGDException;
import jhgdc.library.Playlist;
import jhgdc.library.PlaylistItem;
import jhgdc.library.EmptyPlaylistItem;

/**
 * This is the main entrypoint into the application.
 * 
 * @author Matthew Mole
 */
public class ahgdClient extends Activity {
    /** Called when the activity is first created. */
	private HGDClient jc;
	private FileBrowser f;
	private String[] listItems = {};
	private String[] test = {"1","2"};
	private ArrayAdapter myAdapter;
	private String hostname;
	private String port;
	private String username;
	private String password;
	
	private ListView filelist;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        f = new FileBrowser();
        
        filelist = (ListView) findViewById(R.id.filesystem);
        filelist.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView parent, View v, int position,
        	long id) {
        		Toast.makeText(parent.getContext(), "You have selected " + listItems[position], Toast.LENGTH_SHORT).show();
        		if (f.changeDirectory(listItems[position])) {
        			Toast.makeText(parent.getContext(), "You changed dir " + listItems[position], Toast.LENGTH_SHORT).show();
        			listItems = FileBrowser.toStringArray(f.listDirectory());
        			//myAdapter.notifyDataSetChanged();
        			resetAdapter();
        		}
        		else if (f.isValidToUpload(new File(f.currentPath + "/" + listItems[position]))) {
        			enqueue(f.currentPath + "/" + listItems[position]);
        		}
        	}
        	});
		
        File[] fs = f.listDirectory(new File("/"));
        listItems = FileBrowser.toStringArray(fs);

        myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        
        filelist.setAdapter(myAdapter);
        
        Log.i("ahgdc", "Example started");
        
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("host");
        alert.setMessage("enter hostname:port");
        
        final EditText input = new EditText(this);
        alert.setView(input);
        
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				hostname = input.getText().toString().split(":")[0];
				port = input.getText().toString().split(":")[1];
				connect();
			}
		});
        
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
        
        alert.show();
        
        AlertDialog.Builder alert2 = new AlertDialog.Builder(this);
        alert2.setTitle("username");
        alert2.setMessage("enter username:password");
        
        final EditText input2 = new EditText(this);
        alert2.setView(input2);
        
        alert2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				username = input2.getText().toString().split(":")[0];
				password = input2.getText().toString().split(":")[1];
			}
		});
        
        alert2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
        
        alert2.show();
        
        /*
         * 127.0.0.1 gives connection refused:
         * See http://stackoverflow.com/questions/3497253/java-net-connectexception-connection-refused-android-emulator
         */
    }
    
    public void connect() {
    	jc = new HGDClient();
        try {
        	Log.i("ahgdc", "Attempting to connect to " + hostname + ":" + port);
	        jc.connect(hostname, Integer.parseInt(port)); 
	        Log.i("ahgdc", "Logging in with username " + username + " and password " + password);
	        jc.login(username, password);
	        Log.i("ahgdc", "Playlist items");
	        String[] playlist = jc.requestPlaylist();
	        for (String item : playlist) {
	        	Log.i("ahgdc", item);
	        }
	        Log.i("ahgdc", "Disconnecting");
	        //jc.disconnect(true);
	        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
	    }
        catch (IOException e) {
        	Toast.makeText(getApplicationContext(), "connect IOException", Toast.LENGTH_SHORT).show();
        	Log.e("ahgdc:io", e.toString());
        }
        catch (JHGDException e) {
        	Toast.makeText(getApplicationContext(), "connect JHGDCException", Toast.LENGTH_SHORT).show();
        	Log.e("ahgdc:jhgd", e.toString());
        }
        catch (IllegalStateException e) {
        	Toast.makeText(getApplicationContext(), "connect IllegalStateException", Toast.LENGTH_SHORT).show();
        	Log.e("ahgdc:is", e.toString());
        }
        catch (NumberFormatException e) {
        	Toast.makeText(getApplicationContext(), "connect NumberFormatException", Toast.LENGTH_SHORT).show();
        	Log.e("ahgdc:nf", e.toString());
        }
        
        Log.i("ahgdc", "Example stopped");
    }
    
    public void resetAdapter() {
    	myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);       
        filelist.setAdapter(myAdapter);
    }
    
    public void log(String tag, String message) {
    	Log.i(tag, message);
    }
    
    // THE FOLLOWING METHODS LIAISE WITH libjhgdc:HGDClient IN ORDER TO PROVIDE HGDC FUNCTIONALITY
    
    /**
     * Vote off the current song
     * 
     * @return True on success.
     */
    public boolean vote() {
    	try {
    		PlaylistItem response = jc.getCurrentPlaying();		
    		if (!response.isEmpty()) {
    			vote(response.getId());
    			return true;
    		}
    		else {
    			Toast.makeText(this.getBaseContext(), R.string.NothingPlaying, Toast.LENGTH_SHORT).show();
    			return false;
    		}
    	}
    	catch (IllegalArgumentException e) {
    		Toast.makeText(this.getBaseContext(), R.string.IAE, Toast.LENGTH_SHORT).show();
    	}
    	catch (IllegalStateException e) {
    		if (e.getMessage().equals("Client not connected")) {
    			Toast.makeText(this.getBaseContext(), R.string.ISE_NotConnected, Toast.LENGTH_SHORT).show();
    		}
    		else if (e.getMessage().equals("Client not authenticated")) {
    			Toast.makeText(this.getBaseContext(), R.string.ISE_NotAuthenticated, Toast.LENGTH_SHORT).show();
    		}
    		else {
    			Toast.makeText(this.getBaseContext(), R.string.Error, Toast.LENGTH_SHORT).show();
    		}
    	}
    	catch (IOException e) {
    		Toast.makeText(this.getBaseContext(), R.string.IOE, Toast.LENGTH_SHORT).show();
    	}
    	catch (JHGDException e) {
    		Toast.makeText(this.getBaseContext(), R.string.JHGDE, Toast.LENGTH_SHORT).show();
    	}
    	return false;
    }
    
    /**
     * Vote off the song that corresponds to the trackId
     * 
     * @return True on success.
     */
    public boolean vote(String trackId) {
    	try {
    		//TODO: check trackId is valid
    		jc.requestVoteOff(trackId);
    		return true;
    	}
    	catch (IllegalArgumentException e) {
    		Toast.makeText(this.getBaseContext(), R.string.IAE, Toast.LENGTH_SHORT).show();
    	}
    	catch (IllegalStateException e) {
    		if (e.getMessage().equals("Client not connected")) {
    			Toast.makeText(this.getBaseContext(), R.string.ISE_NotConnected, Toast.LENGTH_SHORT).show();
    		}
    		else if (e.getMessage().equals("Client not authenticated")) {
    			Toast.makeText(this.getBaseContext(), R.string.ISE_NotAuthenticated, Toast.LENGTH_SHORT).show();
    		}
    		else {
    			Toast.makeText(this.getBaseContext(), R.string.Error, Toast.LENGTH_SHORT).show();
    		}
    	}
    	catch (IOException e) {
    		Toast.makeText(this.getBaseContext(), R.string.IOE, Toast.LENGTH_SHORT).show();
    	}
    	catch (JHGDException e) {
    		Toast.makeText(this.getBaseContext(), R.string.JHGDE, Toast.LENGTH_SHORT).show();
    	}
    	return false;
    }

    /**
     * Upload a file to the HGD server.
     */
    public void enqueue(String filename) {
    	try {
    		//String filename = f.chooseFile();
    		if (f.isValidToUpload(new File(filename))) {
    			jc.requestQueue(new File(filename));
    		}
    	}
    	catch (FileNotFoundException e) {
    		Toast.makeText(this.getBaseContext(), R.string.Queue_NotFound, Toast.LENGTH_SHORT).show();
    	}
    	catch (IllegalStateException e) {
    		if (e.getMessage().equals("Client not connected")) {
    			Toast.makeText(this.getBaseContext(), R.string.ISE_NotConnected, Toast.LENGTH_SHORT).show();
    		}
    		else if (e.getMessage().equals("Client not authenticated")) {
    			Toast.makeText(this.getBaseContext(), R.string.ISE_NotAuthenticated, Toast.LENGTH_SHORT).show();
    		}
    		else {
    			Toast.makeText(this.getBaseContext(), R.string.Error, Toast.LENGTH_SHORT).show();
    		}
    	}
    	catch (IOException e) {
    		Toast.makeText(this.getBaseContext(), R.string.IOE, Toast.LENGTH_SHORT).show();
    	}
    	catch (JHGDException e) {
    		Toast.makeText(this.getBaseContext(), R.string.JHGDE, Toast.LENGTH_SHORT).show();
    	}
    	
    }
    
    /**
     * Return the current Playlist.
     * 
     * @return a Playlist object, populated with PlaylistItems.
     */
    public Playlist getPlaylist() {
    	try {
    		return jc.getPlaylist();
    	}
    	catch (IllegalArgumentException e) {
    		Toast.makeText(this.getBaseContext(), R.string.IAE, Toast.LENGTH_SHORT).show();
    	}
    	catch (IllegalStateException e) {
    		Toast.makeText(this.getBaseContext(), R.string.ISE_NotConnected, Toast.LENGTH_SHORT).show();
    	}
    	catch (IOException e) {
    		Toast.makeText(this.getBaseContext(), R.string.IOE, Toast.LENGTH_SHORT).show();
    	}
    	catch (JHGDException e) {
    		Toast.makeText(this.getBaseContext(), R.string.JHGDE, Toast.LENGTH_SHORT).show();
    	}
    	return null;
    }
    
    /**
     * Return the current playing song.
     *
     * @return A PlaylistItem populated with the details of the current playing song.
     */
    public PlaylistItem getCurrent() {
    	try {
    		return jc.getCurrentPlaying();
    	}
    	catch (IllegalArgumentException e) {
    		Toast.makeText(this.getBaseContext(), R.string.IAE, Toast.LENGTH_SHORT).show();
    	}
    	catch (IllegalStateException e) {
    		Toast.makeText(this.getBaseContext(), R.string.ISE_NotConnected, Toast.LENGTH_SHORT).show();
    	}
    	catch (IOException e) {
    		Toast.makeText(this.getBaseContext(), R.string.IOE, Toast.LENGTH_SHORT).show();
    	}
    	catch (JHGDException e) {
    		Toast.makeText(this.getBaseContext(), R.string.JHGDE, Toast.LENGTH_SHORT).show();
    	}
    	return null;
    }
    
    
    
    
}