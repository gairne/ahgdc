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
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
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
public class ahgdClient extends TabActivity {
    /** Called when the activity is first created. */
	private HGDClient jc;
	
	//private String hostname;
	//private String port;
	//private String user;
	
	private ServerDetails connectedTo;
	
	//filebrowser
	private ListView filelist;
	private FileBrowser f;
	private String[] listItems = {};
	private ArrayAdapter myAdapter;
	
	//playlist
	private ListView songlist;
	private SimpleAdapter songAdapter;
	
	//servers
	private ListView serverlist;
	private TextView currentServer;
	private ArrayAdapter<String> serverAdapter;
	private Button addServer;
	private ArrayList<ServerDetails> servers = new ArrayList<ServerDetails>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        TabHost tabs = getTabHost();
        jc = new HGDClient();
        
        this.getLayoutInflater().inflate(R.layout.main, tabs.getTabContentView(), true);
        
        Resources resources = getResources();
        
        TabHost.TabSpec t_upload = tabs.newTabSpec("filebrowser").setContent(R.id.filebrowser).setIndicator("Upload", resources.getDrawable(R.drawable.upload));
        TabHost.TabSpec t_playlist = tabs.newTabSpec("playlist").setContent(R.id.playlist).setIndicator("Playlist", resources.getDrawable(R.drawable.playlist));
        TabHost.TabSpec t_servers = tabs.newTabSpec("servers").setContent(R.id.serversframe).setIndicator("Servers", resources.getDrawable(R.drawable.servers));
        
        tabs.addTab(t_upload);
        tabs.addTab(t_playlist);
        tabs.addTab(t_servers);
        
        init_upload_tab();
        init_playlist_tab();
        init_servers_tab();
                
        /*AlertDialog.Builder alert = new AlertDialog.Builder(this);
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
        
        alert.show();*/
        
