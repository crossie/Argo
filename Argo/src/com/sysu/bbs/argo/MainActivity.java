package com.sysu.bbs.argo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Board;
import com.sysu.bbs.argo.api.dao.Section;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.SessionManager.LoginSuccessListener;
import com.sysu.bbs.argo.util.SimpleErrorListener;
import com.sysu.bbs.argo.util.StringRequestPost;
import com.sysu.bbs.argo.view.BaseBoardFragment;
import com.sysu.bbs.argo.view.BoardFragment;
import com.sysu.bbs.argo.view.Top10Fragment;

public class MainActivity extends FragmentActivity 
	implements OnChildClickListener, LoginSuccessListener, 
		OnRefreshListener<ExpandableListView>,OnGroupExpandListener, OnItemClickListener {

	//private ArgoSQLOpenHelper mDBHelper;// = new ArgoSQLOpenHelper(MainActivity.this, "argodb", null, 1);
	//private SQLiteDatabase db;// = mDBHelper.getWritableDatabase();
	
	private RequestQueue mRequestQueue = null;
	
	private BoardFragment mBoardFragment = null;
	private Top10Fragment mTop10Fragment = null;
	private Fragment mCurrFragment = null;
	
	private SlidingMenu mSlidingMenu;
	//private SearchView mSearchBoard;
	private AutoCompleteTextView mSearchBoard;
	private PullToRefreshExpandableListView mBoardListView;
	private List<Map<String,String>> mSectionGroupList = new ArrayList<Map<String,String>>();
	private List<Map<String,String>> mFavList = new ArrayList<Map<String,String>>();
	private List<List<Map<String,String>>> mBoardList = new ArrayList<List<Map<String,String>>>();
	private List<Map<String,String>> mSearchList = new ArrayList<Map<String,String>>();
	private SimpleExpandableListAdapter mBoardAdapter;
	private SimpleAdapter mSearchAdapter;
	
	private LinearLayout mHeader;
	
	private final String EN = "EN";
	private final String CN = "CN";
	private final String SECCODE = "SECCODE";
	private final String SECNAME = "SECNAME";
	private String mCurrBoard;
	
	private String mUserid = null;
	private boolean isFavoriteInitialized = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (!SessionManager.loginSuccessListeners.contains(this))
			SessionManager.loginSuccessListeners.add(this);
		
		mSlidingMenu = new SlidingMenu(this);
		mSlidingMenu.setMenu(R.layout.sliding_menu_left);
		mSlidingMenu.setSecondaryMenu(R.layout.sliding_menu_right);
		mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mSlidingMenu.setShadowDrawable(R.drawable.shadow);
		mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		mSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadowright);
		mSlidingMenu.setBehindOffsetRes(R.dimen.menu_offset);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		
		//mSearchBoard = (SearchView) findViewById(R.id.menu_left_search);
		//SearchManager searchManager =
		//           (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		//mSearchBoard.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		//mSearchBoard.setOnSuggestionListener(this);
		mSearchBoard = (AutoCompleteTextView) findViewById(R.id.menu_left_search);
		mSearchAdapter = new SimpleAdapter(this, mSearchList, 
				android.R.layout.simple_expandable_list_item_2, new String[]{EN, CN}, 
				new int[]{android.R.id.text1, android.R.id.text2});
		mSearchBoard.setAdapter(mSearchAdapter);
		mSearchBoard.setOnItemClickListener(this);
		
		mBoardListView = (PullToRefreshExpandableListView) mSlidingMenu.getMenu().findViewById(R.id.menu_left_list);
		mBoardAdapter = new SimpleExpandableListAdapter(this, 
				mSectionGroupList, android.R.layout.simple_expandable_list_item_1,
				new String[] { SECNAME }, 
				new int[] { android.R.id.text1 }, 
				mBoardList, android.R.layout.simple_expandable_list_item_2, 
				new String[] { EN, CN}, 
				new int[] { android.R.id.text1, android.R.id.text2});

		TextView top10Header = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
		top10Header.setText("今日十大");
		
		mHeader = new LinearLayout(this);
		mHeader.setOrientation(LinearLayout.VERTICAL);
		mHeader.addView(top10Header);
		
		mBoardListView.getRefreshableView().addHeaderView(mHeader);
		
		
		mBoardListView.getRefreshableView().setAdapter(mBoardAdapter);
		mBoardListView.setOnRefreshListener(this);
		mBoardListView.getRefreshableView().setOnChildClickListener(this);
		
		
		
		Map<String,String> favMap = new HashMap<String,String>();
		favMap.put(SECNAME, "收藏夹");
		mSectionGroupList.add(favMap);
		mBoardList.add(mFavList);
		
		File sections = new File(getFilesDir(), "sections.json");
		FileInputStream fis = null;
		BufferedReader br = null;
		if (sections.exists()) {
			try {
				fis = new FileInputStream(sections);
				br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
				String s = "", line;
				while ((line = br.readLine()) != null)
					s += line;
				Log.d("onCreate", s);
				initSections(s);
			} catch (FileNotFoundException e) {
				refreshSection();
			} catch (IOException e) {
				refreshSection();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						
					}
				}
			}
			
		} else {
			refreshSection();
		}
				
		
