package com.sysu.bbs.argo.util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sysu.bbs.argo.api.API;

public class SessionManager implements Listener<String> {

	private Context mContext;
	private RequestQueue requestQueue;
	private static String mUsername;
	private static String mPassword;

	public static boolean isLoggedIn = false;
	
	public static String BROADCAST_LOGIN = "com.sysu.bbs.argo.util.login";
	public static String BROADCAST_LOGOUT = "com.sysu.bbs.argo.util.logout";
	

	public SessionManager(Context con, String username, String password) {
		mContext = con;
		mUsername = username;
		mPassword = password;

		requestQueue = Volley.newRequestQueue(mContext);

	}

	public void login() {

		HashMap<String, String> param = new HashMap<String, String>();
		param.put("userid", mUsername);
		param.put("passwd", mPassword);

		//CookieManager cm = new CookieManager(new PersistentCookieStore(mContext),CookiePolicy.ACCEPT_ALL);
		//cm.setCookiePolicy();
		//CookieHandler.setDefault(cm);

		requestQueue.add(new StringRequestPost(API.POST.AJAX_LOGIN, this, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Toast.makeText(mContext, "ÍøÂç´íÎó£¬µÇÂ¼Ê§°Ü", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(BROADCAST_LOGIN);
				intent.putExtra("userid", "");
				mContext.sendBroadcast(intent);
				
			}
			
		},param));
	}
	
	public void logout() {
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
								mContext.sendBroadcast(intent);

							} else {
								Toast.makeText(mContext,
										"logout failed, " + logoutResult.getString("error"),
										Toast.LENGTH_SHORT).show();
								Intent intent = new Intent(BROADCAST_LOGOUT);
								intent.putExtra("success", false);
								mContext.sendBroadcast(intent);
							}
						} catch (JSONException e) {
							Toast.makeText(mContext, "unexpected error in logout",
									Toast.LENGTH_LONG).show();
							Intent intent = new Intent(BROADCAST_LOGOUT);
							intent.putExtra("success", false);
							mContext.sendBroadcast(intent);
						}
					}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Toast.makeText(mContext, "ÍøÂç´íÎó£¬ÎÞ·¨×¢Ïú",
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent(BROADCAST_LOGIN);
				intent.putExtra("success", false);
				mContext.sendBroadcast(intent);
			}
		}));
	}

	@Override
	public void onResponse(String response) {
		loginResponse(response);

	}

	private void loginResponse(String response) {
		try {
			JSONObject loginResult = new JSONObject(response);
			if (loginResult != null
					&& loginResult.getString("success").equals("1")) {
				SessionManager.isLoggedIn = true;
				Intent intent = new Intent(BROADCAST_LOGIN);
				intent.putExtra("userid", mUsername);
				mContext.sendBroadcast(intent);

			} else {
				Toast.makeText(mContext,
						"login failed, " + loginResult.getString("error"),
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(BROADCAST_LOGIN);
				intent.putExtra("userid", "");
				mContext.sendBroadcast(intent);

			}
		} catch (JSONException e) {
			Toast.makeText(mContext, "unexpected error in login",
					Toast.LENGTH_LONG).show();
			Intent intent = new Intent(BROADCAST_LOGIN);
			intent.putExtra("userid", "");
			mContext.sendBroadcast(intent);
		}
	}
	
	public static String getUsername() {
		return mUsername;
	}

}
