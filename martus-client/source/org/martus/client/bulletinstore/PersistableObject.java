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

public abstract class PersistableObject 
{
	public static class UnknownTypeException extends IOException
	{
		
	}
	
	public abstract int getType();
	abstract void internalWriteTo(DataOutputStream dataOut) throws IOException;
	
	public static PersistableObject createFrom(DataInputStream dataIn) throws IOException
	{
		int type = dataIn.readInt();
		switch(type)
		{
			case TYPE_INT: return new PersistableInt(dataIn);
			case TYPE_STRING: return new PersistableString(dataIn);
			case TYPE_VECTOR: return new PersistableVector(dataIn);
			case TYPE_MAP: return new PersistableMap(dataIn);
			default: throw new UnknownTypeException();
		}
	}
	
	public void writeTo(DataOutputStream dataOut) throws IOException
	{
		dataOut.writeInt(getType());
		internalWriteTo(dataOut);
	}
	
	static final int TYPE_INT = 1;
	static final int TYPE_STRING = 2;
	static final int TYPE_VECTOR = 3;
	static final int TYPE_MAP = 4;
}
