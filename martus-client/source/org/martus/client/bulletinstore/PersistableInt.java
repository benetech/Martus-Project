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

package org.martus.client.bulletinstore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PersistableInt extends PersistableObject
{
	public PersistableInt(int intValue)
	{
		value = intValue;
	}
	
	public PersistableInt(Integer integerValue)
	{
		value = integerValue.intValue();
	}
	
	public PersistableInt(DataInputStream dataIn) throws IOException
	{
		value = dataIn.readInt();
	}
	
	public int getType()
	{
		return TYPE_INT;
	}
	
	public int asInt()
	{
		return value;
	}
	
	public boolean equals(Object rawOther)
	{
		if(! (rawOther instanceof PersistableInt))
			return false;
		
		PersistableInt other = (PersistableInt)rawOther;
		return (value == other.value);
	}
	
	public int hashCode()
	{
		return value;
	}

	void internalWriteTo(DataOutputStream dataOut) throws IOException
	{
		dataOut.writeInt(value);
	}
	
	private int value;
}
