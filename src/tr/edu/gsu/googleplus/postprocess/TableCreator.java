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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.jersey.api.client.UniformInterfaceException;

import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * This class contains methods used to create the conversion table
 * used by {@link IdConverter}<br/>
 * 
 * -Xms16g -Xmx16g -XX:+UseConcMarkSweepGC
 * cat edges.*.table > edges.table 
 *  
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class TableCreator
{	/*** Number of Persons to be processed */
	private static final int NODE_NBR = 80080893;
	
	/**
	 * Creates the table used by {@link IdConverter}
	 * to convert the relationships.
	 * 
	 * @param args
	 * 		Not used.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing one of the files. 
	 * @throws InterruptedException 
	 * 		Problem while waiting for the threads to finish
	 */
	public static void main(String[] args) throws FileNotFoundException, InterruptedException
	{	// init log
		logger.setName("Conversion");
		
		// create common structures
		logger.log("Create structures");
		fullMap = new HashMap<String, Integer>(NODE_NBR);
		
		// start the threads
		int threads = 32;
		processRelationships(threads);
		
		// record the obtained table
		recordTable();
		
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
	/** File containing both kinds of ids */
	private static final String PERSONS_FILE = FOLDER + File.separator + "ids.table";
	/** Lock associated with the output file */
	private static Lock outputLock = new ReentrantLock();
	/** Condition associated with the above lock */
	private static Condition outputCond = outputLock.newCondition();
	
	/////////////////////////////////////////////////////////////////
	// PERSONS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Complete map of ids */
	private static Map<String, Integer>	fullMap;
	/** Current id */
	private static int currentId = 0;
	/** Lock associated to the above objects */
	private static Lock idLock = new ReentrantLock();
	
	/**
	 * Returns the int id associated to the specified {@code String} gid.
	 * Might use the cache if the {@code useCache} is set to {@code true}.
	 * 
	 * @param gid
	 * 		The original {@code String} gid
	 */
	private static void checkGid(String gid)
    {	logger.increaseOffset();
    	
    	idLock.lock();
    	{	// check if already present in map
			Integer id = fullMap.get(gid);
			// otherwise: add it
			if(id==null)
			{	// determine new id
				id = currentId;
				currentId++;
				// insert in map
				fullMap.put(gid,id);
			}
    	}
    	idLock.unlock();
		
    	logger.decreaseOffset();
	}
	
	/**
	 * Records the content of the hash map
	 * in a file to be used by {@link IdConverter}.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the output file.
	 */
	private static void recordTable() throws FileNotFoundException
	{	logger.log("Record the conversion table");
		logger.increaseOffset();
		
		// open output file
		logger.log("Open output file");
		FileOutputStream fileOut = new FileOutputStream(PERSONS_FILE);
		OutputStreamWriter osw = new OutputStreamWriter(fileOut);
		PrintWriter writer = new PrintWriter(osw);
		
		// write each entry in the table
		logger.increaseOffset();
		int count = 0;
		for(Entry<String,Integer> e: fullMap.entrySet())
		{	count++;
			// record the entry
			String gid = e.getKey();
			Integer id = e.getValue();
			writer.println(gid + "\t" + id);

			// log process
			if(count%1000000 == 0)
				logger.log("Progress: "+count+"/"+15000);
		}
    	logger.decreaseOffset();
		logger.log("Number of persons recorded: "+count);
		
		
		// close output file
		logger.log("Close output file");
		writer.close();
    	logger.decreaseOffset();
	}

	/////////////////////////////////////////////////////////////////
	// RELATIONSHIPS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates the next relationship file to be processed */
	private static int relationshipNumber = -1;
	/** Lock associated to the above counter */
	private static Lock fileLock = new ReentrantLock();
	/** Number of terminated threads */
	private static int finishedThreads = 0;
	
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
		{	relationshipNumber++;
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
				
				// start the conversion (approx. 15000 relationships in each file)
				logger.increaseOffset();
				int count = 0;
				while(scanner.hasNextLine())
				{	count++;
					String line = scanner.nextLine();
					if(!line.isEmpty())
					{	String parts[] = line.split("\\t");
						checkGid(parts[0]);
						checkGid(parts[1]);
						if(count%1000 == 0)
							logger.log("Progress: "+count+"/"+15000);
					}
				}
				logger.decreaseOffset();
				
				// close the files
				scanner.close();
				logger.log("Done with file number "+nbr+"/"+77);
			}
		}
		while(!finished);
		
		logger.log("No more files to process: this thread is exiting");
		outputLock.lock();
		{	finishedThreads++;
			outputCond.signalAll();
		}
		outputLock.unlock();
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
	 * @throws InterruptedException
	 * 		Problem while waiting for the threads to finish
	 */
	public static void processRelationships(int threadNbr) throws FileNotFoundException, InterruptedException
	{	logger.log("Create threads to process relationships");
	
		// new thread(s)
		for(int t=0;t<threadNbr;t++)
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
	
		// the very first thread waits for the others to finish
		outputLock.lock();
		{	while(finishedThreads<threadNbr)
				outputCond.await();
		}
		outputLock.unlock();
	}
}