        /*
         * 127.0.0.1 gives connection refused:
         * See http://stackoverflow.com/questions/3497253/java-net-connectexception-connection-refused-android-emulator
         */
    }
    
    public void init_playlist_tab() {
    	songlist = (ListView) findViewById(R.id.playlist);
    	songlist.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView parent, View v, int position, long id) {
    			resetSongAdapter();
    		}
    	});
    	
    	resetSongAdapter();
    	
    	//String[] temp = new String[1];
    	//temp[0] = "Click to refresh";
    	
    	ArrayList<HashMap<String, String>> songData = new ArrayList<HashMap<String, String>>();
    	HashMap<String, String> map;
    	
    	
        map = new HashMap<String, String>();
        map.put("title", "Click to refresh");
        map.put("artist", "");
        map.put("user", "");
        songData.add(map);
    	
    	//songAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, temp);
    	songAdapter = new SimpleAdapter (this.getBaseContext(), songData, R.layout.playlistitem,
                new String[] {"title", "artist", "user"}, new int[] {R.id.title, R.id.artist, R.id.user});
        
        songlist.setAdapter(songAdapter);
    }
    
    public void init_servers_tab() {
    	parseDetails();
    	
    	currentServer = (TextView) findViewById(R.id.currentserver);
    	currentServer.setText("Not connected");
    	
    	serverlist = (ListView) findViewById(R.id.serverlist);
        serverAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, convertServers(servers));
        serverlist.setAdapter(serverAdapter);
        
        addServer = (Button) findViewById(R.id.serveradd);
        addServer.setOnClickListener(addServerHandler);
        
        serverlist.setOnItemClickListener(connectServer);
        serverlist.setOnItemLongClickListener(editServer);
        
    }
    
    private OnItemClickListener connectServer = new OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id)
        {
        	try {
            	jc.disconnect(true);
            	currentServer.setText("Not connected");
            }
            catch (IllegalStateException e) {
            	//It's fine.
            }
            catch (IOException e) {
            	Log.e("ahgdc:io", e.toString());
            }
            catch (JHGDException e) {
            	Log.e("ahgdc:jhgd", e.toString());
            }
        	connect(servers.get(position));
        }
    };
    
    private OnItemLongClickListener editServer = new OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView parent, View v, int position, long id)
        {
        	//prompt for edit or delete
        	return true;
        }
    };

    
    public void promptForServer() {
	    AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setTitle("host");
	    alert.setMessage("enter user@hostname:port");
	    
	    final EditText input = new EditText(this);
	    alert.setView(input);
	    
	    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				String user = input.getText().toString().split("@")[0];
				String hostname = input.getText().toString().split("@")[1].split(":")[0];
				String port = input.getText().toString().split("@")[1].split(":")[1];
				
				servers.add(new ServerDetails(hostname, port, user));
				
			}
		});
	    
	    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});
	    
	    alert.show();
    }
    
    public Button.OnClickListener addServerHandler = new Button.OnClickListener() {
        public void onClick(View v) {
            promptForServer();
        }
    };
    
    public void init_upload_tab() {
    	f = new FileBrowser();
        
        filelist = (ListView) findViewById(R.id.filebrowser);
        filelist.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView parent, View v, int position,
        	long id) {
        		Toast.makeText(parent.getContext(), "You have selected " + listItems[position], Toast.LENGTH_SHORT).show();
        		if (f.changeDirectory(listItems[position])) {
        			Toast.makeText(parent.getContext(), "You changed dir " + listItems[position], Toast.LENGTH_SHORT).show();
        			listItems = FileBrowser.toStringArray(f.listDirectory());
        			//myAdapter.notifyDataSetChanged();
        			resetFileListAdapter();
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
    }
    
    public void resetServerAdapter() {
    	serverAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, convertServers(servers));
        serverlist.setAdapter(serverAdapter);
    }
    
    public void resetFileListAdapter() {
    	myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);       
        filelist.setAdapter(myAdapter);
    }
    
    public void resetSongAdapter() {
    	//String[] strlist;
    	ArrayList<HashMap<String, String>> songData = new ArrayList<HashMap<String, String>>();
    	HashMap<String, String> map;
    	
    	try {
    		ArrayList<PlaylistItem> playlist = getPlaylist().getItems();
    		
    		if (playlist.isEmpty()) {
    			map = new HashMap<String, String>();
                map.put("title", "Nothing playing");
                map.put("artist", "");
                map.put("user", "");
                songData.add(map);
    		}
    		else {
    			//strlist = new String[playlist.size()];
    			//int i = 0;
            	for (PlaylistItem p : playlist) {
            		//strlist[i] = p.getTitle();
            		//i++;
            		map = new HashMap<String, String>();
                    map.put("title", p.getTitle());
                    map.put("artist", p.getArtist());
                    map.put("user", p.getUser());
                    songData.add(map);
            	}
    		}
    	}
    	catch (NullPointerException e) {
    		//strlist = new String[1];
        	//strlist[0] = "Nothing playing (exception)";
    		map = new HashMap<String, String>();
            map.put("title", "Click to refresh");
            map.put("artist", "");
            map.put("user", "");
            songData.add(map);
    	}
    	
    	//songAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, temp);
    	songAdapter = new SimpleAdapter (this.getBaseContext(), songData, R.layout.playlistitem,
                new String[] {"title", "artist", "user"}, new int[] {R.id.title, R.id.artist, R.id.user});
        
        songlist.setAdapter(songAdapter);
    }
    
    public void log(String tag, String message) {
    	Log.i(tag, message);
    }
    
    //TODO: SECURE ENTRY
    public void promptPassword() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setTitle("password");
	    alert.setMessage("enter password");
	    
	    final EditText input = new EditText(this);
	    alert.setView(input);
	    
	    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				String password = input.getText().toString();
				login(password);
			}
		});
	    
	    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});
	    
	    alert.show();
    }
    
    public void login(String password) {
    	try {
	    	Log.i("ahgdc", "Logging in with username " + this.connectedTo.getUser() + " and password " + password);
	        jc.login(this.connectedTo.getUser(), password);
    	}
    	catch (IOException e) {
        	Log.e("ahgdc:io", e.toString());
        }
        catch (JHGDException e) {
        	Log.e("ahgdc:jhgd", e.toString());
        }
    	
        /*Log.i("ahgdc", "Playlist items");
        String[] playlist = jc.requestPlaylist();
        for (String item : playlist) {
        	Log.i("ahgdc", item);
        }*/
    }
    
    //TODO: 
    // THE FOLLOWING METHODS LIAISE WITH libjhgdc:HGDClient IN ORDER TO PROVIDE HGDC FUNCTIONALITY
    public void connect(ServerDetails server) {
        try {
        	Log.i("ahgdc", "Attempting to connect to " + server.getHostname() + ":" + server.getPort());
	        jc.connect(server.getHostname(), Integer.parseInt(server.getPort()));
	        currentServer.setText(server.toString());
	        this.connectedTo = server;
	    }
        catch (IOException e) {
        	Log.e("ahgdc:io", e.toString());
        }
        catch (JHGDException e) {
        	Log.e("ahgdc:jhgd", e.toString());
        }
    }
    
    
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
    		;
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
    		Log.i("", "IAE");
    	}
    	catch (IllegalStateException e) {
    		Toast.makeText(this.getBaseContext(), R.string.ISE_NotConnected, Toast.LENGTH_SHORT).show();
    		Log.i("", "ISE");
    	}
    	catch (IOException e) {
    		Toast.makeText(this.getBaseContext(), R.string.IOE, Toast.LENGTH_SHORT).show();
    		Log.i("", "IOE");
    	}
    	catch (JHGDException e) {
    		Toast.makeText(this.getBaseContext(), R.string.JHGDE, Toast.LENGTH_SHORT).show();
    		Log.i("", "JHGDE");
    	}
    	
    	Log.i("","null here");
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
    
    public void saveDetails() {
    	
    }
    
    public String[] convertServers(ArrayList<ServerDetails> arraylist) {
    	String[] toRet = new String[arraylist.size()];
    	for (int i = 0; i < arraylist.size(); i++) {
    		toRet[i] = arraylist.get(i).toString();
    	}
    	return toRet;
    }
    
    public void parseDetails() {
    	servers.clear();
    	servers.add(new ServerDetails("10.0.0.2", "6633", "test"));
    }
}