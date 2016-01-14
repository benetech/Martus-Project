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
package org.martus.common;

import java.util.Arrays;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeMessage;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.TestCaseEnhanced;

public class TestFieldCollectionMemoryUsage extends TestCaseEnhanced
{
	public TestFieldCollectionMemoryUsage(String name)
	{
		super(name);
	}

	public void testObjectReuse() throws Exception
	{
		FieldSpec[] template1 = {createLongMessageFieldSpec(10000),};
		FieldSpec[] template2 = {createLongMessageFieldSpec(10000),};
		assertFalse("templates are identical object?", template1 == template2);
		assertTrue("templates not equal contents?", Arrays.equals(template1, template2));
		FieldCollection c1 = new FieldCollection(template1);
		FieldCollection c2 = new FieldCollection(template2);
		assertFalse("collections are identical object?", c1 == c2);
		assertTrue("underlying FieldSpecs not shared?", c1.getSpecs().get(0) == c2.getSpecs().get(0));
		assertTrue("returned FieldSpecCollection not identical?", c1.getSpecs() == c2.getSpecs());
	}
	
	public void testBugInRequiredFieldCacheing() throws Exception
	{
		MockMartusSecurity security = MockMartusSecurity.createClient();
		Bulletin b = new BulletinForTesting(security);
		b.getField(Bulletin.TAGAUTHOR).getFieldSpec().setRequired();

		FieldSpecCollection topSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldCollection fields = new FieldCollection(topSpecs);
		assertFalse(fields.findByTag(Bulletin.TAGAUTHOR).getFieldSpec().isRequiredField());
	}
	
	public void testMemoryUsage() throws Exception
	{
		if(!RUN_REALLY_SLOW_TEST)
			return;
		
		byte[] bigArray = new byte[30000000];
		bigArray[0] = 0;
		
		int bulletinCount = 250;
		int fieldCount = 10;
		int fieldSize = 10000;
		
		FieldCollection[] packets = new FieldCollection[bulletinCount];
		for(int b = 0; b < packets.length; ++b)
		{
			FieldSpecCollection fields = new FieldSpecCollection();
			for(int f = 0; f < fieldCount; ++f)
			{
				fields.add(createLongMessageFieldSpec(fieldSize));
			}
			packets[b] = new FieldCollection(fields);
		}
		bigArray = null;
	}

	private FieldSpec createLongMessageFieldSpec(int length)
	{
		char[] labelChars = new char[length];
		Arrays.fill(labelChars, 'x');
		String longLabel = new String(labelChars);
		FieldSpec longMessage = FieldSpec.createFieldSpec(longLabel, new FieldTypeMessage());
		return longMessage;
	}
	
	boolean RUN_REALLY_SLOW_TEST = false;
}
