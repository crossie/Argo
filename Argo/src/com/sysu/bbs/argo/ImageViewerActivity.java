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

			File savedFile;
			@Override
			protected Boolean doInBackground(File... files) {
				File input = files[0];
				savedFile = files[1];
				BufferedInputStream bis = null;
				BufferedOutputStream bos = null;
				byte[] tmp = new byte[1000];
				try {
					bis = new BufferedInputStream(new FileInputStream(input));
					bos = new BufferedOutputStream(new FileOutputStream(savedFile));
					while (bis.read(tmp) > 0)
						bos.write(tmp);					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					return Boolean.valueOf(false);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					return Boolean.valueOf(false);
				} finally {
					try {
						if (bis != null)
							bis.close();
						if (bos != null)
							bos.close();
					} catch (IOException e) {
						return Boolean.valueOf(false);
					}
						
				}
				return Boolean.valueOf(true);
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				if (result.booleanValue())
					Toast.makeText(ImageViewerActivity.this, "已保存至 " + savedFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(ImageViewerActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
			}
			
		};
		if (!cache.exists()) 
			Toast.makeText(ImageViewerActivity.this, "图片未加载完成，不能保存", Toast.LENGTH_SHORT).show();
		else if (!cache.canRead())
			Toast.makeText(ImageViewerActivity.this, "读取缓存文件出错", Toast.LENGTH_SHORT).show();
		else
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

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.close_enter_slide_in, R.anim.close_exit_slide_out);
	}
}
