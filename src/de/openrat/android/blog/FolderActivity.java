/**
 * 
 */
package de.openrat.android.blog;

import java.io.File;
import java.io.IOException;
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
import de.openrat.android.blog.util.FileUtils;
import de.openrat.client.CMSRequest;

/**
 * @author dankert
 * 
 */
public class FolderActivity extends ListActivity
{

	private static final int NOTIFICATION_UPLOAD = 1;
	private static final int NOTIFICATION_PUBLISH = 2;
	private static final String ID2 = "id";
	public static final String CLIENT = "client";
	private static final String NAME = "name";
	private static final String TYP = "type";
	private static final String DESCRIPTION = "description";
	private static final int ACTIVITY_CHOOSE_FILE = 1;
	private static final int ACTIVITY_CHOOSE_IMAGE = 2;
	private CMSRequest request;
	private ArrayList<FolderEntry> data;
	private String folderid;

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
						JSONObject obj = inhalte.getJSONObject(names
								.getString(i));

						final FolderEntry entry = new FolderEntry();
						entry.type = FType.valueOf(obj.getString("type")
								.toUpperCase());
						entry.name = obj.getString("name");
						entry.description = obj.getString("desc");
						entry.id = names.getString(i);
						data.add(entry);

					}

				} catch (Exception e)
				{
					e.printStackTrace();
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
									// To get the id of the clicked item in the
									// list use menuInfo.id
									FolderEntry en = data.get(mInfo.position);

									request.clearParameters();
									request.trace=true;
									request.setMethod("POST");
									request.setAction("folder");
									request.setActionMethod("multiple");
									request.setId(folderid);


									request.setParameter("type", "delete");
									request.setParameter("ids", en.id);
									// Erstmal alles aktivieren was geht
									// TODO: Abfrage der gewünschten
									// Einstellungen über AlertDialog.
									request.setParameter("commit", "1");
									String response = null;

									// the next two lines initialize the
									// Notification, using the
									// configurations above

									try
									{
										response = request.performRequest();
										JSONObject json = new JSONObject(
												response);
										System.out.println("nachlöschen: "
												+ response);

									} catch (IOException e)
									{
										// System.err.println(response);
										System.err.println(e.getMessage());
									} catch (JSONException e)
									{
										e.printStackTrace();
									} finally
									{
									}
									;
								}
							});
			AlertDialog alert = builder.create();
			alert.show();

			return true;

		case R.id.menu_publish:

			menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

			// To get the id of the clicked item in the list use menuInfo.id
			entry = data.get(menuInfo.position);

			request.clearParameters();
			request.setAction(entry.type.name().toLowerCase());
			request.setActionMethod("pub");

			// Erstmal alles aktivieren was geht
			// TODO: Abfrage der gewünschten Einstellungen über AlertDialog.
			request.setParameter("subdirs", "1");
			request.setParameter("pages", "1");
			request.setParameter("files", "1");
			String response = null;

			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			Intent notificationIntent = new Intent(this, FolderActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);

			// the next two lines initialize the Notification, using the
			// configurations above
			Notification notification = new Notification(R.drawable.publish,
					getResources().getString(R.string.publish), System
							.currentTimeMillis());
			notification.setLatestEventInfo(getApplicationContext(),
					getResources().getString(R.string.publish), entry.name,
					contentIntent);
			notification.flags = Notification.FLAG_ONGOING_EVENT
					| Notification.FLAG_NO_CLEAR;

			nm.notify(NOTIFICATION_PUBLISH, notification);

			try
			{
				Thread.sleep(10000);
				response = request.performRequest();
				JSONObject json = new JSONObject(response);

			} catch (IOException e)
			{
				System.err.println(response);
				System.err.println(e.getMessage());
			} catch (JSONException e)
			{
				e.printStackTrace();
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			} finally
			{
				// notification.flags |= Notification.FLAG_NO_CLEAR;

				notification.setLatestEventInfo(getApplicationContext(),
						getResources().getString(R.string.publish_ok),
						entry.name, contentIntent);
				notification.flags = 0;
				nm.notify(NOTIFICATION_PUBLISH, notification);
				// nm.cancel(NOTIFICATION_PUBLISH);
			}

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
				// Upload durchführen
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

				NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

				Intent notificationIntent = new Intent(this,
						FolderActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(this,
						0, notificationIntent, 0);

				request.clearParameters();
				request.setAction("folder");
				request.setActionMethod("createnewfile");
				request.setMethod("POST");
				request.trace = true;

				try
				{
					final File file = new File(filePath);

					// the next two lines initialize the Notification, using the
					// configurations above
					Notification notification = new Notification(
							R.drawable.upload, getResources().getString(
									R.string.upload), System
									.currentTimeMillis());
					notification.setLatestEventInfo(getApplicationContext(),
							getResources().getString(R.string.upload), file
									.getName(), contentIntent);
					notification.flags |= Notification.FLAG_SHOW_LIGHTS;

					nm.notify(NOTIFICATION_UPLOAD, notification);

					byte[] fileBytes = FileUtils.getBytesFromFile(file);
					request.setFile("file", fileBytes, file.getName(),
							"image/jpeg", "binary");
					// request.setFile("file", inputStream,file.length(),
					// file.getName(), "image/jpeg", "binary");

					ProgressDialog dialog = ProgressDialog.show(
							FolderActivity.this, getResources().getString(
									R.string.loading), getResources()
									.getString(R.string.waitingforlogin));

					String response = request.performRequest();
					dialog.dismiss();

					// String response = request.performRequest("TEST TEST");
					System.out.println("nach dem Hochladen" + response);
					Toast.makeText(this, R.string.publish, Toast.LENGTH_SHORT);

				} catch (IOException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				} finally
				{
					nm.cancel(NOTIFICATION_UPLOAD);
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
