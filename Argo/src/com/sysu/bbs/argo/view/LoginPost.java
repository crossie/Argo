package com.sysu.bbs.argo.view;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sysu.bbs.argo.R;

public class LoginPost extends ListFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		View view = inflater.inflate(R.layout.login_post, container, false);

		LoginPostAdapter adapter = new LoginPostAdapter(getActivity(),
				android.R.layout.simple_list_item_1,
				Arrays.asList(getResources().getStringArray(
						R.array.right_draw_items)));

		setListAdapter(adapter);

		return view;
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

	private class LoginPostAdapter extends ArrayAdapter<String> {

		Context con;
		List<String> list;

		public LoginPostAdapter(Context context, int resource,
				List<String> objects) {
			super(context, resource, objects);
			con = context;
			list = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tmp = (TextView) convertView;

			if (tmp == null) {
				tmp = (TextView) LayoutInflater.from(con).inflate(
						R.layout.item_about_dialog, parent, false);
			}

			tmp.setText(list.get(position));

			return tmp;
		}

	}

}
