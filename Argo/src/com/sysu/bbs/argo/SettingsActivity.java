package com.sysu.bbs.argo;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.content.res.TypedArray;
import android.os.Bundle;

import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.view.SettingsFragment;

public class SettingsActivity extends SwipeBackActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_setting);
		getFragmentManager().beginTransaction().add(R.id.activity_setting, new SettingsFragment()).commit();
		super.onCreate(savedInstanceState);
		
		//ʵ���˳�ʱ�Ķ���,������ΪʲôҪ����д����
		TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowAnimationStyle});
		int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);      
		activityStyle.recycle();
		activityStyle = getTheme().obtainStyledAttributes(windowAnimationStyleResId, 
				new int[] {android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation});
		activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);
		activityCloseExitAnimation = activityStyle.getResourceId(1, 0);
		activityStyle.recycle();
	}
	
	/**
	 * ���������˳�����
	 */
	protected int activityCloseEnterAnimation;
	/**
	 * ͬ�����������˳�����
	 */
	protected int activityCloseExitAnimation;
	/**
	 * ʵ���˳�������δ֪Ϊ��Ҫ����д�Ż��ж���
	 */
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
	}
}
