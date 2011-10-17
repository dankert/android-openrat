/**
 * 
 */
package de.openrat.android.blog.service;

import java.io.File;
import java.io.IOException;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.openrat.android.blog.FolderActivity;
import de.openrat.android.blog.R;
import de.openrat.client.OpenRatClient;

/**
 * @author dankert
 * 
 */
public class UploadIntentService extends IntentService
{

	public static final String EXTRA_REQUEST = "request";
	public static final String EXTRA_FILENAME = "file";
	private static final int NOTIFICATION_UPLOAD = 1;

	public UploadIntentService()
	{
		super("UploadIntentService");
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent)
	{
		final String filePath = intent.getStringExtra(EXTRA_FILENAME);
		final OpenRatClient client = (OpenRatClient) intent
				.getSerializableExtra(EXTRA_REQUEST);

		final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		final Intent notificationIntent = new Intent(this, FolderActivity.class);
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		final Notification notification = new Notification(R.drawable.logo,
				getResources().getString(R.string.upload), System
						.currentTimeMillis());
		notification.setLatestEventInfo(getApplicationContext(), getResources()
				.getString(R.string.upload_ok), filePath, contentIntent);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		nm.notify(NOTIFICATION_UPLOAD, notification);
		
		try
		{
			final File file = new File(filePath);

			client.uploadFile(EXTRA_FILENAME,file);

			// Alles OK.
			final String msgText = getResources().getString(R.string.upload_ok);
			notification.setLatestEventInfo(getApplicationContext(),
					msgText, file
							.getName(), contentIntent);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			nm.notify(NOTIFICATION_UPLOAD, notification);
			Log.d(this.getClass().getName(), msgText);
		}
		catch (IOException e)
		{
			// Fehler ist aufgetreten.
			final String msgText = getResources().getString(R.string.upload_fail);
			notification.setLatestEventInfo(getApplicationContext(),
					msgText, e.getMessage(),
					contentIntent);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			nm.notify(NOTIFICATION_UPLOAD, notification);

			Log.e(this.getClass().getName(), msgText, e);
		}
		finally
		{
		}
	}

}
