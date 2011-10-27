package de.openrat.client;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import de.openrat.android.blog.util.FileUtils;
import de.openrat.android.client.FolderEntry;
import de.openrat.android.client.FolderEntry.FType;

/**
 * Komfortabler Zugriff auf das OpenRat-CMS.
 * 
 * @author Jan Dankert
 * 
 */
public class OpenRatClient extends CMSRequest
{

	/**
	 * 
	 */
	public OpenRatClient()
	{
		super();
	}

	/**
	 * @param host
	 * @param path
	 * @param port
	 */
	public OpenRatClient(String host, String path, int port)
	{
		super(host, path, port);
	}

	/**
	 * @param host
	 * @param path
	 */
	public OpenRatClient(String host, String path)
	{
		super(host, path);
	}

	/**
	 * @param host
	 */
	public OpenRatClient(String host)
	{
		super(host);
	}

	/**
	 * Ermittelt den Inhalt eines Ordners.
	 * 
	 * @param folderid
	 *            Id des zu ladenen Ordners. Falls <code>null</code>, wird der
	 *            Rootfolder geladen.
	 * @return Ordner-Einträge
	 */
	public String getRootFolder() throws IOException
	{
		clearParameters();
		setAction("tree");
		setActionMethod("load");

		JSONObject json = readJSON();

		try
		{
			String folderurl = json.getJSONArray("zeilen").getJSONObject(1)
					.getString("url");
			String[] urlParts = folderurl.split("[^0-9]+");
			String folderid = urlParts[urlParts.length - 1];
			return folderid;
		}
		catch (JSONException e)
		{
			throw new OpenRatClientException(
					"JSON-Error while resolving root folder", e);
		}
	}

	/**
	 * Liest den Inhalt für ein Seitenelement.
	 * 
	 * @return
	 */
	public Map<String, String> getValue(String pageid, String elementid)
			throws IOException
	{
		clearParameters();
		setAction("pageelement");
		setActionMethod("edit");
		setId(pageid);
		setParameter("elementid", elementid);

		JSONObject json = readJSON();

		try
		{
			final Map<String, String> properties = new HashMap<String, String>();

			properties.put("type", json.getString("type"));
			properties.put("text", json.optString("text"));
			return properties;
		}
		catch (JSONException e)
		{
			throw new OpenRatClientException(
					"JSON-Error while resolving root folder", e);
		}
	}

	/**
	 * Setzt einen neuen Inhalt für ein Seitenelement.
	 * 
	 * @return
	 */
	public void setValue(String pageid, String elementid, String type,
			String value, boolean release, boolean publish) throws IOException
	{
		clearParameters();
		setAction("pageelement");
		setActionMethod("save");
		setId(pageid);
		setParameter("elementid", elementid);
		setParameter("text", value);
		setParameter("release", release ? "1" : "0");
		setParameter("publish", publish ? "1" : "0");
		setMethod("POST");

		readJSON();
	}

	/**
	 * Ermittelt den Inhalt eines Ordners.
	 * 
	 * @param folderid
	 *            Id des zu ladenen Ordners. Falls <code>null</code>, wird der
	 *            Rootfolder geladen.
	 * @return Ordner-Einträge
	 */
	public List<FolderEntry> getFolderEntries(String folderid)
			throws IOException
	{
		final List<FolderEntry> data = new ArrayList<FolderEntry>();

		super.clearParameters();
		super.setParameter("id", folderid);

		super.setParameter("action", "folder");
		super.setParameter("subaction", "show");

		try
		{
			JSONObject json = readJSON();
			if (!(json.get("object") instanceof JSONObject))
				return data; // Ordner ist leer.

			JSONObject inhalte = json.getJSONObject("object");
			JSONArray names = inhalte.names();

			for (int i = 0; i < names.length(); i++)
			{
				JSONObject obj = inhalte.getJSONObject(names.getString(i));

				final FolderEntry entry = new FolderEntry();
				entry.type = FType.valueOf(obj.getString("type").toUpperCase());
				entry.name = obj.getString("name");
				entry.description = obj.getString("desc");
				entry.id = names.getString(i);
				data.add(entry);

			}

		}
		catch (JSONException e)
		{
			throw new OpenRatClientException("Coult not determine root folder",
					e);
		}

		return data;
	}

