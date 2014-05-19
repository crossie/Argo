package com.sysu.bbs.argo;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sysu.bbs.argo.adapter.FavGridAdapter;
import com.sysu.bbs.argo.adapter.PostAdapter;
import com.sysu.bbs.argo.adapter.PostHeadAdapter;
import com.sysu.bbs.argo.adapter.Top10Adapter;
import com.sysu.bbs.argo.api.API;
import com.sysu.bbs.argo.api.dao.Board;
import com.sysu.bbs.argo.api.dao.Post;
import com.sysu.bbs.argo.api.dao.PostHead;
import com.sysu.bbs.argo.api.dao.Top10;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.SimpleErrorListener;
import com.sysu.bbs.argo.util.SessionManager.LoginSuccessListener;
import com.sysu.bbs.argo.util.StringRequestPost;
import com.sysu.bbs.argo.view.LoginDialog;
import com.sysu.bbs.argo.view.LoginDialog.Communicator;
import com.sysu.bbs.argo.view.LoginPost;

public class MainActivity extends FragmentActivity implements
		OnItemClickListener, LoginSuccessListener, Communicator,
		OnScrollListener {

	RequestQueue requestQueue;

	DrawerLayout rightDrawer;
	SlidingPaneLayout lefSlidingPane;

	ListView detailView = null;
	Top10Adapter top10Adapter;
	ArrayList<Top10> top10List;

	GridView favGrid;
	FavGridAdapter favAdapter;
	ArrayList<Board> favList;

	PostAdapter postAdapter;
	ArrayList<Post> postList;
	ArrayList<PostHead> postHeadListNormal;

	PostHeadAdapter postHeadAdapter;
	ArrayList<PostHead> postHeadListTopic;

	ArrayList<PostHead> currPostHeadList;
	ArrayAdapter<?> currAdapter;

	String currMode = "normal";
	String currBoard;
	int firstIndexNormal, lastIndexNormal;
	int firstIndexTopic, lastIndexTopic;
	int currFirstIndex, currLastIndex;

	// TODO can auto load when approaching top or bottom ?
	FrameLayout headerWrapper, footerWrapper;
	View header, footer, empty;

	// boolean isLoginPreInitialized = false;
	boolean isLoginPostInitialized = false;
	// boolean isFavInitialized = false;
	// boolean isSectionInitialized = false;

	LoginDialog loginPre;

	LoginPost loginPost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		requestQueue = Volley.newRequestQueue(this);

		if (!SessionManager.loginSuccessListeners.contains(this))
			SessionManager.loginSuccessListeners.add(this);

		loginPre = new LoginDialog();
		loginPost = new LoginPost();

		rightDrawer = (DrawerLayout) findViewById(R.id.main_layout);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.right_drawer, loginPre, "LoginPre");
		ft.commit();

		lefSlidingPane = (SlidingPaneLayout) findViewById(R.id.left_sliding_pane);

		headerWrapper = new FrameLayout(this);
		footerWrapper = new FrameLayout(this);
		empty = findViewById(R.id.pb_for_empty_list);

		detailView = (ListView) findViewById(R.id.detail_list);
		detailView.setOnItemClickListener(this);
		// registerForContextMenu(detailView);

		header = getLayoutInflater().inflate(R.layout.view_detail_header, null);
		footer = getLayoutInflater().inflate(R.layout.view_detail_footer, null);

		// headerWrapper.addView(header);
		// footerWrapper.addView(footer);
		detailView.addHeaderView(headerWrapper);
		detailView.addFooterView(footerWrapper);
		// detailView.setEmptyView(empty);
		detailView.setOnScrollListener(this);

		top10List = new ArrayList<Top10>();
		top10Adapter = new Top10Adapter(this, R.id.name_cn, top10List);

		postList = new ArrayList<Post>();
		postHeadListNormal = new ArrayList<PostHead>();
		postAdapter = new PostAdapter(this, R.id.name_cn, postList);

		postHeadListTopic = new ArrayList<PostHead>();
		postHeadAdapter = new PostHeadAdapter(this, R.id.name_cn,
				postHeadListTopic);

		changeBoard("top10");
	}

	public void onSwitch(View view) {
		switch (view.getId()) {
		case R.id.mail:
			changeBoard("mail");
			break;
		case R.id.top10:
			changeBoard("top10");
			break;
		}
	}

	public void handyButtonClick(View view) {
		switch (view.getId()) {
		case R.id.new_post:
			Intent intent = new Intent(this, AddPostActivity.class);
			Bundle param = new Bundle();
			param.putString("type", "new");
			param.putString("boardname", currBoard);

			intent.putExtras(param);

			startActivity(intent);
			break;
		case R.id.refresh:

			String url = API.GET.AJAX_POST_LIST + "?";
			url += "type=" + currMode + "&boardname=" + currBoard + "&start="
					+ (currLastIndex + 1);
			// TODO use lastIndexNormal or firstIndexNormal based on bottom or
			// top
			footerWrapper.addView(new ProgressBar(this, null,
					android.R.attr.progressBarStyleSmall));
			requestQueue.add(new StringRequest(Method.GET, url,
					new Listener<String>() {

						@Override
						public void onResponse(String response) {
							postResponse(response, 2);
							footerWrapper.removeAllViews();
						}
					}, new SimpleErrorListener(this) {
						@Override
						public void onErrorResponse(VolleyError error) {
							footerWrapper.removeAllViews();
							super.onErrorResponse(error);
						}
					}));
			break;

		}
	}

	public void initFav() {
		favGrid = (GridView) findViewById(R.id.fav_grid);
		favList = new ArrayList<Board>();
		favAdapter = new FavGridAdapter(this, R.id.name_cn, favList);
		favGrid.setAdapter(favAdapter);
		favGrid.setEmptyView(findViewById(R.id.pb_for_empty_list));
		favGrid.setOnItemClickListener(this);

		boolean isFavInDatabase = false;
		if (isFavInDatabase) {
			// TODO: get favorites from database
		} else {
			requestQueue.add(new StringRequest(Method.GET,
					API.GET.AJAX_USER_FAV, new Listener<String>() {

						@Override
						public void onResponse(String response) {
							favResponse(response);
						}

					}, new SimpleErrorListener(this)));
		}
	}

	public void changeBoard(final String board) {

		currBoard = board;

		if (board.equals("top10")) {
			headerWrapper.removeAllViews();
			footerWrapper.removeAllViews();
			requestQueue.add(new StringRequestPost(API.GET.AJAX_COMM_TOPTEN,
					new Listener<String>() {

						@Override
						public void onResponse(String response) {

							top10Response(response);

						}

					}, new SimpleErrorListener(this), null));

		} else if (board.equals("mail")) {

		} else {
			getActionBar().setTitle(board);

			String url = API.GET.AJAX_POST_LIST + "?";
			url += "type=normal&boardname=" + board;

			if (detailView.getAdapter() != postAdapter) {
				detailView.setAdapter(postAdapter);
			}
			postList.clear();
			postAdapter.notifyDataSetChanged();

			currPostHeadList = postHeadListNormal;
			currFirstIndex = firstIndexNormal;
			currLastIndex = lastIndexNormal;

			postHeadListNormal.clear();
			postHeadListTopic.clear();

			sendGetPostRequest(url);

		}
	}

	private void sendGetPostRequest(String url) {
		headerWrapper.addView(new ProgressBar(this, null,
				android.R.attr.progressBarStyleSmall));

		requestQueue.add(new StringRequest(Method.GET, url,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {

						postResponse(response, 3);
					}
				}, new SimpleErrorListener(this) {
					@Override
					public void onErrorResponse(VolleyError error) {
						headerWrapper.removeAllViews();
						super.onErrorResponse(error);
					}
				}));
	}

	public void postResponse(String response, int where) {

		try {
			JSONObject result = new JSONObject(response);
			if (result.getString("success").equals("1")) {
				JSONArray postHeadArray = result.getJSONArray("data");
				if (where >= 2) { // refresh at bottom or from change board
					for (int index = 0; index < postHeadArray.length(); index++) {
						PostHead postHead = new PostHead(
								postHeadArray.getJSONObject(index));
						postHead.setBoardname(currBoard);
						currPostHeadList.add(postHead);
					}
				} else if (where == 1) { // at top
					for (int index = postHeadArray.length() - 1; index >= 0; index--) {
						PostHead postHead = new PostHead(
								postHeadArray.getJSONObject(index));
						postHead.setBoardname(currBoard);
						currPostHeadList.add(0, postHead);
					}
				}
				// TODO update firstIndexNormal and lastIndexNormal
				int count = currPostHeadList.size();
				switch (where) {
				case 1:
					currFirstIndex = currPostHeadList.get(0).getIndex();
					if (currMode.equals("normal")) {
						firstIndexNormal = currFirstIndex;
						loadPost(0, postHeadArray.length(), where);
					} else if (currMode.equals("topic")) {
						firstIndexTopic = currFirstIndex;
						headerWrapper.removeAllViews();
						headerWrapper.addView(header);
						postHeadAdapter.notifyDataSetChanged();
					}
					break;
				case 2:

					currLastIndex = currPostHeadList.get(count - 1).getIndex();
					if (currMode.equals("normal")) {
						lastIndexNormal = currLastIndex;
						loadPost(count - postHeadArray.length(),
								postHeadArray.length(), where);
					} else if (currMode.equals("topic")) {
						lastIndexTopic = currLastIndex;
						headerWrapper.removeAllViews();
						headerWrapper.addView(header);
						postHeadAdapter.notifyDataSetChanged();
					}
					break;
				case 3:
					currFirstIndex = currPostHeadList.get(0).getIndex();
					currLastIndex = currPostHeadList.get(count - 1).getIndex();

					firstIndexNormal = currFirstIndex;
					lastIndexNormal = currFirstIndex;
					if (currMode.equals("normal")) {
						loadPost(0, postHeadArray.length(), where);
					} else if (currMode.equals("topic")) {
						headerWrapper.removeAllViews();
						headerWrapper.addView(header);
						postHeadAdapter.notifyDataSetChanged();
					}
					break;
				default:
					break;
				}

			} else {
				Toast.makeText(this,
						"failed to get post, " + result.getString("error"),
						Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			Toast.makeText(this, "unexpected error in getting post",
					Toast.LENGTH_LONG).show();
		}
	}

	private void loadPost(final int curr, final int left, final int where) {
		if (left == 0) {
			headerWrapper.removeAllViews();
			headerWrapper.addView(header);
			postAdapter.notifyDataSetChanged();
			// detailView.smoothScrollToPositionFromTop(0, 72, 200);

			return;
		}
		// TODO should change to get from head or tail
		PostHead postHead = postHeadListNormal.get(curr);
		String url = API.GET.AJAX_POST_GET + "?boardname="
				+ postHead.getBoardname() + "&filename="
				+ postHead.getFilename();

		requestQueue.add(new StringRequest(Method.GET, url,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						try {
							JSONObject res = new JSONObject(response);
							if (res.getString("success").equals("1")) {
								JSONObject postObject = res
										.getJSONObject("data");
								Post post = new Post(postObject);
								if (where > 2) // refresh at bottom or from
												// changeBoard
									postList.add(post);
								else
									postList.add(0, post);

								// TODO enhance this !! don't call so many
								// times !!
								// postAdapter.notifyDataSetChanged();

								loadPost(curr + 1, left - 1, where);
							} else {
								Toast.makeText(
										MainActivity.this,
										"failed to get post, "
												+ res.getString("error"),
										Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							Toast.makeText(MainActivity.this,
									"unexpected error in getting post",
									Toast.LENGTH_LONG).show();
						}

					}
					// TODO how to cope with firstIndex and lastIndex ?
				}, new SimpleErrorListener(this)));

	}

	public void top10Response(String response) {
		detailView.setAdapter(top10Adapter);
		try {
			JSONObject result = new JSONObject(response);
			if (result.getString("success").equals("1")) {
				JSONArray top10Array = result.getJSONArray("data");
				top10List.clear();
				for (int i = 0; i < top10Array.length(); i++) {
					Top10 top10 = new Top10(top10Array.getJSONObject(i));
					top10List.add(top10);
				}
				top10Adapter.notifyDataSetChanged();
				getActionBar().setTitle("今日十大");
			} else {
				Toast.makeText(this,
						"failed to get top 10, " + result.getString("error"),
						Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			Toast.makeText(this, "unexpected error in getting top 10",
					Toast.LENGTH_LONG).show();
		}
	}

	public void favResponse(String response) {
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

	@Override
	public void onItemClick(AdapterView<?> view, View item, int pos, long row) {
		if (view == favGrid) {
			lefSlidingPane.closePane();
			Board board = favList.get(pos);
			changeBoard(board.getBoardname());
		} else if (view == detailView) {
			view.showContextMenuForChild(item);
		}
	}

	public RequestQueue getRequestQueue() {
		return requestQueue;
	}

	public void setRequestQueue(RequestQueue requestQueue) {
		this.requestQueue = requestQueue;
	}

	@Override
	public void actionAfterLogin() {
		initFav();
		if (rightDrawer.isDrawerOpen(Gravity.END))
			rightDrawer.closeDrawers();
		if (!isLoginPostInitialized) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.right_drawer, loginPost, "LoginPost");
			ft.commitAllowingStateLoss();

			isLoginPostInitialized = true;

		}

	}

	@Override
	public void passParam(String username, String password, boolean saveUser,
			boolean savePassword) {
		SessionManager sm = new SessionManager(this, username, password,
				saveUser, savePassword);
		sm.login();

	}

	@Override
	public void onScroll(AbsListView list, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem + visibleItemCount < totalItemCount
				|| currBoard == null || currBoard.equals("top10")
				|| currBoard.equals("mail"))
			return;

		// TODO add footer
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public void loadEarlier(View v) {
		String url = API.GET.AJAX_POST_LIST + "?";
		url += "type=" + currMode + "&boardname=" + currBoard + "&start="
				+ (currFirstIndex - 20);

		headerWrapper.removeAllViews();
		headerWrapper.addView(new ProgressBar(this, null,
				android.R.attr.progressBarStyleSmall));
		requestQueue.add(new StringRequest(Method.GET, url,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						postResponse(response, 1);

					}
				}, new SimpleErrorListener(this)));

	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.post_popup, menu);
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * getMenuInflater().inflate(R.menu.main, menu); return true; }
	 * 
	 * @Override public boolean onPrepareOptionsMenu(Menu menu) { //
	 * getMenuInflater().inflate(R.menu.main, menu); return true; }
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { switch
	 * (item.getItemId()) { case R.id.view_mode_normal: if
	 * (!currMode.equals("normal")) { currMode = "normal"; currPostHeadList =
	 * postHeadListNormal; currFirstIndex = firstIndexNormal; currLastIndex =
	 * lastIndexNormal; detailView.setAdapter(postAdapter);
	 * 
	 * } break; case R.id.view_mode_topic: if (!currMode.equals("topic")) {
	 * currMode = "topic"; currPostHeadList = postHeadListTopic; currFirstIndex
	 * = firstIndexTopic; currLastIndex = lastIndexTopic;
	 * detailView.setAdapter(postHeadAdapter);
	 * 
	 * if (currPostHeadList.size() == 0) { String url = API.GET.AJAX_POST_LIST +
	 * "?"; url += "type=topic&boardname=" + currBoard;
	 * headerWrapper.removeAllViews(); sendGetPostRequest(url); } } break; }
	 * return true; }
	 */
}
