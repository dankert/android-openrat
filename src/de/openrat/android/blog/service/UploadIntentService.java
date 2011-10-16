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
import de.openrat.android.blog.FolderActivity;
import de.openrat.android.blog.R;
import de.openrat.android.blog.util.FileUtils;
import de.openrat.client.CMSRequest;

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
		final CMSRequest request = (CMSRequest) intent
				.getSerializableExtra(EXTRA_REQUEST);

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Intent notificationIntent = new Intent(this, FolderActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		request.clearParameters();
		request.setAction("folder");
		request.setActionMethod("createnewfile");
		request.setMethod("POST");
		request.trace = true;

		final Notification notification = new Notification(R.drawable.upload,
				getResources().getString(R.string.upload), System
						.currentTimeMillis());
		notification.setLatestEventInfo(getApplicationContext(), getResources()
				.getString(R.string.upload_ok), filePath, contentIntent);

		nm.notify(NOTIFICATION_UPLOAD, notification);
		try
		{
			final File file = new File(filePath);

			notification.setLatestEventInfo(getApplicationContext(),
					getResources().getString(R.string.upload), file.getName(),
					contentIntent);
			notification.flags |= Notification.FLAG_NO_CLEAR;

			byte[] fileBytes = FileUtils.getBytesFromFile(file);
			request.setFile(EXTRA_FILENAME, fileBytes, file.getName(),
					"image/jpeg", "binary");

			String response = request.performRequest();

			System.out.println("nach dem Hochladen" + response);

			notification.setLatestEventInfo(getApplicationContext(),
					getResources().getString(R.string.upload_ok), file
							.getName(), contentIntent);
			notification.flags = 0;
			nm.notify(NOTIFICATION_UPLOAD, notification);
		}
		catch (IOException e)
		{
			notification.setLatestEventInfo(getApplicationContext(),
					getResources().getString(R.string.upload_fail), "",
					contentIntent);
			notification.flags = 0;
			nm.notify(NOTIFICATION_UPLOAD, notification);

			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally
		{
		}
	}

}
