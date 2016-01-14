/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.common.test;

import org.junit.Test;
import org.martus.common.DefaultLanguageSettingsProvider;
import org.martus.common.MiniLocalization;
import org.martus.util.TestCaseEnhanced;

public class TestDefaultLanguageSettingsProvider extends TestCaseEnhanced
{

	public TestDefaultLanguageSettingsProvider(String name)
	{
		super(name);
	}

	@Test
	public void testBasics()
	{
		DefaultLanguageSettingsProvider defaultLanguageProvider = new DefaultLanguageSettingsProvider();
		assertEquals(defaultLanguageProvider.getCurrentLanguage(), MiniLocalization.ENGLISH);
		defaultLanguageProvider.setCurrentLanguage("fr");
		assertEquals("Default Language Provider is immutable, should still be English", defaultLanguageProvider.getCurrentLanguage(), MiniLocalization.ENGLISH);

		assertEquals(defaultLanguageProvider.getCurrentCalendarSystem(), MiniLocalization.GREGORIAN_SYSTEM);
		defaultLanguageProvider.setCurrentCalendarSystem(MiniLocalization.PERSIAN_SYSTEM);
		assertEquals(MiniLocalization.PERSIAN_SYSTEM, defaultLanguageProvider.getCurrentCalendarSystem());
		
		String defaultDateFormat = "MM/dd/yyyy";
		assertEquals(defaultLanguageProvider.getCurrentDateFormat(), defaultDateFormat);
		defaultLanguageProvider.setCurrentDateFormat("dd-MM-yyyy");
		assertEquals("Default Language Provider is immutable, should still be default calendar", defaultLanguageProvider.getCurrentDateFormat(), defaultDateFormat);
		
		assertTrue(defaultLanguageProvider.getAdjustPersianLegacyDates());
		
		
		assertTrue(defaultLanguageProvider.getAdjustThaiLegacyDates());	
	}
	
}
