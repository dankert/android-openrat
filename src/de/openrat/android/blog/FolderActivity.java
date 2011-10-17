/**
 * 
 */
package de.openrat.android.blog;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import de.openrat.android.blog.FolderEntry.FType;
import de.openrat.android.blog.adapter.FolderContentAdapter;
import de.openrat.android.blog.service.PublishIntentService;
import de.openrat.android.blog.service.UploadIntentService;
import de.openrat.client.CMSRequest;
import de.openrat.client.OpenRatClient;

/**
 * @author dankert
 * 
 */
public class FolderActivity extends ListActivity
{

	private static final String ID2 = "id";
	public static final String CLIENT = "client";
	private static final String NAME = "name";
	private static final String TYP = "type";
	private static final String DESCRIPTION = "description";
	private static final int ACTIVITY_CHOOSE_FILE = 1;
	private static final int ACTIVITY_CHOOSE_IMAGE = 2;
	private OpenRatClient client;
	private List<FolderEntry> data;
	private String folderid;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.listing);

		super.onCreate(savedInstanceState);

		data = new ArrayList<FolderEntry>();
		client = (OpenRatClient) getIntent().getSerializableExtra(CLIENT);

		AsyncTask<String, Void, List<FolderEntry>> loadTask = new AsyncTask<String, Void, List<FolderEntry>>()
		{

			ProgressDialog dialog = new ProgressDialog(FolderActivity.this);

			@Override
			protected void onPreExecute()
			{
				dialog.setTitle(getResources().getString(R.string.loading));
				dialog.setMessage(getResources().getString(
						R.string.waitingforcontent));
				dialog.show();
			}

			protected void onPostExecute(List<FolderEntry> result)
			{
				dialog.dismiss();

				final ListAdapter adapter = new FolderContentAdapter(
						FolderActivity.this, data);
				setListAdapter(adapter);
			};

			@Override
			protected List<FolderEntry> doInBackground(String... params)
			{
				//
				folderid = getIntent().getStringExtra("folderid");

				try
				{
					data = client.getFolderEntries(folderid);
				}
				catch (IOException e)
				{
					Log.e(this.getClass().getName(),e.getMessage(),e);
					Toast.makeText(FolderActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
				}
				
				return data;
			}
		};

		loadTask.execute();
		// final ListAdapter adapter = new SimpleAdapter(this, data,
		// R.layout.listing_entry, from, to);

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
						intent.putExtra(CLIENT, client);
						intent.putExtra("folderid", entry.id);
						startActivity(intent);
						break;
					case PAGE:
						intent = new Intent(FolderActivity.this,
								PageElementsActivity.class);
						intent.putExtra(CLIENT, client);
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
				intent = new Intent(FolderActivity.this,
						PropertiesActivity.class);
				intent.putExtra(CLIENT, client);
				intent.putExtra("objectid", entry.id);
				intent.putExtra(TYP, entry.type);
				startActivity(intent);
				return true;

			case R.id.menu_delete:

				final AdapterContextMenuInfo mInfo = (AdapterView.AdapterContextMenuInfo) item
						.getMenuInfo();

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Sicher?").setCancelable(false)
						.setPositiveButton(
								getResources().getString(R.string.delete),
								new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog,
											int id)
									{
										// To get the id of the clicked item in
										// the
										// list use menuInfo.id
										FolderEntry en = data
												.get(mInfo.position);

										client.clearParameters();
										client.trace = true;
										client.setMethod("POST");
										client.setAction("folder");
										client.setActionMethod("multiple");
										client.setId(folderid);

										client.setParameter("type", "delete");
										client.setParameter("ids", en.id);
										// Erstmal alles aktivieren was geht
										// TODO: Abfrage der gewünschten
										// Einstellungen über AlertDialog.
										client.setParameter("commit", "1");
										String response = null;

										// the next two lines initialize the
										// Notification, using the
										// configurations above

										try
										{
											response = client.performRequest();
											JSONObject json = new JSONObject(
													response);
											System.out.println("nachlöschen: "
													+ response);

										}
										catch (IOException e)
										{
											// System.err.println(response);
											System.err.println(e.getMessage());
										}
										catch (JSONException e)
										{
											e.printStackTrace();
										}
										finally
										{
										}
										;
									}
								});
				AlertDialog alert = builder.create();
				alert.show();

				return true;

			case R.id.menu_publish:

				menuInfo = (AdapterView.AdapterContextMenuInfo) item
						.getMenuInfo();
				entry = data.get(menuInfo.position);

				final Intent publishIntent = new Intent(this,
						PublishIntentService.class);

				publishIntent.putExtra(PublishIntentService.EXTRA_REQUEST,
						client);
				publishIntent.putExtra(PublishIntentService.EXTRA_TYPE,
						entry.type.name().toLowerCase());
				publishIntent.putExtra(PublishIntentService.EXTRA_NAME,
						entry.name);
				publishIntent.putExtra(PublishIntentService.EXTRA_ID, entry.id);
				startService(publishIntent);

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
								}).setSingleChoiceItems(
								new String[] { "a", "b" }, 1,
								new OnClickListener()
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

				chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
				chooseFile.setType("image/*");
				intent = Intent.createChooser(chooseFile, "Choose an image");
				startActivityForResult(intent, ACTIVITY_CHOOSE_IMAGE);
				return true;

			case R.id.menu_newfolder:
			case R.id.menu_newpage:

				intent = new Intent(this, NewActivity.class);
				intent.putExtra("request", getIntent().getSerializableExtra(
						"request"));
				intent.putExtra("menuid", item.getItemId());
				startActivity(intent);
				return true;

			default:
				Toast.makeText(this, "??: " + item.getItemId(),
						Toast.LENGTH_SHORT);

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
						// Upload durchführen
						Uri uri = data.getData();

						String filePath;
						if (requestCode == ACTIVITY_CHOOSE_IMAGE)
						{

							filePath = getPath(uri);
						}
						else
						{
							filePath = uri.getPath();
						}

						final Intent uploadIntent = new Intent(
								FolderActivity.this, UploadIntentService.class);
						uploadIntent.putExtra(
								UploadIntentService.EXTRA_FILENAME, filePath);
						uploadIntent.putExtra(
								UploadIntentService.EXTRA_REQUEST, client);
						startService(uploadIntent);

						Toast.makeText(this, R.string.publish,
								Toast.LENGTH_SHORT);
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
