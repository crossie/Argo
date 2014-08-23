package com.sysu.bbs.argo.view;

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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
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
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Board;
import com.sysu.bbs.argo.api.dao.Section;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.SimpleErrorListener;
import com.sysu.bbs.argo.util.StringRequestPost;

public class LeftMenuFragment extends DialogFragment 
	implements OnItemClickListener, OnRefreshListener<ExpandableListView>, 
		OnChildClickListener {

	private AutoCompleteTextView mSearchBoard;
	private PullToRefreshExpandableListView mBoardListView;
	private List<Map<String,String>> mSectionGroupList = new ArrayList<Map<String,String>>();
	private List<Map<String,String>> mFavList = new ArrayList<Map<String,String>>();
	private List<List<Map<String,String>>> mBoardList = new ArrayList<List<Map<String,String>>>();
	private List<Map<String,String>> mSearchList = new ArrayList<Map<String,String>>();
	private SimpleExpandableListAdapter mBoardAdapter;
	private SimpleAdapter mSearchAdapter;
	
	private final String EN = "EN";
	private final String CN = "CN";
	private final String SECCODE = "SECCODE";
	private final String SECNAME = "SECNAME";
	private LinearLayout mHeader;
	private TextView mMailHeader;
	
	private String mUserid = null;
	private String mCurrBoard;
	
	//private File mFilesDir = null;
	
	//private boolean isFavoriteInitialized = false;
	
	BoardChangedListener mBoardChangedListener;
	
	private RequestQueue mRequestQueue = null;
	
	private static final String BOARDNAME_TOP10 = "今日十大";
	
	//private Activity mSavedActivity;
	
	private BroadcastReceiver mSessionStatusReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.frag_left_menu, container, false);
		
		mBoardListView = (PullToRefreshExpandableListView) v.findViewById(R.id.menu_left_list);
		mBoardAdapter = new SimpleExpandableListAdapter(getActivity(), 
				mSectionGroupList, android.R.layout.simple_expandable_list_item_1,
				new String[] { SECNAME }, 
				new int[] { android.R.id.text1 }, 
				mBoardList, android.R.layout.simple_expandable_list_item_2, 
				new String[] { EN, CN}, 
				new int[] { android.R.id.text1, android.R.id.text2});
		
		mSearchBoard = (AutoCompleteTextView) v
				.findViewById(R.id.menu_left_search);
		mSearchAdapter = new SimpleAdapter(getActivity(), mSearchList,
				android.R.layout.simple_expandable_list_item_2,
				new String[] { EN, CN }, new int[] { android.R.id.text1,
						android.R.id.text2 });
		mSearchBoard.setAdapter(mSearchAdapter);
		mSearchBoard.setOnItemClickListener(this);
		
		if (getDialog() == null) {
			
			TextView top10Header = (TextView) inflater.inflate(
					android.R.layout.simple_list_item_1, null);
			top10Header.setText("今日十大");
			//top10Header.setCompoundDrawablesWithIntrinsicBounds(R.drawable.top10, 0, 0, 0);
			top10Header.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					mCurrBoard = BOARDNAME_TOP10;
					mBoardChangedListener.changeBoard(BOARDNAME_TOP10);

				}

			});

			mHeader = new LinearLayout(getActivity());
			mHeader.setOrientation(LinearLayout.VERTICAL);
			mHeader.addView(top10Header);
			/*		
			mMailHeader = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
			mMailHeader.setText("站内信");
			mMailHeader.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					changeBoard("站内信");
					
				}
				
			});
			
			mMailHeader.setVisibility(View.GONE);
			
			mHeader.addView(mMailHeader);
			*/
			
			mBoardListView.getRefreshableView().addHeaderView(mHeader);
		} else {
			getDialog().setTitle("选择版面");
		}

				
		mBoardListView.getRefreshableView().setAdapter(mBoardAdapter);
		mBoardListView.setOnRefreshListener(this);
		mBoardListView.getRefreshableView().setOnChildClickListener(this);
		//mBoardListView.getRefreshableView().setOnGroupExpandListener(this);
			
		Map<String,String> favMap = new HashMap<String,String>();
		favMap.put(SECNAME, "收藏夹");
		mSectionGroupList.add(favMap);
		mBoardList.add(mFavList);
		
		mSessionStatusReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context con, Intent intent) {
				String action = intent.getAction();
				if (action.equals(SessionManager.BROADCAST_LOGIN)) {
					String userid = intent.getStringExtra("userid");
					if (userid != null && !userid.equals("")) {
						succeeded(con, userid);
					} 
				} else if (action.equals(SessionManager.BROADCAST_LOGOUT)) {
					if (intent.getBooleanExtra("success", false)) {
						mFavList.clear();
						mBoardAdapter.notifyDataSetChanged();
					}
				}
				
			}
		};
				
		return v;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SessionManager.BROADCAST_LOGIN);  
		intentFilter.addAction(SessionManager.BROADCAST_LOGOUT);  
		getActivity().registerReceiver(mSessionStatusReceiver, intentFilter);
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		getActivity().unregisterReceiver(mSessionStatusReceiver);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

