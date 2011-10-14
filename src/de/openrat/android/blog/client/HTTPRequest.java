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
package de.openrat.android.blog.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

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
public class HTTPRequest implements Serializable
{

	private static final String CRLF = "\r\n";

	private Multipart multipart = new Multipart();

	// some constants...
	private static final String CHARSET_UTF8 = "UTF-8";
	private static final String HTTP_GET = "GET";
	private static final String HTTP_POST = "POST";

	/**
	 * if <code>true</code>, Tracing-Output will be logged to stdout. Default:
	 * <code>false</code>.
	 */
	// this is public, for easier use.
	public boolean trace = false;

	/**
	 * HTTP-method, must be "GET" or "POST", default: "GET".
	 */
	private String method = HTTP_GET;

	/**
	 * Parameter map.
	 */
	private Map<String, String> parameter = new HashMap<String, String>();
	private Map<String, String> requestHeader = new HashMap<String, String>();

	private String serverPath;
	private String serverHost;
	private int serverPort;

	private String proxyHostname;
	private int proxyPort;
	private SocketAddress socketAddress;

	private String cookieName;
	private String cookieValue;
	private String language;

	/**
     * 
     */
	public HTTPRequest()
	{
		super();
		this.language = Locale.getDefault().getLanguage();
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	/**
	 * Setting a HTTP-Cookie.
	 * 
	 * @param name
	 *            name
	 * @param value
	 *            value
	 */
	public void setCookie(String name, String value)
	{

		this.cookieName = this.urlEncode(name);
		this.cookieValue = this.urlEncode(value);
	}

	/**
	 * URL-Encoder.
	 * 
	 * @param value
	 * @return url-encoded value
	 */
	private String urlEncode(String value)
	{

		try
		{
			return URLEncoder.encode(value, CHARSET_UTF8);
		} catch (UnsupportedEncodingException e)
		{
			// maybe... this would be strange
			throw new IllegalStateException(CHARSET_UTF8
					+ " ist not supported by this VM");
		}
	}

	/**
	 * Setting a HTTP-Proxy.
	 * 
	 * @param host
	 *            hostname
	 * @param port
	 *            port
	 */
	public void setProxy(String host, int port)
	{

		this.proxyHostname = host;
		this.proxyPort = port;
	}

	/**
	 * Set the HTTP Method. Default is "GET".
	 * 
	 * @param method
	 *            HTTP-method
	 */
	public void setMethod(String method)
	{

		if (!HTTP_GET.equalsIgnoreCase(method)
				&& !HTTP_POST.equalsIgnoreCase(method))
			throw new IllegalArgumentException("Method must be '" + HTTP_POST
					+ "' or '" + HTTP_GET + "'.");

		this.method = method.toUpperCase();
	}

	/**
	 * Clear parameter values.
	 */
	public void clearParameters()
	{

		parameter.clear();
		requestHeader.clear();
		multipart.parts.clear();
	}

	/**
	 * Setting a parameter value. <strong>DO NOT url-encode your values</strong>
	 * as this is done automatically inside this method!
	 * 
	 * @param paramName
	 *            name
	 * @param paramValue
	 *            value
	 */
	public void setParameter(String paramName, String paramValue)
	{

		if (paramName == null || paramValue == null || "" == paramName)
			throw new IllegalArgumentException(
					"parameter name and value must have values");

		parameter.put(paramName, paramValue);
	}

	/**
	 * 
	 * Setting a parameter value. <strong>DO NOT url-encode your values</strong>
	 * as this is done automatically inside this method!
	 * 
	 * @param paramName
	 *            name
	 * @param paramValue
	 *            value
	 */
	public void setHeader(String paramName, String paramValue)
	{

		if (paramName == null || paramValue == null || "" == paramName)
			throw new IllegalArgumentException(
					"parameter name and value must have values");

		requestHeader.put(paramName, paramValue);
	}

	/**
	 * Constructs a CMS-Request to the specified server.<br>
	 * Server-Path is "/", Server-Port is 80.
	 * 
	 * @param host
	 *            hostname
	 */
	public HTTPRequest(String host)
	{

		super();
		this.serverHost = host;
		this.serverPath = "/";
		this.serverPort = 80;
	}

	/**
	 * Constructs a CMS-Request to the specified server/path.<br>
	 * Server-Port is 80.
	 * 
	 * @param host
	 *            hostname
	 * @param path
	 *            path
	 */
	public HTTPRequest(String host, String path)
	{

		super();
		this.serverHost = host;
		this.serverPath = path;
		this.serverPort = 80;
	}

	/**
	 * Constructs a CMS-Request to the specified server/path/port.
	 * 
	 * @param host
	 *            hostname
	 * @param path
	 *            path
	 * @param port
	 *            port-number
	 */
	public HTTPRequest(String host, String path, int port)
	{

		super();
		this.serverHost = host;
		this.serverPath = path;
		this.serverPort = port;
	}

	/**
	 * Sends a request to the openrat-server and parses the response into a DOM
	 * tree document.
	 * 
	 * @return server response as a DOM tree
	 * @throws IOException
	 *             if server is unrechable or responds non-wellformed XML
	 */
	public String performRequest() throws IOException
	{
		return performRequest(null);
	}

	/**
	 * Sends a request to the openrat-server and parses the response into a DOM
	 * tree document.
	 * 
	 * @return server response as a DOM tree
	 * @throws IOException
	 *             if server is unrechable or responds non-wellformed XML
	 */
	public String performRequest(String body) throws IOException
	{

		final Socket socket = new Socket();

		try
		{

			final boolean useProxy = this.proxyHostname != null;
			final boolean useCookie = this.cookieName != null;

			if (serverPath == null)
				this.serverPath = "/";
			if (!serverPath.startsWith("/"))
				this.serverPath = "/" + this.serverPath;

			// When a client uses a proxy, it typically sends all requests to
			// that proxy, instead
			// of to the servers in the URLs. Requests to a proxy differ from
			// normal requests in one
			// way: in the first line, they use the complete URL of the resource
			// being requested,
			// instead of just the path.
			if (useProxy)
			{
				socketAddress = new InetSocketAddress(this.proxyHostname,
						this.proxyPort);
			} else
			{
				socketAddress = new InetSocketAddress(this.serverHost,
						serverPort);
			}

			socket.setKeepAlive(false);
			socket.setReuseAddress(false);
			socket.connect(socketAddress, 5000);

			final StringBuffer header = new StringBuffer();

			final StringBuffer parameterList = new StringBuffer();

			for (Entry<String, String> entry : this.parameter.entrySet())
			{
				if (parameterList.length() > 0)
					parameterList.append("&");
				parameterList.append(this.urlEncode(entry.getKey()));
				parameterList.append("=");
				parameterList.append(this.urlEncode(entry.getValue()));
			}

			String httpUrl = this.serverPath;

			if (useProxy)
				// See RFC 2616 Section 5.1.2 "Request-URI"
				// "The absolute URI form is REQUIRED when the request is being made to a proxy"
				httpUrl = "http://" + this.serverHost + httpUrl;

			if (HTTP_GET.equals(this.method)
					|| (body != null || multipart.parts.size() > 0))
				httpUrl = httpUrl + "?" + parameterList;

			// using HTTP/1.0 as this is supported by all HTTP-servers and
			// proxys.
			// We have no need for HTTP/1.1 at the moment.
			header.append(this.method + " " + httpUrl + " HTTP/1.0"+CRLF);

			// Setting the HTTP Header
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Host", this.serverHost);
			headers.put("User-Agent",
					"Mozilla/5.0; compatible (OpenRat android-client)");
			headers.put("Accept", "application/json");
			headers.put("Accept-Language", language);
			headers.put("Accept-Charset", "utf-8");
			headers.put("Connection", "close");
			if (useCookie)
				headers.put("Cookie", cookieName + "=" + cookieValue);

			if (HTTP_POST.equals(this.method))
			{
				if (body == null && multipart.parts.size() == 0)
				{
					headers.put("Content-Type",
							"application/x-www-form-urlencoded");
					headers.put("Content-Length", "" + parameterList.length());
				} else if (multipart.parts.size() > 0)
				{

					headers.put("Content-Type", multipart.getContentType());
					headers.put("Content-Length", ""+multipart.getPayload().getBytes().length);
					
					
				} else
				{
					headers.put("Content-Type", "text/plain");

				}
			}

			headers.putAll(requestHeader);
			for (String headerName : headers.keySet())
			{
				header.append(headerName + ": " + headers.get(headerName)
						+ CRLF);

			}

			header.append(CRLF);

			if (HTTP_POST.equals(this.method))
			{
				if (body == null && multipart.parts.size() == 0)
					header.append(parameterList);
				else if (multipart.parts.size() > 0)
					header.append(multipart.getPayload());
				else
					header.append(body);
			}

			if (this.trace)
				System.out.println("--- request ---");
			if (this.trace)
				System.out.println(header.toString());

			final PrintWriter printWriter = new PrintWriter(socket
					.getOutputStream(), true);
			printWriter.write(header.toString());

			printWriter.flush();

			final InputStream inputStream = socket.getInputStream();
			final int available = inputStream.available();

			final BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

			final String httpResponse = bufferedReader.readLine().trim();
			final String httpRetCode = httpResponse.substring(9, 12);

			if (this.trace)
				System.out.println("--- response ---");
			if (this.trace)
				System.out.println(httpResponse);

			// Check if we got the status 200=OK.
			if (!httpRetCode.equals("200"))
			{

				// non-200-status seems to be an error.
				throw new IOException("No HTTP 200: Status=" + httpRetCode
						+ " (" + httpResponse + ")");
			}

			while (true)
			{

				String responseHeader = bufferedReader.readLine().trim();

				if (responseHeader.equals(""))
					break;

				if (this.trace)
					System.out.println(responseHeader);
			}

			StringBuffer response = new StringBuffer();
			while (bufferedReader.ready())
			{

				response.append(bufferedReader.readLine() + "\n");
			}
			socket.close();

			if (this.trace)
				System.out.println("--- response body ---");
			if (this.trace)
				System.out.println(response + "\n\n\n");

			return response.toString();
		} finally
		{
			try
			{
				socket.close(); // Clean up the socket.
			} catch (IOException e)
			{
				// We have done our very best.
			}
		}

	}

	public void setFile(String name, byte[] value, String filename,
			String type, String encoding)
	{

		Part part = new Part();
		part.file = value;
		part.filename = filename;
		part.encoding = encoding;
		part.name = name;
		part.contentType = type;
		this.multipart.parts.add(part);
	}

	public void setText(String name, String value)
	{

		Part part = new Part();
		part.name = name;
		part.text = value;
		part.contentType = "text/plain";
		this.multipart.parts.add(part);
	}

	private class Multipart implements Serializable
	{

		private static final String  CRLF = "\r\n";
		private static final String BOUNDARY = "614BA262123F3B29656A745C5DD26";
		List<Part> parts = new ArrayList<Part>();

		public String getPayload()
		{
			StringBuffer body = new StringBuffer();

			for (Part part : parts)
			{
				body.append("--" + BOUNDARY + CRLF);
				body.append("Content-Type: " + part.contentType + CRLF);

				if (part.encoding != null)
					body.append("Content-Transfer-Encoding: " + part.encoding
							+ CRLF);

				body.append("Content-Disposition: form-data; name=\""
						+ part.name
						+ "\""
						+ (part.filename != null ? ("; filename=\""
								+ part.filename + "\"") : "") + CRLF);
				body.append(CRLF);
				if (part.file.length > 0)
					body.append(new String(part.file));
				else
					body.append(part.text);
				body.append(CRLF);
			}
			body.append("--" + BOUNDARY + "--");
			return body.toString();
		}

		public String getContentType()
		{
			return "multipart/form-data; boundary=" + Multipart.BOUNDARY;
		}
	}

	private class Part implements Serializable
	{
		public byte[] file;
		public String filename;
		public String text;
		public String name;
		public String contentType;
		public String encoding;
	}
}
