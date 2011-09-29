/**
 * 
 */
package de.openrat.android.blog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import de.openrat.android.blog.client.CMSRequest;

/**
 * @author dankert
 * 
 */
public class ProjectActivity extends ListActivity
{
	private static final String ID2 = "id";
	public static final String CLIENT = "client";
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private CMSRequest request;
	private List<Map<String, String>> data;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.listing);
		setTitle(R.string.connect);

		super.onCreate(savedInstanceState);

		int[] to = new int[] { R.id.listentry_name, R.id.listentry_description };
		;
		String[] from = new String[] { NAME, DESCRIPTION };
		;
		data = new ArrayList<Map<String, String>>();

		request = (CMSRequest) getIntent().getSerializableExtra(CLIENT);

		request.clearParameters();
		request.setParameter("action", "index");
		request.setParameter("subaction", "projectmenu");
		String response = null;
		try
		{
			response = request.performRequest();

		} catch (IOException e)
		{
			response = e.getMessage();
		}

		try
		{
			System.out.println(response);
			JSONObject json = new JSONObject(response);
			JSONArray projects = json.getJSONArray("projects");
			for (int i = 0; i < projects.length(); i++)
			{
				final Map<String, String> values = new HashMap<String, String>();
				JSONObject project = projects.getJSONObject(i);
				values.put(NAME, project.getString("name"));
				values.put(DESCRIPTION, "");
				values.put(ID2, project.getString("id"));
				data.add(values);

			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

		ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Toast.makeText(ProjectActivity.this, "Click on " + position,
						Toast.LENGTH_SHORT).show();

				final Intent i = new Intent(ProjectActivity.this,
						FolderActivity.class);
				i.putExtra(CLIENT, request);

				// Projekt auswählen
				String projectid = data.get(position).get(ID2);

				request = (CMSRequest) getIntent().getSerializableExtra(CLIENT);

				request.clearParameters();
				request.setParameter("action", "index");
				request.setParameter("subaction", "project");
				request.setParameter("id", projectid);
				String response = null;
				try
				{
					response = request.performRequest();
					System.out.println("Projekt ausgewählt: " + response);

				} catch (IOException e)
				{
					System.err.println("Fehler bei Projektauswahl: "+response);
					System.err.println(e.getMessage());
				}

				startActivity(i);
			}
		});

		final ListAdapter adapter = new SimpleAdapter(this, data,
				R.layout.listing_entry, from, to);
		setListAdapter(adapter);
	}
}
