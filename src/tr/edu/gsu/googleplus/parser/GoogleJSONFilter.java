package tr.edu.gsu.googleplus.parser;

/*
 * Copyright (c) 2011 Jason Grey (@jt55401)
 * JavaPlus - http://code.google.com/p/javaplus/
 * 
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * 
 * @since 1
 * @version 1
 * @author Jason Grey
 *
 */
public class GoogleJSONFilter extends ClientFilter
{
	/** Google+ URL */
	public static final Logger LOG = Logger.getLogger("com.googleplus");
	/** */
	private static final Pattern pre = Pattern.compile("([\\[{,]),");
	/** */
	private static final Pattern post = Pattern.compile(",([\\]},])");
	
	/**
	 * 
	 * @param s
	 * @return
	 */
// TODO modified	
	private static final String cleanJSON(String s){
		String result = null;
		try
		{	result = s.substring(5);
			result = pre.matcher(result).replaceAll("$1null,");
			result = post.matcher(result).replaceAll(",null$1");
		}
		catch(StringIndexOutOfBoundsException e)
		{	//e.printStackTrace();
			result = "";
		}
		// jason s was also un-escaping \\'s - what for?
		return result;
	}

	@Override
	public ClientResponse handle(ClientRequest req) throws ClientHandlerException {
		
        ClientResponse clientResponse = getNext().handle(req);
        
        String response = clientResponse.getEntity(String.class);
        LOG.logp(Level.FINE, "GoogleJSONFilter", "handle", "Response before cleaning", response);
        
        response = cleanJSON(response);
        
        LOG.logp(Level.FINE, "GoogleJSONFilter", "handle", "Response after cleaning", response);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(response.getBytes());
        clientResponse.setEntityInputStream(bais);
        
        return clientResponse;
	}
}
