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

package org.martus.amplifier.lucene;

import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.search.SearchConstants;

public class QueryBuilder
{
	QueryBuilder(String queryString, String[] fields) throws Exception
	{
		query = parseMultiFieldQuery(queryString, fields, ERROR_PARSING_MULTIQUERY);
	}

	QueryBuilder(Map fields) throws Exception
	{
		query = parseAdvancedQuery(fields);
	}
	
	QueryBuilder(String searchFor) throws Exception
	{
		query = parseSingleFieldQuery(searchFor, SearchResultConstants.IN_ALL_FIELDS, ERROR_PARSING_QUERY);
	}

	public Query getQuery()
	{
		return query;
	}
	

	static Query parseEventDateQuery(Map fields)
			throws Exception 
	{
		String startDate	= (String) fields.get(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD);
		String endDate 	= (String) fields.get(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD);


		String queryString = "";
	
		Boolean searchUnknowns = (Boolean)fields.get(LuceneSearchConstants.SEARCH_UNKNOWN_DATES_FIELD);
		if(searchUnknowns != null && searchUnknowns.booleanValue())
		{
			String unknownRange = setRangeQuery(LuceneSearchConstants.UNKNOWN_DATE, LuceneSearchConstants.UNKNOWN_DATE);
			queryString += getFieldQuery(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, unknownRange);
			queryString += OR;
			queryString += getFieldQuery(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, unknownRange);
			queryString += OR;
		}

		String startDateString = setRangeQuery("*", endDate);
		String endDateString   = setRangeQuery(startDate, "?");
		queryString += " ( ";
		queryString += getFieldQuery(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, startDateString);
		queryString += AND;
		queryString += getFieldQuery(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD,endDateString);
		queryString += " ) ";

		return parseSingleFieldQuery(queryString,SearchConstants.SEARCH_EVENT_DATE_INDEX_FIELD, ERROR_PARSING_QUERY);
	
	}
	
	static Query parseEntryDateQuery(Map fields)
			throws Exception 
	{		
		String startDate	= (String) fields.get(SearchConstants.SEARCH_ENTRY_DATE_INDEX_FIELD);

		if (startDate == null)				
			return null;

		return parseSingleFieldQuery(buildEntryDateSearchString(startDate), SearchConstants.SEARCH_ENTRY_DATE_INDEX_FIELD,
			"Improperly formed advanced find entry date type in bulletin query: ");		
	}
	
	static public String buildEntryDateSearchString(String startDate)
	{
		String endDate = "9999-99-99";
		return setRangeQuery(startDate, endDate);
	}
	
	static Query parseAnyWordsQuery(Map fields) throws Exception
	{
		return parseStringQuery(fields, SearchResultConstants.ANYWORD_TAG);
	}

	static Query parseTheseWordsQuery(Map fields) throws Exception
	{
		return parseStringQuery(fields, SearchResultConstants.THESE_WORD_TAG);
	}

	static Query parseExactPhraseQuery(Map fields) throws Exception
	{
		return parseStringQuery(fields, SearchResultConstants.EXACTPHRASE_TAG);
	}	

	static Query parseWithoutWordsQuery(Map fields) throws Exception
	{
		return parseStringQuery(fields, SearchResultConstants.WITHOUTWORDS_TAG);
	}

	private static Query parseStringQuery(Map fields, String queryTag)
		throws Exception
	{
		String fieldString = (String) fields.get(SearchResultConstants.RESULT_FIELDS_KEY);
		String queryString = (String) fields.get(queryTag);
		
		if (queryString == null || queryString.length() <= 1)
			return null;
			
		return parseSingleFieldQuery(queryString, fieldString, "Improperly formed advanced find bulletin query: ");
	}	

	static Query parseAdvancedQuery(Map fields) throws Exception
	{
		BooleanQuery query = new BooleanQuery();
		
		Query foundEventDateQuery = parseEventDateQuery(fields);					
		query.add(foundEventDateQuery, true, false);
		
		Query foudAnywordsQuery = parseAnyWordsQuery(fields);
		if (foudAnywordsQuery != null)
			query.add(foudAnywordsQuery, true, false);

		Query foudThesewordsQuery = parseTheseWordsQuery(fields);
		if (foudThesewordsQuery != null)
			query.add(foudThesewordsQuery, true, false);

		Query foudExactPhraseQuery = parseExactPhraseQuery(fields);
		if (foudExactPhraseQuery != null)
			query.add(foudExactPhraseQuery, true, false);

		Query foudWithoutWordsQuery = parseWithoutWordsQuery(fields);
		if (foudWithoutWordsQuery != null)
			query.add(foudWithoutWordsQuery, true, false);
			
		addLanguageQuery(query, fields);
			
		Query foudEntryDateQuery = parseEntryDateQuery(fields);
		if (foudEntryDateQuery != null)
			query.add(foudEntryDateQuery, true, false);			
			
		return query;	
	}

	private static void addLanguageQuery(BooleanQuery wholeQuery, Map fields)
		throws Exception
	{
		String fieldString = (String) fields.get(SearchResultConstants.RESULT_LANGUAGE_KEY);
		if (fieldString != null)
		{				
			Query newQuery = parseSingleFieldQuery(fieldString,SearchConstants.SEARCH_LANGUAGE_INDEX_FIELD, "Improperly formed advanced find language type in bulletin query: ");
			wholeQuery.add(newQuery, true, false);
		}
	}			

	static Query parseMultiFieldQuery(String query, String[] fields, String msg)
			throws Exception 
	{
		return MultiFieldQueryParser.parse(query, fields, getAnalyzer());
	}
	
	static Query parseSingleFieldQuery(String query, String field, String msg)
			throws Exception 
	{
		return QueryParser.parse(query, field, getAnalyzer());
	}

	static String setRangeQuery(String from, String to)
	{
		return "[ " + from + " TO " + to + " ]";
	}
	
	static String getFieldQuery(String fieldTag, String query)
	{
		return fieldTag + ":" + query;
	}
		
	static Analyzer getAnalyzer()
	{
		return LuceneBulletinIndexer.getAnalyzer();
	}

	final static String AND = " AND ";
	final static String OR = " OR ";
	private static final String ERROR_PARSING_QUERY = "Improperly formed query: ";
	private static final String ERROR_PARSING_MULTIQUERY = "Improperly formed multiquery: ";

	Query query;
}
