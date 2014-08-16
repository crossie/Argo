package com.sysu.bbs.argo.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.adapter.PostHeadAdapter;
import com.sysu.bbs.argo.api.dao.PostHead;

public class TopicFragment extends AbstractBoardFragment<PostHead> {

	PostHeadAdapter mPostHeadAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_topic, container, false);
		
		mListView = (PullToRefreshListView) v.findViewById(android.R.id.list);
		mListView.setOnRefreshListener(this);
		mListView.setMode(Mode.BOTH);
		mListView.setEmptyView(v.findViewById(android.R.id.empty));
		
		mType = "topic";
		mPostHeadAdapter = new PostHeadAdapter(getActivity(), android.R.layout.simple_list_item_1, mDataList);
		mAdapter = mPostHeadAdapter;
		mListView.setAdapter(mAdapter);
		
		return v;
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

}
