package com.sysu.bbs.argo.util;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public class StringRequestPost extends StringRequest {

	HashMap<String, String> param;
	
	public StringRequestPost(int method, String url, Listener<String> listener,
			ErrorListener errorListener,HashMap<String, String> param) {		
		super(method, url, listener, errorListener);
		this.param = param;
	}
	public StringRequestPost(int method, String url, Listener<String> listener){
		this(method, url, listener, null, null);
	}
	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		// TODO Auto-generated method stub
		return param;
	}

}
