package com.sysu.bbs.argo.util;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;

public class SimpleErrorListener implements ErrorListener {

	Context context;
	public SimpleErrorListener(Context con) {
		context = con;
	}
	@Override
	public void onErrorResponse(VolleyError error) {
		Toast.makeText(context, "ÍøÂç´íÎó", Toast.LENGTH_SHORT).show();
	}

}
