/**
 * 
 */
package de.openrat.android.client.util;

import java.io.IOException;
import java.net.SocketTimeoutException;

import de.openrat.android.blog.R;

/**
 * @author dankert
 *
 */
public class ExceptionUtils
{

	public static int getResourceStringId( Throwable throwable) {
		
		if	( throwable instanceof SocketTimeoutException )
			return R.string.error_timeout;
		else if	( throwable instanceof IOException )
			return R.string.error_io;
		else 
			return R.string.error_io;
	}
}
