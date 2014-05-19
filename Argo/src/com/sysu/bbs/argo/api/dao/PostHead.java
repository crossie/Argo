package com.sysu.bbs.argo.api.dao;

import org.json.JSONException;
import org.json.JSONObject;

public class PostHead {
	String update;
	String id;//这个帖子所属主题的第一个帖子的filename中间的那个时间戳（如M.123456789.A的时间戳是123456789
	String title;
	int index;
	String flag;
	String unread;
	String owner;
	String filename;
	String mark;
	String boardname;
	
	public PostHead(JSONObject obj) throws JSONException {
		//TODO initialize other fields
		id = obj.getString("id");
		title = obj.getString("title");
		filename = obj.getString("filename");
		owner = obj.getString("owner");
		index = Integer.valueOf(obj.getString("index"));
		update = obj.getString("update");
		flag = obj.getString("flag");
		mark = obj.getString("mark");
		unread = obj.getString("unread");
		
		
	}
	public String getUpdate() {
		return update;
	}
	public void setUpdate(String update) {
		this.update = update;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getUnread() {
		return unread;
	}
	public void setUnread(String unread) {
		this.unread = unread;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	public String getBoardname() {
		return boardname;
	}
	public void setBoardname(String boardname) {
		this.boardname = boardname;
	}
}
