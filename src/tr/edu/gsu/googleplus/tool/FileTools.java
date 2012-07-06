package tr.edu.gsu.googleplus.tool;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * This class contains various methods
 * related to file management.
 *  
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class FileTools
{	
	/////////////////////////////////////////////////////////////////
	// FOLDERS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** main folder of the project */
	public final static String ROOT_FOLDER = new File("").getAbsolutePath();	
	/** folder containing the project resources */
	public final static String RES_FOLDER = "res";
	/** folder containing the files related to Google+ ids */
	public final static String IDS_FOLDER = RES_FOLDER + File.separator + "ids"; 
	/** folder containing the network files */
	public final static String NETWORKS_FOLDER = RES_FOLDER + File.separator + "networks"; 
	/** folder containing the log files */
	public final static String LOG_FOLDER = "log"; 
	/** folder containing the outputted text files */
	public final static String OUT_FOLDER = RES_FOLDER + File.separator + "out"; 
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/**
	 * Download the file at the
	 * specified URL.
	 * 
	 * @param input
	 * 		Input URL as a {@code String}. 
	 * @param output 
	 * 		Output file path as a {@code String}.
	 * @throws IOException
	 * 		Problem while accessing the URL or the output file. 
	 */
	public static void downloadFile(String input, String output) throws IOException
	{	// open connection
		URL url = new URL(input);
		logger.log("Opening connection to " + input);
        InputStream in = url.openStream();

        // copying file
		logger.log("Copying file "+output);
        FileOutputStream out = new FileOutputStream(output);
        byte[] buffer = new byte[1024];
        int bytesRead,total=0;
        while((bytesRead=in.read(buffer)) != -1)
        {	total = total + bytesRead;
        	out.write(buffer,0,bytesRead);
        	//System.out.print(".");
        }
    	//System.out.println();
        
        // close streams
		logger.log("Done: "+total+" bytes read ("+total/1024+" kB)");
        in.close();
        out.close();
	}
	
	/**
	 * Re-number an edge list file,
	 * starting from 1 instead of 0.
	 * 
	 * @param input
	 * 		The file to be converted.
	 * @param output
	 * 		The result of the conversion.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while finding one of the files.
	 */
	public static void convertIdNumbering(String input, String output) throws FileNotFoundException
	{	// open the input file
		logger.log("Open the input file");
		FileInputStream file = new FileInputStream(input);
		InputStreamReader reader = new InputStreamReader(file);
		Scanner scanner = new Scanner(reader);
		
		// open the output file
		logger.log("Open the output file");
		FileOutputStream fileOut = new FileOutputStream(output);
		OutputStreamWriter osw = new OutputStreamWriter(fileOut);
		PrintWriter writer = new PrintWriter(osw);
		
		// convert the ids by adding 1 to them
		// so that the numnering starts from 1 instead of 0
		logger.increaseOffset();
		int count = 0;
		while(scanner.hasNextLine())
		{	count++;
			String line = scanner.nextLine();
			if(!line.isEmpty())
			{	String parts[] = line.split("\\t");
				int id1 = Integer.parseInt(parts[0]) + 1;
				int id2 = Integer.parseInt(parts[1]) + 1;
				writer.println(id1 + "\t" + id2);
				if(count%1000000 == 0)
					logger.log("Progress: "+count);
			}
		}
		logger.decreaseOffset();
		
		// close everything
		logger.log("Close both files");
		scanner.close();
		writer.println();
		writer.close();
	}
}
