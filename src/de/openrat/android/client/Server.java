package de.openrat.android.client;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import de.openrat.android.client.util.ServerList;

public class Server extends PreferenceActivity
{

	public static final String NAME = "name";
	private String serverName;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.serverName = getIntent().getStringExtra(NAME);
		getPreferenceManager().setSharedPreferencesName(serverName);
		
		addPreferencesFromResource(R.xml.server);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.server, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_delete:

				SharedPreferences globalPrefs = PreferenceManager
						.getDefaultSharedPreferences(this);
				ServerList serverList= new ServerList(
						globalPrefs.getString("server", ""));
				
				// Server entfernen und alles löschen.
				serverList.removeServer(serverName);
				getPreferenceManager().getSharedPreferences().edit().clear().commit();
				
				globalPrefs.edit().putString("server",
						serverList.toPlain()).commit();
				finish();
				return true;
		}
		return false;
	}

}
