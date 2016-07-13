package com.slowalker.musicbox.view;

import com.slowalker.musicbox.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AttrDialog {
	private Dialog dialog;
	private Bundle musicInfo;
	public AttrDialog(Context ctx, Bundle bundle)
	{
		dialog = new Dialog(ctx);
		musicInfo = bundle;
	}
	public Dialog create()
	{
		dialog.setContentView(R.layout.music_list_show_attr_dlg);
		dialog.setTitle("歌曲信息");
		TextView title = (TextView) dialog.findViewById(R.id.musicList_showAttr_title);
		TextView artist = (TextView) dialog.findViewById(R.id.musicList_showAttr_artist);
		TextView album = (TextView) dialog.findViewById(R.id.musicList_showAttr_album);
	    TextView genres = (TextView) dialog.findViewById(R.id.musicList_showAttr_genres);
	    TextView playCount = (TextView) dialog.findViewById(R.id.musicList_showAttr_playCount);
	    TextView mood = (TextView) dialog.findViewById(R.id.musicList_showAttr_mood);
		TextView duration = (TextView) dialog.findViewById(R.id.musicList_showAttr_duration);
		TextView size = (TextView) dialog.findViewById(R.id.musicList_showAttr_size);
		TextView type = (TextView) dialog.findViewById(R.id.musicList_showAttr_type);
		TextView year = (TextView) dialog.findViewById(R.id.musicList_showAttr_year);
		TextView bitrate = (TextView) dialog.findViewById(R.id.musicList_showAttr_bitrate);
		TextView track = (TextView) dialog.findViewById(R.id.musicList_showAttr_track);
		TextView path = (TextView) dialog.findViewById(R.id.musicList_showAttr_path);
		title.setText(musicInfo.getString("Title"));
		artist.setText(musicInfo.getString("Artist"));
		album.setText(musicInfo.getString("Album"));
		genres.setText("风格：" + musicInfo.getString("Genres"));
		playCount.setText("播放次数：" + musicInfo.getString("PlayCount"));
		mood.setText("情感：" + musicInfo.getString("Mood"));
		duration.setText("时长：" + musicInfo.getString("Duration"));
		size.setText("大小：" + musicInfo.getString("Size"));
		type.setText("格式：" + musicInfo.getString("Type"));
		year.setText("年份：" + musicInfo.getString("Year"));
		bitrate.setText("比特率：" + musicInfo.getString("Bitrate"));
		track.setText("音轨：" + musicInfo.getString("Track"));
		path.setText(musicInfo.getString("Path"));
		
		Button btn_pos = (Button) dialog.findViewById(R.id.musicList_showAttr_pos);
		btn_pos.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				
			}
			
		});
		return dialog;
	}
	
}
