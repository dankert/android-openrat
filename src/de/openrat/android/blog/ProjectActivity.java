/**
 * 
 */
package de.openrat.android.blog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import de.openrat.android.blog.FolderEntry.FType;
import de.openrat.android.blog.adapter.FolderContentAdapter;
import de.openrat.client.CMSRequest;

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
	private List<FolderEntry> data;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.listing);

		super.onCreate(savedInstanceState);

		int[] to = new int[] { R.id.listentry_name, R.id.listentry_description };
		;
		String[] from = new String[] { NAME, DESCRIPTION };
		;
		data = new ArrayList<FolderEntry>();

		request = (CMSRequest) getIntent().getSerializableExtra(CLIENT);

		request.clearParameters();
		request.setParameter("action", "index");
		request.setParameter("subaction", "projectmenu");
		String response = null;
		try
		{
			ProgressDialog dialog = ProgressDialog.show(ProjectActivity.this,
					getResources().getString(R.string.loading), getResources()
							.getString(R.string.waitingforprojects));
			response = request.performRequest();
			dialog.dismiss();

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
				JSONObject project = projects.getJSONObject(i);

				final FolderEntry entry = new FolderEntry();
				entry.type = FType.PROJECT;
				entry.name = project.getString("name");
				entry.description = "";
				entry.id = project.getString("id");

				data.add(entry);

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
				final Intent i = new Intent(ProjectActivity.this,
						FolderActivity.class);
				i.putExtra(CLIENT, request);

				// Projekt auswählen
				String projectid = data.get(position).id;

				request = (CMSRequest) getIntent().getSerializableExtra(CLIENT);

				request.clearParameters();
				request.setParameter("action", "index");
				request.setParameter("subaction", "project");
				request.setParameter("id", projectid);
				String response = null;
				try
				{
					ProgressDialog dialog = ProgressDialog.show(
							ProjectActivity.this, getResources().getString(
									R.string.loading), getResources()
									.getString(R.string.waitingforlogin));
					response = request.performRequest();
					dialog.dismiss();

				} catch (IOException e)
				{
					Toast.makeText(ProjectActivity.this, e.getMessage(),
							Toast.LENGTH_LONG);
					System.err
							.println("Fehler bei Projektauswahl: " + response);
					System.err.println(e.getMessage());
				}

				startActivity(i);
			}
		});

		final ListAdapter adapter = new FolderContentAdapter(this, data);
		setListAdapter(adapter);
	}
}
