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
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import android.hgd.ahgdConstants;
import android.hgd.protocol.PlaylistItem;

import jhgdc.library.HGDClient;
import jhgdc.library.HGDConsts;
import jhgdc.library.JHGDException;

/**
 * This is the main entrypoint into the application.
 * At the moment this is an example - a proof of concept.
 * To run this, you must have a hgd server installed on your computer
 * accepting connections from port 6633.
 * 
 * You also require a username 'test' with password 'password.
 * 
 * Steps to complete:
 * git clone git://github.com/vext01/hgd.git hgd
 * cd hgd
 * autoreconf && autoconf && ./configure && make
 * ./hgd-admin user-add test
 * ./hgd-netd &
 * ./hgd-playd &
 * 
 * @author Matthew Mole
 */
public class ahgdClient extends Activity {
    /** Called when the activity is first created. */
	private HGDClient jc;
	private FileBrowser f;
	private String[] listItems = {};
	
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
        		}
        	}
        	});
		
        File[] fs = f.listDirectory(new File("/"));
        listItems = FileBrowser.toStringArray(fs);

        filelist.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems));
        
        Log.i("ahgdc", "Example started");
        
        String host = "10.0.2.2";
        /*
         * 127.0.0.1 gives connection refused:
         * See http://stackoverflow.com/questions/3497253/java-net-connectexception-connection-refused-android-emulator
         */
        int port = 6633;
        String username = "test";
        String password = "password";
        
        jc = new HGDClient();
        try {
        	Log.i("ahgdc", "Attempting to connect to " + host + ":" + port);
	        jc.connect(host, port); 
	        Log.i("ahgdc", "Logging in with username " + username + " and password " + password);
	        jc.login(username, password);
	        Log.i("ahgdc", "Playlist items");
	        String[] playlist = jc.requestPlaylist();
	        for (String item : playlist) {
	        	Log.i("ahgdc", item);
	        }
	        Log.i("ahgdc", "Disconnecting");
	        jc.disconnect(true);
	    }
        catch (IOException e) {
        	Log.e("ahgdc:io", e.toString());
        }
        catch (JHGDException e) {
        	Log.e("ahgdc:jhgd", e.toString());
        }
        
        Log.i("ahgdc", "Example stopped");
    }
    
    public void log(String tag, String message) {
    	Log.i(tag, message);
    }
    
    // THE FOLLOWING METHODS LIAISE WITH libjhgdc:HGDClient IN ORDER TO PROVIDE HGDC FUNCTIONALITY
    
    /**
     * Vote off the current song
     * 
     * @author Matthew Mole
     */
    public boolean vote() {
    	try {
    		PlaylistItem response = PlaylistItem.getPlaylistItem(jc.requestNowPlaying()); 		
    		if (!response.isEmpty()) {
    			vote(response.getId());
    			return true;
    		}
    		else {
    			return false;
    		}
    	}
    	catch (JHGDException e) {
    		//Failed to get current playing
    		return false;
    	}
    	catch (IOException e) {
    		return false;
    	}
    	catch (IllegalStateException e) {
    		return false;
    	}
    	catch (IllegalArgumentException e) {
    		//Wrong format
    		return false;
    	}
    }
    
    /**
     * Vote off the song that corresponds to the trackId
     * 
     * @author Matthew Mole
     */
    public boolean vote(String trackId) {
    	try {
    		//TODO: check trackId is valid
    		jc.requestVoteOff(trackId);
    		return true;
    	}
    	catch (JHGDException e) {
    		//Failed to get current playing
    		return false;
    	}
    	catch (IOException e) {
    		return false;
    	}
    	catch (IllegalStateException e) {
    		return false;
    	}
    	catch (IllegalArgumentException e) {
    		//Wrong format
    		return false;
    	}
    }
    
    /**
     * 
     * @author Matthew Mole
     */
    public void enqueue() {
    	try {
    		BufferedReader input = new BufferedReader(new InputStreamReader(openFileInput("/")));
    	}
    	catch (FileNotFoundException e) {
    		
    	}
    }
    
    /**
     * 
     * @author Matthew Mole
     */
    public void getPlaylist() {
    	
    }
}