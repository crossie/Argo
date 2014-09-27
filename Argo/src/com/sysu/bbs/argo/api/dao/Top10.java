package com.sysu.bbs.argo.api.dao;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Top10  implements Parcelable {
	String filename;
	String author;
	int num;
	String time;
	String title;
	String board;
	
	public Top10(JSONObject json) throws JSONException {
		filename = json.getString("filename");
		author = json.getString("author");
		num = Integer.parseInt(json.getString("num").trim());
		time = json.getString("time");
		title = json.getString("title");
		board = json.getString("board");
	}

	public Top10(Parcel in) {
		filename = in.readString();
		author = in.readString();
		num = in.readInt();
		time = in.readString();
		title = in.readString();
		board = in.readString();
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

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
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

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int f) {
		dest.writeString(filename);
		dest.writeString(author);
		dest.writeInt(num);
		dest.writeString(time);
		dest.writeString(title);
		dest.writeString(board);
		
	}
	
	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public Top10 createFromParcel(Parcel in ) {
			return new Top10(in);
		}
		public Top10[] newArray(int size) {
			return new Top10[size];
		}
	};
}
