/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.common.field;

import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;

public class MartusDateRangeField extends MartusField
{
	public MartusDateRangeField(FieldSpec specToUse)
	{
		super(specToUse, PoolOfReusableChoicesLists.EMPTY_POOL);
	}
	
	public MartusField createClone()
	{
		MartusField clone = new MartusDateRangeField(getFieldSpec());
		clone.setData(getData());
		return clone;
	}
	
	public MartusField getSubField(String tag, MiniLocalization localization)
	{
		MartusFlexidate date = localization.createFlexidateFromStoredData(getData());
		if(tag.equals(SUBFIELD_BEGIN))
			return createDateSubField(tag, date.getBeginDate());
		
		if(tag.equals(SUBFIELD_END))
			return createDateSubField(tag, date.getEndDate());
		
		return null;
	}

	private MartusField createDateSubField(String tag, MultiCalendar singleDateString)
	{
		FieldSpec subSpec = FieldSpec.createSubField(spec, tag, "", new FieldTypeDate());
		subSpec.setParent(spec);
		MartusField sub = new MartusField(subSpec, PoolOfReusableChoicesLists.EMPTY_POOL);
		sub.setData(MartusFlexidate.toStoredDateFormat(singleDateString));
		return sub;
	}

	public static final String SUBFIELD_END = "end";
	public static final String SUBFIELD_BEGIN = "begin";

}
