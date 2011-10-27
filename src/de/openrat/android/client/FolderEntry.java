package de.openrat.android.client;

public class FolderEntry
{
	public FType type;
	public String id;
	public String name;
	public String description;

	public enum FType
	{
		FOLDER, PAGE, FILE, LINK, PROJECT;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "FolderEntry [id=" + id + ", name=" + name + ", type=" + type
				+ "]";
	}

}
