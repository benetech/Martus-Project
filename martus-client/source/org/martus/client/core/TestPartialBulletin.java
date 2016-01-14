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

import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.field.MartusDateRangeField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;
import org.martus.util.TestCaseEnhanced;

public class TestPartialBulletin extends TestCaseEnhanced
{
	public TestPartialBulletin(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		localization = new MiniLocalization();
		localization.setCurrentLanguageCode(MiniLocalization.ENGLISH);
	}
	
	public void testBasics() throws Exception
	{
		MockMartusSecurity security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		String[] tagsToStore = new String[] {
			Bulletin.TAGTITLE,
			Bulletin.TAGLANGUAGE,
		};
		for(int i = 0; i < tagsToStore.length; ++i)
			b.set(tagsToStore[i], tagsToStore[i]);
		PartialBulletin pb = new PartialBulletin(new SafeReadableBulletin(b, localization), tagsToStore);
		
		assertEquals("Didn't copy uid?", b.getUniversalId(), pb.getUniversalId());
		for(int i = 0; i < tagsToStore.length; ++i)
		{
			String tag = tagsToStore[i];
			assertEquals("Didn't store " + tag + "?", b.get(tag), pb.getData(tag));
		}
	}
	
	public void testPseudoTags() throws Exception
	{
		String tags[] = {Bulletin.PSEUDOFIELD_LAST_SAVED_DATE, Bulletin.PSEUDOFIELD_LOCAL_ID, Bulletin.TAGSTATUS,};
		MockMartusSecurity security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		for(int i = 0; i < tags.length; ++i)
			assertNotEquals("Pseudotag not working: " + tags[i] + "?", "", b.get(tags[i]));
		PartialBulletin pb = new PartialBulletin(new SafeReadableBulletin(b, localization), tags);
		for(int i = 0; i < tags.length; ++i)
			assertEquals("Didn't copy pseudo tag " + tags[i] + "?", b.get(tags[i]), pb.getData(tags[i]));
	}
	
	public void testSubFields() throws Exception
	{
		MockMartusSecurity security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);

		MultiCalendar beginDate = MultiCalendar.createFromGregorianYearMonthDay(2003, 07, 25);
		MultiCalendar endDate = MultiCalendar.createFromGregorianYearMonthDay(2007, 01, 14);
		b.set(Bulletin.TAGEVENTDATE, MartusFlexidate.toBulletinFlexidateFormat(beginDate, endDate));

		SafeReadableBulletin readableBulletin = new SafeReadableBulletin(b, localization);
		String tags[] = {Bulletin.TAGEVENTDATE + "." + MartusDateRangeField.SUBFIELD_BEGIN};
		PartialBulletin pb = new PartialBulletin(readableBulletin, tags);
		for(int i = 0; i < tags.length; ++i)
		{
			String expected = readableBulletin.getPossiblyNestedField(tags[i]).getData();
			assertEquals("Didn't copy subfield " + tags[i] + "?", expected, pb.getData(tags[i]));
		}
		
	}
	
	public void testGridColumns() throws Exception
	{
		MockMartusSecurity security = MockMartusSecurity.createClient();
		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setTag("grid");
		gridSpec.setLabel("Grid");
		gridSpec.addColumn(FieldSpec.createCustomField("", "Label", new FieldTypeNormal()));
		Bulletin b = new Bulletin(security, new FieldSpecCollection(new FieldSpec[] {gridSpec}), new FieldSpecCollection());
		GridData gridData = new GridData(gridSpec, PoolOfReusableChoicesLists.EMPTY_POOL);
		gridData.addEmptyRow();
		String sampleCellValue = "Data";
		gridData.setValueAt(sampleCellValue, 0, 0);
		b.set(gridSpec.getTag(), gridData.getXmlRepresentation());
		
		SafeReadableBulletin readableBulletin = new SafeReadableBulletin(b, localization);
		String[] tags = {gridSpec.getTag() + "." + "Label"}; 
		PartialBulletin pb = new PartialBulletin(readableBulletin, tags);
		String gotData = pb.getData(tags[0]);
		assertEquals("No grid cell data?", sampleCellValue + "\n", gotData);
	}
	
	public void testEquals() throws Exception
	{
		MockMartusSecurity security = MockMartusSecurity.createClient();
		Bulletin b1 = new Bulletin(security);
		Bulletin b2 = new Bulletin(security);
		String[] tagsToStore = new String[] {
			Bulletin.TAGTITLE,
			Bulletin.TAGLANGUAGE,
		};
		for(int i = 0; i < tagsToStore.length; ++i)
		{
			b1.set(tagsToStore[i], tagsToStore[i]);
			b2.set(tagsToStore[i], "Not " + tagsToStore[i]);
		}
		PartialBulletin pb1 = new PartialBulletin(new SafeReadableBulletin(b1, localization), tagsToStore);
		PartialBulletin pb2a = new PartialBulletin(new SafeReadableBulletin(b2, localization), tagsToStore);
		PartialBulletin pb2b = new PartialBulletin(new SafeReadableBulletin(b2, localization), tagsToStore);
		assertNotEquals("not seeing different?", pb1, pb2a);
		assertEquals("identical not equal?", pb2a, pb2b);
		assertEquals("hashes not consistent?", pb2a.hashCode(), pb2b.hashCode());
	}

	MiniLocalization localization;
}
