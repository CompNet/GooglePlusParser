package tr.edu.gsu.googleplus.parser;

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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import tr.edu.gsu.googleplus.data.Person;
import tr.edu.gsu.googleplus.data.Relationship;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLogger;
import tr.edu.gsu.googleplus.tool.log.HierarchicalLoggerManager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

/**
 * Service allowing to retrieve data
 * from Google+ through JSON.
 * We focus only on individual data
 * (describing some person information)
 * and relational data (describing
 * the followees/follower links between
 * persons).<br/>
 * Note this class contains portions of source
 * code written by Jason Grey for his
 * <a href="https://plus.google.com/114074126238467553553/posts">JavaPlus</a> API.
 * 
 * @since 1
 * @version 1
 * @author Jason Grey
 * @author Vincent Labatut
 */
public class GooglePlusParser
{
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();

	/////////////////////////////////////////////////////////////////
	// CLIENT		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Rest client used to retrieve the G+ data */
	private static final Client restClient = new Client();
	static
	{	restClient.addFilter(new GoogleJSONFilter());
	}
	
	/////////////////////////////////////////////////////////////////
	// INDIVIDUAL	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Template URL to retrieve individual data describing some user of interest  */
	private static final String individualUrlTemplate = "https://plus.google.com/_/profiles/get/%1$s";
	/** for statistical purposes : average access time */
	public static double extractPersonAverageTime = 0;
	/** for statistical purposes : number of calls to this function */
	public static int extractPersonCallCount = 0;
	/** associated lock */
	private static Lock extractPersonLock = new ReentrantLock();

	/**
	 * Returns the average time to get 
	 * the data describing  one user.
	 * 
	 * @return
	 * 		The average time in ms.
	 */
	public static double getExtractPersonAverageTime()
	{	double result;
		extractPersonLock.lock();
		result = extractPersonAverageTime;
		extractPersonLock.unlock();
		return result;
	}
	
	/**
	 * Retrieves the information describing a person
	 * from its Google+ page and returns the corresponding
	 * {@link Person} object. If the id does not correspond
	 * to a valid G+ account, then null is returned.
	 * 
	 * @param id
	 * 		G+ id of the person of interest.
	 * @return
	 * 		A {@code Person} object representing the retrieved data,
	 * 		or {@code null} if the id is invalid.
	 * 
	 * @throws UniformInterfaceException
	 * 		Problem while retrieving the data.
	 * @throws MalformedURLException
	 * 		Problem while retrieving the data.
	 * @throws URISyntaxException
	 * 		Problem while retrieving the data.
	 * @throws InterruptedException
	 * 		Problem while retrieving the data.
	 * @throws JSONException
	 * 		Problem while retrieving the data.
	 */
	public static Person extractPerson(final String id) throws UniformInterfaceException, MalformedURLException, URISyntaxException, InterruptedException, JSONException
	{	long before = System.currentTimeMillis();
		URL url = buildURL(individualUrlTemplate,id);
		JSONArray json = getJSON(url);
		Person result = null;
		if(json!=null && getFromArray(json,0,1)!=null)	// for some reason, some of the ids do not exist, which results in a null json object
			result = parsePerson(json);
		else
			logger.log("WARNING: GID "+id+" does not correspond to a valid G+ account");
		long after = System.currentTimeMillis();
		
		long elapsed = after-before;
		extractPersonLock.lock();
		extractPersonAverageTime = extractPersonAverageTime*(extractPersonCallCount/(extractPersonCallCount+1)) 
			+ elapsed/(extractPersonCallCount+1);
		extractPersonCallCount++;
		extractPersonLock.unlock();
		
		return result;
	}

