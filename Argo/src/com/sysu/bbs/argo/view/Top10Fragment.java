package com.sysu.bbs.argo.view;

import com.sysu.bbs.argo.R;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Top10Fragment extends ListFragment {
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
	return inflater.inflate(R.layout.frag_top10, container, false);
}
}
