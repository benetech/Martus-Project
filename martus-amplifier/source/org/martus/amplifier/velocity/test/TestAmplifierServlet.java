/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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
package org.martus.amplifier.velocity.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.martus.amplifier.velocity.AmplifierServlet;
import org.martus.util.TestCaseEnhanced;


public class TestAmplifierServlet extends TestCaseEnhanced
{
	public TestAmplifierServlet(String name)
	{
		super(name);
	}
	
	public void testMapHTMLDisplayFormatting() throws Exception
	{
		Map testMap = new HashMap();
		testMap.put(key1, value1);
		testMap.put(key2, value2);
		testMap.put(key3, value3);
		testMap.put(key4, value4);
		testMap.put(key5, value5);
		testMap.put(key6, value6);
		AmplifierServlet.formatDataForHtmlDisplay(testMap);
		assertEquals("&lt;HTML>", testMap.get(key1));		
		assertEquals("&amp;test", testMap.get(key2));		
		assertEquals("a<BR/>b", testMap.get(key3));		
		String tab = "&nbsp;&nbsp;&nbsp;&nbsp;";
		assertEquals(tab+"a"+tab+tab+"b"+tab, testMap.get(key4));		
		assertEquals("&nbsp;&nbsp;a&nbsp;&nbsp; b&nbsp;&nbsp;", testMap.get(key5));		
		assertEquals(value6, testMap.get(key6));		

		Map testEmptyMap = new HashMap();
		AmplifierServlet.formatDataForHtmlDisplay(testEmptyMap);

		Map testNullMap = null;
		AmplifierServlet.formatDataForHtmlDisplay(testNullMap);
	}	

	public void testVectprHTMLDisplayFormatting() throws Exception
	{
		Vector testVector = new Vector();
		testVector.add(value1);
		testVector.add(value2);
		testVector.add(value3);
		testVector.add(value4);
		testVector.add(value5);
		testVector.add(value6);
		AmplifierServlet.formatDataForHtmlDisplay(testVector);
		assertEquals("&lt;HTML>", testVector.get(0));		
		assertEquals("&amp;test", testVector.get(1));		
		assertEquals("a<BR/>b", testVector.get(2));		
		String tab = "&nbsp;&nbsp;&nbsp;&nbsp;";
		assertEquals(tab+"a"+tab+tab+"b"+tab, testVector.get(3));		
		assertEquals("&nbsp;&nbsp;a&nbsp;&nbsp; b&nbsp;&nbsp;", testVector.get(4));		
		assertEquals(value6, testVector.get(5));
		
		Vector testEmptyVector = new Vector();
		AmplifierServlet.formatDataForHtmlDisplay(testEmptyVector);

		Vector testNullVector = null;
		AmplifierServlet.formatDataForHtmlDisplay(testNullVector);
	}	

	final String key1 = "key1";
	final String key2 = "key2";
	final String key3 = "key3";
	final String key4 = "key4";
	final String key5 = "key5";
	final String key6 = "key6";
	final String value1 = "<HTML>";
	final String value2 = "&test";
	final String value3 = "a\nb";
	final String value4 = "	a		b	";
	final String value5 = "  a   b  ";
	final String value6 = " a b c ";
}
