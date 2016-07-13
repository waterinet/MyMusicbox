package com.slowalker.musicbox;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.slowalker.musicbox.util.MusicFuntions;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;


public class MusicGrid extends Activity
     
{
	//private TableLayout mGrid;
	public static final int ID_GET_PLAYLIST_NAME_DLG = 0;
	List<Map<String, Object>> data;
	SimpleAdapter adapter;
	GridView gv;
	SharedPreferences sp;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("musicGrid", "oncreate");
        setContentView(R.layout.music_grid);
        sp = getSharedPreferences("MusicBox", MODE_PRIVATE);
        data = new ArrayList<Map<String, Object>>();
        gv = (GridView) this.findViewById(R.id.mainGrid);
        addOriginalItems();
        addPlayListItems();
        sortData();
        adapter = new SimpleAdapter(this, data, R.layout.music_grid_item,
        		   new String[] { "Image", "Name", "Number" },
        		   new int[] { R.id.gridItem_Image, R.id.gridItem_name, R.id.gridItem_num });
        gv.setAdapter(adapter);
        setListener();
        
       
   }
	
	private void sortData()
	{
		int start = 8; 
		int size = data.size();
		for (int i = 0; i < size - start - 1; ++i)
		{
			for (int j = start; j < size - 1 - i; ++j)
			{
				String str1 = (String) data.get(j).get("Name");
				String str2 = (String) data.get(j+1).get("Name");
				if (compare(str1, str2) > 0)
					swap(j, j+1);
			}
		}
    }
	public static int compare(String str1, String str2)
	{
		if (checkPlaylistName(str1) && checkPlaylistName(str2))
		{
			int order1 = Integer.parseInt(str1.substring(4));
			int order2 = Integer.parseInt(str2.substring(4));
			if (order1 > order2)
				return 1;
			else if (order1 < order2)
			    return -1;
			else
				return 0;
		}
		else if (checkPlaylistName(str1) && !checkPlaylistName(str2))
		{
			return 1;
		}
		else if (!checkPlaylistName(str1) && checkPlaylistName(str2))
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
	private void swap(int index1, int index2)
	{
		Map<String, Object> map = data.get(index1);
		data.set(index1, data.get(index2));
		data.set(index2, map);
	}
   
   private void addOriginalItems()
   {
	   Cursor cur = this.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
			               null, null, null, null);
	   Map<String, Object> map = new HashMap<String, Object>();
	   map.put("Image", R.drawable.allmusic);
	   map.put("Name", "全部音乐");
	   map.put("Number", Integer.toString(cur.getCount())+"首歌曲");
	   data.add(map);
	   
	   
	   cur = this.managedQuery(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
			               null, null, null, null);
	   map = new HashMap<String, Object>();
	   map.put("Image", R.drawable.in_artist);
	   map.put("Name", "艺术家");
	   map.put("Number", Integer.toString(cur.getCount())+"名艺术家");
	   data.add(map);
	   
	   
	   cur = this.managedQuery(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
               null, null, null, null);
	   map = new HashMap<String, Object>();
	   map.put("Image", R.drawable.in_album);
	   map.put("Name", "专辑");
	   map.put("Number", Integer.toString(cur.getCount())+"张专辑");
	   data.add(map);
	   
	   int numOfMusicWithMood = MusicFuntions.getNumOfMusicWithMood(this);
	   map = new HashMap<String, Object>();
	   map.put("Image", R.drawable.mood);
	   map.put("Name", "情感列表");
	   map.put("Number", Integer.toString(numOfMusicWithMood)+"首歌曲");
	   data.add(map);
	   
	   int numOfFavorite = MusicFuntions.getNumOfFavoriteMusic(this, 30);
	   map = new HashMap<String, Object>();
	   map.put("Image", R.drawable.favorite);
	   map.put("Name", "最常播放");
	   map.put("Number", Integer.toString(numOfFavorite)+"首歌曲");
	   data.add(map);
	   
	   long limit = 24 * 60 * 60; // one day
	   int numOfLatest = MusicFuntions.getNumOfLatestAddedMusic(this, limit);
	   map = new HashMap<String, Object>();
	   map.put("Image", R.drawable.latest_added);
	   map.put("Name", "最近添加");
	   map.put("Number", Integer.toString(numOfLatest)+"首歌曲");
	   data.add(map);
	   
	   map = new HashMap<String, Object>();
	   map.put("Image", R.drawable.playlist);
	   map.put("Name", "正在播放");
	   int num = MusicFuntions.getNumOfCurrentList(this);
	   map.put("Number", Integer.toString(num)+"首歌曲");
	   data.add(map);
	  
	   map = new HashMap<String, Object>();
	   map.put("Image", R.drawable.new_list);
	   map.put("Name", "新建列表");
	   map.put("Number", "");
	   data.add(map);
	  
   }
   
   
   private void setListener()
   {
	   gv.setOnItemClickListener(new OnItemClickListener() 
 	   {

 		  @Override
 		  public void onItemClick(AdapterView<?> adView, View target, int position, long id) 
 		  {
 			   Intent intent = new Intent();
 			   switch (position)
 			   {
 			   case 0:
 				   intent = new Intent(MusicGrid.this, MusicList.class);
 				   intent.putExtra("ClassName", "MusicList");
 				   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          		   getGroup().nextActivity(intent);
          		   break;
 			   case 1:
 				   intent = new Intent(MusicGrid.this, ArtistList.class);
 				   intent.putExtra("ClassName", "ArtistList");
 				   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          		   getGroup().nextActivity(intent);
          		   break;
 			   case 2:
 				   intent = new Intent(MusicGrid.this, AlbumList.class);
 				   intent.putExtra("ClassName", "AlbumList");
 				   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          		   getGroup().nextActivity(intent);
          		   break;
 			   case 3:
 				   intent = new Intent(MusicGrid.this, MoodList.class);
 				   intent.putExtra("ClassName", "MoodList");
 				   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         		   getGroup().nextActivity(intent);
 				   break;
 			   case 4:
 				   intent = new Intent(MusicGrid.this, MusicList.class);
 				   intent.putExtra("ClassName", "MusicList");
 				   intent.putExtra("Favorite", 30);
 				   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				   getGroup().nextActivity(intent);
 				   break;
 			   case 5:
 				   intent = new Intent(MusicGrid.this, MusicList.class);
				   intent.putExtra("ClassName", "MusicList");
				   long limit = 24 * 60 * 60; //a day in seconds
				   intent.putExtra("Latest_added", limit);
				   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				   getGroup().nextActivity(intent);
         		   break;
 			   case 6:
 				   intent = new Intent(MusicGrid.this, MusicList.class);
				   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				   intent.putExtra("ClassName", "MusicList");
				   intent.putExtra("PlayList", "CurrentList");
         	       getGroup().nextActivity(intent);
 				   break;
 			   case 7:
 				  showDialog(ID_GET_PLAYLIST_NAME_DLG);
 				  break;
 			   default:
 				   String playListName = (String) data.get(position).get("Name");
           	       intent = new Intent(MusicGrid.this, MusicList.class);
  				   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
           	       intent.putExtra("ClassName", "MusicList");
  				   intent.putExtra("PlayList", playListName);
  				   getGroup().nextActivity(intent);
  				   break;
 			   }
               
 		  }
 		   
 	  });
 	  gv.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
 	  {

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
		{
			// TODO Auto-generated method stub
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			int position = info.position;
			if (position > 7)
			{
			    String playlistName = (String) data.get(position).get("Name");
			    checkAndDeletePlaylistName(playlistName);
			    MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.remove_playlist, menu);
				menu.setHeaderTitle("请选择一项操作");
				menu.findItem(R.id.mItem_remov_pl).setIntent(
			        new Intent().putExtra("PlayListName", playlistName)
			                    .putExtra("ItemPosition", position));
			        
			}
		}
 		  
 	  });
 	 	
		
   }
   
   public boolean onContextItemSelected(MenuItem item)  
	{
		if (item.getItemId() == R.id.mItem_remov_pl)
		{
			Intent intent = item.getIntent();
			String playListName = intent.getExtras().getString("PlayListName");
			MusicFuntions.removePlayListFromMediaStore(this, playListName);
			int position = intent.getExtras().getInt("ItemPosition");
		    data.remove(position);
			adapter.notifyDataSetChanged();
		}
		return true; 
	
	}
	
	protected Dialog onCreateDialog(int id)
	{
		final Dialog dialog = new Dialog(this);
		switch(id)
		{
		case ID_GET_PLAYLIST_NAME_DLG:
			dialog.setContentView(R.layout.pick_playlist_name_dlg);
			dialog.setTitle("新建列表");
			//int playListCount = MusicFuntions.getPlayListCount(this);
			final EditText et = (EditText) dialog.findViewById(R.id.dlg_playListName);
			et.setText(getSuggestedPlaylistName());
			Button btn_pos = (Button) dialog.findViewById(R.id.dlg_positive);
			Button btn_neg = (Button) dialog.findViewById(R.id.dlg_negative);
			btn_pos.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					String playListName = et.getText().toString();
					if (!MusicFuntions.addPlayListToMediaStore(MusicGrid.this, playListName))
					{
						Toast.makeText(MusicGrid.this, "播放列表已存在，请重新添加", 
								Toast.LENGTH_SHORT).show();
					}
					else
					{
					    addPlayList(playListName);
						removeDialog(ID_GET_PLAYLIST_NAME_DLG);
					}
				}
				
			});
			btn_neg.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					removeDialog(ID_GET_PLAYLIST_NAME_DLG);
				}
				
			});
			
		}
		return dialog;
		
	}
	private String getSuggestedPlaylistName()
	{
		String prefix = "播放列表";
		int postfix = 1;
		String str = sp.getString("UserListNames", "");
		if (str.isEmpty())
			return prefix+Integer.toString(postfix);
		else
		{
			ArrayList<Integer> names = new ArrayList<Integer>();
		    for (String s : str.split(";"))
				names.add(Integer.parseInt(s));
			for (int i = 1; i < 100; ++i)
			{
				if (!names.contains(i))
				{
					postfix = i;
					break;
				}
			}
			return prefix + Integer.toString(postfix);
		}
	}
	
	private void addPlayList(String playlistName)
    {
		checkAndSavePlaylistName(playlistName);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Image", R.drawable.playlist);
		map.put("Name", playlistName);
		map.put("Number", Integer.toString(0)+"首歌曲");
		data.add(map);
		sortData();
	    adapter.notifyDataSetChanged();
     }
	
	private void checkAndSavePlaylistName(String playlistName)
	{
		if (checkPlaylistName(playlistName))
		{
			String str = sp.getString("UserListNames", "");
			StringBuilder sb = new StringBuilder(str);
			if (!str.isEmpty())
				sb.append(";");
			sb.append(playlistName.substring(4));
			SharedPreferences.Editor se = sp.edit();
			se.putString("UserListNames", sb.toString()).commit();
		}
	}
	private void checkAndDeletePlaylistName(String playlistName)
	{
		if (checkPlaylistName(playlistName))
		{
			String name = playlistName.substring(4);
			String str = sp.getString("UserListNames", "");
			if (!str.isEmpty())
			{
				StringBuilder sb = new StringBuilder();
				for (String s : str.split(";"))
				{
					if (!s.equals(name))
					{
						sb.append(s);
						sb.append(";");
					}
				}
				if (sb.length() > 0)
					sb.deleteCharAt(sb.length() - 1);
				SharedPreferences.Editor se = sp.edit();
				se.putString("UserListNames", sb.toString()).commit();
			}
		}
	}
	public static boolean checkPlaylistName(String playlistName)
	{
		Matcher m = Pattern.compile("播放列表[1-9]\\d*")
        .matcher(playlistName);
		if (m.matches())
			return true;
		else
			return false;
	}
	
	private void addPlayListItems()
	{
		Cursor cur = this.managedQuery(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, 
				          null, null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
		if (cur.moveToFirst())
		{
			while (!cur.isAfterLast())
			{
				long playListId = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Playlists._ID));
				String playListName = cur.getString(cur.getColumnIndex(
						             MediaStore.Audio.Playlists.NAME));
				Map<String, Object> map = new HashMap<String, Object>();
			    map.put("Image", R.drawable.playlist);
			    map.put("Name", playListName);
			    int count = MusicFuntions.getNumOfMusicInPlayList(this, playListId);
			    map.put("Number", Integer.toString(count)+"首歌曲");
			    data.add(map);
				cur.moveToNext();
			}
		}
	}
	
	
	
	 public void onBackPressed()
    {
		getGroup().onBackPressed();
        Log.v("musicgrid", "back");
    		
    }
	 
	 private MainActivity getGroup()
	 {
		   return (MainActivity) getParent();
	 }
	 public void onStart()
    {
    	super.onStart();
    	Log.v("musicGrid", "onstart");
    }
    protected void onResume()
    {
    	super.onResume();
    	Log.v("musicGrid", "onResume");
    }
    protected void onPause()
    {
    	super.onPause();
    	Log.v("musicGrid", "onPause");
    }
    protected void onStop()
    {
    	super.onStop();
    	Log.v("musicGrid", "onstop");
    }
    protected void onDestroy()
    {
    	super.onDestroy();
    	Log.v("musicGrid", "destroy");
    	 
    }

	
	


}
