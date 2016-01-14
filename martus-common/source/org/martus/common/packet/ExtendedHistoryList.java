/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2010, Beneficent
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
package org.martus.common.packet;

import java.io.IOException;
import java.util.Vector;

import org.martus.common.MartusXml;
import org.martus.common.XmlWriterFilter;

public class ExtendedHistoryList
{
	public ExtendedHistoryList()
	{
		histories = new Vector();
	}
	
	public ExtendedHistoryList(ExtendedHistoryList extendedHistory)
	{
		this();
		histories.addAll(extendedHistory.getHistories());
	}

	public Vector getHistories()
	{
		return histories;
	}

	public void add(String accountId, BulletinHistory history)
	{
		ExtendedHistoryEntry newHistory = new ExtendedHistoryEntry(accountId, history);
		histories.add(newHistory);
	}

	public int size()
	{
		return histories.size();
	}

	public ExtendedHistoryEntry getHistory(int i)
	{
		return (ExtendedHistoryEntry)histories.get(i);
	}
	
	public void internalWriteXml(XmlWriterFilter dest) throws IOException
	{
		dest.writeStartTag(MartusXml.ExtendedHistorySectionName);
		for(int i = 0; i < size(); ++i)
		{
			dest.writeStartTag(MartusXml.ExtendedHistoryEntryName);

			ExtendedHistoryEntry entry = getHistory(i);
			dest.writeStartTag(MartusXml.ExtendedHistoryClonedFromAccountName);
			dest.writeEncoded(entry.getClonedFromAccountId());
			dest.writeEndTag(MartusXml.ExtendedHistoryClonedFromAccountName);
			entry.getClonedHistory().internalWriteXml(dest);
			
			dest.writeEndTag(MartusXml.ExtendedHistoryEntryName);
		}
		dest.writeEndTag(MartusXml.ExtendedHistorySectionName);
	}

	public boolean equals(Object arg0)
	{
		throw new RuntimeException("ExtendedHistoryList.equals not supported");
	}

	private Vector histories;

}
