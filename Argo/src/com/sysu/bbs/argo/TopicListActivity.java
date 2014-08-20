package com.sysu.bbs.argo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sysu.bbs.argo.adapter.PostAdapter;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Post;
import com.sysu.bbs.argo.util.SimpleErrorListener;

public class TopicListActivity extends SwipeBackActivity implements
		OnItemClickListener {

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

		/*getSwipeBackLayout().setEdgeSize(
				getWindowManager().getDefaultDisplay().getWidth());*/
		getSwipeBackLayout().setSensitivity(this, 0.3f);
		Intent intent = getIntent();
		mBoardName = intent.getStringExtra("boardname");
		mFileName = intent.getStringExtra("filename");

		mTopicListView = (PullToRefreshListView) findViewById(R.id.activity_topic_list);
		mFileNameListDesc = new ArrayList<String>();
		mFileNameListAsec = new ArrayList<String>();
		mPostAdapterDesc = new PostAdapter(this,
				android.R.layout.simple_list_item_1, mFileNameListDesc,
				mBoardName);
		mPostAdapterAsec = new PostAdapter(this,
				android.R.layout.simple_list_item_1, mFileNameListAsec,
				mBoardName);
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("post_order", true)) {
			mTopicListView.getRefreshableView().setAdapter(mPostAdapterDesc);
			mCurrAdapter = mPostAdapterDesc;
		} else {
			mTopicListView.getRefreshableView().setAdapter(mPostAdapterAsec);
			mCurrAdapter = mPostAdapterAsec;
		}
		mTopicListView.getRefreshableView().setEmptyView(
				findViewById(R.id.topiclist_empty));
		mTopicListView.setOnItemClickListener(this);

		registerForContextMenu(mTopicListView.getRefreshableView());

		

		requestQueue = Volley.newRequestQueue(this);

		// loadPost(0, filenames.length);
		String url = API.GET.AJAX_POST_TOPICLIST + "?boardname=" + mBoardName
				+ "&filename=" + mFileName;
		requestQueue.add(new StringRequest(Method.GET, url,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						try {
							JSONObject res = new JSONObject(response);
							if (res.getString("success").equals("1")) {
								JSONArray resArray = res.getJSONArray("data");
								for (int i = 0; i < resArray.length(); i++) {
									mFileNameListDesc.add(0,
											resArray.getString(i));
									mFileNameListAsec.add(resArray.getString(i));
								}
								mPostAdapterDesc.notifyDataSetChanged();
								mPostAdapterAsec.notifyDataSetChanged();
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

				}, new SimpleErrorListener(this, "")));

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

		switch (item.getItemId()) {
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.post_popup, menu);
		menu.removeItem(R.id.menu_post_topic);
		// super.onCreateContextMenu(menu, v, menuInfo);

	}

	@Override
	public void onItemClick(AdapterView<?> listView, View listItem, int pos,
			long id) {

		mTopicListView.getRefreshableView().showContextMenuForChild(listItem);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		
		Post post = mCurrAdapter.getPost(mCurrAdapter.getItem(info.position - 1));
		
		String link = String.format("http://bbs.sysu.edu.cn/bbscon?board=%s&file=%s", post.getBoard(), post.getFilename());
		String content = "发信人: %s (%s), 信区: %s\n" 
						 + "标  题: %s\n"	
						 + "发帖时间: %s\n"
						 + "原文链接: %s\n"
						 + "%s";
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMM HH:mm   ", Locale.US);
		Calendar update = Calendar.getInstance();
		update.setTimeInMillis(1000*Long.valueOf(post.getPost_time()));
		Date date = update.getTime();
		
		content = String.format(content, post.getUserid(),
						post.getUsername(),
						post.getBoard(),
						post.getTitle(),
						sdf.format(date),
						link,
						post.getRawcontent());
		switch (item.getItemId()) {
		case R.id.menu_post_copy:
			ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("post", content);
			cm.setPrimaryClip(clip);
			Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_post_topic:
			break;
		case R.id.menu_post_share:
			Intent intent = new Intent(Intent.ACTION_SEND);

			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "分享内容和链接");
			intent.putExtra(Intent.EXTRA_TEXT, content);
			startActivity(Intent.createChooser(intent, "分享到..."));
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}
}
