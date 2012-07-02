package tr.edu.gsu.googleplus.tool;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import org.h2.tools.Server;

import tr.edu.gsu.googleplus.data.Person;
import tr.edu.gsu.googleplus.data.Relationship;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * This class contains various methods
 * related to database management.
 *  
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class DbTools
{	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// CONNECTION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the DB */
	public static String dbName = null;
	/** Filename of the DB */
	public static String dbFileName = null;
	/** File extension of the DB */
	public final static String DB_EXT = ".h2.sql";
	/** Folder containing the DB files */
	public static String dbFolderPath = null;
	/** Full path of the DB */
	public static String dbFullPath = null;
	/** Full URL of the DB */
//	public static String dbUrl = "jdbc:derby:C:\\Eclipse\\workspaces\\Extraction\\Database\\googleplus.data;create=true";
//	public static String dbUrl = "jdbc:h2:/home/vlabatut/eclipse/workspaces/Extraction/Database/googleplus/databases/googleplus.data;create=true";
//	public static String dbUrl = "jdbc:h2:/home/vlabatut/eclipse/workspaces/Extraction/Database/googleplus/databases/googleplus.bis.data;create=true";
//	public static String dbUrl = "jdbc:h2:C:\\Eclipse\\workspaces\\Extraction\\Database\\googleplus\\databases\\googleplus.data;create=true";
	public static String dbUrl = null;
	/** Current connection to the DB */
	public static Connection connection = null;
	static { setDbName("googleplus");}
	
	/**
	 * Changes the DB name. By default, it is
	 * "googleplus". This must be done before
	 * the DB is opened.
	 * 
	 * @param name
	 * 		The new DB name.
	 */
	public static void setDbName(String name)
	{	dbName= name;
		dbFileName = dbName + ".data";
		dbFolderPath = FileTools.ROOT_FOLDER + File.separator + FileTools.RES_FOLDER + File.separator + "database";
		dbFullPath = dbFolderPath + File.separator + dbFileName;
		dbUrl = "jdbc:h2:" + dbFullPath + ";create=true";
	}
	
	/**
	 * Open a connection to the DB.
	 * 
	 * @param concurrent 
	 * 		If {@code true}, H2 concurrent feature is used. 
	 * @throws ClassNotFoundException
	 * 		Problem while accessing the DB driver.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void openDb(boolean concurrent) throws ClassNotFoundException, SQLException
	{	if(connection==null)
		{	// load DB driver
			logger.log("Load JDBC driver");
		    //Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		    Class.forName("org.h2.Driver");
		    
		    // DB connection
			logger.log("Connect to DB");
			String url = dbUrl;
			if(concurrent)
				url = url + ";MVCC=TRUE";
		    connection = DriverManager.getConnection(url);
		}
	}
	
	/**
	 * Allows to open/close the DB in server mode.
	 * Useful in order to unlock it after a power failure.
	 * 
	 * @throws ClassNotFoundException
	 * 		Problem while accessing the DB driver.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void openCloseDbSeverMode() throws ClassNotFoundException, SQLException
	{	if(connection==null)
		{	// init server
			Server server = Server.createTcpServer(new String[]{"-tcpAllowOthers"}).start();
		
			// load DB driver
			logger.log("Load JDBC driver");
		    //Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		    Class.forName("org.h2.Driver");
		    
		    // DB connection
			logger.log("Connect to DB");
			//String url = "jdbc:h2:tcp://localhost/~/eclipse/workspaces/Extraction/Database/googleplus/databases/googleplus.data";
			
		    connection = DriverManager.getConnection(dbUrl);
		    
		    // close connection and server
		    connection.close();
		    server.stop();
		}
	}
	
	/**
	 * Closes the current connection
	 * to the DB. 
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void closeDb() throws SQLException
	{	logger.log("Close connection");
	    // http://db.apache.org/derby/docs/10.8/getstart/getstart-single.html#rwwdactivity3
	    //DriverManager.getConnection(url + ";shutdown=true");
	    connection.close();
	}

	/////////////////////////////////////////////////////////////////
	// STRUCTURE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Remove both PERSON and RELATIONSHIP
	 * tables from the DB and creates
	 * them again.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void resetTables() throws SQLException
	{	logger.log("Reset tables");
		logger.increaseOffset();
		
		// remove tables
		removeTables();
		// create tables
		createTables();
		
		logger.decreaseOffset();
	}
	
	/**
	 * Creates both PERSON and RELATIONSHIP
	 * tables in the DB.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void createTables() throws SQLException
	{	// create person table
		logger.log("Create PERSON table");
		Person.createTable();
		
		// create relationship table
		logger.log("Create RELATIONSHIP table");
		Relationship.createTable();
	}
	
	/**
	 * Removes both PERSON and RELATIONSHIP
	 * tables from the DB.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void removeTables() throws SQLException
	{	// remove relationship table
		logger.log("Drop RELATIONSHIP table");
		Relationship.dropTable();
		
		// remove person table
		logger.log("Drop PERSON table");
		Person.dropTable();
	}
	
	/////////////////////////////////////////////////////////////////
	// CONTENT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Inserts somme fake data in
	 * the DB in order to
	 * test various methods.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void insertDummyData() throws SQLException
	{	logger.log("Insert dummy content");
		logger.increaseOffset();
		
		java.util.Date utilDate = new java.util.Date();
		Date sqlDate = new java.sql.Date(utilDate.getTime());
		String date = sqlDate.toString();
		
		// insert a	few persons
		{	String data[][] = 
			{	{"1123784561234561234565",date,"AAAAA","aaaaa","0"},
				{"2845616548465164651685",date,"BBBBB","bbbbb","1"},
				{"3746846516453156486486",date,"CCCCC","ccccc","2"},
				{"4846546548645646846841",date,"DDDDD","ddddd","2"},
				{"5178291815178155912142",date,"EEEEE","eeeee","2"},
				{"6189465196848974351323",date,"FFFFF","fffff","2"}
			};
			for(String d[]: data)
			{	String query = "INSERT INTO PERSON VALUES ('"+d[0]+"', '"+d[1]+"', '"+d[2]+"', '"+d[3]+"', "+d[4]+")";
				logger.log(query);
				Statement statement = connection.createStatement();
				statement.execute(query);
				statement.close();
			}
		}
		
		// inser a few relationships
		{	String data[][] = 
			{	{"1123784561234561234565","2845616548465164651685",date},
				{"1123784561234561234565","3746846516453156486486",date},
				{"3746846516453156486486","1123784561234561234565",date},
				{"4846546548645646846841","1123784561234561234565",date},
				{"4846546548645646846841","2845616548465164651685",date},
				{"4846546548645646846841","5178291815178155912142",date},
				{"5178291815178155912142","4846546548645646846841",date},
				{"6189465196848974351323","2845616548465164651685",date},
				{"6189465196848974351323","5178291815178155912142",date}
			};
			for(String d[]: data)
			{	String query = "INSERT INTO RELATIONSHIP VALUES ('"+d[0]+"', '"+d[1]+"', '"+d[2]+"')";
				Statement statement = connection.createStatement();
				statement.execute(query);
				statement.close();
			}
		}
		
		logger.decreaseOffset();
	}
	
	/**
	 * Returns the number of rows
	 * in the specified table.
	 * 
	 * @param tableName 
	 * 		Name of the concerned table.
	 * @return
	 * 		Number of rows in the DB.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static int getTableSize(String tableName) throws SQLException
	{	Statement statement = DbTools.connection.createStatement();
		String query = "SELECT COUNT(*) FROM "+tableName+"";
		ResultSet resultSet = statement.executeQuery(query);
		resultSet.next();
		int result = resultSet.getInt(1);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// EXPORT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Displays the full content of the DB
	 * on the console.
	 * 
	 * @throws SQLException 
	 * 		Problem while accessing the DB.
	 */
	public static void displayData() throws SQLException
	{	logger.log("Display content");
		logger.increaseOffset();

		// display person table
		Person.outputTable();
		
		// display relationship table
		Relationship.displayTable();
		
		logger.decreaseOffset();
	}
	
	/**
	 * Exports the full content of
	 * the RELATIONSHIP table
	 * as an edgelist file.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the edgelist file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the edgelist file.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void exportRelationshipsAsEdgelist() throws FileNotFoundException, UnsupportedEncodingException, SQLException
	{	logger.log("Exporting relationships");
		
		// init file
		String path = FileTools.NETWORKS_FOLDER + File.separator + dbName + ".edgelist";
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
        	Relationship relationship = Relationship.build(results);
        	printWriter.println(relationship.getSourceId() + "\t" + relationship.getTargetId());
        }
        results.close();
        statement.close();
        
        // close file
		printWriter.close();
	}
	
	/**
	 * Exports the full content of
	 * the PERSON table
	 * as a nodelist file.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the nodelist file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the nodelist file.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void exportPersonsAsNodelist() throws FileNotFoundException, UnsupportedEncodingException, SQLException
	{	logger.log("Exporting persons");
		
		// init file
		String path = FileTools.NETWORKS_FOLDER + File.separator + dbName + ".nodelist";
		FileOutputStream fileOut = new FileOutputStream(path);
		OutputStreamWriter writer = new OutputStreamWriter(fileOut,"UTF-8");
		PrintWriter printWriter = new PrintWriter(writer);

		// fill with relationships
		Statement statement = DbTools.connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT * FROM PERSON");
        while(results.next())
        {	Person person = Person.build(results);
            printWriter.println(person.getId() + "\t" + person.getFirstname() + "\t" + person.getLastname());
        }
        results.close();
        statement.close();
        
        // close file
		printWriter.close();
	}
	
	/////////////////////////////////////////////////////////////////
	// REFORM			/////////////////////////////////////////////
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
//		{	logger.log("rename PERSON to PERSON_OLD");
//    		String query = "ALTER TABLE PERSON RENAME TO PERSON_OLD";
//			Statement statement = DbTools.connection.createStatement();
//			statement.execute(query);
//			statement.close();
//			
//			logger.log("rename RELATIONSHIP to RELATIONSHIP_OLD");
//    		query = "ALTER TABLE RELATIONSHIP RENAME TO RELATIONSHIP_OLD";
//			statement = DbTools.connection.createStatement();
//			statement.execute(query);
//			statement.close();
//		}
		
		// create new tables
//		{	logger.log("create new PERSON table");
//    		String query = 	"CREATE TABLE PERSON (";
//			query = query + 	"ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,";
//			query = query + 	"GOOGLE_ID VARCHAR(22) NOT NULL,";
//			query = query + 	"DATE_RETRIEVED DATE NOT NULL,";
//			query = query + 	"PROCESSED SMALLINT DEFAULT 0";
//			query = query + ")";
//			Statement statement = DbTools.connection.createStatement();
//			statement.execute(query);
//			statement.close();
//			
//			logger.log("create new RELATIONSHIP table");
//    		query =	"CREATE TABLE RELATIONSHIP (";
//			query = query + 	"SOURCE_ID INTEGER NOT NULL,";
//			query = query + 	"TARGET_ID INTEGER NOT NULL,";
//			query = query + 	"DATE_RETRIEVED DATE NOT NULL,";
//			query = query + 	"PRIMARY KEY (SOURCE_ID, TARGET_ID),";
//			query = query + 	"FOREIGN KEY(SOURCE_ID) REFERENCES PERSON(ID),";
//			query = query + 	"FOREIGN KEY(TARGET_ID) REFERENCES PERSON(ID)";
//			query = query + ")";
//			statement = DbTools.connection.createStatement();
//			statement.execute(query);
//			statement.close();
//		}
		
		// copy old persons in new table
//		{	logger.log("copy existing persons");
//			// init time-related variables
//			int NBR = 1000;
//			int index = 0;
//			long durations[] = new long[NBR];
//			Arrays.fill(durations,0l);
//				
//			// start process
//	    	Statement statement = DbTools.connection.createStatement();
//		    ResultSet results = statement.executeQuery("SELECT * FROM PERSON_OLD");
//		    int r = 1;
//		    while(results.next())
//		    {	long before = System.currentTimeMillis();
//				
//		    	// get data
//		    	int c = 1;
//		    	String googleId = results.getString(c);c++;
//		        Date dateRetrieved = results.getDate(c);c++;
//		        String firstName = results.getString(c);c++;
//		        String lastName = results.getString(c);c++;
//		        int state = results.getInt(c);c++;
//		        
//		    	// insert data
//		    	{	String query = 	"INSERT INTO PERSON (GOOGLE_ID,DATE_RETRIEVED,PROCESSED) VALUES ('"+googleId+"','"+dateRetrieved+"','"+state+"')";
//		    		Statement statement2 = DbTools.connection.createStatement();
//					statement2.execute(query);
//					statement2.close();		    		
//		    	}
//				long after = System.currentTimeMillis();
//
//				// log
//				long elapsed = after - before;
//				durations[index] = elapsed;
//				index = (index + 1) % NBR;
//				double total = 0;
//				for(int i=0;i<NBR;i++) total = total + durations[i];
//				double avrgTime = total / NBR;
//				String avrgTimeStr = timeNf.format(avrgTime);
//				long expectedTime = Math.round((relNbr-r) * avrgTime);
//				String expectedTimeStr = TimeTools.formatDuration(expectedTime);
//				logger.log("..[" + avrgTimeStr + " ms - " + expectedTimeStr + "] " + r + "/" + relNbr + ".\t" 
//	        		+ googleId + "\t\t" 
//	        		+ firstName + "\t\t" 
//	        		+ lastName + "\t\t" 
//	        		+ dateRetrieved + "\t\t" 
//	        		+ state);
//		    	
//		        r++;
//		        
//		    }
//		    results.close();
//		    statement.close();
//		}
		
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
//		{	// remove relationship table
//			{	logger.log("remove RELATIONSHIP_OLD table");
//				String query = 	"DROP TABLE RELATIONSHIP_OLD";
//				Statement statement = DbTools.connection.createStatement();
//				statement.execute(query);
//				statement.close();		
//			}
//			
//			// remove person table
//			{	logger.log("remove PERSON_OLD table");
//				String query = 	"DROP TABLE PERSON_OLD";
//				Statement statement = DbTools.connection.createStatement();
//				statement.execute(query);
//				statement.close();
//			}
//		}		
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
		String path = FileTools.NETWORKS_FOLDER + File.separator + dbName + ".ref.edgelist";
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
		String path = FileTools.NETWORKS_FOLDER + File.separator + dbName + ".ref.nodelist";
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
