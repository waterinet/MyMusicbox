package com.slowalker.musicbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class AlbumList extends ListActivity implements OnItemClickListener{
	
	private ListView lv;
	private List<Map<String, Object>> data;
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    lv = getListView();
	    data = new ArrayList<Map<String, Object>>();
	    Cursor cur = this.managedQuery(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, 
				          null, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
	    
		if (cur.moveToFirst())
		{
			while (!cur.isAfterLast())
			{
				Map<String, Object> map = new HashMap<String, Object>();
				String albumArtPath = cur.getString(cur.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
				map.put("ALBUM", cur.getString(cur.getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
				map.put("ARTIST", cur.getString(cur.getColumnIndex(MediaStore.Audio.Albums.ARTIST)));
				int numOfSongs = cur.getInt(cur.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS));
				map.put("NUMBER_OF_SONGS", Integer.toString(numOfSongs)+"首歌曲");
				Bitmap bm;
				if (albumArtPath == null)
				{
					bm = BitmapFactory.decodeResource(getResources(), 
							            R.drawable.default_albumart_mini);
				}
				else
				{
					bm = BitmapFactory.decodeFile(albumArtPath);
				}
				map.put("ALBUM_ART", bm);
				data.add(map);
				cur.moveToNext();
			}
		}
		MyListAdapter adapter = new MyListAdapter();
	    lv.setAdapter(adapter);
	    //lv.setCacheColorHint(Color.TRANSPARENT);
	    lv.setOnItemClickListener(this);
	    
	}
	@Override
	public void onItemClick(AdapterView<?> adView, View target, int position, long id) {
		// TODO Auto-generated method stub
	    String thisAlbum = (String) data.get(position).get("ALBUM");
		Intent intent = new Intent(this, MusicList.class);
		intent.putExtra("ClassName", "MusicList");
		intent.putExtra("album", thisAlbum);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		getGroup().nextActivity(intent);
	}
	
	private class ViewHolder
	{
		TextView album;
		TextView artist;
		TextView num_songs;
		ImageView albumArt;
	}
	private class MyListAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if (convertView == null)
			{
				convertView = LayoutInflater.from(AlbumList.this)
                                  .inflate(R.layout.album_list_item, null);
				holder = new ViewHolder();
				holder.album = (TextView) convertView.findViewById(R.id.item_album);
				holder.artist = (TextView) convertView.findViewById(R.id.itme_albumArtist);
				holder.num_songs = (TextView) convertView.findViewById(R.id.itme_album_NumOfSongs);
				holder.albumArt = (ImageView) convertView.findViewById(R.id.item_albumArt);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.album.setText((String) data.get(position).get("ALBUM"));
			holder.artist.setText( (String) data.get(position).get("ARTIST"));
			holder.num_songs.setText( (String) data.get(position).get("NUMBER_OF_SONGS"));
			holder.albumArt.setImageBitmap( (Bitmap) data.get(position).get("ALBUM_ART"));
			return convertView;
		}
		
	}
	
	public void onBackPressed()
    {
	    getGroup().onBackPressed();
        Log.v("alubmlist", "back");
    		
    }
	private MainActivity getGroup()
	{
		return (MainActivity) getParent();
	}
	
	public void onStart()
    {
    	super.onStart();
    	Log.v("albumList", "onstart");
    }
    protected void onResume()
    {
    	super.onResume();
    	Log.v("albumList", "onResume");
    }
    protected void onPause()
    {
    	super.onPause();
    	Log.v("albumList", "onPause");
    }
    protected void onStop()
    {
    	super.onStop();
    	Log.v("albumList", "onstop");
    }
    protected void onDestroy()
    {
    	super.onDestroy();
    	Log.v("albumList", "destroy");
    	 
    }

}
