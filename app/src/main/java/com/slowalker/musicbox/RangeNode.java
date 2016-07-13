package com.slowalker.musicbox;

public class RangeNode {

	public boolean isExisted;
	public int start;
	public int end;
	public RangeNode(int start, int end, boolean isExisted)
	{
		this.start = start;
		this.end = end;
		this.isExisted = isExisted;
	}
	public RangeNode(boolean isExisted)
	{
		this.isExisted = isExisted;
		start = 0;
		end = 0;
	}
}
