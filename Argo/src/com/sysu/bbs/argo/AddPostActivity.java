package com.sysu.bbs.argo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.sysu.bbs.argo.util.PostService;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.Splitter;
import com.sysu.bbs.argo.view.LeftMenuFragment;
//import com.sysu.bbs.argo.view.LoginDialog.Communicator;
import com.sysu.bbs.argo.view.LeftMenuFragment.BoardChangedListener;
import com.sysu.bbs.argo.view.LoginDialog;
/**
 * 发表新帖
 * @author abcd
 *
 */
public class AddPostActivity extends SwipeBackActivity implements
		BoardChangedListener {

	private EditText mEditTitle;
	private EditText mEditContent;
	private Bundle mNewPostBundle;

	private Button mChooseBoard;

	private BroadcastReceiver mLoginReceiver;

	private static final int REQUEST_CODE = 6384;
	// String mDraft;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_post);

		/*
		 * getSwipeBackLayout().setEdgeSize(
		 * getWindowManager().getDefaultDisplay().getWidth());
		 */

		mEditTitle = (EditText) findViewById(R.id.new_post_title);
		mEditContent = (EditText) findViewById(R.id.new_post_content);
		mChooseBoard = (Button) findViewById(R.id.new_post_choose_board);
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		//从其他app分享过来
		if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {

			mEditContent.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
			mNewPostBundle = new Bundle();
			mNewPostBundle.putString("type", "new");
			mChooseBoard.setVisibility(View.VISIBLE);
			mChooseBoard.setText("选择版面");

			return;
		}
		mNewPostBundle = intent.getExtras();

		if (mNewPostBundle.getInt("_where_") == 1) { // reply button
			if (mNewPostBundle.getString("type").equals("reply")) {

				if (!mNewPostBundle.getString("title").startsWith("Re: "))
					mEditTitle.setText("Re: "
							+ mNewPostBundle.getString("title"));
				else
					mEditTitle.setText(mNewPostBundle.getString("title"));

				String tmp = mNewPostBundle.getString("content");

				tmp = tmp.substring(0, Math.min(150, tmp.length()));
				tmp = tmp.replaceAll("(?m)^", ": ").replace("\n", "<br/>");

				String quote = "<br/><font color=\"#888888\">【 在 %s (%s) 的大作中提到: 】<br/>%s</font>";
				quote = String.format(quote,
						mNewPostBundle.getString("userid"),
						mNewPostBundle.getString("username"), tmp);

				mEditContent.setText(Html.fromHtml(quote));
			}
		} else if (mNewPostBundle.getInt("_where_") == 2) {// draft

			mEditTitle.setText(mNewPostBundle.getString("title"));

			String content = "%s<br/><font color=\"#888888\">%s</font>";
			String parsedContent = "";
			String parsedQuote = "";

			Splitter splitter = new Splitter("【 在 .* 的大作中提到: 】", true);
			String[] tmp = splitter.split(mNewPostBundle.getString("content"));
			if (tmp != null) {
				parsedContent = tmp[0];
				if (tmp.length > 1) {
					parsedQuote = "";
					for (int i = 1; i < tmp.length; i++) {
						parsedQuote += tmp[i];
						if (i < tmp.length - 1)
							parsedQuote +=  "<br/>";
					}
				}

			}
			content = String.format(content, parsedContent, parsedQuote).replace("\n", "<br/>");

			mEditContent.setText(Html.fromHtml(content));

			// mDraft = mNewPostBundle.getString("_draft_");
		}
		
		if (mNewPostBundle.getString("boardname", "null").equals("null")) {
			mNewPostBundle.putString("type", "new");
			mChooseBoard.setVisibility(View.VISIBLE);
			mChooseBoard.setText("选择版面");
		}

		mEditContent.post(new Runnable() {

			@Override
			public void run() {
				mEditContent.requestFocus();
				
			}
			
		});
		mLoginReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context con, Intent intent) {
				String action = intent.getAction();
				if (action.equals(SessionManager.BROADCAST_LOGIN)) {
					String userid = intent.getStringExtra("userid");
					if (userid != null && !userid.equals("")) {
						sendPost();
					}
				}

			}
		};

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SessionManager.BROADCAST_LOGIN);
		registerReceiver(mLoginReceiver, intentFilter);
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		try {
			unregisterReceiver(mLoginReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_new_post, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.add_attachment:
	        Intent target = FileUtils.createGetContentIntent();
	        // Create the chooser Intent
	        Intent intent = Intent.createChooser(
	                target, getString(R.string.chooser_title));
	        try {
	            startActivityForResult(intent, REQUEST_CODE);
	        } catch (ActivityNotFoundException e) {
	            // The reason for the existence of aFileChooser
	        }
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
		LeftMenuFragment chooseBoard = null;
		switch (v.getId()) {
		case R.id.new_post_choose_board:
			chooseBoard = new LeftMenuFragment();
			chooseBoard.show(getSupportFragmentManager(), "chooseboard");
			break;
		case R.id.new_post_send:
			if (!SessionManager.isLoggedIn) {

				LoginDialog loginDialog = new LoginDialog();
				loginDialog.show(getSupportFragmentManager(), "loginDialog");
				break;

			}
			if (mNewPostBundle.getString("boardname") == null
					|| mNewPostBundle.getString("boardname").equals("")) {
				chooseBoard = new LeftMenuFragment();
				chooseBoard.show(getSupportFragmentManager(), "chooseboard");
				break;
			}
			sendPost();
			break;
		}

	}

	public void sendPost() {
		Intent service = new Intent(this, PostService.class);

		mNewPostBundle.putString("title", mEditTitle.getText().toString());
		mNewPostBundle.putString("content", mEditContent.getText().toString());

		service.putExtras(mNewPostBundle);

		startService(service);
		finish();
	}

	@Override
	public void changeBoard(String boardname) {
		mNewPostBundle.putString("boardname", boardname);
		mChooseBoard.setText(boardname);

	}
	
	@Override
	public void onBackPressed() {
		String draft = mNewPostBundle.getString("_draft_");
		if (draft != null && !draft.equals("")) {
			super.onBackPressed();
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this, AlertDialog.THEME_HOLO_DARK);
		builder.setMessage("保存草稿？")
		.setPositiveButton("是", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				mNewPostBundle.putString("title", mEditTitle.getText().toString());
				mNewPostBundle.putString("content", mEditContent.getText().toString());
				add2Draft(mNewPostBundle);
				AddPostActivity.super.onBackPressed();
				
			}
		}).setNegativeButton("否", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				AddPostActivity.super.onBackPressed();
				
			}
		})
		.setNeutralButton("取消", null)
		.show();
		
	}
	
	private void add2Draft(Bundle bundle) {

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
			bw.write(bundle.getString("content"));
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
