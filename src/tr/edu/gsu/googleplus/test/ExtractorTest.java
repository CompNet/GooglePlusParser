package tr.edu.gsu.googleplus.test;

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import org.codehaus.jettison.json.JSONException;

import com.sun.jersey.api.client.UniformInterfaceException;

import tr.edu.gsu.googleplus.explorer.PersonExtractor;
import tr.edu.gsu.googleplus.explorer.RelationshipExtractor;
import tr.edu.gsu.googleplus.tool.DbTools;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * Initializes the DB with all the availale Google+ ids.</br>
 * 
 * I used the idea exposed in these posts
 * from <a href="http://blog.webdistortion.com/2011/06/12/google-people-search-how-big-is-googles-social-network/">Paul Anthony</a>
 * and <a href="http://www.blackhatworld.com/blackhat-seo/black-hat-seo/315777-list-google-profiles-do-what-you-want.html">NgocChinh</a>.</br>
 * 
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class ExtractorTest
{	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// PROCESSING	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Various tests conducted on the extraction
	 * processes implemented in the program.
	 * 
	 * @param arg
	 * 		Not used.
	 * 
	 * @throws ClassNotFoundException 
	 * 		Problem while loading the DB driver.
	 * @throws SQLException 
	 * 		Problem while accessing the DB.
	 * @throws IOException 
	 * 		Problem while retrieving the network.
	 * @throws JSONException 
	 * 		Problem while retrieving the network.
	 * @throws InterruptedException 
	 * 		Problem while retrieving the network.
	 * @throws URISyntaxException 
	 * 		Problem while retrieving the network.
	 * @throws UniformInterfaceException 
	 * 		Problem while retrieving the network.
	 */
	public static void main(String arg[]) throws ClassNotFoundException, SQLException, IOException, UniformInterfaceException, URISyntaxException, InterruptedException, JSONException
	{	logger.setName("ExtractorTest");
		
		// test ego network extraction
		testEgoNetwork();
		
		// test full network extraction
//		testFullNetwork();
	}
	
	/**
	 * Retrieves an example ego network,
	 * i.e. a network centered on a specific user.
	 * You can play with the g+ id and/or
	 * radius to translate/expand the network.
	 * 
	 * @throws ClassNotFoundException
	 * 		Problem while loading the DB driver.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 * @throws UniformInterfaceException
	 * 		Problem while retrieving the network.
	 * @throws MalformedURLException
	 * 		Problem while retrieving the network.
	 * @throws URISyntaxException
	 * 		Problem while retrieving the network.
	 * @throws InterruptedException
	 * 		Problem while retrieving the network.
	 * @throws JSONException
	 * 		Problem while retrieving the network.
	 * @throws UnsupportedEncodingException
	 * 		Problem while retrieving the network.
	 * @throws FileNotFoundException
	 * 		Problem while exporting the network.
	 */
	public static void testEgoNetwork() throws ClassNotFoundException, SQLException, UniformInterfaceException, MalformedURLException, URISyntaxException, InterruptedException, JSONException, FileNotFoundException, UnsupportedEncodingException
	{	DbTools.setDbName("googleplus.ego");
		DbTools.openDb(false);
		
		// init DB
//		DbTools.createTables();
		DbTools.resetTables();
		
		// set parameters
		int radius = 1;
		String id = "105143088102952395556"; // moi
		
		// populate DB
		RelationshipExtractor.retrieveEgoNetwork(id,radius);
		
		// export to file
		DbTools.exportRelationshipsAsEdgelist();
		DbTools.exportPersonsAsNodelist();
		
		// close DB
		DbTools.closeDb();
	}

	/**
	 * Shows how to retrieve the full G+ network.
	 * Watch out: it takes ages, especially since
	 * there is a captcha protection.
	 * 
	 * @throws ClassNotFoundException
	 * 		Problem while loading the DB driver.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 * @throws IOException
	 * 		Problem while retrieving the network.
	 * @throws UniformInterfaceException
	 * 		Problem while retrieving the network.
	 * @throws URISyntaxException
	 * 		Problem while retrieving the network.
	 * @throws InterruptedException
	 * 		Problem while retrieving the network.
	 * @throws JSONException
	 * 		Problem while retrieving the network.
	 */
	public static void testFullNetwork() throws ClassNotFoundException, SQLException, IOException, UniformInterfaceException, URISyntaxException, InterruptedException, JSONException
	{	DbTools.setDbName("googleplus.full");
		DbTools.openDb(false);
	
		// init DB
//		DbTools.createTables();
		DbTools.resetTables();
		
		// get the list of persons in G+
		int limit = 1; // we just process the first few pages of G+ ids, for time reasons
		PersonExtractor.retrieveAllPersons(limit);
		// retrieve their personal info
		PersonExtractor.retrievePersonalData();
		
		// populate DB
		int threadNbr = 2; // as an example, we use 2 threads to retrieve the relationships
		RelationshipExtractor.retrieveAllRelationships(threadNbr);
		
		// export to file
		DbTools.exportRelationshipsAsEdgelist();
		DbTools.exportPersonsAsNodelist();
		
		// close DB
		DbTools.closeDb();
	}
}
