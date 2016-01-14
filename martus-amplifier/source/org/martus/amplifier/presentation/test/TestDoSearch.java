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

import java.util.List;
import java.util.Vector;

import org.apache.velocity.context.Context;
import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.presentation.DoSearch;
import org.martus.amplifier.presentation.SimpleSearch;
import org.martus.amplifier.search.BulletinIndexException;
import org.martus.amplifier.search.BulletinInfo;
import org.martus.amplifier.velocity.AmplifierServletRequest;
import org.martus.amplifier.velocity.AmplifierServletSession;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;

public class TestDoSearch extends TestCaseEnhanced
{
	public TestDoSearch(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		MartusAmplifier.staticAmplifierDirectory = createTempDirectory();
	}
	
	public void tearDown() throws Exception
	{
		DirectoryUtils.deleteEntireDirectoryTree(MartusAmplifier.staticAmplifierDirectory);
		super.tearDown();
	}

	public void testNoResults() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = new MockContext();
		
		DoSearch sr = new DoSearch();
		String sampleQueryString = "owiefijweofiejoifoiwjefoiwef";
		request.putParameter("query", sampleQueryString);
		String templateName = sr.selectTemplate(request, response, context);
		assertEquals("NoSearchResults.vm", templateName);
		assertEquals(sampleQueryString, request.getSession().getAttribute("simpleQuery"));
	}

	public void testYesResults() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		request.parameters.put(SearchResultConstants.RESULT_SORTBY_KEY, "title");
		MockAmplifierResponse response = null;
		Context context = new MockContext();

		SearchResultsForTesting sr = new SearchResultsForTesting();
		request.putParameter("query", "owiefijweofiejoifoiwjefoiwef");
		String templateName = sr.selectTemplate(request, response, context);
		assertEquals("SearchResults.vm", templateName);

		int expectedFoundCount = 3;
		Vector foundBulletins = (Vector)context.get("foundBulletins");
		assertEquals(expectedFoundCount, foundBulletins.size());
		BulletinInfo info = (BulletinInfo)foundBulletins.get(0);
		assertEquals(uid1, info.getBulletinId());
		assertEquals("Total bulletin count incorrect?", new Integer(expectedFoundCount), context.get("totalBulletins"));

		request.putParameter("query", ""); 
		templateName = sr.selectTemplate(request, response, context);
		assertEquals("NoSearchResults.vm", templateName);

	}

	public void testLanguageCodeToString() throws Exception
	{
		BulletinInfo bulletinInfo1 = new BulletinInfo(uid1);
		bulletinInfo1.set("language", bulletin1Language);
		BulletinInfo bulletinInfo2 = new BulletinInfo(uid2);
		bulletinInfo2.set("language", bulletin2Language);
		BulletinInfo bulletinInfo3 = new BulletinInfo(uid3);
		bulletinInfo3.set("language", bulletin3Language);

		SearchResultsForTesting sr = new SearchResultsForTesting();

		sr.convertLanguageCode(bulletinInfo3);
		assertEquals("Unknown LanguageCode should be returned unchanged?", bulletin3Language, bulletinInfo3.get("language"));
		
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
	
	public void testSortBy() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = new MockContext();
		AmplifierServletSession session = request.getSession();
		
		String sortBy = "language";
		
		DoSearch sr = new DoSearch();
		String sampleQueryString = "owiefijweofiejoifoiwjefoiwef";
		request.putParameter("query", sampleQueryString);
		
		sr.selectTemplate(request, response, context);
		assertEquals("entrydate", session.getAttribute(SearchResultConstants.RESULT_SORTBY_KEY));
		
		request.putParameter(SearchResultConstants.RESULT_SORTBY_KEY, sortBy);
		sr.selectTemplate(request, response, context);
		assertEquals(sortBy, session.getAttribute(SearchResultConstants.RESULT_SORTBY_KEY));
	}

	
/*	private Context createSampleSearchResults(MockAmplifierRequest request, HttpServletResponse response) throws Exception
	{
		Context context = new MockContext();
		SearchResultsForTesting sr = new SearchResultsForTesting();
		request.putParameter("query", "test");
		request.parameters.put("index","1");
		sr.selectTemplate(request, response, context);
		return context;
	}
*/
	final UniversalId uid1 = UniversalIdForTesting.createDummyUniversalId();
	final UniversalId uid2 = UniversalIdForTesting.createDummyUniversalId();
	final UniversalId uid3 = UniversalIdForTesting.createDummyUniversalId();
	final String bulletin1Title = "title 1";
	final String bulletin2Title = "title 2";
	final String bulletin3Title = "title 3";
	final String bulletin1Language = "en";
	final String bulletin2Language = "es";
	final String bulletin3Language = "un";


	class SearchResultsForTesting extends DoSearch
	{
		public List getSearchResults(AmplifierServletRequest request)
			throws Exception, BulletinIndexException
		{
			if(request.getParameter("query")=="")
				return new Vector();
			if(request.getParameter("query")==null)
				throw new Exception("malformed query");
			Vector infos = new Vector();
			BulletinInfo bulletinInfo1 = new BulletinInfo(uid1);
			bulletinInfo1.set("title", bulletin1Title);
			bulletinInfo1.set("language", bulletin1Language);
			infos.add(bulletinInfo1);
			
			BulletinInfo bulletinInfo2 = new BulletinInfo(uid2);
			bulletinInfo2.set("title", bulletin2Title);
			bulletinInfo2.set("language", bulletin2Language);
			infos.add(bulletinInfo2);
			
			BulletinInfo bulletinInfo3 = new BulletinInfo(uid3);
			bulletinInfo3.set("title", bulletin3Title);
			bulletinInfo3.set("language", bulletin3Language);
			infos.add(bulletinInfo3);
			return infos;
		}

	}
}
