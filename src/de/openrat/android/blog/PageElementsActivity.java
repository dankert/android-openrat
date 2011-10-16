/**
 * 
 */
package de.openrat.android.blog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleAdapter;

/**
 * @author dankert
 * 
 */
public class PageElementsActivity extends ListActivity
{
	private static final String NAME = "name";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.listing);

		int[] to = new int[] { R.id.name };
		String[] from = new String[] { NAME };
		
		ArrayList<Map<String,?>> list = new ArrayList<Map<String,?>>();
		new SimpleAdapter(this,list,R.layout.listing_pageelement, from, to);

	}

}
