package tr.edu.gsu.googleplus.explorer;

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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.jettison.json.JSONException;

import com.sun.jersey.api.client.UniformInterfaceException;

import tr.edu.gsu.googleplus.data.Person;
import tr.edu.gsu.googleplus.data.PersonState;
import tr.edu.gsu.googleplus.data.Relationship;
import tr.edu.gsu.googleplus.parser.GooglePlusParser;
import tr.edu.gsu.googleplus.tool.DbTools;
import tr.edu.gsu.googleplus.tool.TimeTools;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

/**
 * Explores the neighborhood of the 
 * Google+ users contained in the DB.
 * 
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class RelationshipExtractor
{	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// COUNTER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Counts the number of users processed so far */
	private static int personsProcessed = 0;
	/** Associated lock */
	private static Lock personsProcessedLock = new ReentrantLock();
	/** Counts the number of users yet to be processed */
	private static int personsUnprocessed = 0;
	/** Associated lock */
	private static Lock personsUnprocessedLock = new ReentrantLock();
	/** Time the processed started */
	private static long startTime = 0;
	
	/**
	 * Adds 1 to the current count
	 * of processed users.
	 * The method is synchronized because
	 * several threads might try to access it
	 * at the same time. 
	 */
	private static void incrementPersonsProcessed()
	{	personsProcessedLock.lock();
		personsProcessed++;
		personsProcessedLock.unlock();
	}
	
	/**
	 * Returns the current count
	 * of processed users.
	 * The method is synchronized because
	 * several threads might try to access it
	 * at the same time. 
	 * 
	 * @returns
	 * 		Number of processed users.
	 */
	private static int getPersonsProcessed()
	{	int result;
		personsProcessedLock.lock();
		result = personsProcessed;
		personsProcessedLock.unlock();
		return result;
	}
	
	/**
	 * Initializes the total count
	 * of users remaining to process.
	 * The method is synchronized because
	 * several threads might try to access it
	 * at the same time. 
	 * 
	 * @param value
	 * 		Number of users remaining to process.
	 */
	private static void setPersonsUnprocessed(int value)
	{	personsUnprocessedLock.lock();
		personsUnprocessed = value;
		personsUnprocessedLock.unlock();
	}
	
	/**
	 * Returns the current count
	 * of users remaining to process.
	 * The method is synchronized because
	 * several threads might try to access it
	 * at the same time. 
	 * 
	 * @returns
	 * 		Number of users to process.
	 */
	private static int getPersonsUnprocessed()
	{	int result;
		personsUnprocessedLock.lock();
		result = personsUnprocessed;
		personsUnprocessedLock.unlock();
		return result;
	}
	
	/**
	 * Inits the start time
	 * (for stats purposes)
	 */
	private static void initStartTime()
	{	startTime = System.currentTimeMillis();
	}

	/////////////////////////////////////////////////////////////////
	// PAUSE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Pause used to prevent trigerring G+ captcha mechanism */
	private static long pauseDuration = 125;
	
	/**
	 * Change the current pause duration.
	 * 
	 * @param pauseDuration
	 * 		New value for the pause.
	 */
	public static void setPauseDuration(long pauseDuration)
	{	RelationshipExtractor.pauseDuration = pauseDuration;
	}

	/////////////////////////////////////////////////////////////////
	// FULL NETWORK		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Used to process the Persons */
