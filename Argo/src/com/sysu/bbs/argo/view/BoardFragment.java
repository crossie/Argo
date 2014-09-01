package com.sysu.bbs.argo.view;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sysu.bbs.argo.AddPostActivity;
import com.sysu.bbs.argo.R;

public class BoardFragment extends Fragment  {

	private AbstractBoardFragment mCurrFragment;
	private NormalFragment mNormalFragment;
	private TopicFragment mTopicFragment;
	private String mCurrBoard;
	private static String FRAG_TAG_NORMAL = "FRAG_TAG_NORMAL";
	private static String FRAG_TAG_TOPIC = "FRAG_TAG_TOPIC";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_board, container, false);		
		setHasOptionsMenu(true);
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		FragmentManager fm = getChildFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
				
		if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("post_mode", true)) {
			mNormalFragment = (NormalFragment) fm.findFragmentByTag(FRAG_TAG_NORMAL);
			if (mNormalFragment == null) {
				mNormalFragment = new NormalFragment();
				ft.add(R.id.frag_board, mNormalFragment, FRAG_TAG_NORMAL);
			}			
			mCurrFragment = mNormalFragment;
		} else {
			mTopicFragment = (TopicFragment) fm.findFragmentByTag(FRAG_TAG_TOPIC);
			if (mTopicFragment == null) {
				mTopicFragment = new TopicFragment();
				ft.add(R.id.frag_board, mTopicFragment, FRAG_TAG_TOPIC);
			}
			mCurrFragment = mTopicFragment;
		}
		ft.commit();
		
		super.onActivityCreated(savedInstanceState);
	}

	public void changeBoard(String boardname) {
		if (!boardname.equals(mCurrBoard)) {
			mCurrBoard = boardname;
			mCurrFragment.changeBoard(mCurrBoard);
		}
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_board_fragment, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem normalMode = menu.findItem(R.id.view_mode_normal);
		MenuItem topicMode = menu.findItem(R.id.view_mode_topic);
		MenuItem addPost = menu.findItem(R.id.add_new_topic);
		if (isHidden()) {
			addPost.setVisible(false);
			normalMode.setVisible(false);
			topicMode.setVisible(false);
		} else {
			addPost.setVisible(true);
			if (mCurrFragment == mNormalFragment) {
				normalMode.setVisible(false);
				topicMode.setVisible(true);
			} else if (mCurrFragment == mTopicFragment) {
				normalMode.setVisible(true);
				topicMode.setVisible(false);
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FragmentManager fm = getChildFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		ft.hide(mCurrFragment);
		switch(item.getItemId()) {
		case R.id.view_mode_normal:
			mNormalFragment = (NormalFragment) fm.findFragmentByTag(FRAG_TAG_NORMAL);
			if (mNormalFragment == null) {
				mNormalFragment = new NormalFragment();
				ft.add(R.id.frag_board, mNormalFragment, FRAG_TAG_NORMAL);
			}			
			mCurrFragment = mNormalFragment;
			
			ft.show(mCurrFragment);
			ft.commit();
			fm.executePendingTransactions();
			
			mNormalFragment.changeBoard(mCurrBoard);
			break;
		case R.id.view_mode_topic:
			mTopicFragment = (TopicFragment) fm.findFragmentByTag(FRAG_TAG_TOPIC);
			if (mTopicFragment == null) {
				mTopicFragment = new TopicFragment();
				ft.add(R.id.frag_board, mTopicFragment, FRAG_TAG_TOPIC);
			}
			mCurrFragment = mTopicFragment;
			
			ft.show(mCurrFragment);
			ft.commit();
			fm.executePendingTransactions();
			// this should be called only when mTopicFragment is created for the first time
			//but call multi time won't hurt....
			mTopicFragment.changeBoard(mCurrBoard);
			break;
		case R.id.add_new_topic:
			Intent intent = new Intent(getActivity(), AddPostActivity.class);
			Bundle param = new Bundle();
			param.putString("type", "new");
			param.putString("boardname", mCurrBoard);

			intent.putExtras(param);

			startActivity(intent);
			break;
		}		
		return true;
	}

}
