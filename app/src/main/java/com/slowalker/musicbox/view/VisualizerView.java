package com.slowalker.musicbox.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class VisualizerView extends View{
	private ArrayList<Float> data;
	private Paint mPaint = new Paint();
    public VisualizerView(Context context) {
        super(context);
        init();
    }
    public VisualizerView(Context context, AttributeSet attr)
	{
		super(context, attr);
		init();
	}
    private void init() {
    	data = new ArrayList<Float>();
    	
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(6.0f);
        mPaint.setColor(Color.WHITE);
    }

    public void updateVisualizer(ArrayList<Float> data) {
    	this.data = data;
    	postInvalidate();
    }
   

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        if (data.isEmpty()) {
            return;
        }
        float posX = 5.0f;
        for (float f : data)
        {
        	float intensity = (f + 2.26f) * 80;
        	canvas.drawLine(posX, getHeight(), posX, getHeight()-intensity, mPaint);
        	posX += 9.5f;
        }
        
    
    }
    

 }
