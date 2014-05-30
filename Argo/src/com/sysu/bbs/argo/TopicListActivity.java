package com.sysu.bbs.argo;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sysu.bbs.argo.adapter.PostAdapter;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Post;
import com.sysu.bbs.argo.util.SimpleErrorListener;

public class TopicListActivity extends ListActivity {

	ArrayList<Post> postList;
	PostAdapter postAdapter;
	String boardname;
	String[] filenames = null;

	RequestQueue requestQueue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		boardname = intent.getStringExtra("boardname");
		filenames = intent.getStringArrayExtra("filenames");

		postList = new ArrayList<Post>();
		postAdapter = new PostAdapter(this, R.id.name_cn, postList);

		getListView().setAdapter(postAdapter);

		requestQueue = Volley.newRequestQueue(this);

		loadPost(0, filenames.length);

		super.onCreate(savedInstanceState);
	}

	private void loadPost(final int curr, final int left) {
		if (left == 0)
			return;
		String url = API.GET.AJAX_POST_GET + "?boardname=" + boardname;

		url = url + "&filename=" + filenames[curr];
		requestQueue.add(new StringRequest(Method.GET, url,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						try {
							JSONObject res = new JSONObject(response);
							if (res.getString("success").equals("1")) {
								JSONObject postObject = res
										.getJSONObject("data");
								Post post = new Post(postObject);
								postList.add(post);
								postAdapter.notifyDataSetChanged();
								if (left > 0)
									loadPost(curr + 1, left - 1);
							} else {
								Toast.makeText(
										TopicListActivity.this,
										"failed to get post, "
												+ res.getString("error"),
										Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							Toast.makeText(TopicListActivity.this,
									"unexpected error in getting post",
									Toast.LENGTH_LONG).show();
						}

					}
				}, new SimpleErrorListener(this)));
	}
}
