package com.sysu.bbs.argo.view;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.sysu.bbs.argo.AddPostActivity;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.adapter.BoardFragmentPagerAdapter;
/**
 * 代表一个打开的版面，注意和AbstractBoardFragment的区别
 * @author scim
 * @see AbstractBoardFragment
 */
public class BoardFragment extends Fragment implements OnClickListener, Comparable<BoardFragment>  {

	@SuppressWarnings("rawtypes")
	private AbstractBoardFragment mCurrFragment;
	/**
	 * 帖子模式
	 */
	private NormalFragment mNormalFragment;
	/**
	 * 主题模式
	 */
	private TopicFragment mTopicFragment;
	/**
	 * 本Fragment打开的版面
	 */
	private String mCurrBoard;
	private static String FRAG_TAG_NORMAL = "FRAG_TAG_NORMAL";
	private static String FRAG_TAG_TOPIC = "FRAG_TAG_TOPIC";
	private static String OUTSTAT_CURR_BOARD = "OUTSTAT_CURR_BOARD_BoardFragment";
	private static String OUTSTAT_CURR_MODE = "OUTSTAT_CURR_MODE_BoardFragment";

	public BoardFragment() {
		
	}
	public BoardFragment(String boardname) {
		mCurrBoard = boardname;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_board, container, false);		
		view.findViewById(R.id.floating_btn_new_post).setOnClickListener(this);
		view.findViewById(R.id.floating_btn_switch_mode).setOnClickListener(this);
		setHasOptionsMenu(true);
		if (savedInstanceState != null) {
			mCurrBoard = savedInstanceState.getString(OUTSTAT_CURR_BOARD);
		}
		return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(OUTSTAT_CURR_BOARD, mCurrBoard);
		if (mCurrFragment == mNormalFragment)
			outState.putInt(OUTSTAT_CURR_MODE, 1);
		else
			outState.putInt(OUTSTAT_CURR_MODE, 2);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		FragmentManager fm = getChildFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		
		int mode = 1;
		
		if (savedInstanceState != null ) 
			mode = savedInstanceState.getInt(OUTSTAT_CURR_MODE);
		else if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("post_mode", true))
			mode = 1;
		else 
			mode = 2;
						
		mNormalFragment = (NormalFragment) fm.findFragmentByTag(FRAG_TAG_NORMAL);
		mTopicFragment = (TopicFragment) fm.findFragmentByTag(FRAG_TAG_TOPIC);
		switch (mode) {
		case 1:
			if (mNormalFragment == null) {
				mNormalFragment = new NormalFragment(mCurrBoard);
				ft.add(R.id.frag_board, mNormalFragment, FRAG_TAG_NORMAL);
			}			
			mCurrFragment = mNormalFragment;
			if (mTopicFragment != null) 
				ft.hide(mTopicFragment);
			ft.show(mNormalFragment);
			break;
		case 2:			
			if (mTopicFragment == null) {
				mTopicFragment = new TopicFragment(mCurrBoard);
				ft.add(R.id.frag_board, mTopicFragment, FRAG_TAG_TOPIC);
			}
			mCurrFragment = mTopicFragment;
			if (mNormalFragment != null)
				ft.hide(mNormalFragment);
			ft.show(mTopicFragment);
			break;
		}
		ft.commit();
		
		super.onActivityCreated(savedInstanceState);
	}
/*
	public void changeBoard(String boardname) {
		if (!boardname.equals(mCurrBoard)) {
			mCurrBoard = boardname;
			mCurrFragment.changeBoard(mCurrBoard, false);
		}
		
	}
	
*/
	public String getCurrentBoard() {
		// TODO Auto-generated method stub
		return mCurrBoard;
	}
	@Override
	public void onClick(View view) {
		FragmentManager fm = getChildFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		switch (view.getId()) {
		case R.id.floating_btn_switch_mode:
			ft.hide(mCurrFragment);
			if (mCurrFragment == mNormalFragment) {
				mTopicFragment = (TopicFragment) fm.findFragmentByTag(FRAG_TAG_TOPIC);
				if (mTopicFragment == null) {
					mTopicFragment = new TopicFragment(mCurrBoard);
					ft.add(R.id.frag_board, mTopicFragment, FRAG_TAG_TOPIC);
				}
				mCurrFragment = mTopicFragment;
			} else {
				mNormalFragment = (NormalFragment) fm.findFragmentByTag(FRAG_TAG_NORMAL);
				if (mNormalFragment == null) {
					mNormalFragment = new NormalFragment(mCurrBoard);
					ft.add(R.id.frag_board, mNormalFragment, FRAG_TAG_NORMAL);
				}			
				mCurrFragment = mNormalFragment;
			}
			ft.show(mCurrFragment);
			ft.commit();
			fm.executePendingTransactions();
			
			mCurrFragment.changeBoard(mCurrBoard, false);
			break;
		case R.id.floating_btn_new_post:
			Intent intent = new Intent(getActivity(), AddPostActivity.class);
			Bundle param = new Bundle();
			param.putString("type", "new");
			param.putString("boardname", mCurrBoard);

			intent.putExtras(param);

			startActivity(intent);
			break;
		}
		
	}
	@Override
	public int compareTo(BoardFragment frag) {
		if (frag == null)
			return 1;
		Bundle bundle = frag.getArguments();
		int pos = bundle.getInt(BoardFragmentPagerAdapter.FRAG_ARG_KEY);
		bundle = getArguments();
		int pos2 = bundle.getInt(BoardFragmentPagerAdapter.FRAG_ARG_KEY);
		
		return pos2 - pos;
	}

}
