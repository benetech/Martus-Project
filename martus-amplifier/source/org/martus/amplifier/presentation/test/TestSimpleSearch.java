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

package org.martus.amplifier.presentation.test;

import org.apache.velocity.context.Context;
import org.martus.amplifier.presentation.SimpleSearch;
import org.martus.util.TestCaseEnhanced;

public class TestSimpleSearch extends TestCaseEnhanced
{
	public TestSimpleSearch(String name)
	{
		super(name);
	}

	public void testBasics() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = new MockContext();
		
		SimpleSearch ss = new SimpleSearch();
		String templateName = ss.selectTemplate(request, response, context);
		assertEquals("SimpleSearch.vm", templateName);
	}
	
	public void testPopulateSimpleSearch() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;		
		Context context = new MockContext();
		
		SimpleSearch servlet = new SimpleSearch();					
		String templateName = servlet.selectTemplate(request, response, context);
					
		assertEquals("SimpleSearch.vm", templateName);				
		assertEquals("The defaultSimpleSearch is empty", "", context.get("defaultSimpleSearch"));		
		
		String sampleQuery = "this is what the user is searching for";		
		request.getSession().setAttribute("simpleQuery", sampleQuery);		
		
		servlet = new SimpleSearch();
		servlet.selectTemplate(request, response, context);
		
		assertEquals("The defaultSimpleSearch match.", sampleQuery, context.get("defaultSimpleSearch"));				
	}
	
}
