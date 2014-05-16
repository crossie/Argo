package com.sysu.bbs.argo;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.Volley;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Board;
import com.sysu.bbs.argo.api.dao.Top10;
import com.sysu.bbs.argo.util.StringRequestPost;
import com.sysu.bbs.argo.view.LoginPre;
import com.sysu.bbs.argo.view.Top10Fragment;

public class MainActivity extends FragmentActivity /*
													 * implements TabListener,
													 * OnPageChangeListener
													 */{

	RequestQueue requestQueue;

	DrawerLayout rightDrawer;
	SlidingPaneLayout lefSlidingPane;
	GridView favGrid;
	FavGridAdapter favAdapter;
	ArrayList<Board> favList = new ArrayList<Board>();
	
	Top10Fragment top10Frag;
	Top10Adapter top10Adapter;
	ArrayList<Top10> top10List = new ArrayList<Top10>();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		requestQueue = Volley.newRequestQueue(this);

		rightDrawer = (DrawerLayout) findViewById(R.id.main_layout);
		lefSlidingPane = (SlidingPaneLayout) findViewById(R.id.left_sliding_pane);

		boolean userNotFound = true;
		// TODO: get user name and password from db
		// TODO: if failed to get user name and password
		if (userNotFound) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.right_drawer, new LoginPre(), "LoginPre");
			ft.commit();

			rightDrawer.openDrawer(GravityCompat.END);
			rightDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN,
					GravityCompat.END);
		} else {
			// TODO: GET User name and password and login

			// TODO: if log in successfully, call init()
		}

	}

	public void login(View loginBtn) {
		final TextView tvUsername = (TextView) findViewById(R.id.user_name);
		final TextView tvPassword = (TextView) findViewById(R.id.password);
		if (tvUsername.getText() == null || tvPassword.getText() == null
				|| tvUsername.getText().toString().equals("")
				|| tvPassword.getText().toString().equals("")) {
			Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
			return;
		}

		HashMap<String, String> param = new HashMap<String, String>();
		param.put("userid", tvUsername.getText().toString());
		param.put("passwd", tvPassword.getText().toString());

		CookieManager cm = new CookieManager();
		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cm);

		requestQueue.add(new StringRequestPost(Method.POST,
				API.POST.AJAX_LOGIN, new Listener<String>() {
					@Override
					public void onResponse(String response) {
						loginResponse(response,
								tvUsername.getText().toString(), tvPassword
										.getText().toString());
					}

				}, null, param));

	}

	private void loginResponse(String response, String username, String password) {
		try {
			JSONObject loginResult = new JSONObject(response);

			if (loginResult != null
					&& loginResult.getString("success").equals("1")) {
				rightDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
						GravityCompat.END);
				rightDrawer.closeDrawer(GravityCompat.END);
				lefSlidingPane.openPane();

				CheckBox saveUserName = (CheckBox) findViewById(R.id.save_user_name);
				CheckBox savePassword = (CheckBox) findViewById(R.id.save_password);

				if (saveUserName.isChecked()) {
					// TODO: save user name to database
					if (savePassword.isChecked())
						;// TODO: save password to database
				}

				initFav();
				initDetailView();

			} else {
				Toast.makeText(this,
						"login failed, " + loginResult.getString("error"),
						Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			Toast.makeText(this, "unexpected error in login", Toast.LENGTH_LONG)
					.show();
		}
	}

	private void favResponse(String response) {
		try {
			JSONObject org = new JSONObject(response);
			if (org.getString("success").equals("1")) {
				JSONArray arr = org.getJSONArray("data");
				favList.clear();
				for (int i = 0; i < arr.length(); i++) {
					Board board = new Board(arr.getJSONObject(i));
					favList.add(board);
				}
				favAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(this,
						"failed to get favorites, " + org.getString("error"),
						Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			Toast.makeText(this, "unexpected error in getting favorites",
					Toast.LENGTH_LONG).show();
		}
	}

	private void top10Response(String response) {
		try {
			JSONObject result = new JSONObject(response);
			if (result.getString("success").equals("1")) {
				JSONArray top10 = result.getJSONArray("data");
				for (int i = 0; i < top10.length(); i++) {
					Log.d("test", top10.get(i).toString());
				}
			}
		} catch (JSONException e) {
			Toast.makeText(this, "unexpected error in getting top 10",
					Toast.LENGTH_LONG).show();
		}
	}
	private void initFav() {
		favGrid = (GridView) findViewById(R.id.fav_grid);
		favGrid.setEmptyView(findViewById(R.id.pb_for_empty_list));
		favAdapter = new FavGridAdapter(this, R.id.name_en, favList);
		favGrid.setAdapter(favAdapter);

		boolean isFavInDatabase = false;
		if (isFavInDatabase) {
			// TODO: get favorites from database
		} else {
			requestQueue.add(new StringRequestPost(Method.GET,
					API.GET.AJAX_USER_FAV, new Listener<String>() {

						@Override
						public void onResponse(String response) {
							favResponse(response);
						}

					}));
		}

		// TODO: get all boards from database or network
		// TODO: get top10 from database or network
		// TODO get mail from database or network
		// TODO: set drawer to LoginPost fragment
	}

	private void initDetailView() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		top10Frag = new Top10Fragment();
		//TODO: continue
		ft.add(R.id.view_detail, top10Frag, "top10");
		ft.commit();
		getActionBar().setTitle(
				getResources().getString(R.string.actionbar_title_top10));

		requestQueue.add(new StringRequestPost(Method.GET,
				API.GET.AJAX_COMM_TOPTEN, new Listener<String>() {

					@Override
					public void onResponse(String response) {
						top10Response(response);

					}

				}));
	}

	private class DetailAdapter extends FragmentPagerAdapter {

		public DetailAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int pos) {
			// return fragDetail[pos];
			return null;
		}

		@Override
		public int getCount() {
			// return tabs.length;
			return 0;
		}

	}

	private class FavGridAdapter extends ArrayAdapter<Board> {

		private class FavHolder {
			TextView nameEn;
			TextView nameCn;

			FavHolder(TextView en, TextView cn) {
				nameEn = en;
				nameCn = cn;
			}
		}

		public FavGridAdapter(Context context, int resource, List<Board> board) {

			super(context, resource, board);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View tmp = convertView;
			FavHolder holder = null;
			if (tmp == null) {
				tmp = LayoutInflater.from(this.getContext()).inflate(
						R.layout.item_favorite, parent, false);
				TextView nameEn = (TextView) tmp.findViewById(R.id.name_en);
				TextView nameCn = (TextView) tmp.findViewById(R.id.name_cn);
				holder = new FavHolder(nameEn, nameCn);
				tmp.setTag(holder);

			} else
				holder = (FavHolder) tmp.getTag();

			holder.nameEn.setText(getItem(position).getBoardname());
			holder.nameCn.setText(getItem(position).getTitle());

			return tmp;
		}

	}

	private class Top10Adapter extends ArrayAdapter<Top10> {

		public Top10Adapter(Context context, int resource,
				int textViewResourceId, List<Top10> objects) {
			super(context, resource, textViewResourceId, objects);
		}
		
	}
	/*
	 * @Override public void onTabReselected(Tab arg0, FragmentTransaction arg1)
	 * {
	 * 
	 * }
	 * 
	 * @Override public void onTabSelected(Tab tab, FragmentTransaction ft) { //
	 * vpDetail.setCurrentItem(tab.getPosition());
	 * 
	 * }
	 * 
	 * @Override public void onTabUnselected(Tab arg0, FragmentTransaction arg1)
	 * {
	 * 
	 * }
	 * 
	 * @Override public void onPageScrollStateChanged(int arg0) {
	 * 
	 * }
	 * 
	 * @Override public void onPageScrolled(int arg0, float arg1, int arg2) {
	 * 
	 * }
	 * 
	 * @Override public void onPageSelected(int pos) { //
	 * actionBar.setSelectedNavigationItem(pos);
	 * 
	 * }
	 */

}
