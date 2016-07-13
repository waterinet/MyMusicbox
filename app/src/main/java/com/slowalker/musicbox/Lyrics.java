package com.slowalker.musicbox;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.text.TextPaint;


public class Lyrics {

	/**
	 * @param args
	 */
    private int duration;
    private boolean exists; 
    private String title = null;
    private String artist = null;
    private String album = null;
    private String author = null;
    private static int DIGIT = 0;
    private static int LETTER = 1;
    private static int CN_CHAR = 2;
    private static int OTHER = 3;
    private TreeMap<Integer, String> sortedLyric = new TreeMap<Integer, String>();  
    private TreeMap<Integer, Bundle> lyricForDisplay = new TreeMap<Integer, Bundle>();
    public Lyrics()
    {
    	exists = false;
    	duration = 0;
    }
	public Lyrics(String lyricPath, int duration) throws IOException
	{
		if (lyricPath == null)
		{
			exists = false;
			duration = 0;
		}
		else
		{
		    BufferedReader in = new BufferedReader(new InputStreamReader(
		    		                 new FileInputStream(lyricPath), "GBK"));
			String str;
			while((str = in.readLine()) != null)
			    parseLyricLine(str);
			in.close();
			this.duration = duration;
			sortedLyric.put(0, "");
			//sortedLyric.put(duration, "");
			exists = true;
			
		 }
		
	}
	private void parseLyricLine(String line)
	{
		String str = removeComment(line);
	    if (str != null)
		{
			getLyricIdTags(str);
			getLyricTimeTags(str);
		}
		
	}
	
	private void getLyricIdTags(String line)
	{
        Matcher m = Pattern.compile("\\[((ti)|(ar)|(al)|(by)):.+\\]")
		     .matcher(line);
		while (m.find())
		{
			String type = m.group().substring(1, 3);
			if (type.equals("ti"))
			{
				title = m.group().substring(4, m.end()-1);
			}
			else if (type.equals("ar"))
			{
				artist = m.group().substring(4, m.end()-1);
			}
			else if (type.equals("al"))
			{
				album = m.group().substring(4, m.end()-1);
			}
			else if (type.equals("by"))
			{
				author = m.group().substring(4, m.end()-1);
			}
				
		}
	}
	private void getLyricTimeTags(String line)
	{
		Matcher m = Pattern.compile("\\[\\d{1,2}:\\d{1,2}([\\.:]\\d{1,2})?\\]").matcher(line);
    
        List<Integer> time = new ArrayList<Integer>(); 
        int begIndex = 0;
        while (m.find())
        {
	        time.add(getTimeOfLine(m.group()));
	        begIndex = m.end();
        }
        for (int i : time)
        {
	        sortedLyric.put(i, line.substring(begIndex, line.length()));
        }
    }
	private int getTimeOfLine(String time)
	{
		int minute = 0;
		int second = 0;
		int milSec = 0;
		int firstDivider = time.indexOf(':');
		int secondDivider = -1;
		if (time.lastIndexOf(':') != firstDivider)// case [ : : ]
			secondDivider = time.lastIndexOf(':');
		else if (time.indexOf('.') != -1)   //case [ : . ]
			secondDivider = time.indexOf('.');
		else
			secondDivider = time.length() - 1; //case [ : ]
	    minute = Integer.parseInt(time.substring(1, firstDivider));
		second = Integer.parseInt(time.substring(firstDivider+1, secondDivider));
		if (secondDivider != time.length()-1)
			milSec = Integer.parseInt(time.substring(secondDivider+1, time.length()-1));
	
		return milSec + second*1000 + minute*60*1000;
	}
	private String removeComment(String line)
	{
		Matcher m = Pattern.compile("\\[:\\]").matcher(line);
		String str = line;
		while (m.find())
		{
			if (m.start() == 0) 
				str = null;
			else
			    str = line.substring(0, m.start());
		}
		return str;
	}
	public void fillDisplayMap(int width, TextPaint tp)
	{
		for (int key : sortedLyric.keySet())
		{
			String line = sortedLyric.get(key);
			ArrayList<String> lines = new ArrayList<String>();
			if (tp.measureText(line) <= width)
			{
				lines.add(line);
			}
			else
			{
				int begIndex = 0;
				for (int i = 0; i <= line.length(); ++i)
				{
					String str = line.substring(begIndex, i);
					if (tp.measureText(str) > width)
					{
					    int dividePoint = getDividePoint(line, begIndex, i-1);
						lines.add(line.substring(begIndex, dividePoint));
						begIndex = dividePoint;
					}
					if (i == line.length())
						lines.add(line.substring(begIndex, i));
				}
				
			}
			Bundle bundle = new Bundle();
			bundle.putInt("Number", lines.size());
			bundle.putStringArrayList("Lines", lines);
			lyricForDisplay.put(key, bundle);
			
		}
	}
	private int getDividePoint(String str, int begIndex, int endIndex)
	{
		//not to divide (letter,letter) or (digit,digit)
		int original = endIndex; 
		while ( (charType(str.charAt(endIndex)) == LETTER && charType(str.charAt(endIndex-1)) == LETTER) 
				|| ( charType(str.charAt(endIndex)) == DIGIT && charType(str.charAt(endIndex-1)) == DIGIT) )
		{
			if (endIndex > begIndex)
			    endIndex--;
			else 
			{
				endIndex = original;
				break;
			}
		}
		return endIndex;
		
	}
	private int charType(char c)
	{
		if (c >= '0' && c <= '9')
			return DIGIT;
		else if ( (c >= 'a' && c <= 'z') ||
				    (c >= 'A' && c <= 'Z'))
			return LETTER;
		else if (Character.isLetter(c))
			return CN_CHAR;
		else
			return OTHER;
		
	}
	
