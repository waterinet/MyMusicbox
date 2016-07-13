package com.slowalker.musicbox.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AutoScrollTextView extends TextView  
{	 
    public AutoScrollTextView(Context context) 
    {  
	     super(context);  
	}
	public AutoScrollTextView(Context context, AttributeSet attrs, int defStyle) 
	{  
	     super(context, attrs, defStyle);  
	}  
	public AutoScrollTextView(Context context, AttributeSet attrs) 
	{  
	     super(context, attrs);  
	}  
	public boolean isFocused() 
	{  
	     return true;  
	}  
	 
}  
