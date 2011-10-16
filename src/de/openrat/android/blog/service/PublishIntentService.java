/**
 * 
 */
package de.openrat.android.blog.service;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import de.openrat.android.blog.FolderActivity;
import de.openrat.android.blog.R;
import de.openrat.client.CMSRequest;

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
		final CMSRequest request = (CMSRequest) intent
				.getSerializableExtra(EXTRA_REQUEST);

		String type = intent.getStringExtra(EXTRA_TYPE);
		String id = intent.getStringExtra(EXTRA_ID);
		String name = intent.getStringExtra(EXTRA_NAME);
		
		request.clearParameters();
		request.setAction(type );
		request.setActionMethod("pub");
		request.setId(id);

		// Erstmal alles aktivieren was geht
		// TODO: Abfrage der gewünschten Einstellungen über AlertDialog.
		request.setParameter("subdirs", "1");
		request.setParameter("pages", "1");
		request.setParameter("files", "1");
		String response = null;

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Intent notificationIntent = new Intent(this,
				FolderActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this,
				0, notificationIntent, 0);

		// the next two lines initialize the Notification, using the
		// configurations above
		Notification notification = new Notification(
				R.drawable.publish, getResources().getString(
						R.string.publish), System.currentTimeMillis());
		notification.setLatestEventInfo(getApplicationContext(),
				getResources().getString(R.string.publish), name,
				contentIntent);
		notification.flags = Notification.FLAG_ONGOING_EVENT
				| Notification.FLAG_NO_CLEAR;

		nm.notify(NOTIFICATION_PUBLISH, notification);

		try
		{
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
			}
			
			response = request.performRequest();
			JSONObject json = new JSONObject(response);

			notification.setLatestEventInfo(getApplicationContext(),
					getResources().getString(R.string.publish_ok),
					name, contentIntent);
			notification.flags = 0;
			nm.notify(NOTIFICATION_PUBLISH, notification);
		}
		catch (IOException e)
		{
			System.err.println(response);
			System.err.println(e.getMessage());
			
			notification.setLatestEventInfo(getApplicationContext(),
					getResources().getString(R.string.publish_fail),
					name, contentIntent);
			notification.flags = 0;
			nm.notify(NOTIFICATION_PUBLISH, notification);
		}
		catch (JSONException e)
		{
			notification.setLatestEventInfo(getApplicationContext(),
					getResources().getString(R.string.publish_fail),
					name, contentIntent);
			notification.flags = 0;
			nm.notify(NOTIFICATION_PUBLISH, notification);
			e.printStackTrace();
		}
		finally
		{
		}

	}

}
