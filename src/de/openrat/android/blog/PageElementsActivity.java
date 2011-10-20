/**
 * 
 */
package de.openrat.android.blog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import de.openrat.android.blog.adapter.SimpleNameAdapter;
import de.openrat.android.blog.util.OpenRatClientAsyncTask;
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
				SimpleNameAdapter adapter = new SimpleNameAdapter(PageElementsActivity.this,
						new ArrayList<String>(data.values()));
				setListAdapter(adapter);
			};

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
				Toast.makeText(PageElementsActivity.this, elementid+": "+data.get(elementid),
						Toast.LENGTH_SHORT);

			}
		});

	}

}
