package com.slowalker.musicbox;
    
import java.io.IOException;
import java.util.ArrayList;

import com.slowalker.musicbox.util.MusicFuntions;
import com.slowalker.musicbox.util.MyConstant;
import com.slowalker.musicbox.util.MyIntentAction;
import com.slowalker.musicbox.view.AttrDialog;
import com.slowalker.musicbox.view.EqPresetDialog;
import com.slowalker.musicbox.view.LyricView;
import com.slowalker.musicbox.view.VisualizerView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
public class MusicPlay extends Activity implements OnGestureListener 
{
    private SeekBar mSeekbar;
    private ImageButton playBtn;
    private ImageButton pauseBtn;
    private TextView current_playtime;
    private TextView duration;
    private TextView title;
	private TextView artist;
	private TextView album;
	private ImageView album_art;
	private LyricView lyric;
	private TextView miniLyric;
	private ImageButton mode;
	private GestureDetector detector;
	private ViewFlipper flipper;
	private VisualizerView visual;
	private Visualizer mVisualizer;
	private boolean mIsBound;
	private int currentView;
    private MusicPlayService musicService;
    private int playMode;
    private boolean refrain;
    //private Display display; 
  
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.music_play);
        findViews();
        init();
        startService(new Intent(this, MusicPlayService.class));
    	doBindService();
    	Log.v("musicplay", "oncreate");
    	
    }
    
    private ServiceConnection mConnection = new ServiceConnection()
	{
    	@Override
    	public void onServiceConnected(ComponentName className, IBinder service)
    	{
    		musicService = ((MusicPlayService.musicBinder)service).getService();
    		Log.v("musicplay", "connected");
    		mIsBound = true;
    		playMode = musicService.getPlayMode();
    		refrain = musicService.getRefrainMode();
    		setViewAttr();
    		Bundle bundle = musicService.getCurrentMusicInfo();
			showMusicInfo(bundle.getString("title"), bundle.getString("artist"),
					      bundle.getString("album"));
			seekBarSetup();
			showViewOfFlipper();
    	}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			musicService = null;
			Log.v("musicplay", "disconnected");
		}
   };
   
    private void init()
    {
    	mIsBound = false;
    	mVisualizer = null;
    	//display = getWindowManager().getDefaultDisplay(); 
    	getLastView();
    	mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    		@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser == true)
				{	
					if (mIsBound)
					    musicService.seekTo(progress);
					Log.v("Seekbar", Integer.toString(progress));
				}
				
			}
		});
    	/*View parent = (View) mSeekbar.getParent();
    	parent.post(new Runnable()
    	{
    		@Override
			public void run() {
				// TODO Auto-generated method stub
				Rect bounds = new Rect();
		        mSeekbar.getHitRect(bounds);
		        bounds.bottom += 6;
		        bounds.top -= 6;
		        TouchDelegate touchDelegate = 
		        	    new TouchDelegate(bounds, mSeekbar);
		                   
                ((View) mSeekbar.getParent()).setTouchDelegate(touchDelegate);
		        
			}
    		
    	});*/
    	detector = new GestureDetector(this);
    	registerReceiver();
    }
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
    private void getLastView()
    {
    	SharedPreferences sp = getSharedPreferences("MusicBox", MODE_PRIVATE);
    	currentView = sp.getInt("LastViewOfMusicPlayFlipper", 0);
    	if (currentView == 1)
    	    flipper.showNext();
    	if (currentView == 2)
    	{
    		flipper.showNext();
    		flipper.showNext();
    	}
    }
    private void findViews()
    {
    	playBtn = (ImageButton)this.findViewById(R.id.playBtn);
    	pauseBtn = (ImageButton)this.findViewById(R.id.pauseBtn);
    	mSeekbar = (SeekBar)this.findViewById(R.id.musicSeekbar);
    	current_playtime = (TextView)this.findViewById(R.id.current_playtime);
    	duration = (TextView)this.findViewById(R.id.duration);
    	title = (TextView)this.findViewById(R.id.title);
    	artist = (TextView)this.findViewById(R.id.artist);
    	album = (TextView)this.findViewById(R.id.album);
    	flipper = (ViewFlipper) this.findViewById(R.id.visualFlipper);
    	album_art = (ImageView)flipper.findViewById(R.id.albumArt);
    	lyric = (LyricView) flipper.findViewById(R.id.lyric);
    	miniLyric = (TextView) this.findViewById(R.id.miniLyric);
    	visual = (VisualizerView) flipper.findViewById(R.id.visual);
    	mode = (ImageButton) this.findViewById(R.id.playMode);
    	
    }
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {

		@Override
		public void onReceive(Context ctx, Intent intent) {
			// TODO Auto-generated method stub
	        
			if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED))
			{
				if (mIsBound)
				{
					Bundle bundle = musicService.getCurrentMusicInfo();
					showMusicInfo(bundle.getString("title"), bundle.getString("artist"),
							      bundle.getString("album"));
				}
				updateViewOfFlipper();
				seekBarSetup();
                Log.v("musicplay", "adc_received");
			}
			else if (intent.getAction().equals(MyIntentAction.PLAY))
			{
				playBtn.setVisibility(View.GONE);
	 		    pauseBtn.setVisibility(View.VISIBLE);
	 		    resumeViewOfFlipper();
	 		    sbHandler.removeCallbacks(updateSeekbar);
	 		    sbHandler.post(updateSeekbar);
	 		    Log.v("musicplay", "play_received");
			}
			else if (intent.getAction().equals(MyIntentAction.PAUSE))
			{
				pauseBtn.setVisibility(View.GONE);
	 		    playBtn.setVisibility(View.VISIBLE);
	 		    pauseViewOfFlipper();
	 		    sbHandler.removeCallbacks(updateSeekbar);
	 		    Log.v("musicplay", "pause_received");
			}
			else if (intent.getAction().equals(MyIntentAction.EXIT_APP))
			{
				MusicPlay.this.finish();
			}
			
			
		}
    	
    };
  
   private void setViewAttr()
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
    	
       switch (playMode)
       {
    	case MyConstant.playmode.ORDER:
    		mode.setImageResource(R.drawable.order);
    		break;
    	case MyConstant.playmode.SINGLE:
    		mode.setImageResource(R.drawable.single);
    		break;
    	case MyConstant.playmode.LIST_CIRCULATE:
    		mode.setImageResource(R.drawable.list_circulate);
    		break;
    	case MyConstant.playmode.SHUFFLE:
    		mode.setImageResource(R.drawable.shuffle);
    		break;
       }
       if (refrain)
       {
    		mSeekbar.setEnabled(false);
    		lyric.setRefrain(false);
       }
   }
    
    private void showViewOfFlipper()
    {
    	switch (currentView)
    	{
    	 case 0: //album_art
    		 if (mIsBound)
    		 {
    			 long album_id = musicService.getCurrentMusicInfo().getLong("album_id");
                 album_art.setImageBitmap(MusicFuntions.getAlbumArtFromId(this, album_id));
    		 }
    		 miniLyricSetup();
             break;
    	 case 1: //lyric
    		 lyricSetup();
    		 miniLyric.setVisibility(View.GONE);
    		 break;
    	 case 2: //visual
    		 VisualizerSetup();
    		 miniLyricSetup();
    		 break;
    	}
    }
    private void updateViewOfFlipper()
    {
    	switch (currentView)
    	{
    	 case 0: //album_art
    		 if (mIsBound)
    		 {
    			 long album_id = musicService.getCurrentMusicInfo().getLong("album_id");
                 album_art.setImageBitmap(MusicFuntions.getAlbumArtFromId(this, album_id));
    		 }
    		 break;
    	 case 1: //lyric
    		 lyricSetup();
    		 break;
    	 case 2: //visual
    		 VisualizerSetup();
    		 break;
    	}
    }
    private void pauseViewOfFlipper()
    {
    	switch (currentView)
    	{
    	 case 0: //album_art
    		 lyHandler.removeCallbacks(updateMiniLyric);
    		 break;
    	 case 1: //lyric
    		 lyHandler.removeCallbacks(updateLyricText);
    		 break;
    	 case 2: //visual
    		 if (mVisualizer != null)
    		     mVisualizer.setEnabled(false);
    		 lyHandler.removeCallbacks(updateMiniLyric);
    		 break;
    	}
    }
    private void resumeViewOfFlipper()
    {
    	switch (currentView)
    	{
    	 case 0: //album_art
    		 lyHandler.removeCallbacks(updateMiniLyric);
    		 lyHandler.post(updateMiniLyric);;
             break;
    	 case 1: //lyric
    		 lyHandler.removeCallbacks(updateLyricText);
    		 lyHandler.post(updateLyricText);
    		 break;
    	 case 2: //visual
    		 lyHandler.removeCallbacks(updateMiniLyric);
    		 lyHandler.post(updateMiniLyric);
    		 if (mVisualizer != null)
    		     mVisualizer.setEnabled(true);
    		 break;
    	}
    }
    private void showMusicInfo(String mTitle, String mArtist, String mAlbum)
    {
    	title.setText(mTitle);
    	artist.setText(mArtist);
    	album.setText(mAlbum);
    }
    
    public void doClick(View view) throws IllegalStateException, IOException 
    {
    	switch(view.getId()) {
        case R.id.playBtn:
        	if (mIsBound)
                musicService.resumePlay();
        	break;
        case R.id.pauseBtn:
        	if (mIsBound)
        	    musicService.pause();
            break;
        case R.id.previous:
        	if (mIsBound)
        	    musicService.previous();
        	break;
        case R.id.next:
        	if (mIsBound)
        	    musicService.next();
        	break;
        case R.id.back:
        	this.startActivity(new Intent(this, MainActivity.class));
        	break;
        case R.id.currentList:
        	if (mIsBound)
        	{
        		Intent intent = new Intent(this, MusicList.class);
            	intent.putExtra("CurrentId", musicService.getCurrentMusicId());
            	intent.putExtra("IsPlaying", musicService.isPlaying());
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	intent.putExtra("PlayList", "CurrentList");
            	this.startActivity(intent);
        	}
        	break;
        case R.id.playMode:
        	if (playMode == MyConstant.playmode.ORDER)
        	{
        		playMode = MyConstant.playmode.SINGLE;
        		mode.setImageResource(R.drawable.single);
        		Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
        	}
        	else if (playMode == MyConstant.playmode.SINGLE)
        	{
        		playMode = MyConstant.playmode.LIST_CIRCULATE;
        		mode.setImageResource(R.drawable.list_circulate);
        		Toast.makeText(this, "列表循环", Toast.LENGTH_SHORT).show();
        	}
        	else if (playMode == MyConstant.playmode.LIST_CIRCULATE)
        	{
        		playMode = MyConstant.playmode.SHUFFLE;
        		mode.setImageResource(R.drawable.shuffle);
        		Toast.makeText(this, "列表随机", Toast.LENGTH_SHORT).show();
        	}
        	
        	else if (playMode == MyConstant.playmode.SHUFFLE)
        	{
        		playMode = MyConstant.playmode.ORDER;
        		mode.setImageResource(R.drawable.order);
        		Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();
        	}
        	musicService.setPlayMode(playMode);
        	break;
       }
    }
    
    private Handler sbHandler = new Handler();
    private Runnable updateSeekbar = new Runnable()
	{
	     public void run()
	    {
	    	 if (mIsBound)
	    	 {
	    		 int millis = musicService.getCurrentPosition();
	     		 mSeekbar.setProgress(millis);
	     		 current_playtime.setText(MusicFuntions.formatMillis(millis));
	     		 sbHandler.postDelayed(updateSeekbar, 100);
	    	 }
    		 
	    }
    };
    private void seekBarSetup()
    {
    	//sbHandler.removeCallbacks(updateSeekbar);
    	if (mIsBound)
    	{
    		int millis = musicService.getDuration();
    		mSeekbar.setMax(millis);
    		duration.setText(MusicFuntions.formatMillis(millis));
    		sbHandler.removeCallbacks(updateSeekbar);
    		sbHandler.post(updateSeekbar);
    	}
    	
    }
    
    private Handler lyHandler = new Handler();
    private Runnable updateLyricText = new Runnable()
	{
	     public void run()
	    {
	    	 if (mIsBound)
	    	 {
	    		 int millis = musicService.getCurrentPosition();
	     		 lyric.setCurrentTime(millis);
	     		 lyHandler.postDelayed(updateLyricText, 100);
	    	 }
    		 
	    }
    };
    private Runnable updateMiniLyric = new Runnable()
    {
    	public void run()
	    {
    		if (mIsBound)
    		{
    			int millis = musicService.getCurrentPosition();
        	    miniLyric.setText(musicService.getLyric().seekTo(millis));
        	    lyHandler.postDelayed(updateMiniLyric, 100);
    		}
    		
	    }
    };
    private void lyricSetup()
    {
    	if (mIsBound)
    	{
    		lyric.setLyric(musicService.getLyric(), refrain);
            lyHandler.removeCallbacks(updateLyricText);
    		lyHandler.post(updateLyricText);
    	}
	}
    private void miniLyricSetup()
    {
    	lyHandler.removeCallbacks(updateMiniLyric);
    	lyHandler.post(updateMiniLyric);
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
    		Log.v("musicplay", "unbound");
    		mIsBound = false;
    	}
    }
    
    private void VisualizerSetup() {
    	
        mVisualizer = new Visualizer(musicService.getAudioSessionId());
        mVisualizer.setEnabled(false);
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        final int[] index = new int[] { 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,
                 19,20,23,28,32,37,44,51,60,70,81,95,112,130,153,
                 179,209,255,279,325,395,464 };
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                    int samplingRate) {}
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) 
            {
            	ArrayList<Float> data = new ArrayList<Float>();
                for (int i : index)
            	{
            		byte re = bytes[i*2];
            		byte im = bytes[i*2+1];
            		float intensity;
            		if (re == 0 && im == 0)
            			intensity = -2.26f;
            		else
            			intensity = (float) (Math.log10(Math.sqrt(re*re + im*im)/181.02f));
            		data.add(intensity);
            	}
            	visual.updateVisualizer(data);
            	
            }
			
        }, Visualizer.getMaxCaptureRate()/2, false, true);
       
        mVisualizer.setEnabled(true);
    }
	
   
    
   @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
    	
      // if (event.getRawY() > 0 && event.getRawY() < display.getHeight()*0.75f)
            return detector.onTouchEvent(event);
      // else
        	//return true;
    }
    
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
		
		// TODO Auto-generated method stub
		if (e1.getX() - e2.getX() > 100) // <---
		{
            flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
            flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
            currentView = flipper.getDisplayedChild();
            switch (currentView)
            {
            case 0: //album_art --> lyric
            	lyricSetup();
            	lyHandler.removeCallbacks(updateMiniLyric);
            	miniLyric.setVisibility(View.GONE);
            	currentView = 1;
            	Log.v("flipper", "a->l");
                break;
            case 1: //lyric --> visual
            	lyHandler.removeCallbacks(updateLyricText);
            	VisualizerSetup();
            	miniLyricSetup();
            	miniLyric.setVisibility(View.VISIBLE);
            	currentView = 2;
            	Log.v("flipper", "l->v");
                break;
            case 2: //visual --> album_art
            	mVisualizer.setEnabled(false);
            	if (mIsBound)
            	{
            		long album_id = musicService.getCurrentMusicInfo().getLong("album_id");
                	album_art.setImageBitmap(MusicFuntions.getAlbumArtFromId(this, album_id));
            	}
            	currentView = 0;
            	Log.v("flipper", "v->a");
            	break;
            }
            flipper.showNext();
            return true;
       
		} else if (e1.getX() - e2.getX() < -100)//--->
        {
            flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
            flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
            currentView = flipper.getDisplayedChild();
            switch (currentView)
            {
            case 0: //album_art --> visual
            	VisualizerSetup();
            	currentView = 2;
            	Log.v("flipper", "a->v");
            	break;
            case 1: // lyric --> album_art
            	lyHandler.removeCallbacks(updateLyricText);
            	if (mIsBound)
            	{
            		long album_id = musicService.getCurrentMusicInfo().getLong("album_id");
                	album_art.setImageBitmap(MusicFuntions.getAlbumArtFromId(this, album_id));
            	}
            	miniLyricSetup();
            	miniLyric.setVisibility(View.VISIBLE);
            	currentView = 0;
            	Log.v("flipper", "l->a");
            	break;
            case 2: //visual -- > lyric
            	mVisualizer.setEnabled(false);
            	lyricSetup();
            	lyHandler.removeCallbacks(updateMiniLyric);
            	miniLyric.setVisibility(View.GONE);
            	currentView = 1;
            	Log.v("flipper", "v->l");
            	break;
            }
            flipper.showPrevious();
            return true;
        }
        return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.play_options, menu);
		if (mIsBound)
		{
			if (musicService.isNull())
			{
				menu.setGroupVisible(R.id.play_group_1, false);
			}
		}
		MenuItem rfItem = menu.findItem(R.id.play_refrain);
		if (refrain)
		{
			rfItem.setTitle("关闭副歌播放");
		}
		else
		{
			rfItem.setTitle("打开副歌播放");
		}
	    return true;
     }
     public boolean onOptionsItemSelected(MenuItem item)
     {
    	switch(item.getItemId())
    	{
    	case R.id.play_refrain:
    		if (refrain)                          //disable
    		{
    			if (mIsBound)
    			{
    				refrain = false;
        			lyric.setRefrain(false);
        			musicService.setRefrainMode(false);
        			item.setTitle("打开副歌播放");
        			mSeekbar.setEnabled(true);
    			}
    			
    		}
    		else                                     //enable 
    		{
    			if (mIsBound)
    			{
    				refrain = true;
        			lyric.setRefrain(true);
        			musicService.setRefrainMode(true);
        			item.setTitle("关闭副歌播放");
        			mSeekbar.setEnabled(false);
    			}
    			
    		}
    		break;
    	case R.id.play_musicAttr:
    		if (mIsBound)
    		{
    			long musicId = musicService.getCurrentMusicId();
    			Bundle bundle = MusicFuntions.getMusicInfoToShow(this, musicId);
    			new AttrDialog(this, bundle).create().show();
    		}
    		break;
    	case R.id.play_equalizer:
            if (mIsBound)
            {
            	new EqPresetDialog(this, musicService.getEqPresetNames()).create().show();
            }
    		break;
    	case R.id.play_exit:
    		LocalBroadcastManager.getInstance(this).sendBroadcast(
    				                    new Intent(MyIntentAction.EXIT_APP));
    		stopService(new Intent(this, MusicPlayService.class));
    		this.finish();
    		break;
    	
    	}
		return true;
    	 
     }
	

    
    public void onStart()
    {
    	super.onStart();
    	sbHandler.removeCallbacks(updateSeekbar);
    	sbHandler.post(updateSeekbar);
    	resumeViewOfFlipper();
    	Log.v("musicplay", "onstart");
    	
    }
    protected void onResume()
    {
    	super.onResume();
    	Log.v("musicplay", "onResume");
    	
    }
    protected void onPause()
    {
    	super.onPause();
    	Log.v("musicplay", "onPause");
    }
    protected void onStop()
    {
    	super.onStop();
    	sbHandler.removeCallbacks(updateSeekbar);
    	pauseViewOfFlipper();
        Log.v("musicplay", "onstop");
    }
    protected void onDestroy()
    {
    	super.onDestroy();
    	unregisterReceiver();
    	if (mVisualizer != null)
    	    mVisualizer.release();
    	SharedPreferences sp = this.getSharedPreferences("MusicBox", MODE_PRIVATE);
    	SharedPreferences.Editor se = sp.edit();
		se.putInt("LastViewOfMusicPlayFlipper", currentView);
		se.commit();
    	doUnbindService(); 
    	Log.v("musicplay", "destroy");	
    }
		

	    
}

