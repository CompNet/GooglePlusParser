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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import tr.edu.gsu.googleplus.tool.DbTools;
import tr.edu.gsu.googleplus.tool.FileTools;
import tr.edu.gsu.googleplus.tool.TimeTools;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * This class was supposed to be used to convert the
 * string-based DB into an integer-based one, for speed
 * and memory reasons. It was to slow, so a purely file-based
 * approach was finaly used, implemented in {@link IdConverter}.
 *  
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class DbReformer
{	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// PROCESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Part of the cache used when reforming the DB: cache size */
	private static final Integer CACHE_SIZE = 10000;
	/** Part of the cache used when reforming the DB: maps of known ids */ 
	private static final HashMap<String,Integer> CACHE_MAP = new HashMap<String, Integer>(CACHE_SIZE);
	/** Part of the cache used when reforming the DB ordered list of */ 
	private static final LinkedList<String> CACHE_FIFO = new LinkedList<String>();

	/**
	 * The initial DB structure turns out to be very inefficient
	 * due to the use of Strings as ids. This method is an attempt 
	 * at converting this DB into a better one, relying on actual
	 * integer ids.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void reformDb() throws SQLException
	{	NumberFormat timeNf = NumberFormat.getInstance(Locale.ENGLISH);
		timeNf.setMaximumFractionDigits(2);
		int persNbr = 80080893; //Person.getTableSize();
		int relNbr = 473106758; //DbTools.getTableSize("RELATIONSHIP_OLD");
	    logger.log(persNbr+" persons");
		logger.log(relNbr+" relationships");
		
		// rename old tables
		{	logger.log("rename PERSON to PERSON_OLD");
    		String query = "ALTER TABLE PERSON RENAME TO PERSON_OLD";
			Statement statement = DbTools.connection.createStatement();
			statement.execute(query);
			statement.close();
			
			logger.log("rename RELATIONSHIP to RELATIONSHIP_OLD");
    		query = "ALTER TABLE RELATIONSHIP RENAME TO RELATIONSHIP_OLD";
			statement = DbTools.connection.createStatement();
			statement.execute(query);
			statement.close();
		}
		
		// create new tables
		{	logger.log("create new PERSON table");
    		String query = 	"CREATE TABLE PERSON (";
			query = query + 	"ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,";
			query = query + 	"GOOGLE_ID VARCHAR(22) NOT NULL,";
			query = query + 	"DATE_RETRIEVED DATE NOT NULL,";
			query = query + 	"PROCESSED SMALLINT DEFAULT 0";
			query = query + ")";
			Statement statement = DbTools.connection.createStatement();
			statement.execute(query);
			statement.close();
			
			logger.log("create new RELATIONSHIP table");
    		query =	"CREATE TABLE RELATIONSHIP (";
			query = query + 	"SOURCE_ID INTEGER NOT NULL,";
			query = query + 	"TARGET_ID INTEGER NOT NULL,";
			query = query + 	"DATE_RETRIEVED DATE NOT NULL,";
			query = query + 	"PRIMARY KEY (SOURCE_ID, TARGET_ID),";
			query = query + 	"FOREIGN KEY(SOURCE_ID) REFERENCES PERSON(ID),";
			query = query + 	"FOREIGN KEY(TARGET_ID) REFERENCES PERSON(ID)";
			query = query + ")";
			statement = DbTools.connection.createStatement();
			statement.execute(query);
			statement.close();
		}
		
		// copy old persons in new table
		{	logger.log("copy existing persons");
			// init time-related variables
			int NBR = 1000;
			int index = 0;
			long durations[] = new long[NBR];
			Arrays.fill(durations,0l);
				
			// start process
	    	Statement statement = DbTools.connection.createStatement();
		    ResultSet results = statement.executeQuery("SELECT * FROM PERSON_OLD");
		    int r = 1;
		    while(results.next())
		    {	long before = System.currentTimeMillis();
				
		    	// get data
		    	int c = 1;
		    	String googleId = results.getString(c);c++;
		        Date dateRetrieved = results.getDate(c);c++;
		        String firstName = results.getString(c);c++;
		        String lastName = results.getString(c);c++;
		        int state = results.getInt(c);c++;
		        
		    	// insert data
		    	{	String query = 	"INSERT INTO PERSON (GOOGLE_ID,DATE_RETRIEVED,PROCESSED) VALUES ('"+googleId+"','"+dateRetrieved+"','"+state+"')";
		    		Statement statement2 = DbTools.connection.createStatement();
					statement2.execute(query);
					statement2.close();		    		
		    	}
				long after = System.currentTimeMillis();

				// log
				long elapsed = after - before;
				durations[index] = elapsed;
				index = (index + 1) % NBR;
				double total = 0;
				for(int i=0;i<NBR;i++) total = total + durations[i];
				double avrgTime = total / NBR;
				String avrgTimeStr = timeNf.format(avrgTime);
				long expectedTime = Math.round((relNbr-r) * avrgTime);
				String expectedTimeStr = TimeTools.formatDuration(expectedTime);
				logger.log("..[" + avrgTimeStr + " ms - " + expectedTimeStr + "] " + r + "/" + relNbr + ".\t" 
	        		+ googleId + "\t\t" 
	        		+ firstName + "\t\t" 
	        		+ lastName + "\t\t" 
	        		+ dateRetrieved + "\t\t" 
	        		+ state);
		    	
		        r++;
		        
		    }
		    results.close();
		    statement.close();
		}
		
		// copy old relationships in new table
		{	logger.log("copy existing relationships");
			CACHE_MAP.clear();
			CACHE_FIFO.clear();
			// init time-related variables
			int NBR = 1000;
			int index = 0;
			long durations[] = new long[NBR];
			Arrays.fill(durations,0l);
		
	    	Statement statement = DbTools.connection.createStatement();
		    ResultSet results = statement.executeQuery("SELECT * FROM RELATIONSHIP_OLD");
		    int r = 1;
		    while(results.next())
		    {	long before = System.currentTimeMillis();
			
	    		// get data
		    	int c = 1;
		    	String sourceGoogleId = results.getString(c);c++;
		    	String targetGoogleId = results.getString(c);c++;
		        Date date = results.getDate(c);c++;
		        logger.log(r + ".\t" + sourceGoogleId + "\t\t" + targetGoogleId + "\t\t" + date);
		        // get ids
		        int sourceId = getId(sourceGoogleId);
		        int targetId =  getId(targetGoogleId);
		         // insert data
		    	{	String query = 	"INSERT INTO RELATIONSHIP (SOURCE_ID,TARGET_ID,DATE_RETRIEVED) VALUES ('"+sourceId+"','"+targetId+"','"+date+"')";
		    		Statement statement2 = DbTools.connection.createStatement();
					statement2.execute(query);
					statement2.close();
		    	}
				long after = System.currentTimeMillis();
				
				// log
				long elapsed = after - before;
				durations[index] = elapsed;
				index = (index + 1) % NBR;
				double total = 0;
				for(int i=0;i<NBR;i++) total = total + durations[i];
				double avrgTime = total / NBR;
				String avrgTimeStr = timeNf.format(avrgTime);
				long expectedTime = Math.round((relNbr-r) * avrgTime);
				String expectedTimeStr = TimeTools.formatDuration(expectedTime);
				logger.log("..[" + avrgTimeStr + " ms - " + expectedTimeStr + "] " + r + "/" + persNbr + ".\t" 
	        		+ sourceId + " (" + sourceGoogleId + ")" + "\t\t" 
	        		+ targetId + " (" + targetGoogleId + ")");
		    	
		        r++;
		    }
		    results.close();
		    statement.close();
		}
		
		// remove old tables
		{	// remove relationship table
			{	logger.log("remove RELATIONSHIP_OLD table");
				String query = 	"DROP TABLE RELATIONSHIP_OLD";
				Statement statement = DbTools.connection.createStatement();
				statement.execute(query);
				statement.close();		
			}
			
			// remove person table
			{	logger.log("remove PERSON_OLD table");
				String query = 	"DROP TABLE PERSON_OLD";
				Statement statement = DbTools.connection.createStatement();
				statement.execute(query);
				statement.close();
			}
		}		
	}
	
	private static int getId(String gid) throws SQLException
    {	// check if the id is already in the cache
		Integer result = CACHE_MAP.get(gid);
		if(result!=null)
		{	// just update the cache
			CACHE_FIFO.remove(gid);
			CACHE_FIFO.offer(gid);
		}
		
    	// otherwise, retrieve it from the DB and update the cache
		else
		{	// query the DB
			String query =	"SELECT ID FROM PERSON WHERE GOOGLE_ID='"+gid+"'";
			Statement statement = DbTools.connection.createStatement();
		    ResultSet results = statement.executeQuery(query);
		    results.next();
		    result = results.getInt(1);
			statement.close();
			
			// possibly make some room
			if(CACHE_FIFO.size()==CACHE_SIZE)
			{	String temp = CACHE_FIFO.poll();
				CACHE_FIFO.remove(temp);
			}
			// cache the value
			CACHE_MAP.put(gid,result);
			CACHE_FIFO.offer(gid);
		}
		
///		logger.log("CACHE: "+gid+" >> "+result);
		
		return result;
	}

	/**
	 * Exports the full content of
	 * the reformed RELATIONSHIP table
	 * as an edgelist file.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the edgelist file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the edgelist file.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void exportReformedRelationshipsAsEdgelist() throws FileNotFoundException, UnsupportedEncodingException, SQLException
	{	logger.log("Exporting relationships");
		
		// init file
		String path = FileTools.NETWORKS_FOLDER + File.separator + DbTools.dbName + ".ref.edgelist";
		FileOutputStream fileOut = new FileOutputStream(path);
		OutputStreamWriter writer = new OutputStreamWriter(fileOut,"UTF-8");
		PrintWriter printWriter = new PrintWriter(writer);
    	logger.log("File: "+path);

		// fill with relationships
		Statement statement = DbTools.connection.createStatement();
//        ResultSet results = statement.executeQuery("SELECT * FROM RELATIONSHIP_OLD");
		ResultSet results = statement.executeQuery("SELECT * FROM RELATIONSHIP");
        int count = 0;
        while(results.next())
        {	count++;
        	logger.log("Relationship #"+count);
    		int c = 1;
        	int sourceId = results.getInt(c);c++;
        	int targetId = results.getInt(c);c++;
        	printWriter.println(sourceId + "\t" + targetId);
        }
        results.close();
        statement.close();
        
        // close file
		printWriter.close();
	}

	/**
	 * Exports the full content of
	 * the reformed PERSON table
	 * as a nodelist file.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the nodelist file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the nodelist file.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void exportReformedPersonsAsNodelist() throws FileNotFoundException, UnsupportedEncodingException, SQLException
	{	logger.log("Exporting persons");
		
		// init file
		String path = FileTools.NETWORKS_FOLDER + File.separator + DbTools.dbName + ".ref.nodelist";
		FileOutputStream fileOut = new FileOutputStream(path);
		OutputStreamWriter writer = new OutputStreamWriter(fileOut,"UTF-8");
		PrintWriter printWriter = new PrintWriter(writer);

		// fill with relationships
		Statement statement = DbTools.connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT * FROM PERSON");
        while(results.next())
        {	int c = 1;
    		int id = results.getInt(c);c++;
    		String gid = results.getString(c);c++;
            printWriter.println(id + "\t" + gid);
        }
        results.close();
        statement.close();
        
        // close file
		printWriter.close();
	}
}
