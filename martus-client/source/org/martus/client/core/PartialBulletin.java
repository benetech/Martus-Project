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
package org.martus.client.core;

import java.util.HashMap;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.packet.UniversalId;

public class PartialBulletin
{
	public PartialBulletin(SafeReadableBulletin copyFrom, String[] tagsToStore)
	{
		fieldData = new HashMap();

		uid = copyFrom.getUniversalId();
		for(int i = 0; i < tagsToStore.length; ++i)
		{
			MartusField field = copyFrom.getPossiblyNestedField(tagsToStore[i]);
			if(field != null)
				fieldData.put(tagsToStore[i], field.getData());
			//System.out.println("PartialBulletin copying: " + field);
		}
	}
	
	public PartialBulletin(Bulletin copyFrom, String[] tagsToStore)
	{
		fieldData = new HashMap();

		uid = copyFrom.getUniversalId();
		for(int i = 0; i < tagsToStore.length; ++i)
		{
			MartusField field = copyFrom.getField(tagsToStore[i]);
			if(field != null)
				fieldData.put(tagsToStore[i], field.getData());
			//System.out.println("PartialBulletin copying: " + field);
		}
	}
	
	public PartialBulletin(SafeReadableBulletin copyFrom, MiniFieldSpec[] specsToStore)
	{
		fieldData = new HashMap();

		uid = copyFrom.getUniversalId();
		for(int i = 0; i < specsToStore.length; ++i)
		{
			MiniFieldSpec spec = specsToStore[i];
			MartusField field = copyFrom.getPossiblyNestedField(spec);
			if(field != null)
				fieldData.put(specsToStore[i].getTag(), field.getData());
			//System.out.println("PartialBulletin copying: " + field);
		}
	}
	
	public UniversalId getUniversalId()
	{
		return uid;
	}
	
	public String getData(String tag)
	{
		return (String)fieldData.get(tag);
	}
	
	public boolean equals(Object rawOther)
	{
		if(!(rawOther instanceof PartialBulletin))
			return false;
		
		PartialBulletin other = (PartialBulletin)rawOther;
		return (uid.equals(other.uid) && fieldData.equals(other.fieldData));
	}
	
	public int hashCode()
	{
		return uid.hashCode();
	}

	
	UniversalId uid;
	HashMap fieldData;
}
