package com.sysu.bbs.argo.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.TopicListActivity;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Top10;
import com.sysu.bbs.argo.util.SimpleErrorListener;

public class Top10Fragment extends Fragment 
	implements OnRefreshListener<ListView>, OnItemClickListener{

	private Top10Adapter mTop10Adapter;
	private ArrayList<Top10> mTop10List = new ArrayList<Top10>();
	private RequestQueue requestQueue = null;
	private TextView mEmptyView = null;
	
	private PullToRefreshListView mPullRefreshListView;
	
	private int[] mRankingBackground = new int[] {R.color.ranking_1,R.color.ranking_2,R.color.ranking_3,
			R.color.ranking_4,R.color.ranking_5,R.color.ranking_6,R.color.ranking_7,
			R.color.ranking_8,R.color.ranking_9,R.color.ranking_10,};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View  v =  inflater.inflate(R.layout.frag_top10, container, false);
		mPullRefreshListView = (PullToRefreshListView) v.findViewById(R.id.frag_top10_list);
		mTop10Adapter = new Top10Adapter(getActivity(), R.layout.item_top10,
				mTop10List);
		mPullRefreshListView.setOnRefreshListener(this);
		mPullRefreshListView.setOnItemClickListener(this);
		mPullRefreshListView.setAdapter(mTop10Adapter);
		
		mEmptyView = (TextView) v.findViewById(R.id.frag_top10_empty);
		mEmptyView.setText(R.string.top10_loading);
		mEmptyView.setVisibility(View.VISIBLE);
		//mPullRefreshListView.getRefreshableView().setEmptyView(mEmptyView);
		return v;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		requestQueue = Volley.newRequestQueue(getActivity());
		refresh();
		mPullRefreshListView.setRefreshing();
		super.onActivityCreated(savedInstanceState);
	}

	private void refresh() {
		requestQueue.add(new StringRequest(Method.GET, API.GET.AJAX_COMM_TOPTEN,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						mPullRefreshListView.onRefreshComplete();
						top10Response(response);

					}

				}, new SimpleErrorListener(getActivity(), "网络错误，无法加载今日十大") {
					@Override
					public void onErrorResponse(VolleyError error) {
						mPullRefreshListView.onRefreshComplete();
						mEmptyView.setText(R.string.top10_load_fail);
						mEmptyView.setVisibility(View.VISIBLE);
						super.onErrorResponse(error);
					}
				}));
	}
	private class Top10Adapter extends ArrayAdapter<Top10> {

		public Top10Adapter(Context context, int resource, List<Top10> objects) {
			super(context, resource, objects);
		}

		private class Top10Holder {
			TextView tvNum;
			TextView tvTitle;
			TextView tvAuthor;

			Top10Holder(TextView num, TextView title, TextView author) {
				tvNum = num;
				tvTitle = title;
				tvAuthor = author;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View tmp = convertView;
			Top10Holder holder = null;
			if (tmp == null) {
				tmp = LayoutInflater.from(getActivity()).inflate(
						R.layout.item_top10, parent, false);
				TextView tvNum = (TextView) tmp.findViewById(R.id.text_num);
				TextView tvTitle = (TextView) tmp.findViewById(R.id.text_title);
				TextView tvAuthor = (TextView) tmp
						.findViewById(R.id.text_user_board_time);

				holder = new Top10Holder(tvNum, tvTitle, tvAuthor);
				tmp.setTag(holder);

			} else
				holder = (Top10Holder) tmp.getTag();

			Top10 top10 = getItem(position);
			holder.tvNum.setText(String.valueOf(top10.getNum()));
			holder.tvNum.setBackgroundResource(mRankingBackground[position]);
			
			SimpleDateFormat sdf = new SimpleDateFormat("    ddMMM HH:mm", Locale.US);
			Calendar update = Calendar.getInstance();
			update.setTimeInMillis(1000*Long.valueOf(top10.getTime()));
			Date date = update.getTime();
			
			holder.tvAuthor.setText(top10.getAuthor() + "@"
					+ top10.getBoard() + sdf.format(date));
			holder.tvTitle.setText(top10.getTitle());

			return tmp;
		}

	}

	private void top10Response(String response) {
		try {
			JSONObject result = new JSONObject(response);
			if (result.getString("success").equals("1")) {
				mEmptyView.setVisibility(View.GONE);
				JSONArray top10Array = result.getJSONArray("data");
				mTop10List.clear();
				for (int i = 0; i < top10Array.length(); i++) {
					Top10 top10 = new Top10(top10Array.getJSONObject(i));
					mTop10List.add(top10);
				}
				mTop10Adapter.notifyDataSetChanged();
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
		intent.putExtra("boardname", mTop10List.get(position-1).getBoard());
		intent.putExtra("filename", mTop10List.get(position-1).getFilename());
		
		startActivity(intent);
		
	}
}
