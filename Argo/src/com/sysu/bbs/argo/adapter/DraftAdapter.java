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

import android.app.Activity;
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

	private HashMap<File, Draft> mDraftMap;
	public DraftAdapter(Context context, int resource, ArrayList<File> objects) {
		super(context, resource, objects);
		mDraftMap = new HashMap<File, Draft>();
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
					builder.setMessage("是否删除草稿？")
					.setPositiveButton("是",new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							File file = getItem(position);
							remove(file);
							file.delete();
							
						}
					})
					.setNegativeButton("否", null)
					.show();
				}
			});
			v.setTag(holder);
		}
		
		File file = getItem(position);
		Draft draft = mDraftMap.get(file);
		if (draft == null ) {
			draft = getDraft(file);
			mDraftMap.put(file, draft);
		}
		holder.tvContent.setText(draft.getParsedContent());
		String quote = draft.getParsedQuote();
		if (quote != null && !quote.equals(""))
			holder.tvQuote.setText(draft.getParsedQuote());
		else
			holder.tvQuote.setVisibility(View.GONE);
		holder.tvTitle.setText(draft.getTitle());
		holder.tvBoardname.setText(draft.getBoard());
		
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMM HH:mm   ", Locale.US);
		Calendar update = Calendar.getInstance();
		update.setTimeInMillis(Long.valueOf(draft.getPost_time()));
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
	/**
	 * 从文件中读取草稿
	 * @param file
	 * 保存草稿的文件
	 */
	private Draft getDraft(File file) {
		FileInputStream fis = null;
		BufferedReader br = null;
		Draft draft = null;
		try {
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			
			draft = new Draft();
			draft.setType(br.readLine());
			draft.setBoard(br.readLine());
			draft.setFilename(br.readLine());
			draft.setTitle(br.readLine());
			draft.setPost_time(br.readLine());

			String ah = br.readLine();
			if (ah != null && !ah.equals("null") && !ah.equals("")) {
				draft.setPath(ah);
			}
			
			String s = "", line;
			while ((line = br.readLine()) != null)
				s += line + "\n";
			draft.setRawcontent(s);
			
			return draft;
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
	public void onItemClick(AdapterView<?> draftList, View draftView, int pos, long id) {

		
		Draft draft = mDraftMap.get(getItem(pos));
		Intent intent = new Intent(getContext(), AddPostActivity.class);
		Bundle param = new Bundle();
		param.putString("type", draft.getType());
		param.putString("boardname", draft.getBoard());
		param.putString("articleid", draft.getFilename());
		param.putString("title", draft.getTitle());
		param.putString("content", draft.getRawcontent());
		param.putString("userid", draft.getUserid());
		param.putString("username", draft.getUsername());
		param.putString("attach", draft.getPath());
		param.putInt("_where_", 2);
		param.putString("_draft_", getItem(pos).getAbsolutePath());

		intent.putExtras(param);

		getContext().startActivity(intent);
		((Activity)getContext()).overridePendingTransition(R.anim.open_enter_slide_in, R.anim.open_exit_slide_out);
	}
	
	private class Draft extends Post {
		String path;
		String getPath() {
			return path;
		}
		void setPath(String p) {
			path = p;
		}
	}

}
