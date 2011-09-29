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
import android.widget.AdapterView.OnItemClickListener;
import de.openrat.android.blog.client.CMSRequest;

/**
 * @author dankert
 * 
 */
public class FolderActivity extends ListActivity
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

		//
		request.clearParameters();
		request.setParameter("action", "folder");
		request.setParameter("subaction", "show");
		String folderid = getIntent().getStringExtra("folderid");
		request.setParameter("id", folderid != null ? folderid : "1");

		String response = null;
		try
		{
			response = request.performRequest();
			System.out.println("Ordnerinhalt: " + response);

		} catch (IOException e)
		{
			System.err.println("Fuck Folder");
			System.err.println(response);
			System.err.println(e.getMessage());
		}

		try
		{
			System.out.println(response);
			JSONObject json = new JSONObject(response);
			JSONObject inhalte = json.getJSONObject("object");
			JSONArray names = inhalte.names();
			for (int i = 0; i < names.length(); i++)
			{
				JSONObject obj = inhalte.getJSONObject(names.getString(i));

				final Map<String, String> values = new HashMap<String, String>();
				values.put(NAME, "[" + obj.getString("type") + "] "
						+ obj.getString("name"));
				values.put(DESCRIPTION, obj.getString("desc"));
				values.put(ID2, names.getString(i));
				data.add(values);

			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

		final ListAdapter adapter = new SimpleAdapter(this, data,
				R.layout.listing_entry, from, to);
		setListAdapter(adapter);

		ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				final Intent i = new Intent(FolderActivity.this,
						FolderActivity.class);
				i.putExtra(CLIENT, request);
				i.putExtra("folderid", data.get(position).get(ID2));
				startActivity(i);
			}
		});
	}

}
