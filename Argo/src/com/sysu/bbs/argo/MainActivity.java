package com.sysu.bbs.argo;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.sysu.bbs.argo.util.PersistentCookieStore;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.UnreadService;
import com.sysu.bbs.argo.view.AbstractBoardFragment;
import com.sysu.bbs.argo.view.BoardFragment;
import com.sysu.bbs.argo.view.LeftMenuFragment;
import com.sysu.bbs.argo.view.LeftMenuFragment.BoardChangedListener;
import com.sysu.bbs.argo.view.MailFragment;
import com.sysu.bbs.argo.view.RightMenuFragment;
import com.sysu.bbs.argo.view.Top10Fragment;

public class MainActivity extends FragmentActivity implements BoardChangedListener {

	private BoardFragment mBoardFragment = null;
	private Top10Fragment mTop10Fragment = null;
	private MailFragment mMailFragment = null;
	private Fragment mCurrFragment = null;
	private LeftMenuFragment mLeftMenuFragment = null;
	private RightMenuFragment mRightMenuFragment = null;
	
	//private Fragment mLeftMenuFragment = null;
	
	private SlidingMenu mSlidingMenu;
	//private SearchView mSearchBoard;
	
	private String mCurrBoard;
	
	protected int activityCloseEnterAnimation;
	protected int activityCloseExitAnimation;
	
	private static String FRAG_TAG_LEFT_MENU = "FRAG_TAG_LEFT_MENU";
	private static String FRAG_TAG_RIGHT_MENU = "FRAG_TAG_RIGHT_MENU";
	private static String FRAG_TAG_TOP10 = "FRAG_TAG_TOP10";
	private static String FRAG_TAG_BOARD = "FRAG_TAG_BOARD";
	
	private PersistentCookieStore mCookieStore = null;
	
	//private BroadcastReceiver mConnectionReceiver;

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
		mSlidingMenu = new SlidingMenu(this);
		mSlidingMenu.setMenu(R.layout.sliding_menu_left);
		mSlidingMenu.setSecondaryMenu(R.layout.sliding_menu_right);
		mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		//mSlidingMenu.setShadowDrawable(R.drawable.shadow);
		mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		//mSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadowright);
		mSlidingMenu.setBehindOffsetRes(R.dimen.menu_offset);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		
		mTop10Fragment = (Top10Fragment) fm.findFragmentByTag(FRAG_TAG_TOP10);
		if (mTop10Fragment == null) {
			mTop10Fragment = new Top10Fragment();
			ft.add(R.id.main_layout, mTop10Fragment, FRAG_TAG_TOP10);
		}

		mBoardFragment = (BoardFragment) fm.findFragmentByTag(FRAG_TAG_BOARD);
		if (mBoardFragment == null) {
			mBoardFragment = new BoardFragment();
			ft.add(R.id.main_layout, mBoardFragment, FRAG_TAG_BOARD);
		} 
		
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
		
		ft.hide(mBoardFragment);		
		ft.commit();

		mCurrFragment = mTop10Fragment;
		
		IntentFilter intentFilter = new IntentFilter(); 
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); 
		
/*		mConnectionReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context conn, Intent arg1) {
				ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); 
				NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); 
				NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
				if ((mobNetInfo != null && mobNetInfo.isConnected()) ||
						(wifiNetInfo != null && wifiNetInfo.isConnected())) { 
					autoLogin();
				}
			}
		};
	*/	
		//registerReceiver(mConnectionReceiver, intentFilter); 
		mCookieStore = new PersistentCookieStore(this);
		CookieManager cm = new CookieManager(mCookieStore,CookiePolicy.ACCEPT_ALL);
		//cm.setCookiePolicy();
		CookieHandler.setDefault(cm);
		
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
	protected void onDestroy() {
		Intent service = new Intent(this, UnreadService.class);
		stopService(service);
		//unregisterReceiver(mConnectionReceiver);
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		mCookieStore.persist();
		super.onPause();
	}

/*	@Override
	protected void onResume() {
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				autoLogin();
				
			}
		}, 3000);
		
		
		super.onResume();
	}*/
	
/*	private void autoLogin() {
		if (!SessionManager.isLoggedIn) {
			//auto login
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(this);
			String user = sp.getString("userid", "");
			String pwd = sp.getString("password", "");
			if (!user.equals("") &&
					!pwd.equals("")) {
				SessionManager sm = new SessionManager(this, user, pwd);
				sm.login();
			}
		}
	}*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		if (mCurrFragment instanceof AbstractBoardFragment)
			return true;
		else
			return false;
	}

	@Override
	public void changeBoard(String boardname) {
		mSlidingMenu.showContent();
		getActionBar().setTitle(boardname);	
		mCurrBoard = boardname;

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		if (mCurrFragment != null)
			ft.hide(mCurrFragment);
		
		switch (boardname) {
		case "今日十大":
			mCurrFragment = mTop10Fragment;
			break;
		case "站内信":
			if (mMailFragment == null) {
				mMailFragment = new MailFragment();
				ft.add(R.id.main_layout, mMailFragment);
			}
			mCurrFragment = mMailFragment;
			break;
		default:
		/*	if (mBoardFragment == null) {
				mBoardFragment = new BoardFragment();
				ft.add(R.id.main_layout, mBoardFragment);
			}*/
			mCurrFragment = mBoardFragment;
			break;
		}
		ft.show(mCurrFragment);
		ft.commit();
		fm.executePendingTransactions();
		
		if (!mCurrBoard.equals("今日十大") && 
				!mCurrBoard.equals("站内信") )
			mBoardFragment.changeBoard(boardname);
			
		invalidateOptionsMenu();
	}
}
