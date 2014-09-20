package com.sysu.bbs.argo.adapter;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.sysu.bbs.argo.view.BoardFragment;

public class BoardFragmentPagerAdapter extends AbstractFragmentPagerAdapter {
	
	/**
	 * 当前打开的版面列表
	 */
	private List<BoardFragment> mOpenBoardList;
	public BoardFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		mOpenBoardList = new ArrayList<BoardFragment>();

	}

	@Override
	public Fragment getItem(int pos) {		
		return mOpenBoardList.get(pos);
	}

	@Override
	public int getCount() {
		return mOpenBoardList.size();
	}

	public void openBoard(String newboard) {
		BoardFragment boardfrag = new BoardFragment(newboard);
		mOpenBoardList.add(boardfrag);
		notifyDataSetChanged();
		//boardfrag.changeBoard(newboard);
		
	}
	
	@Override
	public String[] getTabTitle() {
		List<String> tabTitles = new ArrayList<String>();
		String[] tmp = new String[mOpenBoardList.size()];
		
		for (BoardFragment boardFrag: mOpenBoardList) {
			tabTitles.add(boardFrag.getCurrentBoard());
		}
		
		return tabTitles.toArray(tmp);
		
	}

}