//		if (mFilesDir == null)
//			mFilesDir = getActivity().getFilesDir();
		
		if (SessionManager.isLoggedIn) {
			succeeded(getActivity(), SessionManager.getUsername());
		}
		
		mBoardChangedListener = (BoardChangedListener) getActivity();
		mRequestQueue = Volley.newRequestQueue(getActivity());
		
		File sections = new File(getActivity().getFilesDir(), "sections.json");
		FileInputStream fis = null;
		BufferedReader br = null;
		if (sections.exists()) {
			try {
				fis = new FileInputStream(sections);
				br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
				String s = "", line;
				while ((line = br.readLine()) != null)
					s += line;
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
		
		super.onActivityCreated(savedInstanceState);
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
	
	private Map<String,String> newBoardItem(String en, String cn) {
		Map<String,String> tmp = new HashMap<String,String>();
		tmp.put(EN, en);
		tmp.put(CN, cn);
		return tmp;
	}
	
	private void refreshSection() {
		
		mRequestQueue.add(new StringRequest(Method.GET,
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
								
								File file = new File(getActivity().getFilesDir(), "sections.json");
								fos = new FileOutputStream(file);
								bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
								
								JSONArray arr = org.getJSONObject("data").getJSONArray("all");
								
								bw.write(arr.toString());
								initSections(arr.toString());
													
								mBoardAdapter.notifyDataSetChanged();
								mSearchAdapter.notifyDataSetChanged();
							}
						}catch (JSONException e) {
							Toast.makeText(getActivity(), "unexpected error in getting favorites",
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

		}, new SimpleErrorListener(getActivity(), "网络错误,无法加载版面") {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (!SessionManager.isLoggedIn) 
					mBoardListView.onRefreshComplete();
				super.onErrorResponse(error);
			}
		}));
	}
	
	@Override
	public void onRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
		
		String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
				DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

		refreshSection();
		if (SessionManager.isLoggedIn)
			refreshFavorite();
	}
	
	private void refreshFavorite() {
		
		mRequestQueue.add(new StringRequest(Method.GET,
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
								File file = new File(getActivity().getFilesDir(), mUserid + ".json");
								fos = new FileOutputStream(file);
								bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
								bw.write(arr.toString());
								
								mBoardAdapter.notifyDataSetChanged();
								
								//isFavoriteInitialized = true;
							}
						} catch (JSONException e) {
								Toast.makeText(getActivity(), "unexpected error in getting favorites",
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

			}, new SimpleErrorListener(getActivity(), "网络错误,无法刷新收藏夹") {
				@Override
				public void onErrorResponse(VolleyError error) {
					mBoardListView.onRefreshComplete();
					super.onErrorResponse(error);
				}
			}));
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
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_main, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {

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
						Toast.makeText(getActivity(), "操作成功",
								Toast.LENGTH_LONG).show();
						
						//List<Map<String, String>> favoriteList = mBoardList.get(0);
						for (Map<String, String> item: mFavList) {
							Collection<String> favorites = item.values();
							if (favorites.contains(mCurrBoard)) {
								mFavList.remove(item);
								mBoardAdapter.notifyDataSetChanged();
								getActivity().invalidateOptionsMenu();
								break;
							}
						}
					} else {
						Toast.makeText(getActivity(), "操作失败," + res.getString("error"),
								Toast.LENGTH_LONG).show();
					}
					
				} catch (JSONException e) {
					Toast.makeText(getActivity(), "unexpected error in modifying favorites",
							Toast.LENGTH_LONG).show();
				}
				
			}
			
		}, new SimpleErrorListener(getActivity(), null),param));
		
		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View item, int groupPosition,
			int childPosition, long id) {
		
		TextView en = (TextView) item.findViewById(android.R.id.text1);
		String boardname = en.getText().toString();
		
		mCurrBoard = boardname;
		mBoardChangedListener.changeBoard(boardname);
		if (getDialog() != null) {
			getDialog().dismiss();
		}
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		TextView en = (TextView) view.findViewById(android.R.id.text1);		
		String boardname = en.getText().toString();
		mSearchBoard.setText("");
		mCurrBoard = boardname;
		mBoardChangedListener.changeBoard(boardname);
		if (getDialog() != null) {
			getDialog().dismiss();
		}
		View v=getActivity().getCurrentFocus();
	    if ( v == null )
	        return;
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		
	}

	private void succeeded(Context con, String userid) {
		//Log.i("leftmenu", "succeeded");
		mUserid = userid;
		File dir = con.getFilesDir();
		File sections = new File(dir, mUserid + ".json");
		FileInputStream fis = null;
		BufferedReader br = null;
		if (sections.exists()) {
			try {
				fis = new FileInputStream(sections);
				br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));	
				String s = "", line;
				while ((line = br.readLine()) != null)
					s += line;
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
				
		//mMailHeader.setVisibility(View.VISIBLE);

	}
		
	public interface BoardChangedListener {
		public void changeBoard(String boardname);
	}
	
}
