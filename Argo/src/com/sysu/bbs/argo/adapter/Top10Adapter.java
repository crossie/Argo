package com.sysu.bbs.argo.adapter;

import java.util.ArrayList;
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
import com.sysu.bbs.argo.api.dao.Top10;
import com.sysu.bbs.argo.util.SimpleErrorListener;

public class Top10Adapter extends ArrayAdapter<Top10> implements
		OnClickListener {

	Context context;
	ArrayList<Top10> top10List;

	public Top10Adapter(Context activity, int resource, List<Top10> objects) {
		super(activity, resource, objects);
		context = activity;
		top10List = (ArrayList<Top10>) objects;
	}

	private class Top10Holder {
		TextView tvNum;
		TextView tvTitle;
		TextView tvAuthor;
		String boardname;
		String filename;

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
		Top10 top10 = getItem(position);
		if (tmp == null) {
			tmp = LayoutInflater.from(context).inflate(R.layout.item_top10,
					parent, false);
			TextView tvNum = (TextView) tmp.findViewById(R.id.text_num);
			TextView tvTitle = (TextView) tmp.findViewById(R.id.text_title);
			TextView tvAuthor = (TextView) tmp
					.findViewById(R.id.text_user_board);

			holder = new Top10Holder(tvNum, tvTitle, tvAuthor);
			tmp.setTag(holder);
			tmp.setOnClickListener(this);

		} else
			holder = (Top10Holder) tmp.getTag();

		holder.tvNum.setText(String.valueOf(top10.getNum()));
		holder.tvAuthor.setText(top10.getAuthor() + " ·¢±íÓÚ " + top10.getBoard());
		holder.tvTitle.setText(top10.getTitle());
		holder.boardname = top10.getBoard();
		holder.filename = top10.getFilename();

		return tmp;
	}

	@Override
	public void onClick(View view) {
		final Top10Holder holder = (Top10Holder) view.getTag();
		MainActivity mainActivity = (MainActivity) context;
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

								Intent intent = new Intent(context,
										TopicListActivity.class);
								intent.putExtra("boardname", holder.boardname);
								intent.putExtra("filenames", filenames);

								context.startActivity(intent);

							} else {
								Toast.makeText(
										context,
										"failed to get post, "
												+ res.getString("error"),
										Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							Toast.makeText(context,
									"unexpected error in getting post",
									Toast.LENGTH_SHORT).show();
						}

					}

				}, new SimpleErrorListener(context)));

	}
}
