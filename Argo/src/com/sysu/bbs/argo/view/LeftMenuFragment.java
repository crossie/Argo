package com.sysu.bbs.argo.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Board;
import com.sysu.bbs.argo.api.dao.Section;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.SimpleErrorListener;
import com.sysu.bbs.argo.util.StringRequestPost;
/**
 * 左滑菜单，用于导航，选择版面
 * @author scim
 *
 */
public class LeftMenuFragment extends DialogFragment implements
		OnItemClickListener, OnRefreshListener2<ExpandableListView>,
		OnChildClickListener {

	private final String EN = "EN";
	private final String CN = "CN";
	private final String SECCODE = "SECCODE";
	private final String SECNAME = "SECNAME";
	/**
	 * used to indicate whether there are unread posts in a board
	 */
	private final String UNREAD_MARK = " ..";
	
	private static final String OUTSTATE_SECTION_GROUP_LIST_KEY = "OUTSTATE_SECTION_GROUP_LIST_KEY";
	private static final String OUTSTATE_FAV_LIST_KEY = "OUTSTATE_FAV_LIST_KEY";
	private static final String OUTSTATE_BOARD_LIST_KEY = "OUTSTATE_BOARD_LIST_KEY";
	private static final String OUTSTATE_SEARCH_LIST_KEY = "OUTSTATE_SEARCH_LIST_KEY";
	/**
	 * used to control how many left to clear unread status
	 */
	private int mHowmanytogo;

	private AutoCompleteTextView mSearchBoard;
	private PullToRefreshExpandableListView mBoardListView;
	private ArrayList<Map<String, Spanned>> mSectionGroupList = null;
	private String[] mGroupFrom = new String[] { SECNAME };
	private int[] mGroupTo = new int[] { android.R.id.text1 };

	private ArrayList<Map<String, Spanned>> mFavList = null;
	private ArrayList<List<Map<String, Spanned>>> mBoardList = null;
	private ArrayList<Map<String, String>> mSearchList = null;
	private String[] mChildFrom = new String[] { EN, CN };
	private int[] mChildTo = new int[] { android.R.id.text1, android.R.id.text2 };

	private SimpleExpandableListAdapter mBoardAdapter;
	private SimpleAdapter mSearchAdapter;

	private LinearLayout mHeader;
	//private TextView mMailHeader;

	//private String mUserid = null;
	//private String mCurrBoard = BOARDNAME_HOME;

	BoardChangedListener mBoardChangedListener;

	//private RequestQueue mRequestQueue = null;

	private static final String BOARDNAME_HOME = "首页";
	private BroadcastReceiver mSessionStatusReceiver;
	
	ProgressDialog mClearUnreadProgressDialog = null;
	
	//private boolean mInitialize = false;

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frag_left_menu, container, false);

		if (savedInstanceState != null) {
			mSearchList = (ArrayList<Map<String, String>>) savedInstanceState.get(OUTSTATE_SEARCH_LIST_KEY);
			mBoardList = (ArrayList<List<Map<String, Spanned>>>) savedInstanceState.get(OUTSTATE_BOARD_LIST_KEY);
			mFavList = (ArrayList<Map<String, Spanned>>) savedInstanceState.get(OUTSTATE_FAV_LIST_KEY);
			mSectionGroupList = (ArrayList<Map<String, Spanned>>) savedInstanceState.get(OUTSTATE_SECTION_GROUP_LIST_KEY);
		} else {
			mSearchList = new ArrayList<Map<String, String>>();
			mBoardList = new ArrayList<List<Map<String, Spanned>>>();
			mFavList = new ArrayList<Map<String, Spanned>>();
			mSectionGroupList = new ArrayList<Map<String, Spanned>>();
			
			Map<String, Spanned> favMap = new HashMap<String, Spanned>();
			favMap.put(SECNAME, Html.fromHtml("收藏夹"));
			mSectionGroupList.add(favMap);
			mBoardList.add(mFavList);
		}
		
		mBoardListView = (PullToRefreshExpandableListView) v
				.findViewById(R.id.menu_left_list);
		mBoardAdapter = new SimpleExpandableListAdapter(getActivity(),
				mSectionGroupList,
				android.R.layout.simple_expandable_list_item_1, mGroupFrom,
				mGroupTo, mBoardList,
				android.R.layout.simple_expandable_list_item_2, mChildFrom,
				mChildTo) {
			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,
					View convertView, ViewGroup parent) {

				View v;
				if (convertView == null) {
					v = newGroupView(isExpanded, parent);
				} else {
					v = convertView;
				}
				try{
					bindView(v, mSectionGroupList.get(groupPosition), mGroupFrom,
						mGroupTo);
				}
				catch(IndexOutOfBoundsException e) {
					//TODO don't know why there are such exception occasionally
					//Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
				return v;
			}

			@Override
			public View getChildView(int groupPosition, int childPosition,
					boolean isLastChild, View convertView, ViewGroup parent) {

				View v;
				if (convertView == null) {
					v = newChildView(isLastChild, parent);
				} else {
					v = convertView;
				}
				try {
					bindView(v, mBoardList.get(groupPosition).get(childPosition),
						mChildFrom, mChildTo);
				} catch(IndexOutOfBoundsException e) {
					//TODO don't know why there are such exception occasionally
					//Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
				return v;
			}

			private void bindView(View view, Map<String, Spanned> data,
					String[] from, int[] to) {
				int len = to.length;

				for (int i = 0; i < len; i++) {
					TextView v = (TextView) view.findViewById(to[i]);
					if (v != null) {
						v.setText(data.get(from[i]));
					}
				}
			}
		};

		mSearchBoard = (AutoCompleteTextView) v
				.findViewById(R.id.menu_left_search);
		mSearchAdapter = new SimpleAdapter(getActivity(), mSearchList,
				android.R.layout.simple_expandable_list_item_2, new String[] {
						EN, CN }, new int[] { android.R.id.text1,
						android.R.id.text2 });
		mSearchBoard.setAdapter(mSearchAdapter);
		mSearchBoard.setOnItemClickListener(this);

		if (getDialog() == null) {

			TextView homeHeader = (TextView) inflater.inflate(
					android.R.layout.simple_list_item_1, null);
			homeHeader.setText(BOARDNAME_HOME);
			// top10Header.setCompoundDrawablesWithIntrinsicBounds(R.drawable.top10,
			// 0, 0, 0);
			homeHeader.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					//mCurrBoard = BOARDNAME_HOME;
					mBoardChangedListener.changeBoard(BOARDNAME_HOME);

				}

			});

			mHeader = new LinearLayout(getActivity());
			mHeader.setOrientation(LinearLayout.VERTICAL);
			mHeader.addView(homeHeader);
			/*
			 * mMailHeader = (TextView)
			 * getLayoutInflater().inflate(android.R.layout.simple_list_item_1,
			 * null); mMailHeader.setText("站内信");
			 * mMailHeader.setOnClickListener(new OnClickListener() {
			 * 
			 * @Override public void onClick(View arg0) { changeBoard("站内信");
			 * 
			 * }
			 * 
			 * });
			 * 
			 * mMailHeader.setVisibility(View.GONE);
			 * 
			 * mHeader.addView(mMailHeader);
			 */

			mBoardListView.getRefreshableView().addHeaderView(mHeader);
		} else {
			getDialog().setTitle("选择版面");
		}

		mBoardListView.getRefreshableView().setAdapter(mBoardAdapter);
		mBoardListView.setOnRefreshListener(this);
		mBoardListView.getRefreshableView().setOnChildClickListener(this);
		registerForContextMenu(mBoardListView.getRefreshableView());
			
		mSessionStatusReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context con, Intent intent) {

				String action = intent.getAction();
				if (action.equals(SessionManager.BROADCAST_LOGIN)) {
					String userid = intent.getStringExtra("userid");
					if (userid != null && !userid.equals("")) {
						//mInitialize = true;
						refreshSection();
						refreshFavorite();
					}
				} else if (action.equals(SessionManager.BROADCAST_LOGOUT)) {
					if (intent.getBooleanExtra("success", false)) {
						mFavList.clear();
						refreshSection();
						mBoardAdapter.notifyDataSetChanged();
					}
				} 
			}
		};
		
		setHasOptionsMenu(true);

		return v;
	}

	@Override
	public void onResume() {

		super.onResume();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SessionManager.BROADCAST_LOGIN);
		intentFilter.addAction(SessionManager.BROADCAST_LOGOUT);
		try {
			getActivity().registerReceiver(mSessionStatusReceiver, intentFilter);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void onStop() {

		super.onStop();
		try {
			getActivity().unregisterReceiver(mSessionStatusReceiver);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	@SuppressWarnings({ "deprecation" })
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		mBoardChangedListener = (BoardChangedListener) getActivity();
		//mRequestQueue = Volley.newRequestQueue(getActivity());
		
		mBoardListView.setPullLabel(getActivity().getString(R.string.label_pull_clear_unread), 
				Mode.PULL_FROM_END);
		mBoardListView.setReleaseLabel(getActivity().getString(R.string.label_release_clear_unread), 
				Mode.PULL_FROM_END);
		mBoardListView.setRefreshingLabel(getActivity().getString(R.string.label_refreshing_clear_unread), 
				Mode.PULL_FROM_END);
		mBoardListView.setPullLabel(getActivity().getString(R.string.label_pull), 
				Mode.PULL_FROM_START);
		mBoardListView.setReleaseLabel(getActivity().getString(R.string.label_release), 
				Mode.PULL_FROM_START);
		mBoardListView.setRefreshingLabel(getActivity().getString(R.string.label_refreshing), 
				Mode.PULL_FROM_START);

		if (savedInstanceState == null) {
			//mInitialize = true;
			if (SessionManager.isLoggedIn) {
				refreshFavorite();
			}
			
			refreshSection();
		}
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(OUTSTATE_SEARCH_LIST_KEY, mSearchList);
		outState.putSerializable(OUTSTATE_BOARD_LIST_KEY, mBoardList);
		outState.putSerializable(OUTSTATE_FAV_LIST_KEY, mFavList);
		outState.putSerializable(OUTSTATE_SECTION_GROUP_LIST_KEY, mSectionGroupList);
		super.onSaveInstanceState(outState);
	}

	private Map<String, Spanned> newBoardItem(Board board) {
		Map<String, Spanned> tmp = new HashMap<String, Spanned>();
		if (board.isUnread())
			tmp.put(EN, Html.fromHtml(board.getBoardname() 
					+ "<font color=\"#FF0000\"><sup>"
					+ UNREAD_MARK + "</sup></font>" ));
		else
			tmp.put(EN, Html.fromHtml(board.getBoardname()));
		tmp.put(CN, Html.fromHtml(board.getTitle()));
		return tmp;
	}

	private void refreshSection() {

		StringRequest getSections = new StringRequest(Method.GET,
				API.GET.AJAX_BOARD_ALLS, new Listener<String>() {
					@Override
					public void onResponse(String response) {
						try {
							JSONObject org = new JSONObject(response);
							if (org.getString("success").equals("1")) {

								mBoardList.clear();
								mSectionGroupList.clear();
								mSearchList.clear();

								Map<String, Spanned> favMap = new HashMap<String, Spanned>();
								favMap.put(SECNAME, Html.fromHtml("收藏夹"));
								mSectionGroupList.add(favMap);
								mBoardList.add(mFavList);

								JSONArray arr = org.getJSONObject("data")
										.getJSONArray("all");
								
								for (int i = 0; i < arr.length(); i++) {
									Section sec = new Section(arr.getJSONObject(i));
									Map<String, Spanned> tmp = new HashMap<String, Spanned>();
									tmp.put(SECCODE, Html.fromHtml(sec.getSeccode()));
									tmp.put(SECNAME, Html.fromHtml(sec.getSecname()));
									mSectionGroupList.add(tmp);
									
									List<Map<String, Spanned>> childList = 
											new ArrayList<Map<String, Spanned>>();									
									mBoardList.add(childList);
								}
								//separate into 2 loops to ensure order
								for (int i = 0; i < arr.length(); i++) {
									Section sec = new Section(arr.getJSONObject(i));
									getSection(sec.getSeccode(), i + 1);
								}

							}
						} catch (JSONException e) {
							Toast.makeText(getActivity(),
									"unexpected error in getting favorites",
									Toast.LENGTH_LONG).show();
						} finally {
							if (!SessionManager.isLoggedIn)
								mBoardListView.onRefreshComplete();
						}
					}

				}, new SimpleErrorListener(getActivity(), "网络错误,无法加载版面") {
					@Override
					public void onErrorResponse(VolleyError error) {
						if (!SessionManager.isLoggedIn)
							mBoardListView.onRefreshComplete();
						super.onErrorResponse(error);
					}
				});
		getSections.setRetryPolicy(new DefaultRetryPolicy(3000, 2, 2));
		getSections.setTag(API.GET.AJAX_BOARD_ALLS);
		SessionManager.getRequestQueue().add(getSections);
	}
	private void getSection(String seccode, final int grouppos) {
		String url = API.GET.AJAX_BOARD_GETBYSEC + "?sec_code=" + seccode;
		StringRequest getSection = new StringRequest(Method.GET, url,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						try {
							
							JSONObject org = new JSONObject(
									response);
							if (org.getString("success")
									.equals("1")) {
								JSONArray arr = org.getJSONArray(
										"data");
								List<Map<String, Spanned>> childList = mBoardList.get(grouppos);
								for (int i = 0; i < arr.length(); i++) {
									Board board = new Board(arr.getJSONObject(i));
									Map<String, Spanned> tmpChild = newBoardItem(board);
									childList.add(tmpChild);
									
									Map<String, String> tmpSearch = new HashMap<String, String>();
									tmpSearch.put(EN, board.getBoardname());
									tmpSearch.put(CN, board.getTitle());
									mSearchList.add(tmpSearch);
								}					
								mBoardAdapter.notifyDataSetChanged();
								mSearchAdapter.notifyDataSetChanged();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}

				}, null);
		getSection.setTag(API.GET.AJAX_BOARD_GETBYSEC);
		getSection.setRetryPolicy(new DefaultRetryPolicy(3000, 2, 2));
		SessionManager.getRequestQueue().add(getSection);
	}

	private void refreshFavorite() {

		StringRequest getFavorite = new StringRequest(Method.GET, API.GET.AJAX_USER_FAV,
				new Listener<String>() {
					@Override
					public void onResponse(String response) {
						try {
							JSONObject org = new JSONObject(response);
							if (org.getString("success").equals("1")) {

								mFavList.clear();

								JSONArray arr = org.getJSONArray("data");
								initFav(arr.toString());
								mBoardAdapter.notifyDataSetChanged();
							}
						} catch (JSONException e) {
							Toast.makeText(getActivity(),
									"unexpected error in getting favorites",
									Toast.LENGTH_LONG).show();
						} finally {
							mBoardListView.onRefreshComplete();
						}
					}

				}, new SimpleErrorListener(getActivity(), "网络错误,无法刷新收藏夹") {
					@Override
					public void onErrorResponse(VolleyError error) {
						mBoardListView.onRefreshComplete();
						super.onErrorResponse(error);
					}
				});
		getFavorite.setTag(API.GET.AJAX_USER_FAV);
		getFavorite.setRetryPolicy(new DefaultRetryPolicy(3000, 2, 2));
		SessionManager.getRequestQueue().add(getFavorite);
	}

	private void initFav(String string) {
		try {
			JSONArray arr = new JSONArray(string);
			for (int i = 0; i < arr.length(); i++) {
				Board board = new Board(arr.getJSONObject(i));
				mFavList.add(newBoardItem(board));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void manipulateFav(final int itemId, final String board) {

		HashMap<String, String> param = new HashMap<String, String>();
		param.put("boardname", board);
		String url = null;

		switch (itemId) {
		case R.string.delete_from_favorite:
			url = API.POST.AJAX_USER_DELFAV;
			break;
		case R.string.add_to_favorite:
			url = API.POST.AJAX_USER_ADDFAV;
			break;
		default:
			return;

		}

		StringRequestPost favOp = new StringRequestPost(url, new Listener<String>() {

			@Override
			public void onResponse(String response) {
				try {
					JSONObject res = new JSONObject(response);
					if (res.getString("success").equals("1")) {
						Toast.makeText(getActivity(), "操作成功", Toast.LENGTH_SHORT)
								.show();
						if (itemId == R.string.delete_from_favorite) {
							for (Map<String, Spanned> item : mFavList) {
								String boardname = item.get(EN).toString().replace(UNREAD_MARK, "");
								if (boardname.equals(board)) {
									mFavList.remove(item);
									mBoardAdapter.notifyDataSetChanged();
									getActivity().invalidateOptionsMenu();
									break;
								}
							}
						} else if (itemId == R.string.add_to_favorite) {
							refreshFavorite();
						}
					} else {
						Toast.makeText(getActivity(),
								"操作失败," + res.getString("error"),
								Toast.LENGTH_LONG).show();
					}

				} catch (JSONException e) {
					Toast.makeText(getActivity(),
							"unexpected error in modifying favorites",
							Toast.LENGTH_LONG).show();
				}

			}

		}, new SimpleErrorListener(getActivity(), null), param);
		
		favOp.setTag(url);
		favOp.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 2));
		SessionManager.getRequestQueue().add(favOp);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View item,
			int groupPosition, int childPosition, long id) {

		TextView en = (TextView) item.findViewById(android.R.id.text1);
		String boardname = en.getText().toString();

		//mCurrBoard = boardname.replace(UNREAD_MARK, "");
		mBoardChangedListener.changeBoard(boardname.replace(UNREAD_MARK, ""));
		if (getDialog() != null) {
			getDialog().dismiss();
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		TextView en = (TextView) view.findViewById(android.R.id.text1);
		String boardname = en.getText().toString();
		mSearchBoard.setText("");
		//mCurrBoard = boardname.replace(UNREAD_MARK, "");
		mBoardChangedListener.changeBoard(boardname.replace(UNREAD_MARK, ""));
		if (getDialog() != null) {
			getDialog().dismiss();
		}
		View v = getActivity().getCurrentFocus();
		if (v == null)
			return;
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);

	}

	public interface BoardChangedListener {
		public void changeBoard(String boardname);
	}

	@Override
	public void onPullDownToRefresh(
			PullToRefreshBase<ExpandableListView> refreshView) {
		String label = DateUtils.formatDateTime(getActivity(),
				System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

		refreshSection();
		if (SessionManager.isLoggedIn)
			refreshFavorite();
		
	}

	@Override
	public void onPullUpToRefresh(
			PullToRefreshBase<ExpandableListView> refreshView) {
		if (!SessionManager.isLoggedIn) {
			Toast.makeText(getActivity(), "未登录不能清除未读", Toast.LENGTH_SHORT).show();
			mBoardListView.onRefreshComplete();
			return;
		}
		
		mClearUnreadProgressDialog = new ProgressDialog(getActivity());
		mClearUnreadProgressDialog.setCancelable(true);
		mClearUnreadProgressDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface arg0) {
				SessionManager.getRequestQueue().cancelAll(API.POST.AJAX_BOARD_CLEAR);
				Toast.makeText(getActivity(), "已取消", Toast.LENGTH_SHORT).show();
				refreshFavorite();
				refreshSection();
				
			}
		});
		mClearUnreadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mClearUnreadProgressDialog.show();
			
		mHowmanytogo = 0;
		for (List<Map<String, Spanned>> boards: mBoardList) {
			mHowmanytogo += boards.size();
		}
		Iterator<List<Map<String, Spanned>>> groupIterator = mBoardList.iterator();
		while (groupIterator.hasNext()) {
			Iterator<Map<String, Spanned>> childIterator = groupIterator.next().iterator();
			while (childIterator.hasNext()) {
				String boardname = childIterator.next().get(EN).toString().replace(UNREAD_MARK, "");
				clearUnread(boardname);
			}

		}
		
		mBoardListView.onRefreshComplete();

	}

	private void clearUnread(final String boardname) {
		
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("boardname", boardname);
		
		StringRequestPost clearUnreadRequest = new StringRequestPost(API.POST.AJAX_BOARD_CLEAR, new Listener<String>() {

			@Override
			public void onResponse(String response) {
				mHowmanytogo--; 
				try {
					JSONObject res = new JSONObject(response);
					if (res.getString("success").equals("1"))
						mClearUnreadProgressDialog.setMessage( boardname + " 处理完成, 还剩 " + mHowmanytogo + "个待处理");
					else
						mClearUnreadProgressDialog.setMessage( boardname + " 处理失败");
				} catch (JSONException e) {
					mClearUnreadProgressDialog.setMessage( boardname + " 处理失败");
					e.printStackTrace();
				}
								
				if (mHowmanytogo == 0) {
					mClearUnreadProgressDialog.dismiss();
					refreshFavorite();
					refreshSection();
				}
				
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				mClearUnreadProgressDialog.dismiss();	
				Toast.makeText(getActivity(), "网络错误，清除未读失败", Toast.LENGTH_SHORT).show();
				SessionManager.getRequestQueue().cancelAll(API.POST.AJAX_BOARD_CLEAR);
				refreshFavorite();
				refreshSection();
			}
			
		}, param);
		
		clearUnreadRequest.setTag(API.POST.AJAX_BOARD_CLEAR);
		clearUnreadRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 2));
		SessionManager.getRequestQueue().add(clearUnreadRequest);
		
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (!SessionManager.isLoggedIn) 
			return;
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
		//添加清除未读的菜单
		//第一个参数用来区分是哪里的context menu被按
		//第二个参数是菜单id
		//第三个参数是菜单的顺序
		//第四个参数是菜单的文字
		//其他地方的菜单的参数意义相同
		menu.add(R.layout.frag_left_menu, R.string.clear_unread, 0, R.string.clear_unread);
		//如果长按的分组，不用添加收藏夹的操作菜单
		if (ExpandableListView.PACKED_POSITION_TYPE_GROUP 
				== ExpandableListView.getPackedPositionType(info.packedPosition))
			return;
		int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		//获取被长的版面名称
		String board = mBoardList.get(groupPos).get(childPos).
				get(EN).toString().replace(UNREAD_MARK, "");	
		
		boolean isFavorite = false;
		List<Map<String, Spanned>> favoriteList = mBoardList.get(0);
		for (Map<String, Spanned> item : favoriteList) {
			String boardname = item.get(EN).toString().replace(UNREAD_MARK, "");
			if (boardname.equals(board)) {//长按的是收藏夹中的一个
				isFavorite = true;
				break;
			}
		}
		
		if (isFavorite) {
			menu.add(R.layout.frag_left_menu, R.string.delete_from_favorite, 0, R.string.delete_from_favorite);
		} else {
			menu.add(R.layout.frag_left_menu, R.string.add_to_favorite, 0, R.string.add_to_favorite);
		}

		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() != R.layout.frag_left_menu)
			return false;
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
				.getMenuInfo();
		int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);

		switch(item.getItemId()) {
		case R.string.clear_unread:			
			mClearUnreadProgressDialog = new ProgressDialog(getActivity());
			mClearUnreadProgressDialog.setCancelable(true);
			mClearUnreadProgressDialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface arg0) {
					
					SessionManager.getRequestQueue().cancelAll(API.POST.AJAX_BOARD_CLEAR);
					//dismiss();
					Toast.makeText(getActivity(), "已取消", Toast.LENGTH_SHORT).show();
					refreshFavorite();
					refreshSection();
					
				}
			});
			mClearUnreadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mClearUnreadProgressDialog.show();
			
			mHowmanytogo = mBoardList.get(groupPos).size();
			if (ExpandableListView.PACKED_POSITION_TYPE_GROUP 
					== ExpandableListView.getPackedPositionType(info.packedPosition)) {
				Iterator<Map<String, Spanned>> childIterator = mBoardList.get(groupPos).iterator();
				while (childIterator.hasNext()) {
					String boardname = childIterator.next().get(EN).toString().replace(UNREAD_MARK, "");
					clearUnread(boardname);
				}
				
			} else {
				String boardname = mBoardList.get(groupPos).get(childPos).
						get(EN).toString().replace(UNREAD_MARK, "");
				clearUnread(boardname);
			}
			return true;
		case R.string.delete_from_favorite:
		case R.string.add_to_favorite:
			manipulateFav(item.getItemId(),
					mBoardList.get(groupPos).get(childPos).
						get(EN).toString().replace(UNREAD_MARK, ""));
			return true;
		}
		
		return super.onContextItemSelected(item);

	}

}
