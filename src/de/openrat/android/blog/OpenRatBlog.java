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
package de.openrat.android.blog;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import de.openrat.client.OpenRatClient;

/**
 * @author Jan Dankert
 */
public class OpenRatBlog extends Activity
{
	private static final String PREFS_NAME = "OR_BLOG_PREFS";
	private OpenRatClient client;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		int port = Integer.parseInt(prefs.getString("port", "80"));
		String path = prefs.getString("path", "/");
		String host = prefs.getString("hostname", "");

		client = new OpenRatClient(host, path, port);

		@SuppressWarnings("unused")
		TextView tv = (TextView) findViewById(R.id.hello);

		View connect = findViewById(R.id.connect);
		connect.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				AsyncTask<Void, Void, Void> loginTask = new AsyncTask<Void, Void, Void>()
				{
					ProgressDialog dialog = new ProgressDialog(OpenRatBlog.this);

					@Override
					protected void onPreExecute()
					{
						dialog.setTitle(R.string.loading);
						dialog.setMessage(getResources().getString(
								R.string.waitingforlogin));
						dialog.show();
					}

					@Override
					protected void onPostExecute(Void result)
					{
						dialog.dismiss();
					}

					@Override
					protected Void doInBackground(Void... params)
					{
						try
						{
							client.login(prefs.getString("username", ""), prefs
									.getString("password", ""));


							// Verbindung und Login waren erfolgreich.
							// Jetzt zur Projekt-Liste wechseln.
							final Intent intent = new Intent(OpenRatBlog.this,
									ProjectActivity.class);
							intent.putExtra(ProjectActivity.CLIENT, client);
							startActivity(intent);
						}
						catch (final IOException e1)
						{
							dialog.dismiss();
							// Verbindung nicht möglich...
							Log.i(this.getClass().getName(), e1.getMessage(),
									e1);
							runOnUiThread(new Runnable()
							{

								@Override
								public void run()
								{
									Toast.makeText(OpenRatBlog.this, e1
											.getMessage(), Toast.LENGTH_LONG);
								}
							});
						}
						
						return null;
					}
				};
				loginTask.execute();

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