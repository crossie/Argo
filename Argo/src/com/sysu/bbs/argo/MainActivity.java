package com.sysu.bbs.argo;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.sysu.bbs.argo.adapter.pager.BoardFragmentPagerAdapter;
import com.sysu.bbs.argo.adapter.pager.HomeFragmentPagerAdapter;
import com.sysu.bbs.argo.adapter.pager.PrivateFragmentPagerAdapter;
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

	/**
	 * 左侧导航菜单，可以切换到首页或者版面
	 */
	private LeftMenuFragment mLeftMenuFragment = null;
	/**
	 * 右侧菜单，设置，草稿等的入口
	 */
	private RightMenuFragment mRightMenuFragment = null;

	/**
	 * view pager that hosts the home page
	 */
	private ViewPager mHomeViewPager = null;
	/**
	 * view pager that hosts the boards that are currently open
	 */
	private ViewPager mBoardViewPager = null;
	/**
	 * view pager that hosts mailbox, sms, etc...
	 */
	private ViewPager mPrivateViewPager = null;
	/**
	 * tab列表，包含mHomeViewPager，mBoardViewPager，mPrivateViewPager的tab
	 */
	private ArrayList<Tab> mTabList = new ArrayList<Tab>();
	/**
	 * 保存tab的标题<br/>
	 * 因为不同的view pager的tab不同，标题也不同，所以一个map的entry代表一个view pager的tab title
	 */
	private HashMap<String, ArrayList<String>> mTabTitles = null;

	private BoardFragmentPagerAdapter mBoardPagerAdapter = null;
	private HomeFragmentPagerAdapter mHomePagerAdapter = null;
	private PrivateFragmentPagerAdapter mPrivatePagerAdapter = null;
	//private AbstractFragmentPagerAdapter mCurrentPagerAdapter = null;
	
	private SlidingMenu mSlidingMenu;
	
	//private String mCurrBoard;

	private static final String FRAG_TAG_LEFT_MENU = "FRAG_TAG_LEFT_MENU";
	private static final String FRAG_TAG_RIGHT_MENU = "FRAG_TAG_RIGHT_MENU";
	private static final String OUTSTATE_CURR_FRAG_KEY = "OUTSTATE_CURR_FRAG_KEY";
	private static final String OUTSTATE_TAB_TITLE_KEY = "OUTSTATE_TAB_TITLE_KEY";
	private static final String VIEW_PAGER_TYPE_HOME = "首页";
	private static final String VIEW_PAGER_TYPE_PRIVATE = "站内信";
	private static final String VIEW_PAGER_TYPE_BOARD = "BOARD";
	private static final String PREFERENCE_ISLOGGEDIN = "PREFERENCE_ISLOGGEDIN";
	/**
	 * "自动登录"的关键
	 */
	private PersistentCookieStore mCookieStore = null;
	/**
	 * 用于实现按两次返回键退出 <br/>
	 * 大于2 时退出 <br/>
	 * 其实可以用一个boolean来实现...
	 */
	private int mBackCounter = 0;
	

	/**
	 * 实现退出动画，未知为何要这样写才会有动画
	 */
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.close_enter_slide_in, R.anim.close_exit_slide_out);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setDisplayHomeAsUpEnabled(true);

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
		
		mBoardPagerAdapter = new BoardFragmentPagerAdapter(fm);
		mHomePagerAdapter = new HomeFragmentPagerAdapter(fm);
		mPrivatePagerAdapter = new PrivateFragmentPagerAdapter(fm);
		
		mHomeViewPager = (ViewPager) findViewById(R.id.home_view_pager);
		mHomeViewPager.setOnPageChangeListener(this);
		mHomeViewPager.setAdapter(mHomePagerAdapter);
		mHomeViewPager.setOffscreenPageLimit(4);
		//mCurrViewPager = mHomeViewPager;
				
		mBoardViewPager = (ViewPager) findViewById(R.id.board_view_pager);
		mBoardViewPager.setOnPageChangeListener(this);
		mBoardViewPager.setAdapter(mBoardPagerAdapter);
		mBoardViewPager.setOffscreenPageLimit(10);
		
		mPrivateViewPager = (ViewPager) findViewById(R.id.private_view_pager);
		mPrivateViewPager.setOnPageChangeListener(this);
		mPrivateViewPager.setAdapter(mPrivatePagerAdapter);
		mPrivateViewPager.setOffscreenPageLimit(4);
		
		if (savedInstanceState == null) {
			mTabTitles = new HashMap<String, ArrayList<String>>();
			ArrayList<String> home = new ArrayList<String>(Arrays.asList(mHomePagerAdapter.getTabTitle()));
			mTabTitles.put(VIEW_PAGER_TYPE_HOME, home);
			//初始化首页的tab
			initTabs(home, VIEW_PAGER_TYPE_HOME);
			//TODO 初始化站内信，短信信息的tab
			ArrayList<String> priv = new ArrayList<String>(Arrays.asList(mPrivatePagerAdapter.getTabTitle()));
			mTabTitles.put(VIEW_PAGER_TYPE_PRIVATE, priv);
			initTabs(priv, VIEW_PAGER_TYPE_PRIVATE);
			
			mTabTitles.put(VIEW_PAGER_TYPE_BOARD, new ArrayList<String>());
			//TODO not yet implement
			//getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			//changeTabs(VIEW_PAGER_TYPE_HOME);
		}
	
	}
	/**
	 * 保存cookie和登录状态
	 */
	@Override
	protected void onPause() {
		mCookieStore.persist();
		SharedPreferences sp = getPreferences(0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(PREFERENCE_ISLOGGEDIN, SessionManager.isLoggedIn);
		editor.commit();
		//overridePendingTransition(R.anim.open_enter_slide_in, R.anim.open_exit_slide_out);
		super.onPause();
	}
	
	/**
	 * 保存当前可见的是哪一个view pager
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mHomeViewPager.getVisibility() == View.VISIBLE) {
			outState.putInt(OUTSTATE_CURR_FRAG_KEY, 0);
		} else if (mBoardViewPager.getVisibility() == View.VISIBLE) {
			outState.putInt(OUTSTATE_CURR_FRAG_KEY, 1);
		} else if (mPrivateViewPager.getVisibility() == View.VISIBLE) {
			outState.putInt(OUTSTATE_CURR_FRAG_KEY, 2);
		}
		outState.putSerializable(OUTSTATE_TAB_TITLE_KEY, mTabTitles);

		super.onSaveInstanceState(outState);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		//need to call super implementation first in order to restore the fragments
		//otherwise can not get titles of mBoardPagerAdapter
		super.onRestoreInstanceState(savedInstanceState);
		mTabTitles = (HashMap<String, ArrayList<String>>) savedInstanceState.getSerializable(OUTSTATE_TAB_TITLE_KEY);
		initTabs(mTabTitles.get(VIEW_PAGER_TYPE_HOME), VIEW_PAGER_TYPE_HOME);
		initTabs(mTabTitles.get(VIEW_PAGER_TYPE_BOARD), null);
		initTabs(mTabTitles.get(VIEW_PAGER_TYPE_PRIVATE), VIEW_PAGER_TYPE_PRIVATE);
		
		int lastFrag = savedInstanceState.getInt(OUTSTATE_CURR_FRAG_KEY);
		if ( lastFrag == 0) {			
			//TODO not yet implement
			//getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			//changeTabs(VIEW_PAGER_TYPE_HOME);
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			//getActionBar().setSelectedNavigationItem(0);
			mHomeViewPager.setCurrentItem(0);
			mHomeViewPager.setVisibility(View.VISIBLE);			
			mBoardViewPager.setVisibility(View.GONE);
			mPrivateViewPager.setVisibility(View.GONE);
		}
		else if (lastFrag == 1) {			
			changeTabs("");
			if (mBoardPagerAdapter.getCount() > 1) {
				getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
				getActionBar().setSelectedNavigationItem(0);
				mBoardViewPager.setCurrentItem(0);				
			}
			mHomeViewPager.setVisibility(View.GONE);
			mBoardViewPager.setVisibility(View.VISIBLE);
			mPrivateViewPager.setVisibility(View.GONE);
		}
		else if (lastFrag == 2) {			
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			changeTabs(VIEW_PAGER_TYPE_PRIVATE);
			getActionBar().setSelectedNavigationItem(0);
			mPrivateViewPager.setCurrentItem(0);
			mHomeViewPager.setVisibility(View.GONE);
			mBoardViewPager.setVisibility(View.GONE);
			mPrivateViewPager.setVisibility(View.VISIBLE);
		}

	}
	
	@Override
	public void onBackPressed() {
		mBackCounter++;
		if (mBackCounter > 1)
			super.onBackPressed();
		else {
			if (mSlidingMenu.isMenuShowing())
				mSlidingMenu.showContent();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			if (!mSlidingMenu.isMenuShowing())
				mSlidingMenu.showMenu();
			else
				mSlidingMenu.showContent();
			return true;
		case R.id.show_right_menu:
			if (!mSlidingMenu.isSecondaryMenuShowing())				
				mSlidingMenu.showSecondaryMenu();
			else
				mSlidingMenu.showContent();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * 切换版面：切换到其他view pager，或者新打开一个版面
	 */
	@Override
	public void changeBoard(String boardname) {
		mSlidingMenu.showContent();
		switch (boardname) {
		case VIEW_PAGER_TYPE_HOME:
			//mCurrentPagerAdapter = mHomePagerAdapter;
			mHomeViewPager.setVisibility(View.VISIBLE);
			mBoardViewPager.setVisibility(View.GONE);
			//TODO NOT YET IMPLEMENT
			//getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			//changeTabs(VIEW_PAGER_TYPE_HOME);
			break;
		case VIEW_PAGER_TYPE_PRIVATE:
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			break;
		default:
			mHomeViewPager.setVisibility(View.GONE);
			mBoardViewPager.setVisibility(View.VISIBLE);
			mBoardPagerAdapter.openBoard(boardname);
			newTab(boardname);
			if (mBoardPagerAdapter.getCount() > 1)
				getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			else 
				getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			changeTabs(boardname);
			mBoardViewPager.setCurrentItem(mBoardPagerAdapter.getBoardPosition(boardname));	
			break;
		}

	}
	/**
	 * 初始化tab，第一次启动，以及onRestoreInstanceState要用
	 * @param tabTitles
	 * tab的title
	 * @param tag
	 * tab的tag
	 */
	private void initTabs(ArrayList<String> tabTitles, String tag) {
		//ActionBar actionbar = getActionBar();
		
		for (String title: tabTitles) {
			Tab tab = getActionBar().newTab();
			tab.setTabListener(this);
			tab.setText(title);
			//tab == null表示这个一个版面的tab，这里设置tab的tag为版面名称
			tab.setTag(tag == null ? title : tag);
			//actionbar.addTab(tab);
			mTabList.add(tab);
		}
	}
	/**
	 * 打开版面时，如果是新打开，则新建一个tab，否则不用做
	 * @param title
	 * 刚打开的版面的名称
	 */
	private void newTab(String title) {
		for (Tab tab: mTabList) {
			if (title.equals(tab.getTag()))
				//如果这个title的tab已经存在就不用新建了
				return;
		}
		Tab newtab = getActionBar().newTab();
		newtab.setText(title);
		newtab.setTabListener(this);
		newtab.setTag(title);
		mTabList.add(newtab);
		ArrayList<String> boards = mTabTitles.get(VIEW_PAGER_TYPE_BOARD);
		boards.add(title);
	}

	/**
	 * 版面变更后，tab也要跟着变
	 * @param boardname
	 * 可能是VIEW_PAGER_TYPE_HOME，VIEW_PAGER_TYPE_PRIVATE，或者版面名称<br/>
	 */
	private void changeTabs(String boardname) {
		ActionBar actionbar = getActionBar();
		actionbar.removeAllTabs();
		for (Tab tab: mTabList) {
			//boardname.equals(tab.getTag())，则此tab不是版面的tab，要和boardname相等才显示
			//tab.getTag() == null，此tab是版面的tab
			if (tab.getTag().equals(boardname) || 
					(!tab.getTag().equals(VIEW_PAGER_TYPE_HOME) &&
					 !tab.getTag().equals(VIEW_PAGER_TYPE_PRIVATE) &&
					 !boardname.equals(VIEW_PAGER_TYPE_HOME) &&
					 !boardname.equals(VIEW_PAGER_TYPE_PRIVATE)))
				actionbar.addTab(tab);
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

	/**
	 * 滑动切换显示的版面之后，要相应设置选中的tab
	 */
	@Override
	public void onPageSelected(int position) {
		if (getActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS)
			getActionBar().setSelectedNavigationItem(position);
		
	}

	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {

	}

	/**
	 * 点击tab后切换显示的版面
	 */
	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
		if (mHomeViewPager.getVisibility() == View.VISIBLE)
			mHomeViewPager.setCurrentItem(tab.getPosition());
		else if (mBoardViewPager.getVisibility() == View.VISIBLE)
			mBoardViewPager.setCurrentItem(tab.getPosition());
		else if (mPrivateViewPager.getVisibility() == View.VISIBLE)
			mPrivateViewPager.setCurrentItem(tab.getPosition());
		
	}

	@Override
	public void onTabUnselected(Tab arg0, android.app.FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 关闭一个版面<br/>
	 * 如果只打开了一个版面，则不操作<br/>
	 * 此外，如果删除一个之后只剩下一个版面，那么就不再显示tab<br/>
	 * 有4项要处理
	 * <li>从view pageradapter里删除这个版面对应的fragment</li>
	 * <li>从mTabTitles里删除对应的版面名称</li>
	 * <li>从action bar里删除对应位置的tab</li>
	 * <li>从mTabList里删除对应的tab</li><br/>
	 * 
	 * @param v
	 * 关闭按钮. 在xml里指定的onClick的方法都是在activity里的
	 */
	public void closeBoard(View v) {
		if (v.getId() == R.id.floating_btn_close_board /*&&
				mBoardPagerAdapter.getCount() > 1*/) {
			ActionBar actionBar = getActionBar();
			int curr = mBoardViewPager.getCurrentItem();
			String boardname = mTabTitles.get(VIEW_PAGER_TYPE_BOARD).get(curr);
			mBoardPagerAdapter.closeBoard(curr);
			mTabTitles.get(VIEW_PAGER_TYPE_BOARD).remove(curr);
			actionBar.removeTabAt(curr);
			if (mBoardPagerAdapter.getCount() == 1)
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			else if (mBoardPagerAdapter.getCount() == 0) {
				//TODO NOT YET IMPLEMENT
				//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
				//changeTabs(VIEW_PAGER_TYPE_HOME);
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
				mBoardViewPager.setVisibility(View.GONE);
				mHomeViewPager.setVisibility(View.VISIBLE);
			}
			Iterator<Tab> iter = mTabList.iterator();
			while (iter.hasNext()) {
				Tab tab = iter.next();
				if (boardname.equals(tab.getTag())) {
					iter.remove();
					break;
				}
			}
			
		}
	}
	
}
