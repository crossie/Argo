package com.sysu.bbs.argo.util;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sysu.bbs.argo.api.API;

public class SessionManager {

	public static Context context;
	private static RequestQueue requestQueue;
	private static String mUsername;
	private static String mPassword;

	public static boolean isLoggedIn = false;
	
	public static String BROADCAST_LOGIN = "com.sysu.bbs.argo.util.login";
	public static String BROADCAST_LOGOUT = "com.sysu.bbs.argo.util.logout";
	
	public static RequestQueue getRequestQueue() {
		if (context == null) {
			throw new IllegalStateException("Context not initialized");
		}
		if (requestQueue == null) {
			requestQueue = Volley.newRequestQueue(context);
		}
		
		return requestQueue;
		
	}
	public static void  login(String username, String password) {

		mUsername = username;
		mPassword = password;
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("userid", mUsername);
		param.put("passwd", mPassword);

		//CookieManager cm = new CookieManager(new PersistentCookieStore(context),CookiePolicy.ACCEPT_ALL);
		//cm.setCookiePolicy();
		//CookieHandler.setDefault(cm);

		StringRequestPost loginRequest = 
				new StringRequestPost(API.POST.AJAX_LOGIN, new Listener<String>() {

					@Override
					public void onResponse(String response) {
						try {
							JSONObject loginResult = new JSONObject(response);
							if (loginResult != null
									&& loginResult.getString("success").equals("1")) {
								SessionManager.isLoggedIn = true;
								Intent intent = new Intent(BROADCAST_LOGIN);
								intent.putExtra("userid", mUsername);
								context.sendBroadcast(intent);

							} else {
								Toast.makeText(context,
										"login failed, " + loginResult.getString("error"),
										Toast.LENGTH_SHORT).show();
								Intent intent = new Intent(BROADCAST_LOGIN);
								intent.putExtra("userid", "");
								context.sendBroadcast(intent);

							}
						} catch (JSONException e) {
							Toast.makeText(context, "unexpected error in login",
									Toast.LENGTH_LONG).show();
							Intent intent = new Intent(BROADCAST_LOGIN);
							intent.putExtra("userid", "");
							context.sendBroadcast(intent);
						}
						
					}
					
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(context, "ÍøÂç´íÎó£¬µÇÂ¼Ê§°Ü", Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(BROADCAST_LOGIN);
						intent.putExtra("userid", "");
						context.sendBroadcast(intent);
						
					}
					
				},param);
		loginRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 2));
		loginRequest.setTag(API.POST.AJAX_LOGIN);
		SessionManager.getRequestQueue().add(loginRequest);
	}
	
	public static void logout() {
		requestQueue.add(new StringRequest(API.POST.AJAX_LOGOUT, 
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						try {
							JSONObject logoutResult = new JSONObject(response);

							if (logoutResult != null
									&& logoutResult.getString("success").equals("1")) {
								SessionManager.isLoggedIn = false;
								Intent intent = new Intent(BROADCAST_LOGOUT);
								intent.putExtra("success", true);
								context.sendBroadcast(intent);

							} else {
								Toast.makeText(context,
										"logout failed, " + logoutResult.getString("error"),
										Toast.LENGTH_SHORT).show();
								Intent intent = new Intent(BROADCAST_LOGOUT);
								intent.putExtra("success", false);
								context.sendBroadcast(intent);
							}
						} catch (JSONException e) {
							Toast.makeText(context, "unexpected error in logout",
									Toast.LENGTH_LONG).show();
							Intent intent = new Intent(BROADCAST_LOGOUT);
							intent.putExtra("success", false);
							context.sendBroadcast(intent);
						}
					}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Toast.makeText(context, "ÍøÂç´íÎó£¬ÎÞ·¨×¢Ïú",
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent(BROADCAST_LOGOUT);
				intent.putExtra("success", false);
				context.sendBroadcast(intent);
			}
		}));
	}
	
	public static String getUsername() {
		return mUsername;
	}

}
