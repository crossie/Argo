package com.sysu.bbs.argo.view;


import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.api.API;

public class MailFragment extends Fragment implements OnRefreshListener<ListView> {
	
	private int mTotal;
	private PullToRefreshListView mMailListView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_mail, container, false);
		mMailListView = (PullToRefreshListView) v.findViewById(R.id.frag_mail_list);
		mMailListView.setOnRefreshListener(this);
		return v;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
		requestQueue.add(new StringRequest(API.GET.AJAX_MAIL_MAILBOX, 
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						
					}
				}, null));
		requestQueue.add(new StringRequest(API.GET.AJAX_MAIL_LIST + "?start=227", 
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d("mail", response);
						
					}
				}, null));
		requestQueue.add(new StringRequest(API.GET.AJAX_MAIL_GET + "?index=229", 
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						try {
							JSONObject a = new JSONObject(response);
							Log.d("mail", a.toString());
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}, null));
		super.onActivityCreated(savedInstanceState);
	}
	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		// TODO Auto-generated method stub
		
	}
	
}
