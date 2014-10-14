package com.sysu.bbs.argo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
/**
 * 用于实现站内信，短信息等站内私人通信功能
 * @author scim
 *
 */
public class PrivateFragmentPagerAdapter extends AbstractFragmentPagerAdapter {

	public PrivateFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] getTabTitle() {
		// TODO Auto-generated method stub
		return new String[] {"站内信", "短信息"};
	}



}
