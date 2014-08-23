package com.sysu.bbs.argo.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sysu.bbs.argo.AddPostActivity;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.api.dao.Post;

public class DraftAdapter extends ArrayAdapter<File> implements OnItemClickListener {

	private HashMap<File, Post> mPostMap;
	public DraftAdapter(Context context, int resource, ArrayList<File> objects) {
		super(context, resource, objects);
		mPostMap = new HashMap<File, Post>();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = convertView;
		DraftHolder holder = null;
		if (v != null) {
			holder = (DraftHolder) v.getTag();
		} else {
			v = LayoutInflater.from(getContext()).inflate(R.layout.item_draft, parent, false);
			holder = new DraftHolder();
			holder.tvTitle = (TextView) v.findViewById(R.id.draft_post_title);
			holder.tvContent = (TextView) v.findViewById(R.id.draft_post_content);
			holder.tvQuote = (TextView) v.findViewById(R.id.draft_post_quote);
			holder.tvTime = (TextView) v.findViewById(R.id.draft_post_time);
			holder.tvBoardname = (TextView) v.findViewById(R.id.draft_boardname);
			holder.btnDelete = (ImageButton) v.findViewById(R.id.draft_delete);
			holder.btnDelete.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getContext(), AlertDialog.THEME_HOLO_DARK);
					builder.setMessage("ÊÇ·ñÉ¾³ý²Ý¸å£¿")
					.setPositiveButton("ÊÇ",new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							File file = getItem(position);
							remove(file);
							file.delete();
							
						}
					})
					.setNegativeButton("·ñ", null)
					.show();


					
				}
			});
			v.setTag(holder);
		}
		
		File file = getItem(position);
		Post post = mPostMap.get(file);
		if (post == null ) {
			post = getPost(file);
			mPostMap.put(file, post);
		}
		holder.tvContent.setText(post.getParsedContent());
		String quote = post.getParsedQuote();
		if (quote != null && !quote.equals(""))
			holder.tvQuote.setText(post.getParsedQuote());
		else
			holder.tvQuote.setVisibility(View.GONE);
		holder.tvTitle.setText(post.getTitle());
		holder.tvBoardname.setText(post.getBoard());
		
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMM HH:mm   ", Locale.US);
		Calendar update = Calendar.getInstance();
		update.setTimeInMillis(Long.valueOf(post.getPost_time()));
		Date date = update.getTime();		
		holder.tvTime.setText(sdf.format(date));
		
		return v;
	}
	
	private class DraftHolder {
		TextView tvTitle;
		TextView tvContent;
		TextView tvQuote;
		TextView tvTime;
		TextView tvBoardname;
		ImageButton btnDelete;
	}
	
	private Post getPost(File file) {
		FileInputStream fis = null;
		BufferedReader br = null;
		Post post = null;
		try {
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			
			post = new Post();
			post.setType(br.readLine());
			post.setBoard(br.readLine());
			post.setFilename(br.readLine());
			post.setTitle(br.readLine());
			post.setPost_time(br.readLine());
			
			String s = "", line;
			while ((line = br.readLine()) != null)
				s += line;
			post.setRawcontent(s);
			
			return post;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> draftList, View draft, int pos, long id) {

		
		Post post = mPostMap.get(getItem(pos));
		Intent intent = new Intent(getContext(), AddPostActivity.class);
		Bundle param = new Bundle();
		param.putString("type", post.getType());
		param.putString("boardname", post.getBoard());
		param.putString("articleid", post.getFilename());
		param.putString("title", post.getTitle());
		param.putString("content", post.getRawcontent());
		param.putString("userid", post.getUserid());
		param.putString("username", post.getUsername());
		param.putInt("_where_", 2);
		param.putString("_draft_", getItem(pos).getAbsolutePath());

		intent.putExtras(param);

		getContext().startActivity(intent);
		
	}

}
