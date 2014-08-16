package com.sysu.bbs.argo.adapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sysu.bbs.argo.AddPostActivity;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Post;
import com.sysu.bbs.argo.util.SimpleErrorListener;

public class PostAdapter extends ArrayAdapter<String> implements OnClickListener {

	private static HashMap<String, Post> mPostMap;
	//how to initialize board name ?
	private String mBoardName;
	private RequestQueue mRequestQueue;


	public PostAdapter(Context con, int resource, List<String> objects, String boardname) {
		super(con, resource, objects);
		mPostMap = new HashMap<String, Post>();
		mBoardName = boardname;
		mRequestQueue = Volley.newRequestQueue(getContext());
	}

	private class PostViewHolder {
		TextView tvUserid;
		TextView tvTitle;
		TextView tvPosttime;
		TextView tvContent;
		TextView tvQuote;
		ImageButton btnReply;
		Request<String> request;
		int position;

		PostViewHolder(TextView userid, TextView title, TextView posttime, TextView content,
				TextView quote, ImageButton reply) {
			tvUserid = userid;
			tvTitle = title;
			tvPosttime = posttime;
			tvContent = content;
			tvQuote = quote;
			btnReply = reply;
		}

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View tmp = convertView;
		PostViewHolder holder = null;
		if (tmp == null) {
			tmp = LayoutInflater.from(getContext()).inflate(R.layout.item_post,
					parent, false);
			TextView tvUserid = (TextView) tmp.findViewById(R.id.post_userid);
			TextView tvTitle = (TextView) tmp.findViewById(R.id.post_title);
			TextView tvPosttime = (TextView) tmp.findViewById(R.id.post_time);
			TextView tvContent = (TextView) tmp.findViewById(R.id.post_content);
			TextView tvQuote = (TextView) tmp.findViewById(R.id.post_quote);
			ImageButton btnReply = (ImageButton) tmp
					.findViewById(R.id.add_comment);

			tvQuote.setOnClickListener(this);
			tvQuote.setVisibility(View.GONE);
			//tvContent.setOnClickListener(this);
			btnReply.setOnClickListener(this);
			btnReply.setVisibility(View.GONE);
			
		/*	tvUserid.setOnClickListener(this);
			tvTitle.setOnClickListener(this);
			tvPosttime.setOnClickListener(this);*/

			holder = new PostViewHolder(tvUserid, tvTitle, tvPosttime, tvContent, tvQuote,
					btnReply);

			tmp.setTag(holder);
		} else {
			holder = (PostViewHolder) tmp.getTag();
			//reset holder status to loading...
			setupHolder(holder, null);
		}
		
		holder.position = position;
		
		if (holder.request != null) 
			holder.request.cancel();
		
		String filename = getItem(position);
		if (mPostMap.containsKey(filename)) {
			Post post = mPostMap.get(filename);
			if (post != null && post.getBoard().equals(mBoardName)) {				
				setupHolder(holder, post);
				return tmp;
			} 
		} else {
			getPost(holder);
		}
		
		return tmp;

	}
	
	private void getPost(final PostViewHolder holder) {

		String url = API.GET.AJAX_POST_GET + "?boardname="
				+ mBoardName + "&filename="
				+ getItem(holder.position);
		holder.request = new StringRequest(Method.GET, url,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						postResponse(holder, response);
					}
			
				},
				new SimpleErrorListener(getContext(), null) {
					@Override
					public void onErrorResponse(VolleyError error) {
						holder.btnReply.setImageResource(R.drawable.ic_action_refresh);
						holder.btnReply.setVisibility(View.VISIBLE);
						holder.tvContent.setText(R.string.load_failure);
						holder.request = null;
						super.onErrorResponse(error);
					}
				});
		mRequestQueue.add(holder.request);
	}
	
	private void setupHolder(PostViewHolder holder, Post post) {
		if (post != null) {
			String content = post.getParsedContent();
			holder.tvContent.setText(content);
			holder.tvContent.setTag(post);
			
			SimpleDateFormat sdf = new SimpleDateFormat("ddMMM HH:mm   ", Locale.US);
			Calendar update = Calendar.getInstance();
			update.setTimeInMillis(1000*Long.valueOf(post.getPost_time()));
			Date date = update.getTime();
			
			holder.tvTitle.setText(post.getTitle());
			holder.tvPosttime.setText(sdf.format(date));
			holder.tvUserid.setText(post.getUserid() +
					"(" + post.getUsername() + ")");
			holder.btnReply.setTag(post);
			holder.btnReply.setImageResource(R.drawable.ic_action_chat);
			holder.btnReply.setVisibility(View.VISIBLE);
	
			if (post.getParsedQuote() != null && !post.getParsedQuote().equals("")) {
				holder.tvQuote.setVisibility(View.VISIBLE);
				holder.tvQuote.setText(post.getParsedQuote());
			} else
				holder.tvQuote.setVisibility(View.GONE);
			
			holder.request = null;
		} else {
			holder.tvContent.setText(R.string.loading);
			holder.tvContent.setTag(null);		
			holder.tvTitle.setText("");
			holder.tvUserid.setText("");
			holder.btnReply.setTag(null); 
			holder.btnReply.setVisibility(View.GONE);
			holder.tvQuote.setVisibility(View.GONE);
		}
	}

	private void postResponse(PostViewHolder holder, String response) {
		try {
			JSONObject res = new JSONObject(response);
			if (res.getString("success").equals("1")) {
				JSONObject postObject = res
						.getJSONObject("data");
				Post post = new Post(postObject);
				setupHolder(holder, post);
				mPostMap.put(post.getFilename(), post);
			}
		} catch(JSONException e) {
			Toast.makeText(getContext(),
					"unexpected error in getting post",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_comment:
			Post post = (Post) v.getTag();
			if (post != null)
				reply(post);
			else {
				View parent = (View) v.getParent().getParent().getParent();
				PostViewHolder holder = (PostViewHolder) parent.getTag();
				setupHolder(holder, null);
				getPost(holder);
			}
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
		default:
			
			
/*			final Post holder = (Post) v.getTag();
			if ( ! (context instanceof MainActivity))
				return;
			MainActivity mainActivity = (MainActivity) context;
			RequestQueue requestQueue = mainActivity.getRequestQueue();
			*/
			break;
		}
	}

	private void reply(Post post) {
		Intent intent = new Intent(getContext(), AddPostActivity.class);
		Bundle param = new Bundle();
		param.putString("type", "reply");
		param.putString("boardname", post.getBoard());
		param.putString("articleid", post.getFilename());
		param.putString("title", post.getTitle());
		param.putString("content", post.getParsedContent());
		param.putString("userid", post.getUserid());
		param.putString("username", post.getUsername());
		param.putInt("_where_", 1);

		intent.putExtras(param);

		getContext().startActivity(intent);
	}

	public String getBoardName() {
		return mBoardName;
	}

	public void setBoardName(String boardName) {
		this.mBoardName = boardName;
	}
	
	public Post getPost(String filename) {
		return mPostMap.get(filename);
	}
}

/**/