	protected JSONObject readJSON() throws OpenRatClientException
	{
		byte[] response;
		try
		{
			response = super.performRequest();
			Log.d("client", "Server-Response:\n"
					+ new String(response, "UTF-8"));
		}
		catch (SocketTimeoutException e)
		{
			throw new OpenRatClientException("Timeout exceeded", e);
		}
		catch (IOException e)
		{
			throw new OpenRatClientException(
					"I/O-Error while performing the request", e);
		}

		try
		{
			JSONObject json;
			try
			{
				json = new JSONObject(new String(response, "UTF-8"));
			}
			catch (UnsupportedEncodingException e1)
			{
				throw new OpenRatClientException("UTF-8 not supported?!", e1);
			}

			try
			{
				// Versuchen, die 1. Notice zu lesen. Falls es eine gibt,
				// Exception mit dem Notice-Text werfen.
				final String status = json.getString("notice_status");

				if (!status.equalsIgnoreCase("ok"))
				{
					String msgText;
					try
					{
						msgText = json.getJSONArray("notices").getJSONObject(0)
								.getString("text");
					}
					catch (JSONException e)
					{
						msgText = "Not OK (Server response does not include \"OK\" and does not include a notice text";
					}
					throw new OpenRatClientException(msgText);
				}
				else
					return json; // Alles OK.
			}
			catch (JSONException e)
			{
				// throw new OpenRatClientException(
				// "Server error: Response does not include a attribute 'notice_status'.\n"+response);
				return json; // Alles OK, kann passieren, wenn es keine Notices
				// gibt.
			}

		}
		catch (JSONException e)
		{
			throw new OpenRatClientException(
					"JSON Parsing Error. Original response was:\n"
							+ new String(response) + "\n\n", e);
		}
	}

	/**
	 * @author dankert
	 * 
	 */
	public class OpenRatClientException extends IOException
	{
		/**
		 * 
		 */
		public OpenRatClientException()
		{
			super();
		}

		/**
		 * @param throwable
		 */
		public OpenRatClientException(Throwable throwable)
		{
			super(throwable.getMessage());
		}

		private Throwable cause;

		/**
		 * @param detailMessage
		 */
		public OpenRatClientException(String detailMessage)
		{
			super(detailMessage);
		}

		/**
		 * @param detailMessage
		 */
		public OpenRatClientException(String detailMessage, Throwable cause)
		{
			super(detailMessage);
			this.cause = cause;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see java.lang.Throwable#getCause()
		 */
		@Override
		public Throwable getCause()
		{
			return this.cause;
		}

	}

	/**
	 * @throws IOException
	 * 
	 */
	public void uploadFile(String filenName, File file) throws IOException
	{

		super.clearParameters();
		super.setAction("folder");
		super.setActionMethod("createnewfile");
		super.setMethod("POST");

		byte[] fileBytes;
		try
		{
			fileBytes = FileUtils.getBytesFromFile(file);
		}
		catch (IOException e)
		{
			throw new OpenRatClientException(e);
		}
		super.setFile(filenName, fileBytes, file.getName(), "image/jpeg",
				"binary");

		@SuppressWarnings("unused")
		final JSONObject response = readJSON();
	}

	/**
	 * @param type
	 *            Typ
	 * @param id
	 *            Id
	 * @throws IOException
	 */
	public void publish(String type, String id) throws IOException
	{

		super.clearParameters();
		super.setAction(type);
		super.setActionMethod("pub");
		super.setId(id);

		// Erstmal alles aktivieren was geht
		// TODO: Abfrage der gewünschten Einstellungen über AlertDialog.
		super.setParameter("subdirs", "1");
		super.setParameter("pages", "1");
		super.setParameter("files", "1");

		@SuppressWarnings("unused")
		JSONObject response = readJSON();
	}

	public void login(String login, String password, String database)
			throws IOException
	{
		super.setParameter("action", "index");
		super.setParameter("subaction", "login");
		if (database.length() > 0)
			super.setParameter("dbid", database);
		super.setParameter("login_name", login);
		super.setParameter("login_password", password);

		JSONObject json = readJSON();
		JSONObject session;
		try
		{
			session = json.getJSONObject("session");
			final String sessionName = session.getString("name");
			final String sessionId = session.getString("id");
			setCookie(sessionName, sessionId);
		}
		catch (JSONException e)
		{
			try
			{
				final String msgText = json.getJSONArray("notices")
						.getJSONObject(0).getString("text");
				throw new OpenRatClientException(msgText, e);
			}
			catch (JSONException e1)
			{
				throw new OpenRatClientException(e);
			}
		}
	}

	public List<FolderEntry> loadProjects() throws IOException
	{

		List<FolderEntry> data = new ArrayList<FolderEntry>();

		super.clearParameters();
		super.setParameter("action", "index");
		super.setParameter("subaction", "projectmenu");
		JSONObject json = readJSON();

		try
		{
			JSONArray projects = json.getJSONArray("projects");

			for (int i = 0; i < projects.length(); i++)
			{
				JSONObject project = projects.getJSONObject(i);

				final FolderEntry entry = new FolderEntry();
				entry.type = FType.PROJECT;
				entry.name = project.getString("name");
				entry.description = "";
				entry.id = project.getString("id");

				data.add(entry);

			}
		}
		catch (JSONException e)
		{
			Log.e(this.getClass().getName(), e.getMessage(), e);
		}

		return data;
	}

	/**
	 * Wählt ein Projekt.
	 * 
	 * @param projectid
	 *            Projekt-ID.
	 * @throws IOException
	 */
	public void selectProject(String projectid) throws IOException
	{

		super.clearParameters();
		super.setAction("index");
		super.setActionMethod("project");
		super.setParameter("id", projectid);
		// super.setMethod("POST");

		readJSON();
	}

