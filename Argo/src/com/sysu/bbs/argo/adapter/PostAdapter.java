package com.sysu.bbs.argo.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sysu.bbs.argo.AddPostActivity;
import com.sysu.bbs.argo.ImageViewerActivity;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Post;
import com.sysu.bbs.argo.api.dao.PostHead;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.SimpleErrorListener;

public class PostAdapter extends ArrayAdapter<PostHead> implements OnClickListener {

	private HashMap<String, Post> mPostMap;
	private String mBoardName;

	public PostAdapter(Context con, int resource, List<PostHead> objects, 
			HashMap<String, Post> postMap, String boardname) {
		super(con, resource, objects);
		//mPostMap = new HashMap<String, Post>();
		mPostMap = postMap;
		mBoardName = boardname;
		//mRequestQueue = Volley.newRequestQueue(getContext());
	}

	private class PostViewHolder {
		TextView tvUserid;
		TextView tvTitle;
		TextView tvPosttime;
		TextView tvContent;
		TextView tvQuote;
		ImageButton btnReply;
		ImageButton btnPicture;
		ImageButton btnAttachment;
		Request<String> request;
		int position;

		PostViewHolder(TextView userid, TextView title, TextView posttime, TextView content,
				TextView quote, ImageButton reply, ImageButton picture,
				ImageButton attachment) {
			tvUserid = userid;
			tvTitle = title;
			tvPosttime = posttime;
			tvContent = content;
			tvQuote = quote;
			btnReply = reply;
			btnPicture = picture;
			btnAttachment = attachment;
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
			ImageButton btnPicture = (ImageButton) tmp.findViewById(R.id.view_image);
			ImageButton btnAttachment = (ImageButton) tmp.findViewById(R.id.view_attachment);

			tvQuote.setOnClickListener(this);
			btnReply.setOnClickListener(this);
			btnPicture.setOnClickListener(this);
			btnAttachment.setOnClickListener(this);
			//加载过程中不显示这些view
			tvQuote.setVisibility(View.GONE);
			btnReply.setVisibility(View.GONE);
			btnPicture.setVisibility(View.GONE);
			btnAttachment.setVisibility(View.GONE);
			
			holder = new PostViewHolder(tvUserid, tvTitle, tvPosttime, tvContent, tvQuote,
					btnReply, btnPicture, btnAttachment);

			tmp.setTag(holder);
		} else {
			holder = (PostViewHolder) tmp.getTag();
			//reset holder status to loading...
			setupHolder(holder, null);
		}
		
		holder.position = position;
		
		if (holder.request != null) 
			holder.request.cancel();
		
		PostHead postHead = getItem(position);
		if (postHead.getUnread().equals("0"))
			tmp.setBackgroundResource(R.drawable.post_background);
		else
			tmp.setBackgroundResource(R.drawable.post_background_unread);
		
		String filename = postHead.getFilename();
		if (mPostMap.containsKey(filename)) {
			Post post = mPostMap.get(filename);
			if (post != null && post.getBoard().equals(mBoardName)) {				
				setupHolder(holder, post);
				tmp.setBackgroundResource(R.drawable.post_background);
				return tmp;
			} 
		} 

		getPost(holder);		
		return tmp;

	}
	
	private void getPost(final PostViewHolder holder) {

		String url = API.GET.AJAX_POST_GET + "?boardname="
				+ mBoardName + "&filename="
				+ getItem(holder.position).getFilename();
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
		holder.request.setRetryPolicy(new DefaultRetryPolicy(3000, 2, 2));
		holder.request.setTag(API.GET.AJAX_POST_GET);
		SessionManager.getRequestQueue().add(holder.request);
	}
	/**
	 * 设置或者加载各个view的值
	 * @param holder
	 * 要初始化的view holder
	 * @param post
	 * 用于初如化view holder的帖子
	 */
	private void setupHolder(PostViewHolder holder, Post post) {
		if (post != null) {
			String content = post.getParsedContent();
			holder.tvContent.setText(content);
			holder.tvContent.setTag(post);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MMMdd日 HH:mm   ", Locale.CHINESE);
			Calendar update = Calendar.getInstance();
			update.setTimeInMillis(1000*Long.valueOf(post.getPost_time()));
			Date date = update.getTime();
			
			holder.tvTitle.setText(post.getTitle());
			holder.tvPosttime.setText(sdf.format(date));
			holder.tvUserid.setText(post.getUserid() +
					"(" + (post.getUsername().equals("这家伙还没起昵称") ? "" : post.getUsername()) + ")");
			holder.btnReply.setTag(post);
			holder.btnReply.setImageResource(R.drawable.ic_action_chat);
			holder.btnReply.setVisibility(View.VISIBLE);
			
			ArrayList<String> imageUrl = new ArrayList<String>();
			Pattern pattern = Pattern.compile("((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)",
									Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(content);
			while (matcher.find()) {
				String url = content.substring(matcher.start(0), matcher.end(0));
				String lower = url.toLowerCase();
				//判断是不是图片链接
				//TODO 发送HEAD request去判断链接是不是图片
				if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
						lower.endsWith(".gif") || lower.endsWith(".bmp") ||
						lower.endsWith(".png"))					
					imageUrl.add(url);
			}
			if (imageUrl.size() > 0) {
				holder.btnPicture.setVisibility(View.VISIBLE);
				holder.btnPicture.setTag(imageUrl);
			}
	
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
			//post 为null，说明帖子未加载，所以不显示这个view
			holder.btnReply.setVisibility(View.GONE);
			holder.tvQuote.setVisibility(View.GONE);
			holder.btnAttachment.setVisibility(View.GONE);
			holder.btnPicture.setVisibility(View.GONE);
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
		case R.id.view_attachment:
			break;
		case R.id.view_image:
			Intent intent = new Intent(getContext(), ImageViewerActivity.class);
			Bundle param = new Bundle();
			param.putStringArrayList(ImageViewerActivity.IMAGE_LIST_KEY, (ArrayList<String>) v.getTag());
			intent.putExtras(param);
			getContext().startActivity(intent);
			((Activity)getContext()).overridePendingTransition(R.anim.open_enter_slide_in, R.anim.open_exit_slide_out);
			break;
		default:
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
		param.putString("content", post.getRawcontent());
		param.putString("userid", post.getUserid());
		param.putString("username", post.getUsername());
		param.putInt("_where_", 1);

		intent.putExtras(param);

		getContext().startActivity(intent);
		((Activity)getContext()).overridePendingTransition(R.anim.open_enter_slide_in, R.anim.open_exit_slide_out);
	}
/*
	public String getBoardName() {
		return mBoardName;
	}

	public void setBoardName(String boardName) {
		this.mBoardName = boardName;
	}
*/	
	public Post getPost(String filename) {
		return mPostMap.get(filename);
	}
}

/**/