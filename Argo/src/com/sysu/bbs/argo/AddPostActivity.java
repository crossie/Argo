package com.sysu.bbs.argo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.View;
import android.widget.EditText;

import com.sysu.bbs.argo.util.PostService;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.SessionManager.LoginSuccessListener;
import com.sysu.bbs.argo.view.LoginDialog;
import com.sysu.bbs.argo.view.LoginDialog.Communicator;

public class AddPostActivity extends FragmentActivity implements
		LoginSuccessListener, Communicator {

	EditText editTitle;
	EditText editContent;
	Bundle newPostbundle;

	String quote = "<br/><font color=\"#888888\">【 在 %s (%s) 的大作中提到: 】<br/>%s</font>";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_new_post);

		editTitle = (EditText) findViewById(R.id.new_post_title);
		editContent = (EditText) findViewById(R.id.new_post_content);
		Intent intent = getIntent();

		newPostbundle = intent.getExtras();

		if (newPostbundle.getString("type").equals("reply")) {
			if (!newPostbundle.getString("title").startsWith("Re: "))
				editTitle.setText("Re: " + newPostbundle.getString("title"));
			else
				editTitle.setText(newPostbundle.getString("title"));

			String tmp = newPostbundle.getString("content");
			tmp = tmp.substring(0, Math.min(100, tmp.length()));
			tmp = tmp.replaceAll("(?m)^", ": ");

			quote = ""
					+ String.format(quote, newPostbundle.getString("userid"),
							newPostbundle.getString("username"), tmp);

			editContent.setText(Html.fromHtml(quote));
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
	public void actionAfterLogin() {
		SessionManager.loginSuccessListeners.remove(this);
		sendPost();

	}

	public void sendPost() {
		Intent service = new Intent(this, PostService.class);

		newPostbundle.putString("title", editTitle.getText().toString());
		newPostbundle.putString("content", editContent.getText().toString());
		service.putExtras(newPostbundle);

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
