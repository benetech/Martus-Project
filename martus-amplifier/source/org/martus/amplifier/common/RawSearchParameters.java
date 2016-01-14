/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2007, Beneficent
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

package org.martus.amplifier.common;

import java.util.HashMap;
import java.util.Map;

import org.martus.amplifier.main.EventDatesIndexedList;
import org.martus.amplifier.search.SearchConstants;
import org.martus.amplifier.velocity.AmplifierServletRequest;
import org.martus.amplifier.velocity.AmplifierServletSession;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;

public class RawSearchParameters
{
	public RawSearchParameters(String simpleSearch)
	{
		inputParameters = getDefaultAdvancedFields();
		inputParameters.put(SearchResultConstants.THESE_WORD_TAG, simpleSearch);
	}

	public RawSearchParameters(AmplifierServletRequest request)
	{
		inputParameters = loadFromRequest(request);
	}
	
	String get(String key)
	{
		return (String) inputParameters.get(key);
	}
	
	String getFormattedString(SearchParameters.LuceneQueryFormatter formatter)
	{
		return formatter.getFormattedString(getParameters());
	}

	Map getParameters()
	{
		return inputParameters;
	}
	
	private Map loadFromRequest(AmplifierServletRequest request)
	{
		Map requestParameters = new HashMap();
		for(int i=0; i< SearchResultConstants.ADVANCED_KEYS.length; i++)
		{
			String key = SearchResultConstants.ADVANCED_KEYS[i];
			String value = request.getParameter(key);	
			if (value != null)
				requestParameters.put(key, value);				
			
			if (value == null && isQueryString(key))				
				requestParameters.put(SearchResultConstants.ADVANCED_KEYS[i], "");
			
		}
		
		return requestParameters;
	}
	
	private boolean isQueryString(String key)
	{
		if (key.equals(SearchResultConstants.EXACTPHRASE_TAG)||
			key.equals(SearchResultConstants.ANYWORD_TAG)||
			key.equals(SearchResultConstants.THESE_WORD_TAG)||
			key.equals(SearchResultConstants.WITHOUTWORDS_TAG))
			return true;
				
		return false;	
	}

	public void saveSearchInSession(AmplifierServletSession session)
	{
		AdvancedSearchInfo info = new AdvancedSearchInfo(getParameters());
		session.setAttribute("defaultAdvancedSearch", info);	
	}

	public static void clearSimpleSearch(AmplifierServletSession session)
	{
		session.setAttribute("simpleQuery", "");
		session.setAttribute("defaultSimpleSearch", "");
	}

	public static void clearAdvancedSearch(AmplifierServletSession session)
	{
		AdvancedSearchInfo info = new AdvancedSearchInfo(getDefaultAdvancedFields());
		session.setAttribute("defaultAdvancedSearch", info);	
	}

	public static HashMap getDefaultAdvancedFields()
	{
		HashMap defaultMap = new HashMap();
		defaultMap.put(SearchResultConstants.EXACTPHRASE_TAG, "");
		defaultMap.put(SearchResultConstants.ANYWORD_TAG, "");
		defaultMap.put(SearchResultConstants.THESE_WORD_TAG, "");	
		defaultMap.put(SearchResultConstants.WITHOUTWORDS_TAG, "");
		defaultMap.put(SearchResultConstants.RESULT_FIELDS_KEY, SearchResultConstants.IN_ALL_FIELDS);
		defaultMap.put(SearchResultConstants.RESULT_ENTRY_DATE_KEY, SearchResultConstants.ENTRY_ANYTIME_TAG);
		defaultMap.put(SearchResultConstants.RESULT_INCLUDE_UNKNOWNS_KEY, SearchResultConstants.INCLUDE_UNKNOWNS_VALUE);
		defaultMap.put(SearchResultConstants.RESULT_LANGUAGE_KEY, SearchResultConstants.LANGUAGE_ANYLANGUAGE_LABEL);
		defaultMap.put(SearchResultConstants.RESULT_SORTBY_KEY, SearchConstants.SEARCH_ENTRY_DATE_INDEX_FIELD);

		EventDatesIndexedList eventDatesIndexedList = EventDatesIndexedList.getEventDatesIndexedList();

		defaultMap.put(SearchResultConstants.RESULT_START_DAY_KEY, "1");
		defaultMap.put(SearchResultConstants.RESULT_START_MONTH_KEY, "0");
		defaultMap.put(SearchResultConstants.RESULT_START_YEAR_KEY, Integer.toString(eventDatesIndexedList.getEarliestYear()));
		
		defaultMap.put(SearchResultConstants.RESULT_END_DAY_KEY, Integer.toString(new MultiCalendar().getGregorianDay()));
		defaultMap.put(SearchResultConstants.RESULT_END_MONTH_KEY, Integer.toString((new MultiCalendar().getGregorianMonth() - 1)));
		defaultMap.put(SearchResultConstants.RESULT_END_YEAR_KEY, Integer.toString(eventDatesIndexedList.getLatestYear()));
				
		return defaultMap;	
	}
	
	public String getFieldToSearchIn()
	{
		return get(SearchResultConstants.RESULT_FIELDS_KEY);
	}
	
	public String getLanguage()
	{
		return get(SearchResultConstants.RESULT_LANGUAGE_KEY);
	}
	
	public String getEntryDate()
	{
		return get(SearchResultConstants.RESULT_ENTRY_DATE_KEY);
	}
	
	public String getSortBy()
	{
		return get(SearchResultConstants.RESULT_SORTBY_KEY);
	}

	public String getStartDate()
	{			
		String yearTag = SearchResultConstants.RESULT_START_YEAR_KEY;
		String monthTag = SearchResultConstants.RESULT_START_MONTH_KEY;
		String dayTag = SearchResultConstants.RESULT_START_DAY_KEY;
		return getDateFromRawParameters(yearTag, monthTag, dayTag);
	}

	public String getEndDate()
	{	
		String yearTag = SearchResultConstants.RESULT_END_YEAR_KEY;
		String monthTag = SearchResultConstants.RESULT_END_MONTH_KEY;
		String dayTag = SearchResultConstants.RESULT_END_DAY_KEY;
		return getDateFromRawParameters(yearTag, monthTag, dayTag);
	}
	
	public boolean includeUnknowns()
	{
		return (get(SearchResultConstants.RESULT_INCLUDE_UNKNOWNS_KEY) != null);
	}

	private String getDateFromRawParameters(String yearTag, String monthTag, String dayTag)
	{
		int year = Integer.parseInt(get(yearTag));
		int month = Integer.parseInt(get(monthTag));
		int day = Integer.parseInt(get(dayTag));
		MultiCalendar startDate = SearchParameters.getDate(year, month, day);
		return MartusFlexidate.toStoredDateFormat(startDate);
	}

	Map inputParameters;
}
