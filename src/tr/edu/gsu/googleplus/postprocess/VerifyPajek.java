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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Scanner;


public class VerifyPajek
{	public static void main(String[] args) throws FileNotFoundException
	{	// 30 871 965
		
		// open the input file
		FileInputStream file = new FileInputStream(FILE);
		InputStreamReader reader = new InputStreamReader(file);
		Scanner scanner = new Scanner(reader);
		
		// skip header
		String line = null;
		do
		{	line = scanner.nextLine();
			System.out.println(line);
		}
		while(scanner.hasNextLine() && line.startsWith("*"));
		
		// init
		int count = 0;
		long max = Long.MIN_VALUE;
		long min = Long.MAX_VALUE;
		
		// check each line
		while(scanner.hasNextLine())
		{	count++;
			System.out.println(count + ". min=" + min + " max=" + max);
			line = scanner.nextLine();
			if(!line.isEmpty())
			{	String parts[] = line.split(" ");
				long id1 = Long.parseLong(parts[0]);
				long id2 = Long.parseLong(parts[1]);
				if(id1 > id2)
				{	max = Math.max(max,id1);
					min = Math.min(min,id2);
				}
				else
				{	max = Math.max(max,id2);
					min = Math.min(min,id1);
				}
			}
		}	
		
		// close the file
		scanner.close();
	}
	
	/////////////////////////////////////////////////////////////////
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	private static final String FOLDER = ".." + File.separator + "Database" + File.separator + "googleplus";
	private static final String FILE = FOLDER + File.separator + "giantcomp.net";
}