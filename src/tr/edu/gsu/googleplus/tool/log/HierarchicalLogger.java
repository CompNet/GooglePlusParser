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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import tr.edu.gsu.googleplus.tool.FileTools;
import tr.edu.gsu.googleplus.tool.TimeTools;

/**
 * Wrapper allowing to introduce
 * a hierarchy in the {@code Logger} API.
 * Messages are displayed with an offset
 * the user can control manually.
 * This can be used to reproduce
 * graphically the way methods
 * are chained, e.g.:</br>
 * {@code method1}</br>
 * {@code ..method2}</br>
 * {@code ....method3}</br>
 * {@code ..method4}</br>
 * where {@code method1} calls {@code method2}
 * and {@code method4}, and {@code method2}
 * calls {@code method3}.
 * 
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class HierarchicalLogger
{	
	/**
	 * Builds a new hierarchical logger
	 * with the specifed name.
	 * 
	 * @param name
	 * 		Name of the new logger.
	 */
	HierarchicalLogger(String name)
	{	this.name = name;
	}
	
    /////////////////////////////////////////////////////////////////
	// NAME			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** General name of the logger */
	private String name = null;
	
	/**
	 * Change the name of this logger.
	 * 
	 * @param name
	 * 		New name of this logger.
	 */
	public void setName(String name)
	{	this.name = name;
	}
	
	/////////////////////////////////////////////////////////////////
	// THREADS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Number of threads managed since the begining of the logging */
	private int count = 0;
    /** Classic loggers used by this hierarchical logger */
	private final HashMap<Long,Logger> loggerMap = new HashMap<Long, Logger>();
	
	/**
	 * Retrieves or builds a basic logger
	 * in order to handle internally a thread.
	 * 
	 * @return
	 * 		A logger assigned to the current thread.
	 */
	private synchronized Logger getLogger()
	{	Thread thread = Thread.currentThread();
		long id = thread.getId();
		Logger result = loggerMap.get(id);
		if(result==null)
		{	try
			{	String loggerName = name + "." + count;
				thread.setName("RelationshipExtractor#"+count);
				// console handler
				ConsoleHandler ch = new ConsoleHandler();
				ch.setLevel(Level.ALL);
				HierarchicalFormatter formatter = new HierarchicalFormatter(10000,count);
				ch.setFormatter(formatter);
				
				// file handler
				String filename = FileTools.LOG_FOLDER + File.separator 
					+ TimeTools.getCurrentTime() + "."
					+  loggerName + "." 
					+ "%g"								// replaced by the file number during runtime
					+ LOG_EXTENSION;
				int size = 1024*1024*10;
				FileHandler fh = new FileHandler(filename,size,100);
				fh.setLevel(Level.ALL);
				fh.setEncoding("UTF-8");
				formatter = new HierarchicalFormatter(0,count);
				fh.setFormatter(formatter);
				
				// logger
				result = Logger.getLogger(loggerName);
				result.setLevel(Level.ALL);
				result.addHandler(ch);
				result.addHandler(fh);
				result.setUseParentHandlers(false);
				
				loggerMap.put(id,result);
				offsetMapLock.lock();
					offsetMap.put(id,0);
				offsetMapLock.unlock();
				count++;
			}
			catch(SecurityException e)
			{	e.printStackTrace();
			}
			catch (IOException e)
			{	e.printStackTrace();
			}
		}
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Extension of the log file names (actually simple text files) */  
	private static final String LOG_EXTENSION = ".log";

    /////////////////////////////////////////////////////////////////
	// OFFSET		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /** Offsets used to represent the current levels in the method calls hierarchies */
	private final HashMap<Long,Integer> offsetMap = new HashMap<Long, Integer>();
	/** corresponding lock */
	private final Lock offsetMapLock = new ReentrantLock();
    /**
     * Increases the current offset
     * of this logger.
     */
    public void increaseOffset()
    {	long id = Thread.currentThread().getId();
		offsetMapLock.lock();
    	int offset = offsetMap.get(id);
    	offset++;
    	offsetMap.put(id,offset);
    	offsetMapLock.unlock();
    }
    
    /**
     * Decreases the current offset
     * of this logger.
     */
    public void decreaseOffset()
    {	long id = Thread.currentThread().getId();
		offsetMapLock.lock();
		int offset = offsetMap.get(id);
		offset--;
		offsetMap.put(id,offset);
		offsetMapLock.unlock();
	}
    
    /**
     * Retrieves the offset associated to the
     * current thread.
     * 
     * @return
     * 		The offset of the current thread.
     */
    public int getOffset()
    {	long id = Thread.currentThread().getId();
		offsetMapLock.lock();
		int result = offsetMap.get(id);
		offsetMapLock.unlock();
		return result;
	}
    
	/////////////////////////////////////////////////////////////////
	// LOGGING		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
    /**
     * Logs a new message.
     * 
     * @param msg 
     * 		The message to be logged.
     */
    public void log(String msg)
    {	List<String> msgs = new ArrayList<String>();
    	msgs.add(msg);
    	log(msgs);
    }

    /**
     * Logs a series of new messages.
     * 
     * @param msg 
     * 		The list of messages to be logged.
     */
    public void log(List<String> msg)
    {	Logger logger = getLogger();
    	Object params[] = {msg,getOffset()};
		logger.log(Level.INFO,msg.get(0),params);
    }
}
