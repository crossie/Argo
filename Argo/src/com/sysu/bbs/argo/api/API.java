package com.sysu.bbs.argo.api;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.android.volley.Request.Method;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public final class API {
	private final static String entry = "http://bbs.sysu.edu.cn";
	Context context;
	HashMap<String, String> param;
	public API(Context c,HashMap<String, String> p) {
		context = c;
		param = p;
	}
	public static final class GET {
		public static final String AJAX_SECTION = entry + "/ajax/section/";
		
		public static final String AJAX_USER_FAV = entry + "/ajax/user/fav/";
		public static final String AJAX_USER_QUERY = entry + "/ajax/user/query/";
		
		public static final String AJAX_BOARD_ALL = entry + "/ajax/board/all/";
		public static final String AJAX_BOARD_ALLS = entry + "/ajax/board/alls/";
		public static final String AJAX_BOARD_GET = entry + "/ajax/board/get/";
		public static final String AJAX_BOARD_GETBYSEC = entry + "/ajax/board/getbysec/";
		
		public static final String AJAX_POST_LIST = entry + "/ajax/post/list/";
		public static final String AJAX_POST_GET = entry + "/ajax/post/get/";
		public static final String AJAX_POST_TOPICLIST = entry + "/ajax/post/topiclist/";
		
		public static final String AJAX_MAIL_MAILBOX = entry + "/ajax/mail/mailbox/";
		public static final String AJAX_MAIL_LIST = entry + "/ajax/mail/list/";
		public static final String AJAX_MAIL_GET = entry + "/ajax/mail/get/";
		
		public static final String AJAX_ANN = entry + "/ajax/ann/";
		public static final String AJAX_ANC = entry + "/ajax/anc/";
		
		public static final String AJAX_COMM_TOPTEN = entry + "/ajax/comm/topten/";
				
	}
	public static final class POST {
		public static final String AJAX_LOGIN = entry + "/ajax/login/";
		public static final String AJAX_LOGOUT = entry + "/ajax/logout/";
		public static final String AJAX_USER_ADDFAV = entry + "/ajax/user/addfav/";
		public static final String AJAX_USER_DELFAV = entry + "/ajax/user/delfav";
		public static final String AJAX_POST_ADD = entry + "/ajax/post/add/";
		public static final String AJAX_POST_DEL = entry + "/ajax/post/del/";
		public static final String AJAX_MAIL_SEND = entry + "/ajax/mail/send/";
	}
	
	JSONObject result;
	
	public JSONObject losgin() {
		CookieManager cm = new CookieManager();
		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cm);
		//TODO: mind the network status here		
		//TODO: should share RequestQueue within this class ?
		RequestQueue requestQueue = Volley.newRequestQueue(context);
		RequestFuture<String> future = RequestFuture.newFuture();
		requestQueue.add(new StringRequest(Method.POST, POST.AJAX_LOGIN, future, future){
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				return param;
			}
		});
		
		try {
			result = new JSONObject(future.get());
		} catch (JSONException e) {
			result = null;
		} catch (InterruptedException e) {
			result = null;
		} catch (ExecutionException e) {
			result = null;
		}
		
		
		return result;
	}
}
