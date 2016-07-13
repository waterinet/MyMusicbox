package com.slowalker.musicbox;
import java.io.IOException;
import java.util.ArrayList;

import com.slowalker.musicbox.util.MusicFuntions;
import com.slowalker.musicbox.util.MyIntentAction;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MusicPlayService extends Service{

	private MediaPlayer mediaPlayer;
	private Uri mUri;
	private NotificationManager nManager;
	private Notification notification;
	private boolean isForeground;
	private Lyrics lyric;
    private SharedPreferences sp;
    private ArrayList<Long> musicList;
    private int lastPosition;
    private final IBinder mBinder = new musicBinder();
    private LocalBroadcastManager mBroadcastManager;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private int playMode;
    private boolean refrain;
    private Equalizer mEqualizer;
    private short eqType;
    //private Handler pHandler;
    private RefrainEndListener rel = null;
    public class musicBinder extends Binder
    {
    	MusicPlayService getService()
    	{
    		return MusicPlayService.this;
    	}
    }
	@Override
	public IBinder onBind(Intent arg0) 
	{
		Log.v("fromService", "onBind");
		return mBinder;    
	}
	public boolean onUnbind(Intent intent)
	{
		Log.v("fromService", "unBind");
		return true;
	}
	public void onCreate()
	{
		Log.v("fromService", "oncreate");
		init();
		getLastPlayInfo();
		prepareLastMusic();
		setListener();
	}
	private void prepareLastMusic()
	{
		if (mUri != null)
		{
			try {
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setDataSource(this, mUri);
				mediaPlayer.prepare();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setupEqualizer();
			mediaPlayer.seekTo(lastPosition);
			if (refrain)
			{
				playRefrain();
			}
		}
	}
	
	private void init()
	{
		mediaPlayer = null;
		try {
			lyric = new Lyrics(null, 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		isForeground = false;
		mEqualizer = null;
		musicList = new ArrayList<Long>();
		mBroadcastManager = LocalBroadcastManager.getInstance(this);
		sp = getSharedPreferences("MusicBox", MODE_PRIVATE);
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() { 
			  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) 
			  { 
				 
				  if (key.equals("CurrentList"))
				  {
					  String currentList = sp.getString(key, "");
					  if (!currentList.isEmpty())
					  {
						  musicList.clear();
						  for (String str : currentList.split(";"))
						  {
							  musicList.add(Long.parseLong(str));
						  }
						  Log.v("fromService", "currentListUpdated");
					  }
					  Log.v("fromService", "prefsChanged");
				  }
				  else if (key.equals("EqType"))
				  {
					  eqType = (short) prefs.getInt("EqType", 0);
					  if (mEqualizer != null)
					  {
						  mEqualizer.usePreset(eqType);
						  Log.v("fromService", "EqTypeUpdated");
					  }
				  }
				 
			  } 
		};
		sp.registerOnSharedPreferenceChangeListener(listener);
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		Log.v("fromService", "onstart");
		if (intent != null)
		{
			String action = intent.getAction();
			if (action != null && action.equals(MyIntentAction.MUSIC_SELECTED_IN_LIST))
			{
				Uri uri = intent.getData();
				if (uri.getLastPathSegment().equals(mUri.getLastPathSegment()))
				{
					if (isPlaying())
				        pause();
					else
						resumePlay();
				}
				else
				{
				    playNewMusic(uri);
				}
				
			}
			else if (action != null && action.equals(MyIntentAction.SEEK_TO_FROM_LYRIC))
			{
				int timeToSeek = intent.getExtras().getInt("SeekTo");
				mediaPlayer.seekTo(timeToSeek);
				
			}
		}
		return START_REDELIVER_INTENT;
	}
	public void onDestroy()
	{
		if (mediaPlayer != null)
		{
			pause();
			if (mEqualizer != null)
			    mEqualizer.release();
			if (rel != null && rel.isRunning())
				rel.stopListening();
			SharedPreferences.Editor se = sp.edit();
			se.putInt("LastPosition", mediaPlayer.getCurrentPosition());
			long currentMusicId = Long.parseLong(mUri.getLastPathSegment());
			se.putLong("LastMusicId", currentMusicId);
			se.putInt("PlayMode", playMode);
			se.putBoolean("Refrain", refrain);
			se.commit();
			stop();
			mediaPlayer.release();
		}
		Log.v("fromService", "destroy");
	}
	
	private void startForeground()
	{
		Bundle bundle = getCurrentMusicInfo();
		String musicInfo = bundle.getString("title") + "--" + bundle.getString("artist");
		notification = new Notification(R.drawable.normal_label_mini, 
				musicInfo, System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, MusicPlay.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, "MusicBox", musicInfo, pendingIntent);
		if (isForeground)
		{
			nManager.notify(1, notification);
		}
		else
		{
			startForeground(1, notification);
			isForeground = true;
		}
	}
	private void stopForground()
	{
		this.stopForeground(true);
		isForeground = false;
	}
	
	private void getLastPlayInfo()
    {
		//if no last play info existed, use first music in the DB
		 SharedPreferences.Editor se = sp.edit();
		 String list = sp.getString("CurrentList", "");
		 list = MusicFuntions.checkCurrentList(this, list);
		 if (list.isEmpty())
		 {
			 Cursor cur = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
                     new String[] { MediaStore.Audio.Media._ID }, 
                     null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			 if (cur.moveToFirst())
			 {
				 long id = cur.getLong(0);
				 musicList.add(id);
				 se.putString("CurrentList", Long.toString(id));
				 se.commit();
			 }
			 cur.close();
		 }
		 else
		 {
			 String playList[] = list.split(";");
			 for (String str : playList)
		   	 {
		   		 musicList.add(Long.parseLong(str));
		   	 }
		 }
		 lastPosition = sp.getInt("LastPosition", 0);
		 if (musicList.isEmpty())
		 {
			 mUri = null;
			 lastPosition = 0;
		 }
		 else
		 {
			 long lastId = sp.getLong("LastMusicId", musicList.get(0));
			 if (lastId != musicList.get(0))
			 {
				 if (!MusicFuntions.checkMusicId(this, lastId))
				 {
					 lastId = musicList.get(0);
					 lastPosition = 0;
				 }
			 }
			 mUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, lastId);
		 }
		 findLyric();
		 playMode = sp.getInt("PlayMode", 0);
		 refrain = sp.getBoolean("Refrain", false);
		 eqType = (short) sp.getInt("EqType", 0);
    }
	
	private void playNewMusic(Uri uri)
	{
		if (mediaPlayer != null)
		{
			mediaPlayer.reset();
			mUri = uri;
			findLyric();
			try {
				mediaPlayer.setDataSource(this, mUri);
				mediaPlayer.prepare();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setupEqualizer();
			if (refrain)
			{
				playRefrain();
			}
			mBroadcastManager.sendBroadcast(new Intent(Intent.ACTION_DATE_CHANGED)
		        .putExtra("MusicId", Long.parseLong(mUri.getLastPathSegment()))
		        .putExtra("IsPlaying", isPlaying()));
		    Log.v("fromService", "adc_broadcast");
			
		    mediaPlayer.start();
			startForeground();
			mBroadcastManager.sendBroadcast(new Intent(MyIntentAction.PLAY)
			    .putExtra("MusicId", Long.parseLong(mUri.getLastPathSegment())));
			Log.v("fromService", "play_broadcast");
		}
	}
	
	public void resumePlay()
	{
		if (mediaPlayer != null && !mediaPlayer.isPlaying())
		{
			mediaPlayer.start();
			startForeground();
			mBroadcastManager.sendBroadcast(new Intent(MyIntentAction.PLAY)
			   .putExtra("MusicId", Long.parseLong(mUri.getLastPathSegment())));
			Log.v("fromService", "play_broadcast");
		}
	}
	public void pause()
	{
		if (mediaPlayer != null && mediaPlayer.isPlaying())
		{
			mediaPlayer.pause();
			stopForground();
			mBroadcastManager.sendBroadcast(new Intent(MyIntentAction.PAUSE)
			   .putExtra("MusicId", Long.parseLong(mUri.getLastPathSegment())));
			Log.v("fromService", "pause_broadcast");
		}
	}
	
	public void stop()
	{
		if (mediaPlayer != null)
		{
			mediaPlayer.stop();
			stopForground();
		}
	}
	public void next() throws IllegalArgumentException, SecurityException, 
	                          IllegalStateException, IOException
	{
		long nextMusicId = MusicFuntions.nextMusicId(mUri, musicList, playMode);
		if (nextMusicId != -1)
		{
			mUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
	    			 nextMusicId);
			changeMusic(mUri);
		}
    }
	public void previous() throws IllegalArgumentException, SecurityException, 
	                               IllegalStateException, IOException
	{
		long preMusicId = MusicFuntions.preMusicId(mUri, musicList, playMode);
		if (preMusicId != -1)
		{
			mUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
	    			 preMusicId);
			changeMusic(mUri);
		}
    	
    }
	private void changeMusic(Uri uri) throws IllegalArgumentException,
        SecurityException, IllegalStateException, IOException
	{
		if (mediaPlayer != null)
		{
			boolean isChangeWhilePlaying = mediaPlayer.isPlaying();
			mUri = uri;
			findLyric();
			lyric = MusicFuntions.getLyricFromId(this, Long.parseLong(mUri.getLastPathSegment()));
			mediaPlayer.reset();
			mediaPlayer.setDataSource(this, mUri);
			mediaPlayer.prepare();
			setupEqualizer();
			if (refrain)
			{
				playRefrain();
			}
			if (isChangeWhilePlaying)
			{   
				mediaPlayer.start();
				startForeground();
			}
			mBroadcastManager.sendBroadcast(new Intent(Intent.ACTION_DATE_CHANGED)
    	       .putExtra("MusicId", Long.parseLong(mUri.getLastPathSegment()))
    	       .putExtra("IsPlaying", isPlaying()));
			Log.v("fromService", "adc_broadcast");
	    }
	}
	
	public boolean isNull()
	{
		if (mediaPlayer == null)
			return true;
		else
			return false;
	}
	public int getCurrentPosition()
	{
		if (mediaPlayer != null)
		{
			return mediaPlayer.getCurrentPosition();
		}
		else
		{
			return 0;
		}
		
	}
	public void seekTo(int position)
	{
		if (mediaPlayer != null)
		{
			mediaPlayer.seekTo(position);
		}
	}
	public int getDuration()
	{
		if (mediaPlayer != null)
		{
			return mediaPlayer.getDuration();
		}
		else
		{
			return 0;
		}
	}
	public boolean isPlaying()
	{
		if (mediaPlayer != null)
		{
			return mediaPlayer.isPlaying();
		}
		else
		{
			return false;
		}
	}
	public Bundle getCurrentMusicInfo()
	{
		Bundle bundle = new Bundle();
		if (mUri != null)
		{
	        long musicId = Long.parseLong(mUri.getLastPathSegment());
			bundle = MusicFuntions.getMusicInfoFromId(this, musicId);
		}
		return bundle;
		
	}
	public long getCurrentMusicId()
	{
		if (mUri != null)
		{
			return Long.parseLong(mUri.getLastPathSegment());
		}
		else
		{
			return -1;
		}
	}
	public int getAudioSessionId()
	{
		if (mediaPlayer != null)
		{
			return mediaPlayer.getAudioSessionId();
		}
		else
		{
			return 0; // error occured
		}
		
	}
	
	public int getPlayMode()
	{
		return playMode;
	}
	
	public void setPlayMode(int mode)
	{
		playMode = mode;
	}
	public boolean getRefrainMode()
	{
		return refrain;
	}
	public void setRefrainMode(boolean mode)
	{
		refrain = mode;
	}
	/*public boolean testAndSetRerainMode()
	{
		boolean retVal = refrain;
		refrain = !refrain;
		return retVal;
	}*/
	public Lyrics getLyric()
	{
		return lyric;
	}
	
	private void findLyric() 
	{
		if (mUri != null)
		{
			try {
				lyric = MusicFuntions.getLyricFromId(this, Long.parseLong(mUri.getLastPathSegment()));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	private void setListener()
	{
		if (mediaPlayer != null)
		{
			mediaPlayer.setOnCompletionListener(new OnCompletionListener()
	    	{

				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					long nextMusicId = MusicFuntions.nextMusicId(mUri, musicList, playMode);
					if (nextMusicId != -1) //when -1 returned , stop playing
					{
						mUri = ContentUris.withAppendedId(
								MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, nextMusicId);
						playNewMusic(mUri);
					}
					else
					{
						if (!musicList.isEmpty())
						{
							Uri uri = ContentUris.withAppendedId(
									MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicList.get(0));
							try {
								changeMusic(uri);
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (SecurityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							mBroadcastManager.sendBroadcast(new Intent(MyIntentAction.PAUSE)
						      .putExtra("MusicId", Long.parseLong(mUri.getLastPathSegment())));
							Log.v("fromService", "pause_broadcast");
						}
					}
					mediaPlayer.setOnCompletionListener(this);
					Log.v("fromService", "setListener");
				}
	    		
	    	});
		}
	}
	
	private void playRefrain()
	{
		if (mUri != null)
		{
			long musicId = Long.parseLong(mUri.getLastPathSegment());
			RangeNode node = MusicFuntions.getRefrainFromId(
	                                   getApplicationContext(), musicId);
			if (node.isExisted)
			{
				setRefrainEndListener(node.start, node.end);
			}
		}
		
	}
	private void setRefrainEndListener(int start, int end)
	{
		if (rel == null)
		{
			rel = new RefrainEndListener(mediaPlayer, start, end);
		}
		else
		{
			rel.updateListener(start, end);
		}
		rel.startListening();
	}
	
	private void setupEqualizer() {
        // Create the Equalizer object (an AudioEffect subclass) and attach it to our media player,
        // with a default priority (0).
        mEqualizer = new Equalizer(0, getAudioSessionId());
        mEqualizer.setEnabled(true);
        mEqualizer.usePreset(eqType);
    }
	public CharSequence[] getEqPresetNames()
	{
		
		CharSequence[] names = new CharSequence[]{};
		if (mEqualizer != null)
		{
			short num = mEqualizer.getNumberOfPresets();
			names = new CharSequence[num];
			for (short i = 0; i < num; ++i)
			{
				names[i] = mEqualizer.getPresetName(i);
			}
		}
		return names;
	}
	
	
	
}
