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

package org.martus.server.forclients;

import java.util.Vector;

import org.martus.common.BulletinSummary;
import org.martus.common.LoggerInterface;
import org.martus.common.MartusUtilities;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.server.main.MartusServer;


public abstract class SummaryCollector implements Database.PacketVisitor
{
	protected SummaryCollector(MartusServer serverToUse, String authorAccountToUse, Vector retrieveTagsToUse)
	{
		server = serverToUse;
		authorAccountId = authorAccountToUse;
		retrieveTags = retrieveTagsToUse;
		
		summaries = new Vector();
	}
	
	public ReadableDatabase getDatabase()
	{
		return server.getDatabase();
	}
	
	public void visit(DatabaseKey key)
	{
		if(!BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
			return;
		
		if(!MartusServer.keyBelongsToClient(key, authorAccountId))
			return;
		
		if(!isWanted(key))
			return;
		
		try
		{
			BulletinHeaderPacket bhp = server.loadBulletinHeaderPacket(getDatabase(), key);
			if(!isAuthorized(bhp))
				return;
			
			String summary = extractSummary(bhp, getDatabase(), retrieveTags, server.getLogger());
			summaries.add(summary);
		}
		catch (Exception e)
		{
			server.logError("in summary collector: " + getClass().getName(), e);
		}
	}
	
	abstract public String callerAccountId();
	abstract public boolean isWanted(DatabaseKey key);
	abstract public boolean isAuthorized(BulletinHeaderPacket bhp);
	
	public Vector collectSummaries()
	{
		server.getStore().visitAllBulletinsForAccount(this, authorAccountId);
		return summaries;	
	}
	
	public static String extractSummary(BulletinHeaderPacket bhp, ReadableDatabase db, Vector tags, LoggerInterface logger)
	{
		String summary = bhp.getLocalId() + BulletinSummary.fieldDelimeter;
		summary  += bhp.getFieldDataPacketId();
		for(int t=0; t < tags.size(); ++t)
		{
			String tag = (String)tags.get(t);
			if(tag.equals(NetworkInterfaceConstants.TAG_BULLETIN_SIZE))
			{
				int size = MartusUtilities.getBulletinSize(db, bhp);
				summary += BulletinSummary.fieldDelimeter + size;
			}
			else if(tag.equals(NetworkInterfaceConstants.TAG_BULLETIN_DATE_SAVED))
			{
				summary += BulletinSummary.fieldDelimeter + bhp.getLastSavedTime();
			}
			else if(tag.equals(NetworkInterfaceConstants.TAG_BULLETIN_HISTORY))
			{
				BulletinHistory history = bhp.getHistory();
				if(history.size() > 0)
				{
					summary += BulletinSummary.fieldDelimeter + history.toString();
				}
			}
			else
				logger.logWarning("requested unknown summary tag: " + tag);
		}
		return summary;
	}

	private MartusServer server;
	protected String authorAccountId;
	private Vector summaries;
	private Vector retrieveTags;
}