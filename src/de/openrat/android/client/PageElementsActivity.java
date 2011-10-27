/**
 * 
 */
package de.openrat.android.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import de.openrat.android.client.adapter.SimpleNameAdapter;
import de.openrat.android.client.util.OpenRatClientAsyncTask;
import de.openrat.client.OpenRatClient;

/**
 * @author dankert
 * 
 */
public class PageElementsActivity extends ListActivity
{
	public static final String ID = "id";
	public static final String CLIENT = "client";
	private String objectid;
	private OpenRatClient client;

	Map<String, String> data;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.listing);

		client = (OpenRatClient) getIntent().getSerializableExtra(CLIENT);

		new OpenRatClientAsyncTask(this, R.string.waitingforcontent)
		{

			@Override
			protected void callServer() throws IOException
			{
				objectid = getIntent().getStringExtra(ID);
				data = client.getPageElements(objectid);
			}

			protected void doOnSuccess()
			{
				SimpleNameAdapter adapter = new SimpleNameAdapter(
						PageElementsActivity.this, new ArrayList<String>(data
								.values()), android.R.drawable.ic_menu_edit);
				setListAdapter(adapter);
			}

		}.execute();

		ListView list = getListView();
		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				String elementid = Arrays.asList(
						data.keySet().toArray(new String[] {})).get(position);

				Intent intent = new Intent(PageElementsActivity.this,
						EditorActivity.class);
				intent.putExtra(EditorActivity.ELEMENTID, elementid);
				intent.putExtra(EditorActivity.OBJECTID, objectid);
				intent.putExtra(EditorActivity.CLIENT, client);
				startActivity(intent);
			}
		});

	}

}
