package de.openrat.android.blog;

import java.io.IOException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import de.openrat.android.blog.client.CMSRequest;

public class OpenRatBlog extends Activity
{
	private static final String PREFS_NAME = "OR_BLOG_PREFS";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		CMSRequest request = new CMSRequest("demo.openrat.de");

		request.setParameter("action", "index");
		request.setParameter("subaction", "login");
		request.setParameter("dbid", "db1");
		request.setParameter("login_name", "admin");
		request.setParameter("login_password", "admin");
		String response = null;
		try
		{
			response = request.performRequest();

		} catch (IOException e)
		{
			response = e.getMessage();
		}

		try
		{
			JSONObject json = new JSONObject(response);
			JSONObject session = json.getJSONObject("session");
			final String sessionName = session.getString("name");
			final String sessionId = session.getString("id");

			final String msgText = json.getJSONArray("notices")
					.getJSONObject(0).getString("text");
//			final String msgText2 = json.getJSONArray("notics")
//					.getJSONObject(0).getString("text");

			// TextView text = new TextView(this);
			// text.setText("Sitzung '" + sessionId + "': " + sessionId);

			request.setCookie(sessionName, sessionId);
			TextView tv = new TextView(this);
			tv.setVerticalScrollBarEnabled(true);
			tv.setHorizontalScrollBarEnabled(true);
			tv.setScrollBarStyle(TextView.SCROLLBARS_INSIDE_INSET);
			tv.setText(msgText + "\nSitzung '" + sessionName + "': "
					+ sessionId + "\nAusgabe: " + response);
			
			ScrollView scrollView =new ScrollView(this);
			scrollView.setScrollContainer(true);
			scrollView.setFocusable(true);
			scrollView.addView(tv);

			setContentView(scrollView);

		} catch (Exception e)
		{
			response = e.getMessage();

			TextView tv = new TextView(this);
			tv.setText("Fehler: " + response);
			setContentView(tv);

		}

		// Restore preferences
		// SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		// boolean silent = settings.getBoolean("silentMode", false);

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
			startActivity( new Intent(this,Configuration.class));
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