package tr.edu.gsu.googleplus.data;

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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.h2.constant.ErrorCode;
import org.h2.jdbc.JdbcSQLException;

import tr.edu.gsu.googleplus.tool.DbTools;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * Represents a Google+ relationship
 * between two users. The relationship
 * is directed from a source user towards
 * a target user.
 * 
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class Relationship implements Comparable<Relationship>
{	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// DATE RETRIEVED	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** date this relationship data was retrieved */
	private Date dateRetrieved;
	
	/**
	 * Returns the date this relationship
	 * data was retrieved.
	 * 
	 * @return
	 * 		Date this relationship data was retrieved.
	 */
	public Date getDateRetrieved()
	{	return dateRetrieved;
	}

	/**
	 * Changes the date this relationship
	 * data was retrieved.
	 * 
	 * @param dateRetrieved
	 * 		The new date for this relationship.
	 */
	public void setDateRetrieved(Date dateRetrieved)
	{	this.dateRetrieved = dateRetrieved;
	}

	/////////////////////////////////////////////////////////////////
	// SOURCE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Google+ user constituting the source of this relationship (i.e. follower) */
	private String sourceId;
	
	/**
	 * Returns the source user
	 * of this relationship.
	 * 
	 * @return
	 * 		Source user of this relationship.
	 */
	public String getSourceId()
	{	return sourceId;
	}

	/**
	 * Changes the source user
	 * of this relationship.
	 * 
	 * @param sourceId
	 * 		The new source user of this relationship.
	 */
	public void setSourceId(String sourceId)
	{	this.sourceId = sourceId;
	}

	/////////////////////////////////////////////////////////////////
	// TARGET			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Google+ user constituting the target of this relationship (i.e. followee) */
	private String targetId;
	
	/**
	 * Returns the target user
	 * of this relationship.
	 * 
	 * @return
	 * 		Target user of this relationship.
	 */
	public String getTargetId()
	{	return targetId;
	}

	/**
	 * Changes the target user
	 * of this relationship.
	 * 
	 * @param targetId
	 * 		The new target user of this relationship.
	 */
	public void setTargetId(String targetId)
	{	this.targetId = targetId;
	}

	/////////////////////////////////////////////////////////////////
	// STRENGTH			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** strength of the relationship */
	private Float strength;
	
	/**
	 * Returns the strength
	 * of this relationship.
	 * 
	 * @return
	 * 		Strength of this relationship.
	 */
	public Float getStrength()
	{	return strength;
	}

	/**
	 * Changes the strength
	 * of this relationship.
	 * 
	 * @param strength
	 * 		The new strength of this relationship.
	 */
	public void setStrength(Float strength)
	{	this.strength = strength;
	}

	/////////////////////////////////////////////////////////////////
	// PERSONS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Returns the set of user ids
	 * cited in at list one of the 
	 * specified relationships.
	 * 
	 * @param relationships
	 * 		A list of relationships.
	 * @return
	 * 		The set of corresponding ids.
	 */
	public static Set<String> getIdsFromRelationships(Collection<Relationship> relationships)
	{	Set<String> result = new TreeSet<String>();
		for(Relationship relationship: relationships)
		{	String sourceId = relationship.getSourceId();
			result.add(sourceId);
			String targetId = relationship.getTargetId();
			result.add(targetId);
		}
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public int compareTo(Relationship o)
	{	int result = sourceId.compareTo(o.getSourceId());
		if(result==0)
			result = targetId.compareTo(o.getTargetId());
		
		return result;
	}
	
	@Override
	public boolean equals(Object o)
	{	boolean result = false;
		
		if(o==null && o instanceof Relationship)
		{	Relationship r = (Relationship)o;
			result = compareTo(r)==0;
		}
		
		return result;
	}
	
	@Override
	public int hashCode()
	{	String id = sourceId + ">" + targetId;
		int result = id.hashCode();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATABASE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** for statistical purposes : average access time */
	public static double insertDbAverageTime = 0;
	/** for statistical purposes : number of calls to this function */
	private static float insertDbCallCount = 0;
	/** associated lock */
	private static Lock insertDbLock = new ReentrantLock();

	/**
	 * Returns the average time to insert 
	 * a relationship in the DB.
	 * 
	 * @return
	 * 		The average time in ms.
	 */
	public static double getInsertDbAverageTime()
	{	double result;
		insertDbLock.lock();
		result = insertDbAverageTime;
		insertDbLock.unlock();
		return result;
	}

	/**
	 * Deletes the table meant to contain
	 * the {@link Relationship} data.
	 *  
	 * @throws SQLException
	 * 		Problem while accessign the DB.
	 */
	public static void dropTable() throws SQLException
	{	String query = 	"DROP TABLE RELATIONSHIP";
		Statement statement = DbTools.connection.createStatement();
		statement.execute(query);
		statement.close();
	}

	/**
	 * Creates the table meant to contain
	 * the {@link Relationship} data.
	 *  
	 * @throws SQLException
	 * 		Problem while accessign the DB.
	 */
	public static void createTable() throws SQLException
	{	String query =	"CREATE TABLE RELATIONSHIP (";
		query = query + 	"SOURCE_ID VARCHAR(256) NOT NULL,";
		query = query + 	"TARGET_ID VARCHAR(256) NOT NULL,";
		query = query + 	"DATE_RETRIEVED DATE NOT NULL,";
		query = query + 	"PRIMARY KEY (SOURCE_ID, TARGET_ID),";
		query = query + 	"FOREIGN KEY(SOURCE_ID) REFERENCES PERSON(ID),";
		query = query + 	"FOREIGN KEY(TARGET_ID) REFERENCES PERSON(ID)";
		query = query + ")";
		Statement statement = DbTools.connection.createStatement();
		statement.execute(query);
		statement.close();
	}

	/**
	 * Displays the whole content of the
	 * DB's RELATIONSHIP table.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void displayTable() throws SQLException
	{	logger.log(".: RELATIONSHIPS :.");
		Statement statement = DbTools.connection.createStatement();
	    ResultSet results = statement.executeQuery("SELECT * FROM RELATIONSHIP");
	    ResultSetMetaData meta = results.getMetaData();
	    int cols = meta.getColumnCount();
	    logger.log("\t");
	    for (int c=1; c<=cols; c++)
	    {	logger.log(meta.getColumnLabel(c)+"\t\t");  
	    }
	    logger.log("\n-------------------------------------------------");
	    int r = 1;
	    while(results.next())
	    {	int c = 1;
	    	String sourceId = results.getString(c);c++;
	    	String targetId = results.getString(c);c++;
	        Date date = results.getDate(c);c++;
	        logger.log(r + ".\t" + sourceId + "\t\t" + targetId + "\t\t" + date);
	        r++;
	    }
	    results.close();
	    statement.close();
	}
	
	/**
	 * Builds a {@ode Relationship} object
	 * from the data retrieved from
	 * the DB. This data is specified
	 * through the {@code resultSet} parameter.
	 * 
	 * @param resultSet
	 * 		Data retrieved from the DB.
	 * @return
	 * 		The corresponding {@code Relationship} object.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static Relationship build(ResultSet resultSet) throws SQLException
	{	Relationship result = new Relationship();
		int c = 1;
    	result.sourceId = resultSet.getString(c);c++;
    	result.targetId = resultSet.getString(c);c++;
        result.dateRetrieved = resultSet.getDate(c);c++;
		return result;
	}

	/**
	 * Returns the number of relationships
	 * registered in the RELATIONSHIP table.
	 * 
	 * @return
	 * 		Number of relationships in the DB.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static int getTableSize() throws SQLException
	{	String tableName = "RELATIONSHIP";
		int result = DbTools.getTableSize(tableName);
		return result;
	}
	
	/**
	 * Insert this relationship in
	 * the database.
	 * 
	 * @throws SQLException
	 * 		If the insertion fails.
	 */
	public void insertDb() throws SQLException
	{	long before = System.currentTimeMillis();
		// check if both users exist
		Person source = Person.retrieveFromId(sourceId);
		if(source==null)
			Person.insertDb(sourceId);
		Person target = Person.retrieveFromId(targetId);
		if(target==null)
			Person.insertDb(targetId);
		
		// check if relationship already exists
		Statement statement = null;
		try
		{	// current date
			java.util.Date utilDate = new java.util.Date();
			Date sqlDate = new java.sql.Date(utilDate.getTime());
			String date = sqlDate.toString();
			
			// insertion
			String query = 	"INSERT INTO RELATIONSHIP (SOURCE_ID,TARGET_ID,DATE_RETRIEVED) VALUES ('"+sourceId+"','"+targetId+"','"+date+"')";
			statement = DbTools.connection.createStatement();
			statement.execute(query);
			statement.close();
		}
		catch(JdbcSQLException e)
		{	statement.close();
			//e.printStackTrace();
			if(e.getErrorCode()==ErrorCode.DUPLICATE_KEY_1)
				logger.log("WARNING: relationship ["+this+"] already present in the DB");
			else
				throw e;
		}
//		try
//		{	Thread.sleep(5000);
//		}
//		catch (InterruptedException e)
//		{	e.printStackTrace();
//		}
		long after = System.currentTimeMillis();

		double elapsed = after-before;
		insertDbLock.lock();
		insertDbAverageTime = insertDbAverageTime*(insertDbCallCount/(insertDbCallCount+1)) 
			+ elapsed/(insertDbCallCount+1);
		insertDbCallCount++;
		insertDbLock.unlock();
	}
	
	/////////////////////////////////////////////////////////////////
	// STRING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = sourceId + ">>" + targetId;
		return result;
	}
}
