package com.slowalker.musicbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;


public class ArtistList extends ExpandableListActivity  {
	
	private List<Map<String, Object>> groups;
	private List< List<Map<String, Object>> > children;
	private ExpandableListView ev;
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    init();
	    Log.v("artistList", "oncreate");
	    Cursor cur = this.managedQuery(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, 
				          null, null, null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
	    
	    List<Long> artistIds = new ArrayList<Long>();
	    if (cur.moveToFirst())
	    {
	    	while (!cur.isAfterLast())
	    	{
	    		Map<String, Object> map = new HashMap<String, Object>();
	    		artistIds.add(cur.getLong(cur.getColumnIndex(MediaStore.Audio.Artists._ID)));
	    		String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
	    		map.put("ARTIST", artist);
	    		int numAlbums = cur.getInt(cur.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS));
	    		int numTracks = cur.getInt(cur.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
	    		map.put("NUMBER_OF_ALBUMS", numAlbums);
	    		map.put("NUMBER_OF_TRACKS", numTracks);
	    		groups.add(map);
	    		cur.moveToNext();
	    	}
	    }
	    
	    
	   for (Long artistId : artistIds)
	    {
	    	Uri artistsAlbumsUri = MediaStore.Audio.Artists.Albums.getContentUri("external", artistId);
            Cursor cr = this.managedQuery(artistsAlbumsUri, null, null, null, null);
    		List<Map<String, Object>> children_of_a_group = new ArrayList<Map<String, Object>>(); 
    		if (cr.moveToFirst())
    		{
    			while (!cr.isAfterLast())
    			{
    				Map<String, Object> map = new HashMap<String, Object>();
    				map.put("ALBUM", cr.getString(cr.getColumnIndex(MediaStore.Audio.Artists.Albums.ALBUM)));
    				int numOfSongs = cr.getInt(cr.getColumnIndex(MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS));
    				map.put("NUMBER_OF_SONGS", numOfSongs);
    				int numOfSongsForArtist = cr.getInt(cr.getColumnIndex(
    						            MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS_FOR_ARTIST));
    				map.put("NUMBER_OF_SONGS_FOR_ARTIST", numOfSongsForArtist);
    				String albumArtPath = cr.getString(cr.getColumnIndex(MediaStore.Audio.Artists.Albums.ALBUM_ART));
    				if (albumArtPath == null)
    					map.put("ALBUM_ART", R.drawable.default_albumart_mini);
    				else
    					map.put("ALBUM_ART", albumArtPath);
    				children_of_a_group.add(map);
    				cr.moveToNext();
    			}
    		}
    		children.add(children_of_a_group);
	    }
	    MyExpandableListAdapter adapter = new MyExpandableListAdapter();
   		this.setListAdapter(adapter);
   		//ev.setCacheColorHint(Color.TRANSPARENT);
   		
   		ev.setOnChildClickListener(new OnChildClickListener()
	    {
            @Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, 
			                            int childPosition, long id) 
            {
				// TODO Auto-generated method stub
            	String artist = (String) groups.get(groupPosition).get("ARTIST");
            	String albumOfArtist = (String) children.get(groupPosition).get(childPosition)
            	                     .get("ALBUM");
            	Intent intent = new Intent(ArtistList.this, MusicList.class);
            	intent.putExtra("artist", artist);
        		intent.putExtra("album_of_artist", albumOfArtist);
        		intent.putExtra("ClassName", "MusicList");
        		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		getGroup().nextActivity(intent);
        		return true;
			}
	    	
	    });
	   
	}
	private void init()
	{
		ev = this.getExpandableListView();
		groups = new ArrayList<Map<String, Object>>();
		children = new ArrayList< List<Map<String, Object>> >();
		
	}
	
	private class MyExpandableListAdapter extends BaseExpandableListAdapter 
	{
        @Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return children.get(groupPosition).get(childPosition);
			
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null)
			{
				convertView = LayoutInflater.from(ArtistList.this)
				                      .inflate(R.layout.artist_list_child, null);
			}
			ImageView albumArt = (ImageView) convertView.findViewById(R.id.child_albumArt);
			
			Object artData = children.get(groupPosition).get(childPosition).get("ALBUM_ART");
			if (artData.getClass().getSimpleName().equals("Integer"))//res id
			    albumArt.setImageResource(R.drawable.default_albumart_mini);
			else  //string path
				albumArt.setImageBitmap(BitmapFactory.decodeFile((String) artData));
			
			TextView album = (TextView)convertView.findViewById(R.id.child_album);
			album.setText((CharSequence) children.get(groupPosition).get(childPosition).get("ALBUM"));
			TextView numOfSongs = (TextView)convertView.findViewById(R.id.child_numOfSongs);
			int num1 = (Integer)(children.get(groupPosition).get(childPosition).get("NUMBER_OF_SONGS"));
			int num2 = (Integer) (children.get(groupPosition).get(childPosition).get("NUMBER_OF_SONGS_FOR_ARTIST"));
			numOfSongs.setText(Integer.toString(num2)+"首歌曲/专辑共"+Integer.toString(num1)+"首歌曲");
			return convertView;
			
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return children.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return groups.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return groups.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				                   View convertView, ViewGroup parent ) {
			// TODO Auto-generated method stub
			if (convertView == null)
			{
				convertView = LayoutInflater.from(ArtistList.this)
				                 .inflate(R.layout.artist_list_group, null);
			}
			TextView artist = (TextView) convertView.findViewById(R.id.group_artist);
			TextView numOfAlbums = (TextView) convertView.findViewById(R.id.group_numOfAlbums);
			TextView numOfTracks = (TextView) convertView.findViewById(R.id.group_numOfTracks);
			artist.setText((CharSequence) groups.get(groupPosition).get("ARTIST"));
			int numAlbums = (Integer) groups.get(groupPosition).get("NUMBER_OF_ALBUMS");
			numOfAlbums.setText(Integer.toString(numAlbums)+"张专辑");
			int numTracks = (Integer) groups.get(groupPosition).get("NUMBER_OF_TRACKS");
			numOfTracks.setText(Integer.toString(numTracks)+"首歌曲");
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return true;
		}
		

	}

	public void onBackPressed()
    {
		getGroup().onBackPressed();
        Log.v("artistlist", "back");
    		
    }
	
	private MainActivity getGroup()
	{
		 return (MainActivity) getParent();
	}
	
	public void onStart()
    {
    	super.onStart();
    	Log.v("ArtistList", "onstart");
    }
    protected void onResume()
    {
    	super.onResume();
    	Log.v("ArtistList", "onResume");
    }
    protected void onPause()
    {
    	super.onPause();
    	Log.v("ArtistList", "onPause");
    }
    protected void onStop()
    {
    	super.onStop();
    	Log.v("ArtistList", "onstop");
    }
    protected void onDestroy()
    {
    	super.onDestroy();
    	Log.v("ArtistList", "destroy");
    	 
    }
	
}