//	private static Scanner remainingPersons = null;
	private static ResultSet remainingPersons = null;
	/** Associated lock */
	private static Lock remainingPersonsLock = new ReentrantLock();
	
	
	
	/**
	 * Processes a G+ user in order to retrieve all
	 * his relationships (both incoming and outgoing).
	 * 
	 * @param person
	 * 		The {@code Person} to be processed.
	 * @return
	 * 		The set of retrieved {@code Relationship}.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 * @throws MalformedURLException
	 * 		Problem while accessing Google+.
	 * @throws UniformInterfaceException
	 * 		Problem while accessing Google+.
	 * @throws URISyntaxException
	 * 		Problem while accessing Google+.
	 * @throws InterruptedException
	 * 		Problem while accessing Google+.
	 * @throws JSONException
	 * 		Problem while accessing Google+.
	 */
	private static Set<Relationship> processUser(Person person) throws SQLException, MalformedURLException, UniformInterfaceException, URISyntaxException, InterruptedException, JSONException
	{	logger.log("Processing  #"+getPersonsProcessed()+"/"+getPersonsUnprocessed()+": "+person);
		logger.increaseOffset();
	
		// mark as currently processed
		person.setState(PersonState.PROCESSING);
		person.updateDb();
		
		// retrieve and insert relationships
		Set<Relationship> relationships = retrieveNeighborhood(person);
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(2);
		String avrgFlrsStr = nf.format(GooglePlusParser.getExtractFollowersAverageTime());
		String avrgFlesStr = nf.format(GooglePlusParser.getExtractFolloweesAverageTime());
		logger.log("Average time for internet access: flrs="+avrgFlrsStr+"ms fles="+avrgFlesStr+"ms");
		insertRelationships(relationships);
		String avrgDbStr = nf.format(Relationship.getInsertDbAverageTime());
		logger.log("Average time for db insertion: "+avrgDbStr+"ms");
		
		// mark as processed
		person.setState(PersonState.PROCESSED);
		person.updateDb();
		
		long currentTime = System.currentTimeMillis();
		double elapsedTime = currentTime - startTime;
		double msAverage = getPersonsProcessed()/elapsedTime;
		double hAverage = msAverage * 3600000;
		long remainingTime = Math.round(getPersonsUnprocessed()/msAverage);
		logger.decreaseOffset();
		logger.log("Hourly average: "+ nf.format(hAverage) + " estimated remaining time: " + TimeTools.formatDuration(remainingTime));
		return relationships;
	}
	
	/**
	 * Retrieves the relationships for
	 * all the users already present
	 * in the DB.
	 * 
	 * @param threadNbr 
	 * 		Number of threads to be used to perform the task.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 * @throws MalformedURLException
	 * 		Problem while accessing Google+.
	 * @throws UniformInterfaceException
	 * 		Problem while accessing Google+.
	 * @throws URISyntaxException
	 * 		Problem while accessing Google+.
	 * @throws InterruptedException
	 * 		Problem while accessing Google+.
	 * @throws JSONException
	 * 		Problem while accessing Google+.
	 */
	public static void retrieveAllRelationships(int threadNbr) throws SQLException, MalformedURLException, UniformInterfaceException, URISyntaxException, InterruptedException, JSONException
	{	initStartTime();
		logger.log("Starting process at "+TimeTools.getTime(startTime));
	
		// new thread(s)
		for(int t=0;t<threadNbr-1;t++)
		{	Runnable runnable = new Runnable()
			{	@Override
				public void run()
				{	try
					{	threadProcess();
					}
					catch (MalformedURLException e)
					{	e.printStackTrace();
					}
					catch (UniformInterfaceException e)
					{	e.printStackTrace();
					}
					catch (SQLException e)
					{	e.printStackTrace();
					}
					catch (URISyntaxException e)
					{	e.printStackTrace();
					}
					catch (InterruptedException e)
					{	e.printStackTrace();
					}
					catch (JSONException e)
					{	e.printStackTrace();
					}
				}
			};
			Thread thread = new Thread(runnable);
			thread.start();
			Thread.sleep(500);	// to ensure consistent numbering between threads and loggers
		}
	
		// existing thread
		threadProcess();
	}
	
	/**
	 * Implements the processing
	 * of a single thread retrieving
	 * relationship data.
	 *  
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 * @throws MalformedURLException
	 * 		Problem while accessing Google+.
	 * @throws UniformInterfaceException
	 * 		Problem while accessing Google+.
	 * @throws URISyntaxException
	 * 		Problem while accessing Google+.
	 * @throws InterruptedException
	 * 		Problem while accessing Google+.
	 * @throws JSONException
	 * 		Problem while accessing Google+.
	 */
	private static void threadProcess() throws SQLException, MalformedURLException, UniformInterfaceException, URISyntaxException, InterruptedException, JSONException
	{	//personsProcessed = 0;
		Person person = getNextPerson();
		while(person!=null)
		{	processUser(person);
			person = getNextPerson();
			incrementPersonsProcessed();
		}
	}
	
	/**
	 * Fetches the next non-processed Person
	 * of the DB.
	 * 
	 * @return
	 * 		The next Person to be processed.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	private static synchronized Person getNextPerson() throws SQLException
	{	Person result = null;
		PersonState state = PersonState.UNPROCESSED;
		remainingPersonsLock.lock();
		try
		{	if(remainingPersons==null)
			{	setPersonsUnprocessed(0);
				
				// retrieve the total number of persons yet to be processed
				{	Statement statement = DbTools.connection.createStatement();
					String query = "SELECT COUNT(*) FROM PERSON WHERE PROCESSED='"+state.ordinal()+"'";
					ResultSet temp = statement.executeQuery(query);
					if(temp.next())
						setPersonsUnprocessed(temp.getInt(1));
				}		
				// retrieving these persons
				{	//File file = new File("/home/vlabatut/eclipse/workspaces/Extraction/GooglePlusBis/res/list.txt");
					//FileInputStream fis = new FileInputStream(file);
					//InputStreamReader isr = new InputStreamReader(fis);
					//remainingPersons = new Scanner(isr);
					
					Statement statement = DbTools.connection.createStatement();
					String query = "SELECT * FROM PERSON WHERE PROCESSED='"+state.ordinal()+"'";
					remainingPersons = statement.executeQuery(query);
				}
				
				initStartTime();
				logger.log("Database all set at at "+TimeTools.getTime(startTime));
			}
		
			if(!remainingPersons.isClosed() && remainingPersons.next())
			{	//result = new Person();
				//String id = remainingPersons.next();
				//result.setId(id);
				result = Person.build(remainingPersons);
			}
			else
			{	remainingPersons.close();
			}
		}
		finally
		{	remainingPersonsLock.unlock();
		}
		
		// force the thread to sleep a bit, in order not
		// to trigger G+ new captcha security mechanism
		try
		{	Thread.sleep(pauseDuration);
		}
		catch (InterruptedException e)
		{	e.printStackTrace();
		}		
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// EGO-CENTRED		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Gets the ego network centered on the specified
	 * user with specified maximal radius. The processing
	 * is supposed to start from an empty DB, and
	 * was implemented just for testing purposes.
	 * 
	 * @param id
	 * 		Id of the central user.
	 * @param radius
	 * 		Radius of the network.
	 * 
	 * @throws UniformInterfaceException
	 * 		Problem while accessing Google+.
	 * @throws MalformedURLException
	 * 		Problem while accessing Google+.
	 * @throws URISyntaxException
	 * 		Problem while accessing Google+.
	 * @throws InterruptedException
	 * 		Problem while accessing Google+.
	 * @throws JSONException
	 * 		Problem while accessing Google+.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	public static void retrieveEgoNetwork(String id, int radius) throws UniformInterfaceException, MalformedURLException, URISyntaxException, InterruptedException, JSONException, SQLException
	{	logger.log("Retrieving ego network for user "+id+" and radius "+radius);
		Set<String> personsLeft = new TreeSet<String>();
		personsLeft.add(id);
		processUsers(radius,personsLeft);
	}
	
	/**
	 * Explore the listed ids in order to
	 * complete an ego network. The {@code radius}
	 * parameter is used as a limite for
	 * the exploration of the network.
	 * 
	 * @param radius
	 * 		Current radius limit.
	 * @param personsLeft
	 * 		List of ids remaining to be processed.
	 * 
	 * @throws UniformInterfaceException
	 * 		Problem while accessing Google+.
	 * @throws MalformedURLException
	 * 		Problem while accessing Google+.
	 * @throws URISyntaxException
	 * 		Problem while accessing Google+.
	 * @throws InterruptedException
	 * 		Problem while accessing Google+.
	 * @throws JSONException
	 * 		Problem while accessing Google+.
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	private static void processUsers(int radius, Set<String> personsLeft) throws UniformInterfaceException, MalformedURLException, URISyntaxException, InterruptedException, JSONException, SQLException
	{	logger.log("Processing radius "+radius);
		logger.increaseOffset();
		Set<String> neighbors = new TreeSet<String>();
		
		for(String id: personsLeft)
		{	Person person1 = Person.retrieveFromId(id);
			if(person1==null)
			{	person1 = new Person();
				person1.setId(id);
				Person.insertDb(id);
			}
			if(person1.getState()==PersonState.UNPROCESSED)
			{	logger.log("Processing person "+person1.getId());
				logger.increaseOffset();
				
				// personal info
				Person person2 = GooglePlusParser.extractPerson(person1.getId());
		    	person1.updateFrom(person2);
				logger.log("Processing personal info: "+person1);
				
			    // relationship info
				String msg = "Processing relational info: ";
				if(radius>0)
				{	Set<Relationship> relationships = retrieveNeighborhood(person1);
					insertRelationships(relationships);
					Set<String> temp = Relationship.getIdsFromRelationships(relationships);
					temp.remove(id);
					neighbors.addAll(temp);
					msg = msg + temp.size()+" neighbors";
				}
				else
					msg = msg + "radius reached";
				logger.log(msg);
				
			
				// update DB
				logger.log("Update DB");
				logger.decreaseOffset();
				person1.setState(PersonState.PROCESSED);
				person1.updateDb();
			}
		}
		
		logger.decreaseOffset();
		if(!neighbors.isEmpty())
			processUsers(radius-1,neighbors);
	}

	/////////////////////////////////////////////////////////////////
	// COMMON			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Get the users connected to the specified user.
	 * The method returns them under the form of
	 * a set of {@link Relationship} objects.</br>
	 * Normally, retrieving only the incoming xor outgoing
	 * links for all users should be enough to get a 
	 * complete representation of the graph. However,
	 * now users can hide some of their followees and/or
	 * all their followers. Using both followers and followees
	 * allow cross-checking relationships and minimizing the
	 * number of links not retrieved.
	 *  
	 * @param person
	 * 		The user's whose connections are required.
	 * @return 
	 * 		The corresponding set of {@code Relationship} objects.
	 * 
	 * @throws MalformedURLException
	 * 		Problem while using the Google+ parser.
	 * @throws UniformInterfaceException
	 * 		Problem while using the Google+ parser.
	 * @throws URISyntaxException
	 * 		Problem while using the Google+ parser.
	 * @throws InterruptedException
	 * 		Problem while using the Google+ parser.
	 * @throws JSONException
	 * 		Problem while using the Google+ parser.
	 */
	private static Set<Relationship> retrieveNeighborhood(Person person) throws MalformedURLException, UniformInterfaceException, URISyntaxException, InterruptedException, JSONException
	{	String id = person.getId();
		Set<Relationship> result = GooglePlusParser.extractFollowers(id);
		result.addAll(GooglePlusParser.extractFollowees(id));
		return result;
	}

	/**
	 * Inserts all the specified relationships
	 * in the DB.
	 * 
	 * @param relationships
	 * 		Relationships to be inserted.
	 * 
	 * @throws SQLException
	 * 		Problem while accessing the DB.
	 */
	private static void insertRelationships(Set<Relationship> relationships) throws SQLException
	{	for(Relationship relationship: relationships)
			relationship.insertDb();
	}
}