	public String seekTo(int millis)
	{
		if (exists)
		{
			if (millis < 0)
				millis = 0;
		    if (millis > duration)
				millis = duration;
			return  sortedLyric.floorEntry(millis).getValue();
			
		}
		else
			return "";
		
		    
		
	}
	public Bundle getLineFromTime(int millis)
	{
		Bundle bundle = new Bundle();
		if (millis < 0)
			millis = 0;
		if (millis > duration)
			millis = lyricForDisplay.lowerKey(duration);
		int fromTime = lyricForDisplay.floorKey(millis);
		Integer toTime = lyricForDisplay.higherKey(fromTime);
		if (toTime == null)
			toTime = duration;
	   
	    bundle.putInt("FromTime", fromTime);
	    bundle.putInt("ToTime", toTime.intValue());
	    bundle.putInt("Number", lyricForDisplay.get(fromTime).getInt("Number"));
	    bundle.putStringArrayList("Lines", 
	    		    lyricForDisplay.get(fromTime).getStringArrayList("Lines"));
	    
	    return bundle;
	    
	}
	public ArrayList<Bundle> valuesOfHeadMap(int millis, boolean inclusive) 
	{
		ArrayList<Bundle> values = new ArrayList<Bundle>();
		int fromTime = lyricForDisplay.floorKey(millis);
		NavigableMap<Integer, Bundle> headMap = lyricForDisplay.headMap(fromTime, inclusive);
		for (int key : headMap.keySet())
		{
			values.add(headMap.get(key));
		}
		//values.remove(values.size()-1)
		
		for (int i = 0; i < values.size()/2; ++i) //reverse order
		{
			int symIndex = (values.size()-1) - i;
			Bundle bundle = values.get(i);
			values.set(i, values.get(symIndex));
			values.set(symIndex, bundle);
		}
		return values;
	}
	
	public ArrayList<Bundle> valuesOfTailMap(int millis, boolean inclusive)
	{
		ArrayList<Bundle> values = new ArrayList<Bundle>();
		int fromTime = lyricForDisplay.floorKey(millis);
		NavigableMap<Integer, Bundle> tailMap = lyricForDisplay.tailMap(fromTime, inclusive);
		for (int key : tailMap.keySet())
			values.add(tailMap.get(key));
		return values;
	}
	public int getDuration()
	{
		return duration;
	}
	public boolean exists()
	{
		return exists;
	}

}
