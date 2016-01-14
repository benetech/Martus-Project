/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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
/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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

package org.martus.client.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.martus.common.FieldCollection;
import org.martus.common.FieldSpecCollection;
import org.martus.common.FieldCollection.CustomFieldsParseException;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;

public class CustomFieldsDuplicateLabelChecker
{
	public Vector getDuplicatedLabels(String topSectionXml, String bottomSectionXml)
	{
		try 
		{
			FieldSpecCollection allSpecs = mergeSections(topSectionXml, bottomSectionXml);
			return getDuplicatedLabels(allSpecs);
		} 
		catch (CustomFieldsParseException e) 
		{
		}
		return new Vector();
	}

	public Vector getDuplicatedLabels(FieldSpecCollection allSpecs)
	{
		Vector duplicateLabelsFound = new Vector();
		HashSet foundLabels = new HashSet();
		for (int i = 0; i < allSpecs.size(); i++)
		{
			FieldSpec thisSpec = allSpecs.get(i);
			String label = thisSpec.getLabel().trim();
			if(label.length() == 0)
				continue;
		
			if(foundLabels.contains(label))
				addToVectorIfNotAlreadyThere(duplicateLabelsFound, label);				
			foundLabels.add(label);
			
			if(!thisSpec.getType().isGrid())
				continue;

			Vector duplicatedGridLabels = getDuplicatedGridLabels((GridFieldSpec)thisSpec);
			addAllUniqueLabels(duplicateLabelsFound, duplicatedGridLabels);
		}
		return duplicateLabelsFound;
	}

	private void addToVectorIfNotAlreadyThere(Vector vector, Object label)
	{
		if(!vector.contains(label))
			vector.add(label);
	}

	private void addAllUniqueLabels(Vector duplicateLabelsFound, Vector duplicatedGridLabels)
	{
		for(int j=0;j<duplicatedGridLabels.size(); ++j)
		{
			addToVectorIfNotAlreadyThere(duplicateLabelsFound, duplicatedGridLabels.get(j));
		}
	}

	private Vector getDuplicatedGridLabels(GridFieldSpec grid)
	{
		Vector duplicatedGridLabels = new Vector();
		Vector gridLabels = grid.getAllColumnLabels();
		HashSet uniqueGridColumnLabels = new HashSet();
		for(Iterator iter = gridLabels.iterator(); iter.hasNext();)
		{
			String gridColumnLabel = ((String)iter.next()).trim();
			if(uniqueGridColumnLabels.contains(gridColumnLabel))
				addToVectorIfNotAlreadyThere(duplicatedGridLabels, gridColumnLabel);
			uniqueGridColumnLabels.add(gridColumnLabel);
		}
		return duplicatedGridLabels;
	}

	private FieldSpecCollection mergeSections(String topSectionXml,
			String bottomSectionXml) throws CustomFieldsParseException
	{
		FieldSpec[] topSection = FieldCollection.parseXml(topSectionXml).asArray();
		FieldSpec[] bottomSection = FieldCollection.parseXml(bottomSectionXml).asArray();
		int topLength = topSection.length;
		int bottomLength = bottomSection.length;
		FieldSpec[] allSpecs = new FieldSpec[topLength + bottomLength];
		System.arraycopy(topSection, 0, allSpecs, 0, topLength);
		System.arraycopy(bottomSection, 0, allSpecs, topLength, bottomLength);
		return new FieldSpecCollection(allSpecs);
	}
}