package de.openrat.android.blog;


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

}
