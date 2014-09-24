package com.sysu.bbs.argo.view;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
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
		case 3:
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
		case 4:
			final ProgressBar loading = (ProgressBar)getActivity().findViewById(R.id.checkUpdateLoading);
			loading.setVisibility(View.VISIBLE);
			String requestUrl = "https://raw.githubusercontent.com/bao050400287/argorel/master/check_update.html";
			SessionManager.getRequestQueue().add(new JsonObjectRequest(requestUrl, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(final JSONObject obj) {
						loading.setVisibility(View.GONE);
						try {
							PackageInfo pi = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
							if (obj.getInt("versionCode") > pi.versionCode){
								new AlertDialog.Builder(getActivity())   
								.setTitle("发现新版本")  
								.setMessage("是否更新到最新版本？")  
								.setPositiveButton("是", new OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										Intent intent = new Intent();        
										intent.setAction("android.intent.action.VIEW");    
										try {
											Uri content_url = Uri.parse(obj.getString("url"));
											intent.setData(content_url);  
											startActivity(intent);
										} catch (JSONException e) {
											Toast.makeText(getActivity(), "更新链接错误",
												     Toast.LENGTH_SHORT).show();
										}   
									}
								})  
								.setNegativeButton("否", null)  
								.show();
							}
						} catch (NameNotFoundException e1) {
							loading.setVisibility(View.GONE);
							Toast.makeText(getActivity(), "系统版本错误",
								     Toast.LENGTH_SHORT).show();
						} catch (JSONException e) {
							loading.setVisibility(View.GONE);
							Toast.makeText(getActivity(), "获取更新数据错误",
								     Toast.LENGTH_SHORT).show();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						loading.setVisibility(View.GONE);
						Toast.makeText(getActivity(), "网络错误,请稍后再试",
							     Toast.LENGTH_SHORT).show();
					}
				}
			));
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
