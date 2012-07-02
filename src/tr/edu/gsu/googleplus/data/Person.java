package tr.edu.gsu.googleplus.data;

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

import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.h2.constant.ErrorCode;
import org.h2.jdbc.JdbcSQLException;

import tr.edu.gsu.googleplus.tool.DbTools;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * Represents a Google+ user.
 * The class is not really complete,
 * in the sense many fields available
 * on the website are still missing.
 * 
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class Person implements Comparable<Person>
{	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// GOOGLE+ ID	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** unique id of this Google+ user */
	private String id;
	
	/**
	 * Returns the unique id
	 * of this Google+ user.
	 * 
	 * @return
	 * 		This user's id.
	 */
	public String getId()
	{	return id;
	}

	/**
	 * Changes the unique id
	 * of this Google+ user.
	 * 
	 * @param id
	 * 		The new id of this Google+ user.
	 */
	public void setId(String id)
	{	this.id = id;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATE RETRIEVED	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** date this Google+ user data was retrieved */
	private Date dateRetrieved;
	
	/**
	 * Returns the date this Google+ user's
	 * data was retrieved.
	 * 
	 * @return
	 * 		Date this user's data was retrieved.
	 */
	public Date getDateRetrieved()
	{	return dateRetrieved;
	}

	/**
	 * Changes the date this Google+ user's
	 * data was retrieved.
	 * 
	 * @param dateRetrieved
	 * 		The new date for this Google+ user.
	 */
	public void setDateRetrieved(Date dateRetrieved)
	{	this.dateRetrieved = dateRetrieved;
	}

	/////////////////////////////////////////////////////////////////
	// NAMES				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** firstname of this Google+ user */
	private String firstname;
	/** family name of this Google+ user */
	private String lastname;
	
	/**
	 * Returns the firstname
	 * of this Google+ user.
	 * 
	 * @return
	 * 		This user's firstname.
	 */
	public String getFirstname()
	{	return firstname;
	}

	/**
	 * Changes the firstname
	 * of this Google+ user.
	 * 
	 * @param firstname
	 * 		The new firstname of this Google+ user.
	 */
	public void setFirstname(String firstname)
	{	this.firstname = firstname;
	}

	/**
	 * Returns the lastname
	 * of this Google+ user.
	 * 
	 * @return
	 * 		This user's lastname.
	 */
	public String getLastname()
	{	return lastname;
	}

	/**
	 * Changes the lastname
	 * of this Google+ user.
	 * 
	 * @param lastname
	 * 		The new lastname of this Google+ user.
	 */
	public void setLastname(String lastname)
	{	this.lastname = lastname;
	}
	
	/////////////////////////////////////////////////////////////////
	// URLS				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** profile URL of this Google+ user */
	private URL profileUrl;
	/** picture URL of this Google+ user */
	private URL pictureUrl;
	
	/**
	 * Returns the profile URL
	 * of this Google+ user.
	 * 
	 * @return
	 * 		This user's profile URL.
	 */
	public URL getProfileUrl()
	{	return profileUrl;
	}

	/**
	 * Changes the profile URL
	 * of this Google+ user.
	 * 
	 * @param profileUrl
	 * 		The new profile URL of this Google+ user.
	 */
	public void setProfileUrl(URL profileUrl)
	{	this.profileUrl = profileUrl;
	}

	/**
	 * Returns the picture URL
	 * of this Google+ user.
	 * 
	 * @return
	 * 		This user's picture URL.
	 */
	public URL getPictureUrl()
	{	return pictureUrl;
	}

	/**
	 * Changes the picture URL
	 * of this Google+ user.
	 * 
	 * @param pictureUrl
	 * 		The new picture URL of this Google+ user.
	 */
	public void setPictureUrl(URL pictureUrl)
	{	this.pictureUrl = pictureUrl;
	}

	/////////////////////////////////////////////////////////////////
	// DATE RETRIEVED	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** state of this person (not processed yet, currently processed, or completely processed) */
	private PersonState state = PersonState.UNPROCESSED;
	
	/**
	 * Returns the state of this Google+ user.
	 * 
	 * @return
	 * 		State of this Google+ user.
	 */
	public PersonState getState()
	{	return state;
	}

	/**
	 * Changes the state of this Google+ user's.
	 * 
	 * @param state
	 * 		The new state of this Google+ user.
	 */
	public void setState(PersonState state)
	{	this.state = state;
	}

	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public int compareTo(Person o)
	{	int result = id.compareTo(o.getId());
		return result;
	}
	
	@Override
	public boolean equals(Object o)
	{	boolean result = false;
		
		if(o==null && o instanceof Person)
		{	Person p = (Person)o;
			result = compareTo(p)==0;
		}
		
		return result;
	}
	
	@Override
	public int hashCode()
	{	int result = id.hashCode();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// COPY				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Complete this {@code Person}
	 * with the fields of the 
	 * specified {@code Person}.
	 * An existing field is lost
	 * if the corresponding field
	 * in the specified {@code Person} is
	 * not null.
	 * 
	 * @param person
	 * 		The person to be used for updating.
	 * @return
	 * 		The method returns {@code true} if at least one field was modified. 
	 */
	public boolean updateFrom(Person person)
	{	boolean result = false;
		
		if(person!=null)
		{	if(person.firstname!=null && !person.firstname.equals(firstname))
			{	firstname = person.firstname;
				result = true;
			}
			if(person.lastname!=null && !person.lastname.equals(lastname))
			{	lastname = person.lastname;
				result = true;
			}
			if(person.pictureUrl!=null && !person.pictureUrl.equals(pictureUrl))
			{	pictureUrl = person.pictureUrl;
				result = true;
			}
			if(person.profileUrl!=null && !person.profileUrl.equals(profileUrl))
			{	profileUrl = person.profileUrl;
				result = true;
			}
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// DATABASE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Deletes the table meant to contain
	 * the Person data.
	 *  
	 * @throws SQLException
	 * 		Problem while accessign the DB.
	 */
	public static void dropTable() throws SQLException
	{	String query = 	"DROP TABLE PERSON";
		Statement statement = DbTools.connection.createStatement();
		statement.execute(query);
		statement.close();
	}

	/**
	 * Creates the table meant to contain
	 * the Person data.
	 *  
	 * @throws SQLException
	 * 		Problem while accessign the DB.
	 */
	public static void createTable() throws SQLException
	{	String query = 	"CREATE TABLE PERSON (";
		query = query + 	"ID VARCHAR(256) NOT NULL PRIMARY KEY,";
		query = query + 	"DATE_RETRIEVED DATE NOT NULL,";
		query = query + 	"FIRSTNAME VARCHAR(64),";
		query = query + 	"LASTNAME VARCHAR(64),";
		query = query + 	"PROCESSED SMALLINT DEFAULT 0";
		query = query + ")";
		Statement statement = DbTools.connection.createStatement();
		statement.execute(query);
		statement.close();
	}
	
	/**
	 * Displays the whole content of the
	 * DB's PERSON table.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void outputTable() throws SQLException
	{	logger.log(".: PERSONS :.");
    	Statement statement = DbTools.connection.createStatement();
	    ResultSet results = statement.executeQuery("SELECT * FROM PERSON");
	    
//String path = FileTools.OUT_FOLDER + File.separator + "persons.txt";
//File file = new File(path);
//FileOutputStream fo = null;
//try
//{	fo = new FileOutputStream(file);
//}
//catch (FileNotFoundException e)
//{	e.printStackTrace();
//}
//OutputStreamWriter osw = new OutputStreamWriter(fo);
//PrintWriter pw = new PrintWriter(osw);
	    
	    // display field names
	    ResultSetMetaData meta = results.getMetaData();
	    int cols = meta.getColumnCount();
	    logger.log("\t");
	    for (int c=1; c<=cols; c++)
	    	logger.log(meta.getColumnLabel(c)+"\t\t");
	    
	    // displays content
	    logger.log("\n-------------------------------------------------");
	    int r = 1;
//int count = 0;
	    while(results.next())
	    {	int c = 1;
	    	String id = results.getString(c);c++;
	        Date dateRetrieved = results.getDate(c);c++;
	        String firstname = results.getString(c);c++;
	        String lastname = results.getString(c);c++;
	        int state = results.getInt(c);c++;
	    	logger.log(r + ".\t" 
	        		+ id + "\t\t" 
	        		+ dateRetrieved + "\t\t" 
	        		+ firstname + "\t\t" 
	        		+ lastname + " \t\t" 
	        		+ state);
	        r++;
//if(state<2)
//{	pw.println(id);
//	count++;
//}
	    }
	    
//pw.close();	    
	    
	    results.close();
	    statement.close();
//System.out.println("remaining users: "+count);
	}
	
	/**
	 * Builds a {@code Person} object
	 * from the data retrieved from
	 * the DB. This data is specified
	 * through the {@code resultSet} parameter.
	 * 
	 * @param resultSet
	 * 		Data retrieved from the DB.
	 * @return
	 * 		The corresponding {@code Person} object.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static Person build(ResultSet resultSet) throws SQLException
	{	Person result = new Person();
		int c = 1;
    	result.id = resultSet.getString(c);c++;
        result.dateRetrieved = resultSet.getDate(c);c++;
        result.firstname = resultSet.getString(c);c++;
        result.lastname = resultSet.getString(c);c++;
        result.state = PersonState.valueOf(resultSet.getInt(c));c++;
		return result;
	}

	/**
	 * Returns the number of persons
	 * registered in the PERSON table.
	 * 
	 * @return
	 * 		Number of persons in the DB.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static int getTableSize() throws SQLException
	{	String tableName = "PERSON";
		int result = DbTools.getTableSize(tableName);
		return result;
	}
	
	/**
	 * Insert a new person in
	 * the database.
	 * 
	 * @param id
	 * 		The id of the new person. 
	 * 
	 * @throws SQLException
	 * 		If the insertion fails.
	 */
	public static void insertDb(String id) throws SQLException
	{	// current date
		java.util.Date utilDate = new java.util.Date();
		Date sqlDate = new java.sql.Date(utilDate.getTime());
		String date = sqlDate.toString();
	
		// insertion
		boolean done = false;
		do
		{	Statement statement = null;
			try
			{	String query = 	"INSERT INTO PERSON (ID,DATE_RETRIEVED,PROCESSED) VALUES ('"+id+"','"+date+"','"+PersonState.UNPROCESSED.ordinal()+"')";
				statement = DbTools.connection.createStatement();
				statement.execute(query);
				statement.close();
				done = true;
			}
			catch(JdbcSQLException e)
			{	statement.close();
				//e.printStackTrace();
				if(e.getErrorCode()==ErrorCode.DUPLICATE_KEY_1)
				{	logger.log("WARNING: person ["+id+"] already present in the DB");
					done = true;
				}
				else if(e.getErrorCode()==ErrorCode.IO_EXCEPTION_2)
					logger.log("ERROR: IO Exception when accessing person ["+id+"]. Retrying"); //116390196781226829514
				else
					throw e;
			}
		}
		while(!done);
	}
	
	/**
	 * Update the data corresponding to this
	 * Person in the DB, using the fields
	 * of this object.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public void updateDb() throws SQLException
	{	// build query	
		String query =	"UPDATE PERSON ";
		query = query +	"SET PROCESSED="+state.ordinal();
		if(firstname!=null)
		{	String fn = firstname.replace("'","''");
			query = query + ", FIRSTNAME='"+fn+"'";
		
		}
		if(lastname!=null)
		{	String ln = lastname.replace("'","''");
			query = query + ", LASTNAME='"+ln+"'";
		}
		query = query +	" WHERE ID='"+id+"'";
		
		// apply modification
		Statement statement = DbTools.connection.createStatement();
		statement.execute(query);
		statement.close();
	}
	
	/**
	 * Retrieve the {@link Person} with specified
	 * id from the DB.
	 * 
	 * @param id
	 * 		The G+ id of the concerned Person.
	 * @return
	 * 		A {@code Person} object representing the corresponding G+ user.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static Person retrieveFromId(String id) throws SQLException
	{	// build query	
		String query =	"SELECT * FROM PERSON ";
		query = query +	"WHERE ID='"+id+"'";
		
		// get Person
		Statement statement = DbTools.connection.createStatement();
		statement.execute(query);
        ResultSet results = statement.executeQuery(query);
        Person result = null;
        if(results.next())
        	result = Person.build(results);
		statement.close();
    	
    	return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// STRING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = id + " (";
		String temp = null;
		if(lastname!=null)
			temp = lastname;
		if(firstname!=null)
			if(temp==null)
				temp = firstname;
			else
				temp = temp + ", " + firstname;
		result = result + temp + ")"; 
		return result;
	}
}