	/**
	 * Parse a JSON array to extract
	 * meaningful data describing
	 * a Google+ user.</br>
	 * TODO: to be completed with other fields.
	 * 
	 * @param json
	 * 		Textual representation of a JSON array.
	 * @return
	 * 		A {@code Person} object representing the extracted parsed data.
	 * 
	 * @throws JSONException
	 * 		Problem with the received JSON array.
	 * @throws MalformedURLException
	 * 		Problem with an URL contained in the profile.
	 */
	private static Person parsePerson(JSONArray json) throws JSONException, MalformedURLException
	{	Person result = new Person();
		Object temp;
		
		// id
//		String id = (String)getFromArray(json,1,0);	// NOTE v1
		String id = (String)getFromArray(json,0,1,0);
		result.setId(id);
		// date retrieved
		result.setDateRetrieved(new Date());
		
		// names
//		temp = getFromArray(json,1,2,4,1);			// NOTE v1
		temp = getFromArray(json,0,1,2,4,1);
		if(temp!=null)
		{	String firstname = (String) temp; 
			result.setFirstname(firstname);
		}
//		temp = getFromArray(json,1,2,4,2);			// NOTE v1
		temp = getFromArray(json,0,1,2,4,2);
		if(temp!=null)
		{	String lastname = (String) temp;
			result.setLastname(lastname);
		}

		// urls
//		temp = getFromArray(json,1,2,3);			// NOTE v1
		temp = getFromArray(json,0,1,2,3);
		if(temp!=null)
		{	String urlStr = (String) temp;
			URL url = new URL("http:"+urlStr);
			result.setPictureUrl(url);
		}
//		temp = getFromArray(json,1,2,2);			// NOTE v1
		temp = getFromArray(json,0,1,2,2);
		if(temp!=null)
		{	String urlStr = (String) temp;
			URL url = new URL(urlStr);
			result.setProfileUrl(url);
		}
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// RELATIONSHIPS		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Template URL to retrieve data describing the users followed by some user of interest */
	private static final String followeeUrlTemplate =	"https://plus.google.com/_/socialgraph/lookup/visible/?o=%%5Bnull%%2Cnull%%2C%%22%1$s%%22%%5D";
	/** Template URL to retrieve data describing the users following some user of interest */
	private static final String followerUrlTemplate =	"https://plus.google.com/_/socialgraph/lookup/incoming/?o=%%5Bnull%%2Cnull%%2C%%22%1$s%%22%%5D&n=1000000";
	/** Enum class used internally to switch between follower and followee */
	private enum Mode {FOLLOWER, FOLLOWEE}
	/** for statistical purposes : average access time */
	public static double extractFollowersAverageTime = 0;
	/** for statistical purposes : number of calls to this function */
	private static float extractFollowersCallCount = 0;
	/** associated lock */
	private static Lock extractFollowersLock = new ReentrantLock();
	/** for statistical purposes : average access time */
	public static double extractFolloweesAverageTime = 0;
	/** for statistical purposes : number of calls to this function */
	private static float extractFolloweesCallCount = 0;
	/** associated lock */
	private static Lock extractFolloweesLock = new ReentrantLock();
	
	/**
	 * Returns the average time to get 
	 * the lists of followers for one user.
	 * 
	 * @return
	 * 		The average time in ms.
	 */
	public static double getExtractFollowersAverageTime()
	{	double result;
		extractFollowersLock.lock();
		result = extractFollowersAverageTime;
		extractFollowersLock.unlock();
		return result;
	}
	
	/**
	 * Retrieves the set of incoming relationships
	 * for the specified target.
	 * 
	 * @param target
	 * 		Google+ id of the considered user.
	 * @return
	 * 		The set of people following the considered user.
	 * 
	 * @throws MalformedURLException
	 * 		Problem while retrieving the data.
	 * @throws UniformInterfaceException
	 * 		Problem while retrieving the data.
	 * @throws URISyntaxException
	 * 		Problem while retrieving the data.
	 * @throws InterruptedException
	 * 		Problem while retrieving the data.
	 * @throws JSONException
	 * 		Problem while retrieving the data.
	 */
	public static Set<Relationship> extractFollowers(String target) throws MalformedURLException, UniformInterfaceException, URISyntaxException, InterruptedException, JSONException
	{	long before = System.currentTimeMillis();
		URL url = buildURL(followerUrlTemplate,target);
		JSONArray json = getJSON(url);
		//Thread.sleep(10000);
		
		Set<Relationship> result = parseRelationships(json,target,Mode.FOLLOWER);
		long after = System.currentTimeMillis();
		
		double elapsed = after-before;
		extractFollowersLock.lock(); //2587.25*4
		extractFollowersAverageTime = extractFollowersAverageTime*(extractFollowersCallCount/(extractFollowersCallCount+1)) 
			+ elapsed/(extractFollowersCallCount+1);
		extractFollowersCallCount++;
		extractFollowersLock.unlock();
		
		return result;
	}

	/**
	 * Returns the average time to get 
	 * the lists of followees for one user.
	 * 
	 * @return
	 * 		The average time in ms.
	 */
	public static double getExtractFolloweesAverageTime()
	{	double result;
		extractFolloweesLock.lock();
		result = extractFolloweesAverageTime;
		extractFolloweesLock.unlock();
		return result;
	}
	
	/**
	 * Retrieves the set of outgoing relationships
	 * for the specified target.
	 * 
	 * @param source
	 * 		Google+ id of the considered user.
	 * @return
	 * 		The set of people the considered user follows.
	 * 
	 * @throws MalformedURLException
	 * 		Problem while retrieving the data.
	 * @throws UniformInterfaceException
	 * 		Problem while retrieving the data.
	 * @throws URISyntaxException
	 * 		Problem while retrieving the data.
	 * @throws InterruptedException
	 * 		Problem while retrieving the data.
	 * @throws JSONException
	 * 		Problem while retrieving the data.
	 */
	public static Set<Relationship> extractFollowees(String source) throws MalformedURLException, UniformInterfaceException, URISyntaxException, InterruptedException, JSONException
	{	long before = System.currentTimeMillis();
		URL url = buildURL(followeeUrlTemplate,source);
		JSONArray json = getJSON(url);
		
		Set<Relationship> result = parseRelationships(json,source,Mode.FOLLOWEE);
		long after = System.currentTimeMillis();
		
		double elapsed = after-before;
		extractFolloweesLock.lock();
		extractFolloweesAverageTime = extractFolloweesAverageTime*(extractFolloweesCallCount/(extractFolloweesCallCount+1)) 
			+ elapsed/(extractFolloweesCallCount+1);
		extractFolloweesCallCount++;
		extractFolloweesLock.unlock();
		
		return result;
	}

	/**
	 * Parses a JSON array in order
	 * to retrieve the list of relationships
	 * it contains. The method returns a
	 * set of {@code Relationship} objects
	 * whose initialization depends on the 
	 * mode parameter.  
	 * 
	 * @param json
	 * 		The JSON array object to be parsed.
	 * @param id
	 * 		The id of the considered Google+ user.
	 * @param mode
	 * 		The type of relationship (follower/followee)
	 * @return
	 * 		A set containing all the retrieved relationships.
	 * 
	 * @throws JSONException
	 * 		Problem while parsing the JSON object.
	 */
	private static Set<Relationship> parseRelationships(JSONArray json, String id, Mode mode) throws JSONException
	{	Set<Relationship> result = new TreeSet<Relationship>();
//		JSONArray personList = (JSONArray)getFromArray(json,0,2);	// NOTE v0
//		JSONArray personList = (JSONArray)getFromArray(json,2);		// NOTE v1
		JSONArray personList = (JSONArray)getFromArray(json,0,2);
		
		int size = 0;
		if(mode==Mode.FOLLOWER)
		{	if(json.length()==5) 
				size = json.getInt(4);
			// else: no followers
		}
		int count;
		
		for(count=0;count<personList.length();count++)
		{	JSONArray array = personList.getJSONArray(count);
			Relationship relationship = parseRelationship(array,id,mode);
			if(relationship!=null)	// in case it is a self-relation (we ignore it)
				result.add(relationship);
		}
		
		String text = "retrieved: " + count;
		if(mode==Mode.FOLLOWER)
			text = text + "/" + size;
		logger.log(text);
		
		return result;
	}

	/**
	 * Parses a JSON array in order
	 * to retrieve the relationship
	 * it contains. The method returns a
	 * {@code Relationship} object
	 * whose initialization depends on the 
	 * mode parameter.
	 * If the relationship involves twice the
	 * same node, then {@code null} is returned.
	 * 
	 * @param json
	 * 		The JSON array object to be parsed.
	 * @param id1
	 * 		The id of the considered Google+ user.
	 * @param mode
	 * 		The type of relationship (follower/followee)
	 * @return
	 * 		The retrieved relationship, or {@code null} if
	 * 		it is a self-relationship. 
	 * 
	 * @throws JSONException
	 * 		Problem while parsing the JSON object.
	 */
	private static Relationship parseRelationship(JSONArray json, String id1, Mode mode) throws JSONException
	{	Relationship result = null;
		String id2 = (String)getFromArray(json,0,2);
		
		if(id1.equals(id2))
			logger.log("WARNING: self-relationship between"+id1+" (and itself) detected");
		else
		{	result = new Relationship();
			// date retrieved
			result.setDateRetrieved(new Date());
	
			// source
			if(mode==Mode.FOLLOWEE)
				result.setSourceId(id1);
			else
				result.setSourceId(id2);
			
			// target
			if(mode==Mode.FOLLOWER)
				result.setTargetId(id1);
			else
				result.setTargetId(id2);
			
			// strength
			Object temp = getFromArray(json,2,3);
			if(temp!=null)
			{	String strengthStr = (String) temp;
				Float strength = new Float(strengthStr);
				result.setStrength(strength);
			}
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// COMMON		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Builds the appropriate URL using
	 * the specified template and Google+
	 * user id.
	 * 
	 * @param template
	 * 		URL template.
	 * @param id
	 * 		Id of the concerned Google+ user.
	 * @return
	 * 		The appropriate URL.
	 * 
	 * @throws MalformedURLException
	 * 		Problem while building the URL.
	 */
	private static URL buildURL(final String template, final String id) throws MalformedURLException
	{	String urlStr = String.format(template,id);
		URL result = new URL(urlStr);
		return result;
	}

	/**
	 * Send the request to the specified URL,
	 * get the response and converts it into
	 * a JSON array object.
	 * 
	 * @param url
	 * 		The targeted url.
	 * @return
	 * 		A {@code JSONArray} object representing the response.
	 * 
	 * @throws UniformInterfaceException
	 * 		Problem while retrieving the data.
	 * @throws URISyntaxException
	 * 		Problem while retrieving the data.
	 * @throws InterruptedException
	 * 		Problem while sleeping.
	 */
	private static JSONArray getJSON(final URL url) throws UniformInterfaceException, URISyntaxException, InterruptedException
	{	URI uri = url.toURI();
		WebResource webRes = restClient.resource(uri);
		JSONArray result = null;
		boolean retry;
		int retried = 0;
// TODO inserer ici un chrono	
// TODO modified		
		do
		{	retry = false;
			retried++;
			try
			{	result = webRes.get(JSONArray.class);
			}
			catch(UniformInterfaceException e)
			{	retry = true;		
			}
			catch(ClientHandlerException e)
			{	retry = true;		
			}
			finally
			{	Thread.sleep(50); // per Jason S, probably a good idea to sleep a while...
			}
		}
		while(retry && retried<50);
		return result;
	}

	/**
	 * Returns the object located at the
	 * specified position in the specified
	 * JSON array.
	 * 
	 * @param json
	 * 		The JSON array containing the requested object.
	 * @param i
	 * 		The location of the requested object.
	 * @return
	 * 		The requested object(?)
	 * 
	 * @throws JSONException
	 * 		Object not found in the specified JSON object.
	 */
	private static Object getFromArray(JSONArray json, int... i) throws JSONException
	{	Object result = null;
		int idx = i[0];
		Object obj = json.get(idx);
		
		if(obj instanceof String)
			result = obj;
		
		else if(obj instanceof Long)
			result = obj;
		
		else if(obj instanceof JSONArray)
		{	if(i.length==1)
				result = obj;
			else
			{	int[] nextLevel = Arrays.copyOfRange(i,1,i.length);
				result = getFromArray((JSONArray)obj,nextLevel);
			}
		}
		
		else if(i.length==1 && obj==JSONObject.NULL)
			result = null;
		
		else
			throw new IllegalStateException("Didn't know how to handle object of type: " + obj.getClass().getName());
		
		return result;
	}
}
