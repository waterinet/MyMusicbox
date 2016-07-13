package com.slowalker.musicbox.mood;


import com.slowalker.musicbox.util.DatabaseHelper;
import com.slowalker.musicbox.util.MusicFuntions;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

public class MoodClassifyService extends Service {

	private DatabaseHelper helper;
	SimpleMoodClassifier smc;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	public void onCreate()
	{
		helper = new DatabaseHelper(this, "musicbox", null, 1);
		try {
			smc = new SimpleMoodClassifier(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.v("moodService", "start");
		classifyAndStore();
		synWithMediaStore();
		return START_STICKY;
	}
	public void onDestroy()
	{
		Log.v("MoodService", "destroy");
	}
	private void synWithMediaStore()
	{
		Log.v("MoodService", "syn_start");
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cur = db.query("music_mood", 
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
					deleteFromDatabase(db, cur.getString(0));
				}
				cur.moveToNext();
			}
		}
		cur.close();
		db.close();
		Log.v("MoodService", "syn_end");
	}
	/*private boolean checkFeatureFile()
	{
		
		String path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC)+"/features";
		File featureFile = new File(path);
		if (!featureFile.exists())
			return false;
		SharedPreferences sp = this.getSharedPreferences("MusicBox", MODE_PRIVATE);
		SharedPreferences.Editor se = sp.edit();
		long storedLastModified = sp.getLong("LastModified", 0);
		long lastModified = featureFile.lastModified();
		if (lastModified > storedLastModified)
		{
			se.putLong("LastModified", lastModified);
			se.commit();
			return true;
		}
		return false;
	}*/
	private void classifyAndStore()
	{
		Log.v("MoodService", "classify_start");
		MusicFeaturesProvider mfp = null;
		try {
			mfp = new MusicFeaturesProvider();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SQLiteDatabase db = helper.getWritableDatabase();
	    while (mfp.hasNext())
		{
			Pair<String, FeatureNode> pair = mfp.next();
			if (!isMusicExistedInDB(db, pair.first))
			{
				try {
					int mood = smc.classify(pair.second.features);
					insertToDatabase(db, pair.first, mood, pair.second.start, pair.second.end);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			    }
		    }
		}
	    
	    db.close();
		Log.v("MoodService", "classify_end");
	}
	private void insertToDatabase(SQLiteDatabase db, String displayName, 
			   int mood, int start, int end) throws Exception
	{
		ContentValues cv = new ContentValues();
		long musicId = MusicFuntions.getMusicIdFromDisplayName(this, displayName);
		if (musicId != -1) // musicId found, or just ignore
		{
			cv.put("DISPLAY_NAME", displayName);
			cv.put("ID", musicId);
			cv.put("MOOD", mood);
			cv.put("REFRAIN_START", start);
			cv.put("REFRAIN_END", end);
			cv.put("TIME_ADDED", System.currentTimeMillis());
			if (db.insert("music_mood", null, cv) == -1)
				throw new Exception("Fail to insert into table music_mood!");
			
		}
		
	}
	private void deleteFromDatabase(SQLiteDatabase db, String displayName)
	{
		db.delete("music_mood", "DISPLAY_NAME=?", new String[] { displayName }); 
		 
	}
	
	private boolean isMusicExistedInDB(SQLiteDatabase db, String displayName)
    {
    	Cursor cur = db.query("music_mood", null, 
                              "DISPLAY_NAME=?", new String[] { displayName }, 
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
	
	

}
