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
package org.martus.amplifier.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.martus.amplifier.common.AmplifierLocalization;
import org.martus.amplifier.common.CharacterUtil;
import org.martus.amplifier.common.RawSearchParameters;
import org.martus.amplifier.common.SearchParameters;
import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.search.BulletinInfo;
import org.martus.amplifier.search.BulletinSearcher;
import org.martus.amplifier.search.Results;
import org.martus.amplifier.search.SearchConstants;
import org.martus.amplifier.velocity.AmplifierServletRequest;
import org.martus.amplifier.velocity.AmplifierServletSession;


public class DoSearch extends AbstractSearchResultsServlet
{
	public void configureSessionFromRequest(AmplifierServletRequest request)
	{
		AmplifierServletSession session = request.getSession();
		
		String basicQueryString = request.getParameter(SearchResultConstants.RESULT_BASIC_QUERY_KEY);
		String searchedForString = (String)session.getAttribute("searchedFor");
		String searchType = request.getParameter("typeOfSearch");
		
		if (searchType != null && searchType.equals("quickSearchAll"))
		{	
			searchedForString = "Search All Bulletins";
			basicQueryString = "";			
		}
		
		if(basicQueryString != null)
		{
			searchedForString = basicQueryString;
		}
		else
		{
			searchedForString = "Advanced Search";
			basicQueryString = "";
		}
		
		session.setAttribute("searchedFor", searchedForString);
		session.setAttribute("defaultSimpleSearch", basicQueryString);
		session.setAttribute("simpleQuery", basicQueryString);
		session.setAttribute("typeOfSearch", searchType);
	}

	List getBulletinsToDisplay(AmplifierServletRequest request)
		throws Exception
	{
		return  getSearchResults(request);
	}

	public List getSearchResults(AmplifierServletRequest request)
		throws Exception
	{
		AmplifierServletSession session = request.getSession();
		String searchType = (String) session.getAttribute("typeOfSearch");
		
		if (searchType != null && searchType.equals("quickSearchAll"))
		{			
			RawSearchParameters.clearAdvancedSearch(session);							
			return getSearchResults(session, new RawSearchParameters(""));
		}										
		
		if (isSimpleSearch(request))
		{
			String simpleQueryString = getSimpleSearchString(request);
			RawSearchParameters.clearAdvancedSearch(session);
					
			simpleQueryString = CharacterUtil.removeRestrictCharacters(simpleQueryString);		
			RawSearchParameters raw = new RawSearchParameters(simpleQueryString);
						
			if (simpleQueryString.equals(""))
				return new ArrayList();
					
			return getSearchResults(session, raw);
		}
		RawSearchParameters.clearSimpleSearch(session);										
		RawSearchParameters raw = new RawSearchParameters(request);
		return getSearchResults(session, raw);			
	}

	private boolean isSimpleSearch(AmplifierServletRequest request)
	{
		if(getSimpleSearchString(request) == null)
			return false;

		return true;
	}

	private String getSimpleSearchString(AmplifierServletRequest request)
	{
		return request.getParameter(SearchResultConstants.RESULT_BASIC_QUERY_KEY);
	}

	private List getSearchResults(
		AmplifierServletSession session,
		RawSearchParameters raw)
		throws Exception
	{
		raw.saveSearchInSession(session);
		
		SearchParameters sp = new SearchParameters(raw);
		Map fields = sp.getSearchFields();
		
		return getResults(fields);
	}

	public List getResults(Map fields) throws Exception
	{
		BulletinSearcher searcher = MartusAmplifier.openBulletinSearcher();
		
		try
		{
			Results results = searcher.search(fields);

			ArrayList list = new ArrayList();
			for (int i = 0; i < results.getCount(); i++)
			{
				BulletinInfo bulletinInfo = results.getBulletinInfo(i);
				convertLanguageCode(bulletinInfo);
				formatDataForHtmlDisplay(bulletinInfo.getFields());
				list.add(bulletinInfo);
			}
			return list;
		}
		finally
		{
			searcher.close();
		}
	}
	
	public void convertLanguageCode(BulletinInfo bulletinInfo)
	{
		String code = bulletinInfo.get(SearchConstants.SEARCH_LANGUAGE_INDEX_FIELD);
		if(code == null)
			return;
		String languageString = AmplifierLocalization.getLanguageString(code);
		if(languageString == null)
			return;				
		bulletinInfo.set(SearchConstants.SEARCH_LANGUAGE_INDEX_FIELD, languageString);
	}

}