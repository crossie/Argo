package com.sysu.bbs.argo.view;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sysu.bbs.argo.R;

public class LoginPost extends ListFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setListAdapter(ArrayAdapter.createFromResource(getActivity(),
				R.array.right_menu_items, android.R.layout.simple_list_item_1));

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
		case 0:
			break;
		case 1:
			AboutDialog about = new AboutDialog();
			about.show(getActivity().getSupportFragmentManager(), null);
			break;
		case 2:
			break;
		}
	}

}
