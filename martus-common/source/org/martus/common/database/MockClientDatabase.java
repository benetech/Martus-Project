/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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

package org.martus.common.database;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.martus.common.MartusUtilities;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;

public class MockClientDatabase extends MockDatabase
{
	public boolean mustEncryptLocalData()
	{
		return true;
	}
	public void verifyAccountMap() throws MartusUtilities.FileVerificationException, MissingAccountMapSignatureException
	{
	}
	

	public void deleteAllData()
	{
		packetMap = new TreeMap();
		timestampMap = new TreeMap();
		super.deleteAllData();
	}

	synchronized void addKeyToMap(DatabaseKey key, byte[] record)
	{
		DatabaseKey newKey = DatabaseKey.createLegacyKey(key.getUniversalId());
		packetMap.put(newKey, record);
		timestampMap.put(newKey, new Date().getTime());
	}

	synchronized byte[] readRawRecord(DatabaseKey key)
	{
		DatabaseKey newKey = DatabaseKey.createLegacyKey(key.getUniversalId());
		return (byte[])packetMap.get(newKey);
	}

	synchronized void internalDiscardRecord(DatabaseKey key)
	{
		DatabaseKey newKey = DatabaseKey.createLegacyKey(key.getUniversalId());
		packetMap.remove(newKey);
	}

	Map getPacketMapFor(DatabaseKey key)
	{
		return packetMap;
	}

	public synchronized Set internalGetAllKeys()
	{
		Set keys = new HashSet();
		keys.addAll(packetMap.keySet());
		return keys;
	}

	public File getInterimDirectory(String accountId) throws IOException
	{
		// NOTE: return null to force usage of system default temp directory
		return null;
	}
	
	public long getPacketTimestamp(DatabaseKey key) throws IOException, RecordHiddenException
	{
		Long timestamp = (Long)timestampMap.get(key);
		if(timestamp == null)
			return 0;
		return timestamp.longValue();
	}

	Map packetMap;
	Map timestampMap;
}
