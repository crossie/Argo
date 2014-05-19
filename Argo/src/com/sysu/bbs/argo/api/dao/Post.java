package com.sysu.bbs.argo.api.dao;

import java.sql.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sysu.bbs.argo.util.Splitter;

public class Post {
	String userid;
	String username;
	String title;
	String board;
	Date post_time;
	String rawcontent;
	String rawsignature;
	String bbsname;
	Attachment ah;
	String filename;
	String perm_del;
	
	String parsedContent;
	String parsedQuote;
	
	public Post(JSONObject obj) throws JSONException {
		userid = obj.getString("userid");
		username = obj.getString("username");
		title = obj.getString("title");
		board = obj.getString("board");
		rawcontent = obj.getString("rawcontent");
		filename = obj.getString("filename");
		Log.d("post",obj.toString());
		//TODO initialize other fields
		
		parse(rawcontent);
	}
	
	private void parse(String rawcontent2) {
/*		String[] tmp = rawcontent2.split("【 在 .* 的大作中提到: 】");
		if (tmp != null) {
			parsedContent = tmp[0];
			Log.d("parsed", parsedContent);
			if (tmp.length > 1) {
				parsedQuote = tmp[1].replaceFirst(": ", "");
				Log.d("parsed", parsedQuote);
			}
		}*/
		Splitter splitter = new Splitter("【 在 .* 的大作中提到: 】", true);
		String[] tmp = splitter.split(rawcontent2);
		if (tmp != null) {
			parsedContent = tmp[0].replaceAll("(?m)^[ \t]*\r?\n", "").trim();
			Log.d("parsed", parsedContent);
			if (tmp.length > 1) {
				parsedQuote = "";
				for (int i = 1; i < tmp.length; i++)
					parsedQuote += tmp[i];
				parsedQuote = parsedQuote.trim();
				Log.d("parsed", parsedQuote);
			}
			
		}
		
	}

	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
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
	public Date getPost_time() {
		return post_time;
	}
	public void setPost_time(Date post_time) {
		this.post_time = post_time;
	}
	public String getRawcontent() {
		return rawcontent;
	}
	public void setRawcontent(String rawcontent) {
		this.rawcontent = rawcontent;
	}
	public String getRawsignature() {
		return rawsignature;
	}
	public void setRawsignature(String rawsignature) {
		this.rawsignature = rawsignature;
	}
	public String getBbsname() {
		return bbsname;
	}
	public void setBbsname(String bbsname) {
		this.bbsname = bbsname;
	}
	public Attachment getAh() {
		return ah;
	}
	public void setAh(Attachment ah) {
		this.ah = ah;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getPerm_del() {
		return perm_del;
	}
	public void setPerm_del(String perm_del) {
		this.perm_del = perm_del;
	}

	public String getParsedContent() {
		return parsedContent;
	}

	public void setParsedContent(String parsedContent) {
		this.parsedContent = parsedContent;
	}

	public String getParsedQuote() {
		return parsedQuote;
	}

	public void setParsedQuote(String parsedQuote) {
		this.parsedQuote = parsedQuote;
	}
	
	
}
