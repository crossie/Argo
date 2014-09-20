package com.sysu.bbs.argo;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.sysu.bbs.argo.adapter.AbstractFragmentPagerAdapter;
import com.sysu.bbs.argo.adapter.BoardFragmentPagerAdapter;
import com.sysu.bbs.argo.adapter.HomeFragmentPagerAdapter;
import com.sysu.bbs.argo.adapter.PrivateFragmentPagerAdapter;
import com.sysu.bbs.argo.util.PersistentCookieStore;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.view.LeftMenuFragment;
import com.sysu.bbs.argo.view.LeftMenuFragment.BoardChangedListener;
import com.sysu.bbs.argo.view.RightMenuFragment;
/**
 * 主程序入口
 * @author scim
 *
 */
public class MainActivity extends FragmentActivity 
	implements BoardChangedListener, OnPageChangeListener, TabListener {

	private LeftMenuFragment mLeftMenuFragment = null;
	private RightMenuFragment mRightMenuFragment = null;

	private ViewPager mHomeViewPager = null;
	private ViewPager mBoardViewPager = null;
	//private ViewPager mCurrViewPager = null;

	private BoardFragmentPagerAdapter mBoardPagerAdapter = null;
	private HomeFragmentPagerAdapter mHomePagerAdapter = null;
	private PrivateFragmentPagerAdapter mPrivatePagerAdapter = null;
	//private AbstractFragmentPagerAdapter mCurrentPagerAdapter = null;
	
	private SlidingMenu mSlidingMenu;
	
	private String mCurrBoard;
	
	protected int activityCloseEnterAnimation;
	protected int activityCloseExitAnimation;
	
	private static String FRAG_TAG_LEFT_MENU = "FRAG_TAG_LEFT_MENU";
	private static String FRAG_TAG_RIGHT_MENU = "FRAG_TAG_RIGHT_MENU";
	
	private static String PREFERENCE_ISLOGGEDIN = "PREFERENCE_ISLOGGEDIN";
	
	private PersistentCookieStore mCookieStore = null;
	/**
	 * 用于实现按两次返回键退出 <br/>
	 * 大于2 时退出
	 */
	private int mBackCounter = 0;
	

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mCookieStore = new PersistentCookieStore(this);
		CookieManager cm = new CookieManager(mCookieStore,CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cm);
		SessionManager.isLoggedIn = sessionAlive();
		SessionManager.context = this;
		

		
		mSlidingMenu = new SlidingMenu(this);
		mSlidingMenu.setMenu(R.layout.sliding_menu_left);
		mSlidingMenu.setSecondaryMenu(R.layout.sliding_menu_right);
		mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
		//mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		//mSlidingMenu.setShadowDrawable(R.drawable.shadow);
		mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		//mSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadowright);
		mSlidingMenu.setBehindOffsetRes(R.dimen.menu_offset);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
	
		mLeftMenuFragment = (LeftMenuFragment) fm.findFragmentByTag(FRAG_TAG_LEFT_MENU);
		if (mLeftMenuFragment == null) {
			mLeftMenuFragment = new LeftMenuFragment();
			ft.add(R.id.sliding_menu_left, mLeftMenuFragment, FRAG_TAG_LEFT_MENU);
		} 
		
		mRightMenuFragment = (RightMenuFragment) fm.findFragmentByTag(FRAG_TAG_RIGHT_MENU);
		if (mRightMenuFragment == null) {
			mRightMenuFragment = new RightMenuFragment();
			ft.add(R.id.sliding_menu_right, mRightMenuFragment, FRAG_TAG_RIGHT_MENU);
		} 
		
		ft.commit();
		
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mBoardPagerAdapter = new BoardFragmentPagerAdapter(fm);
		mHomePagerAdapter = new HomeFragmentPagerAdapter(fm);
		mPrivatePagerAdapter = new PrivateFragmentPagerAdapter(fm);
		
		mHomeViewPager = (ViewPager) findViewById(R.id.home_view_pager);
		mHomeViewPager.setOnPageChangeListener(this);
		mHomeViewPager.setAdapter(mHomePagerAdapter);
		//mCurrViewPager = mHomeViewPager;
		initTab(mHomePagerAdapter.getTabTitle());
		
		mBoardViewPager = (ViewPager) findViewById(R.id.board_view_pager);
		mBoardViewPager.setOnPageChangeListener(this);
		mBoardViewPager.setAdapter(mBoardPagerAdapter);
		
		//实现退出时的动画,不明白为什么要这样写才行
		TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowAnimationStyle});
		int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);      
		activityStyle.recycle();
		activityStyle = getTheme().obtainStyledAttributes(windowAnimationStyleResId, 
				new int[] {android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation});
		activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);
		activityCloseExitAnimation = activityStyle.getResourceId(1, 0);
		activityStyle.recycle();
		
	}
	
	@Override
	protected void onPause() {
		mCookieStore.persist();
		SharedPreferences sp = getPreferences(0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(PREFERENCE_ISLOGGEDIN, SessionManager.isLoggedIn);
		editor.commit();
		super.onPause();
	}
	
	@Override
	public void onBackPressed() {
		mBackCounter++;
		//Log.e("count", "" + mBackCounter);
		if (mBackCounter > 1)
			super.onBackPressed();
		else {
			Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					mBackCounter--;
					
				}
			}, 2000);
		}
	}


	@Override
	public void changeBoard(String boardname) {
		mSlidingMenu.showContent();
		//getActionBar().setTitle(boardname);
		if (boardname.equals(mCurrBoard))
			return;
		mCurrBoard = boardname;		
		
		switch (boardname) {
		case "首页":
			//mCurrentPagerAdapter = mHomePagerAdapter;
			mHomeViewPager.setVisibility(View.VISIBLE);
			mBoardViewPager.setVisibility(View.GONE);
			initTab(mHomePagerAdapter.getTabTitle());
			break;
		case "站内信":
			//mCurrentPagerAdapter = mPrivatePagerAdapter;
			break;
		default:
			//mCurrentPagerAdapter = mBoardPagerAdapter;
			mHomeViewPager.setVisibility(View.GONE);
			mBoardViewPager.setVisibility(View.VISIBLE);
			mBoardPagerAdapter.openBoard(mCurrBoard);
			initTab(mBoardPagerAdapter.getTabTitle());
			mBoardViewPager.setCurrentItem(mBoardPagerAdapter.getCount() - 1);
			break;
		}
		//mHomeViewPager.setAdapter(mCurrentPagerAdapter);
		//initTab(mCurrentPagerAdapter.getTabTitle());
		//if (mCurrentPagerAdapter == mBoardPagerAdapter) {			
		//	mHomeViewPager.setCurrentItem(mBoardPagerAdapter.getCount() - 1);
		//}
			
		invalidateOptionsMenu();
	}
	
	private void initTab(String[] tabTitle) {
		ActionBar actionBar = getActionBar();
		//tabTitle = mCurrentPagerAdapter.getTabTitle();
		actionBar.removeAllTabs();
		for (String title: tabTitle) {
			ActionBar.Tab tab = actionBar.newTab();
			tab.setText(title);
			tab.setTabListener(this);
			actionBar.addTab(tab);
		}
	}

	public boolean sessionAlive() {		

		return getPreferences(0).getBoolean(PREFERENCE_ISLOGGEDIN, false);
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		if (getActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS)
			getActionBar().setSelectedNavigationItem(position);
		
	}

	@Override
	public void onTabReselected(Tab arg0, android.app.FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
		mHomeViewPager.setCurrentItem(tab.getPosition());
		
	}

	@Override
	public void onTabUnselected(Tab arg0, android.app.FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}
}
