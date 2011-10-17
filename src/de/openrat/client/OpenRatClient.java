package de.openrat.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.openrat.android.blog.FolderEntry;
import de.openrat.android.blog.FolderEntry.FType;
import de.openrat.android.blog.util.FileUtils;

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
	public List<FolderEntry> getFolderEntries(String folderid)
			throws IOException
	{
		if (folderid == null)
		{

			clearParameters();
			setAction("tree");
			setActionMethod("load");

			JSONObject json = readJSON();

			try
			{
				folderid = json.getJSONArray("zeilen").getJSONObject(1)
						.getString("name");
			}
			catch (JSONException e)
			{
				throw new OpenRatClientException(
						"JSON-Error while resolving root folder", e);
			}
		}

		final List<FolderEntry> data = new ArrayList<FolderEntry>();

		super.clearParameters();
		super.setParameter("id", folderid);

		super.setParameter("action", "folder");
		super.setParameter("subaction", "show");

		try
		{
			JSONObject json = readJSON();
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
		String response;
		try
		{
			response = super.performRequest();
		}
		catch (IOException e)
		{
			throw new OpenRatClientException(
					"I/O-Error while performing the request", e);
		}

		try
		{
			final JSONObject json = new JSONObject(response);

			try
			{
				// Versuchen, die 1. Notice zu lesen. Falls es eine gibt, Exception mit dem Notice-Text werfen.
				final String msgText = json.getJSONArray("notices")
						.getJSONObject(0).getString("text");
				throw new OpenRatClientException(msgText);
			}
			catch (JSONException e)
			{
				// Keine Notice gefunden. Das deutet auf eine fehlerfreie Ausführung hin :)
				return json;
			}

		}
		catch (JSONException e)
		{
			throw new OpenRatClientException(
					"JSON Parsing Error. Original repsonse was:\n" + response,
					e);
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

	public void login(String login, String password) throws IOException
	{
		super.setParameter("action", "index");
		super.setParameter("subaction", "login");
		super.setParameter("dbid", "db1");
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
}
