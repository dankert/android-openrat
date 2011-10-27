/**
 * 
 */
package de.openrat.android.blog.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.openrat.android.blog.R;

/**
 * @author dankert
 * 
 */
public class SimpleNameAdapter extends BaseAdapter
{

	/**
	 * Hold onto a copy of the entire Contact List.
	 */
	private List<String> data = new ArrayList<String>();

	private LayoutInflater inflator;

	private int imageRes;

	public SimpleNameAdapter(Context context, List<String> data, int imageRes)
	{
		inflator = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.data = data;
		this.imageRes = imageRes;
	}

	public int getCount()
	{
		return data.size();
	}

	public Object getItem(int position)
	{
		return data.get(position);
	}

	/** Use the array index as a unique id. */
	public long getItemId(int position)
	{
		return position;

	}

	/**
	 * @param convertView
	 *            The old view to overwrite, if one is passed
	 * @returns a ContactEntryView that holds wraps around an ContactEntry
	 */
	public View getView(int position, View convertView, ViewGroup parent)
	{

		final View view = inflator.inflate(R.layout.listing_entry, null);
		
		final ImageView image = (ImageView) view
				.findViewById(R.id.listentry_image);
		image.setImageResource(imageRes);

		final TextView name = (TextView) view.findViewById(R.id.listentry_name);
		name.setText(data.get(position));

		final TextView desc = (TextView) view
				.findViewById(R.id.listentry_description);
		desc.setText("");

		return view;
	}

}
