package com.sysu.bbs.argo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
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
		if (!draftDir.exists())
			draftDir.mkdir();
		mDraftList.addAll(Arrays.asList(draftDir.listFiles()));
		mAdapter.notifyDataSetChanged();

		mHandler = new Handler();
		mDraftObserver = new DraftObserver(draftDir.getAbsolutePath());
		mDraftObserver.startWatching();
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

}
