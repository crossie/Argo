package com.sysu.bbs.argo.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Top10;

public class Top10Fragment extends AbstractHomeFragment<Top10> {
	/**
	 * colors used to indicate the hotness of top10
	 */
	private int[] mRankingBackground = new int[] {R.color.ranking_1,R.color.ranking_2,R.color.ranking_3,
			R.color.ranking_4,R.color.ranking_5,R.color.ranking_6,R.color.ranking_7,
			R.color.ranking_8,R.color.ranking_9,R.color.ranking_10,};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mUrl = API.GET.AJAX_COMM_TOPTEN;
		super.onActivityCreated(savedInstanceState);
	}
	private class Top10Adapter extends ArrayAdapter<Top10> {

		public Top10Adapter(Context context, int resource, List<Top10> objects) {
			super(context, resource, objects);
		}

		private class Top10Holder {
			TextView tvNum;
			TextView tvTitle;
			TextView tvAuthor;

			Top10Holder(TextView num, TextView title, TextView author) {
				tvNum = num;
				tvTitle = title;
				tvAuthor = author;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View tmp = convertView;
			Top10Holder holder = null;
			if (tmp == null) {
				tmp = LayoutInflater.from(getActivity()).inflate(
						R.layout.item_top10, parent, false);
				TextView tvNum = (TextView) tmp.findViewById(R.id.text_num);
				TextView tvTitle = (TextView) tmp.findViewById(R.id.text_title);
				TextView tvAuthor = (TextView) tmp
						.findViewById(R.id.text_user_board_time);

				holder = new Top10Holder(tvNum, tvTitle, tvAuthor);
				tmp.setTag(holder);

			} else
				holder = (Top10Holder) tmp.getTag();

			Top10 top10 = getItem(position);
			holder.tvNum.setText(String.valueOf(top10.getNum()));
			holder.tvNum.setBackgroundResource(mRankingBackground[position]);
			
			SimpleDateFormat sdf = new SimpleDateFormat("    ddMMM HH:mm", Locale.US);
			Calendar update = Calendar.getInstance();
			update.setTimeInMillis(1000*Long.valueOf(top10.getTime()));
			Date date = update.getTime();
			
			holder.tvAuthor.setText(top10.getAuthor() + "@"
					+ top10.getBoard() + sdf.format(date));
			holder.tvTitle.setText(top10.getTitle());

			return tmp;
		}

	}
	@Override
	public String getBoard(Top10 item) {
		
		return item.getBoard();
	}
	@Override
	public String getFilename(Top10 item) {
		
		return item.getFilename();
	}
	@Override
	public Top10 newItem(JSONObject json) {
		try {
			return new Top10(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	@Override
	public ArrayAdapter<Top10> newAdapter() {
		
		return new Top10Adapter(getActivity(), R.layout.item_top10,
				mDataList);
	}
}
