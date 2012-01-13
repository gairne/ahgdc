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

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity {
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
	}
	
	/**
	 * Activity has been started - perhaps after having been killed.
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }
	
	/**
     * Activity has just been started (following onCreate) or has been restarted
     * having been previously stopped
     */
    @Override
    public void onStart() {
    	super.onStart();
    }
    
    /**
     * Activity has just been resumed having been previously paused
     */
    @Override
    public void onResume() {
    	super.onResume();
    }
    
    /**
     * Activity has been paused because another activity is in the foreground (has focus)
     */
    @Override
    public void onPause() {
    	super.onPause();

    	Intent data = new Intent();
    	setResult(RESULT_OK, data);
    }
    
    /**
     * Activity has been stopped because it is no longer visible
     */
    @Override
    public void onStop() {
    	super.onStop();
    }
    
    /**
     * Activity has become visible again after being stopped
     */
    @Override
    public void onRestart() {
    	super.onRestart();
    }
    
    /**
     * Activity is about to shut down gracefully - i.e. without being killed for memory.
     */
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }	
}
