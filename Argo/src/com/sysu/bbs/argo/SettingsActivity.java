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
		
		//实现退出时的动画,不明白为什么要这样写才行
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
	 * 用于设置退出动画
	 */
	protected int activityCloseEnterAnimation;
	/**
	 * 同上用于设置退出动画
	 */
	protected int activityCloseExitAnimation;
	/**
	 * 实现退出动画，未知为何要这样写才会有动画
	 */
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
	}
}
