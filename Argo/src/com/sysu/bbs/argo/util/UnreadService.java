package com.sysu.bbs.argo.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Board;
import com.sysu.bbs.argo.api.dao.Section;

public class UnreadService extends IntentService {

	public static String BROADCAST_UNREAD = "com.sysu.bbs.argo.util.UnreadService.unread";
	private RequestQueue mRequestQueue = null;

	public UnreadService() {
		super("MyIntentService");
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (mRequestQueue == null)
			mRequestQueue = Volley.newRequestQueue(this);
		while (true) {
			synchronized (this) {
				try {
					mRequestQueue.add(new StringRequest(Method.GET,
							API.GET.AJAX_BOARD_ALLS, new Listener<String>() {
								@Override
								public void onResponse(String response) {

									try {
										JSONObject org = new JSONObject(
												response);
										if (org.getString("success")
												.equals("1")) {
											JSONArray arr = org.getJSONObject(
													"data").getJSONArray("all");
											for (int i = 0; i < arr.length(); i++) {
												Section sec = new Section(arr.getJSONObject(i));
												getUnreadStatus(sec.getSeccode());
											}

										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

							}, null));

					Thread.sleep(60 * 1000 * 5);
					//System.out.println("intent service");
				} catch (Exception e) {
				}
			}
		}

	}

	private void getUnreadStatus(String seccode) {
		String url = API.GET.AJAX_BOARD_GETBYSEC + "?sec_code=" + seccode;
		mRequestQueue.add(new StringRequest(Method.GET, url,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						try {
							
							JSONObject org = new JSONObject(
									response);
							if (org.getString("success")
									.equals("1")) {
								JSONArray arr = org.getJSONArray(
										"data");
								Intent intent = new Intent(UnreadService.BROADCAST_UNREAD);
								for (int i = 0; i < arr.length(); i++) {
									Board board = new Board(arr.getJSONObject(i));
									intent.putExtra(board.getBoardname(), board.isUnread());
								}
								
								sendBroadcast(intent);
								
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}

				}, null));
	}

}