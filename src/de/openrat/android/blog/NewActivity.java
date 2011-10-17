package de.openrat.android.blog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
		if (menuid == R.id.menu_newpage)
		{
			radioGroupTemplates.setVisibility(View.VISIBLE);

			RadioButton radioButton = new RadioButton(this);
			radioButton.setText("test");
			radioGroupTemplates.addView(radioButton);

		}
		else
		{
			radioGroupTemplates.setVisibility(View.INVISIBLE);
		}

		final Button button = (Button) findViewById(R.id.button_save);
		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				
			}
		});
	}
}
