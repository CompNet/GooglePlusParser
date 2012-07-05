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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.codehaus.jettison.json.JSONException;

import com.sun.jersey.api.client.UniformInterfaceException;

import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * Test of the log features.
 * 
 * @since 1
 * @version 1
 * @author Vincent Labatut
 *
 */
public class LogTest
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
	 * 
	 * @param arg
	 * 		Not used.
	 * @throws UniformInterfaceException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @throws JSONException
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	public static void main(String arg[]) throws UniformInterfaceException, URISyntaxException, InterruptedException, JSONException, SecurityException, IOException
	{	logger.setName("LogTest");
		
		logger.log("Aaaaaaaaa");
		logger.increaseOffset();
		logger.log("Bbbbbbbbbb");
		logger.increaseOffset();
		logger.log(Arrays.asList("Ccccccccc","Dddddddd","Eeeeeeeeeee"));
		logger.decreaseOffset();
		logger.decreaseOffset();
		logger.log("Fffffffff");
	}
}
