package com.sysu.bbs.argo.view;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.TopicListActivity;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.SimpleErrorListener;

abstract public class AbstractHomeFragment<T extends Parcelable> extends Fragment 
	implements OnRefreshListener<ListView>, OnItemClickListener{
	
	protected ArrayAdapter<T> mAdapter;
	protected ArrayList<T> mDataList = null;
	protected TextView mEmptyView = null;	
	protected PullToRefreshListView mPullRefreshListView;
	protected String mUrl;
	protected int mItemLayout;
	protected static final String OUTSTATE_HOME_KEY = "OUTSTATE_HOME_KEY";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View  v =  inflater.inflate(R.layout.frag_home, container, false);
		mPullRefreshListView = (PullToRefreshListView) v.findViewById(R.id.frag_home_list);

		mPullRefreshListView.setOnRefreshListener(this);
		mPullRefreshListView.setOnItemClickListener(this);
				
		mEmptyView = (TextView) v.findViewById(R.id.frag_home_empty);

		//mPullRefreshListView.getRefreshableView().setEmptyView(mEmptyView);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		if (savedInstanceState == null) {
			mDataList = new ArrayList<T>();
		} else {
			mDataList = savedInstanceState.getParcelableArrayList(OUTSTATE_HOME_KEY);
		}
		
		mAdapter = newAdapter();
		mPullRefreshListView.setAdapter(mAdapter);
		
		if (savedInstanceState == null || mDataList.size() == 0) {
			mEmptyView.setText(R.string.top10_loading);
			mEmptyView.setVisibility(View.VISIBLE);
			refresh();
			mPullRefreshListView.setRefreshing();
		}
		
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(OUTSTATE_HOME_KEY, mDataList);
		super.onSaveInstanceState(outState);
	}
	
	private void refresh() {
		StringRequest getData = new StringRequest(Method.GET, mUrl,
				new Listener<String>() {

					@Override
					public void onResponse(String _response) {
						mPullRefreshListView.onRefreshComplete();
						response(_response);

					}

				}, new SimpleErrorListener(getActivity(), "网络错误，无法加载今日十大") {
					@Override
					public void onErrorResponse(VolleyError error) {
						mPullRefreshListView.onRefreshComplete();
						if (mDataList.size() == 0) {
							mEmptyView.setText(R.string.top10_load_fail);
							mEmptyView.setVisibility(View.VISIBLE);
						}
						super.onErrorResponse(error);
					}
				});
		getData.setTag(mUrl);
		getData.setRetryPolicy(new DefaultRetryPolicy(3000, 2, 2));
		SessionManager.getRequestQueue().add(getData);
	}
	
	protected void response(String _response) {
		try {
			JSONObject result = new JSONObject(_response);
			if (result.getString("success").equals("1")) {
				mEmptyView.setVisibility(View.GONE);
				JSONArray dataArray = result.getJSONArray("data");
				mDataList.clear();
				for (int i = 0; i < dataArray.length(); i++) {
					T tmp = newItem(dataArray.getJSONObject(i));
					
					mDataList.add(tmp);
				}
				mAdapter.notifyDataSetChanged();
			}
		} catch (JSONException e) {
			mEmptyView.setVisibility(View.VISIBLE);
			mEmptyView.setText(R.string.top10_unknown_error);
			Toast.makeText(getActivity(), "unexpected error in getting top 10",
					Toast.LENGTH_LONG).show();
		}
	}
	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
				DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		refresh();
		mEmptyView.setText(R.string.top10_loading);
	}
	@Override
	public void onItemClick(AdapterView<?> listView, View item, int position, long row) {
		
		Intent intent = new Intent(getActivity(), TopicListActivity.class);
		intent.putExtra("boardname", getBoard(mDataList.get(position-1)));
		intent.putExtra("filename", getFilename(mDataList.get(position-1)));
		
		startActivity(intent);
		
	}
	
	abstract public String getBoard(T item);
	abstract public String getFilename(T item);
	abstract public T newItem(JSONObject json);
	abstract public ArrayAdapter<T> newAdapter();
}
