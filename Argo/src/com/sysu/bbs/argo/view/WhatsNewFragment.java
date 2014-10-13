package com.sysu.bbs.argo.view;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Whatsnew;

public class WhatsNewFragment extends AbstractHomeFragment<Whatsnew> {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mUrl = API.GET.AJAX_V2_TOP_TOPIC + "?cursor=1";
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	protected void response(String _response) {
		try {
			JSONObject result = new JSONObject(_response);
			System.out.println(result.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.response(_response);
	}

	@Override
	public String getBoard(Whatsnew item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFilename(Whatsnew item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Whatsnew newItem(JSONObject json) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayAdapter<Whatsnew> newAdapter() {
		// TODO Auto-generated method stub
		return null;
	}
}
