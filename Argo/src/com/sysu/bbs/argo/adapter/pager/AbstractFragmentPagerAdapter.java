package com.sysu.bbs.argo.adapter.pager;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

abstract public class AbstractFragmentPagerAdapter extends FragmentPagerAdapter {

	public AbstractFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}
	
	abstract public String[] getTabTitle();

}
