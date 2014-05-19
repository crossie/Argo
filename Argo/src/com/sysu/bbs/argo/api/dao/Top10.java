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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		this.board = board;
	}
	
	
}
