package tr.edu.gsu.googleplus.explorer;

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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;

import com.sun.jersey.api.client.UniformInterfaceException;

import tr.edu.gsu.googleplus.data.Person;
import tr.edu.gsu.googleplus.parser.GooglePlusParser;
import tr.edu.gsu.googleplus.tool.DbTools;
import tr.edu.gsu.googleplus.tool.FileTools;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * Initializes the DB with
 * all the availale Google+
 * ids.</br>
 * I used the idea exposed in these posts
 * from <a href="http://blog.webdistortion.com/2011/06/12/google-people-search-how-big-is-googles-social-network/">Paul Anthony</a>
 * and <a href="http://www.blackhatworld.com/blackhat-seo/black-hat-seo/315777-list-google-profiles-do-what-you-want.html">NgocChinh</a>.</br>
 * 
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class PersonExtractor
{	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/**
	 * Retrieve the list of Google+ users.
	 * Those are stored in files copied in the res/ids folder.
	 * They are also inserted in the DB (PERSON table)
	 * 
	 * @param limit
	 * 		Limits the number of pages retrieved (for testing purpose).
	 * 		Zero means no limit (normal use).
	 * 
	 * @throws IOException
	 * 		Problem while accessing some URL or output file. 
	 * @throws SQLException 
	 * 		Problem while accessing the DB.
	 */
	public static void retrieveAllPersons(int limit) throws IOException, SQLException
	{	// force to re-load cached files, even if they already exist
		boolean force = false;
		
		// get main file
		String pathStr = downloadMainFile(force);
       
		// parse it to retrieve sitemap files
        List<String> sitemapUrls = parseMainFile(pathStr);
        
        // get each sitemap file
        if(limit>0)
        	sitemapUrls = sitemapUrls.subList(0,limit);
        List<String> sitemapPaths = downloadSitemaps(sitemapUrls,force);
        
        // parse them to retrieve Google+ ids
        int from = 0;
        parseSitemaps(sitemapPaths,from);
	}
	
	/**
	 * Downloads the main file
	 * listing all the sitemaps files.
	 * If the file already exists locally,
	 * it is not downloaded unless the {@code force}
	 * parameter is set to {@code true}.
	 * 
	 * @param force
	 * 		Download the file even if it already exists.
	 * @return
	 * 		The path of the downloaded main file.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the URL.
	 */
	private static String downloadMainFile(boolean force) throws IOException
	{	String fileName = "profiles-sitemap.xml";
		String urlStr = "http://www.gstatic.com/s2/sitemaps/"+fileName;
        String pathStr = FileTools.IDS_FOLDER + File.separator + fileName;
        File file = new File(pathStr);
        if(force || !file.exists())
        	FileTools.downloadFile(urlStr,pathStr);
        return pathStr;
	}
	
	/**
	 * Parses the main file allowing to retrieve
	 * the Google+ ids.</br> 
	 * The parsing is very raw, no need for
	 * any fancy API like JDOM to access
	 * the XML content, here.
	 * 
	 * @param pathStr
	 * 		Location of the file to be parsed.
	 * @return
	 * 		A list of URLs under the form of {@code String}.
	 * 
	 * @throws IOException
	 */
	private static List<String> parseMainFile(String pathStr) throws IOException
	{	List<String> result = new ArrayList<String>();
		
		// open the file to be read
		logger.log("Open file "+pathStr);
		FileInputStream fin = new FileInputStream(pathStr);
		DataInputStream in = new DataInputStream(fin);
		InputStreamReader isr = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(isr);
		
		// read content line by line
		logger.log("Start parsing:");
		logger.increaseOffset();
		final String startTag = "<loc>";
		final String endTag = "</loc>";
		String line;
		while((line=br.readLine()) != null)
		{	logger.log(line);
			logger.increaseOffset();
			int index = line.indexOf(startTag);
			if(index!=-1)
			{	int startIdx = index + startTag.length();
				int endIdx = line.indexOf(endTag);
				String urlStr = line.substring(startIdx,endIdx);
				result.add(urlStr);
				logger.log(urlStr);
			}
			logger.decreaseOffset();
		}
		logger.decreaseOffset();

		// close the file
		logger.log("Close file");
		in.close();
		
		return result;
	}
	
	/**
	 * Download all sitemap files.
	 * Each one contains a list of
	 * Google+ user URLs.
	 * If some file already exists locally,
	 * it is not downloaded unless the {@code force}
	 * parameter is set to {@code true}.
	 * 
	 * @param urlStrs
	 * 		List of URLs to be processed.
	 * @param force
	 * 		Download the files even if they already exist.
	 * @return
	 * 		List of downloaded files.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the URL or output files.
	 */
	private static List<String> downloadSitemaps(List<String> urlStrs, boolean force) throws IOException
	{	logger.log("Download sitemaps:");
		logger.increaseOffset();
		List<String> result = new ArrayList<String>();
		int count = 0;
		
        for(String urlStr: urlStrs)
        {	logger.log("Download sitemap "+urlStr);
			int index = urlStr.lastIndexOf('/');
        	String fileName = urlStr.substring(index+1);
        	String pathStr = FileTools.IDS_FOLDER + File.separator + fileName;
        	result.add(pathStr);
        	File file = new File(pathStr);
        	if(force || !file.exists())
        		FileTools.downloadFile(urlStr,pathStr);
        	count++;
        }
		logger.decreaseOffset();
        
        logger.log("Downloaded "+count+"/"+urlStrs.size()+" sitemaps");
		return result;
	}

	/**
	 * Analyze all the sitemap files
	 * in order to retrieve
	 * the Google+ user URLs,
	 * and then record all these
	 * ids as new Persons in the DB.
	 * 
	 * @param pathStrs
	 * 		Paths of the downloaded sitemap files.
	 * @param from
	 * 		Starts the parsing from the specified file.
	 * 
	 * @throws IOException
	 * 		Problem while accessing an input file.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	private static void parseSitemaps(List<String> pathStrs, int from) throws IOException, SQLException
	{	int count = 0;
		int idCount = 0;
		boolean start = false;
		
		logger.increaseOffset();
		for(String pathStr: pathStrs)
		{	if(from==0 || pathStr.endsWith("sitemap-"+from+".txt"))
				start = true;
			if(start)
			{	// open the file to be read
				logger.log("Open file "+pathStr);
				FileInputStream fin = new FileInputStream(pathStr);
				DataInputStream in = new DataInputStream(fin);
				InputStreamReader isr = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(isr);
				
				// read content line by line
				logger.log("Start parsing:");
				logger.increaseOffset();
				String line;
				while((line=br.readLine()) != null)
				{	idCount++;
					logger.log(line);
					logger.increaseOffset();
					
					// NOTE at first, every user with a google profile automatically had a g+ profile too
					// but this was changed later, which forces us to ignore google profile users here 
					int index = line.lastIndexOf('/');
					String id = line.substring(index+1);
					if(line.contains("plus"))
					{	logger.log(">>"+id);
						Person.insertDb(id);
					}
					else
					{	logger.log(">> "+id+" ignored (not a G+ profile)");
					}
					
					logger.decreaseOffset();
				}
				logger.decreaseOffset();
			
				// close the file
				logger.log("Close file");
				in.close();
				count++;
			}
		}
    	
		logger.decreaseOffset();
		logger.log("Parsed "+count+"/"+pathStrs.size()+" files (total: "+idCount+" ids");
	}
	
	/**
	 * For each Person in the DB,
	 * retrieves its personal information
	 * using the Google+ parser,
	 * and possibly updates the data
	 * contained in the DB.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 * @throws JSONException 
	 * 		Problem while accessing Google+.
	 * @throws InterruptedException 
	 * 		Problem while accessing Google+.
	 * @throws URISyntaxException 
	 * 		Problem while accessing Google+.
	 * @throws MalformedURLException 
	 * 		Problem while accessing Google+.
	 * @throws UniformInterfaceException 
	 * 		Problem while accessing Google+.
	 */
	public static void retrievePersonalData() throws SQLException, UniformInterfaceException, MalformedURLException, URISyntaxException, InterruptedException, JSONException
	{	logger.increaseOffset();
		int count = 0;
		int total = Person.getTableSize();
		
		Statement statement = DbTools.connection.createStatement();
	    ResultSet results = statement.executeQuery("SELECT * FROM PERSON");
	    while(results.next())
	    {	count++;
	    	Person person1 = Person.build(results);
	    	Person person2 = GooglePlusParser.extractPerson(person1.getId());
	    	boolean modified = person1.updateFrom(person2);
	    	if(modified)
	    		person1.updateDb();
			logger.log(count+ "/"+total+": "+person1.getId());
	    }
	    
	    results.close();
	    statement.close();
	    
		logger.decreaseOffset();
		logger.log("Processed: "+count);
	}
}
