package com.sysu.bbs.argo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.widget.ListView;

import com.sysu.bbs.argo.adapter.DraftAdapter;

public class DraftActivity extends SwipeBackActivity {

	private ListView mDraftListView;
	private DraftAdapter mAdapter;
	private ArrayList<File> mDraftList;
	private FileObserver mDraftObserver;
	private String mDraftPath;
	/**
	 * used to post runnable to main thread
	 */
	private Handler mHandler;

	// private HashMap<File, Post> mDraftMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draft);
		/*getSwipeBackLayout().setEdgeSize(
				getWindowManager().getDefaultDisplay().getWidth());*/
		mDraftListView = (ListView) findViewById(R.id.draft_list);
		mDraftListView.setEmptyView(findViewById(R.id.draft_empty));

		// mDraftList = new ArrayList<File>();

		mDraftList = new ArrayList<File>();
		mAdapter = new DraftAdapter(this, android.R.layout.simple_list_item_1,
				mDraftList);
		mDraftListView.setAdapter(mAdapter);
		mDraftListView.setOnItemClickListener(mAdapter);

		mDraftPath = getFilesDir().getAbsolutePath() + "/Draft";

		File draftDir = new File(mDraftPath);
		mHandler = new Handler();
		mDraftObserver = new DraftObserver(draftDir.getAbsolutePath());
		mDraftObserver.startWatching();
		
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

	@Override
	protected void onResume() {
		mDraftList.clear();
		File draftDir = new File(mDraftPath);
		if (!draftDir.exists())
			draftDir.mkdir();
		mDraftList.addAll(Arrays.asList(draftDir.listFiles()));
		mAdapter.notifyDataSetChanged();
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		mDraftObserver.stopWatching();
		super.onDestroy();
	}

	private class DraftObserver extends FileObserver {

		public DraftObserver(String path) {
			super(path);
			// TODO Auto-generated constructor stub
		}

		/**
		 * react to draft file changes <br/>
		 * because this does not run on main thread, thus need to use handler to post runnables
		 */
		@Override
		public void onEvent(int event, final String path) {
			switch (event) {
			case FileObserver.CREATE:
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mDraftList.add(new File(mDraftPath + "/" + path));
						mAdapter.notifyDataSetChanged();
					}
				});

				break;
			case FileObserver.DELETE:
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mDraftList.remove(new File(mDraftPath + "/" + path));
						mAdapter.notifyDataSetChanged();
					}
				});

				break;

			}

		}

	}
	
	public static void add2Draft(File post, Bundle bundle) {
		FileOutputStream fos = null; 
		BufferedWriter bw = null; 
		
		try {
			fos = new FileOutputStream(post);
			bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			bw.write(bundle.getString("type") + "\n");
			bw.write(bundle.getString("boardname") + "\n");
			bw.write(bundle.getString("articleid") + "\n");
			bw.write(bundle.getString("title") + "\n");
			bw.write(System.currentTimeMillis() + "\n");
			bw.write(bundle.getString("attach"));
			bw.write(bundle.getString("content"));
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
