package com.sysu.bbs.argo.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.TopicListActivity;
import com.sysu.bbs.argo.adapter.PostHeadAdapter;
import com.sysu.bbs.argo.api.dao.PostHead;

public class TopicFragment extends AbstractBoardFragment<PostHead> implements OnItemClickListener {

	PostHeadAdapter mPostHeadAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_topic, container, false);
		
		mListView = (PullToRefreshListView) v.findViewById(android.R.id.list);
		mListView.setOnRefreshListener(this);
		mListView.setMode(Mode.BOTH);
		mListView.setEmptyView(v.findViewById(android.R.id.empty));
		mListView.setOnItemClickListener(this);
		
		mType = "topic";

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		mPostHeadAdapter = new PostHeadAdapter(getActivity(), android.R.layout.simple_list_item_1, mDataList);
		mAdapter = mPostHeadAdapter;
		mListView.setAdapter(mAdapter);
		//mListView.setOnLongClickListener(this);
		registerForContextMenu(mListView.getRefreshableView());
		
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	protected void add2DataList(PostHead postHead, boolean head) {
		for (PostHead tmp: mDataList) {
			if (tmp.getFilename().equals(postHead.getFilename())) 
				return;
		}
		if (head)
			mDataList.add(0, postHead);
		else
			mDataList.add(postHead);
		
	}

	@Override
	protected void setAdapterBoard(String board) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		// 
		//getActivity().getMenuInflater().inflate(R.menu.post_popup, menu);
		//menu.removeItem(R.id.menu_post_topic);
		//super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(R.layout.frag_topic, R.string.menu_title_copy, 0, R.string.menu_title_copy);
		menu.add(R.layout.frag_topic, R.string.menu_title_share, 0, R.string.menu_title_share);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() != R.layout.frag_topic)
			return false;
		
		if (!getUserVisibleHint())
			return false;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		PostHead posthead = mPostHeadAdapter.getItem(info.position - 1);
		
		String link = String.format("http://bbs.sysu.edu.cn/bbstcon?board=%s&file=%s", posthead.getBoardname(), posthead.getFilename());
		String content = "发信人: %s, 信区: %s\n" 
						 + "标  题: %s\n"	
						 + "发帖时间: %s\n"
						 + "原文链接: %s";
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMM HH:mm   ", Locale.US);
		Calendar update = Calendar.getInstance();
		update.setTimeInMillis(1000*Long.valueOf(posthead.getUpdate()));
		Date date = update.getTime();
		
		content = String.format(content, posthead.getOwner(),
				posthead.getBoardname(),
				posthead.getTitle(),
				sdf.format(date),
				link);
		
		Intent intent = null;
		
		switch (item.getItemId()) {
		case R.string.menu_title_copy:		
			ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("post", content);
			cm.setPrimaryClip(clip);
			Toast.makeText(getActivity(), "复制成功", Toast.LENGTH_SHORT).show();
			return true;

		case R.string.menu_title_share:
			intent = new Intent(Intent.ACTION_SEND);

			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "分享内容和链接");
			intent.putExtra(Intent.EXTRA_TEXT, content);
			startActivity(Intent.createChooser(intent, "分享到..."));
			return true;
		default:
			break;
		}
		return super.onContextItemSelected(item);

	}

	@Override
	public void onItemClick(AdapterView<?> listView, View listItem, int pos, long id) {

		Intent intent = new Intent(getActivity(), TopicListActivity.class);
		intent.putExtra("boardname", mPostHeadAdapter.getItem(pos - 1).getBoardname());
		intent.putExtra("filename", mPostHeadAdapter.getItem(pos - 1).getFilename());

		getActivity().startActivity(intent);
		
	}
	

}
