package com.sysu.bbs.argo.view;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.sysu.bbs.argo.R;

public class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
		super.onActivityCreated(savedInstanceState);
	}
}
