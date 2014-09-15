package com.sysu.bbs.argo.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.api.dao.PostHead;

public class PostHeadAdapter extends ArrayAdapter<PostHead>  {
	ArrayList<PostHead> postHeadList;
	Context con;

	public PostHeadAdapter(Context context, int resource, List<PostHead> objects) {
		super(context, resource, objects);
		postHeadList = (ArrayList<PostHead>) objects;
		con = context;
	}

	private class TopicHolder {
		TextView tvTitle;
		TextView tvUserTime;
		String boardname;
		String filename;

		TopicHolder(TextView title, TextView userTime) {
			tvTitle = title;
			tvUserTime = userTime;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View tmp = convertView;
		TopicHolder holder;
		if (tmp == null) {
			tmp = LayoutInflater.from(con).inflate(R.layout.item_topic, parent,
					false);
			TextView title = (TextView) tmp.findViewById(R.id.topic_title);
			TextView userTime = (TextView) tmp
					.findViewById(R.id.topic_user_time);
			holder = new TopicHolder(title, userTime);

			//tmp.setOnClickListener(this);

		} else {
			holder = (TopicHolder) tmp.getTag();
		}

		PostHead postHead = postHeadList.get(position);
		
		if (postHead.getUnread().equals(""))
			tmp.setBackgroundResource(R.drawable.post_background);
		else
			tmp.setBackgroundResource(R.drawable.post_background_unread);

		holder.tvTitle.setText(postHead.getTitle());

		SimpleDateFormat sdf = new SimpleDateFormat("   ddMMM HH:mm:ss", Locale.US);
		Calendar update = Calendar.getInstance();
		update.setTimeInMillis(1000*Long.valueOf(postHead.getUpdate()));
		Date date = update.getTime();
		holder.tvUserTime.setText(postHead.getOwner() + sdf.format(date));
		
		holder.boardname = postHead.getBoardname();
		holder.filename = postHead.getFilename();
		
		tmp.setTag(holder);

		return tmp;
	}

}
