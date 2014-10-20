package com.sysu.bbs.argo.adapter.pager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.sysu.bbs.argo.view.BoardFragment;

public class BoardFragmentPagerAdapter extends AbstractFragmentPagerAdapter {
	
	/**
	 * ��ǰ�򿪵İ����б�
	 */
	private List<BoardFragment> mOpenBoardList;
	private FragmentManager mFragmentManager;
	private int mFragMaxPos = -1;
	public static final String FRAG_ARG_KEY = "FRAG_ARG_KEY";
	public BoardFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		mFragmentManager = fm;
		mOpenBoardList = new ArrayList<BoardFragment>();
		List<Fragment> savedFrag = fm.getFragments();
		if (savedFrag != null) {
			for (Fragment frag: savedFrag) {
				if (frag instanceof BoardFragment) {
					mOpenBoardList.add((BoardFragment) frag);
					Bundle bundle = frag.getArguments();
					int pos = bundle.getInt(FRAG_ARG_KEY);
					if (pos > mFragMaxPos)
						mFragMaxPos = pos;
				}
				
			}
		}
		Collections.sort(mOpenBoardList);
	}

	@Override
	public Fragment getItem(int pos) {		
		return mOpenBoardList.get(pos);
	}

	@Override
	public int getCount() {
		return mOpenBoardList.size();
	}
	
	@Override
	public int getItemPosition(Object object) {
		if (mOpenBoardList.contains(object))
			return mOpenBoardList.indexOf(object);
		return POSITION_NONE;
	}

	/**
	 * ��δ�򿪵İ���board
	 * @param board
	 * Ҫ�򿪵İ�������
	 */
	public void openBoard(String board) {
		for (int i = 0; i < mOpenBoardList.size(); i++) {
			if (board.equals(mOpenBoardList.get(i).getCurrentBoard()))
				return;
		}
		BoardFragment boardfrag = new BoardFragment(board);
		Bundle bundle = new Bundle();
		bundle.putInt(FRAG_ARG_KEY, ++mFragMaxPos);
		boardfrag.setArguments(bundle);
		mOpenBoardList.add(boardfrag);
		notifyDataSetChanged();
	}
	/**
	 * ����board��view pager���λ��
	 * @param board
	 * Ҫ��ѯ��view pager�е�λ�ð�������
	 * @return
	 * ������view pager�е�λ��
	 */
	public int getBoardPosition(String board) {
		for (int i = 0; i < mOpenBoardList.size(); i++) {
			if (board.equals(mOpenBoardList.get(i).getCurrentBoard()))
				return i;
		}
	
		return 0;		
	}
	
	@Override
	public String[] getTabTitle() {
	
		return null;
		
	}

	public void closeBoard(int index) {
		mFragmentManager.beginTransaction().remove(mOpenBoardList.get(index)).commit();
		mOpenBoardList.remove(index);
		notifyDataSetChanged();
		
	}
	
	@Override
	public long getItemId(int position) {
		return mOpenBoardList.get(position).getCurrentBoard().hashCode();
	}

}
