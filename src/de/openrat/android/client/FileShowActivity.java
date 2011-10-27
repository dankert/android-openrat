/**
 * 
 */
package de.openrat.android.client;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import de.openrat.android.client.util.OpenRatClientAsyncTask;
import de.openrat.client.OpenRatClient;

/**
 * @author dankert
 * 
 */
public class FileShowActivity extends Activity
{
	public static final String ID = "id";
	public static final String CLIENT = "client";
	private String objectid;
	private OpenRatClient client;

	byte[] data;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		client = (OpenRatClient) getIntent().getSerializableExtra(CLIENT);

		new OpenRatClientAsyncTask(this, R.string.waitingforcontent)
		{

			@Override
			protected void callServer() throws IOException
			{
				objectid = getIntent().getStringExtra(ID);
				data = client.getFileContent(objectid);
			}

			protected void doOnSuccess()
			{
				Bitmap imageBitmap = BitmapFactory.decodeByteArray(data,0,data.length);
				setContentView(R.layout.show_image);
				ImageView image = (ImageView) findViewById(R.id.image);
				image.setImageBitmap(imageBitmap);
			}

		}.execute();
	}

}
