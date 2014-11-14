package com.sysu.bbs.argo.api.dao;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Attachment  implements Parcelable {
	String filename;
	String origname;
	String desc;
	String filetype;
	String articleid;
	String link;
	String is_picture;
	public Attachment(Parcel in) {
		filename = in.readString();
		origname = in.readString();
		desc = in.readString();
		filetype = in.readString();
		articleid = in.readString();
		link = in.readString();
		is_picture = in.readString();
	}
	public Attachment(JSONObject obj) throws JSONException {
		filename = obj.getString("filename");
		origname = obj.getString("origname");
		desc = obj.getString("desc");
		filetype = obj.getString("filetype");
		articleid = obj.getString("articleid");
		link = obj.getString("link");
		is_picture = obj.getString("is_picture");
	}
	public Attachment() {
		
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getOrigname() {
		return origname;
	}
	public void setOrigname(String origname) {
		this.origname = origname;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getFiletype() {
		return filetype;
	}
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}
	public String getArticleid() {
		return articleid;
	}
	public void setArticleid(String articleid) {
		this.articleid = articleid;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getIs_picture() {
		return is_picture;
	}
	public void setIs_picture(String is_picture) {
		this.is_picture = is_picture;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int f) {
		dest.writeString(filename);
		dest.writeString(origname);
		dest.writeString(desc);
		dest.writeString(filetype);
		dest.writeString(articleid);
		dest.writeString(link);
		dest.writeString(is_picture);		
	}
	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public Attachment createFromParcel(Parcel in ) {
			return new Attachment(in);
		}
		public Attachment[] newArray(int size) {
			return new Attachment[size];
		}
	};
}
