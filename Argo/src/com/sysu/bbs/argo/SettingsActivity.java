package com.sysu.bbs.argo;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.os.Bundle;

import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.view.SettingsFragment;

public class SettingsActivity extends SwipeBackActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_setting);
		getFragmentManager().beginTransaction().add(R.id.activity_setting, new SettingsFragment()).commit();
		super.onCreate(savedInstanceState);
	}
}
