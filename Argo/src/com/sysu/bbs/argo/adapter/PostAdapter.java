package com.sysu.bbs.argo.adapter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sysu.bbs.argo.AddPostActivity;
import com.sysu.bbs.argo.MainActivity;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.TopicListActivity;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Post;

public class PostAdapter extends ArrayAdapter<Post> implements OnClickListener {

	Context context;
	ArrayList<Post> postList;
	// HashMap<PostHead, Post> post = new HashMap<PostHead, Post>();
	RequestQueue requestQueue;
	static View pbEmpty = null;

	public PostAdapter(Context con, int resource, List<Post> objects) {
		super(con, resource, objects);
		context = con;
		postList = (ArrayList<Post>) objects;
		requestQueue = Volley.newRequestQueue(context);

	}

	private class PostViewHolder {
		TextView tvUserid;
		TextView tvTitle;
		TextView tvContent;
		TextView tvQuote;
		ImageButton btnReply;

		PostViewHolder(TextView userid, TextView title, TextView content,
				TextView quote, ImageButton reply) {
			tvUserid = userid;
			tvTitle = title;
			tvContent = content;
			tvQuote = quote;
			btnReply = reply;
		}

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Post post = postList.get(position);

		View tmp = convertView;
		PostViewHolder holder = null;
		if (tmp == null) {
			tmp = LayoutInflater.from(context).inflate(R.layout.item_post,
					parent, false);
			TextView tvUserid = (TextView) tmp.findViewById(R.id.post_userid);
			TextView tvTitle = (TextView) tmp.findViewById(R.id.post_title);
			TextView tvContent = (TextView) tmp.findViewById(R.id.post_content);
			TextView tvQuote = (TextView) tmp.findViewById(R.id.post_quote);
			ImageButton btnReply = (ImageButton) tmp
					.findViewById(R.id.add_comment);

			tvQuote.setOnClickListener(this);
			tvContent.setOnClickListener(this);
			btnReply.setOnClickListener(this);

			holder = new PostViewHolder(tvUserid, tvTitle, tvContent, tvQuote,
					btnReply);

			tmp.setTag(holder);
		} else {
			holder = (PostViewHolder) tmp.getTag();
		}
		String content = post.getParsedContent();
		holder.tvContent.setText(content);
		holder.tvContent.setTag(post);
		
		holder.tvTitle.setText(post.getTitle());
		holder.tvUserid.setText(post.getUserid());
		holder.btnReply.setTag(Integer.valueOf(position));

		if (post.getParsedQuote() != null && !post.getParsedQuote().equals("")) {
			holder.tvQuote.setVisibility(View.VISIBLE);
			holder.tvQuote.setText(post.getParsedQuote());
		} else
			holder.tvQuote.setVisibility(View.GONE);

		return tmp;

	}

	public void getPostResponse(String response) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_comment:
			Integer tag = (Integer) v.getTag();
			int pos = tag.intValue();
			Post post = postList.get(pos);
			reply(post);
			break;
		case R.id.post_quote:
			TextView textview = (TextView) v;
			if (textview.getEllipsize() == TruncateAt.END) {
				textview.setEllipsize(null);
				textview.setMaxLines(Integer.MAX_VALUE);
			} else {
				textview.setEllipsize(TruncateAt.END);
				textview.setMaxLines(2);
			}
			break;
		case R.id.post_content:
			final Post holder = (Post) v.getTag();
			if ( ! (context instanceof MainActivity))
				return;
			MainActivity mainActivity = (MainActivity) context;
			RequestQueue requestQueue = mainActivity.getRequestQueue();
			String url = API.GET.AJAX_POST_TOPICLIST + "?boardname="
					+ holder.getBoard() + "&filename=" + holder.getFilename();
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
									intent.putExtra("boardname", holder.getBoard());
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

					}, null));
			break;
		}
	}

	private void reply(Post post) {
		Intent intent = new Intent(context, AddPostActivity.class);
		Bundle param = new Bundle();
		param.putString("type", "reply");
		param.putString("boardname", post.getBoard());
		param.putString("articleid", post.getFilename());
		param.putString("title", post.getTitle());
		param.putString("content", post.getParsedContent());
		param.putString("userid", post.getUserid());
		param.putString("username", post.getUsername());

		intent.putExtras(param);

		context.startActivity(intent);
	}
}

/**/