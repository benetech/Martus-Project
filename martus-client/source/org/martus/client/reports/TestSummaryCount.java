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
package org.martus.client.reports;

import org.martus.common.ReusableChoices;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.util.TestCaseEnhanced;

public class TestSummaryCount extends TestCaseEnhanced
{
	public TestSummaryCount(String name)
	{
		super(name);
	}

	public void testBasics()
	{
		String[] labels = {"first", "second", "third"};
		int FIRST = 0;
		int SECOND = 1;
		int THIRD = 2;
		
		String[][] values = {
				{"A", "1", "a"},
				{"A", "1", "b"},
				{"A", "1", "b"},
				{"A", "2", "a"},
				{"B", "3", "c"},
		};
		SummaryCount master = new SummaryCount(new StringVector(labels));
		for(int sampleDataIndex = 0; sampleDataIndex < values.length; ++sampleDataIndex)
		{
			ReusableChoices choices = new ReusableChoices("", "");
			String[] codeValues = values[sampleDataIndex];
			for(int breakLevel = 0; breakLevel < codeValues.length; ++breakLevel)
				choices.add(new ChoiceItem(codeValues[breakLevel], codeValues[breakLevel]));
			master.increment(choices);
		}
		
		assertEquals("top-level wrong count?", 5, master.count());
		assertEquals(2, master.getChildCount());
		
		SummaryCount a = master.getChild(0);
		assertEquals(labels[FIRST], a.label());
		assertEquals("A", a.value());
		assertEquals(4, a.count());
		assertEquals(2, a.getChildCount());

		SummaryCount a1 = a.getChild(0);
		assertEquals(labels[SECOND], a1.label());
		assertEquals("1", a1.value());
		assertEquals(3, a1.count());
		assertEquals(2, a1.getChildCount());

		SummaryCount a1a = a1.getChild(0);
		assertEquals(labels[THIRD], a1a.label());
		assertEquals("a", a1a.value());
		assertEquals(1, a1a.count());
		assertEquals(0, a1a.getChildCount());

		SummaryCount a1b = a1.getChild(1);
		assertEquals(labels[THIRD], a1b.label());
		assertEquals("b", a1b.value());
		assertEquals(2, a1b.count());
		assertEquals(0, a1b.getChildCount());

		SummaryCount a2 = a.getChild(1);
		assertEquals(labels[SECOND], a2.label());
		assertEquals("2", a2.value());
		assertEquals(1, a2.count());
		assertEquals(1, a2.getChildCount());

		SummaryCount a2a = a2.getChild(0);
		assertEquals(labels[THIRD], a2a.label());
		assertEquals("a", a2a.value());
		assertEquals(1, a2a.count());
		assertEquals(0, a2a.getChildCount());

		SummaryCount b = master.getChild(1);
		assertEquals(labels[FIRST], b.label());
		assertEquals("B", b.value());
		assertEquals(1, b.count());
		assertEquals(1, b.getChildCount());
		
		SummaryCount b3 = b.getChild(0);
		assertEquals(labels[SECOND], b3.label());
		assertEquals("3", b3.value());
		assertEquals(1, b3.count());
		assertEquals(1, b3.getChildCount());

		SummaryCount b3c = b3.getChild(0);
		assertEquals(labels[THIRD], b3c.label());
		assertEquals("c", b3c.value());
		assertEquals(1, b3c.count());
		assertEquals(0, b3c.getChildCount());


	}
}
