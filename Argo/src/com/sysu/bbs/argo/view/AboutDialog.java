package com.sysu.bbs.argo.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AboutDialog extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);

		PackageManager manager = getActivity().getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String version = info == null ? "unknown version" : info.versionCode + ", " + info.versionName;
		builder.setMessage("Argo " + version + "\nAuthor:\n      站内ID scim,\n     微博 @输了就强退").setTitle("关于");
		builder.setPositiveButton("关闭", new OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dismiss();
			}
			
		});
		return builder.create();
	}
}
