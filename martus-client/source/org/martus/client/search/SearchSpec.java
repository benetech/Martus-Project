/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.client.search;

import org.json.JSONObject;

public class SearchSpec
{
	public SearchSpec(JSONObject searchGridToUse, boolean finalOnlyToUse, boolean sameRowsOnlyToUse)
	{
		searchGrid = searchGridToUse;
		finalOnly = finalOnlyToUse;
		sameRowsOnly = sameRowsOnlyToUse;
	}
	
	public SearchSpec(JSONObject json)
	{
		searchGrid = json.getJSONObject(TAG_SEARCH_GRID);
		finalOnly = json.getBoolean(TAG_FINAL_ONLY);
		sameRowsOnly = json.optBoolean(TAG_SAME_ROWS_ONLY);
	}
	
	public JSONObject getSearchGrid()
	{
		return searchGrid;
	}
	
	public boolean getFinalOnly()
	{
		return finalOnly;
	}
	
	public boolean getSameRowsOnly()
	{
		return sameRowsOnly;
	}
	
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		json.put(TAG_JSON_TYPE, JSON_TYPE);
		json.put(TAG_VERSION, VERSION);
		json.put(TAG_SEARCH_GRID, getSearchGrid());
		json.put(TAG_FINAL_ONLY, getFinalOnly());
		json.put(TAG_SAME_ROWS_ONLY, getSameRowsOnly());
		return json;
	}
	
	static final String TAG_JSON_TYPE = "JsonType";
	static final String TAG_VERSION = "Version";
	static final String TAG_SEARCH_GRID = "SearchGrid";
	static final String TAG_FINAL_ONLY = "FinalOnly";
	static final String TAG_SAME_ROWS_ONLY = "SameRowsOnly";
	
	static final String JSON_TYPE = "SearchSpec";
	static final int VERSION = 2;
	
	private JSONObject searchGrid;
	private boolean finalOnly;
	private boolean sameRowsOnly;
}