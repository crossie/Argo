package com.sysu.bbs.argo.api.dao;

import org.json.JSONObject;

import android.os.Parcelable;

public abstract class HomeData implements Parcelable {
	abstract public String getBoard();
	abstract public String getFilename();
	abstract public void init(JSONObject json);
}
