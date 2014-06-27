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
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.Volley;
import com.sysu.bbs.argo.api.API;

public class SessionManager implements Listener<String> {

	Context mContext;
	RequestQueue requestQueue;
	String mUsername;
	String mPassword;
	boolean isSaveUser;
	boolean isSavePassword;

	public static ArrayList<LoginSuccessListener> loginSuccessListeners = new ArrayList<LoginSuccessListener>();

	public static boolean isLoggedIn = false;

	public SessionManager(Context con, String username, String password,
			boolean saveUser, boolean savePassword) {
		mContext = con;
		mUsername = username;
		mPassword = password;
		isSaveUser = saveUser;
		isSavePassword = savePassword;

		requestQueue = Volley.newRequestQueue(mContext);

	}

	public void login() {

		HashMap<String, String> param = new HashMap<String, String>();
		param.put("userid", mUsername);
		param.put("passwd", mPassword);

		CookieManager cm = new CookieManager();
		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cm);

		requestQueue.add(new StringRequestPost(API.POST.AJAX_LOGIN, this, new SimpleErrorListener(mContext,""),
				param));
	}

	public interface LoginSuccessListener {
		public void actionAfterLogin(String userid);
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

				for (LoginSuccessListener listener : loginSuccessListeners)
					listener.actionAfterLogin(mUsername);

				if (isSaveUser) {
					// TODO: save user name to database
					if (isSavePassword)
						;// TODO: save mPassword to database
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

}
