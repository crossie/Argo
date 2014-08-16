package com.sysu.bbs.argo.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class ArgoSQLOpenHelper extends SQLiteOpenHelper {

	public ArgoSQLOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		db.execSQL("create table argo_post(_id integer primary key autoincrement, "
				+ "type varchar(100), articleid varchar(100), boardname varchar(100),"
				+ "title varchar(100), content varchar(100));");
		/*db.execSQL("create table board(_id integer primary key autoincrement, "
				+ "seccode varchar(100), title varchar(100), boardname varchar(100));");
		db.execSQL("create table user_info(_id integer primary key autoincrement, "
				+ "userid varchar(100), password varchar(100), last_login timestamp);");
		db.execSQL("create table user_favorite (_id integer primary key autoincrement, "
				+ "userid varchar(100), boardname varchar(100));");*/
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
