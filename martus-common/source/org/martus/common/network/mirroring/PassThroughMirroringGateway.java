/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2013, Beneficent
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
package org.martus.common.network.mirroring;

import java.util.Vector;

import org.martus.common.MartusLogger;

public class PassThroughMirroringGateway implements	CallerSideMirroringInterface
{
	public PassThroughMirroringGateway(SupplierSideMirroringInterface realHandler)
	{
		handler = realHandler;
	}
	
	public Vector request(String callerAccountId, Vector parameters, String signature)
	{
		parameters = recursivelyConvertVectorsToArrays(parameters);
		Vector result = handler.request(callerAccountId, parameters, signature);
		result = recursivelyConvertVectorsToArrays(result);
		return result;
	}

	private Vector recursivelyConvertVectorsToArrays(Vector vectorToConvert)
	{
		Vector withArraysInsteadOfVectors = new Vector();
		for (Object object : vectorToConvert)
		{
			if (object instanceof Vector)
			{
				MartusLogger.log("Converting vector");
				Vector childVector = (Vector)object;
				childVector = recursivelyConvertVectorsToArrays(childVector);
				object = childVector.toArray();
			}
			else if(object instanceof Object[])
			{
				MartusLogger.log("Converting array");
				Object[] childArray = (Object[])object;
				recursivelyConvertVectorsToArrays(childArray);
			}
			withArraysInsteadOfVectors.add(object);
		}
		
		return withArraysInsteadOfVectors;
	}

	private void recursivelyConvertVectorsToArrays(Object[] arrayToConvert)
	{
		for(int i = 0; i < arrayToConvert.length; ++i)
		{
			Object object = arrayToConvert[i];
			if (object instanceof Vector)
			{
				MartusLogger.log("Converting vector");
				Vector childVector = (Vector)object;
				childVector = recursivelyConvertVectorsToArrays(childVector);
				object = childVector.toArray();
			}
			else if(object instanceof Object[])
			{
				MartusLogger.log("Converting array");
				Object[] childArray = (Object[])object;
				recursivelyConvertVectorsToArrays(childArray);
			}
			
			arrayToConvert[i] = object;
		}
	}

	private SupplierSideMirroringInterface handler;
}
