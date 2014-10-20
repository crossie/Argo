package com.sysu.bbs.argo.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import uk.co.senab.photoview.PhotoViewAttacher;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.ImageRequest;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.util.SessionManager;

public class ImageViewerFragment extends Fragment {
	public static final String IMAGE_URL_KEY = "IMAGE_URL_KEY";
	private ImageView mImageView;
	private PhotoViewAttacher mAttacher;
	private ProgressBar mLoadingImagePB;
	private String mUrl, mUrlHash;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_image_viewer, container, false);
		mLoadingImagePB = (ProgressBar)view.findViewById(R.id.pb_loading_image);
		mImageView = (ImageView) view.findViewById(R.id.image_view);
		mAttacher = new PhotoViewAttacher(mImageView);
		Bundle bundle = getArguments();
		mUrl = bundle.getString(IMAGE_URL_KEY);
		try {
			mUrlHash = new String(MessageDigest.getInstance("MD5").digest(mUrl.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			
		}
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		//try to get image from cache
		File imageDir = new File(getActivity().getFilesDir(), "Cache");
		if (!imageDir.exists())
			imageDir.mkdir();
		final File cache = new File(imageDir, mUrlHash);
		if (cache.exists()) {//cache exists
			try {
				Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(cache));
				mImageView.setImageBitmap(bitmap);
				mLoadingImagePB.setVisibility(View.GONE);
				mAttacher.update();
			} catch (FileNotFoundException e) {

			}
			
		} else {//cache not exists, get through network
		
			ImageRequest getImage = new ImageRequest(mUrl, new Listener<Bitmap>() {
	
				@Override
				public void onResponse(Bitmap img) {
					mLoadingImagePB.setVisibility(View.GONE);
					mImageView.setImageBitmap(img);
					//save to cache
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(cache);
						img.compress(Bitmap.CompressFormat.PNG, 100, fos);
					} catch (FileNotFoundException e) {
						
					} finally {
						if (fos != null)
							try {
								fos.close();
							} catch (IOException e) {
								
							}
					}
					
					
				}
			}, 0, 0, null, null );
			getImage.setRetryPolicy(new DefaultRetryPolicy(3000, 2, 2));
			getImage.setTag(mUrl);
			SessionManager.getRequestQueue().add(getImage);			
		}
		super.onActivityCreated(savedInstanceState);
	}
	
	public void save() {
		
	}
}
