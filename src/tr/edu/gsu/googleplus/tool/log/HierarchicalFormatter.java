package tr.edu.gsu.googleplus.tool.log;

/*
 * Google+ Network Extractor
 * Copyright 2011-2012 Vincent Labatut 
 * 
 * This file is part of Google+ Network Extractor.
 * 
 * Google+ Network Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Google+ Network Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Google+ Network Extractor.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import tr.edu.gsu.googleplus.tool.TimeTools;

/**
 * Displays the log in a hierarchical way,
 * using the offset associated to the
 * log record by the logger.
 * 
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class HierarchicalFormatter extends SimpleFormatter
{	
	/**
	 * Creates a new formatter with
	 * the specified limit for
	 * a line of text.
	 * 
	 * @param maxCols
	 * 		Limit of a line of text.
	 */
	public HierarchicalFormatter(int maxCols)
	{	super();
		this.maxCols = maxCols;
	}
	
	/**
	 * Creates a new formatter with
	 * the specified limit for a line of text
	 * and the specified thread id. 
	 * 
	 * @param maxCols
	 * 		Column limit for a line of text.
	 * @param threadNbr
	 * 		Number of the concerned thread.
	 */
	public HierarchicalFormatter(int maxCols, int threadNbr)
	{	this(maxCols);
		this.threadNbr = threadNbr;
	}
	
	/////////////////////////////////////////////////////////////////
	// THREAD		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Number of the concerned thread */
	private Integer threadNbr = null;
	
	/////////////////////////////////////////////////////////////////
	// FORMAT		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** largest line allowed */
	private int maxCols = 0;
	
	@Override
	public String format(LogRecord record)
	{	String result = "";
		Object[] parameters = record.getParameters();

		// hierarchical formatting
		if(parameters!=null && parameters.length>0)
		{	@SuppressWarnings("unchecked")
			List<String> msg = (List<String>)parameters[0];
			int offset = (Integer)parameters[1];
			
			// init
			String hour = TimeTools.getTime(record.getMillis())+" ";
			int length = hour.length();
			String space = "";
			for(int i=0;i<length;i++)
				space = space + " ";
			String lvl = "";
			for(int i=0;i<offset;i++)
				lvl = lvl + ".";
			if(threadNbr!=null)
			{	hour = hour + "(" + threadNbr + ") ";
				space = space + "    ";
			}
			
			// display
			for(int i=0;i<msg.size();i++)
			{	String m = msg.get(i);
				String pre;
				if(i==0)
					pre = hour;
				else
					pre = space;
				result = result + pre + lvl + m;
				
				if(maxCols>0)
				{	String temp = null;
					while(result.length()>maxCols)
					{	if(temp==null)
							temp = result.substring(0,maxCols);
						else
							temp = temp + "\n" + result.substring(0,maxCols);
						result = result.substring(maxCols);
					}
					if(temp==null)
						temp = result;
					else
						temp = temp + "\n" + result;
					result = temp;
				}
				
				result = result + "\n";
			}
		}
		
		// classic formatting
		else
			result = super.format(record);
		
		return result;
	}	
}
