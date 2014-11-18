package com.sysu.bbs.argo.view;


import java.io.File;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.sysu.bbs.argo.R;

public class SettingsFragment extends PreferenceFragment implements OnPreferenceClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		findPreference("clear_pic_cache").setOnPreferenceClickListener(this);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		if (pref.getKey().equals("clear_pic_cache")) {
			File imageDir = new File(getActivity().getFilesDir(), "Cache");
			int count = 0;
			for (File cache: imageDir.listFiles()) {
				if (cache.delete())
					count++;
			}
			Toast.makeText(getActivity(), String.format("¹²É¾³ý %d ¸öÍ¼Æ¬»º´æ", count), 
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}
}
