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
/**
 * ����һ���򿪵İ��棬ע���AbstractBoardFragment������
 * @author scim
 * @see AbstractBoardFragment
 */
public class BoardFragment extends Fragment implements OnClickListener  {

	@SuppressWarnings("rawtypes")
	private AbstractBoardFragment mCurrFragment;
	/**
	 * ����ģʽ
	 */
	private NormalFragment mNormalFragment;
	/**
	 * ����ģʽ
	 */
	private TopicFragment mTopicFragment;
	/**
	 * ��Fragment�򿪵İ���
	 */
	private String mCurrBoard;
	private static String FRAG_TAG_NORMAL = "FRAG_TAG_NORMAL";
	private static String FRAG_TAG_TOPIC = "FRAG_TAG_TOPIC";
	private static String OUTSTAT_CURR_BOARD = "OUTSTAT_CURR_BOARD";
	

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
			Log.e("BoardFragment", "onCreateView " + mCurrBoard);
		}
		return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(OUTSTAT_CURR_BOARD, mCurrBoard);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		FragmentManager fm = getChildFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
				
		if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("post_mode", true)) {
			mNormalFragment = (NormalFragment) fm.findFragmentByTag(FRAG_TAG_NORMAL);
			if (mNormalFragment == null) {
				mNormalFragment = new NormalFragment(mCurrBoard);
				ft.add(R.id.frag_board, mNormalFragment, FRAG_TAG_NORMAL);
			}			
			mCurrFragment = mNormalFragment;
		} else {
			mTopicFragment = (TopicFragment) fm.findFragmentByTag(FRAG_TAG_TOPIC);
			if (mTopicFragment == null) {
				mTopicFragment = new TopicFragment(mCurrBoard);
				ft.add(R.id.frag_board, mTopicFragment, FRAG_TAG_TOPIC);
			}
			mCurrFragment = mTopicFragment;
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

}
