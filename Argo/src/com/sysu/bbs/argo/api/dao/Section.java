package com.sysu.bbs.argo.api.dao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Section {
	String seccode;
	String secname;
	JSONArray boards;
	
	public Section(JSONObject json) throws JSONException {
		seccode = json.getString("seccode");
		secname = json.getString("secname");
		boards = json.getJSONArray("boards");
	}

	public String getSeccode() {
		return seccode;
	}

	public void setSeccode(String seccode) {
		this.seccode = seccode;
	}

	public String getSecname() {
		return secname;
	}

	public void setSecname(String secname) {
		this.secname = secname;
	}

	public JSONArray getBoards() {
		return boards;
	}

	public void setBoards(JSONArray boards) {
		this.boards = boards;
	}
}
