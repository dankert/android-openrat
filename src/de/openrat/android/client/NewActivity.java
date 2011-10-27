package de.openrat.android.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import de.openrat.android.client.util.OpenRatClientAsyncTask;
import de.openrat.client.OpenRatClient;

public class NewActivity extends Activity
{

	public static final String EXTRA_CLIENT = "request";
	public static final String EXTRA_MENUID = "menuid";
	public static final String EXTRA_FOLDERID = "folderid";

	private OpenRatClient request;
	private int menuid;
	private Map<String, String> templates;
	private String folderid;

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		request = (OpenRatClient) getIntent()
				.getSerializableExtra(EXTRA_CLIENT);
		menuid = getIntent().getIntExtra(EXTRA_MENUID, 0);
		folderid = getIntent().getStringExtra(EXTRA_FOLDERID);

		setContentView(R.layout.new1);

		final EditText editText = (EditText) findViewById(R.id.newname);
		final Spinner spinner = (Spinner) findViewById(R.id.spinner);
		if (menuid == R.id.menu_newpage)
		{
			spinner.setVisibility(View.VISIBLE);

			new OpenRatClientAsyncTask(this, R.string.waitingforcontent)
			{

				@Override
				protected void callServer() throws IOException
				{
					templates = request.getTemplates();
				}

				@Override
				protected void doOnSuccess()
				{
					final List<String> valueList = new ArrayList<String>(
							templates.values());
					ArrayAdapter adapter = new ArrayAdapter(NewActivity.this,
							android.R.layout.simple_spinner_item, valueList);
					adapter
							.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spinner.setAdapter(adapter);
				}
			}.execute();
		}
		else
		{
			spinner.setVisibility(View.INVISIBLE);
		}

		final Button button = (Button) findViewById(R.id.button_save);
		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				new OpenRatClientAsyncTask(NewActivity.this,
						R.string.waitingforcontent)
				{

					@Override
					protected void callServer() throws IOException
					{
						if (menuid == R.id.menu_newfolder)
						{
							request.createFolder(folderid, editText.getText()
									.toString());
						}
						if (menuid == R.id.menu_newpage)
						{

							int pos = spinner.getSelectedItemPosition();
							final String templateid = NewActivity.this.templates
									.keySet().toArray(new String[] {})[pos];
							request.createPage(folderid, editText.getText()
									.toString(), templateid);
						}
					}

					@Override
					protected void doOnSuccess()
					{
						NewActivity.this.finish();
					}
				}.execute();
			}
		});
	}
}
