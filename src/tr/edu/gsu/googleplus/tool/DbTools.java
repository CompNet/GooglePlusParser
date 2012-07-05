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
}
