package com.slowalker.musicbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slowalker.musicbox.util.MusicFuntions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

public class MoodList extends Activity {
	List<Map<String, Object>> data;
	GridView gv;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("MoodList", "oncreate");
        setContentView(R.layout.mood_grid);
        gv = (GridView) this.findViewById(R.id.moodGrid);
        data = new ArrayList<Map<String, Object>>();
        fillData();
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.mood_grid_item,
        		   new String[] { "MoodDesc", "NumberOfSongs", "MoodImage" },
        		   new int[] { R.id.mood_item_type, R.id.mood_item_numOfSongs,
        		               R.id.mood_item_image} );
        gv.setAdapter(adapter);
        gv.setOnItemClickListener(new OnItemClickListener()
        {

			@Override
			public void onItemClick(AdapterView<?> adView, View target, int position, 
					long id) 
			{
				// TODO Auto-generated method stub
				int mood = (Integer) data.get(position).get("Mood");
				Intent intent = new Intent(MoodList.this, MusicList.class);
				intent.putExtra("mood", mood);
				intent.putExtra("ClassName", "MusicList");
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getGroup().nextActivity(intent);
				
			}
        	
        });
   }
   private void fillData()
   {
	   int[] num = MusicFuntions.getNumOfMusicByMood(this);
	   int mood;
	   Map<String, Object> map = null;
	   
	   map = new HashMap<String, Object>();
	   mood = 2;
	   map.put("Mood", mood);
	   map.put("MoodDesc", "焦虑急切");
	   map.put("NumberOfSongs", Integer.toString(num[mood-1])+"首歌曲");
	   map.put("MoodImage", R.drawable.anxious);
	   data.add(map);
	  
	   map = new HashMap<String, Object>();
	   mood = 1;
	   map.put("Mood", mood);
	   map.put("MoodDesc", "情绪高昂");
	   map.put("NumberOfSongs", Integer.toString(num[mood-1])+"首歌曲");
	   map.put("MoodImage", R.drawable.exuberance);
	   data.add(map);
	   
	   map = new HashMap<String, Object>();
	   mood = 3;
	   map.put("Mood", mood);
	   map.put("MoodDesc", "低沉伤感");
	   map.put("NumberOfSongs", Integer.toString(num[mood-1])+"首歌曲");
	   map.put("MoodImage", R.drawable.depression);
	   data.add(map);
	   
	   map = new HashMap<String, Object>();
	   mood = 4;
	   map.put("Mood", mood);
	   map.put("MoodDesc", "欢快悠闲");
	   map.put("NumberOfSongs", Integer.toString(num[mood-1])+"首歌曲");
	   map.put("MoodImage", R.drawable.comtentment);
	   data.add(map);
   }
   
   public void onBackPressed()
   {
	   getGroup().onBackPressed();
       Log.v("moodlist", "back");
   		
   }
   
   private MainActivity getGroup()
   {
		return (MainActivity) getParent();
   }

}
