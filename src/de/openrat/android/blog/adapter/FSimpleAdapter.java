/**
 * 
 */
package de.openrat.android.blog.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.widget.SimpleAdapter;

/**
 * @author dankert
 *
 */
public class FSimpleAdapter extends SimpleAdapter
{

	public FSimpleAdapter(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to)
	{
		super(context, data, resource, from, to);
	}
	
	

}
