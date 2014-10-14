package com.sysu.bbs.argo.view;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sysu.bbs.argo.DraftActivity;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.SettingsActivity;
import com.sysu.bbs.argo.util.SessionManager;

public class RightMenuFragment extends Fragment implements OnItemClickListener {

	private ListView mRightListView = null;
	private ArrayList<String> mList = new ArrayList<String>();
	private ArrayAdapter<String> mAdapter;
	private TextView mUserid;

	private ProgressDialog mLogoutProgressDialog;
	private ProgressDialog mCheckUpdateProgressDialog;

	private BroadcastReceiver mSessionStatusReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frag_right_menu, container, false);
		mRightListView = (ListView)v.findViewById(R.id.right_list);
		mRightListView.setOnItemClickListener(this);
		mUserid = (TextView) v.findViewById(R.id.right_drawer_userid);

		mList.addAll(Arrays.asList(getResources().getStringArray(
				R.array.right_menu_items)));
		mAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, mList);
		mRightListView.setAdapter(mAdapter);

		mSessionStatusReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context con, Intent intent) {
				String action = intent.getAction();
				if (action.equals(SessionManager.BROADCAST_LOGIN)) {
					String userid = intent.getStringExtra("userid");
					if (userid != null && !userid.equals("")) {
						mList.remove(0);
						mList.add(0, "注销");
						mAdapter.notifyDataSetChanged();
						mUserid.setText(userid);
					}
				} else if (action.equals(SessionManager.BROADCAST_LOGOUT)) {
					if (intent.getBooleanExtra("success", false)) {
						mList.remove(0);
						mList.add(0, "登录");
						mAdapter.notifyDataSetChanged();
						mUserid.setText("未登录");
					}
					if (mLogoutProgressDialog != null)
						mLogoutProgressDialog.dismiss();
				}

			}
		};

		return v;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (SessionManager.isLoggedIn) {
			mList.remove(0);
			mList.add(0, "注销");
			mAdapter.notifyDataSetChanged();
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			mUserid.setText(sp.getString("userid", ""));
		}

		super.onActivityCreated(savedInstanceState);
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SessionManager.BROADCAST_LOGIN);  
		intentFilter.addAction(SessionManager.BROADCAST_LOGOUT);  
		try {
			getActivity().registerReceiver(mSessionStatusReceiver, intentFilter);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		try {
			getActivity().unregisterReceiver(mSessionStatusReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
		switch (position) {
		case 2:
			startActivity(new Intent(getActivity(), SettingsActivity.class));
			break;
		case 4:
			AlertDialog.Builder builder = new AlertDialog.Builder(
					getActivity(), AlertDialog.THEME_HOLO_DARK);

			PackageManager manager = getActivity().getPackageManager();
			PackageInfo info = null;
			try {
				info = manager
						.getPackageInfo(getActivity().getPackageName(), 0);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String version = info == null ? "unknown version"
					: info.versionCode + ", " + info.versionName;
			TextView message = (TextView) builder
					.setMessage(
							Html.fromHtml("Argo "
									+ version
									+ "<br/>Author:<br/>&nbsp;&nbsp;站内ID scim,<br/>"
									+ "&nbsp;&nbsp;微博 <a href=\"http://weibo.com/crossie\">@输了就强退</a><br/>"))
					.setTitle("关于").show().findViewById(android.R.id.message);
			message.setMovementMethod(LinkMovementMethod.getInstance());
			;
			break;
		case 3:
			mCheckUpdateProgressDialog = new ProgressDialog(getActivity());
			mCheckUpdateProgressDialog.setMessage("检查更新中...");
			mCheckUpdateProgressDialog.setCancelable(false);
			mCheckUpdateProgressDialog
					.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mCheckUpdateProgressDialog.show();
			String requestUrl = "https://raw.githubusercontent.com/crossie/argorel/master/check_update.html";			
			JsonObjectRequest checkUpdateRequest =	new JsonObjectRequest(requestUrl, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(final JSONObject obj) {

						try {
							PackageInfo pi = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
							if (obj.getInt("versionCode") > pi.versionCode){
								final String url = obj.getString("url");
								new AlertDialog.Builder(getActivity(),AlertDialog.THEME_HOLO_DARK)   
								.setTitle("发现新版本")  
								.setMessage(obj.getString("description"))  
								.setPositiveButton("下载", new OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent intent = new Intent();        
										intent.setAction("android.intent.action.VIEW");    
										Uri content_url = Uri.parse(url);
										intent.setData(content_url);  
										startActivity(intent);
									}
								})  
								.setNegativeButton("取消", null)  
								.show();
							} else {
								Toast.makeText(getActivity(), "暂时没更新",
									     Toast.LENGTH_SHORT).show();
							}
						} catch (NameNotFoundException e1) {
							Toast.makeText(getActivity(), "系统版本错误",
								     Toast.LENGTH_SHORT).show();
						} catch (JSONException e) {
							Toast.makeText(getActivity(), "获取更新数据错误",
								     Toast.LENGTH_SHORT).show();
						} finally {
							if (mCheckUpdateProgressDialog != null)
								mCheckUpdateProgressDialog.dismiss();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						if (mCheckUpdateProgressDialog != null)
							mCheckUpdateProgressDialog.dismiss();
						Toast.makeText(getActivity(), "网络错误,请稍后再试",
							     Toast.LENGTH_SHORT).show();
					}
				}
			);
			checkUpdateRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 2, 2));
			SessionManager.getRequestQueue().add(checkUpdateRequest);
			break;
		case 1:
			Intent intent = new Intent(getActivity(), DraftActivity.class);
			startActivity(intent);
			break;
		case 0:
			if (SessionManager.isLoggedIn) {
				mLogoutProgressDialog = new ProgressDialog(getActivity());
				mLogoutProgressDialog.setMessage("注销中...");
				mLogoutProgressDialog.setCancelable(false);
				mLogoutProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mLogoutProgressDialog.show();

				SessionManager.logout();
			} else {
				LoginDialog loginDialog = new LoginDialog();
				loginDialog.show(getFragmentManager(), "loginDialog");
			}
			break;
		}
	}

}
