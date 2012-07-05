package tr.edu.gsu.googleplus;

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

import java.io.IOException;
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
 * Main class of the program, it allows launching the various 
 * steps of the process:
 * <ul> <li>creating and initializing the DB</li?
 * 	    <li>retrieving the necessary files from google</li>
 * 		<li>parsing the personal G+ pages and populating the DB</li>
 * 		<li>exporting the resulting network as an edgelist file</li>
 * </ul>
 * Cf. the JavaDoc for more information.<br><br>
 * 
 * As of October 2011, the program was working fine. But Google
 * then introduced a captcha-based protection when the number of
 * requests on G+ was too important. This is the case with this application,
 * especially if one wants to retrieve a substantial part of the
 * social network. In other terms, this program might not work fast
 * anymore. 
 * 
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class Launcher
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
	 * Retrieves the full G+ network using 32 threads,
	 * stores it in the DB and exports it as edgelist and
	 * nodelist files.
	 * 
	 * @param arg
	 * 		Not used.
	 * @throws IOException 
	 * 		Problem while retrieving the network.
	 * @throws SQLException 
	 * 		Problem while loading the DB driver.
	 * @throws ClassNotFoundException 
	 * 		Problem while loading the DB driver.
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
	{	// set up logger
		logger.setName("FullExtraction");
	
		// oprn DB
		DbTools.setDbName("googleplus");
		DbTools.openDb(false);
	
		// init DB
		DbTools.createTables();
		
		// get the list of persons in G+
		PersonExtractor.retrieveAllPersons(0);
		// retrieve their personal info
		PersonExtractor.retrievePersonalData();
		
		// populate the DB
		RelationshipExtractor.retrieveAllRelationships(32);
		
		// export to file
		DbTools.exportRelationshipsAsEdgelist();
		DbTools.exportPersonsAsNodelist();
		
		// close DB
		DbTools.closeDb();
	}
}
