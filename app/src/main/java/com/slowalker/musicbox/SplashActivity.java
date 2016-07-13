package com.slowalker.musicbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashActivity extends Activity{
	public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.splash);
	    Handler handler = new Handler();
	    handler.postDelayed(new Runnable()
	    {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(intent);
				finishSplash();
			}
	    	
	    }, 1000);
    }
	private void finishSplash()
	{
		this.finish();
	}
}
