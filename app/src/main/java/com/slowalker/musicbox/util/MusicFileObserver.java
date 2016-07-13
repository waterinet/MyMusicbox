package com.slowalker.musicbox.util;

import com.slowalker.musicbox.PlayCountService;
import com.slowalker.musicbox.mood.MoodClassifyService;

import android.content.Context;
import android.content.Intent;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;

public class MusicFileObserver extends FileObserver{

	private Context ctx;
	private Handler handler;
	
	
	public MusicFileObserver(String path) {
		super(path);
		// TODO Auto-generated constructor stub
	}
	
	/* when no file creation or delete happens
	 * in 60s, then the moodClassifyService 
	 * should be start
	 */
	private Runnable runnable1 = new Runnable()
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Intent intent = new Intent(ctx, MoodClassifyService.class);
			ctx.startService(intent);
			Log.v("Observer", "runnable1");
		}
    };
    private Runnable runnable2 = new Runnable()
    {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Intent intent = new Intent(ctx, PlayCountService.class);
			ctx.startService(intent);
			Log.v("Observer", "runnable2");
		}
    	
    };
	public MusicFileObserver(Context ctx, String path)
	{
		super(path);
		this.ctx = ctx;
		handler = new Handler();
		
	}

	@Override
	public void onEvent(int event, String path) {
		// TODO Auto-generated method stub
		if (event == FileObserver.DELETE )
		{
			handler.removeCallbacks(runnable1);
			handler.removeCallbacks(runnable2);
			handler.postDelayed(runnable1, 60 * 1000);
			handler.postDelayed(runnable2, 60 * 1000);
			Log.v("Observer", "FileDelete");
		}
		else if (event == FileObserver.CREATE)
		{
			handler.removeCallbacks(runnable1);
			handler.postDelayed(runnable1, 60 * 1000);
			Log.v("Observer", "FileCreate");
		}
	}

}
