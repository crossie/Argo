package com.sysu.bbs.argo.util;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;

public class SimpleErrorListener implements ErrorListener {

	Context mContext;
	String mMessage;
	public SimpleErrorListener(Context con, String message) {
		mContext = con;
		mMessage = message;
	}
	@Override
	public void onErrorResponse(VolleyError error) {
		if (mMessage != null)
			Toast.makeText(mContext, mMessage, Toast.LENGTH_SHORT).show();
	}

}
