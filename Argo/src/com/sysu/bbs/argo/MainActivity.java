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
 * ���������
 * @author scim
 *
 */
public class MainActivity extends FragmentActivity 
	implements BoardChangedListener, OnPageChangeListener, TabListener {

	/**
	 * ��ർ���˵��������л�����ҳ���߰���
	 */
	private LeftMenuFragment mLeftMenuFragment = null;
	/**
	 * �Ҳ�˵������ã��ݸ�ȵ����
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
	 * tab�б�����mHomeViewPager��mBoardViewPager��mPrivateViewPager��tab
	 */
	private ArrayList<Tab> mTabList = new ArrayList<Tab>();
	/**
	 * ����tab�ı���<br/>
	 * ��Ϊ��ͬ��view pager��tab��ͬ������Ҳ��ͬ������һ��map��entry����һ��view pager��tab title
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
	private static final String VIEW_PAGER_TYPE_HOME = "��ҳ";
	private static final String VIEW_PAGER_TYPE_PRIVATE = "վ����";
	private static final String VIEW_PAGER_TYPE_BOARD = "BOARD";
	private static final String PREFERENCE_ISLOGGEDIN = "PREFERENCE_ISLOGGEDIN";
	/**
	 * "�Զ���¼"�Ĺؼ�
	 */
	private PersistentCookieStore mCookieStore = null;
	/**
	 * ����ʵ�ְ����η��ؼ��˳� <br/>
	 * ����2 ʱ�˳� <br/>
	 * ��ʵ������һ��boolean��ʵ��...
	 */
	private int mBackCounter = 0;
	

	/**
	 * ʵ���˳�������δ֪Ϊ��Ҫ����д�Ż��ж���
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
			//��ʼ����ҳ��tab
			initTabs(home, VIEW_PAGER_TYPE_HOME);
			//TODO ��ʼ��վ���ţ�������Ϣ��tab
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
	 * ����cookie�͵�¼״̬
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
	 * ���浱ǰ�ɼ�������һ��view pager
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
			Toast.makeText(this, "�ٰ�һ���˳�", Toast.LENGTH_SHORT).show();
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
	 * �л����棺�л�������view pager�������´�һ������
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
	 * ��ʼ��tab����һ���������Լ�onRestoreInstanceStateҪ��
	 * @param tabTitles
	 * tab��title
	 * @param tag
	 * tab��tag
	 */
	private void initTabs(ArrayList<String> tabTitles, String tag) {
		//ActionBar actionbar = getActionBar();
		
		for (String title: tabTitles) {
			Tab tab = getActionBar().newTab();
			tab.setTabListener(this);
			tab.setText(title);
			//tab == null��ʾ���һ�������tab����������tab��tagΪ��������
			tab.setTag(tag == null ? title : tag);
			//actionbar.addTab(tab);
			mTabList.add(tab);
		}
	}
	/**
	 * �򿪰���ʱ��������´򿪣����½�һ��tab����������
	 * @param title
	 * �մ򿪵İ��������
	 */
	private void newTab(String title) {
		for (Tab tab: mTabList) {
			if (title.equals(tab.getTag()))
				//������title��tab�Ѿ����ھͲ����½���
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
	 * ��������tabҲҪ���ű�
	 * @param boardname
	 * ������VIEW_PAGER_TYPE_HOME��VIEW_PAGER_TYPE_PRIVATE�����߰�������<br/>
	 */
	private void changeTabs(String boardname) {
		ActionBar actionbar = getActionBar();
		actionbar.removeAllTabs();
		for (Tab tab: mTabList) {
			//boardname.equals(tab.getTag())�����tab���ǰ����tab��Ҫ��boardname��Ȳ���ʾ
			//tab.getTag() == null����tab�ǰ����tab
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
	 * �����л���ʾ�İ���֮��Ҫ��Ӧ����ѡ�е�tab
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
	 * ���tab���л���ʾ�İ���
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
	 * �ر�һ������<br/>
	 * ���ֻ����һ�����棬�򲻲���<br/>
	 * ���⣬���ɾ��һ��֮��ֻʣ��һ�����棬��ô�Ͳ�����ʾtab<br/>
	 * ��4��Ҫ����
	 * <li>��view pageradapter��ɾ����������Ӧ��fragment</li>
	 * <li>��mTabTitles��ɾ����Ӧ�İ�������</li>
	 * <li>��action bar��ɾ����Ӧλ�õ�tab</li>
	 * <li>��mTabList��ɾ����Ӧ��tab</li><br/>
	 * 
	 * @param v
	 * �رհ�ť. ��xml��ָ����onClick�ķ���������activity���
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
