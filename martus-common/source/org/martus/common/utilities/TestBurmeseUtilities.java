/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2015, Beneficent
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
package org.martus.common.utilities;

import org.martus.util.TestCaseEnhanced;

public class TestBurmeseUtilities extends TestCaseEnhanced
{
	public TestBurmeseUtilities(String name)
	{
		super(name);
	}


	public void testGetDisplayableStoreable()
	{
		String englishData = "Test English";
		assertEquals("English text not returned?", BurmeseUtilities.getDisplayable(englishData), englishData);
		
		assertNotEquals("Burmese stored text should not equal displayed text?", BurmeseUtilities.getDisplayable(PTN_1), BurmeseUtilities.getStorable(PTN_1));
		assertEquals("Burmese text not correct?", BurmeseUtilities.getStorable(PTN_1), PTN_1);
		
		String shouldHandleNullString = null;
		assertNull(BurmeseUtilities.getDisplayable(shouldHandleNullString));
		assertNull(BurmeseUtilities.getStorable(shouldHandleNullString));
		
	}
	
	
	static String PTN_1 = "(([\u1000-\u101C\u101E-\u102A\u102C\u102E-\u103F\u104C-\u109F]))(\u1040)(?=\u0020)?";
	
}
