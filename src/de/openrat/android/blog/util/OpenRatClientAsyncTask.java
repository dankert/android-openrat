/**
 * 
 */
package de.openrat.android.blog.util;

import java.io.IOException;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import de.openrat.android.blog.FolderEntry;

/**
 * @author dankert
 * 
 */
public abstract class OpenRatClientAsyncTask extends
		AsyncTask<Void, Void, Void>
{
	private ProgressDialog dialog;
	private Context context;

	public OpenRatClientAsyncTask(Context context, ProgressDialog dialog)
	{
		this.dialog = dialog;
		this.context = context;
	}

	public OpenRatClientAsyncTask(Context context, int title, CharSequence message)
	{
		this.dialog = new ProgressDialog(context);
		dialog.setTitle(title);
		dialog.setMessage(message);
		this.context = context;
	}
	
	public OpenRatClientAsyncTask(Context context, int title, int message)
	{
		this.dialog = new ProgressDialog(context);
		dialog.setTitle(title);
		dialog.setMessage(context.getResources().getString(message));
		this.context = context;
	}

	@Override
	protected void onPreExecute()
	{
		// dialog.setTitle(getResources().getString(R.string.loading));
		// dialog.setMessage(getResources().getString(
		// R.string.waitingforcontent));
		dialog.show();
	}

	protected void onPostExecute(List<FolderEntry> result)
	{
		dialog.dismiss();

	};

	@Override
	protected Void doInBackground(Void... params)
	{
		try
		{
			callServer();
		}
		catch (IOException e)
		{
			dialog.dismiss();
			Log.e(this.getClass().getName(), e.getMessage(), e);

			doOnError(e);

		}

		return null;
	}

	/**
	 * @param e
	 */
	protected void doOnError(IOException e) {
		dialog.setMessage( e.getMessage() );
		dialog.show();
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e1)
		{
		}
		dialog.dismiss();
	}

	/**
	 * @throws IOException
	 */
	protected abstract void callServer() throws IOException;
}
