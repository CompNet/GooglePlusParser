package tr.edu.gsu.googleplus.postprocess;

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.jersey.api.client.UniformInterfaceException;

import tr.edu.gsu.googleplus.tool.FileTools;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * This class contains methods used to convert the string-based database
 * into a more convenient int-based set of files.<br/>
 * 
 * -Xms16g -Xmx16g -XX:+UseConcMarkSweepGC
 * cat edges.*.table > edges.table
 *  
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class IdConverter
{	/*** Number of Persons to be processed */
//	private static final int ID_NBR = 80080893; // raw number, including isolates
	private static final int ID_NBR = 31148138;
	
	/**
	 * Starts the conversion, using some files exported from the DB.
	 * The number of threads must be set in the source code using the {@code threads}
	 * variable below. Using the cache can be decided through the {@code useCache} field.
	 * 
	 * @param args
	 * 		Not used.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing one of the files. 
	 */
	public static void main(String[] args) throws FileNotFoundException
	{	// init log
		logger.setName("Conversion");
		
		// create common structures
		logger.log("Create structures");
		fullMap = new HashMap<String, Integer>(ID_NBR);
		cacheMap = new HashMap<String, Integer>(CACHE_SIZE);
		cacheFifo = new LinkedList<String>();
		
		// load full map
		loadPersonIds();
		
		// start the threads
		int threads = 32;
		processRelationships(threads);
		
		// convert file
//		FileTools.convertIdNumbering("/home/vlabatut/eclipse/workspaces/Extraction/Database/googleplus/edges.noleave.table",
//				"/home/vlabatut/eclipse/workspaces/Extraction/Database/googleplus/edges.noleave.table.edges");
		
		logger.log("Done");
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Folder containing all files */
	private static final String FOLDER = ".." + File.separator + "Database" + File.separator + "googleplus";
	/** Stem of the files containing the original relationships */
	private static final String RELATIONSHIPS_NAME = FOLDER + File.separator + "googleplus.";
	/** Extension of the files containing the original relationships */
	private static final String RELATIONSHIPS_EXT = ".edgelist";
	/** Stem of the files containing the transformed relationships */
	private static final String NEW_NAME = FOLDER + File.separator + "edges.";
	/** Extension of the files containing the transformed relationships */
	private static final String NEW_EXT = ".table";
	/** File containing both kinds of ids */
	private static final String PERSONS_FILE = FOLDER + File.separator + "ids" + NEW_EXT;
	
	/////////////////////////////////////////////////////////////////
	// PERSONS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Complete map of ids */
	private static HashMap<String, Integer>	fullMap;
	/** Part of the cache used when reforming the DB: cache size */
	private static final Integer CACHE_SIZE = 10000;
	/** Part of the cache used when reforming the DB: maps of known ids */ 
	private static HashMap<String,Integer> cacheMap;
	/** Part of the cache used when reforming the DB ordered list of */ 
	private static LinkedList<String> cacheFifo;
	/** Flag indicating if the cache should be used */
	private static boolean useCache = false;
	/** Lock associated to the cache, for when it is shared amongst several threads */
	private static Lock cacheLock = new ReentrantLock();

	/**
	 * Loads the file containing both kinds of ids
	 * and put them in a full map.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the ids file. 
	 */
	private static void loadPersonIds() throws FileNotFoundException
	{	logger.log("Load the Persons map");
		logger.increaseOffset();
		
		// open the input file
		logger.log("Open the input file");
		FileInputStream file = new FileInputStream(PERSONS_FILE);
		InputStreamReader reader = new InputStreamReader(file);
		Scanner scanner = new Scanner(reader);
		
		// read and insert in full map
		logger.increaseOffset();
		int count = 0;
		while(scanner.hasNextLine())
		{	count++;
			String line = scanner.nextLine();
			if(!line.isEmpty())
			{	String parts[] = line.split("\\t");
//				String key = parts[0].substring(1,parts[0].length()-1); // for the old table format which included ""
				String key = parts[0];
				Integer value = Integer.parseInt(parts[1]);
				fullMap.put(key,value);
				if(count%1000000 == 0)
					logger.log("Progress: "+count+"/"+ID_NBR);
			}
		}	
		logger.decreaseOffset();
		logger.log("All ids loaded, map is ready");
		
		// close the file
		scanner.close();
		logger.decreaseOffset();
	}
	
	/**
	 * Returns the int id associated to the specified {@code String} gid.
	 * Might use the cache if the {@code useCache} is set to {@code true}.
	 * 
	 * @param gid
	 * 		The original {@code String} gid
	 * @return
	 * 		The int id.
	 */
	private static int getPersonId(String gid)
    {	logger.increaseOffset();
		Integer result = -1;
    	
    	if(useCache)
		{	cacheLock.lock();
	    	{	// check if the id is already in the cache
				result = cacheMap.get(gid);
				if(result!=null)
				{	// just update the cache
					cacheFifo.remove(gid);
					cacheFifo.offer(gid);
				}
				
		    	// otherwise, retrieve it from the full map and update the cache
				else
				{	// query the full map
					result = fullMap.get(gid);
					
					// possibly make some room in the cache
					if(cacheFifo.size()==CACHE_SIZE)
					{	String temp = cacheFifo.poll();
						cacheFifo.remove(temp);
					}
					// cache the value
					cacheMap.put(gid,result);
					cacheFifo.offer(gid);
				}
			
//				logger.log("CACHE: "+gid+" >> "+result);
	    	}
			cacheLock.unlock();
		}
    	
    	else
    		result = fullMap.get(gid);
		
    	logger.decreaseOffset();
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// RELATIONSHIPS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates the next relationship file to be processed */
	private static int relationshipNumber = -1;
	/** Lock associated to the above counter */
	private static Lock fileLock = new ReentrantLock();
	
	/**
	 * Returns the number of the next relationship file
	 * to be processed. If the converted file already exists,
	 * the current number is skipped.
	 * 
	 * @return
	 * 		A number corresponding to the next relationship file to be processed.
	 */
	private static int getNextRelationshipFile()
	{	logger.increaseOffset();
		int result = -1;
	
		fileLock.lock();
		{	File file = null;
			do
			{	relationshipNumber++;
				String path = NEW_NAME + relationshipNumber + NEW_EXT;
				file = new File(path);
			}
			while(file.exists());
			result = relationshipNumber;
		}
		fileLock.unlock();
		
		logger.decreaseOffset();
		return result;
	}
	
	/**
	 * Implements the process of a single thread. It focuses
	 * on a specific relationship file, open it and use the
	 * in-memory map to convert each relation to couples of integers
	 * (instead of string). Each relation is then recorded on-the-fly
	 * in the corresponding output file.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing one of the two files.
	 */
	private static void threadProcess() throws FileNotFoundException
	{	logger.log("Starting the thread");
		logger.increaseOffset();
		boolean finished = false;
		
		do
		{	// open the input file
			int nbr = getNextRelationshipFile();
			String inPath = RELATIONSHIPS_NAME + nbr +RELATIONSHIPS_EXT;
			File file = new File(inPath);
			// if the file does not exist, then the process is over
			if(!file.exists())
				finished = true;
			else
			{	logger.log("Start processing file number "+nbr+"/"+77);
				FileInputStream fileIn = new FileInputStream(inPath);
				InputStreamReader isr = new InputStreamReader(fileIn);
				Scanner scanner = new Scanner(isr);
				
				// open the output file
				String outPath = NEW_NAME + nbr + NEW_EXT;
				FileOutputStream fileOut = new FileOutputStream(outPath);
				OutputStreamWriter osw = new OutputStreamWriter(fileOut);
				PrintWriter writer = new PrintWriter(osw);
		
				// start the conversion (approx. 15000 relationships in each file
				logger.increaseOffset();
				int count = 0;
				while(scanner.hasNextLine())
				{	count++;
					String line = scanner.nextLine();
					if(!line.isEmpty())
					{	String parts[] = line.split("\\t");
						int id1 = getPersonId(parts[0]);
						int id2 = getPersonId(parts[1]);
						writer.println(id1 + "\t" + id2);
						if(count%1000 == 0)
							logger.log("Progress: "+count+"/"+15000);
					}
				}
				logger.decreaseOffset();
				
				// close the files
				scanner.close();
				writer.println();
				writer.close();
				logger.log("Done with file number "+nbr+"/"+77);
			}
		}
		while(!finished);
		
		logger.log("No more files to process: this thread is exiting");
		logger.decreaseOffset();
	}

	/**
	 * Creates the specified number of threads to process the
	 * relationship files. Each one will process one file at
	 * a time.
	 * 
	 * @param threadNbr
	 * 		Number of threads to work simultaneously on the relationship files.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing one of the files.
	 */
	public static void processRelationships(int threadNbr) throws FileNotFoundException
	{	logger.log("Create threads to process relationships");
	
		// new thread(s)
		for(int t=0;t<threadNbr-1;t++)
		{	Runnable runnable = new Runnable()
			{	@Override
				public void run()
				{	try
					{	threadProcess();
					}
					catch (FileNotFoundException e)
					{	e.printStackTrace();
					}
					catch (UniformInterfaceException e)
					{	e.printStackTrace();
					}
				}
			};
			Thread thread = new Thread(runnable);
			thread.start();
		}
	
		// also use the very first thread
		threadProcess();
	}
}