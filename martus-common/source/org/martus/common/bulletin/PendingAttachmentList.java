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

package org.martus.common.bulletin;

import java.util.Vector;

import org.martus.common.packet.AttachmentPacket;


public class PendingAttachmentList
{
	public PendingAttachmentList()
	{
		data = new Vector();
	}
	
	public int size()
	{
		return data.size();
	}
	
	public AttachmentPacket get(int index)
	{
		return (AttachmentPacket)data.get(index);
	}
	
	public void add(AttachmentPacket packet)
	{
		data.add(packet);
	}
	
	public void addAll(PendingAttachmentList other)
	{
		for(int i=0; i < other.size(); ++i)
			add(other.get(i));
	}
	
	public void clear()
	{
		data.clear();
	}
	
	Vector data;
}
