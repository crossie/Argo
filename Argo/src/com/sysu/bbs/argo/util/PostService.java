package com.sysu.bbs.argo.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.sysu.bbs.argo.DraftActivity;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.api.API;

public class PostService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null)
			return START_NOT_STICKY;

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		final NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		final Bundle postBundle = intent.getExtras();

		final int notificationID = new Random().nextInt();

		final String type = postBundle.getString("type");
		final String boardname = postBundle.getString("boardname");
		final String articleid = postBundle.getString("articleid");
		final String title = postBundle.getString("title");
		final String content = postBundle.getString("content");

		HashMap<String, String> param = new HashMap<String, String>();
		param.put("type", type);
		param.put("boardname", boardname);
		if (articleid != null && !articleid.equals(""))
			param.put("articleid", articleid);
		param.put("title", title);
		param.put("content", content);

		builder.setContentTitle("正在发送帖子...")
				.setSmallIcon(R.drawable.ic_launcher).setContentText(title)
				.setTicker("正在发送帖子...");

		notifyMgr.notify(notificationID, builder.build());

		Volley.newRequestQueue(this).add(
				new StringRequestPost(API.POST.AJAX_POST_ADD,
						new Listener<String>() {
							@Override
							public void onResponse(String response) {
								try {
									JSONObject res = new JSONObject(response);
									if (res.getString("success").equals("1")) {
										builder.setContentTitle("发送成功")
												.setTicker("发送成功");
										notifyMgr.notify(notificationID,
												builder.build());

										Handler handler = new Handler();
										handler.postDelayed(
												new Runnable() {
													@Override
													public void run() {
														notifyMgr.cancel(notificationID);
													}
												}, 5000);
										String draft = postBundle.getString("_draft_");
										if (draft != null && !draft.equals("")) {
											File draftFile = new File(draft);
											draftFile.delete();
										}
									} else {

										postFailed(postBundle, notificationID);

									}
								} catch (JSONException e) {
									Toast.makeText(
											PostService.this,
											"unexpected error in adding new post",
											Toast.LENGTH_LONG).show();
									e.printStackTrace();
									postFailed(postBundle, notificationID);
								} finally {
									stopSelf();
								}

							}
						}, new ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError error) {
								// TODO Auto-generated method stub
								postFailed(postBundle, notificationID);
							}

						}, param));
		return START_NOT_STICKY;
	}

	private void postFailed(Bundle bundle, final int notificationID) {
		
		add2Draft(bundle);
		
		Intent resultIntent = new Intent(PostService.this,
				DraftActivity.class);
		//bundle.putInt("_where_", 2);

		//resultIntent.putExtras(bundle);

		resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(
				PostService.this, 0, resultIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this);
		final NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		builder.setContentIntent(resultPendingIntent);

		builder.setContentTitle("发送失败").setContentIntent(resultPendingIntent)
				.setTicker("发送失败").setSmallIcon(R.drawable.ic_launcher)
				.setContentText(bundle.getString("title"));
		Notification notification = builder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notifyMgr.notify(notificationID, notification);
		
		Handler handler = new Handler();
		handler.postDelayed(
				new Runnable() {
					@Override
					public void run() {
						notifyMgr.cancel(notificationID);
					}
				}, 5000);
	}

	private void add2Draft(Bundle bundle) {
/*		ArgoSQLOpenHelper dbHelper = new ArgoSQLOpenHelper(this, "argo.db", null, 1);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		db.execSQL("insert into argo_post(type, boardname, articleid, title, content) values(?,?,?,?,?);"
				, new String[]{bundle.getString("type"),
						bundle.getString("boardname"),
						bundle.getString("articleid"),
						bundle.getString("title"),
						bundle.getString("content")});
		db.close();
		dbHelper.close();*/

		File draftDir = new File(getFilesDir(), "Draft");
		if (!draftDir.exists())
			draftDir.mkdir();
		File post = new File(draftDir, System.currentTimeMillis() + "");
		FileOutputStream fos = null; 
		BufferedWriter bw = null; 
		
		try {
			fos = new FileOutputStream(post);
			bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			bw.write(bundle.getString("type") + "\n");
			bw.write(bundle.getString("boardname") + "\n");
			bw.write(bundle.getString("articleid") + "\n");
			bw.write(bundle.getString("title") + "\n");
			bw.write(System.currentTimeMillis() + "\n");
			bw.write(bundle.getString("content") + "\n");
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
