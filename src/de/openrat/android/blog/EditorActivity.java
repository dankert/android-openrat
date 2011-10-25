package de.openrat.android.blog;

import java.io.IOException;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import de.openrat.android.blog.util.OpenRatClientAsyncTask;
import de.openrat.client.OpenRatClient;

public class EditorActivity extends Activity
{
	public final static String ELEMENTID = "elementid";
	public final static String OBJECTID = "objectid";
	// public final static String TYPE = "type";
	public static final String CLIENT = "client";
	private OpenRatClient client;
	private String objectid;
	private String elementid;
	private String type;
	private Map<String, String> properties;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.client = (OpenRatClient) getIntent().getSerializableExtra(CLIENT);
		this.objectid = getIntent().getStringExtra(OBJECTID);
		this.elementid = getIntent().getStringExtra(ELEMENTID);

		new OpenRatClientAsyncTask(this, R.string.waitingforcontent)
		{

			@Override
			protected void callServer() throws IOException
			{
				properties = client.getValue(objectid, elementid);
				type = properties.get("type");
			}

			protected void doOnSuccess()
			{
				if (type.equals("longtext"))
				{
					setContentView(R.layout.editor);
					final EditText view = (EditText) findViewById(R.id.text);
					view.setText(properties.get("text"));

					Button button = (Button) findViewById(R.id.save);
					button.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
						{
							final String text = view.getEditableText()
									.toString();
							final CheckBox releaseBox = (CheckBox) findViewById(R.id.release);
							
							new OpenRatClientAsyncTask(EditorActivity.this,
									R.string.waitingforsave)
							{

								@Override
								protected void callServer() throws IOException
								{
									client.setValue(objectid, elementid,
											"longtext", text, releaseBox.isChecked(), false);
								}

								protected void doOnSuccess()
								{
									Toast.makeText(EditorActivity.this,
											R.string.saved, Toast.LENGTH_SHORT);
									EditorActivity.this.finish();
								};
							}.execute();

						}

					});
				}
			}
		}.execute();
	}
}
