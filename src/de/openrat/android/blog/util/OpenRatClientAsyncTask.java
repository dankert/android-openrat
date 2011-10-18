/**
 * 
 */
package de.openrat.android.blog.util;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author dankert
 * 
 */
public abstract class OpenRatClientAsyncTask extends
		AsyncTask<Void, Void, Void>
{
	private ProgressDialog progressDialog;
	private Context context;
	private AlertDialog alertDialog;
	private IOException error;

	public OpenRatClientAsyncTask(Context context, int title, int message)
	{
		this.context = context;

		this.progressDialog = new ProgressDialog(context);
		progressDialog.setTitle(title);
		progressDialog.setMessage(context.getResources().getString(message));
	}

	@Override
	final protected void onPreExecute()
	{
		// dialog.setTitle(getResources().getString(R.string.loading));
		// dialog.setMessage(getResources().getString(
		// R.string.waitingforcontent));
		progressDialog.show();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	final protected void onPostExecute(Void result)
	{
		progressDialog.dismiss();

		if (error != null)
		{
			doOnError(error);
		}
		else
		{
			doOnSuccess();
		}
	}

	/**
	 * Wird aufgerufen, falls die Serveranfrage nicht durchgeführt werden
	 * konnte. Läuft im UI-Thread.
	 * 
	 * @param error
	 *            Exception, die aufgetreten ist.
	 */
	protected void doOnError(IOException error)
	{
		final Builder builder = new AlertDialog.Builder(this.context);
		alertDialog = builder.setCancelable(true).create();
		final int causeRId = ExceptionUtils.getResourceStringId(error);
		String msg = // this.context.getResources().getString(R.string.reason)
		// + ":\n\n" +
		error.getMessage();

		Throwable t = error;
		while (t.getCause() != null)
		{
			t = t.getCause();
			msg += ": " + t.getMessage();
		}

		alertDialog.setTitle(causeRId);
		alertDialog.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		alertDialog.setMessage(msg);
		alertDialog.show();

	}

	/**
	 * Wird aufgerufen, falls die Serveranfrage erfolgreich durchgeführt werden
	 * konnte. Läuft im UI-Thread.
	 */
	protected void doOnSuccess()
	{
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		try
		{
			callServer();
		}
		catch (IOException e)
		{
			Log.e(this.getClass().getName(), e.getMessage(), e);
			error = e;
		}

		return null;
	}

	/**
	 * @throws IOException
	 */
	protected abstract void callServer() throws IOException;
}
