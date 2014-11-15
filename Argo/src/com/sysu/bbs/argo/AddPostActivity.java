package com.sysu.bbs.argo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Html;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
	private ImageButton mAttachButton;
	private Bundle mNewPostBundle;
	private String mAttachPath = "";
	private Uri mCapturedUri;
	/**
	 * 判断是否从其他应用分享内容到argo
	 */
	private boolean mIsShared = false;

	//private Button mChooseBoard;

	private BroadcastReceiver mLoginReceiver;
	

	private static final int REQUEST_CODE_CHOOSE_FILE = 6384;
	private static final int REQUEST_CODE_CAMERA = 6385;
	/**
	 * 保存相机返回的结果
	 */
	//private Uri mCapturedUri;
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
		//mChooseBoard = (Button) findViewById(R.id.new_post_choose_board);
		mAttachButton = (ImageButton) findViewById(R.id.attachment);
		registerForContextMenu(mAttachButton);
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		//从其他app分享过来
		if (Intent.ACTION_SEND.equals(action) ) {			
			mNewPostBundle = new Bundle();
			mNewPostBundle.putString("type", "new");			
			mIsShared = true;
			if ("text/plain".equals(type)) {
				mEditContent.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
			} else if (type.matches("image/.*")) {
				Bundle extras = intent.getExtras();
				if (extras.containsKey(Intent.EXTRA_STREAM)) {
					Uri uri = (Uri)extras.getParcelable(Intent.EXTRA_STREAM);
					String scheme = uri.getScheme();
		            if(scheme.equals("content")){
		                ContentResolver cr = getContentResolver();
		                Cursor c = cr.query(uri,null,null,null,null);
		                c.moveToFirst();
		                mAttachPath = c.getString(c.getColumnIndexOrThrow(Images.Media.DATA));
		                c.close();
		                mAttachButton.setImageResource(R.drawable.ic_action_picture);
		            }
				}
			}
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
			//mChooseBoard.setVisibility(View.VISIBLE);
			//mChooseBoard.setText("选择版面");
		}
		
		mAttachPath = mNewPostBundle.getString("attach");
		if ( mAttachPath != null && !mAttachPath.equals("")) {
			//TODO 设置附件按钮图标
		} else {
			//保证mAttachPath不为null
			mAttachPath = "";
		}

		//将输入焦点放到正文输入框
		mEditContent.post(new Runnable() {

			@Override
			public void run() {
				mEditContent.requestFocus();
				
			}
			
		});
		
		//登录后自动发送帖子
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.new_post_choose_board);
		String boardname = mNewPostBundle.getString("boardname");
		if ( boardname == null 	|| boardname.equals("")) 
			item.setTitle(R.string.new_post_choose_board);
		else
			item.setTitle(boardname);
		return super.onPrepareOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.new_post_choose_board:
			if ( mIsShared) {
				LeftMenuFragment chooseBoard = new LeftMenuFragment();
				chooseBoard.show(getSupportFragmentManager(), "chooseboard");
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
		LeftMenuFragment chooseBoard = null;
		switch (v.getId()) {
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
		case R.id.attachment:
			if (mAttachPath != null && !mAttachPath.equals("")) {
				v.showContextMenu();
				break;
			}
			chooseFile();
	        break;
		case R.id.start_camera:
			String state = Environment.getExternalStorageState();
			if (!state.equals(Environment.MEDIA_MOUNTED)) {
				Toast.makeText(this, "外部存储不可用", Toast.LENGTH_SHORT).show();
				return;
			}
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
									"Argo");
			if (!dir.exists())
				dir.mkdir();
			String filename = new SimpleDateFormat("yyyyMMddHHss").format(new Date());
			File file = new File(dir, "IMG_" + filename + ".jpg");
			mCapturedUri = Uri.fromFile(file);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedUri);
			startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
			overridePendingTransition(R.anim.open_enter_slide_in, R.anim.open_exit_slide_out);
			break;
		}

	}

	public void sendPost() {
		if (mAttachPath != null && !mAttachPath.equals("")) {
			File file = new File(mAttachPath);
			if (!file.exists() || !file.canRead()) {
				Toast.makeText(this, "文件不存在或不可读", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		Intent service = new Intent(this, PostService.class);
		mNewPostBundle.putString("title", mEditTitle.getText().toString());
		mNewPostBundle.putString("content", mEditContent.getText().toString());	
		mNewPostBundle.putString("attach", mAttachPath);
		service.putExtras(mNewPostBundle);
		startService(service);
		finish();
	}

	@Override
	public void changeBoard(String boardname) {
		mNewPostBundle.putString("boardname", boardname);
		//mChooseBoard.setText(boardname);

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
				mNewPostBundle.putString("attach", mAttachPath);
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
	
	private void add2Draft(Bundle source) {

		File draftDir = new File(getFilesDir(), "Draft");
		if (!draftDir.exists())
			draftDir.mkdir();
		File post = new File(draftDir, System.currentTimeMillis() + "");
		DraftActivity.add2Draft(post, source);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) 
			return;
        switch (requestCode) {
        case REQUEST_CODE_CHOOSE_FILE:
            // Get the URI of the selected file
            if (data == null)
            	return;
            final Uri uri = data.getData();
            try {
                // Get the file path from the URI
            	mAttachPath = FileUtils.getPath(this, uri);
                File attach = new File(mAttachPath);
                String pathLower = mAttachPath.toLowerCase();
                if (attach.length() > 1024*1024 && 
                		!(pathLower.endsWith("jpg") || pathLower.endsWith("jpeg") ||
                		pathLower.endsWith("png") || pathLower.endsWith("bmp") ||
                		pathLower.endsWith("gif")) ) {
                	Toast.makeText(this, "非图片文件大小不能超过1MB", Toast.LENGTH_SHORT).show();
                	return;
                }
                
                //change the icon according to the file type
                if (pathLower.endsWith("jpg") || pathLower.endsWith("jpeg") ||
                		pathLower.endsWith("png") || pathLower.endsWith("bmp") ||
                		pathLower.endsWith("gif")) 
                	mAttachButton.setImageResource(R.drawable.ic_action_picture);
                else
                	mAttachButton.setImageResource(R.drawable.ic_action_attachment);
            } catch (Exception e) {
                
            }
            break;
        case REQUEST_CODE_CAMERA:
            mAttachPath = mCapturedUri.getPath();
            mAttachButton.setImageResource(R.drawable.ic_action_picture);
        	break;
        }        	
        super.onActivityResult(requestCode, resultCode, data);
	}
	/**
	 * 实现退出动画，未知为何要这样写才会有动画
	 */
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.close_enter_slide_in, R.anim.close_exit_slide_out);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(R.layout.activity_new_post, R.string.menu_view_attach, 0, R.string.menu_view_attach);
		menu.add(R.layout.activity_new_post, R.string.menu_delete_attach, 0, R.string.menu_delete_attach);
		menu.add(R.layout.activity_new_post, R.string.menu_reselect_attach, 0, R.string.menu_reselect_attach);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() != R.layout.activity_new_post)
			return false;
		switch(item.getItemId()) {
		case R.string.menu_view_attach:
			File file = new File(mAttachPath);
			if (!file.exists() || !file.canRead()) {
				Toast.makeText(this, "文件不存在或不可读", Toast.LENGTH_SHORT).show();
				mAttachPath = null;
				mAttachButton.setImageResource(R.drawable.ic_action_new_attachment);
				return true;
			}				
			Intent intent = new Intent();        
			intent.setAction("android.intent.action.VIEW");    
			//Uri content_url = Uri.fromFile(file);
			intent.setDataAndType(Uri.fromFile(file), getMIMEType(file));  
			try {
				startActivity(intent);
				overridePendingTransition(R.anim.open_enter_slide_in, R.anim.open_exit_slide_out);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(this, "找不到关联的应用程序", Toast.LENGTH_SHORT).show();
				return true;
			}
			return true;
		case R.string.menu_delete_attach:
			mAttachPath = null;
			mAttachButton.setImageResource(R.drawable.ic_action_new_attachment);
			return true;
		case R.string.menu_reselect_attach:
			chooseFile();
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	private void chooseFile() {
		Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_FILE);
            overridePendingTransition(R.anim.open_enter_slide_in, R.anim.open_exit_slide_out);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
	}
	
	public static final String[][] MIME_MapTable= { 
            //{后缀名，MIME类型} 
            {".3gp",    "video/3gpp"}, 
            {".apk",    "application/vnd.android.package-archive"}, 
            {".asf",    "video/x-ms-asf"}, 
            {".avi",    "video/x-msvideo"}, 
            {".bin",    "application/octet-stream"}, 
            {".bmp",    "image/bmp"}, 
            {".c",  "text/plain"}, 
            {".class",  "application/octet-stream"}, 
            {".conf",   "text/plain"}, 
            {".cpp",    "text/plain"}, 
            {".doc",    "application/msword"}, 
            {".docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document"}, 
            {".xls",    "application/vnd.ms-excel"},  
            {".xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}, 
            {".exe",    "application/octet-stream"}, 
            {".gif",    "image/gif"}, 
            {".gtar",   "application/x-gtar"}, 
            {".gz", "application/x-gzip"}, 
            {".h",  "text/plain"}, 
            {".htm",    "text/html"}, 
            {".html",   "text/html"}, 
            {".jar",    "application/java-archive"}, 
            {".java",   "text/plain"}, 
            {".jpeg",   "image/jpeg"}, 
            {".jpg",    "image/jpeg"}, 
            {".js", "application/x-javascript"}, 
            {".log",    "text/plain"}, 
            {".m3u",    "audio/x-mpegurl"}, 
            {".m4a",    "audio/mp4a-latm"}, 
            {".m4b",    "audio/mp4a-latm"}, 
            {".m4p",    "audio/mp4a-latm"}, 
            {".m4u",    "video/vnd.mpegurl"}, 
            {".m4v",    "video/x-m4v"},  
            {".mov",    "video/quicktime"}, 
            {".mp2",    "audio/x-mpeg"}, 
            {".mp3",    "audio/x-mpeg"}, 
            {".mp4",    "video/mp4"}, 
            {".mpc",    "application/vnd.mpohun.certificate"},        
            {".mpe",    "video/mpeg"},   
            {".mpeg",   "video/mpeg"},   
            {".mpg",    "video/mpeg"},   
            {".mpg4",   "video/mp4"},    
            {".mpga",   "audio/mpeg"}, 
            {".msg",    "application/vnd.ms-outlook"}, 
            {".ogg",    "audio/ogg"}, 
            {".pdf",    "application/pdf"}, 
            {".png",    "image/png"}, 
            {".pps",    "application/vnd.ms-powerpoint"}, 
            {".ppt",    "application/vnd.ms-powerpoint"}, 
            {".pptx",   "application/vnd.openxmlformats-officedocument.presentationml.presentation"}, 
            {".prop",   "text/plain"}, 
            {".rc", "text/plain"}, 
            {".rmvb",   "audio/x-pn-realaudio"}, 
            {".rtf",    "application/rtf"}, 
            {".sh", "text/plain"}, 
            {".tar",    "application/x-tar"},    
            {".tgz",    "application/x-compressed"},  
            {".txt",    "text/plain"}, 
            {".wav",    "audio/x-wav"}, 
            {".wma",    "audio/x-ms-wma"}, 
            {".wmv",    "audio/x-ms-wmv"}, 
            {".wps",    "application/vnd.ms-works"}, 
            {".xml",    "text/plain"}, 
            {".z",  "application/x-compress"}, 
            {".zip",    "application/x-zip-compressed"}, 
            {"",        "*/*"}   
        }; 
	/**
	  * 根据文件后缀名获得对应的MIME类型。
	 * @param file
	  */ 
	 public static String getMIMEType(File file) { 
	      
	     String type="*/*"; 
	     String fName = file.getName(); 
	     //获取后缀名前的分隔符"."在fName中的位置。 
	     int dotIndex = fName.lastIndexOf("."); 
	     if(dotIndex < 0){ 
	         return type; 
	     } 
	     /* 获取文件的后缀名*/ 
	     String end=fName.substring(dotIndex,fName.length()).toLowerCase(); 
	     if(end=="")return type; 
	     //在MIME和文件类型的匹配表中找到对应的MIME类型。 
	     for(int i=0;i<MIME_MapTable.length;i++){ //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？ 
	         if(end.equals(MIME_MapTable[i][0])) 
	             type = MIME_MapTable[i][1]; 
	     }        
	     return type; 
	 } 

}
