package de.openrat.android.blog;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Configuration extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
