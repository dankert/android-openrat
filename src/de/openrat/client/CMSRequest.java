/*
OpenRat Java-Client
Copyright (C) 2009 Jan Dankert
 
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
Boston, MA  02110-1301, USA.

 */
package de.openrat.client;

import java.io.Serializable;

/**
 * API-Request to the OpenRat Content Management System. <br>
 * <br>
 * The call to the CMS server is done via a (non-SSL) HTTP connection.<br>
 * <br>
 * Before a call you are able to set some key/value-pairs as parameters. After
 * calling the CMS a DOM-document is returned, which contains the server
 * response.<br>
 * Example <br>
 * 
 * <pre>
 * CMSRequest request = new CMSRequest(&quot;your.openrat.example.com&quot;);
 * //prints tracing information to stdout.
 * request.trace = true;
 * try
 * {
 * 	request.parameter.put(&quot;action&quot;, &quot;index&quot;);
 * 	request.parameter.put(&quot;subaction&quot;, &quot;showlogin&quot;); // login page
 * 	request.parameter.put(&quot;...&quot;, &quot;...&quot;);
 * 	Document response = request.call();
 * 	// now traverse through the dom tree and get your information.
 * } catch (IOException e)
 * {
 * 	// your error handling.
 * }
 * </pre>
 * 
 * @author Jan Dankert
 */
public class CMSRequest extends HTTPRequest implements Serializable
{

	/**
	 * 
	 */
	public CMSRequest()
	{
		super();
	}

	/**
	 * @param host
	 * @param path
	 * @param port
	 */
	public CMSRequest(String host, String path, int port)
	{
		super(host, path, port);
	}

	/**
	 * @param host
	 * @param path
	 */
	public CMSRequest(String host, String path)
	{
		super(host, path);
	}

	/**
	 * @param host
	 */
	public CMSRequest(String host)
	{
		super(host);
	}

	/**
	 * Setzt die Action.
	 * 
	 * @param actionName
	 */
	public void setAction(String actionName)
	{
		super.setParameter("action", actionName);
	}

	/**
	 * Setzt die Action-Methode.
	 * 
	 * @param methodName
	 */
	public void setActionMethod(String methodName)
	{
		super.setParameter("subaction", methodName);
	}
	
	/**
	 * Setzt die Action-Methode.
	 * 
	 * @param id
	 */
	public void setId(String id)
	{
		super.setParameter("id", id);
	}

}
