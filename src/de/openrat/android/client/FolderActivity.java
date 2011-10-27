/**
 * 
 */
package de.openrat.android.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import de.openrat.android.blog.R;
import de.openrat.android.blog.adapter.FolderContentAdapter;
import de.openrat.android.blog.service.PublishIntentService;
import de.openrat.android.blog.service.UploadIntentService;
import de.openrat.android.blog.util.OpenRatClientAsyncTask;
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

		new OpenRatClientAsyncTask(this, R.string.waitingforcontent)
		{
			@Override
			protected void callServer() throws IOException
			{
				folderid = getIntent().getStringExtra("folderid");
				if (folderid == null)
					folderid = client.getRootFolder();

				data = client.getFolderEntries(folderid);
			}

			protected void doOnSuccess()
			{
				final ListAdapter adapter = new FolderContentAdapter(
						FolderActivity.this, data);
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
						intent.putExtra(PageElementsActivity.ID, entry.id);
						startActivity(intent);
						break;
					case FILE:
						intent = new Intent(FolderActivity.this,
								FileShowActivity.class);
						intent.putExtra(CLIENT, client);
						intent.putExtra(FileShowActivity.ID, entry.id);
						startActivity(intent);
						break;
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
				intent.putExtra(TYP, entry.type.name().toLowerCase());
				startActivity(intent);
				return true;

			case R.id.menu_delete:

				final AdapterContextMenuInfo mInfo = (AdapterView.AdapterContextMenuInfo) item
						.getMenuInfo();

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(
						getResources().getString(R.string.areyousure))
						.setCancelable(false).setPositiveButton(
								getResources().getString(R.string.delete),
								new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog,
											int id)
									{
										final FolderEntry en = data
												.get(mInfo.position);

										new OpenRatClientAsyncTask(
												FolderActivity.this,
												R.string.waitingfordelete)
										{
											@Override
											protected void callServer()
													throws IOException
											{
												client.delete(folderid, en.id);
											}
										}.execute();
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
				new OpenRatClientAsyncTask(FolderActivity.this,
						R.string.waitingforlanguageload)
				{
					private Map<String, String> languages;

					@Override
					protected void callServer() throws IOException
					{
						languages = client.getLanguages();
					}

					@Override
					protected void doOnSuccess()
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(
								FolderActivity.this);
						final String[] languageIds = languages.keySet()
								.toArray(new String[0]);
						final String[] languageNames = languages.values()
								.toArray(new String[0]);
						
						builder.setTitle(R.string.language).setItems(
								languageNames, new OnClickListener()
								{

									@Override
									public void onClick(DialogInterface dialog,
											int which)
									{
										final String newlanguageid = languageIds[which];
										new OpenRatClientAsyncTask(
												FolderActivity.this,
												R.string.waitingforlanguagesave)
										{
											@Override
											protected void callServer()
													throws IOException
											{
												client
														.setLanguage(newlanguageid);
											}
										}.execute();
										//alert.cancel();
									}
								});
						final AlertDialog alert = builder.create();
						alert.show();
					}
				}.execute();

				return true;

			case R.id.menu_upload:

				Intent chooseFile;
				Intent intent;
				chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
				chooseFile.setType("file/*");
				intent = Intent.createChooser(chooseFile, getResources()
						.getString(R.string.choosefile));
				startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
				return true;
			case R.id.menu_upload_image:

				chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
				chooseFile.setType("image/*");
				intent = Intent.createChooser(chooseFile, getResources()
						.getString(R.string.chooseimage));
				startActivityForResult(intent, ACTIVITY_CHOOSE_IMAGE);
				return true;

			case R.id.menu_newfolder:
			case R.id.menu_newpage:

				intent = new Intent(this, NewActivity.class);
				intent.putExtra(NewActivity.EXTRA_CLIENT, client);
				intent.putExtra(NewActivity.EXTRA_MENUID, item.getItemId());
				intent.putExtra(NewActivity.EXTRA_FOLDERID, folderid);
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
