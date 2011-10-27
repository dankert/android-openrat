/**
 * 
 */
package de.openrat.android.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import de.openrat.android.client.adapter.FolderContentAdapter;
import de.openrat.android.client.util.OpenRatClientAsyncTask;
import de.openrat.client.OpenRatClient;

/**
 * @author dankert
 * 
 */
public class ProjectActivity extends ListActivity
{
	public static final String CLIENT = "client";
	private OpenRatClient client;
	private List<FolderEntry> data = new ArrayList<FolderEntry>();

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.listing);

		super.onCreate(savedInstanceState);

		client = (OpenRatClient) getIntent().getSerializableExtra(CLIENT);

		new OpenRatClientAsyncTask(this, R.string.waitingforprojects)
		{

			protected void doOnSuccess()
			{
				final ListAdapter adapter = new FolderContentAdapter(
						ProjectActivity.this, data);
				setListAdapter(adapter);
			}

			protected void callServer() throws IOException
			{

				data = client.loadProjects();
				Log.d(ProjectActivity.this.getClass().getSimpleName(), "Lade Projekte: "+data.toString() );
			}
		}.execute();

		ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				// Projekt auswählen
				final String projectid = data.get(position).id;

				new OpenRatClientAsyncTask(ProjectActivity.this,
						R.string.waitingforselectproject)
				{

					@Override
					protected void callServer() throws IOException
					{
						client.selectProject(projectid);
						Log.d(ProjectActivity.this.getClass().getSimpleName(), "Waehle Projekt: "+projectid );
					}

					@Override
					protected void doOnSuccess()
					{
						final Intent i = new Intent(ProjectActivity.this,
								FolderActivity.class);
						i.putExtra(CLIENT, client);

						startActivity(i);

					}
					protected void doOnError(IOException error) {
						super.doOnError(error);
					};
				}.execute();

			}
		});

	}
}
