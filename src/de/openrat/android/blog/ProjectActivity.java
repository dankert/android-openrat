/**
 * 
 */
package de.openrat.android.blog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import de.openrat.android.blog.adapter.FolderContentAdapter;
import de.openrat.client.OpenRatClient;

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
	private OpenRatClient client;
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

		client = (OpenRatClient) getIntent().getSerializableExtra(CLIENT);

		AsyncTask<String, Void, List<FolderEntry>> loadProjectsTask = new AsyncTask<String, Void, List<FolderEntry>>()
		{

			ProgressDialog dialog = new ProgressDialog(ProjectActivity.this);

			@Override
			protected void onPreExecute()
			{
				dialog.setTitle(getResources().getString(R.string.loading));
				dialog.setMessage(getResources().getString(
						R.string.waitingforprojects));
				dialog.show();
			}

			protected void onPostExecute(List<FolderEntry> result)
			{
				dialog.dismiss();

				final ListAdapter adapter = new FolderContentAdapter(
						ProjectActivity.this, data);
				setListAdapter(adapter);
			};

			@Override
			protected List<FolderEntry> doInBackground(String... params)
			{
				//
				try
				{
					data = client.loadProjects();
				}
				catch (final IOException e)
				{
					Log.e(this.getClass().getName(), e.getMessage(), e);
					runOnUiThread(new Runnable()
					{

						@Override
						public void run()
						{
							Toast.makeText(ProjectActivity.this,
									e.getMessage(), Toast.LENGTH_SHORT);
						}
					});
					data = new ArrayList<FolderEntry>();
				}

				return data;
			}
		};
		loadProjectsTask.execute();

		ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				// Projekt auswählen
				final String projectid = data.get(position).id;

				AsyncTask<String, Void, Void> startProjectTask = /**
				 * Starten des
				 * ausgewählten Projektes.
				 * 
				 * @author dankert
				 * 
				 */
				new AsyncTask<String, Void, Void>()
				{

					ProgressDialog dialog = new ProgressDialog(
							ProjectActivity.this);

					@Override
					protected void onPreExecute()
					{
						dialog.setTitle(R.string.loading);
						dialog.setMessage(getResources().getString(
								R.string.waitingforselectproject));
						dialog.show();
					}

					protected void onPostExecute(Void result)
					{
						dialog.dismiss();
					};

					@Override
					protected Void doInBackground(String... params)
					{
						//
						try
						{
							client.selectProject(projectid);
						}
						catch (IOException e)
						{
							Log.e(this.getClass().getName(), e.getMessage(), e);
							Toast.makeText(ProjectActivity.this,
									e.getMessage(), Toast.LENGTH_SHORT);
						}

						final Intent i = new Intent(ProjectActivity.this,
								FolderActivity.class);
						i.putExtra(CLIENT, client);

						startActivity(i);

						return null;
					}
				};

				startProjectTask.execute();

			}
		});

	}
}
