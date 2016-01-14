/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.miradi.utils.EnhancedJsonObject;

public class SyncStatus
{
	public SyncStatus()
	{
		serverToTimestampMap = new HashMap<String, String>();
	}
	
	public SyncStatus(String jsonString) throws Exception
	{
		this();
		
		EnhancedJsonObject json = new EnhancedJsonObject(jsonString);
		EnhancedJsonObject serversToTimestamps = json.optJson(JSON_KEY_SERVERS_TO_TIMESTAMPS);
		Iterator it = serversToTimestamps.keys();
		while(it.hasNext())
		{
			String server = (String) it.next();
			String timestamp = serversToTimestamps.optString(server);
			serverToTimestampMap.put(server, timestamp);
		}
	}
	
	public void setServerTimestamp(String server, String timestamp)
	{
		serverToTimestampMap.put(server, timestamp);
	}
	
	public String getServerTimestamp(String server)
	{
		String timestamp = serverToTimestampMap.get(server);
		if(timestamp == null)
			return "";
		return timestamp;
	}
	
	public EnhancedJsonObject toJson()
	{
		EnhancedJsonObject serversToTimestamps = new EnhancedJsonObject();
		serverToTimestampMap.forEach((server, timestamp) -> serversToTimestamps.put(server, timestamp));
		
		EnhancedJsonObject json = new EnhancedJsonObject();
		json.put(JSON_KEY_SERVERS_TO_TIMESTAMPS, serversToTimestamps);
		
		return json;
	}
	
	public String toJsonString()
	{
		return toJson().toString();
	}
	
	private static final String JSON_KEY_SERVERS_TO_TIMESTAMPS = "ServersToTimestamps";

	private Map<String, String> serverToTimestampMap;
}
