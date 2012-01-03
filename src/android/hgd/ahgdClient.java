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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import android.hgd.ahgdConstants;

import jhgdc.library.HGDClient;
import jhgdc.library.JHGDException;
import jhgdc.library.Playlist;
import jhgdc.library.PlaylistItem;
import android.hgd.jobs.ConnectionThread;
import android.hgd.jobs.UploadThread;

/**
 * This is the main entrypoint into the application.
 * 
 * @author Matthew Mole
 */
public class ahgdClient extends TabActivity {
    /** Called when the activity is first created. */
	public static HGDClient jc;
	
	public static String SERVER_FILENAME = "hgd_server.config";
	
	private boolean active = false;
	private ConnectionThread connthread;
	private UploadThread uploadthread;
	private ServerDetails intendedServer;
	
	//Temporary state variables
	private String toVoteOff;
	
	//filebrowser
	private ListView filelist;
	private FileBrowser f;
	private String[] listItems = {};
	private ArrayAdapter myAdapter;
	
	//playlist
	private ArrayList<HashMap<String, String>> songData;
	private ListView songlist;
	private SimpleAdapter songAdapter;
	
	//servers
	private ListView serverlist;
	private TextView currentServer;
	private ArrayAdapter<String> serverAdapter;
	private Button addServer;
	private ArrayList<ServerDetails> servers = new ArrayList<ServerDetails>();
	
	//
	// USER INTERFACE CREATION
	//
	
	public void createUI() {
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
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        File root = Environment.getExternalStorageDirectory();
        if (!root.canWrite()){
        	Toast.makeText(getApplicationContext(), "Cannot write to root", Toast.LENGTH_SHORT).show();
        }
        SERVER_FILENAME = (new File(root, SERVER_FILENAME)).getAbsolutePath();
        
        createUI();
        /*
         * 127.0.0.1 gives connection refused:
         * See http://stackoverflow.com/questions/3497253/java-net-connectexception-connection-refused-android-emulator
         */
    }
    
    //
    // Initialisation of user interface components
    //
    
