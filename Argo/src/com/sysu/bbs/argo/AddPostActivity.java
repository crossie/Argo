package com.sysu.bbs.argo;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;

import com.sysu.bbs.argo.util.PostService;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.SessionManager.LoginSuccessListener;
import com.sysu.bbs.argo.view.LoginDialog;
import com.sysu.bbs.argo.view.LoginDialog.Communicator;

public class AddPostActivity extends SwipeBackActivity implements
		LoginSuccessListener, Communicator {

	EditText mEditTitle;
	EditText mEditContent;
	Bundle mNewPostBundle;

	String quote = "<br/><font color=\"#888888\">【 在 %s (%s) 的大作中提到: 】<br/>%s</font>";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_new_post);
		
		getSwipeBackLayout().setEdgeSize(getWindowManager().getDefaultDisplay().getWidth());

		mEditTitle = (EditText) findViewById(R.id.new_post_title);
		mEditContent = (EditText) findViewById(R.id.new_post_content);
		Intent intent = getIntent();

		mNewPostBundle = intent.getExtras();

		if (mNewPostBundle.getString("type").equals("reply")) {
			if (!mNewPostBundle.getString("title").startsWith("Re: "))
				mEditTitle.setText("Re: " + mNewPostBundle.getString("title"));
			else
				mEditTitle.setText(mNewPostBundle.getString("title"));

			String tmp = mNewPostBundle.getString("content");
			tmp = tmp.substring(0, Math.min(100, tmp.length()));
			tmp = tmp.replaceAll("(?m)^", ": ");

			quote = ""
					+ String.format(quote, mNewPostBundle.getString("userid"),
							mNewPostBundle.getString("username"), tmp);

			mEditContent.setText(Html.fromHtml(quote));
		}
	}

	public void onClick(View v) {
		if (SessionManager.isLoggedIn) {
			sendPost();
		} else {

			LoginDialog loginDialog = new LoginDialog();
			loginDialog.show(getSupportFragmentManager(), "loginDialog");

		}
	}

	@Override
	public void actionAfterLogin(String userid) {
		SessionManager.loginSuccessListeners.remove(this);
		sendPost();

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
	public void passParam(String username, String password, boolean saveUser,
			boolean savePassword) {
		SessionManager sm = new SessionManager(this, username, password,
				saveUser, savePassword);
		SessionManager.loginSuccessListeners.add(this);
		sm.login();

	}

}
