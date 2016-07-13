package com.slowalker.musicbox.util;

import java.util.LinkedList;

public class BackStack {
	private LinkedList<String> stack;
	private int top;
	public BackStack()
	{
		stack = new LinkedList<String>();
		top = 0;
	}
	public void push(String str)
	{
		stack.add(str);
		top++;
	}
	public String pop()
	{
		String str = null;
		if (!stack.isEmpty())
		{
			str = stack.get(--top);
			stack.remove(top);
		}
		return str;
	}
	public String getTop()
	{
		return stack.get(top - 1);
	}
	public boolean contains(String str)
	{
		return stack.contains(str);
	}

}
