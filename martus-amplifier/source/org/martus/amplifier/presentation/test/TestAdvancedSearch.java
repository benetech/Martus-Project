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

import java.io.File;
import java.util.HashMap;

import org.apache.velocity.context.Context;
import org.martus.amplifier.common.AdvancedSearchInfo;
import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.main.EventDatesIndexedList;
import org.martus.amplifier.main.LanguagesIndexedList;
import org.martus.amplifier.presentation.AdvancedSearch;
import org.martus.util.TestCaseEnhanced;

public class TestAdvancedSearch extends TestCaseEnhanced
{
	public TestAdvancedSearch(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		File empty = createTempFileWithData("");
		File emptyDates = createTempFile();
		LanguagesIndexedList.initialize(empty);
		EventDatesIndexedList.initialize(emptyDates);
	}
	
	public void testBasics() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = new MockContext();
		
		AdvancedSearch as = new AdvancedSearch();
		String templateName = as.selectTemplate(request, response, context);
		assertEquals("AdvancedSearch.vm", templateName);
	}	
	
	public void testPopulateAdvancedSearch() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;		
		Context context = new MockContext();
	
		AdvancedSearch servlet = new AdvancedSearch();					
		String templateName = servlet.selectTemplate(request, response, context);
				
		assertEquals("AdvancedSearch.vm", templateName);				
				
		AdvancedSearchInfo info = defaultAdvancedSearchInfo();
		request.getSession().setAttribute("defaultAdvancedSearch", info);
	
		servlet = new AdvancedSearch();
		servlet.selectTemplate(request, response, context);
		AdvancedSearchInfo defaultInfo = (AdvancedSearchInfo) context.get("defaultAdvancedSearch");
		
		assertEquals("Get exphrase query string", "amp test", defaultInfo.get(SearchResultConstants.EXACTPHRASE_TAG));	
		assertEquals("Get anyword query string", "amp", defaultInfo.get(SearchResultConstants.ANYWORD_TAG));
		assertEquals("Get these query string", "my test", defaultInfo.get(SearchResultConstants.THESE_WORD_TAG));
		assertEquals("Get bulletin field string", "title", defaultInfo.get(SearchResultConstants.RESULT_FIELDS_KEY));
		assertEquals("Get language string", "english", defaultInfo.get(SearchResultConstants.RESULT_LANGUAGE_KEY));				
		
	}	
	
	private AdvancedSearchInfo defaultAdvancedSearchInfo()
	{
		HashMap map = new HashMap();
		map.put(SearchResultConstants.EXACTPHRASE_TAG, "amp test");
		map.put(SearchResultConstants.ANYWORD_TAG, "amp");
		map.put(SearchResultConstants.THESE_WORD_TAG, "my test");
		map.put(SearchResultConstants.RESULT_LANGUAGE_KEY, "english");
		map.put(SearchResultConstants.RESULT_FIELDS_KEY, "title");
		return new AdvancedSearchInfo(map);		
	}
}