	public Map<String, String> getPageElements(String id) throws IOException
	{

		Map<String, String> el = new HashMap<String, String>();
		super.clearParameters();
		super.setAction("page");
		super.setActionMethod("el");
		super.setId(id);

		JSONObject json = readJSON();

		final Map<String, String> elementMap = new LinkedHashMap<String, String>();

		try
		{
			JSONObject elements = json.getJSONObject("el");

			for (Iterator ti = elements.keys(); ti.hasNext();)
			{
				String elementId = (String) ti.next();
				String pageelementName = elements.getJSONObject(elementId)
						.getString("name");
				elementMap.put(elementId, pageelementName);
			}
		}
		catch (JSONException e)
		{
			Log.w(this.getClass().getSimpleName(), "\n\n" + json);
			throw new OpenRatClientException("a property was not found", e);
		}

		return elementMap;

	}

	public Map<String, String> getProperties(String type, String id)
			throws IOException
	{
		super.clearParameters();
		super.setAction(type);
		super.setActionMethod("prop");
		super.setId(id);

		JSONObject json = readJSON();

		final Map<String, String> properties = new HashMap<String, String>();

		try
		{
			properties.put("name", json.getString("name"));
			properties.put("filename", json.getString("filename"));
			properties.put("description", json.getString("description"));
		}
		catch (JSONException e)
		{
			throw new OpenRatClientException("a property was not found", e);
		}

		return properties;

	}

	public void setProperties(String type, String id,
			Map<String, String> properties) throws IOException
	{
		super.clearParameters();
		super.setAction(type);
		if (type.equals("page"))
			super.setActionMethod("prop");
		else
			super.setActionMethod("saveprop");
		super.setMethod("POST");
		super.setId(id);

		for (String name : properties.keySet())
		{
			super.setParameter(name, properties.get(name));
		}

		readJSON();
	}

	public Map<String, String> getTemplates() throws IOException
	{
		super.clearParameters();
		super.setAction("template");
		super.setActionMethod("listing");
		super.setMethod("POST");

		JSONObject json = readJSON();

		final Map<String, String> templateMap = new LinkedHashMap<String, String>();

		try
		{
			JSONObject templates = json.getJSONObject("templates");

			for (Iterator ti = templates.keys(); ti.hasNext();)
			{
				String templateId = (String) ti.next();
				String templateName = templates.getJSONObject(templateId)
						.getString("name");
				templateMap.put(templateId, templateName);
			}
		}
		catch (JSONException e)
		{
			Log.w(this.getClass().getSimpleName(), "\n\n" + json);
			throw new OpenRatClientException("a property was not found", e);
		}

		return templateMap;
	}

	public void createFolder(String folderid, String string) throws IOException
	{
		super.clearParameters();
		super.setId(folderid);
		super.setAction("folder");
		super.setActionMethod("createnewfolder");
		super.setMethod("POST");
		super.setParameter("name", string);

		readJSON();
	}

	public void createPage(String folderid, String string, String templateid)
			throws IOException
	{
		super.clearParameters();
		super.setId(folderid);
		super.setAction("folder");
		super.setActionMethod("createnewpage");
		super.setMethod("POST");
		super.setParameter("name", string);
		super.setParameter("templateid", templateid);

		readJSON();
	}

	public void delete(String folderid, String ids) throws IOException
	{
		super.clearParameters();
		super.setMethod("POST");
		super.setAction("folder");
		super.setActionMethod("multiple");
		super.setId(folderid);

		super.setParameter("type", "delete");
		super.setParameter("ids", ids);
		super.setParameter("commit", "1");

		readJSON();

	}

	public byte[] getFileContent(String objectid) throws IOException
	{
		super.clearParameters();
		super.setMethod("GET");
		super.setAction("file");
		super.setActionMethod("show");
		super.setId(objectid);

		final byte[] content = performRequest();
		return content;
	}

	public Map<String, String> getLanguages() throws IOException
	{
		super.clearParameters();
		super.setMethod("GET");
		super.setAction("language");
		super.setActionMethod("listing");

		final Map<String, String> languageMap = new LinkedHashMap<String, String>();

		final JSONObject json = readJSON();
		try
		{
			JSONObject languageObject = json.getJSONObject("el");

			for (Iterator ti = languageObject.keys(); ti.hasNext();)
			{
				String id = (String) ti.next();
				String name = languageObject.getJSONObject(id)
						.getString("name");
				languageMap.put(id, name);
			}
		}
		catch (JSONException e)
		{
			Log.w(this.getClass().getSimpleName(), "\n\n" + json);
			throw new OpenRatClientException("a property was not found", e);
		}
		return languageMap;

	}

	public void setLanguage(String languageid) throws IOException
	{
		super.clearParameters();
		super.setMethod("GET");
		super.setAction("index");
		super.setActionMethod("language");
		super.setId(languageid);
		
		readJSON();
	}
}
