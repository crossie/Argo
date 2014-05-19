package com.sysu.bbs.argo.util;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public class StringRequestPost extends StringRequest {

	HashMap<String, String> param;
	
	public StringRequestPost(String url, Listener<String> listener,
			ErrorListener errorListener,HashMap<String, String> param) {		
		super(Method.POST, url, listener, errorListener);
		this.param = param;
	}
	public StringRequestPost(String url, Listener<String> listener){
		this(url, listener, null, null);
	}
	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return param;
	}

}
