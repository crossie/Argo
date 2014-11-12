package com.sysu.bbs.argo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sysu.bbs.argo.adapter.pager.ImageViewerPagerAdapter;

public class ImageViewerActivity extends FragmentActivity implements OnPageChangeListener {

	public static final String IMAGE_LIST_KEY = "IMAGE_LIST_KEY";
	private ViewPager mImageViewPager;
	private TextView mIndicator;
	private ArrayList<String> mUrlList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_viewer);
		
		mImageViewPager = (ViewPager) findViewById(R.id.image_view_pager);
		mImageViewPager.setOffscreenPageLimit(5);
		mImageViewPager.setOnPageChangeListener(this);
		
		mUrlList = getIntent().getStringArrayListExtra(IMAGE_LIST_KEY);
		mImageViewPager.setAdapter(new ImageViewerPagerAdapter(getSupportFragmentManager(), mUrlList));
		
		mIndicator = (TextView) findViewById(R.id.indicator);
		mIndicator.setText(String.format("%d/%d", 1, mUrlList.size()));
		
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
	
	public void saveImage(View v) throws NoSuchAlgorithmException {
		if (v.getId() != R.id.save_image)
			return;
		String state = Environment.getExternalStorageState();
		if (!state.equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "外部存储不可用", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String url = mUrlList.get(mImageViewPager.getCurrentItem());
		
		File extStorage = Environment.getExternalStorageDirectory();
		File imageDir = new File(extStorage.getAbsoluteFile(), "Argo/savedImage/");
		imageDir.mkdirs();		
		String name = url.substring(url.lastIndexOf("/") + 1);
		File permanent = new File(imageDir, name + ".png");
		int index = 1;
		while (permanent.exists()) {
			index++;
			permanent = new File(imageDir, name + index + ".png");
		}
		
		String urlHash = new String(MessageDigest.getInstance("MD5")
				.digest(url.getBytes()));
		File cacheDir = new File(getFilesDir(), "Cache");
		File cache = new File(cacheDir, urlHash);
		
		AsyncTask<File, Void, Boolean> saveTask = new AsyncTask<File, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(File... files) {
				File input = files[0];
				File output = files[1];
				BufferedInputStream bis = null;
				BufferedOutputStream bos = null;
				byte[] tmp = new byte[1000];
				try {
					bis = new BufferedInputStream(new FileInputStream(input));
					bos = new BufferedOutputStream(new FileOutputStream(output));
					while (bis.read(tmp) > 0)
						bos.write(tmp);
					Toast.makeText(ImageViewerActivity.this, "已保存至 " + output.getAbsolutePath(), Toast.LENGTH_SHORT).show();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						if (bis != null)
							bis.close();
						if (bos != null)
							bos.close();
					} catch (IOException e) {
						
					}
						
				}
				return null;
			}
			
		};
		
		saveTask.execute(cache, permanent);
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int pos) {
		mIndicator.setText(String.format("%d/%d", pos + 1, mUrlList.size()));
		
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
