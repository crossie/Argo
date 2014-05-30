package com.sysu.bbs.argo.view;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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

public class LoginDialog extends DialogFragment implements OnClickListener {
	EditText etUsername;
	EditText etPassword;
	CheckBox saveUsername;
	CheckBox savePassword;

	Button btn;

	Communicator comm;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.login_pre, container, false);
		etUsername = (EditText) view.findViewById(R.id.user_name);
		etPassword = (EditText) view.findViewById(R.id.password);
		saveUsername = (CheckBox) view.findViewById(R.id.save_user_name);
		savePassword = (CheckBox) view.findViewById(R.id.save_password);

		btn = (Button) view.findViewById(R.id.btn_login);
		btn.setOnClickListener(this);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		comm = (Communicator) getActivity();
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);

		// request a window without the title
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}

	@Override
	public void onClick(View v) {
		String username = etUsername.getText().toString();
		String password = etPassword.getText().toString();
		if (username == null || password == null || username.equals("")
				|| password.equals("")) {
			Toast.makeText(getActivity(), "用户名和密码不能为空", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		boolean isSaveUsername = saveUsername.isChecked();
		boolean isSavePassword = savePassword.isChecked();

		comm.passParam(username, password, isSaveUsername, isSavePassword);
	}

	public interface Communicator {
		public abstract void passParam(String username, String password,
				boolean saveUser, boolean savePassword);
	}
}
