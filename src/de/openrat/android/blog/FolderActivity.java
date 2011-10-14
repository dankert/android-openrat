/**
 * 
 */
package de.openrat.android.blog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import de.openrat.android.blog.FolderEntry.FType;
import de.openrat.android.blog.adapter.FolderContentAdapter;
import de.openrat.android.blog.client.CMSRequest;

/**
 * @author dankert
 * 
 */
public class FolderActivity extends ListActivity
{

	private static final String BOUNDARY = "usadlkuuusdkcua43sfd";
	private static final String ID2 = "id";
	public static final String CLIENT = "client";
	private static final String NAME = "name";
	private static final String TYP = "type";
	private static final String DESCRIPTION = "description";
	private static final int ACTIVITY_CHOOSE_FILE = 1;
	private static final int ACTIVITY_CHOOSE_IMAGE = 2;
	private CMSRequest request;
	private ArrayList<FolderEntry> data;

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
		data = new ArrayList<FolderEntry>();

		request = (CMSRequest) getIntent().getSerializableExtra(CLIENT);

		//
		String folderid = getIntent().getStringExtra("folderid");
		String response = null;
		if (folderid == null)
		{
			request.clearParameters();
			request.setParameter("action", "tree");
			request.setParameter("subaction", "load");

			try
			{
				response = request.performRequest();
				System.out.println("Ordnerinhalt: " + response);
				JSONObject json = new JSONObject(response);
				folderid = json.getJSONArray("zeilen").getJSONObject(1)
						.getString("name");
				// JSONObject inhalte = json.getJSONObject("object");
				// folderid = ...;

			} catch (IOException e)
			{
				System.err.println("Fuck Folder");
				System.err.println(response);
				System.err.println(e.getMessage());
			} catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Folderid: " + folderid);

		request.clearParameters();
		request.setParameter("id", folderid);

		request.setParameter("action", "folder");
		request.setParameter("subaction", "show");

		response = null;
		try
		{
			ProgressDialog dialog = ProgressDialog.show(FolderActivity.this,
					getResources().getString(R.string.loading), getResources()
							.getString(R.string.waiting));
			// try
			// {
			// Thread.sleep(2000L);
			// } catch (InterruptedException e)
			// {
			// e.printStackTrace();
			// }
			response = request.performRequest();
			System.out.println("Ordnerinhalt: " + response);
			dialog.dismiss();

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

				final FolderEntry entry = new FolderEntry();
				entry.type = FType.valueOf(obj.getString("type").toUpperCase());
				entry.name = obj.getString("name");
				entry.description = obj.getString("desc");
				entry.id = names.getString(i);
				data.add(entry);

			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

		// final ListAdapter adapter = new SimpleAdapter(this, data,
		// R.layout.listing_entry, from, to);
		final ListAdapter adapter = new FolderContentAdapter(this, data);
		setListAdapter(adapter);

		ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				FolderEntry entry = data.get(position);

				final Intent intent;
				switch (entry.type)
				{
				case FOLDER:

					intent = new Intent(FolderActivity.this,
							FolderActivity.class);
					intent.putExtra(CLIENT, request);
					intent.putExtra("folderid", entry.id);
					startActivity(intent);
					break;
				case PAGE:
					intent = new Intent(FolderActivity.this,
							PageElementsActivity.class);
					intent.putExtra(CLIENT, request);
					intent.putExtra("pageid", entry.id);
					startActivity(intent);
				default:
				}
			}
		});

		list.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
		{

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenu.ContextMenuInfo menuInfo)
			{
				MenuInflater menuInflater = getMenuInflater();
				menuInflater.inflate(R.menu.context, menu);
				// menu.add(0, 5, 0, "Click Me");

			}

		});
		/**
		 * 
		 list.setonOnItemLongClickListener(new OnItemLongClickListener() {
		 * 
		 * @Override public boolean onItemLongClick(AdapterView<?> parent, View
		 *           view, int position, long id) { Map<String, String> entry =
		 *           data.get(position);
		 * 
		 *           final Intent intent; intent = new
		 *           Intent(FolderActivity.this, PropertiesActivity.class);
		 *           intent.putExtra(CLIENT, request);
		 *           intent.putExtra("objectid", entry.get(ID2));
		 *           startActivity(intent); return true; } });
		 */
	}

	public boolean onContextItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{

		case R.id.menu_properties:

			// This is actually where the magic happens.

			// As we use an adapter view (which the ListView is)

			// We can cast item.getMenuInfo() to AdapterContextMenuInfo

			AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();

			// To get the id of the clicked item in the list use menuInfo.id

			// Log.d("t", "list pos:" + menuInfo.position + " id:" +
			// menuInfo.id);
			FolderEntry entry = data.get(menuInfo.position);
			final Intent intent;
			intent = new Intent(FolderActivity.this, PropertiesActivity.class);
			intent.putExtra(CLIENT, request);
			intent.putExtra("objectid", entry.id);
			intent.putExtra(TYP, entry.type);
			startActivity(intent);
			return true;

		case R.id.menu_publish:

			// This is actually where the magic happens.

			// As we use an adapter view (which the ListView is)

			// We can cast item.getMenuInfo() to AdapterContextMenuInfo

			menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

			// To get the id of the clicked item in the list use menuInfo.id

			entry = data.get(menuInfo.position);
			Toast.makeText(this, R.string.publish, Toast.LENGTH_SHORT);

			return true;

		default:

			return super.onContextItemSelected(item);

		}

		// return true;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.folder, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_language:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.language).setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog,
										int id)
								{
									// Speichern
								}
							}).setSingleChoiceItems(new String[] { "a", "b" },
							1, new OnClickListener()
							{

								@Override
								public void onClick(DialogInterface dialog,
										int which)
								{

								}
							});
			AlertDialog alert = builder.create();
			alert.show();
			return true;

		case R.id.menu_model:

			Toast.makeText(this, "Model", Toast.LENGTH_SHORT);
			return true;
		case R.id.menu_upload:

			Intent chooseFile;
			Intent intent;
			chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
			chooseFile.setType("file/*");
			intent = Intent.createChooser(chooseFile, "Choose a file");
			startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
			return true;
		case R.id.menu_upload_image:

			// Intent chooseFile;
			// Intent intent;
			chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
			chooseFile.setType("image/*");
			intent = Intent.createChooser(chooseFile, "Choose an image");
			startActivityForResult(intent, ACTIVITY_CHOOSE_IMAGE);
			return true;

		default:
			Toast.makeText(this, "??: " + item.getItemId(), Toast.LENGTH_SHORT);

		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		case ACTIVITY_CHOOSE_FILE:
		case ACTIVITY_CHOOSE_IMAGE:
		{
			if (resultCode == RESULT_OK)
			{

				Uri uri = data.getData();

				String filePath;
				if (requestCode == ACTIVITY_CHOOSE_IMAGE)
				{

					filePath = getPath(uri);
				} else
				{
					filePath = uri.getPath();
				}

				System.out.println(filePath);

				request.clearParameters();
				request.setAction("folder");
				request.setActionMethod("createnewfile");
				request.setMethod("POST");
				request.trace = true;

				try
				{
					final File file = new File(filePath);
					BufferedInputStream br = new BufferedInputStream(
							new FileInputStream(file));

					StringBuffer fileContent = new StringBuffer();
					// create a byte array
					byte[] contents = new byte[1024];

					int bytesRead;

					while ((bytesRead = br.read(contents)) != -1)
					{
						fileContent.append(new String(contents, 0, bytesRead));
						// fileContent.append("Hallo ");
					}

					// System.out.println("Body: \n\n\n\n\n"+body+"\n\n\n\n"+"Länge: "+body.length()+"\n\n");
					request.setFile("file", fileContent.toString().getBytes(),
							file.getName(), "image/jpeg", "binary");

					String response = request.performRequest();
					// String response = request.performRequest("TEST TEST");
					System.out.println("nach dem Hochladen" + response);
					Toast.makeText(this, R.string.publish, Toast.LENGTH_SHORT);

				} catch (IOException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
		}
	}

	public String getPath(Uri uri)
	{
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
}
