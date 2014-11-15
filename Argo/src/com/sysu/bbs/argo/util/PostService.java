package com.sysu.bbs.argo.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.sysu.bbs.argo.AddPostActivity;
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

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Bundle postBundle = intent.getExtras();
		int notificationID = new Random().nextInt();
		builder.setContentTitle("正在发送帖子...")
				.setSmallIcon(R.drawable.ic_launcher).setContentText(postBundle.getString("title"))
				.setTicker("正在发送帖子...");
		notifyMgr.notify(notificationID, builder.build());
		post(postBundle, notificationID);

		return START_NOT_STICKY;
	}
	private void post(final Bundle postBundle, final int notificationID) {
		ArrayList<String> paramList = new ArrayList<String>();
		paramList.add("type");
		paramList.add(postBundle.getString("type"));
		paramList.add("boardname");
		paramList.add(postBundle.getString("boardname"));
		paramList.add("title");
		paramList.add(postBundle.getString("title"));
		paramList.add("content");
		paramList.add(postBundle.getString("content"));
		if (postBundle.getString("articleid") != null && !postBundle.getString("articleid").equals("")) {
			paramList.add("articleid");
			paramList.add(postBundle.getString("articleid"));
			
		}
		if (postBundle.getString("attach") != null && !postBundle.getString("attach").equals("")) {
			paramList.add("attach");
			paramList.add(postBundle.getString("attach"));
			
		}

		String[] params = new String[paramList.size()];
		paramList.toArray(params);
		new PostTask(postBundle, notificationID).execute(params);
	}
	private void postFailed(Bundle bundle, final int notificationID) {
		String draft = bundle.getString("_draft_");
		if (draft == null || draft.equals("")) {
			add2Draft(bundle);
		}
		
		updateNotification(notificationID, "发送失败", bundle.getString("title"));
/*
		Intent resultIntent = new Intent(PostService.this, DraftActivity.class);

		resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(
				PostService.this, 0, resultIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
*/

	}
	
	private void updateNotification(final int notifyId, String title, String text) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this);
		final NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		builder.setContentTitle(title)
				.setTicker(title).setSmallIcon(R.drawable.ic_launcher)
				.setContentText(text);
		Notification notification = builder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notifyMgr.notify(notifyId, notification);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				notifyMgr.cancel(notifyId);
			}
		}, 5000);
	}
	private void add2Draft(Bundle source) {

		File draftDir = new File(getFilesDir(), "Draft");
		if (!draftDir.exists())
			draftDir.mkdir();
		File post = new File(draftDir, System.currentTimeMillis() + "");
		DraftActivity.add2Draft(post, source);
	}
	
	class PostTask extends AsyncTask<String, Void, String> {
		
		Bundle postBundle;
		int notificationID;
		
		public PostTask(Bundle postbundle, int notficationid) {
			postBundle = postbundle;
			notificationID = notficationid;
		}

		@Override
		protected String doInBackground(String... params) {
			String boundary = Long.toHexString(System.currentTimeMillis());
			URLConnection connection = null;
			OutputStream os = null;
			FileInputStream fis = null;
			try {
				connection = new URL(API.POST.AJAX_POST_ADD).openConnection();
				connection.setDoOutput(true); // This sets request method to POST.
				connection.setRequestProperty("Content-Type",
						"multipart/form-data; boundary=" + boundary);
				connection.setConnectTimeout(30 * 1000);
				connection.setReadTimeout(30 * 1000);
				os = connection.getOutputStream();
				for (int i = 0; i < params.length - 1; i += 2) {
					StringBuffer sb = new StringBuffer();
					sb.append("\r\n--").append(boundary).append("\r\n");
					if (!params[i].equals("attach")) {
						sb.append(String.format("Content-Disposition: form-data; name=\"%s\"\r\n",params[i]));
						sb.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
						sb.append(params[i + 1]);
						os.write(sb.toString().getBytes());							
					} else {
						int start = params[i+1].lastIndexOf("/") + 1;
						int end = params[i+1].length();
						String filename = params[i+1].substring(start, end);
						File f = new File(params[i + 1]);
						fis = new FileInputStream(f);
						sb.append(String.format("Content-Disposition: form-data; name=\"attach\"; filename=\"%s\"\r\n",filename));						
						String mimeType = AddPostActivity.getMIMEType(f);
						if (mimeType != null && !mimeType.equals("*/*"))
		                	sb.append(String.format("Content-Type: %s;\r\n\r\n", mimeType));
		                else 
		                	sb.append("Content-type: application/octet-stream\r\n\r\n");
		                os.write(sb.toString().getBytes());
		                boolean isImage = filename.endsWith("jpg") || filename.endsWith("jpeg") ||
		                		filename.endsWith("png") || filename.endsWith("bmp") ||
		                		filename.endsWith("gif");
		                if (isImage && f.length() > 1024*1024 ) {
		                	Bitmap bitmap = BitmapFactory.decodeStream(fis);
		                	bitmap.compress(CompressFormat.JPEG, (int)(100*1024*1024/f.length()), os);
		                } else {							
							byte[] buffer = new byte[1024];
							int count = 0;
							while ((count = fis.read(buffer)) > 0)
								os.write(buffer, 0, count);
		                }		            						
					}
				}
				os.write(("\r\n--" + boundary + "--\r\n").getBytes());
			} catch (IOException e) {
				return null;
			} finally {
				try {
					if (os != null) 
						os.close();
					if (fis != null) 
						fis.close();
				} catch (IOException logOrIgnore) {
					return null;
				}
			}				
			try {
				// Connection is lazily executed whenever you request any status.
				int responseCode = ((HttpURLConnection) connection).getResponseCode();
				if (responseCode != 200)
					return null;
				InputStream in = null;
				BufferedInputStream bis = null;
				ByteArrayOutputStream baos = null;
				in = connection.getInputStream();
				bis = new BufferedInputStream(in);
				baos = new ByteArrayOutputStream();
				int i;
				while ((i = bis.read()) > 0)
					baos.write(i);

				return baos.toString();
			
			} catch (IOException e) {
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				postFailed(postBundle, notificationID);
				stopSelf();
				return;
			}
			
			try {
				JSONObject res = new JSONObject(result);
				if (res.getString("success").equals("1")) {
					updateNotification(notificationID, "发送成功", postBundle.getString("title"));
					String draft = postBundle.getString("_draft_");
					if (draft != null && !draft.equals("")) {
						File draftFile = new File(draft);
						draftFile.delete();
					}
				} else {
					postFailed(postBundle, notificationID);
				}
			} catch (JSONException e) {
				Toast.makeText(PostService.this,
						"unexpected error in adding new post",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
				postFailed(postBundle, notificationID);
			} finally {
				stopSelf();
			}				
		}			
	}

}
