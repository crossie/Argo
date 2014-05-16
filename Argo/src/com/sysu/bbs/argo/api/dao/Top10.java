package com.sysu.bbs.argo.api.dao;

import java.sql.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class Top10 {
	String filename;
	String author;
	int num;
	Date time;
	String title;
	String board;
	
	public Top10(JSONObject json) throws JSONException {
		filename = json.getString("filename");
		author = json.getString("author");
		num = Integer.parseInt(json.getString("num").trim());
		time = new Date(Long.parseLong(json.getString("time").toString()));
		title = json.getString("title");
		board = json.getString("board");
	}
	
	
}
