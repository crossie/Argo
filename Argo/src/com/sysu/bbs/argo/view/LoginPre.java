package com.sysu.bbs.argo.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sysu.bbs.argo.R;

public class LoginPre extends Fragment {
	View loginView; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		loginView = inflater.inflate(R.layout.login_pre, container, false);
		
		return loginView;
	}

}
