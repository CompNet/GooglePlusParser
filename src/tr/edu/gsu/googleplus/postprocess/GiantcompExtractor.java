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
import java.util.Scanner;
import java.util.TreeSet;

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
public class GiantcompExtractor
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
		logger.setName("Subgraph extraction");
		
		logger.log("Keep only the nodes specified in the subgraph.nodes file");
		logger.increaseOffset();
		
		// load the list of nodes to be kept
		logger.log("Load nodes ids");
		loadNodeIds();
		
		// record the remaining relationships
		logger.log("Retain only the appropriate links");
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
	/** Original network file */
	private static final String IN_NET_FILENAME = FOLDER + File.separator + "noisolates.edgelist";
	/** Generated subnetwork file */
	private static final String OUT_NET_FILENAME = FOLDER + File.separator + "giantcomp.edgelist";
	/** List of nodes to be kept */
	private static final String IN_NODES_FILENAME = FOLDER + File.separator + "giantcomp.nodes";
	
	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** The list of nodes to be kept */
	private static TreeSet<Integer> nodes = new TreeSet<Integer>();
	
	/**
	 * Load the file to get the nodes.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the input file.
	 */
	private static void loadNodeIds() throws FileNotFoundException
	{	logger.increaseOffset();
		
		// open the input file
		logger.log("Open the input file");
		FileInputStream file = new FileInputStream(IN_NODES_FILENAME);
		InputStreamReader reader = new InputStreamReader(file);
		Scanner scanner = new Scanner(reader);
		
		// populate the set
		logger.log("Retrieve the node list");
		logger.increaseOffset();
		int count = 0;
		while(scanner.hasNextLine())
		{	count++;
			String line = scanner.nextLine();
			if(!line.isEmpty())
			{	int id = Integer.parseInt(line);
				nodes.add(id);
				if(count%1000000 == 0)
					logger.log("Progress: "+count+"/"+NODE_NBR+" (max)");
			}
		}
		logger.decreaseOffset();
		
		scanner.close();
		logger.log("All ids processed, the node list is ready");
		logger.decreaseOffset();
	}
	
	/**
	 * Parse the network file, copying only the links involving
	 * both selected nodes.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the input and/or output files.
	 */
	private static void filterRelationships() throws FileNotFoundException
	{	logger.increaseOffset();
	
		// init processed node set, for verification
		TreeSet<Integer> processedNodes = new TreeSet<Integer>();
	
		// open the output file
		logger.log("Open the output file");
		FileOutputStream fileOut = new FileOutputStream(OUT_NET_FILENAME);
		OutputStreamWriter osw = new OutputStreamWriter(fileOut);
		PrintWriter writer = new PrintWriter(osw);
		
		// open the input file
		logger.log("Open the input file");
		FileInputStream file = new FileInputStream(IN_NET_FILENAME);
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
				if(nodes.contains(id1) && nodes.contains(id2))
				{	writer.println(id1 + "\t" + id2);
					processedNodes.add(id1);
					processedNodes.add(id2);
				}
				if(count%1000000 == 0)
					logger.log("Progress: "+count+"/"+LINK_NBR);
			}
		}
		logger.decreaseOffset();
		
		// close the files
		writer.close();
		scanner.close();
		logger.log("All relationships processed");
		
		// display the missing nodes
		logger.log("Number of nodes actually recorded: "+processedNodes.size()+"/"+nodes.size());
		if(nodes.size()!=processedNodes.size())
		{	logger.log("Missing nodes:");
			logger.increaseOffset();
			for(int i: nodes)
			{	if(!processedNodes.contains(i))
					logger.log(Integer.toString(i));
			}
			logger.decreaseOffset();
		}		
		
		logger.decreaseOffset();
	}
}