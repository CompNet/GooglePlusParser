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
import java.util.Arrays;
import java.util.Scanner;

import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * This class was defined to simplify the edgelist
 * obtained from the IdConverter. The goal is to remove
 * less interesting nodes such as leaves, in order to
 * speed up the various analyses applied to the network.
 *  
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class EdgelistSimplifier
{	/*** Number of Persons to be processed */
	private static final int NODE_NBR = 31148138;
	/*** Number of links to be processed */
	private static final int LINK_NBR = 473106758;
	
	/**
	 * Start the simplification of the network
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
		
		logger.log("Remove the leaves (node with a degree smaller or equal to 1)");
		logger.increaseOffset();
		
		// process the degrees
		processDegrees();
		// record the degrees
		recordDegrees();
		// record the remaining relationships
		filterRelationships();
		
		logger.log("Done");
		logger.decreaseOffset();
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
	/** Stem of the files containing the transformed relationships */
	private static final String FILE_NAME = FOLDER + File.separator + "edges";
	/** Extension of the files containing the transformed relationships */
	private static final String FILE_EXT = ".table";
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** The degrees of the network nodes */
	private static int degrees[] = new int[NODE_NBR];
	
	/**
	 * Parse the file to get the degree
	 * of each node.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the input file.
	 */
	private static void processDegrees() throws FileNotFoundException
	{	logger.increaseOffset();
		
		// init data struct
		Arrays.fill(degrees,0);
		
		// open the input file
		logger.log("Open the input file");
		String filename = FILE_NAME + FILE_EXT;
		FileInputStream file = new FileInputStream(filename);
		InputStreamReader reader = new InputStreamReader(file);
		Scanner scanner = new Scanner(reader);
		
		// read and counts number of appearances
		logger.log("Calculate the degrees");
		logger.increaseOffset();
		int count = 0;
		while(scanner.hasNextLine())
		{	count++;
			String line = scanner.nextLine();
			if(!line.isEmpty())
			{	String parts[] = line.split("\\t");
				int id1 = Integer.parseInt(parts[0]);
				int id2 = Integer.parseInt(parts[1]);
//System.out.println(line);				
				degrees[id1]++;
				degrees[id2]++;
				if(count%1000000 == 0)
					logger.log("Progress: "+count+"/"+LINK_NBR);
			}
		}
		logger.decreaseOffset();
		
		scanner.close();
		logger.log("All ids processed, the structure is ready");
		logger.decreaseOffset();
	}
	
	/**
	 * Parse the network file another time,
	 * this time copying only the links involving
	 * both selected nodes (no leaves).
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the input and/or output files.
	 */
	private static void filterRelationships() throws FileNotFoundException
	{	logger.increaseOffset();
	
		// open the output file
		logger.log("Open the output file");
		String outPath = FILE_NAME + ".noleave" + FILE_EXT;
		FileOutputStream fileOut = new FileOutputStream(outPath);
		OutputStreamWriter osw = new OutputStreamWriter(fileOut);
		PrintWriter writer = new PrintWriter(osw);
		
		// open the input file
		logger.log("Open the input file");
		String filename = FILE_NAME + FILE_EXT;
		FileInputStream file = new FileInputStream(filename);
		InputStreamReader reader = new InputStreamReader(file);
		Scanner scanner = new Scanner(reader);
		
		// record only the appropriate relationships 
		logger.increaseOffset();
		int count = 0;
		while(scanner.hasNextLine())
		{	count++;
			String line = scanner.nextLine();
			if(!line.isEmpty())
			{	String parts[] = line.split("\\t");
				int id1 = Integer.parseInt(parts[0]);
				int id2 = Integer.parseInt(parts[1]);
				if(degrees[id1]>1 && degrees[id2]>1)
				writer.println(id1 + "\t" + id2);
				if(count%1000000 == 0)
					logger.log("Progress: "+count+"/"+LINK_NBR);
			}
		}
		logger.decreaseOffset();
		
		// close the files
		writer.close();
		scanner.close();
		logger.log("All relationships processed");
		logger.decreaseOffset();
	}
	
	/**
	 * Record the degrees in a separate file
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the output file.
	 */
	private static void recordDegrees() throws FileNotFoundException
	{	logger.increaseOffset();
	
		// open the output file
		logger.log("Open the output file");
		String outPath = FOLDER + File.separator + "degree.txt";
		FileOutputStream fileOut = new FileOutputStream(outPath);
		OutputStreamWriter osw = new OutputStreamWriter(fileOut);
		PrintWriter writer = new PrintWriter(osw);
		
		// record only the appropriate relationships 
		logger.increaseOffset();
		for(int count=0;count<degrees.length;count++)
		{	writer.println(degrees[count]);
			if(count%1000000 == 0)
				logger.log("Progress: "+count+"/"+NODE_NBR);
		}
		logger.decreaseOffset();
		
		// close the file
		writer.close();
		logger.log("All relationships processed");
		logger.decreaseOffset();
	}

}