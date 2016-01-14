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

To the extent this copyrighted software code is used in the 
Miradi project, it is subject to a royalty-free license to 
members of the Conservation Measures Partnership when 
used with the Miradi software as specified in the agreement 
between Benetech and WCS dated 5/1/05.
*/
package org.martus.util;

import org.martus.util.language.LanguageOptions;

public class TestDatePreference extends TestCaseEnhanced
{
	public TestDatePreference(String name)
	{
		super(name);
	}

	public void testBasics() throws Exception
	{
		DatePreference pref = new DatePreference();
		assertEquals("Doesn't default to mdy?", "mdy", pref.getMdyOrder());
		assertEquals("Doesn't default to slash delimiter?", '/', pref.getDelimiter());
		assertEquals("Wrong date format code?", "MM/dd/yyyy", pref.getRawDateTemplate());
		assertEquals("Wrong date template?", "MM/dd/yyyy", pref.getDateTemplate());
	}
	
	public void testNonDefaultConstructor() throws Exception
	{
		DatePreference pref = new DatePreference("ymd", '-');
		assertEquals("didn't set mdy?", "ymd", pref.getMdyOrder());
		assertEquals("didn't set delimiter?", '-', pref.getDelimiter());
	}
	
	public void testSetDelimiter() throws Exception
	{
		DatePreference pref = new DatePreference();
		pref.setDelimiter('-');
		assertEquals("didn't set delimiter?", '-', pref.getDelimiter());
		assertEquals("didn't use delimiter?", "MM-dd-yyyy", pref.getDateTemplate());
	}
	
	public void testSetMdyOrder() throws Exception
	{
		DatePreference pref = new DatePreference();
		pref.setMdyOrder("dmy");
		assertEquals("didn't set dmy?", "dmy", pref.getMdyOrder());
		assertEquals("didn't use dmy?", "dd/MM/yyyy", pref.getDateTemplate());
	}
	
	public void testIncorrectPrametersShouldResultToDefaultValues()
	{
		DatePreference pref = new DatePreference();
		try
		{
			pref.setDateTemplate("incorrect");
			fail("Should have thrown");
		}
		catch (Exception ignoreExpected)
		{
		}
	}
	
	public void testRightToLeft() throws Exception
	{
		DatePreference pref = new DatePreference();
		LanguageOptions.setDirectionRightToLeft();
		try
		{
			assertEquals("mdy", pref.getMdyOrder());
			assertEquals("ydm", pref.getMdyOrderForText());
			assertEquals("MM/dd/yyyy", pref.getRawDateTemplate());
			assertEquals("yyyy/dd/MM", pref.getDateTemplate());
		}
		finally
		{
			LanguageOptions.setDirectionLeftToRight();
		}
	}
	
	public void testSetDateTemplate() throws Exception
	{
		DatePreference pref = new DatePreference();
		pref.setDateTemplate("dd.MM.yyyy");
		assertEquals("wrong dmy order?", "dmy", pref.getMdyOrder());
		assertEquals("wrong delimiter?", '.', pref.getDelimiter());
		
		verifySetDateTemplateThrows("missing field", "mm/dd");
		verifySetDateTemplateThrows("dupe field", "mm/dd/yyyy/mm");
	}
	
	private void verifySetDateTemplateThrows(String message, String template)
	{
		DatePreference pref = new DatePreference();
		
		try
		{
			pref.setDateTemplate(template);
			fail("Should have thrown");
		}
		catch(Exception ignoreExpected)
		{
		}
	}
}
