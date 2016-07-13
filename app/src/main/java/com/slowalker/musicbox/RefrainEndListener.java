package com.slowalker.musicbox;

import android.media.MediaPlayer;
import android.os.Handler;

public class RefrainEndListener implements Runnable{

	private MediaPlayer mp;
	private int start;
	private int end;
	private boolean isRunning;
	private Handler handler;
	public  RefrainEndListener(MediaPlayer mp, int start, int end)
	{
		this.mp = mp;
		this.start = start;
		this.end = end;
		handler = new Handler();
		isRunning = false;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (mp != null && isRunning)
		{
			if (mp.getCurrentPosition() > end + 500.0)
			{
				mp.seekTo(mp.getDuration());
				isRunning = false;
			}
			if (isRunning)
				handler.postDelayed(this, 100);
		 	else
				handler.removeCallbacks(this);
		}
		
	}
	public void updateListener(int start, int end)
	{
		this.start = start;
		this.end = end;
	}
	public void startListening()
	{
		mp.seekTo(start);
		handler.post(this);
		isRunning = true;
	}
	public void stopListening()
	{
		handler.removeCallbacks(this);
	}
	public boolean isRunning()
	{
		return isRunning;
	}
	
	
	
}
