package com.sysu.bbs.argo.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.Listener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.TopicListActivity;
import com.sysu.bbs.argo.adapter.PostAdapter;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Post;
import com.sysu.bbs.argo.api.dao.PostHead;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.SimpleErrorListener;
import com.sysu.bbs.argo.util.StringRequestPost;

public class NormalFragment extends AbstractBoardFragment<PostHead> implements
		OnItemClickListener {

	private PostAdapter mPostAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_normal, container, false);

		mListView = (PullToRefreshListView) v.findViewById(android.R.id.list);
		mListView.setOnRefreshListener(this);
		mListView.setMode(Mode.BOTH);
		mListView.setEmptyView(v.findViewById(android.R.id.empty));

		mType = "normal";

		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mPostAdapter = new PostAdapter(getActivity(),
				android.R.layout.simple_list_item_1, mDataList, mCurrBoard);
		mAdapter = mPostAdapter;
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		registerForContextMenu(mListView.getRefreshableView());
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	protected void add2DataList(PostHead postHead, boolean head) {
		//String filename = postHead.getFilename();
		if (head)
			mDataList.add(0, postHead);
		else
			mDataList.add(postHead);

	}

	@Override
	protected void setAdapterBoard(String board) {
		mPostAdapter.setBoardName(board);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		menu.add(R.layout.frag_normal, R.string.menu_title_share, 0, R.string.menu_title_share);
		menu.add(R.layout.frag_normal, R.string.menu_title_copy, 0, R.string.menu_title_copy);
		menu.add(R.layout.frag_normal, R.string.menu_title_topic, 0, R.string.menu_title_topic);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Post post = mPostAdapter.getPost(mPostAdapter.getItem(info.position - 1).getFilename());
		if (post.getPerm_del().equals("1")) {
			menu.add(R.layout.frag_normal, R.string.menu_title_delete, 0, R.string.menu_title_delete);
		} 
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View listItem, int pos,
			long id) {

		mListView.getRefreshableView().showContextMenuForChild(listItem);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() != R.layout.frag_normal)
			return false;

		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final PostHead postHead = mPostAdapter.getItem(info.position - 1);
		final Post post = mPostAdapter.getPost(postHead.getFilename());
		
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
		
		Intent intent = null;
		
		switch (item.getItemId()) {
		case R.string.menu_title_copy:		
			ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("post", content);
			cm.setPrimaryClip(clip);
			Toast.makeText(getActivity(), "复制成功", Toast.LENGTH_SHORT).show();
			return true;
		case R.string.menu_title_topic:
			intent = new Intent(getActivity(), TopicListActivity.class);
			intent.putExtra("boardname", post.getBoard());
			intent.putExtra("filename", post.getFilename());
			
			startActivity(intent);
			return true;
		case R.string.menu_title_share:
			intent = new Intent(Intent.ACTION_SEND);

			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "分享内容和链接");
			intent.putExtra(Intent.EXTRA_TEXT, content);
			startActivity(Intent.createChooser(intent, "分享到..."));
			return true;
		case R.string.menu_title_delete:
			
			AlertDialog.Builder builder = new AlertDialog.Builder(
					getActivity(), AlertDialog.THEME_HOLO_DARK);
			builder.setMessage("确认删除？")
			.setPositiveButton("是",new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					HashMap<String, String> param = new HashMap<String, String>();
					param.put("boardname", post.getBoard());
					param.put("filename", post.getFilename());
					StringRequestPost delRequest = new StringRequestPost(API.POST.AJAX_POST_DEL, 
							new Listener<String>() {

								@Override
								public void onResponse(String response) {									
									try {
										JSONObject res = new JSONObject(response);
										if (res.getString("success").equals("1")) {
											mAdapter.remove(postHead);
											mAdapter.notifyDataSetChanged();
											Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
										} else {
											Toast.makeText(getActivity(), "删除失败 " + res.getString("error"), Toast.LENGTH_SHORT).show();
										}
									} catch (JSONException e) {
										Toast.makeText(getActivity(), "删除失败 " + response, Toast.LENGTH_SHORT).show();
										e.printStackTrace();
									}
								}
						
							}, new SimpleErrorListener(getActivity(), "删除失败"), param);
					delRequest.setTag(API.POST.AJAX_POST_DEL);
					delRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 1));
					SessionManager.getRequestQueue().add(delRequest);
					
														
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
