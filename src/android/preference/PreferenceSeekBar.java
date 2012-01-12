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

package android.preference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PreferenceSeekBar extends Preference implements OnSeekBarChangeListener {

	private SeekBar seekbar;
	private TextView label;
	
	public PreferenceSeekBar(Context context) {
		super(context);
	}
	public PreferenceSeekBar(Context context, AttributeSet as) {
		super(context, as);
	}
	public PreferenceSeekBar(Context context, AttributeSet as, int i) {
		super(context, as, i);
	}
	
	@Override
	public View onCreateView(ViewGroup group) {
		LinearLayout layout = new LinearLayout(getContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		seekbar = new SeekBar(getContext());
		seekbar.setMax(60);
		label = new TextView(getContext());
		seekbar.setOnSeekBarChangeListener(this);
		if (shouldPersist()) {
			seekbar.setProgress(getPersistedInt(10));
			label.setText("Refresh every " + getPersistedInt(10) + " seconds");
		}
		layout.addView(seekbar);
		layout.addView(label);
		return layout;
	}
	
	public int getValue() {
		return getPersistedInt(10);
	}
	
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (shouldPersist()) {
			persistInt(progress);
		}
		label.setText("Refresh every " + getPersistedInt(10) + " seconds");
		notifyChanged();
		
	}
	public void onStartTrackingTouch(SeekBar seekBar) {}
	public void onStopTrackingTouch(SeekBar seekBar) {}
	
}
