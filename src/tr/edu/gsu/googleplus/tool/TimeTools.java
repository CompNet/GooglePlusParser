package tr.edu.gsu.googleplus.tool;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class contains a set of methods linked to time management.
 * They are mainly used by the log classes to add
 * time and date to log messages.
 * 
 * @since 1
 * @version 2
 * @author Vincent Labatut
 */
public class TimeTools
{
	/////////////////////////////////////////////////////////////////
	// HOUR				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** pattern used to format an hour */
	private static final String HOUR_PATTERN = "HH:mm:ss";
	/** format an hour */
	private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat(HOUR_PATTERN);
	
	/**
	 * Returns a string representation of the current hour.
	 *  
	 * @return
	 * 		A string representing the current hour.
	 */
	public static String getCurrentHour()
	{	Calendar cal = Calendar.getInstance();
	    return HOUR_FORMAT.format(cal.getTime());
	}
	
	/////////////////////////////////////////////////////////////////
	// DATE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** pattern used to format a date */
	private static final String DATE_PATTERN = "dd-MM-yyyy";
	/** format a date */
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);
	
	/**
	 * Returns a string representation of the current date.
	 *  
	 * @return
	 * 		A string representing the current date.
	 */
	public static String getCurrentDate()
	{	Calendar cal = Calendar.getInstance();
	    return DATE_FORMAT.format(cal.getTime());
	}

	/////////////////////////////////////////////////////////////////
	// FILESTAMP		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** pattern used to format a date and hour */
	private static final String TIME_PATTERN = "yyyy-MM-dd.HH-mm-ss";
	/** format a date and hour */
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat(TIME_PATTERN);

	/**
	 * Returns a string representation of the current date & hour.
	 *  
	 * @return
	 * 		A string representing the current date & hour.
	 */
	public static String getCurrentTime()
	{	Calendar cal = Calendar.getInstance();
	    return TIME_FORMAT.format(cal.getTime());
	}
	
	/**
	 * Returns a string representation of the specified time
	 * in terms of date & hour.
	 * 
	 * @param time 
	 * 		The time to format. 
	 * @return
	 * 		A string representing the specified time in terms of date & hour.
	 */
	public static String getTime(long time)
	{	Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
	    return TIME_FORMAT.format(cal.getTime());
	}

	/**
	 * Returns a {@code String} representation of
	 * the specified duration. The duration is
	 * expressed in ms whereas the result string
	 * is expressed in days-hours-minutes-seconds.
	 * 
	 * @param duration
	 * 		The duration to be processed (in ms).
	 * @return
	 * 		The corresponding string (in d-h-min-s).
	 */
	public static String formatDuration(long duration)
	{	// processing
		duration = duration / 1000;
		long seconds = duration % 60;
		duration = duration / 60;
		long minutes = duration % 60;
		duration = duration / 60;
		long hours = duration % 24;
		long days = duration / 24;
		
		// generating string
		String result = days + "d " + hours + "h " + minutes + "min " + seconds + "s";
		return result;
	}
}
