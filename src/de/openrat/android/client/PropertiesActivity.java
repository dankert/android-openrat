package de.openrat.android.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import de.openrat.client.OpenRatClient;

public class PropertiesActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.properties);

		final OpenRatClient client = (OpenRatClient) getIntent()
				.getSerializableExtra("client");
		final String id = getIntent().getStringExtra("objectid");
		final String type = getIntent().getStringExtra("type");

		try
		{
			final Map<String, String> properties = client.getProperties(type,
					id);

			final EditText editTextName = (EditText) findViewById(R.id.name);
			editTextName.setText(properties.get("name"));

			final EditText editTextFilename = (EditText) findViewById(R.id.filename);
			editTextFilename.setText(properties.get("filename"));

			final EditText editTextDesc = (EditText) findViewById(R.id.description);
			editTextDesc.setText(properties.get("description"));

			findViewById(R.id.button_save).setOnClickListener(
					new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Map<String, String> properties = new HashMap<String, String>();
							properties.put("name", editTextName.getText()
									.toString());
							properties.put("filename", editTextFilename
									.getText().toString());
							properties.put("description", editTextDesc
									.getText().toString());
							try
							{
								client.setProperties(type, id, properties);
							}
							catch (IOException e)
							{
								Log.e(this.getClass().getSimpleName(), e
										.getMessage(), e);
								Toast.makeText(PropertiesActivity.this, e
										.getMessage(), Toast.LENGTH_SHORT);
							}
							PropertiesActivity.this.finish();
						}
					});
		}
		catch (IOException e)
		{
			Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
		}

	}
}