    public void init_playlist_tab() {
    	songlist = (ListView) findViewById(R.id.playlist);
    	songlist.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView parent, View v, int position, long id) {
    			playlistClicked(position);
    		}
    	});
    	
    	resetSongAdapter();
    	
    	songData = new ArrayList<HashMap<String, String>>();
    	HashMap<String, String> map;
    	
        map = new HashMap<String, String>();
        map.put("title", "Refresh");
        map.put("artist", "");
        map.put("user", "");
        songData.add(map);
    	
    	songAdapter = new SimpleAdapter (this.getBaseContext(), songData, R.layout.playlistitem,
                new String[] {"title", "artist", "user"}, new int[] {R.id.title, R.id.artist, R.id.user});
        
        songlist.setAdapter(songAdapter);
    }
    
    public void init_servers_tab() {
    	currentServer = (TextView) findViewById(R.id.currentserver);
    	currentServer.setText("Not connected");
    	
    	serverlist = (ListView) findViewById(R.id.serverlist);
    	
    	serverlist.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView parent, View v, int position, long id) {
    			serverlistClicked(position);
    		}
    	});
    	
    	serverlist.setOnItemLongClickListener(new OnItemLongClickListener() {
    		public boolean onItemLongClick(AdapterView parent, View v, int position, long id) {
    			return serverlistLongClicked(position);
    		}
    	});
    	
        serverAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, convertServers(servers));
        serverlist.setAdapter(serverAdapter);
        
        addServer = (Button) findViewById(R.id.serveradd);
        addServer.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
                addServerClicked();
            }
        });
        
        readServerConfig();
        resetServerAdapter();
    }
    
    public void init_upload_tab() {
    	f = new FileBrowser();
        
        filelist = (ListView) findViewById(R.id.filebrowser);
        filelist.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView parent, View v, int position, long id) {
        		filelistClicked(position);
        	}
        });
		
        File[] fs = f.listDirectory(new File("/"));
        listItems = FileBrowser.toStringArray(fs);

        myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        
        filelist.setAdapter(myAdapter);
    }
    
    //
    // Updating the data of the User Interface components (refreshes them)
    //
    
    public void resetServerAdapter() {
    	serverAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, convertServers(servers));
        serverlist.setAdapter(serverAdapter);
    }
    
    public void resetFileListAdapter() {
    	myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);       
        filelist.setAdapter(myAdapter);
    }
    
    public void resetSongAdapter() {
    	songData = new ArrayList<HashMap<String, String>>();
    	HashMap<String, String> map;
    	
    	try {
    		ArrayList<PlaylistItem> playlist = getPlaylist().getItems();
    		
    		if (playlist.isEmpty()) {
    			map = new HashMap<String, String>();
                map.put("title", "Nothing playing");
                map.put("artist", "");
                map.put("user", "");
                songData.add(map);
                
                map = new HashMap<String, String>();
                map.put("title", "Refresh");
                map.put("artist", "");
                map.put("user", "");
                songData.add(map);
    		}
    		else {
    			map = new HashMap<String, String>();
                map.put("title", "Refresh");
                map.put("artist", "");
                map.put("user", "");
                songData.add(map);
    			
            	for (PlaylistItem p : playlist) {
            		map = new HashMap<String, String>();
                    map.put("title", p.getTitle());
                    map.put("artist", p.getArtist());
                    map.put("user", p.getUser());
                    songData.add(map);
            	}
    		}
    	}
    	catch (NullPointerException e) {
    		map = new HashMap<String, String>();
            map.put("title", "Click to refresh");
            map.put("artist", "");
            map.put("user", "");
            songData.add(map);
    	}
    	
    	songAdapter = new SimpleAdapter (this.getBaseContext(), songData, R.layout.playlistitem,
                new String[] {"title", "artist", "user"}, new int[] {R.id.title, R.id.artist, R.id.user});
        
        songlist.setAdapter(songAdapter);
    }
    
    //
    // User Interface reactions
    //
    
    private void playlistClicked(int position) {
    	if (songData == null) {
			resetSongAdapter();
		}
		else {
			if (songData.get(position).get("title").equals("Refresh")) {
				resetSongAdapter();
			}
			else {
				vote();
			}
		}
    }
    
    private void serverlistClicked(int position) {
    	if (active) {
    		Toast.makeText(getApplicationContext(), "Already busy", Toast.LENGTH_SHORT).show();
    	}
    	intendedServer = servers.get(position);
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setTitle("password");
	    alert.setMessage("enter password");
	    
	    final EditText input = new EditText(this);
	    alert.setView(input);
	    
	    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				connectServer(input.getText().toString());
			}
		});
	    
	    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
	    
	    alert.show();
    }
    
    private boolean serverlistLongClicked(final int position) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	
    	alert.setTitle("Server deletion");
		alert.setMessage("Are you sure you want to delete the server?");
    	
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				deleteServer(position);
		    }
		});
		   
		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
		    	dialog.cancel();
		    }
		});

		alert.show();
    	return true;
    }
    
    private void addServerClicked() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setTitle("host");
	    alert.setMessage("enter user@hostname:port");
	    
	    final EditText input = new EditText(this);
	    alert.setView(input);
	    
	    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				addServer(input.getText().toString());
			}
		});
	    
	    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
	    
	    alert.show();
    }
    
    private void filelistClicked(int position) {
    	if (f.changeDirectory(listItems[position])) {
			Toast.makeText(getApplicationContext(), new File(f.currentPath).getParent(), Toast.LENGTH_SHORT).show();
			listItems = FileBrowser.toStringArray(f.listDirectory());
			resetFileListAdapter();
		}
		else if (f.isValidToUpload(new File(f.currentPath + "/" + listItems[position]))) {
			uploadthread = new UploadThread(handler, f.currentPath + "/" + listItems[position]);
			uploadthread.start();
		}
    }
    
    //
    // Dialog box reactions
    //
    private void deleteServer(int position) {
    	servers.remove(position);
        writeServerConfig();
        resetServerAdapter();
    }
    
    private void addServer(String entry) {
    	String user = entry.split("@")[0];
		String hostname = entry.split("@")[1].split(":")[0];
		String port = entry.split("@")[1].split(":")[1];
		
		servers.add(new ServerDetails(hostname, port, user));
		writeServerConfig();
		resetServerAdapter();
    }
    
    private void connectServer(String entry) {
		active = true;
    	connthread = new ConnectionThread(handler, intendedServer, entry);
    	connthread.start();
    }
    
    //
    // Incoming messages from worker threads
    //
    
    private Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		switch (msg.arg1) {
    		case ahgdConstants.THREAD_CONNECTION_SUCCESS: {
    			currentServer.setText(intendedServer.getHostname());
    			Toast.makeText(getApplicationContext(), "Connected Successfully", Toast.LENGTH_SHORT).show();
    			active = false;
    			break;
    		}
    		case ahgdConstants.THREAD_CONNECTION_IO_FAIL: {
    			currentServer.setText("Not currently connected");
    			Toast.makeText(getApplicationContext(), "Failed to connect (IO)", Toast.LENGTH_SHORT).show();
    			active = false;
    			break;
    		}
    		case ahgdConstants.THREAD_CONNECTION_JHGDC_FAIL: {
    			currentServer.setText("Not currently connected");
    			Toast.makeText(getApplicationContext(), "Failed to connect (JHGDC)", Toast.LENGTH_SHORT).show();
    			active = false;
    			break;
    		}
    		case ahgdConstants.THREAD_CONNECTION_PWIO_FAIL: {
    			currentServer.setText("Not currently connected");
    			Toast.makeText(getApplicationContext(), "Failed to connect (password IO)", Toast.LENGTH_SHORT).show();
    			active = false;
    			break;
    		}
    		case ahgdConstants.THREAD_CONNECTION_PWJHGDC_FAIL: {
    			currentServer.setText("Not currently connected");
    			Toast.makeText(getApplicationContext(), "Failed to connect (password JHGDC)", Toast.LENGTH_SHORT).show();
    			active = false;
    			break;
    		}
    		//
    		case ahgdConstants.THREAD_UPLOAD_SUCCESS: {
    			break;
    		}
    		}
    	}
    };
    
    public void log(String tag, String message) {
    	Log.i(tag, message);
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
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setMessage("Are you sure you want to vote off?")
    			       .setCancelable(false)
    			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			                vote(toVoteOff);
    			           }
    			       })
    			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			                dialog.cancel();
    			           }
    			       });
    			AlertDialog alert = builder.create();
    			alert.show();
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
    		Toast.makeText(this.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
    		Log.i("ahgdc", e.toString());
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

    public String[] convertServers(ArrayList<ServerDetails> arraylist) {
    	String[] toRet = new String[arraylist.size()];
    	for (int i = 0; i < arraylist.size(); i++) {
    		toRet[i] = arraylist.get(i).toString();
    	}
    	return toRet;
    }
    
    public void writeServerConfig() {
    	try {
    		FileOutputStream os = new FileOutputStream(new File(SERVER_FILENAME));
    		OutputStreamWriter out = new OutputStreamWriter(os);
    		for (String line : convertServers(servers)) {
    			out.write(line + "\n");
    		}
    		out.close();
    	}
    	catch (java.io.IOException e) {
    		Toast.makeText(getApplicationContext(), "IOException in writeServerConfig: " + e.getMessage() + " | " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    		return;
    	}
    }
    
    public void readServerConfig() {
    	servers = new ArrayList<ServerDetails>();
    	try {
    		File input = new File(SERVER_FILENAME);
    		if (!input.exists()) {
    			Toast.makeText(getApplicationContext(), "File does not exist: " + SERVER_FILENAME, Toast.LENGTH_SHORT).show();
    			input.createNewFile();
    		}
    		if (!input.canRead()) {
    			Toast.makeText(getApplicationContext(), "canRead = False: " + SERVER_FILENAME, Toast.LENGTH_SHORT).show();
    			return;
    		}
    		if (!input.canWrite()) {
    			Toast.makeText(getApplicationContext(), "canWrite = False: " + SERVER_FILENAME, Toast.LENGTH_SHORT).show();
    			return;
    		}
    		if (!input.isFile()) {
    			Toast.makeText(getApplicationContext(), "Not a file: " + SERVER_FILENAME, Toast.LENGTH_SHORT).show();
    			return;
    		}
    		FileInputStream fis = new FileInputStream(input);
    		InputStreamReader isr = new InputStreamReader(fis);
    		BufferedReader in = new BufferedReader(isr);
    	    String line;
    	    while ((line = in.readLine()) != null) {
    	    	  servers.add(ServerDetails.toServerDetails(line));
    	    }
    	    in.close();
    	}
    	catch (java.io.FileNotFoundException e) {
    		Toast.makeText(getApplicationContext(), "FileNotFoundException in readServerConfig: " + e.getMessage() + " | " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    		return;
    	}
    	catch (java.io.IOException e) {
    		Toast.makeText(getApplicationContext(), "IOException in readServerConfig: " + e.getMessage() + " | " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    		return;
    	}
    }
}