package com.slowalker.musicbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.slowalker.musicbox.util.DatabaseHelper;
import com.slowalker.musicbox.util.MusicFuntions;
import com.slowalker.musicbox.util.MyIntentAction;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class PlayCountService extends Service{
    class Event
    {
    	public String name;
    	public long time;
    	public long musicId;
    	public boolean isPlaying;
    	public Event(String name, long time, long musicId)
    	{
    		this.name = name;
    		this.time = time;
    		this.musicId = musicId;
    	}
    	public Event(String name, long time, long musicId, boolean isPlaying)
    	{
    		this.name = name;
    		this.time = time;
    		this.musicId = musicId;
    		this.isPlaying = isPlaying;
    	}
    }
    private ArrayList<Event> list;
    private int nextStart;
    private Map<Long, Integer> result;
    private DatabaseHelper helper;
    
    @Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	public void onCreate()
	{
		Log.v("playcout", "oncreate");
		registerReceiver();
		list = new ArrayList<Event>();
		result = new HashMap<Long, Integer>();
		nextStart = 0;
		helper = new DatabaseHelper(this, "musicbox", null, 1);
		
		
	}
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		synWithMediaStore();
		return START_STICKY;
	}
	
	private void synWithMediaStore()
	{
		Log.v("playCount", "syn_start");
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cur = db.query("play_count", 
				               new String[] { "DISPLAY_NAME", "ID" }, 
				               null, 
				               null, 
				               null, 
				               null, 
				               null);
		if (cur.moveToFirst())
		{
			while (!cur.isAfterLast())
			{
				if (!MusicFuntions.matchMusicInfo(this, cur.getString(0), cur.getLong(1)))
				{
					deleteFromDatabase(db, cur.getLong(1));
				}
				cur.moveToNext();
			}
		}
		cur.close();
		db.close();
		Log.v("playCount", "syn_end");
	}
	private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {

		@Override
		public void onReceive(Context ctx, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED))
			{
				Log.v("playcount", "adc_received");
				long currentId = intent.getExtras().getLong("MusicId");
				boolean isPlaying = intent.getExtras().getBoolean("IsPlaying");
				Event event = new Event("change", System.currentTimeMillis(), 
						                                 currentId, isPlaying);
				list.add(event);
                getResult(analysisEventList());
				
			}
			else if (intent.getAction().equals(MyIntentAction.PLAY))
			{
				Log.v("playcount", "play_received");
				long currentId = intent.getExtras().getLong("MusicId");
				Event event = new Event("play", System.currentTimeMillis(), currentId);
				list.add(event);
			}
			else if (intent.getAction().equals(MyIntentAction.PAUSE))
			{
				Log.v("playcount", "pause_received");
				long currentId = intent.getExtras().getLong("MusicId");
				Event event = new Event("pause", System.currentTimeMillis(), currentId);
				list.add(event);
			}
			
		}
    	
    };
    private void storeResult()
    {
    	if (result.isEmpty())
    		return;
    	SQLiteDatabase db = helper.getWritableDatabase();
    	for (long id : result.keySet())
	    {
	    	if (isMusicExistedInDB(db, id))
	    	{
	    		int count = getCount(db, id);
	    		updateDatabase(db, id, count + result.get(id));
	    	}
	    	else
	    	{
	    	    try {
					insertToDatabase(db, id, result.get(id));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	    	}
	    }
    	db.close();
	    result.clear();
    }
    private int getCount(SQLiteDatabase db, long id)
    {
    	Cursor cur = db.query("play_count", new String[] { "PLAY_COUNT" }, 
                "ID=?", new String[] { Long.toString(id) }, 
                null, null, null);
    	int count = 0;
    	if (cur.moveToFirst())
    	{
    		count = cur.getInt(0);
    	}
    	cur.close();
    	return count;
    }
    private void insertToDatabase(SQLiteDatabase db, long id, long count) throws Exception
	{
		String displayName = MusicFuntions.getDisplayNameFromId(this, id);
		if (displayName != null)
		{
			ContentValues cv = new ContentValues();
			cv.put("DISPLAY_NAME", displayName);
			cv.put("ID", id);
			cv.put("PLAY_COUNT", count);
			if (db.insert("play_count", null, cv) == -1)
				throw new Exception("Fail to insert into table play_count");
		}
		
	}
    private void deleteFromDatabase(SQLiteDatabase db, Long id)
	{
		db.delete("play_count", "ID=?", new String[] { Long.toString(id) });
		 
	}
    private void updateDatabase(SQLiteDatabase db, long id, long count)
    {
    	String displayName = MusicFuntions.getDisplayNameFromId(this, id);
    	if (displayName != null)
    	{
    		ContentValues cv = new ContentValues();
        	cv.put("DISPLAY_NAME", displayName);
    		cv.put("ID", id);
    		cv.put("PLAY_COUNT", count);
        	db.update("play_count", cv, "ID=?", new String[] { Long.toString(id)});
    	}
    }
    private boolean isMusicExistedInDB(SQLiteDatabase db, long id)
    {
    	Cursor cur = db.query("play_count", null, 
                              "ID=?", new String[] { Long.toString(id) }, 
                              null, null, null);
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
    private void getResult(long musicId)
    {
    	if (musicId != -1)
    	{
    		if (!result.containsKey(musicId))
    		{
    			result.put(musicId, 1);
    		}
    		else
    		{
    			int count = result.get(musicId);
    			result.put(musicId, ++count);
    			
    		}
    		Bundle bundle = MusicFuntions.getMusicInfoFromId(this, musicId);
    		Log.v("playCount", bundle.getString("title") + " +1");
    		
    		if (result.size() > 4)
    		{
    			try {
					storeResult();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.v("PlayCount", "storeResult");
    		}
    	}
    	else
    	{
    		Log.v("playCount", "not count in");
    	}
    	
    }
    private long analysisEventList()
    {
    	if (!list.get(nextStart).name.equals("change"))
    	{
    		nextStart = indexOf(nextStart + 1, "change");
    		if (nextStart == -1)
    			nextStart = list.size() - 1;
    		return -1;
    	}
    	int changePoint = indexOf(nextStart + 1, "change");
    	if (changePoint != -1)
    	{
    		int start = nextStart;
    		int end = changePoint;
    		nextStart = changePoint;
    		long currentId = list.get(start).musicId;
    		long playTime = 0;
    		for (int i = start + 1; i <= end; ++i)
    		{
    			Event event = list.get(i);
    			Event preEvent = list.get(i - 1);
    			if (i < end && event.musicId != currentId)
    				return -1;
    			if (event.name.equals("play"))
    				;
    			else if (event.name.equals("pause"))
    			{
    				playTime += event.time - preEvent.time;
    			}
    			else   // "change"
    			{
    				if (preEvent.name.equals("play"))
    				{
    					playTime += event.time - preEvent.time;
    				}
    				else if (preEvent.name.equals("change"))
    				{
    					if (preEvent.isPlaying)
    					{
    						playTime += event.time - preEvent.time;
    					}
    				}
    				
    		    }
    			
    		}
    		long duration = MusicFuntions.getDurationFromId(this, currentId);
    		if (duration > 0)
    		{
    			long temp = (long) (duration * 0.5);
    			if (playTime > temp)
    			{
    				return currentId;
    			}
    		}
    	}
    	return -1;
    }
    private int indexOf(int offset, String eventName)
    {
    	if (offset >= list.size())
    		return -1;
    	for (int i = offset; i < list.size(); ++i)
    	{
    		if (list.get(i).name.equals(eventName))
    			return i;
    	}
    	return -1;
    }
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
    public void onDestroy()
    {
    	Log.v("PlayCount", "destroy");
    	unregisterReceiver();
    	try {
			storeResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

}
