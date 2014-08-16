package com.sysu.bbs.argo.api.dao;

import org.json.JSONException;
import org.json.JSONObject;

public class Mail {

	String content;
	String owner;
	String reply;
	String title;
	String index;

	public Mail(JSONObject json) throws JSONException {
		content = json.getString("content");
		owner = json.getString("owner");
		reply = json.getString("reply");
		title = json.getString("title");
		index = json.getString("index");
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getFiletime() {
		return filetime;
	}

	public void setFiletime(String filetime) {
		this.filetime = filetime;
	}

	String flag;
	String filetime;
}
