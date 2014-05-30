package com.sysu.bbs.argo.util;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.android.volley.Response.Listener;
import com.android.volley.toolbox.Volley;
import com.sysu.bbs.argo.AddPostActivity;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.api.API;

public class PostService extends Service {

	NotificationCompat.Builder builder;
	NotificationManager notifyMgr;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null)
			return START_NOT_STICKY;
		Bundle newPostBundle = intent.getExtras();
		
		final String type = newPostBundle.getString("type");
		final String boardname = newPostBundle.getString("boardname");
		final String articleid = newPostBundle.getString("articleid");
		final String title = newPostBundle.getString("title");
		final String content = newPostBundle.getString("content");

		HashMap<String, String> param = new HashMap<String, String>();
		param.put("type", type);
		param.put("boardname", boardname);
		if (articleid != null && !articleid.equals(""))
			param.put("articleid", articleid);
		param.put("title", title);
		param.put("content", content);

		builder = new NotificationCompat.Builder(this);
		builder.setContentTitle("正在发送帖子...").setSmallIcon(R.drawable.ic_launcher)
		.setContentText(title).setTicker("正在发送帖子...");
		
		notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notifyMgr.notify(0, builder.build());

		Volley.newRequestQueue(this).add(
				new StringRequestPost(API.POST.AJAX_POST_ADD,
						new AddPostListener(), null, param));
		return START_NOT_STICKY;
	}

	private class CancelNotification implements Runnable {
		@Override
		public void run() {
			notifyMgr.cancel(0);
		}
	}

	private class AddPostListener implements Listener<String> {
		@Override
		public void onResponse(String response) {
			try {
				JSONObject res = new JSONObject(response);
				if (res.getString("success").equals("1")) {
					builder.setContentTitle("发送成功").setTicker("发送成功");
					notifyMgr.notify(0, builder.build());

					Handler handler = new Handler();
					handler.postDelayed(new CancelNotification(), 5000);
				} else {

					Intent resultIntent = new Intent(PostService.this,
							AddPostActivity.class);
					//TODO need to put the post info in the intent ?
					TaskStackBuilder stackBuilder = TaskStackBuilder
							.create(PostService.this);
					// Adds the back stack
					stackBuilder.addParentStack(AddPostActivity.class);
					// Adds the Intent to the top of the
					// stack
					stackBuilder.addNextIntent(resultIntent);
					// Gets a PendingIntent containing the
					// entire back stack
					PendingIntent resultPendingIntent = stackBuilder
							.getPendingIntent(0,
									PendingIntent.FLAG_UPDATE_CURRENT);

					builder.setContentTitle("发送失败").setContentIntent(
							resultPendingIntent).setTicker("发送失败");
					notifyMgr.notify(0, builder.build());

				}
			} catch (JSONException e) {
				Toast.makeText(PostService.this,
						"unexpected error in adding new post",
						Toast.LENGTH_LONG).show();
			} finally {
				stopSelf();
			}

		}
	}

}
