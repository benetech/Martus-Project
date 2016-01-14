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

package org.martus.common.field;

import org.martus.common.GridData;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;
import org.martus.util.TestCaseEnhanced;

public class TestMartusSearchableGridColumnField extends TestCaseEnhanced 
{
	public TestMartusSearchableGridColumnField(String name)
	{
		super(name);
	}

	public void testGetSubfield() throws Exception
	{
		MiniLocalization localization = new MiniLocalization();
		
		FieldSpec dateRangeSpec = FieldSpec.createCustomField("tag", "Label", new FieldTypeDateRange());
		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.addColumn(dateRangeSpec);

		GridData data = new GridData(gridSpec, PoolOfReusableChoicesLists.EMPTY_POOL);
		data.addEmptyRow();
		final int SEPTEMBER = 9;
		MultiCalendar begin = MultiCalendar.createFromGregorianYearMonthDay(2004, SEPTEMBER, 21);
		MultiCalendar end = MultiCalendar.createFromGregorianYearMonthDay(2005, 3, 18);
		String rangeString = MartusFlexidate.toBulletinFlexidateFormat(begin, end);
		data.setValueAt(rangeString, 0, 0);

		MartusGridField gridField = new MartusGridField(gridSpec, PoolOfReusableChoicesLists.EMPTY_POOL);
		gridField.setData(data.getXmlRepresentation());

		MartusSearchableGridColumnField columnField = new MartusSearchableGridColumnField(gridField, 0, PoolOfReusableChoicesLists.EMPTY_POOL);
		MartusField rawBeginField = columnField.getSubField("begin", localization);
		MartusSearchableGridColumnField beginField = (MartusSearchableGridColumnField)rawBeginField;
		assertTrue("didn't find begin?", beginField.doesMatch(MartusField.EQUAL, "2004-09-21", localization));
	}
}
