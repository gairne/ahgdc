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

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

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
 * @author gairne
 */
public class ahgdClient extends Activity {
    /** Called when the activity is first created. */
	private HGDClient jc;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
}