/*		move to session manager
		cursor = db.rawQuery("select * from user_info order by last_login desc;", null);
		while (cursor.moveToNext()) {
			String userid = cursor.getString(1);
			String passwd = cursor.getString(2);
			SessionManager sm = new SessionManager(this, userid, passwd,
					false, false);
			sm.login();
			break;
		}*/

		//db.close();
		
		mTop10Fragment = new Top10Fragment();
		getSupportFragmentManager().beginTransaction()
			.add(R.id.main_layout, mTop10Fragment).commit();
		mCurrFragment = mTop10Fragment;
	}

	public  RequestQueue getRequestQueue() {
		if (mRequestQueue == null)
			mRequestQueue = Volley.newRequestQueue(this);
		return mRequestQueue;
	}

	@Override
	public void actionAfterLogin(String userid) {
		mUserid = userid;
		refreshFavorite();
	}

	//@Override
	public void passParam(String username, String password, boolean saveUser,
			boolean savePassword) {
		SessionManager sm = new SessionManager(this, username, password,
				saveUser, savePassword);
		sm.login();

	}

	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.post_popup, menu);
	}

	@Override
	public void onRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
		
		String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
				DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

		refreshSection();
		if (SessionManager.isLoggedIn)
			refreshFavorite();
		//TODO stop refresh and set last update time
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		if (!SessionManager.isLoggedIn || groupPosition != 0 || isFavoriteInitialized)
			return;
				
		File sections = new File(getFilesDir(), mUserid + ".json");
		FileInputStream fis = null;
		BufferedReader br = null;
		if (sections.exists()) {
			try {
				fis = new FileInputStream(sections);
				br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));	
				String s = "", line;
				while ((line = br.readLine()) != null)
					s += line;
				Log.d("onGroupExpand", s);
				initFav(s);
			} catch (FileNotFoundException e) {
				refreshFavorite();
			} catch (IOException e) {
				refreshFavorite();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						
					}
				}
			}
			
		} else {
			refreshFavorite();
		}
		
	}
	
	private Map<String,String> newBoardItem(String en, String cn) {
		Map<String,String> tmp = new HashMap<String,String>();
		tmp.put(EN, en);
		tmp.put(CN, cn);
		return tmp;
	}
	
	private void refreshFavorite() {
		
		getRequestQueue().add(new StringRequest(Method.GET,
				API.GET.AJAX_USER_FAV, new Listener<String>() {
					@Override
					public void onResponse(String response) {
						FileOutputStream fos = null;
						BufferedWriter bw = null;
						try {
							JSONObject org = new JSONObject(response);
							if (org.getString("success").equals("1")) {

								mFavList.clear();
								
								//initTop10AndMail();
								JSONArray arr = org.getJSONArray("data");
								initFav(arr.toString());
								File file = new File(getFilesDir(), mUserid + ".json");
								fos = new FileOutputStream(file);
								bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
								bw.write(arr.toString());
								Log.d("refreshFavorite", arr.toString());
								
								mBoardAdapter.notifyDataSetChanged();
								
								isFavoriteInitialized = true;
							}
						} catch (JSONException e) {
								Toast.makeText(MainActivity.this, "unexpected error in getting favorites",
										Toast.LENGTH_LONG).show();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							mBoardListView.onRefreshComplete();
							if (bw != null) {
								try {
									bw.close();
								} catch (IOException e) {
									
								}
							}
						}
					}

			}, new SimpleErrorListener(this, "网络错误,无法刷新收藏夹") {
				@Override
				public void onErrorResponse(VolleyError error) {
					mBoardListView.onRefreshComplete();
					super.onErrorResponse(error);
				}
			}));
	}
	
	
	private void refreshSection() {
		
		getRequestQueue().add(new StringRequest(Method.GET,
				API.GET.AJAX_BOARD_ALLS, new Listener<String>() {
					@Override
					public void onResponse(String response) {
						FileOutputStream fos = null;
						BufferedWriter bw = null;
						try {
							JSONObject org = new JSONObject(response);
							if (org.getString("success").equals("1")) {
																
								mBoardList.clear();
								mSectionGroupList.clear();
								mSearchList.clear();
																
								Map<String,String> favMap = new HashMap<String,String>();
								favMap.put(SECNAME, "收藏夹");
								mSectionGroupList.add(favMap);
								mBoardList.add(mFavList);
								
								File file = new File(getFilesDir(), "sections.json");
								fos = new FileOutputStream(file);
								bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
								
								JSONArray arr = org.getJSONObject("data").getJSONArray("all");
								
								bw.write(arr.toString());
								Log.d("refreshSection", arr.toString());
								initSections(arr.toString());
													
								mBoardAdapter.notifyDataSetChanged();
								mSearchAdapter.notifyDataSetChanged();
							}
						}catch (JSONException e) {
							Toast.makeText(MainActivity.this, "unexpected error in getting favorites",
									Toast.LENGTH_LONG).show();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							if (!SessionManager.isLoggedIn) 
								mBoardListView.onRefreshComplete();
							if (bw != null) {
								try {
									bw.close();
								} catch (IOException e) {
									
								}
							}
						}
					}

		}, new SimpleErrorListener(this, "网络错误,无法刷新") {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (!SessionManager.isLoggedIn) 
					mBoardListView.onRefreshComplete();
				super.onErrorResponse(error);
			}
		}));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		if (mCurrFragment instanceof BaseBoardFragment)
			return true;
		else
			return false;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		boolean isFavorite = false;
		List<Map<String, String>> favoriteList = mBoardList.get(0);
		for (Map<String, String> item: favoriteList) {
			Collection<String> favorites = item.values();
			if (favorites.contains(mCurrBoard)) {
				isFavorite = true;
				break;
			}
		}
		MenuItem deleteFavorite = menu.findItem(R.id.delete_from_favorite);
		MenuItem addToFavorite = menu.findItem(R.id.add_to_favorite);
		if (isFavorite) {
			deleteFavorite.setVisible(true);
			addToFavorite.setVisible(false);
		} else {
			deleteFavorite.setVisible(false);
			addToFavorite.setVisible(true);
		}
		
		if (!SessionManager.isLoggedIn) {
			deleteFavorite.setVisible(false);
			addToFavorite.setVisible(false);
		}
		return false;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("boardname", mCurrBoard);
		String url = null;

		switch(item.getItemId()) {
		case R.id.delete_from_favorite:
			url = API.POST.AJAX_USER_DELFAV;
			break;
		case R.id.add_to_favorite:
			url = API.POST.AJAX_USER_ADDFAV;
			break;
		default:
			return false;
				
		}
		
		mRequestQueue.add(new StringRequestPost(url, new Listener<String>(){

			@Override
			public void onResponse(String response) {
				try {
					JSONObject res = new JSONObject(response);
					if (res.getString("success").equals("1")) {
						Toast.makeText(MainActivity.this, "操作成功",
								Toast.LENGTH_LONG).show();
						
						//List<Map<String, String>> favoriteList = mBoardList.get(0);
						for (Map<String, String> item: mFavList) {
							Collection<String> favorites = item.values();
							if (favorites.contains(mCurrBoard)) {
								mFavList.remove(item);
								mBoardAdapter.notifyDataSetChanged();
								invalidateOptionsMenu();
								break;
							}
						}
					} else {
						Toast.makeText(MainActivity.this, "操作失败," + res.getString("error"),
								Toast.LENGTH_LONG).show();
					}
					
				} catch (JSONException e) {
					Toast.makeText(MainActivity.this, "unexpected error in modifying favorites",
							Toast.LENGTH_LONG).show();
				}
				
			}
			
		}, new SimpleErrorListener(this, null),param));
		
		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View item, int groupPosition,
			int childPosition, long id) {
		
		TextView en = (TextView) item.findViewById(android.R.id.text1);
		String boardname = en.getText().toString();
		
		changeBoard(boardname);
		return false;
	}

	private void changeBoard(String boardname) {
		mSlidingMenu.showContent();
		getActionBar().setTitle(boardname);	
		mCurrBoard = boardname;

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		if (mBoardFragment == null) {
			mBoardFragment = new BoardFragment();
			ft.add(R.id.main_layout, mBoardFragment);
		}
		if (mCurrFragment != null)
			ft.hide(mCurrFragment);

		mCurrFragment = mBoardFragment;
		ft.show(mCurrFragment);
		ft.commit();
		fm.executePendingTransactions();
		mBoardFragment.changeBoard(boardname);
			
		invalidateOptionsMenu();
	}

	//when an item of search suggestion is clicked
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		TextView en = (TextView) view.findViewById(android.R.id.text1);		
		String boardname = en.getText().toString();
		mSearchBoard.setText("");
		changeBoard(boardname);
		
		View v=this.getCurrentFocus();
	    if ( v == null )
	        return;
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		
	}

	private void initFav(String string) {
		try {
			JSONArray arr = new JSONArray(string);
			for (int i = 0; i < arr.length(); i++) {
				Board board = new Board(arr.getJSONObject(i));
				mFavList.add(newBoardItem(board.getBoardname(), board.getTitle()));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void initSections(String string) {
		try {
			JSONArray arr = new JSONArray(string);
			for (int i = 0; i < arr.length(); i++) {
				Section sec = new Section(arr.getJSONObject(i));
				Map<String, String> tmp = new HashMap<String, String>();
				tmp.put(SECCODE, sec.getSeccode());
				tmp.put(SECNAME, sec.getSecname());
				mSectionGroupList.add(tmp);

				JSONArray boards = sec.getBoards();
				List<Map<String, String>> childList = new ArrayList<Map<String, String>>();
				for (int j = 0; j < boards.length(); j++) {
					JSONObject board = boards.getJSONObject(j);
					Map<String, String> tmpChild = newBoardItem(
							board.getString("boardname"), board.getString("title"));
					childList.add(tmpChild);
					mSearchList.add(tmpChild);
				}
				mBoardList.add(childList);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
