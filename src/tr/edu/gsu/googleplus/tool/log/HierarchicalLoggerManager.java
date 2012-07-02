package tr.edu.gsu.googleplus.tool.log;

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

import java.util.HashMap;

/**
 * General manager to handle all
 * the created {@link HierarchicalLogger} objects.
 * 
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class HierarchicalLoggerManager
{	
    /////////////////////////////////////////////////////////////////
	// LOGGERS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of already created hierarchical loggers */
	private static final HashMap<String,HierarchicalLogger> loggers = new HashMap<String, HierarchicalLogger>();
	
	/**
	 * Fetches or creates a new 
	 * anonymous logger (note only
	 * one such logger can exist).
	 * 
	 * @return
	 * 		The anonymous logger.
	 */
	public static synchronized HierarchicalLogger getHierarchicalLogger()
	{	HierarchicalLogger result = getHierarchicalLogger(null);
		return result;
	}

	/**
	 * Retrieves an existing hierarchical logger
	 * from its name, or creates a new one
	 * if it does not exist.
	 * 
	 * @param name
	 * 		Name of the requested logger.
	 * @return
	 * 		The retrieved or created logger.
	 */
	public static synchronized HierarchicalLogger getHierarchicalLogger(String name)
	{	HierarchicalLogger result = loggers.get(name);
		if(result==null)
		{	result = new HierarchicalLogger(name);
			loggers.put(name,result);
		}
		return result;
	}
}
