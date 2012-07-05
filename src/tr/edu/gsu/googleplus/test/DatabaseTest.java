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

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.tools.Recover;

import tr.edu.gsu.googleplus.data.Person;
import tr.edu.gsu.googleplus.data.Relationship;
import tr.edu.gsu.googleplus.postprocess.DbReformer;
import tr.edu.gsu.googleplus.tool.DbTools;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * Test the main DB methods.
 * 
 * Creating a Java application to access a Derby database
 * http://db.apache.org/derby/integrate/plugin_help/derby_app.html
 * 
 * Using Java DB in Desktop Applications
 * http://java.sun.com/developer/technicalArticles/J2SE/Desktop/javadb/
 * 
 * Basic Steps in Using JDBC
 * http://www.javacamp.org/moreclasses/jdbc/jdbc.html
 * 
 * Derby Reference Manual
 * http://db.apache.org/derby/docs/10.2/ref/
 * 
 * KeyData
 * http://www.1keydata.com/sql/sqlcreate.html
 * 
 * W3Schools
 * http://www.w3schools.com/sql/sql_foreignkey.asp
 * 
 * @since 1
 * @version 1
 * @author Vincent
 */
public class DatabaseTest
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
	 * Various tests concerning different DB-related
	 * methods.
	 * 
	 * @param arg
	 * 		Not used.
	 * 
	 * @throws ClassNotFoundException
	 * 		Problem while loading driver.
	 * @throws SQLException 
	 * 		Problem while connecting to DB or executing queries.
	 * @throws UnsupportedEncodingException
	 * 		Problem while exporting as a file.
	 * @throws FileNotFoundException 
	 * 		Problem while exporting as a file.
	 */
	public static void main(String arg[]) throws ClassNotFoundException, SQLException, FileNotFoundException, UnsupportedEncodingException
	{	logger.setName("DatabaseTest");
		DbTools.setDbName("googleplus");
	
//		testDisplay();
//		testCount();
//		testOutput();
		
//		testExport();
//		testImport();
		
//		testRecover();
		
//		testReform();
	}
	
	/**
	 * Tries displaying the DB content.
	 * 
	 * @throws ClassNotFoundException
	 * 		Problem while loading the DB driver.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void testDisplay() throws ClassNotFoundException, SQLException
	{	// open the database
		DbTools.openDb(false);
		
		// create the empty tables
//		DbTools.createTables();
		// alternatively: reset them, if they exist
	    DbTools.resetTables();
		
	    // insert data
	    DbTools.insertDummyData();
	    
	    // display tables content
	    DbTools.displayData();
		
	    // display table sizes
	    logger.log(Person.getTableSize()+" persons");
		logger.log(Relationship.getTableSize()+" relationships");
	    
		// close the database
		DbTools.closeDb();
	}

	/**
	 * Tries converting the DB.
	 * 
	 * @throws ClassNotFoundException
	 * 		Problem while loading the DB driver.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 * @throws UnsupportedEncodingException 
	 * 		Problem while recording the export file.
	 * @throws FileNotFoundException 
	 * 		Problem while recording the export file.
	 */
	public static void testReform() throws ClassNotFoundException, SQLException, FileNotFoundException, UnsupportedEncodingException
	{	// open the database
		DbTools.openDb(false);
		
		// create the empty tables
		DbTools.createTables();
		// alternatively: reset them, if they exist
//	    DbTools.resetTables();
		
	    // insert data
	    DbTools.insertDummyData();
	    
	    // display tables content
	    DbTools.displayData();
	
		// reform DB
		DbReformer.reformDb();

		for(int i=0;i<5;i++)
			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		
	    // export tables content
		DbReformer.exportReformedPersonsAsNodelist();
		DbReformer.exportReformedRelationshipsAsEdgelist();
		
		// close the database
		DbTools.closeDb();
	}
	
	/**
	 * Tries couting the number of unprocessed Persons in the DB.
	 * 
	 * @throws ClassNotFoundException
	 * 		Problem while loading the DB driver.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void testCount() throws ClassNotFoundException, SQLException
	{	// open the database
		DbTools.openDb(false);
		
		// create the empty tables
//		DbTools.createTables();
		// alternatively: reset them, if they exist
	    DbTools.resetTables();
		
	    // insert data
	    DbTools.insertDummyData();
	    
	    // display tables content
	    DbTools.displayData();
		
		// number of processed persons in the DB
		Statement statement = DbTools.connection.createStatement();
		String query = "SELECT COUNT(*) FROM PERSON WHERE PROCESSED=2";
		ResultSet temp = statement.executeQuery(query);
		temp.next();
		logger.log("Number of processed Persons: "+temp.getInt(1));
		
		// close the database
		DbTools.closeDb();
	}

	/**
	 * Tries outputing the DB content as an edgelist file.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the result file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the result file.
	 * @throws ClassNotFoundException
	 * 		Problem while loading the DB driver.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void testOutput() throws FileNotFoundException, UnsupportedEncodingException, ClassNotFoundException, SQLException
	{	// open the database
		DbTools.openDb(false);
		
		// create the empty tables
//		DbTools.createTables();
		// alternatively: reset them, if they exist
	    DbTools.resetTables();
		
	    // insert data
	    DbTools.insertDummyData();
	    
	    // display tables content
	    DbTools.displayData();

	    // export relationships
	    DbTools.exportRelationshipsAsEdgelist();
		
		// close the database
		DbTools.closeDb();
	}

	/**
	 * Tries populating the DB using an SQL script.
	 * The DB must be new.
	 * 
	 * @throws ClassNotFoundException
	 * 		Problem while loading the DB driver.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void testImport() throws ClassNotFoundException, SQLException
	{	// open the database
		DbTools.openDb(false);
		
		// NOTE: the DB must be new
		
	    // import relationships
	    //String query = "RUNSCRIPT FROM 'C:\\Eclipse\\workspaces\\Extraction\\Database\\googleplus\\databases\\googleplus.data.h2.sql'";
	    String query = "RUNSCRIPT FROM '" + DbTools.dbFullPath + DbTools.DB_EXT + "'";
		logger.log("Query: "+query);
		Statement statement = DbTools.connection.createStatement();
		statement.execute(query);
		statement.close();
		
	    // display tables content
	    DbTools.displayData();
		
		// close the database
		DbTools.closeDb();
	}
	
	/**
	 * Tries exporting the DB under the form of an SQL script.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void testExport() throws SQLException
	{	Recover recover = new Recover();
		//recover.runTool("-dir","C:\\Eclipse\\workspaces\\Extraction\\Database\\googleplus\\databases","-db","googleplus.data");
		recover.runTool("-dir",DbTools.dbFolderPath,"-db",DbTools.dbFileName);
	}
	
	/**
	 * Tries opening/closing the DB in server mode.
	 * Generally allows recovering the DB access
	 * after an incident such as a power failure.
	 * 
	 * @throws ClassNotFoundException
	 * 		Problem while loading the DB driver.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void testRecover() throws ClassNotFoundException, SQLException
	{	// open/close in server mode (in order to unlock after a power failure)
		DbTools.openCloseDbSeverMode();
	}
	
	/**
	 * To be written...
	 */
	public static void testBackup()
	{
		// backup: BACKUP TO 'backup.zip'

	}
}
