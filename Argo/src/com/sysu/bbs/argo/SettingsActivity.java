package com.sysu.bbs.argo;

import android.app.Activity;
import android.os.Bundle;

import com.sysu.bbs.argo.view.SettingsFragment;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_setting);
		getFragmentManager().beginTransaction().add(R.id.activity_setting, new SettingsFragment()).commit();
		super.onCreate(savedInstanceState);
	}
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.close_enter_slide_in, R.anim.close_exit_slide_out);
	}
}
