package com.sysu.bbs.argo.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.api.dao.Board;

public class FavGridAdapter extends ArrayAdapter<Board> {
	private class FavHolder {
		TextView nameEn;
		TextView nameCn;

		FavHolder(TextView en, TextView cn) {
			nameEn = en;
			nameCn = cn;
		}
	}

	public FavGridAdapter(Context context, int resource, List<Board> board) {

		super(context, resource, board);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View tmp = convertView;
		FavHolder holder = null;
		if (tmp == null) {
			tmp = LayoutInflater.from(this.getContext()).inflate(
					R.layout.item_favorite, parent, false);
			TextView nameEn = (TextView) tmp.findViewById(R.id.name_en);
			TextView nameCn = (TextView) tmp.findViewById(R.id.name_cn);
			holder = new FavHolder(nameEn, nameCn);
			tmp.setTag(holder);

		} else
			holder = (FavHolder) tmp.getTag();

		holder.nameEn.setText(getItem(position).getBoardname());
		holder.nameCn.setText(getItem(position).getTitle());

		return tmp;
	}

}
