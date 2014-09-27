package com.sysu.bbs.argo.adapter;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.sysu.bbs.argo.view.BoardFragment;

public class BoardFragmentPagerAdapter extends AbstractFragmentPagerAdapter {
	
	/**
	 * 当前打开的版面列表
	 */
	private List<BoardFragment> mOpenBoardList;
	private FragmentManager mFragmentManager;
	public BoardFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		mFragmentManager = fm;
		mOpenBoardList = new ArrayList<BoardFragment>();
		List<Fragment> savedFrag = fm.getFragments();
		if (savedFrag != null) {
			for (Fragment frag: savedFrag) {
				if (frag instanceof BoardFragment)
					mOpenBoardList.add((BoardFragment) frag);
			}
		}
		Log.e("BoardFragmentPagerAdapter", "BoardFragmentPagerAdapter " + mOpenBoardList.size());

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
		else
			return POSITION_NONE;
	}

	/**
	 * 打开未打开的版面board
	 * @param board
	 * 要打开的版面名称
	 */
	public void openBoard(String board) {
		for (int i = 0; i < mOpenBoardList.size(); i++) {
			if (board.equals(mOpenBoardList.get(i).getCurrentBoard()))
				return;
		}
		BoardFragment boardfrag = new BoardFragment(board);
		mOpenBoardList.add(boardfrag);
		notifyDataSetChanged();
	}
	/**
	 * 返回board在view pager里的位置
	 * @param board
	 * 要查询在view pager中的位置版面名称
	 * @return
	 * 版面在view pager中的位置
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

}
