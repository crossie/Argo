/**
 * use in Favorite.
 * /ajax/user/fav/ return an array of Board objects
 */

package com.sysu.bbs.argo.api.dao;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Board {
	String boardname;
	String title;
	ArrayList<String> bm = null;
	Date lastpost = null;
	int total;
	char seccode;
	String type;
	boolean unread;
	int unreadn;
	String lastpostfile;
	String lastfilename;
	String lastauthor;
	
	public Board(JSONObject json) throws JSONException {
		try {
			boardname = json.getString("boardname").trim();
		} catch(JSONException e) {
			boardname = json.getString("filename").trim();
		}
		title = json.getString("title").trim();
		
		bm = new ArrayList<String>();
		JSONArray bmArr = json.getJSONArray("BM");
		for(int i=0; i< bmArr.length(); i++)
			bm.add(bmArr.getString(i));
		
		//TODO: initialize other members
		unread = json.getString("unread").equals("") ? false : true;
	}
	
	public String getBoardname() {
		return boardname;
	}
	public void setBoardname(String boardname) {
		this.boardname = boardname;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ArrayList<String> getBm() {
		return bm;
	}
	public void setBm(ArrayList<String> bm) {
		this.bm = bm;
	}
	public Date getLastpost() {
		return lastpost;
	}
	public void setLastpost(Date lastpost) {
		this.lastpost = lastpost;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public char getSeccode() {
		return seccode;
	}
	public void setSeccode(char seccode) {
		this.seccode = seccode;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isUnread() {
		return unread;
	}
	public void setUnread(boolean unread) {
		this.unread = unread;
	}
	public int getUnreadn() {
		return unreadn;
	}
	public void setUnreadn(int unreadn) {
		this.unreadn = unreadn;
	}
	public String getLastpostfile() {
		return lastpostfile;
	}
	public void setLastpostfile(String lastpostfile) {
		this.lastpostfile = lastpostfile;
	}
	public String getLastfilename() {
		return lastfilename;
	}
	public void setLastfilename(String lastfilename) {
		this.lastfilename = lastfilename;
	}
	public String getLastauthor() {
		return lastauthor;
	}
	public void setLastauthor(String lastauthor) {
		this.lastauthor = lastauthor;
	}

}
