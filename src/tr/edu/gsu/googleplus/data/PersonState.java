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
public enum PersonState
{	
	/////////////////////////////////////////////////////////////////
	// VALUES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** The Person has not been processed at all */
	UNPROCESSED,
	/** The processing has started, but is not finished yet */
	PROCESSING,
	/** The processing is over */
	PROCESSED;

	/////////////////////////////////////////////////////////////////
	// PROCESS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the {@code PersonState} value
	 * corresponding to the specified ordinal value.
	 * In other words, it's the inverse function
	 * of PersonState.ordinal().
	 * 
	 * @param ord
	 * 		Rank of the required value.
	 * @return
	 * 		The value with the specified rank.
	 */
	public static PersonState valueOf(int ord)
	{	PersonState result = null;
		switch(ord)
		{	case 0: 
				result = UNPROCESSED;
				break;
			case 1: 
				result = PROCESSING;
				break;
			case 2: 
				result = PROCESSED;
				break;
		}
		return result;
	}
}
