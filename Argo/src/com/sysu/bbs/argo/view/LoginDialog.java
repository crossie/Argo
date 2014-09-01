package com.sysu.bbs.argo.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.util.SessionManager;

public class LoginDialog extends DialogFragment implements OnClickListener {
	private EditText mUsername;
	private EditText mPassword;
	private CheckBox mSaveUsername;
	private CheckBox mSavePassword;

	private Button mLoginButton;
	private ProgressDialog mLoginProgressDialog;

	private BroadcastReceiver mLoginReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.login_pre, container, false);
		mUsername = (EditText) view.findViewById(R.id.user_name);
		mPassword = (EditText) view.findViewById(R.id.password);
		mSaveUsername = (CheckBox) view.findViewById(R.id.save_user_name);
		mSavePassword = (CheckBox) view.findViewById(R.id.save_password);

		mLoginButton = (Button) view.findViewById(R.id.btn_login);
		mLoginButton.setOnClickListener(this);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		mUsername.setText(sp.getString("userid", ""));
		mPassword.setText(sp.getString("password", ""));

		mLoginReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context con, Intent intent) {
				String action = intent.getAction();
				if (action.equals(SessionManager.BROADCAST_LOGIN)) {
					String userid = intent.getStringExtra("userid");
					if (userid != null && !userid.equals("")) {
						mLoginProgressDialog.dismiss();

						boolean isSaveUsername = mSaveUsername.isChecked();
						boolean isSavePassword = mSavePassword.isChecked();
						SharedPreferences sp = PreferenceManager
								.getDefaultSharedPreferences(getActivity());
						Editor editor = sp.edit();
						if (isSaveUsername) {
							editor.putString("userid", mUsername.getText().toString());

							if (isSavePassword) {
								editor.putString("password", mPassword.getText().toString());
							} else {
								editor.putString("password", "");
							}
							editor.commit();
						} else {
							editor.putString("userid", "");
						}
						dismiss();
					} else {
						mLoginProgressDialog.dismiss();
					}
				}

			}

		};
		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);

		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}

	@Override
	public void onClick(View v) {
		String username = mUsername.getText().toString();
		String password = mPassword.getText().toString();
		if (username == null || password == null || username.equals("")
				|| password.equals("")) {
			Toast.makeText(getActivity(), "用户名和密码不能为空", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		mLoginProgressDialog = new ProgressDialog(getActivity());
		mLoginProgressDialog.setMessage("登录中...");
		mLoginProgressDialog.setCancelable(false);
		mLoginProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mLoginProgressDialog.show();

		SessionManager sm = new SessionManager(getActivity(), username,
				password);
		sm.login();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SessionManager.BROADCAST_LOGIN);  
		getActivity().registerReceiver(mLoginReceiver, intentFilter);
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		getActivity().unregisterReceiver(mLoginReceiver);
	}

}
