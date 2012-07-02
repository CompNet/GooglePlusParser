package tr.edu.gsu.googleplus.test;

/*
 * Google+ Network Extractor
 * Copyright 2011 Vincent Labatut 
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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;

import com.sun.jersey.api.client.UniformInterfaceException;

import tr.edu.gsu.googleplus.data.Person;
import tr.edu.gsu.googleplus.data.Relationship;
import tr.edu.gsu.googleplus.parser.GooglePlusParser;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * Test various parser methods.
 * 
 * @since 1
 * @version 1
 * @author Vincent Labatut
 *
 */
public class ParserTest
{	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// PROCESSING	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	private static final String JG_ID = "114074126238467553553"; // original author of the parser
	private static final String JG_ID = "105143088102952395556"; // me
	
	/**
	 * Various tests conducted on the parser.
	 * 
	 * @param arg
	 * 		Not used.
	 * @throws UniformInterfaceException
	 * 		Problem while using the parser.
	 * @throws MalformedURLException
	 * 		Problem while using the parser.
	 * @throws URISyntaxException
	 * 		Problem while using the parser.
	 * @throws InterruptedException
	 * 		Problem while using the parser.
	 * @throws JSONException
	 * 		Problem while using the parser.
	 */
	public static void main(String arg[]) throws UniformInterfaceException, MalformedURLException, URISyntaxException, InterruptedException, JSONException
	{	logger.setName("ParserTest");
		
		logger.log("Test person");
		testPerson();
		
		logger.log("Test followers");
		testFollowers();
		
		logger.log("Test followees");
		testFollowees();
	}
	
	/**
	 * Retrieves the info of a single G+ user.
	 * 
	 * @throws UniformInterfaceException
	 * 		Problem while using the parser.
	 * @throws MalformedURLException
	 * 		Problem while using the parser.
	 * @throws URISyntaxException
	 * 		Problem while using the parser.
	 * @throws InterruptedException
	 * 		Problem while using the parser.
	 * @throws JSONException
	 * 		Problem while using the parser.
	 */
	private static void testPerson() throws UniformInterfaceException, MalformedURLException, URISyntaxException, InterruptedException, JSONException
	{	Person resolvedPerson = GooglePlusParser.extractPerson(JG_ID);
		if(resolvedPerson==null)
			logger.log("ERROR: gid "+JG_ID+" does not exist");
		else
			logger.log("Name: " + resolvedPerson.getFirstname() + " " + resolvedPerson.getLastname());
	}

	/**
	 * Retrieves the followers of a given G+ user.
	 * 
	 * @throws UniformInterfaceException
	 * 		Problem while using the parser.
	 * @throws MalformedURLException
	 * 		Problem while using the parser.
	 * @throws URISyntaxException
	 * 		Problem while using the parser.
	 * @throws InterruptedException
	 * 		Problem while using the parser.
	 * @throws JSONException
	 * 		Problem while using the parser.
	 */
	private static void testFollowers() throws UniformInterfaceException, MalformedURLException, URISyntaxException, InterruptedException, JSONException
	{	Set<Relationship> followers = GooglePlusParser.extractFollowers(JG_ID);
		
		logger.log("Followers:");
		logger.increaseOffset();
		int count = 0;
		for (Relationship r : followers) {
			Person p = GooglePlusParser.extractPerson(r.getSourceId());
			if(p==null)
				logger.log("ERROR: gid "+r.getSourceId()+" does not exist");
			else
				logger.log(p.toString());
			count++;
		}
		logger.decreaseOffset();
		logger.log("Total: "+count);
	}

	/**
	 * Retrieves the followees of a given G+ user.
	 * 
	 * @throws UniformInterfaceException
	 * 		Problem while using the parser.
	 * @throws MalformedURLException
	 * 		Problem while using the parser.
	 * @throws URISyntaxException
	 * 		Problem while using the parser.
	 * @throws InterruptedException
	 * 		Problem while using the parser.
	 * @throws JSONException
	 * 		Problem while using the parser.
	 */
	private static void testFollowees() throws UniformInterfaceException, MalformedURLException, URISyntaxException, InterruptedException, JSONException
	{	Set<Relationship> followees = GooglePlusParser.extractFollowees(JG_ID);
	
		logger.log("Followees:");
		logger.increaseOffset();
		int count = 0;
		for (Relationship r : followees) {
			Person p = GooglePlusParser.extractPerson(r.getTargetId());
			if(p==null)
				logger.log("ERROR: gid "+r.getSourceId()+" does not exist");
			else
				logger.log(p.toString());
			count++;
		}
		logger.decreaseOffset();
		logger.log("Total: "+count);
	}
}
