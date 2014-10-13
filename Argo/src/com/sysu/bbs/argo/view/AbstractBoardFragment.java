package com.sysu.bbs.argo.view;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.PostHead;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.SimpleErrorListener;
/**
 * 阅读模式的基类. <br/>
 * 主题模式, 帖子模式实现此类<br/>
 * 后续计划添加文摘模式和精华区模式
 * @author scim
 *
 * @param <T>
 * @see BoardFragment
 * @see NormalFragment
 * @see TopicFragment
 */
abstract public class AbstractBoardFragment<T extends Parcelable> extends Fragment 
	implements OnRefreshListener2<ListView> {

	protected int mFirstIndex = -1, mLastIndex = -1;
	protected String mCurrBoard, mType;
	protected ArrayList<T> mDataList = null;
	protected PullToRefreshListView mListView;
	protected ArrayAdapter<T> mAdapter;
	
	private String OUTSTATE_FIRST_INDEX_KEY = "OUTSTATE_FIRST_INDEX_KEY";
	private String OUTSTATE_LAST_INDEX_KEY = "OUTSTATE_LAST_INEX_KEY";
	private String OUTSTATE_CURR_BOARD_KEY = "OUTSTATE_CURR_BOARD_KEY";
	private String OUTSTATE_TYPE_KEY = "OUTSTATE_TYPE_KEY";
	private String OUTSTATE_DATA_LIST_KEY = "OUTSTATE_DATA_LIST_KEY";

	//protected RequestQueue mRequestQueue;
	public AbstractBoardFragment() {
		
	}
	
	public AbstractBoardFragment(String boardname) {
		mCurrBoard = boardname;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			mDataList = new ArrayList<T>();
			changeBoard(mCurrBoard, true);
		}
		else {
			mFirstIndex = savedInstanceState.getInt(OUTSTATE_FIRST_INDEX_KEY);
			mLastIndex = savedInstanceState.getInt(OUTSTATE_LAST_INDEX_KEY);
			mCurrBoard = savedInstanceState.getString(OUTSTATE_CURR_BOARD_KEY);
			mType = savedInstanceState.getString(OUTSTATE_TYPE_KEY);
			mDataList = savedInstanceState.getParcelableArrayList(OUTSTATE_DATA_LIST_KEY);
			if (mDataList.size() == 0)
				changeBoard(mCurrBoard, true);
		}
		super.onActivityCreated(savedInstanceState);
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(OUTSTATE_FIRST_INDEX_KEY, mFirstIndex);
		outState.putInt(OUTSTATE_LAST_INDEX_KEY, mLastIndex);
		outState.putString(OUTSTATE_CURR_BOARD_KEY, mCurrBoard);
		outState.putString(OUTSTATE_TYPE_KEY, mType);
		outState.putParcelableArrayList(OUTSTATE_DATA_LIST_KEY, mDataList);
		super.onSaveInstanceState(outState);
	}
	
	
	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
				DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

		if (mFirstIndex == -1)
			loadData(false, -1);
		else
			loadData(true, mFirstIndex + 1);
		
	}
	
	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
				DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

		if (mLastIndex == -1)
			loadData(false, -1);
		else
			loadData(false, mLastIndex - 20);
		
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		
		//TODO will this work ??
		//getActivity().invalidateOptionsMenu();
		super.onHiddenChanged(hidden);
	}
	
	public void changeBoard(String boardname, boolean init) {
		if (boardname.equals(mCurrBoard) && !init)
			return;
		mCurrBoard = boardname;
		//setAdapterBoard(mCurrBoard);
		mDataList.clear();
		mFirstIndex = -1;
		mLastIndex = -1;
		loadData(false, -1);
	}
	private void loadData(final boolean head, int start) {
		String url = API.GET.AJAX_POST_LIST + "?";
		url += "type=" + mType + "&boardname=" + mCurrBoard;
		
		if (start != -1)
			url = url + "&start=" + start;
		
		
		StringRequest loadPost = new StringRequest(Method.GET, url,
				new Listener<String>() {

					@Override
					public void onResponse(String response){
						try {
							mListView.onRefreshComplete();
							JSONObject result = new JSONObject(response);
							if (!result.getString("success").equals("1")) {
								if (result.getString("code").equals("504")) {
									Toast.makeText(AbstractBoardFragment.this.getActivity(),
											"没有新帖子 ",
											Toast.LENGTH_SHORT).show();
									return;
								}
								Toast.makeText(AbstractBoardFragment.this.getActivity(),
										"failed to get post, " + result.getString("error"),
										Toast.LENGTH_SHORT).show();
								mFirstIndex = -1;
								mLastIndex = -1;
								return;
							}
							JSONArray postHeadArray = result.getJSONArray("data");
							if (head) { 
								for (int index = 0; 
										index < postHeadArray.length(); index++) {
									PostHead postHead = new PostHead(
											postHeadArray.getJSONObject(index));
									postHead.setBoardname(mCurrBoard);
									add2DataList(postHead, head);
									
								}
							} else if (!head) {
								for (int index = postHeadArray.length() - 1; 
										index >= 0; index--) {
									PostHead postHead = new PostHead(
											postHeadArray.getJSONObject(index));
									postHead.setBoardname(mCurrBoard);
									add2DataList(postHead, head);
								}
							}
							mAdapter.notifyDataSetChanged();
							mLastIndex = postHeadArray.getJSONObject(0).getInt("index");
							mFirstIndex = postHeadArray.getJSONObject(postHeadArray.length()-1).getInt("index");
						} catch (JSONException e) {
							Toast.makeText(AbstractBoardFragment.this.getActivity(), 
									"unexpected error in getting post",
									Toast.LENGTH_LONG).show();
							mFirstIndex = -1;
							mLastIndex = -1;
						}
					}
				}, new SimpleErrorListener(AbstractBoardFragment.this.getActivity(), 
						"网络错误，无法加载帖子"){
					@Override
					public void onErrorResponse(VolleyError error) {
						mListView.onRefreshComplete();
						mFirstIndex = -1;
						mLastIndex = -1;
						super.onErrorResponse(error);
					}
				});
		loadPost.setRetryPolicy(new DefaultRetryPolicy(3000, 2, 2));
		loadPost.setTag(API.GET.AJAX_POST_LIST);
		SessionManager.getRequestQueue().add(loadPost);
			
	}
	
	abstract protected void add2DataList(PostHead postHead, boolean head);
	//abstract protected void setAdapterBoard(String board);
}
