package de.openrat.android.blog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class PropertiesActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.properties);

		findViewById(R.id.button_save).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

			}
		});

	}
}
