package com.sysu.bbs.argo.adapter.pager;

import java.util.ArrayList;

import com.sysu.bbs.argo.view.ImageViewerFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ImageViewerPagerAdapter extends FragmentPagerAdapter {

	private ArrayList<String> mImageUrlList;
	public ImageViewerPagerAdapter(FragmentManager fm, ArrayList<String> url) {
		super(fm);
		mImageUrlList = url;
	}

	@Override
	public Fragment getItem(int pos) {
		ImageViewerFragment frag = new ImageViewerFragment();
		Bundle bundle = new Bundle();
		bundle.putString(ImageViewerFragment.IMAGE_URL_KEY, mImageUrlList.get(pos));
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mImageUrlList.size();
	}

}
