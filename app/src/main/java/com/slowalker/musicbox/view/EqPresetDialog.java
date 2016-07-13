package com.slowalker.musicbox.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;


public class EqPresetDialog {
	private Context ctx;
	private CharSequence[] items;
	private SharedPreferences sp;
	public EqPresetDialog(Context ctx, CharSequence[] items)
	{
		sp = ctx.getSharedPreferences("MusicBox", Context.MODE_PRIVATE);
		this.ctx = ctx;
		this.items = items;
	}
	public Dialog create()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle("音效设置");
		builder.setSingleChoiceItems(items, getCheckedItem(), 
				                             new DialogInterface.OnClickListener() 
		{    
			public void onClick(DialogInterface dialog, int item) 
			{        
				setCheckedItem(item);  
			}
		});
		builder.setPositiveButton("确定", new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
			
		});
		return builder.create();
	}
	private int getCheckedItem()
	{
		return sp.getInt("EqType", 0);
	}
	private void setCheckedItem(int item)
	{
		SharedPreferences.Editor se = sp.edit();
		se.putInt("EqType", item);
		se.commit();
	}

}
