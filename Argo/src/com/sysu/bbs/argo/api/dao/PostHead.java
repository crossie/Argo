package com.sysu.bbs.argo.api.dao;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class PostHead implements Parcelable {
	String update;
	String id;//���������������ĵ�һ�����ӵ�filename�м���Ǹ�ʱ�������M.123456789.A��ʱ�����123456789
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
	public PostHead(String filename) {
		this.filename = filename;
		unread = "0";
	}
	public PostHead(Parcel in) {
		update = in.readString();
		id = in.readString();
		title = in.readString();
		index = in.readInt();
		flag = in.readString();
		unread = in.readString();
		owner = in.readString();
		filename = in.readString();
		mark = in.readString();
		boardname = in.readString();
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
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int f) {
		dest.writeString(update);
		dest.writeString(id);
		dest.writeString(title);
		dest.writeInt(index);
		dest.writeString(flag);
		dest.writeString(unread);
		dest.writeString(owner);
		dest.writeString(filename);
		dest.writeString(mark);
		dest.writeString(boardname);
		
	}
	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public PostHead createFromParcel(Parcel in ) {
			return new PostHead(in);
		}
		public PostHead[] newArray(int size) {
			return new PostHead[size];
		}
	};
}
