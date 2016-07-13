package com.slowalker.musicbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slowalker.musicbox.mood.MoodClassifyService;
import com.slowalker.musicbox.util.DatabaseHelper;
import com.slowalker.musicbox.util.MusicFuntions;
import com.slowalker.musicbox.util.MyIntentAction;
import com.slowalker.musicbox.view.AttrDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


public class MusicList extends ListActivity implements OnItemClickListener
{
	private ArrayList<Long> musicList;
	private List<Map<String, Object>> data;
	private SimpleAdapter adapter;
	private ListView lv;
	private boolean isCurrentList;
	private long currentId;
	private boolean isPlaying;
	private SharedPreferences sp;
	private OnCreateContextMenuListener ccmListener;
	private static final int INSERT_TO_PLAYLIST_GROUP = 100;
	private static final int DELETE_MUSIC_FILE_DLG = 1;
    public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    Log.v("musiclsit", "oncreate");
	    init();
	    Intent intent = getIntent();
	    if (this.isChild())
	    {
	    	try {
				currentId = getGroup().getCurrentId();
				isPlaying = getGroup().isPlaying();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
		}
	    else
	    {
	    	currentId = intent.getExtras().getLong("CurrentId");
	    	isPlaying = intent.getExtras().getBoolean("IsPlaying");
	    }
	    if ( intent.hasExtra("artist"))
    	{
	    	showMusicOfArtist(intent);
	    	lv.setOnCreateContextMenuListener(ccmListener);
	    }
    	else if(intent.hasExtra("album"))
    	{
    		showAllMusicOfAlbum(intent);
    		lv.setOnCreateContextMenuListener(ccmListener);
    	}
    	else if (intent.hasExtra("PlayList"))
    	{
    		showMusicOfPlayList(intent);
    	}
    	else if (intent.hasExtra("mood"))
    	{
    		showMusicOfMood(intent);
    		lv.setOnCreateContextMenuListener(ccmListener);
    	}
    	else if (intent.hasExtra("Favorite"))
    	{
    		showMusicOfFavorite(intent);
    		lv.setOnCreateContextMenuListener(ccmListener);
    	}
    	else if (intent.hasExtra("Latest_added"))
    	{
    		showMusicOfLatestAdded(intent);
    		lv.setOnCreateContextMenuListener(ccmListener);
    	}
    	else 
	    {
	    	showAllMusic();
	    	lv.setOnCreateContextMenuListener(ccmListener);
	    	
	    }
	    
	   // lv.setCacheColorHint(Color.TRANSPARENT);
       lv.setOnItemClickListener(this);
	   
	}
    private void init()
	{
		musicList = new ArrayList<Long>();
		data = new ArrayList<Map<String, Object>>();
		lv = getListView();
		isCurrentList = false;
		sp = getSharedPreferences("MusicBox", MODE_PRIVATE);
		ccmListener = new OnCreateContextMenuListener() {
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				// TODO Auto-generated method stub
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo; 
				String title = (String) data.get(info.position).get("TITLE");
				long musicId = musicList.get(info.position);
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.music_list_context_options, menu);
				menu.setHeaderTitle(title); 
				SubMenu sm = menu.addSubMenu("添加到播放列表");
			    ArrayList<Pair<Long, String>> list = 
			    	                    MusicFuntions.getAllPlayListName(MusicList.this);
				int order = 0;
				sm.add(INSERT_TO_PLAYLIST_GROUP, -1, order++, "正在播放")
			           .setIntent(new Intent().putExtra("MusicId", musicId)
			           .putExtra("Title", title));
				for (Pair<Long, String> pair : list)
				{
					sm.add(INSERT_TO_PLAYLIST_GROUP, pair.first.intValue(), order++, pair.second)
					       .setIntent(new Intent().putExtra("MusicId", musicId)
					       .putExtra("Title", title));
				}
		        
		    }
		};
		registerReceiver();
		
	}
   
    
    //context menu item of a specific music that have been long pressed
	//--------------------------------------------------------------------------------
	@Override 
	public boolean onContextItemSelected(MenuItem item)  
	{ 
		if (item.getGroupId() == INSERT_TO_PLAYLIST_GROUP)//add to playlist
		{
		    long playListId = item.getItemId();
			long musicId = item.getIntent().getExtras().getLong("MusicId");
			String title = item.getIntent().getExtras().getString("Title");
			if (playListId == -1)
			{
			    if (MusicFuntions.insertToCurrentList(this, musicId))
			    	Toast.makeText(this, "成功添加 " + title, 500).show();
			    else
			    	Toast.makeText(this, title + " 已存在", 500).show();
			}
			else
			{
				if (MusicFuntions.insertToPlayList(this, playListId, musicId))
					Toast.makeText(this, "成功添加 " + title, 500).show();
				else
					Toast.makeText(this, title + " 已存在", 500).show();
			}
				
	    }
		else if (item.getItemId() == R.id.music_list_remove_music)
		{
			Intent intent = item.getIntent();
			int position = intent.getExtras().getInt("Position");
		    long musicId = musicList.get(position);
		    String title = (String) data.get(position).get("TITLE");
		    if (musicId == currentId)
		    {
		    	Toast.makeText(this, title + " 正在播放", 500).show();
		    	return true;
		    }
		    long playListId = intent.getExtras().getLong("PLAYLIST_ID");
			if (playListId == -1)
			{
				MusicFuntions.removeFromCurrentList(this, musicId);
			}
			else
			{
				MusicFuntions.removeFromPlayList(this, playListId, musicId);
			}
			Toast.makeText(this, title + " 已删除", 500).show();
		    musicList.remove(position);
			data.remove(position);
			adapter.notifyDataSetChanged();
		}
		else if (item.getItemId() == R.id.music_list_music_attr)
		{
			AdapterView.AdapterContextMenuInfo info = 
				                        (AdapterView.AdapterContextMenuInfo) item.getMenuInfo(); 
			long musicId = musicList.get(info.position);
			Bundle musicAttr = MusicFuntions.getMusicInfoToShow(this, musicId);
			new AttrDialog(this, musicAttr).create().show();
		}
		else if (item.getItemId() == R.id.music_list_delete)
		{
			AdapterView.AdapterContextMenuInfo info = 
                                  (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			long musicId = musicList.get(info.position);
			String title = (String) data.get(info.position).get("TITLE");
			Bundle bundle = new Bundle();
			bundle.putInt("Position", info.position);
			bundle.putLong("MusicId", musicId);
			bundle.putString("Title", title);
			if (musicId == currentId)
			{
				Toast.makeText(this, title + " 正在播放", 500).show();
			}
			else
			{
				showDialog(DELETE_MUSIC_FILE_DLG, bundle);
			}
		}
		return true;
	}
	
	protected Dialog onCreateDialog(int id, Bundle bundle)
	{
		Dialog dialog;
		switch(id)
		{
		case DELETE_MUSIC_FILE_DLG:
			final int position = bundle.getInt("Position");
			final long musicId = bundle.getLong("MusicId");
			final String title = bundle.getString("Title");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setTitle("删除 " + title + "?" );
	    	builder.setPositiveButton("确定", new OnClickListener()
	    	{

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					boolean result = MusicFuntions.deleteMusicFileFromId(MusicList.this, musicId);
					if (result)
					{
						musicList.remove(position);
						data.remove(position);
						adapter.notifyDataSetChanged();
						Intent intent = new Intent(MusicList.this, MoodClassifyService.class);
						startService(intent);
						intent = new Intent(MusicList.this, PlayCountService.class);
						startService(intent);
						Toast.makeText(MusicList.this, "成功删除 " + title, 500).show();
					}
					else
					{
						Toast.makeText(MusicList.this, "删除 " + title + " 失败", 500).show();
					}
					removeDialog(DELETE_MUSIC_FILE_DLG);
				
				}
	    		 
	    	 });
	    	 builder.setNegativeButton("取消", new OnClickListener()
	    	 {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
	    		 
	    	 });
	    	 dialog =  builder.create();
	    	 break;
	     
		default:
	    	 dialog = null;
	    	 break;
		}
		return dialog;
		
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context ctx, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(MyIntentAction.PLAY))
			{
				currentId = intent.getExtras().getLong("MusicId");
				setLabelById(currentId, R.drawable.play_label);
				adapter.notifyDataSetChanged();
				Log.v("musicList", "play_received");
			}
			else if (intent.getAction().equals(MyIntentAction.PAUSE))
			{
				currentId = intent.getExtras().getLong("MusicId");
				setLabelById(currentId, R.drawable.pause_label);
				adapter.notifyDataSetChanged();
				Log.v("musicList", "pause_received");
			}
			else if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED))
			{
				setLabelById(currentId, R.drawable.normal_label);
				currentId = intent.getExtras().getLong("MusicId");
				boolean isPlaying = intent.getExtras().getBoolean("IsPlaying");
				if (isPlaying)
				     setLabelById(currentId, R.drawable.play_label);
				else
					 setLabelById(currentId, R.drawable.pause_label);
				adapter.notifyDataSetChanged();
				Log.v("musicList", "adc_received");
			}
			
		}
		
	};
	private void registerReceiver()
	 {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_DATE_CHANGED);
   	    filter.addAction(MyIntentAction.PLAY);
   	    filter.addAction(MyIntentAction.PAUSE);
   	    LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
	 }
    private void unregisterReceiver()
    {
   	    LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }
    
	private void setLabelById(Long musicId, int resId)
	{
          int position = musicList.indexOf(musicId);
          if (position != -1)
	          data.get(position).put("LABEL", resId);
		
		
	}
	//---------------------------------------------------------------------------------
	private void showMusicOfArtist(Intent intent)
	{
		String thisArtist = intent.getExtras().getString("artist");
		String album_of_artist = intent.getExtras().getString("album_of_artist");
		Cursor cur = this.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
                null, "artist=? AND album=?", new String[]{thisArtist, album_of_artist}, 
                MediaStore.Audio.Media.TRACK);
		fillDataAndMusicList(cur, true, false, false);
		adapter = new SimpleAdapter(this, data, R.layout.music_list_item2,
				  new String[]{"LABEL", "TITLE"}, 
				  new int[] { R.id.ml_item2_stateLabel, R.id.ml_item2_title });
		this.setListAdapter(adapter);
		
	}
	
	//--------------------------------------------------------------------------------
	private void showAllMusicOfAlbum(Intent intent)
	{
		String thisAlbum = intent.getExtras().getString("album");
		Cursor cur = this.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
                null, "album=?", new String[]{thisAlbum}, 
                MediaStore.Audio.Media.TRACK);
		fillDataAndMusicList(cur, true, true, false);
		adapter = new SimpleAdapter(this, data, R.layout.music_list_item3,
				  new String[]{"LABEL", "TITLE", "ARTIST"}, 
				  new int[] { R.id.ml_item3_stateLabel, R.id.ml_item3_title, 
				              R.id.ml_item3_artist });
		this.setListAdapter(adapter);
		
	}
	
	//show all music of a specific playList
	//-------------------------------------------------------------------------------
	private void showMusicOfPlayList(Intent intent)
	{
		long playListId;
		//get musicIds of this PlayList
		String playListName = intent.getExtras().getString("PlayList");
		if (playListName.equals("CurrentList"))
		{
			playListId = -1;
			String list = sp.getString("CurrentList", "");
			list = MusicFuntions.checkCurrentList(this, list);
			if (!list.isEmpty())
			{
				String playList[] = list.split(";");
				for (String str : playList)
					musicList.add(Long.parseLong(str));
			}
			isCurrentList = true;
		}
		else
		{
			playListId = MusicFuntions.getPlayListIdFromName(this, playListName);
			Uri membersUri = MediaStore.Audio.Playlists.Members.getContentUri(
	    		    "external", playListId);
			Cursor cur = this.managedQuery(membersUri, null, null, null, 
					    MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
			if (cur.moveToFirst())
			{
				while (!cur.isAfterLast())
				{
					musicList.add(cur.getLong(cur.getColumnIndex(
							MediaStore.Audio.Playlists.Members.AUDIO_ID)));
					cur.moveToNext();
				}
			}
			
		}
		
		//use musicList to get Titles and Artists
		String projection[] = new String[] { MediaStore.Audio.Media.TITLE,
				                             MediaStore.Audio.Media.ARTIST,
				                             MediaStore.Audio.Media.ALBUM};
		for (long lg : musicList)
		{
			Uri mUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, lg);
			Cursor cur = this.managedQuery(mUri, projection, null, null, null);
			cur.moveToFirst();
			String title = cur.getString(0);
			String artist = cur.getString(1);
			String album = cur.getString(2);
			Map<String, Object> map = new HashMap<String, Object>();
			if (lg == currentId)
			{
				if (isPlaying)
					map.put("LABEL", R.drawable.play_label);
				else
					map.put("LABEL", R.drawable.pause_label);
			}
			else
				map.put("LABEL", R.drawable.normal_label);
			map.put("TITLE", title);
			map.put("ARTIST", artist);
			map.put("ALBUM", album);
			data.add(map);
		}
		adapter = new SimpleAdapter(this, data, R.layout.music_list_item1,
				  new String[]{"LABEL", "TITLE", "ARTIST", "ALBUM"}, 
				  new int[] { R.id.ml_item1_stateLabel, R.id.ml_item1_title, 
				              R.id.ml_item1_artist, R.id.ml_item1_album});
		
		this.setListAdapter(adapter);
		
		final long listId = playListId;
        lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				// TODO Auto-generated method stub
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo; 
				String title = (String) data.get(info.position).get("TITLE");
				Intent intent = new Intent(); 
				intent.putExtra("PLAYLIST_ID", listId);
				intent.putExtra("Position", info.position);
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.music_list_context_options, menu);
				menu.setHeaderTitle(title); 
				MenuItem item = menu.findItem(R.id.music_list_remove_music);
				item.setVisible(true);
				item.setIntent(intent);
				
			 }
		});
		
	}
	
	//show all music of a given mood type
	//------------------------------------------------------------------------------------
	private void showMusicOfMood(Intent intent)
	{
		int mood = intent.getExtras().getInt("mood");
		long[] list = getMusicIdOfMood(mood);
		
		Map<String, Object> map = null;
		for (long id : list)
		{
			musicList.add(id);
			Bundle bundle = MusicFuntions.getMusicInfoFromId(this, id);
			map = new HashMap<String, Object>();
			String title = bundle.getString("title");
			String artist = bundle.getString("artist");
			String album = bundle.getString("album");
			map.put("TITLE", title);
			map.put("ARTIST", artist);
			map.put("ALBUM", album);
			if (id == currentId)
			{
				if (isPlaying)
					map.put("LABEL", R.drawable.play_label);
				else
					map.put("LABEL", R.drawable.pause_label);
			}
			else
				map.put("LABEL", R.drawable.normal_label);
			data.add(map);
		}
		adapter = new SimpleAdapter(this, data, R.layout.music_list_item1,
				  new String[]{"LABEL", "TITLE", "ARTIST", "ALBUM"}, 
				  new int[] { R.id.ml_item1_stateLabel, R.id.ml_item1_title, 
				              R.id.ml_item1_artist, R.id.ml_item1_album});
		this.setListAdapter(adapter);
		
	}
	
	private void showAllMusic()
	{
		Cursor cur = this.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
	            null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		fillDataAndMusicList(cur, true, true, true);
		adapter = new SimpleAdapter(this, data, R.layout.music_list_item1,
				new String[]{"LABEL", "TITLE", "ARTIST", "ALBUM"}, 
				new int[] { R.id.ml_item1_stateLabel, R.id.ml_item1_title, 
				            R.id.ml_item1_artist, R.id.ml_item1_album});
	    this.setListAdapter(adapter);
	}
	
	
	//------------------------------------------------------------------------------------
	
	private void showMusicOfFavorite(Intent intent)
	{
		int num = intent.getExtras().getInt("Favorite");
		ArrayList<Pair<Long, Integer>> list = MusicFuntions.getFavoriteMusic(this, num);
		for (Pair<Long, Integer> pair : list)
		{
			long id = pair.first;
			musicList.add(id);
			Bundle bundle = MusicFuntions.getMusicInfoFromId(this, id);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("TITLE", bundle.getString("title"));
			map.put("ARTIST", bundle.getString("artist"));
			map.put("ALBUM", bundle.getString("album"));
			map.put("PLAY_COUNT", "已播放"+Integer.toString(pair.second)+"次");
			if (id == currentId)
			{
				if (isPlaying)
					map.put("LABEL", R.drawable.play_label);
				else
					map.put("LABEL", R.drawable.pause_label);
			}
			else
				map.put("LABEL", R.drawable.normal_label);
			data.add(map);
		}
		adapter = new SimpleAdapter(this, data, R.layout.music_list_item4,
				  new String[]{"LABEL", "TITLE", "ARTIST", "ALBUM", "PLAY_COUNT"}, 
				  new int[] { R.id.ml_item4_stateLabel, R.id.ml_item4_title, 
				              R.id.ml_item4_artist, R.id.ml_item4_album, 
				              R.id.ml_item4_playCount});
		this.setListAdapter(adapter);
	}
	
	//-----------------------------------------------------------------------------------
	private void showMusicOfLatestAdded(Intent intent)
	{
		long limit = intent.getExtras().getLong("Latest_added");
		musicList = MusicFuntions.getLatestAddedMusic(this, limit);
		for (long id : musicList)
		{
			Bundle bundle = MusicFuntions.getMusicInfoFromId(this, id);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("TITLE", bundle.getString("title"));
			map.put("ARTIST", bundle.getString("artist"));
			map.put("ALBUM", bundle.getString("album"));
			if (id == currentId)
			{
				if (isPlaying)
					map.put("LABEL", R.drawable.play_label);
				else
					map.put("LABEL", R.drawable.pause_label);
			}
			else
				map.put("LABEL", R.drawable.normal_label);
			data.add(map);
		}
		adapter = new SimpleAdapter(this, data, R.layout.music_list_item1,
				  new String[]{"LABEL", "TITLE", "ARTIST", "ALBUM"}, 
				  new int[] { R.id.ml_item1_stateLabel, R.id.ml_item1_title, 
				              R.id.ml_item1_artist, R.id.ml_item1_album} );
		this.setListAdapter(adapter);
	}
	
	//use to initialize variable "data"
	//-------------------------------------------------------------------------------------
	private void fillDataAndMusicList(Cursor cur, boolean show_title, 
			                                  boolean show_artist, boolean show_album)
	{
		if (cur.moveToFirst())
		{
			while (!cur.isAfterLast())
			{
				long musicId = cur.getInt(cur.getColumnIndex(MediaStore.Audio.Media._ID));
				musicList.add(musicId);
				Map<String, Object> map = new HashMap<String, Object>();
				if (currentId == musicId)
				{
					if (isPlaying)
						map.put("LABEL", R.drawable.play_label);
					else
						map.put("LABEL", R.drawable.pause_label);
				}
				else
					map.put("LABEL", R.drawable.normal_label);
				
				if (show_title)
				{
					String title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
					map.put("TITLE", title);
				}
				if (show_artist)
				{
					String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
					map.put("ARTIST", artist);
				}
				if (show_album)
			    {
				    String album = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM));
				    map.put("ALBUM", album);
		     	}
				data.add(map);
				cur.moveToNext();
			}
		}
		
	}
	
	//Item click listener
	//---------------------------------------------------------------------------------
	public void onItemClick(AdapterView<?> adView, View target, int position, long id)
	{
		//get music title 
		//String title = (String) data.get(position).get("TITLE");
	   //use title to get music ID 
	    long musicId = musicList.get(position);
	    updateCurrentList(musicId, musicList);
		Uri selectedMusic = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId);
		Intent intent = new Intent(this, MusicPlayService.class);
		intent.setData(selectedMusic);
		intent.setAction(MyIntentAction.MUSIC_SELECTED_IN_LIST);
	    startService(intent);
	}
	
	public void updateCurrentList(long musicId, ArrayList<Long> musicList)
	{
		SharedPreferences sp = getSharedPreferences("MusicBox", MODE_PRIVATE);
		SharedPreferences.Editor se = sp.edit();
		StringBuilder sb = new StringBuilder();
		if (!isCurrentList)
		{
			for (long id : musicList)
			{
				sb.append(Long.toString(id));
				if (id != musicList.get(musicList.size()-1))
				    sb.append(";");
			}
			se.putString("CurrentList", sb.toString());
			se.commit();
		}
		
	}
	/*private void collectMusicList(Cursor cur)
	{
		musicList.clear();
		if (cur.moveToFirst())
		{
			while (!cur.isAfterLast())
			{
				long musicId = cur.getInt(cur.getColumnIndex(MediaStore.Audio.Media._ID));
				musicList.add(musicId);
				cur.moveToNext();
			}
		}
		
	}*/
	
	 
	//query the database named "musicbox"
	private long[] getMusicIdOfMood(int mood)
	{
		SQLiteDatabase db = new DatabaseHelper(this, "musicbox", null, 1)
		                     .getReadableDatabase();
		Cursor cur = db.query("music_mood", new String[] { "ID"}  , 
				              "MOOD=?", new String[] { Integer.toString(mood) }, 
				               null, null, "ID");
		int count = 0;
		long[] list = new long[cur.getCount()];
		if (cur.moveToFirst())
		{
			while (!cur.isAfterLast())
			{
				list[count++] = cur.getLong(0);
				cur.moveToNext();
			}
		}
		cur.close();
		db.close();
		return list;
	}
	
	
	//override the action of BACK button
	//----------------------------------------------------------------------------
	public void onBackPressed()
	{
		if (this.isChild())
		{
			getGroup().onBackPressed();
		}
		else
			super.onBackPressed();
	    Log.v("musiclist", "back");
    		
    }
	
	 private MainActivity getGroup()
	 {
		 return (MainActivity) getParent();
	 }
	
	//------------------------------------------------------------------------------
	public void onStart()
    {
    	super.onStart();
    	Log.v("musicList", "onstart");
    }
    protected void onResume()
    {
    	super.onResume();
    	Log.v("musicList", "onResume");
    }
    protected void onPause()
    {
    	super.onPause();
    	Log.v("musicList", "onPause");
    }
    protected void onStop()
    {
    	super.onStop();
    	Log.v("musicList", "onstop");
    }
    protected void onDestroy()
    {
    	super.onDestroy();
    	unregisterReceiver();
    	Log.v("musicList", "destroy");
    	 
    }
	
}
