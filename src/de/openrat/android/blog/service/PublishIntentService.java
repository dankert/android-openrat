/**
 * 
 */
package de.openrat.android.blog.service;

import java.io.IOException;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.openrat.android.blog.R;
import de.openrat.android.client.FolderActivity;
import de.openrat.client.OpenRatClient;

/**
 * @author dankert
 * 
 */
public class PublishIntentService extends IntentService
{

	public static final String EXTRA_REQUEST = "request";
	public static final String EXTRA_ID = "objectid";
	public static final String EXTRA_TYPE = "type";
	public static final String EXTRA_NAME = "name";
	private static final int NOTIFICATION_PUBLISH = 2;

	public PublishIntentService()
	{
		super("PublishIntentService");
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
		final OpenRatClient client = (OpenRatClient) intent
				.getSerializableExtra(EXTRA_REQUEST);

		String type = intent.getStringExtra(EXTRA_TYPE);
		String id = intent.getStringExtra(EXTRA_ID);
		String name = intent.getStringExtra(EXTRA_NAME);
		
		// Erstmal alles aktivieren was geht
		// TODO: Abfrage der gewünschten Einstellungen über AlertDialog.
		client.setParameter("subdirs", "1");
		client.setParameter("pages", "1");
		client.setParameter("files", "1");

		final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		final Intent notificationIntent = new Intent(this,
				FolderActivity.class);
		final PendingIntent contentIntent = PendingIntent.getActivity(this,
				0, notificationIntent, 0);

		final Notification notification = new Notification(
				R.drawable.logo, getResources().getString(
						R.string.publish_long), System.currentTimeMillis());
		notification.setLatestEventInfo(getApplicationContext(),
				getResources().getString(R.string.publish), name,
				contentIntent);
		notification.flags = Notification.FLAG_ONGOING_EVENT
				| Notification.FLAG_NO_CLEAR;
		nm.notify(NOTIFICATION_PUBLISH, notification);

		try
		{
			int old = client.setTimeout(3600000); // 1 Std.
			client.publish(type, id);
			client.setTimeout(old);

			// Alles OK.
			notification.setLatestEventInfo(getApplicationContext(),
					getResources().getString(R.string.publish_ok),
					name, contentIntent);
			notification.tickerText = getResources().getString(R.string.publish_ok_long);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			nm.notify(NOTIFICATION_PUBLISH, notification);
		}
		catch (IOException e)
		{
			final String msgText = getResources().getString(R.string.publish_fail);
			notification.tickerText = getResources().getString(R.string.publish_fail_long);
			notification.setLatestEventInfo(getApplicationContext(),
					msgText,
					name, contentIntent);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			nm.notify(NOTIFICATION_PUBLISH, notification);
			
			Log.e(this.getClass().getName(), msgText,e);
		}
		finally
		{
		}
	}

}
