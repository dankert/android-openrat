/*
 * Openrat CMS-Client for Android
 * 
 * Copyright (C) 2011 Jan Dankert
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.openrat.android.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.text.TextUtils;

public class ServerList
{

	private List<String> serverList;

	public ServerList(String plainList)
	{
		if (plainList.length() == 0)
		{
			this.serverList = new ArrayList<String>();
		}
		else
		{
			serverList = new ArrayList<String>(Arrays.asList(TextUtils.split(
					plainList, ",")));
		}
	}

	public ServerList addServer(String name)
	{
		this.serverList.add(name);
		return this;
	}

	public ServerList removeServer(String name)
	{
		this.serverList.remove(name);
		return this;
	}

	public String toPlain()
	{
		return TextUtils.join(",", serverList);
	}

}
