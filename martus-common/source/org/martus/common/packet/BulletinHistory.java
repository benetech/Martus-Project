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

package org.martus.common.packet;

import java.io.IOException;
import java.util.Vector;

import org.martus.common.MartusXml;
import org.martus.common.XmlWriterFilter;


public class BulletinHistory
{
	public BulletinHistory()
	{
		localIds = new Vector();
	}
	
	public BulletinHistory(Vector localIds)
	{
		this();
		for(int i=0; i < localIds.size(); ++i)
			add((String)localIds.get(i));
	}
	
	public BulletinHistory(BulletinHistory pullFrom)
	{
		this(pullFrom.localIds);
	}
	
	public static BulletinHistory createFromHistoryString(String localIdsAsString)
	{
		BulletinHistory history = new BulletinHistory();
		String[] localIds = localIdsAsString.split(" ");
		for(int i=0; i < localIds.length; ++i)
		{
			if(localIds[i].length() > 0)
				history.add(localIds[i]);
		}
		return history;
	}

	public int size()
	{
		return localIds.size();
	}
	
	public void add(String localId)
	{
		localIds.add(localId);
	}
	
	public String get(int index)
	{
		return (String)localIds.get(index);
	}
	
	public boolean contains(String localId)
	{
		return localIds.contains(localId);
	}
	
	public boolean equals(Object other)
	{
		throw new RuntimeException("Equals not supported!");
	}
	
	public String toString()
	{
		StringBuffer localIdsString = new StringBuffer();
		for(int i = 0; i < size(); ++i)
			localIdsString.append(get(i) + " ");
		return new String(localIdsString);
	}

	Vector localIds;

	public void internalWriteXml(XmlWriterFilter dest) throws IOException
	{
		dest.writeStartTag(MartusXml.HistoryElementName);
		for(int i=0; i < size(); ++i)
		{
			dest.writeStartTag(MartusXml.AncestorElementName);
			dest.writeDirect(get(i));
			dest.writeEndTag(MartusXml.AncestorElementName);
		}
		dest.writeEndTag(MartusXml.HistoryElementName);
	}
}
