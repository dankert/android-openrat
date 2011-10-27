/*
 * Openrat CMS-Client for Android
 * 
 * Copyright (C) 2011 Jan Dankert
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.openrat.android.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import de.openrat.android.client.adapter.SimpleNameAdapter;
import de.openrat.android.client.util.OpenRatClientAsyncTask;
import de.openrat.android.client.util.ServerList;
import de.openrat.client.OpenRatClient;

/**
 * @author Jan Dankert
 */
public class OpenRatActivity extends ListActivity
{
	private static final String PREFS_NAME = "OR_BLOG_PREFS";
	private OpenRatClient client;
	private List<String> serverList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listing);

		ImageView image = (ImageView) findViewById(R.id.listimage);
		image.setImageResource(R.drawable.openrat);
		image.setVisibility(View.VISIBLE);

		TextView title = (TextView) findViewById(R.id.listtitle);
		title.setText(getResources().getString(R.string.connect));
		title.setVisibility(View.VISIBLE);

		SharedPreferences globalPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		serverList = Arrays.asList(TextUtils.split(globalPrefs.getString(
				"server", ""), ","));

		ArrayList<String> list = new ArrayList<String>();
		for (String server : serverList)
		{
			SharedPreferences preferences = getSharedPreferences(server,
					MODE_PRIVATE);

			list.add(preferences.getString("name", "?"));
		}

		if (list.size() == 0)
		{
			// Noch kein Server konfiguriert. Hinweis anzeigen!
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getResources().getString(R.string.noserver));
			AlertDialog alert = builder.create();
			alert.show();
		}

		final SimpleNameAdapter adapter = new SimpleNameAdapter(this, list,
				android.R.drawable.ic_menu_set_as);

		ListView lv = getListView();
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int pos, long rowId)
			{
				new OpenRatClientAsyncTask(OpenRatActivity.this,
						R.string.waitingforlogin)
				{
					@Override
					protected void callServer() throws IOException
					{
						SharedPreferences prefs = getSharedPreferences(
								serverList.get(pos), MODE_PRIVATE);

						int port = Integer.parseInt(prefs.getString("port",
								"80"));
						String path = prefs.getString("path", "/");
						String host = prefs.getString("hostname", "");
						String dbid = prefs.getString("database", "");

						client = new OpenRatClient(host, path, port);

						final String username = prefs.getString("username", "");
						client.login(username, prefs.getString("password", ""),
								dbid);
						Log.d(OpenRatActivity.this.getClass().getSimpleName(),
								"User login: " + username);

					}

					protected void doOnSuccess()
					{
						// Verbindung und Login waren erfolgreich.
						// Jetzt zur Projekt-Liste wechseln.
						final Intent intent = new Intent(OpenRatActivity.this,
								ProjectActivity.class);
						intent.putExtra(ProjectActivity.CLIENT, client);
						startActivity(intent);
					};

				}.execute();

			}

		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int pos, long rowId)
			{
				Intent intent = new Intent(OpenRatActivity.this, Server.class);
				intent.putExtra(Server.NAME, serverList.get(pos));
				startActivity(intent);
				return true;
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.main, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_preferences:
				startActivity(new Intent(this, Configuration.class));
				return true;
			case R.id.menu_newserver:

				SharedPreferences globalPrefs = PreferenceManager
						.getDefaultSharedPreferences(this);
				String newServername = "" + System.currentTimeMillis();

				ServerList list = new ServerList(globalPrefs.getString(
						"server", "")).addServer(newServername);
				globalPrefs.edit().putString("server", list.toPlain()).commit();

				Intent intent = new Intent(this, Server.class);
				intent.putExtra(Server.NAME, newServername);
				startActivity(intent);
				return true;
		}
		return false;
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		// Save user preferences. We need an Editor object to
		// make changes. All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		// editor.putBoolean("silentMode", mSilentMode);

		// Don't forget to commit your edits!!!
		editor.commit();
	}
}