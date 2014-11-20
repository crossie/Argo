package com.sysu.bbs.argo.adapter.pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.sysu.bbs.argo.view.BoardFragment;
import com.sysu.bbs.argo.view.RecommendFragment;
import com.sysu.bbs.argo.view.Top10Fragment;
import com.sysu.bbs.argo.view.WhatsNewFragment;
/**
 * 首页，包含今日十大，新鲜发言，推荐文章
 * @author scim
 *
 */
public class HomeFragmentPagerAdapter extends AbstractFragmentPagerAdapter {

	private Top10Fragment mTop10Fragment = null;
	private WhatsNewFragment mNewFragment = null;
	private RecommendFragment mRecommendFragment = null;
	
	
	public HomeFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		mTop10Fragment = new Top10Fragment();
		mNewFragment = new WhatsNewFragment();
		mRecommendFragment = new RecommendFragment();
	}

	@Override
	public Fragment getItem(int pos) {
		if ( pos == 0)
			return mTop10Fragment;
		else if (pos == 1)
			return mNewFragment;
		else
			return mRecommendFragment;
	}
	
	@Override
	public int getCount() {
		return 1;
		//return 3;
	}

	@Override
	public String[] getTabTitle() {
		return new String[] {"今日十大", "新鲜发言", "推荐文章"};
	}


}
