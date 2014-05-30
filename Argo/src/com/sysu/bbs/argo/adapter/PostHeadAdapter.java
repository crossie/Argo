package com.sysu.bbs.argo.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.sysu.bbs.argo.MainActivity;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.TopicListActivity;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.PostHead;

public class PostHeadAdapter extends ArrayAdapter<PostHead> implements
		OnClickListener {
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

			tmp.setOnClickListener(this);

		} else {
			holder = (TopicHolder) tmp.getTag();
		}

		PostHead postHead = postHeadList.get(position);

		holder.tvTitle.setText(postHead.getTitle());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyƒÍMM‘¬DD»’  HH:mm:ss");
		Calendar update = Calendar.getInstance();
		update.setTimeInMillis(Long.valueOf(postHead.getUpdate()));

		Date date = update.getTime();
		holder.tvUserTime.setText(postHead.getOwner() + "   "
				+ sdf.format(date));
		
		holder.boardname = postHead.getBoardname();
		holder.filename = postHead.getFilename();
		
		tmp.setTag(holder);

		return tmp;
	}

	@Override
	public void onClick(View view) {
		final TopicHolder holder = (TopicHolder) view.getTag();
		MainActivity mainActivity = (MainActivity) con;
		RequestQueue requestQueue = mainActivity.getRequestQueue();
		String url = API.GET.AJAX_POST_TOPICLIST + "?boardname="
				+ holder.boardname + "&filename=" + holder.filename;
		requestQueue.add(new StringRequest(Method.GET, url,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						try {
							JSONObject res = new JSONObject(response);
							if (res.getString("success").equals("1")) {
								JSONArray resArray = res.getJSONArray("data");
								String[] filenames = new String[resArray
										.length()];
								for (int i = 0; i < resArray.length(); i++)
									filenames[i] = resArray.getString(i);

								Intent intent = new Intent(con,
										TopicListActivity.class);
								intent.putExtra("boardname", holder.boardname);
								intent.putExtra("filenames", filenames);

								con.startActivity(intent);

							} else {
								Toast.makeText(
										con,
										"failed to get post, "
												+ res.getString("error"),
										Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							Toast.makeText(con,
									"unexpected error in getting post",
									Toast.LENGTH_SHORT).show();
						}

					}

				}, null));

	}

}
