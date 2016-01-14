/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.client.network;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.martus.common.packet.UniversalId;

public class RetrieveCommand
{
	public static class DataVersionException extends Exception
	{
		DataVersionException(int actual)
		{
			super("Data version expected " + RetrieveCommand.DATA_VERSION + " but was " + actual);
		}
	}
	public static class OlderDataVersionException extends DataVersionException 
	{
		OlderDataVersionException(int foundVersion)
		{
			super(foundVersion);
		}
	}
	
	public static class NewerDataVersionException extends DataVersionException
	{
		NewerDataVersionException(int foundVersion)
		{
			super(foundVersion);
		}
	}
	
	public RetrieveCommand()
	{
		this(NO_FOLDER, new Vector());
	}
	
	public RetrieveCommand(String destinationFolderName, Collection uidsToRetrieve)
	{
		folderName = destinationFolderName;
		uidsRemainingToRetrieve = new Vector();
		uidsRemainingToRetrieve.addAll(uidsToRetrieve);
		uidsRetrieved = new Vector();
	}
	
	public RetrieveCommand(JSONObject createFrom) throws DataVersionException
	{
		String typeString = createFrom.getString(TAG_JSON_TYPE);
		if(!TYPE_RETRIEVE_COMMAND.equals(typeString))
			throw new RuntimeException("JSON type expected " + TYPE_RETRIEVE_COMMAND + " but was " + typeString);
	
		int version = createFrom.getInt(TAG_DATA_VERSION);
		if(version < getDataVersion())
			throw new OlderDataVersionException(version);
		else if(version > getDataVersion())
			throw new NewerDataVersionException(version);
		
		folderName = createFrom.getString(TAG_FOLDER_NAME);
		uidsRemainingToRetrieve = extractUidsFromJsonObject(createFrom.getJSONObject(TAG_TO_RETRIEVE));
		uidsRetrieved = extractUidsFromJsonObject(createFrom.getJSONObject(TAG_RETRIEVED));
	}

	public String getFolderName()
	{
		return folderName;
	}
	
	public int getRemainingToRetrieveCount()
	{
		return uidsRemainingToRetrieve.size();
	}
	
	public int getRetrievedCount()
	{
		return uidsRetrieved.size();
	}
	
	public int getTotalCount()
	{
		return getRemainingToRetrieveCount() + getRetrievedCount();
	}
	
	public UniversalId getNextToRetrieve()
	{
		return (UniversalId)uidsRemainingToRetrieve.get(0);
	}
	
	public void markAsRetrieved(UniversalId uid)
	{
		if(!uidsRemainingToRetrieve.remove(uid))
			throw new RuntimeException("Attempted to remove a uid not queued for retrieve");
		uidsRetrieved.add(uid);
	}
	
	public int getDataVersion()
	{
		return DATA_VERSION;
	}
	
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		json.put(TAG_JSON_TYPE, TYPE_RETRIEVE_COMMAND);
		json.put(TAG_DATA_VERSION, getDataVersion());

		json.put(TAG_FOLDER_NAME, folderName);
		json.put(TAG_TO_RETRIEVE, createJsonObject(uidsRemainingToRetrieve));
		json.put(TAG_RETRIEVED, createJsonObject(uidsRetrieved));
		
		return json;
	}

	private JSONObject createJsonObject(final Vector vector)
	{
		JSONObject bulletinIds = new JSONObject();
		for(int i = 0; i < vector.size(); ++i)
		{
			UniversalId uid = (UniversalId)vector.get(i);
			
			String accountId = uid.getAccountId();
			JSONArray localIds = bulletinIds.optJSONArray(accountId);
			if(localIds == null)
				localIds = new JSONArray();
			
			localIds.put(uid.getLocalId());
			
			bulletinIds.put(accountId, localIds);
		}
		return bulletinIds;
	}
	
	private Vector extractUidsFromJsonObject(JSONObject toRetrieve)
	{
		Vector vector = new Vector();
		Iterator iter = toRetrieve.keys();
		while(iter.hasNext())
		{
			String accountId = (String)iter.next();
			JSONArray localIds = toRetrieve.getJSONArray(accountId);
			for(int i = 0; i < localIds.length(); ++i)
			{
				String localId = (String)localIds.get(i);
				UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);
				vector.add(uid);
			}
		}
		return vector;
	}
	
	private static final String NO_FOLDER = "";
	
	public static final String TAG_JSON_TYPE = "_Type";
	public static final String TAG_DATA_VERSION = "_DataVersion";
	
	private static final String TAG_FOLDER_NAME = "FolderName";
	private static final String TAG_TO_RETRIEVE = "ToRetrieve";
	private static final String TAG_RETRIEVED = "Retrieved";
	
	static final String TYPE_RETRIEVE_COMMAND = "MartusRetrieveCommand";
	public static final int DATA_VERSION = 1;

	String folderName;
	Vector uidsRemainingToRetrieve;
	Vector uidsRetrieved;
}
