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
import de.openrat.android.client.FolderEntry;

/**
 * @author dankert
 * 
 */
public class FolderContentAdapter extends BaseAdapter
{

	/** Remember our context so we can use it when constructing views. */
	private Context mContext;

	/**
	 * Hold onto a copy of the entire Contact List.
	 */
	private List<FolderEntry> data = new ArrayList<FolderEntry>();

	private LayoutInflater inflator;

	public FolderContentAdapter(Context context, List<FolderEntry> data2)
	{
		mContext = context;
		inflator = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		data = data2;
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

		FolderEntry e = data.get(position);

		final View view = inflator.inflate(R.layout.listing_entry, null);

		ImageView image = (ImageView) view.findViewById(R.id.listentry_image);
		switch (e.type)
		{
		case FOLDER:
			image.setImageResource(R.drawable.icon_folder);
			break;
		case FILE:
			image.setImageResource(R.drawable.icon_file);
			break;
		case PAGE:
			image.setImageResource(R.drawable.icon_page);
			break;
		case LINK:
			image.setImageResource(R.drawable.icon_link);
			break;
		case PROJECT:
			image.setImageResource(R.drawable.icon_project);
			break;
		default:
		}

		TextView name = (TextView) view.findViewById(R.id.listentry_name);
		name.setText(e.name);

		TextView desc = (TextView) view
				.findViewById(R.id.listentry_description);
		desc.setText(e.description);

		return view;
	}

}
