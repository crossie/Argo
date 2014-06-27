package com.sysu.bbs.argo;

import java.util.ArrayList;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sysu.bbs.argo.adapter.PostAdapter;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.util.SimpleErrorListener;

public class TopicListActivity extends SwipeBackActivity {

	private PullToRefreshListView mTopicListView;
	private ArrayList<String> mFileNameListDesc;
	private PostAdapter mPostAdapterDesc;
	private ArrayList<String> mFileNameListAsec;
	private PostAdapter mPostAdapterAsec;
	private PostAdapter mCurrAdapter;
	private String mBoardName;
	private String mFileName;

	RequestQueue requestQueue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_topiclist);
		//TODO not working
		getSwipeBackLayout().setEdgeSize(getWindowManager().getDefaultDisplay().getWidth());
		
		Intent intent = getIntent();
		mBoardName = intent.getStringExtra("boardname");
		mFileName = intent.getStringExtra("filename");
		
		mTopicListView = (PullToRefreshListView) findViewById(R.id.activity_topic_list);
		mFileNameListDesc = new ArrayList<String>();
		mFileNameListAsec = new ArrayList<String>();
		mPostAdapterDesc = new PostAdapter(this, android.R.layout.simple_list_item_1,
				mFileNameListDesc, mBoardName);
		mPostAdapterAsec = new PostAdapter(this, android.R.layout.simple_list_item_1,
				mFileNameListAsec, mBoardName);
		mTopicListView.getRefreshableView().setAdapter(mPostAdapterDesc);
		mCurrAdapter = mPostAdapterDesc;

		requestQueue = Volley.newRequestQueue(this);

		//loadPost(0, filenames.length);
		String url = API.GET.AJAX_POST_TOPICLIST + "?boardname="
				+ mBoardName + "&filename=" + mFileName;
		requestQueue.add(new StringRequest(Method.GET, url,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						try {
							JSONObject res = new JSONObject(response);
							if (res.getString("success").equals("1")) {
								JSONArray resArray = res.getJSONArray("data");
								for (int i = 0; i < resArray.length(); i++) {
									mFileNameListDesc.add(0, resArray.getString(i));
									mFileNameListAsec.add(resArray.getString(i));
								}
								mPostAdapterDesc.notifyDataSetChanged();
							} else {
								Toast.makeText(
										TopicListActivity.this,
										"failed to get post, "
												+ res.getString("error"),
										Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							Toast.makeText(TopicListActivity.this,
									"unexpected error in getting post list",
									Toast.LENGTH_SHORT).show();
						}

					}

				}, new SimpleErrorListener(this,"")));
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_topic_list, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		MenuItem asec = menu.findItem(R.id.chronologically_asec);
		MenuItem desc = menu.findItem(R.id.chronologically_desc);
		if (mCurrAdapter == mPostAdapterDesc) {
			asec.setVisible(true);
			desc.setVisible(false);
			
		} else {
			asec.setVisible(false);
			desc.setVisible(true);
			
		}
			
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case R.id.chronologically_asec:
			mTopicListView.getRefreshableView().setAdapter(mPostAdapterAsec);
			mCurrAdapter = mPostAdapterAsec;
			break;
		case R.id.chronologically_desc:
			mTopicListView.getRefreshableView().setAdapter(mPostAdapterDesc);
			mCurrAdapter = mPostAdapterDesc;
			break;
		}

		return true;
	}
}
