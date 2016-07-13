package com.slowalker.musicbox;

import java.io.File;
import java.io.IOException;

import com.slowalker.musicbox.mood.MoodClassifyService;
import com.slowalker.musicbox.util.BackStack;
import com.slowalker.musicbox.util.MusicFileObserver;
import com.slowalker.musicbox.util.MusicFuntions;
import com.slowalker.musicbox.util.MyIntentAction;

import android.app.ActivityGroup;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MainActivity extends ActivityGroup {
	 
	 private ImageButton playBtn;
	 private ImageButton pauseBtn;
	 private ImageView sAlbumArt;
	 private TextView sTitleArtist;
	 private ProgressBar proBar;
	 private TextView currentTime;
	 private TextView duration;
	 private MusicPlayService musicService;
	 private boolean mIsBound;
     private LinearLayout container;
     private LinearLayout header;
     private MusicFileObserver mfo;
     private BackStack backStack;
    
	 public void onCreate(Bundle savedInstanceState)
     {
    	 super.onCreate(savedInstanceState);
    	 requestWindowFeature(Window.FEATURE_NO_TITLE);
    	 setContentView(R.layout.main_activity);
    	 Log.v("mian", "onCreate");
    	 findViews();
    	 init();
		 this.startService(new Intent(this, MusicPlayService.class));
		 doBindService();
		 this.startService(new Intent(this, PlayCountService.class));
		 Handler handler = new Handler();
    	 handler.postDelayed(new Runnable()
    	 {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				startService(new Intent(MainActivity.this, MoodClassifyService.class));	
				mfo.startWatching();
			}
    		 
    	 }, 1000);
    	 Intent intent = new Intent(this, MusicGrid.class);
    	 intent.putExtra("ClassName", "MusicGrid");
    	 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	 nextActivity(intent);
     }
	 
	 private void init()
	 {
		 mIsBound = false;
		 backStack = new BackStack();
		 File path = Environment.getExternalStoragePublicDirectory(
                 Environment.DIRECTORY_MUSIC);
		 mfo = new MusicFileObserver(this, path.getPath());
		 registerReceiver();
		 MusicFuntions.createDatabaseIfNotExisted(this);
	 }
	 private void findViews()
	 {
		container = (LinearLayout)findViewById(R.id.container);
		header = (LinearLayout) findViewById(R.id.header);
    	playBtn = (ImageButton)this.findViewById(R.id.sPlayBtn);
    	pauseBtn = (ImageButton)this.findViewById(R.id.sPauseBtn);
    	sAlbumArt = (ImageView)this.findViewById(R.id.sAlbumArt);
    	sTitleArtist = (TextView)this.findViewById(R.id.sTitle_artist);
    	proBar = (ProgressBar)this.findViewById(R.id.miniProgressBar);
    	currentTime = (TextView)this.findViewById(R.id.sCurrentTime);
    	duration = (TextView)this.findViewById(R.id.sDuration);
	 }
	 
	 private BroadcastReceiver mReceiver = new BroadcastReceiver()
	 {

		@Override
		public void onReceive(Context ctx, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED))
			{
				updateMiniplayer(ctx);
				Log.v("main", "adc_received");
			}
			else if (intent.getAction().equals(MyIntentAction.PLAY))
			{
				playBtn.setVisibility(View.GONE);
	 		    pauseBtn.setVisibility(View.VISIBLE);
	 		    Log.v("main", "play_received");
			}
			else if (intent.getAction().equals(MyIntentAction.PAUSE))
			{
				pauseBtn.setVisibility(View.GONE);
	 		    playBtn.setVisibility(View.VISIBLE);
	 		    Log.v("main", "pause_received");
			}
			else if (intent.getAction().equals(MyIntentAction.EXIT_APP))
			{
				mfo.stopWatching();
	    		stopService(new Intent(MainActivity.this, MusicPlayService.class));
	    		stopService(new Intent(MainActivity.this, MoodClassifyService.class));
	    		stopService(new Intent(MainActivity.this, PlayCountService.class));
	    		MainActivity.this.finish();
			}
			
		}
		 
	 };
	 private void registerReceiver()
	 {
		 IntentFilter filter = new IntentFilter();
    	 filter.addAction(Intent.ACTION_DATE_CHANGED);
    	 filter.addAction(MyIntentAction.PLAY);
    	 filter.addAction(MyIntentAction.PAUSE);
    	 filter.addAction(MyIntentAction.EXIT_APP);
    	 LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
	 }
     private void unregisterReceiver()
     {
    	 LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
     }
	 
     private void updateMiniplayer(Context ctx)
     {
    	 if (mIsBound)
    	 {
    		 Bundle bundle = musicService.getCurrentMusicInfo();
    		 if (!bundle.isEmpty())
    		 {
    			 sAlbumArt.setImageBitmap(MusicFuntions.getAlbumArtFromId(ctx, 
		                                                    bundle.getLong("album_id")));
                 sTitleArtist.setText(bundle.getString("title") + " -- " + 
	                                                        bundle.getString("artist"));
             }
    	     
    	 }
    	 proBarSetup();
     }
     
     public void nextActivity(Intent intent)
 	 {
    	 setHeaderView(intent);
    	 String name = intent.getStringExtra("ClassName");
    	 backStack.push(name);
 		 container.removeAllViews();
 		 Window next = getLocalActivityManager().startActivity(name, intent);
 		 container.addView(next.getDecorView());
     }
     private void preActivity() throws ClassNotFoundException
     {
    	 backStack.pop();
  		 String name = backStack.getTop();
  		 setHeaderView(name);
  		 String fullName = "com.slowalker.musicbox." + name;
  		 Intent intent = new Intent(this, Class.forName(fullName));
  		 if (name.equals("MusicGrid"))
  		 {
  			 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  		 }
  		 Window next = getLocalActivityManager().startActivity(name, intent);
  		 container.removeAllViews();
  		 container.addView(next.getDecorView());
     }
     private void setHeaderView(Intent intent)
     {
    	 header.setVisibility(View.VISIBLE);
    	 TextView header_name1 = (TextView) findViewById(R.id.header_name1);
		 TextView header_name2 = (TextView) findViewById(R.id.header_name2);
		 header_name2.setVisibility(View.GONE);
         String name = intent.getExtras().getString("ClassName");
         if (name.equals("MusicGrid"))
    	 {
    		 header.setVisibility(View.GONE);
    	 }
    	 else if (name.equals("AlbumList"))
    	 {
    		 header_name1.setText("专辑");
    	 }
    	 else if (name.equals("ArtistList"))
    	 {
    		 header_name1.setText("艺术家");
    	 }
    	 else if (name.equals("MoodList"))
    	 {
    		 header_name1.setText("情感列表");
    	 }
    	 else if (name.equals("MusicList"))
    	 {
    		 if (intent.hasExtra("artist"))
    		 {
    			 String thisArtist = intent.getExtras().getString("artist");
    			 String album_of_artist = intent.getExtras().getString("album_of_artist");
    			 header_name1.setText(thisArtist);
        		 header_name2.setText(album_of_artist);
        		 header_name2.setVisibility(View.VISIBLE);
    		 }
    		 else if (intent.hasExtra("album"))
    		 {
    			 String thisAlbum = intent.getExtras().getString("album");
    			 header_name1.setText(thisAlbum);
    		 }
    		 else if (intent.hasExtra("mood"))
    		 {
    			 int mood = intent.getExtras().getInt("mood");
    		     header_name1.setText(MusicFuntions.getMoodName(mood));
    		 }
    		 else if (intent.hasExtra("Favorite"))
    		 {
        		 header_name1.setText("最常播放");
    		 }
    		 else if (intent.hasExtra("Latest_added"))
    		 {
        		 header_name1.setText("最近添加");
    		 }
    		 else if (intent.hasExtra("PlayList"))
    		 {
    			 String playList = intent.getStringExtra("PlayList");
    			 if (playList.equals("CurrentList"))
    			 {
    	    		 header_name1.setText("正在播放");
    			 }
    			 else
    			 {
    	    		 header_name1.setText(playList);
    			 }
    		 }
    		 else
    		 {
	    		 header_name1.setText("所有音乐");
    		 }
    	 }
    	 
    	 
     }
     
     private void setHeaderView(String name)
     {
    	 header.setVisibility(View.VISIBLE);
    	 TextView header_name1 = (TextView) findViewById(R.id.header_name1);
    	 TextView header_name2 = (TextView) findViewById(R.id.header_name2);
    	 header_name2.setVisibility(View.GONE);
    	 if (name.equals("MusicGrid"))
    	 {
    		 header.setVisibility(View.GONE);
    	 }
    	 else if (name.equals("AlbumList"))
    	 {
    		 header_name1.setText("专辑");
    	 }
    	 else if (name.equals("ArtistList"))
    	 {
    		 header_name1.setText("艺术家");
    	 }
    	 else if (name.equals("MoodList"))
    	 {
    		 header_name1.setText("情感列表");
    	 }
     }
     
     public boolean onCreateOptionsMenu(Menu menu)
     {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_options, menu);
	    return true;
     }
     public boolean onOptionsItemSelected(MenuItem item)
     {
    	switch(item.getItemId())
    	{
    	case R.id.main_exit:
    		LocalBroadcastManager.getInstance(this).sendBroadcast(
		             new Intent(MyIntentAction.EXIT_APP));
    		mfo.stopWatching();
    		stopService(new Intent(this, MusicPlayService.class));
    		stopService(new Intent(this, MoodClassifyService.class));
    		stopService(new Intent(MainActivity.this, PlayCountService.class));
    		this.finish();
    		break;
    	}
		return true;
    	 
     }
     public void doClick(View view) throws IllegalArgumentException, 
                       SecurityException, IllegalStateException, IOException
     {
    	 Intent intent;
    	 switch(view.getId())
    	 {
    	 case R.id.sAlbumArt:
    		 intent = new Intent(this, MusicPlay.class);
    		 this.startActivity(intent);
    		 break;
    	 case R.id.sPlayBtn:
    		 if (mIsBound)
			     musicService.resumePlay();
    		 break;
    	 case R.id.sPauseBtn:
    		 if (mIsBound)
    		     musicService.pause();
    		 break;
    	 case R.id.sNextBtn:
    		 if (mIsBound)
		         musicService.next();
		     break;
    	 case R.id.sPreBtn:
    		 if (mIsBound)
    		     musicService.previous();
     	     break;
    	 }
     }
     private Handler pbHandler = new Handler();
	 private Runnable updateProBar = new Runnable()
	 {
	     public void run()
	    {
	    	 if (mIsBound)
	    	 {  
	    		 int millis = musicService.getCurrentPosition();
	    		 proBar.setProgress(millis);
	    		 currentTime.setText(MusicFuntions.formatMillis(millis));
	    		 pbHandler.postDelayed(updateProBar, 100);
	    	 }
    		 
	    }
     };
     private void proBarSetup()
     {
    	  Log.v("Main", "proBarSetup");
	      //pbHandler.removeCallbacks(updateProBar);
    	  if (mIsBound)
    	  {
    		  int millis = musicService.getDuration();
    		  proBar.setMax(millis);
    		  duration.setText(MusicFuntions.formatMillis(millis));
    		  pbHandler.removeCallbacks(updateProBar);
    		  pbHandler.post(updateProBar);
    	  }
	 }
     
     private ServiceConnection mConnection = new ServiceConnection()
 	{
     	@Override
     	public void onServiceConnected(ComponentName className, IBinder service)
     	{
     		musicService = ((MusicPlayService.musicBinder)service).getService();
     		Log.v("main", "connected");
     		mIsBound = true;
     		updateMiniplayer(MainActivity.this);
     		setPlayOrPauseBtn();
     	}

 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			// TODO Auto-generated method stub
 			musicService = null;
 			Log.v("main", "disconnected");
 		}
 	};
    private void setPlayOrPauseBtn()
    {
    	if (mIsBound)
    	{
    		if (musicService.isPlaying())
        	{
        		playBtn.setVisibility(View.GONE);
     		    pauseBtn.setVisibility(View.VISIBLE);
        	}
        	else
        	{
        		pauseBtn.setVisibility(View.GONE);
     		    playBtn.setVisibility(View.VISIBLE);
        	}
    	}
    	
    }
 	/*public Bundle getCurrenMusicInfo()
 	{
 		Bundle bundle = new Bundle();
 		if (mIsBound)
 		{
 			bundle = musicService.getCurrentMusicInfo();
 		}
 		return bundle;
 	}*/
 	public boolean isPlaying() throws Exception
 	{
 		if (mIsBound)
 		    return musicService.isPlaying();
 		else 
 			throw new Exception("MainActivity: no bound, can not get isPlaying!");
 	}
 	public long getCurrentId() throws Exception
 	{
 		if (mIsBound)
 		{
 			return musicService.getCurrentMusicId();
 		}
 		else 
 			throw new Exception("MainActivity: no bound, can not get currentId!");
 	}
 	private void doBindService()
     {
         
  	      bindService(new Intent(this, MusicPlayService.class), 
  		        			     mConnection, Context.BIND_AUTO_CREATE);
  	     
  	}
 	 private void doUnbindService()
     {
     	if (mIsBound)
     	{
     		unbindService(mConnection);
     		mIsBound = false;
     	}
     }
     
 	public void onStart()
	{
	    super.onStart();
	    pbHandler.removeCallbacks(updateProBar);
	    pbHandler.post(updateProBar);
	    Log.v("main", "onstart");
	}
 	 
    protected void onResume()
    {
    	super.onResume();
    	Log.v("main", "onResume");
    }
    protected void onPause()
    {
    	super.onPause();
    	Log.v("main", "onPause");
    }
    protected void onStop()
    {
    	super.onStop();
    	pbHandler.removeCallbacks(updateProBar);
    	Log.v("main", "onstop");
    }
    protected void onDestroy()
    {
    	super.onDestroy();
    	unregisterReceiver();
    	doUnbindService();
    	Log.v("main", "unBind");
    	Log.v("main", "destroy");
    	
    }
    public void onBackPressed()
    {
        if (backStack.getTop().equals("MusicGrid"))
    		super.onBackPressed();
		else
			try {
				preActivity();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		Log.v("MainActivity", "back");
        
    }
    
    
     
}