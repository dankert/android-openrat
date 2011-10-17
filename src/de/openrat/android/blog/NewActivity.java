package de.openrat.android.blog;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import de.openrat.client.CMSRequest;
import de.openrat.client.OpenRatClient;

public class NewActivity extends Activity
{

	private OpenRatClient request;
	private int menuid;

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		request = (OpenRatClient) getIntent().getSerializableExtra("request");
		menuid = getIntent().getIntExtra("menuid", 0);

		setContentView(R.layout.new1);

		final RadioGroup radioGroupTemplates = (RadioGroup) findViewById(R.id.RadioGroupTemplates);
		radioGroupTemplates.setEnabled(menuid == R.id.menu_newpage);

		RadioButton radioButton = new RadioButton(this);
		radioButton.setText("test");
		radioGroupTemplates.addView(radioButton);
	}
}
