package com.slowalker.musicbox.util;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;

import com.slowalker.musicbox.Lyrics;
import com.slowalker.musicbox.MusicGrid;
import com.slowalker.musicbox.R;
import com.slowalker.musicbox.RangeNode;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;
/*
 * This is a holder class contains functions used by other classes
 * 
 * */

public class MusicFuntions {
	public static boolean matchMusicInfo(Context ctx, String displayName, long musicId)
	{
		long id = getMusicIdFromDisplayName(ctx, displayName);
		if ( musicId == id)
			return true;
		else 
		    return false;
	}
	
	public static long getMusicIdFromTitle(Context ctx, String title)
	{
		String projection[] = new String[] { MediaStore.Audio.Media._ID };
		ContentResolver cr = ctx.getContentResolver();
	    Cursor cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,	
	    		  projection, "title=?", new String[] {title}, null);
	    long musicId = -1; // -1 is returned when the query is failed
		if (cur.moveToFirst())
		    musicId = cur.getLong(0);
		cur.close();
	    return musicId;
	    
	}
	public static long getMusicIdFromDisplayName(Context ctx, String displayName)
	{
		String[] projection = new String[] { MediaStore.Audio.Media._ID };
		ContentResolver cr = ctx.getContentResolver();
		Cursor cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,	
	    		      projection, MediaStore.Audio.Media.DISPLAY_NAME + "=?", 
	    		      new String[] { displayName }, null);
		long musicId = -1; // -1 is returned when the query is failed
		if (cur.moveToFirst())
		    musicId = cur.getLong(0);
		cur.close();
		return musicId;
	}
    public static boolean insertToPlayList(Context ctx, Long playListId, Long musicId)
    {
    	Uri membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playListId);
    	Cursor cur = ctx.getContentResolver().query(
    			          membersUri, 
    			          null, 
    			          MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", 
    			          new String[] { Long.toString(musicId) },
    			          null );
    	int count = cur.getCount();
    	cur.close();
    	if (count > 0)
    	{
    		cur.close();
    		return false;
    	}
    	else
    	{
    		ContentValues values = new ContentValues();
    		values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, musicId);
            values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, 1);
            ctx.getContentResolver().insert(membersUri, values);
            return true;
    	}
    }
    public static void removeFromPlayList(Context ctx, Long playListId, Long musicId)
    {
    	Uri membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playListId);
    	ctx.getContentResolver().delete(membersUri, MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", 
    			new String[] {Long.toString( musicId )});
    }
    public static ArrayList<Pair<Long, String>> getAllPlayListName(Context ctx)
	{
    	ContentResolver cr = ctx.getContentResolver();
    	ArrayList<Pair<Long, String>> list = new ArrayList<Pair<Long, String>>();
		Cursor cur = cr.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, 
                new String[] { MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME }, 
                null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
		if (cur.moveToFirst())
		{
			while (!cur.isAfterLast())
			{
				Pair<Long, String> pair = 
					    new Pair<Long, String>(cur.getLong(0), cur.getString(1));
				list.add(pair);
				for (int i = list.size()-1; i > 0; --i)
				{
					String str1 = list.get(i).second;
					String str2 = list.get(i - 1).second;
					if (MusicGrid.compare(str1, str2) < 0)
					{
						Pair<Long, String> temp = list.get(i);
						list.set(i, list.get(i - 1));
						list.set(i - 1, temp);
					}
				}
				cur.moveToNext();
			}
		}
		cur.close();
		return list;
	}
    
    
    public static boolean addPlayListToMediaStore(Context ctx, String playListName)
	{
    	Cursor cur = ctx.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null,
    			        MediaStore.Audio.Playlists.NAME+"=?", new String[] { playListName }, null);
    	if (cur.getCount() != 0)
    		return false; // palyList already existed
    	cur.close();
		ContentValues values = new ContentValues();
		values.put(MediaStore.Audio.Playlists.NAME, playListName);
		values.put(MediaStore.Audio.Playlists.DATA, "");
        long dateAdded = System.currentTimeMillis();
        values.put(MediaStore.Audio.Playlists.DATE_ADDED, dateAdded);
        long dateModified = System.currentTimeMillis();
        values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, dateModified);
        //insert
        ctx.getContentResolver().insert(
        		  MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
        /*cur = ctx.getContentResolver().query(uri, null, null, null, null);        
        cur.moveToFirst();
        long playListId = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Playlists._ID));*/
        
        return true;
        
   }
    
    public static void removePlayListFromMediaStore(Context ctx, String playListName)
	{
		 ctx.getContentResolver().delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, 
				 MediaStore.Audio.Playlists.NAME + "=?", 
				 new String[] { playListName});
		
	}
    public static Bitmap getAlbumArtFromId(Context ctx, long album_id)
    {
    	ContentResolver cr = ctx.getContentResolver();
    	String mUriAlbums = "content://media/external/audio/albums";
    	String[] projection = new String[]{ "album_art" };
    	Cursor cur = cr.query(Uri.parse(mUriAlbums+"/"+Long.toString(album_id)), 
    			                       projection, null, null, null);
    	if (cur.moveToFirst())
    	{
    		String albumArtPath = cur.getString(0);
    		cur.close();
    		if (albumArtPath == null)
        	    return BitmapFactory.decodeResource(ctx.getResources(), R.drawable.default_albumart);
        	else
        	    return BitmapFactory.decodeFile(albumArtPath);
    	}
    	else
    		return null;
    	
    	
    	
	}
    
    public static long nextMusicId(Uri mUri, ArrayList<Long> musicList, int playMode)
	{
    	if (musicList.isEmpty())
    		return -1;
    	long musicId;
    	switch (playMode)
    	{
    	case MyConstant.playmode.ORDER:
    		long currentId = Long.parseLong(mUri.getLastPathSegment());
    		int currentIndex = musicList.indexOf(currentId);
    		if (currentIndex == musicList.size()-1)
    			musicId = -1;
    		else
    			musicId = musicList.get(++currentIndex);
    		break;
    	case MyConstant.playmode.SINGLE:
    		musicId = Long.parseLong(mUri.getLastPathSegment());
    		break;
    	case MyConstant.playmode.LIST_CIRCULATE:
    		long currentMusicId = Long.parseLong(mUri.getLastPathSegment());
    		int currentMusicIndex = musicList.indexOf(currentMusicId);
    		int nextMusicIndex = (currentMusicIndex == musicList.size()-1) ? 0 : ++currentMusicIndex;
    		musicId = musicList.get(nextMusicIndex);
    		break;
    	case MyConstant.playmode.SHUFFLE:
    		musicId = shuffleMusic(mUri, musicList);
    		break;
    	default:
    		musicId = -1;
    		break;
        }
    	return musicId;
	}
    public static long preMusicId(Uri mUri, ArrayList<Long> musicList, int playMode)
	{
    	if (musicList.isEmpty())
    		return -1;
    	long musicId;
    	switch (playMode)
    	{
    	case MyConstant.playmode.ORDER:
    		long currentId = Long.parseLong(mUri.getLastPathSegment());
    		int currentIndex = musicList.indexOf(currentId);
    		if (currentIndex == 0)
    			musicId = -1;
    		else
    			musicId = musicList.get(--currentIndex);
    		break;
    	case MyConstant.playmode.SINGLE:
    		musicId = Long.parseLong(mUri.getLastPathSegment());
    		break;
    	case MyConstant.playmode.LIST_CIRCULATE:
    		long currentMusicId = Long.parseLong(mUri.getLastPathSegment());
    		int currentMusicIndex = musicList.indexOf(currentMusicId);
    		int preMusicIndex = (currentMusicIndex == 0) ? musicList.size()-1 : --currentMusicIndex;
    		musicId = musicList.get(preMusicIndex);
    		break;
    	case MyConstant.playmode.SHUFFLE:
    		musicId = shuffleMusic(mUri, musicList);
    		break;
    	default:
    		musicId = -1;
    		break;
    	}
    	return musicId;
	}
    private static long shuffleMusic(Uri uri, ArrayList<Long> musicList)
    {
    	if (musicList.isEmpty())
    		return -1;
    	if (musicList.size() == 1)
    		return musicList.get(0);
    	long current = Long.parseLong(uri.getLastPathSegment());
        Random r = new Random();
        long next  = musicList.get(r.nextInt(musicList.size()));
        while (next == current)
        	next = musicList.get(r.nextInt(musicList.size()));
        return next;
    }
    public static String checkCurrentList(Context ctx, String list)
    {
    	if (list.isEmpty())
		 {
			 return list;
		 }
		 else
		 {
			 String playList[] = list.split(";");
			 StringBuilder sb = new StringBuilder();
			 for (String str : playList)
		   	 {
				 if (checkMusicId(ctx, Long.parseLong(str)))
				 {
					 sb.append(str);
					 sb.append(";");
				 }
		   	 }
			 if (sb.length() > 0)
				 sb.deleteCharAt(sb.length() - 1);
			 return sb.toString();
	    }
    }
    public static boolean checkMusicId(Context ctx, long musicId)
    {
    	Uri uri = ContentUris.withAppendedId(
			       MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId);
    	Cursor cur = ctx.getContentResolver().query(uri, null, null, null, null);
    	if (cur.moveToFirst())
    	{
    		cur.close();
    		return true;
    	}
    	else
    	{
    		cur.close();
    		return false;
    	}
    }
    public static Bundle getMusicInfoFromId(Context ctx, long musicId)
	{
		Bundle bundle = new Bundle();
		Uri mUri = ContentUris.withAppendedId(
				       MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId);
        bundle.putLong("Id", musicId);
		String[] projection = new String[]{ "title", "artist", "album", "album_id" };
		Cursor cur = ctx.getContentResolver().query(mUri, projection, null, null, null);
		if (cur.moveToFirst())
		{
			bundle.putString("title", cur.getString(0));
			bundle.putString("artist", cur.getString(1));
			bundle.putString("album", cur.getString(2));
			bundle.putLong("album_id", cur.getLong(3));
		}
		cur.close();
		return bundle;
		
	}
    
    public static long getDurationFromId(Context ctx, long musicId)
    {
    	ContentResolver cr = ctx.getContentResolver();	
    	Uri mUri = ContentUris.withAppendedId(
    			    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId);
    	String[] projection = new String[] { MediaStore.Audio.Media.DURATION };
    	Cursor cur = cr.query(mUri, projection, null, null,  null);
    	long duration = 0;
    	if (cur.moveToFirst())
    	{
    		duration = cur.getLong(0);
    	}
    	cur.close();
    	return duration;
    }
    public static String getDisplayNameFromId(Context ctx, long musicId)
    {
    	ContentResolver cr = ctx.getContentResolver();	
    	Uri mUri = ContentUris.withAppendedId(
    			    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId);
    	String[] projection = new String[] { MediaStore.Audio.Media.DISPLAY_NAME };
    	Cursor cur = cr.query(mUri, projection, null, null,  null);
    	String displayName = null;
    	if (cur.moveToFirst())
    	{
    		displayName = cur.getString(0);
    	}
    	cur.close();
    	return displayName;
    }
    
    public static int getNumInAlbumForArtist(Context ctx, String album, String artist)
    {
    	ContentResolver cr = ctx.getContentResolver();
    	Cursor cur = cr.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, 
    			                  null, "album=?", new String[] { album }, null); 
    	cur.moveToFirst();
    	int album_id = cur.getInt(cur.getColumnIndex(MediaStore.Audio.Albums._ID));
    	Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, album_id);
    	cur = cr.query(uri, null, "artist=?", new String[] { artist }, null);
    	cur.moveToFirst();
    	int retVal = cur.getInt(0);
    	cur.close();
    	return retVal;
    	
    }
    public static int getPlayListCount(Context ctx)
    {
    	ContentResolver cr = ctx.getContentResolver();
        Cursor cur = cr.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, null, null, null);
        int count = cur.getCount();
        cur.close();
        return count;
    }
    public static int getNumOfMusicInPlayList(Context ctx, long playListId)
    {
    	Uri membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playListId);
    	ContentResolver cr = ctx.getContentResolver();
    	Cursor cur = cr.query(membersUri, null, null, null, null);
    	int count = cur.getCount();
    	cur.close();
    	return count;
    }
    public static long getPlayListIdFromName(Context ctx, String playListName)
    {
    	ContentResolver cr = ctx.getContentResolver();
    	Cursor cur = cr.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, 
    			MediaStore.Audio.Playlists.NAME+"=?", new String[] { playListName }, null);
    	cur.moveToFirst();
    	long playListId = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Playlists._ID));
    	cur.close();
    	return playListId;
    }
    public static Lyrics getLyricFromId(Context ctx, long musicId) throws IOException
    {
    	Uri mUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId);
		Cursor cur = ctx.getContentResolver().query(mUri, null, null, null, null);
		if(!cur.moveToFirst())
        {
			cur.close();
			return new Lyrics(null, 0);
        }
		String musicPath = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
		int duration = cur.getInt(cur.getColumnIndex(MediaStore.Audio.Media.DURATION));
		String displayName = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
		cur.close();
		String lyricName = displayName.replaceFirst("\\.\\w{3}", ".lrc");
		String lyricPath = musicPath.replace(displayName, lyricName);
		File file  = new File(lyricPath);
		if (file.exists())
			return new Lyrics(file.getPath(), duration);
		else
			return new Lyrics(null, 0);
	}
    public static boolean insertToCurrentList(Context ctx, long musicId)
    {
    	SharedPreferences sp = ctx.getSharedPreferences("MusicBox", 0);
    	SharedPreferences.Editor se = sp.edit();
    	ArrayList<Long> list = getCurrentList(ctx);
    	if (list.contains(musicId))
    		return false;
    	else
    	{
    		String playList = sp.getString("CurrentList", "");
        	StringBuilder sb = new StringBuilder(playList);
        	if (!playList.isEmpty())
        	{
        		sb.append(";");
        	}
        	sb.append(musicId);
        	se.putString("CurrentList", sb.toString());
        	se.commit();
        	return true;
    	}
    	
    }
    public static ArrayList<Long> getCurrentList(Context ctx)
    {
    	ArrayList<Long> list = new ArrayList<Long>();
    	SharedPreferences sp = ctx.getSharedPreferences("MusicBox", Context.MODE_PRIVATE);
    	String playList = sp.getString("CurrentList", "");
    	if (!playList.isEmpty())
    	{
    		for (String s : playList.split(";"))
    			list.add(Long.parseLong(s));
    	}
    	return list;
    }
    public static void removeFromCurrentList(Context ctx, long musicId)
    {
    	SharedPreferences sp = ctx.getSharedPreferences("MusicBox", 0);
    	SharedPreferences.Editor se = sp.edit();
    	String playList = sp.getString("CurrentList", "");
    	if (playList.isEmpty())
    		return;
    	else
    	{
    		StringBuilder sb = new StringBuilder();
    		for (String str : playList.split(";"))
        	{
        		if (Long.parseLong(str) != musicId)
        		{
        			sb.append(str);
        			sb.append(";");
        		}
        	}
        	if (sb.length() > 0)
        		sb.deleteCharAt(sb.length()-1);
        	se.putString("CurrentList", sb.toString());
        	se.commit();
    	}
    	
    	
    }
    
    public static void createDatabaseIfNotExisted(Context ctx)
    {
    	SQLiteDatabase db = new DatabaseHelper(ctx, "musicbox", null, 1)
              .getReadableDatabase();
    	String sql = "CREATE TABLE IF NOT EXISTS music_mood " +
                     "(DISPLAY_NAME TEXT, ID INTEGER PRIMARY KEY, MOOD INTEGER, " +
		             "REFRAIN_START INTEGER, REFRAIN_END INTEGER, TIME_ADDED INTEGER)"; 
    	db.execSQL(sql);
    	sql = "CREATE TABLE IF NOT EXISTS play_count " +
              "(DISPLAY_NAME TEXT, ID INTEGER PRIMARY KEY, PLAY_COUNT INTEGER)"; 
    	db.execSQL(sql);
    	db.close();
   	 
    }
    public static RangeNode getRefrainFromId(Context ctx, long musicId)
    {
    	SQLiteDatabase db = new DatabaseHelper(ctx, "musicbox", null, 1)
             .getReadableDatabase();
        Cursor cur = db.query("music_mood", new String[] { "REFRAIN_START", "REFRAIN_END" }, 
               "ID=?", new String[] { Long.toString(musicId) }, 
               null, null, null);
        if (cur.moveToFirst())
        {
        	int start = cur.getInt(0);
            int end = cur.getInt(1);
            db.close();
        	return new RangeNode(start, end, true);
        }
        else
        {
        	db.close();
        	return new RangeNode(false);
        }
    }
    public static String getMoodName(int mood)
    {
    	String name = null;
    	switch(mood)
    	{
    	case 1:
    		name = "情绪高昂";
    		break;
    	case 2:
    		name = "焦虑急切";
    		break;
    	case 3:
    		name = "低沉伤感";
    		break;
    	case 4:
    		name = "欢快悠闲";
    		break;
    	}
    	return name;
    }
    
    public static int getNumOfMusicWithMood(Context ctx)
    {
    	SQLiteDatabase db = new DatabaseHelper(ctx, "musicbox", null, 1)
            .getReadableDatabase();
    	Cursor cur = db.query("music_mood", null, null, null, null, null, null);
    	int count = cur.getCount();
    	cur.close();
    	db.close();
    	return count;
    }
    public static int[] getNumOfMusicByMood(Context ctx)
    {
    	int[] num = new int[4];
    	SQLiteDatabase db = new DatabaseHelper(ctx, "musicbox", null, 1)
                      .getReadableDatabase();
    	Cursor cur = null;
    	for (int i = 0; i < 4; ++i)
    	{
    		cur = db.query("music_mood", null, "MOOD=?", 
		               new String[] { Integer.toString(i+1) }, 
		               null, null, null);
    		num[i] = cur.getCount();
    		cur.close();
    	}
	    
	    db.close();
	    return num;
    }
    public static int getMoodFromMusicId(Context ctx, long musicId)
    {
    	SQLiteDatabase db = new DatabaseHelper(ctx, "musicbox", null, 1)
                 .getReadableDatabase();
    	Cursor cur = db.query("music_mood", 
    			               new String[] { "MOOD" }, "ID=?", 
    			               new String[] { Long.toString(musicId) }, 
    			               null, null, null);
    	int mood;
    	if (cur.moveToFirst())
    	{
    		mood = cur.getInt(0);
    	}
    	else
    	{
    		mood = 0;
    	}
    	cur.close();
    	db.close();
    	return mood;
    	
    }
    
    public static int getPlayCountFromMusicId(Context ctx, long musicId)
    {
    	SQLiteDatabase db = new DatabaseHelper(ctx, "musicbox", null, 1)
             .getReadableDatabase();
    	Cursor cur = db.query("play_count", 
	               new String[] { "PLAY_COUNT" }, "ID=?", 
	               new String[] { Long.toString(musicId) }, 
	               null, null, null);
    	int count;
    	if (cur.moveToFirst())
    	{
    		count = cur.getInt(0);
    	}
    	else
    	{
    		count = 0;
    	}
    	cur.close();
    	db.close();
    	return count;
    }
    
    public static int getNumOfCurrentList(Context ctx)
    {
    	SharedPreferences sp = ctx.getSharedPreferences("MusicBox", Context.MODE_PRIVATE);
 	    int num = 0;
 	    String playList = sp.getString("CurrentList", "");
 	    playList = checkCurrentList(ctx, playList);
 	    if (playList.isEmpty())
 		    return num;
 	    else
 	    {
 		    for (char c : playList.toCharArray())
 		    	if (c == ';')
 		    		++num;
 	    }
 	    return ++num;
    }
    public static Bundle getMusicInfoToShow(Context ctx, long musicId)
    {
    	Bundle bundle = new Bundle();
    	if (musicId == -1)
    		return bundle;
    	ContentResolver cr = ctx.getContentResolver();	
    	Uri mUri = ContentUris.withAppendedId(
    			    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId);
    	String[] projection = new String[] { MediaStore.Audio.Media.TITLE,
    			                             MediaStore.Audio.Media.ARTIST,
    			                             MediaStore.Audio.Media.ALBUM,
    			                             MediaStore.Audio.Media.DURATION,
    			                             MediaStore.Audio.Media.TRACK,
    			                             MediaStore.Audio.Media.YEAR,
    			                             MediaStore.Audio.Media.DATA,
    			                             MediaStore.Audio.Media.DISPLAY_NAME,
    			                             MediaStore.Audio.Media.SIZE };
    	Cursor cur = cr.query(mUri, projection, null, null,  null);
    	if (cur.moveToFirst())
    	{
    		bundle.putString("Title", cur.getString(0));
    		bundle.putString("Artist", cur.getString(1));
    		bundle.putString("Album", cur.getString(2));
    		long duration = cur.getLong(3);
    		bundle.putString("Duration", formatMillis(duration));
    		String track = Integer.toString(cur.getInt(4));
    		if (track.equals("0"))
    			track = "未知";
    		bundle.putString("Track", track);
    		String year = Integer.toString(cur.getInt(5));
    		if (year.equals("0"))
    			year = "未知";
    		bundle.putString("Year", year);
    		bundle.putString("Path", cur.getString(6));
    		bundle.putString("Type", getType(cur.getString(7)));
    		long size = cur.getLong(8);
    		NumberFormat formatter = new DecimalFormat("0.00");
    		bundle.putString("Size", formatter.format(size * 1.0f / (1024 * 1024) ) + "MB");
    		bundle.putString("Bitrate", Long.toString((8*size/1024) / (duration/1000)) + "kbps");
    	}
    	cur.close();
    	
    	
    	mUri = ContentUris.withAppendedId(
			    MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, musicId);
    	projection = new String[] { MediaStore.Audio.Genres.NAME };
    	cur = cr.query(mUri, projection, null, null, null);
    	if (cur.moveToFirst())
    	{
    		bundle.putString("Genres", cur.getString(0));
    	}
    	else
    	{
    		bundle.putString("Genres", "未知");
    	}
    	cur.close();
    	
    	int mood = getMoodFromMusicId(ctx, musicId);
    	switch (mood)
		{
    	case 0:
    		bundle.putString("Mood", "未知");
    		break;
		case 1:
			bundle.putString("Mood", "情绪高昂");
			break;
		case 2:
			bundle.putString("Mood", "焦虑急切");
			break;
		case 3:
			bundle.putString("Mood", "低沉伤感");
			break;
		case 4:
			bundle.putString("Mood", "欢快休闲");
			break;
		}
    	
    	int count = getPlayCountFromMusicId(ctx, musicId);
    	bundle.putString("PlayCount", Integer.toString(count));
    	
    	return bundle;
    	
    }
    private static String getType(String displayName)
    {
    	int start = displayName.lastIndexOf('.');
    	return displayName.substring(start + 1);
    }
    public static String formatMillis(long millis)
    {
	   	StringBuilder sb = new StringBuilder();
	   	int inSec = (int) (millis / 1000);
	   	int min = inSec / 60;
	   	int sec = inSec % 60;
	   	sb.append(min>=10 ? min : "0"+Integer.toString(min));
	   	sb.append(":");
	   	sb.append(sec>=10 ? sec : "0"+Integer.toString(sec));
	   	return sb.toString();
   }
    
    public static ArrayList<Pair<Long, Integer>> getFavoriteMusic(Context ctx, int limit)
    {
    	ArrayList<Pair<Long, Integer>> list = new ArrayList<Pair<Long, Integer>>();
    	SQLiteDatabase db = new DatabaseHelper(ctx, "musicbox", null, 1)
            .getReadableDatabase();
    	Cursor cur = db.query("play_count", 
    			               new String[] { "ID", "PLAY_COUNT" }, "PLAY_COUNT>?", 
    			               new String[] { Integer.toString(0) }, 
    			               null, null, "PLAY_COUNT DESC");
    	if (cur.moveToFirst())
    	{
    		int count = 0;
    		while (!cur.isAfterLast())
    		{
    			Pair<Long, Integer> pair = new Pair<Long, Integer>(cur.getLong(0), cur.getInt(1));
    			list.add(pair);
    			if (++count == limit)
    				break;
    			cur.moveToNext();
    		}
    	}
    	cur.close();
    	db.close();
    	return list;
    	
    }
    public static int getNumOfFavoriteMusic(Context ctx, int limit)
    {
    	SQLiteDatabase db = new DatabaseHelper(ctx, "musicbox", null, 1)
            .getReadableDatabase();
	    Cursor cur = db.query("play_count", null, "PLAY_COUNT>?", 
			                   new String[] { Integer.toString(0) }, 
			                   null, null, null);
	    int num;
	    if (cur.getCount() < limit)
	    	num = cur.getCount();
	    else
	    	num = limit;
	    cur.close();
	    db.close();
	    return num;
    }
    public static ArrayList<Long> getLatestAddedMusic(Context ctx, long limit)
    {
    	ArrayList<Long> list = new ArrayList<Long>();
    	ContentResolver cr = ctx.getContentResolver();
    	long cmp = System.currentTimeMillis()/1000 - limit;
    	String[] projection = new String[] { MediaStore.Audio.Media._ID };
    	Cursor cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
    			               projection, 
    			               MediaStore.Audio.Media.DATE_ADDED + ">?", 
    			               new String[] { Long.toString(cmp) },  
    			               MediaStore.Audio.Media.DATE_ADDED + " DESC" );
    	if (cur.moveToFirst())
    	{
    		while (!cur.isAfterLast())
    		{
    			list.add(cur.getLong(0));
    			cur.moveToNext();
    		}
    	}
    	cur.close();
    	return list;
               
    }
    public static int getNumOfLatestAddedMusic(Context ctx, long limit)
    {
    	ContentResolver cr = ctx.getContentResolver();
    	long cmp = System.currentTimeMillis()/1000 - limit;
    	
    	Cursor cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
	                          null, 
	                          MediaStore.Audio.Media.DATE_ADDED + ">?", 
	                          new String[] { Long.toString(cmp) },  
	                          null );
    	int num = cur.getCount();
    	cur.close();
    	return num;
    }
    
    public static boolean deleteMusicFileFromId(Context ctx, long musicId)
    {
    	Uri uri = ContentUris.withAppendedId(
    			           MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId);
    	String[] projection = new String[] { MediaStore.Audio.Media.DATA };
    	Cursor cur = ctx.getContentResolver().query(uri, projection, null, null, null);
    	String musicPath = null;
    	if (cur.moveToFirst())
    	{
    		musicPath = cur.getString(0);
    	}
    	cur.close();
    	boolean retVal = false;
    	if (musicPath != null)
    	{	
    		File file = new File(musicPath);
    		if (file.exists())
    			retVal = file.delete();
    		if (retVal)
    		{
    			deleteLyricFile(musicPath);
                ctx.getContentResolver().delete(
		                		  MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
		            		      MediaStore.Audio.Media._ID + "=?", 
		            		      new String[] { Long.toString(musicId) }); 
    		}

    	}
    	return retVal;
    	
    }
    private static void deleteLyricFile(String musicPath)
    {
    	String lyricPath = null;
    	if (musicPath != null)
    	{
    		int index = musicPath.lastIndexOf('.');
    		if (index != -1)
    		{
    			lyricPath = musicPath.substring(0, index) + ".lrc";
    		}
    	}
    	if (lyricPath != null)
    	{
    		File file = new File(lyricPath);
    		if (file.exists())
    		{
    			file.delete();
    		}
    	}
    }
  
    
 }