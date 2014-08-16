package com.sysu.bbs.argo.util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
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

	public static ArrayList<LoginListener> loginListeners = new ArrayList<LoginListener>();
	public static ArrayList<LogoutListener> logoutListeners = new ArrayList<LogoutListener>();

	public static boolean isLoggedIn = false;

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

		CookieManager cm = new CookieManager();
		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cm);

		requestQueue.add(new StringRequestPost(API.POST.AJAX_LOGIN, this, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Toast.makeText(mContext, "ÍøÂç´íÎó£¬µÇÂ¼Ê§°Ü", Toast.LENGTH_SHORT).show();
				for (LoginListener listener : loginListeners)
					if (listener != null)
						listener.failed();				
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

								for (LogoutListener listener : logoutListeners)
									listener.logout(true);

							} else {
								Toast.makeText(mContext,
										"logout failed, " + logoutResult.getString("error"),
										Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							Toast.makeText(mContext, "unexpected error in logout",
									Toast.LENGTH_LONG).show();
						}
					}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Toast.makeText(mContext, "ÍøÂç´íÎó£¬ÎÞ·¨×¢Ïú",
						Toast.LENGTH_LONG).show();
				for (LogoutListener listener : logoutListeners)
					listener.logout(false);				
			}
		}));
	}

	public interface LoginListener {
		public void succeeded(String userid);
		public void failed();
	}
	public interface LogoutListener {
		public void logout(boolean success);
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

				for (LoginListener listener : loginListeners) {
					if (listener != null )
						listener.succeeded(mUsername);
				}

			} else {
				Toast.makeText(mContext,
						"login failed, " + loginResult.getString("error"),
						Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			Toast.makeText(mContext, "unexpected error in login",
					Toast.LENGTH_LONG).show();
		}
	}
	
	public static String getUsername() {
		return mUsername;
	}

}
