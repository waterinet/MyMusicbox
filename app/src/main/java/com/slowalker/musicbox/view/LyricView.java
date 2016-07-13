package com.slowalker.musicbox.view;

import java.util.ArrayList;
import java.util.Collections;

import com.slowalker.musicbox.Lyrics;
import com.slowalker.musicbox.MusicPlayService;
import com.slowalker.musicbox.util.MusicFuntions;
import com.slowalker.musicbox.util.MyIntentAction;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;


public  class LyricView extends TextView
{
	private Lyrics lyric;
    private int currentTime;
    private Paint paint;
    private Paint highlightPaint;
    private int height;
    private int width;
    private int lineWidth;
    private int positionX;
    private boolean isMoving;
    private boolean refrain;
    public LyricView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}
	public LyricView(Context context, AttributeSet attr)
	{
		super(context, attr);
		init();
	}
	public LyricView(Context context, AttributeSet attr, int i)
	{
		super(context, attr, i);
		init();
	}
	private void init()
	{
		currentTime= 0;
		paint = new Paint();
		paint.setTextSize(20);
		paint.setAntiAlias(true); 
		paint.setTextAlign(Align.LEFT);
		paint.setColor(Color.GRAY);
		
		highlightPaint = new Paint();
		highlightPaint.setTextSize(20);
		highlightPaint.setAntiAlias(true);
		highlightPaint.setTextAlign(Align.LEFT);
		highlightPaint.setColor(Color.WHITE);
		
		positionX = 45;
		lineWidth = 300;
		isMoving  = false;
		
	}
	public void setLyric(Lyrics lyric, boolean refrain)
	{
		this.lyric = lyric; 
		lyric.fillDisplayMap(lineWidth, getPaint());
		this.refrain = refrain;
		
	}
	
	public void setCurrentTime(int position)
	{
		if (!isMoving)
		    this.currentTime = position;
	}
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		height = this.getHeight();
		width = this.getWidth();
		canvas.drawColor(Color.TRANSPARENT);
		if (lyric.exists())
		{
			if (isMoving)
			{
				Paint rectPaint = new Paint();
				rectPaint.setColor(Color.DKGRAY);
				
				canvas.drawRect(positionX-5, height/2-25, positionX+55, height/2, rectPaint);
				canvas.drawText(MusicFuntions.formatMillis(targetTime), 
						                             positionX, height/2-5, highlightPaint);
				canvas.drawLine(0, height/2, width, height/2, paint);
			}
			
		
			Bundle currentLine = lyric.getLineFromTime(currentTime);
			float offset = calOffsetOfCurrentLine();
			Bundle bundle = drawCurrentLine(canvas, offset, height, 
					                    currentLine.getStringArrayList("Lines"));
			
			drawAboveLines(canvas, bundle.getFloat("PositionAbove"), lineWidth, 
					lyric.valuesOfHeadMap(currentTime, false));
			drawBelowLines(canvas, bundle.getFloat("PositionBelow"), lineWidth, height, 
					lyric.valuesOfTailMap(currentTime, false));
		}
		else
		{
			Paint pt = new Paint();
			pt.setColor(Color.WHITE);
			pt.setAntiAlias(true);
			pt.setTextSize(20);
			pt.setTextAlign(Align.CENTER);
			canvas.drawText("歌词不存在", width/2, height/2, pt);
		}
		postInvalidate();
	}
	private Bundle drawCurrentLine(Canvas canvas, float offset, int height, ArrayList<String> displayLines)
	{
		Bundle bundle = new Bundle();
		//y-coordinate from left-top origin, position of first display line of currentLine
	    float position = height/2-offset+20; 
		bundle.putFloat("PositionAbove", position - 30);
		for (String str : displayLines)
		{
			canvas.drawText(str, positionX, position, highlightPaint);
			position += 30;
		}
		bundle.putFloat("PositionBelow", position);
		return bundle;
	}
	private void drawAboveLines(Canvas canvas, float position, int width, ArrayList<Bundle> bundleList)
	{
		boolean label = false;
		for (Bundle bundle : bundleList)
		{
			if (label == true)
				break;
			ArrayList<String> displayLines = new ArrayList<String>(bundle.getStringArrayList("Lines"));
			if (displayLines.size() > 1)
			{
				Collections.reverse(displayLines);
			}
			for (String s : displayLines)
			{
				if (position < 0)
				{
					label = true;
					break;
				}
				canvas.drawText(s, positionX, position, paint);
				position -= 30;
			}
			
		}
		
		
	}
	private void drawBelowLines(Canvas canvas, float position, int width, 
			                              int height, ArrayList<Bundle> bundleList)
	{
		boolean label = false;
		for (Bundle bundle : bundleList)
		{
			if (label == true)
				break;
			ArrayList<String> displayLines = bundle.getStringArrayList("Lines");
			for (String s : displayLines)
			{
				if (position > height)
				{
					label = true;
					break;
				}
				canvas.drawText(s, positionX, position, paint);
				position += 30;
			}
		}
	}
	private float calOffsetOfCurrentLine()
	{
		Bundle bundle = lyric.getLineFromTime(currentTime);
		int numOfLines = bundle.getInt("Number");
		float fromTime = bundle.getInt("FromTime");
		float toTime = bundle.getInt("ToTime");
		float offset = (currentTime - fromTime) * 1.0f/(toTime - fromTime) * (30 * numOfLines);
		return offset;
	}
	//called by other class
	public void setRefrain(boolean refrain)
	{
		this.refrain = refrain;
	}
	
	int targetTime = 0;
	float startY = 0;
    public boolean onTouchEvent(MotionEvent event)
	{
		if (!lyric.exists() || refrain)// disable touch
			return false;
		
		switch (event.getAction())
		{
		 case MotionEvent.ACTION_DOWN:
			 isMoving = true; 
			 targetTime = currentTime;
			 startY = event.getY(); 
		     break;
		 case MotionEvent.ACTION_MOVE:
			 targetTime = moveTo(event.getY()-startY);
			 currentTime = targetTime;
			 postInvalidate();
			 break;
		 case MotionEvent.ACTION_UP:
			 Intent intent = new Intent(getContext(), MusicPlayService.class);
			  intent.setAction(MyIntentAction.SEEK_TO_FROM_LYRIC);
			 intent.putExtra("SeekTo", currentTime);
			 getContext().startService(intent); 
			 isMoving = false;
			 break;
		}
		
		return true;
		
	}
	private int moveTo(float rangeY)
	{
		int targetTime = currentTime;
		if (rangeY > 0)
		{
			int offset = (int) (rangeY * 8);
			targetTime = (targetTime >= offset) ? targetTime - offset : 0 ;
		}
		else if (rangeY < 0)
		{
			int offset = (int) (-rangeY * 8);
			targetTime = (targetTime + offset <= lyric.getDuration()) ? 
					             targetTime + offset : lyric.getDuration(); 
		}
		else 
			targetTime = currentTime;
		return targetTime;
		/*Bundle lineInfo = lyric.getLineFromTime(currentTime);
		float offset = calOffsetOfCurrentLine();
		if (rangeY > 0) // back
		{
			boolean backToBeg = true;
			rangeY -= offset;
			targetTime = lineInfo.getInt("FromTime");
			ArrayList<Integer> headKeys = lyric.keysOfHeadMap(currentTime, false);
			for (int key : headKeys)
			{
				Bundle line = lyric.getLineFromTime(key);
				int num = line.getInt("Number");
				int duration = line.getInt("ToTime") - line.getInt("FromTime");
				if ((rangeY -= num * 30) < 0)
				{
					rangeY += num * 30;
					int timeOffset = (int) (duration * rangeY / (num * 30)) ;
					targetTime -= timeOffset;
					backToBeg = false;
					break;
				}
				targetTime -= duration;
			}
			if (backToBeg)
				targetTime = 0;
		}
		else if (rangeY < 0) //forward
		{
			rangeY = -rangeY;
			boolean toEnd = true;
			rangeY += offset;
			targetTime = lineInfo.getInt("FromTime");
			ArrayList<Integer> tailKeys = lyric.keysOfTailMap(currentTime, true);
			for (int key : tailKeys)
			{
				Bundle line = lyric.getLineFromTime(key);
				int num = line.getInt("Number");
				int duration = line.getInt("ToTime") - line.getInt("FromTime");
				if ((rangeY -= num * 30) < 0)
				{
					rangeY += num * 30;
					int timeOffset = (int) (duration * rangeY / (num * 30)) ;
					targetTime += timeOffset;
					toEnd = false;
					break;
				}
				targetTime += duration;
					
			}
			if (toEnd)
				targetTime = lyric.getDuration();
		}
		else
			targetTime = currentTime;*/
		
	}
	
	
	
}