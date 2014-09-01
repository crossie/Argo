package com.sysu.bbs.argo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
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
import com.sysu.bbs.argo.api.dao.PostHead;
import com.sysu.bbs.argo.util.SimpleErrorListener;
import com.sysu.bbs.argo.util.StringRequestPost;

public class TopicListActivity extends SwipeBackActivity implements
		OnItemClickListener {

	private PullToRefreshListView mTopicListView;
	private ArrayList<PostHead> mFileNameListDesc;
	private PostAdapter mPostAdapterDesc;
	private ArrayList<PostHead> mFileNameListAsec;
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
		mFileNameListDesc = new ArrayList<PostHead>();
		mFileNameListAsec = new ArrayList<PostHead>();
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
									PostHead tmp = new PostHead(resArray.getString(i));
									tmp.setBoardname(mBoardName);
									mFileNameListDesc.add(0, tmp);
									mFileNameListAsec.add(tmp);
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

		menu.add(R.layout.activity_topiclist, R.string.menu_title_copy, 0, R.string.menu_title_copy);
		menu.add(R.layout.activity_topiclist, R.string.menu_title_share, 0, R.string.menu_title_share);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Post post = mCurrAdapter.getPost(mCurrAdapter.getItem(info.position - 1).getFilename());
		if (post.getPerm_del().equals("1")) {
			menu.add(R.layout.activity_topiclist, R.string.menu_title_delete, 0, R.string.menu_title_delete);
		} 

	}

	@Override
	public void onItemClick(AdapterView<?> listView, View listItem, int pos,
			long id) {

		mTopicListView.getRefreshableView().showContextMenuForChild(listItem);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		if (item.getGroupId() != R.layout.activity_topiclist)
			return false;

		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final PostHead postHead = mCurrAdapter.getItem(info.position - 1);
		final Post post = mCurrAdapter.getPost(postHead.getFilename());
		
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
		case R.string.menu_title_copy:
			ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("post", content);
			cm.setPrimaryClip(clip);
			Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();
			return true;
		case R.string.menu_title_share:
			Intent intent = new Intent(Intent.ACTION_SEND);

			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "分享内容和链接");
			intent.putExtra(Intent.EXTRA_TEXT, content);
			startActivity(Intent.createChooser(intent, "分享到..."));
			return true;
		case R.string.menu_title_delete:
			AlertDialog.Builder builder = new AlertDialog.Builder(
					this, AlertDialog.THEME_HOLO_DARK);
			builder.setMessage("确认删除？")
			.setPositiveButton("是",new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					HashMap<String, String> param = new HashMap<String, String>();
					param.put("boardname", post.getBoard());
					param.put("filename", post.getFilename());
					requestQueue.add(new StringRequestPost(API.POST.AJAX_POST_DEL, 
							new Listener<String>() {

								@Override
								public void onResponse(String response) {
									try {
										JSONObject res = new JSONObject(response);
										if (res.getString("success").equals("1")) {
												
										} else {
											Toast.makeText(TopicListActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
										}
									} catch (JSONException e) {
										Toast.makeText(TopicListActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
										e.printStackTrace();
									}
								}
						
							}, new SimpleErrorListener(TopicListActivity.this, "删除失败"), param));
					
														
				}
			})
			.setNegativeButton("否", null)
			.show();			
			
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}
}
