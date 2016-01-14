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

package org.martus.amplifier.common;


public interface SearchResultConstants
{
	final String RESULT_START_YEAR_KEY 	= "startYear";
	final String RESULT_START_MONTH_KEY = "startMonth";
	final String RESULT_START_DAY_KEY 	= "startDay";
	final String RESULT_END_YEAR_KEY  	= "endYear";
	final String RESULT_END_MONTH_KEY 	= "endMonth";
	final String RESULT_END_DAY_KEY 	= "endDay";
	final String RESULT_INCLUDE_UNKNOWNS_KEY = "includeUnknowns";
	final String RESULT_LANGUAGE_KEY 	= "language";
	final String RESULT_FIELDS_KEY		= "fields";
	final String RESULT_ENTRY_DATE_KEY	= "entryDate";
	final String RESULT_SORTBY_KEY		= "sortBy";
	final String RESULT_BASIC_FIELD_KEY	= "field";
	final String RESULT_BASIC_QUERY_KEY	= "query";
	
	final String THESE_WORD_TAG 		= "allWordsQuery";
	final String ANYWORD_TAG 			= "anyWordsQuery";
	final String EXACTPHRASE_TAG 		= "exactPhraseQuery";
	final String WITHOUTWORDS_TAG  		= "excludeWordsQuery";
	final String INCLUDE_UNKNOWNS_VALUE	= "includeUnknowns";
	
	final static String THESE_WORD_LABEL 	= "with all of these words";
	final static String EXACTPHRASE_LABEL 	= "with this exact phrase";
	final static String ANYWORD_LABEL 		= "with any of these words";
	final static String WITHOUTWORDS_LABEL  = "without any of these words";
	
	
	final static String ENTRY_ANYTIME_LABEL		= "any time";
	final static String ENTRY_PAST_WEEK_LABEL	= "week";
	final static String ENTRY_PAST_MONTH_LABEL	= "month";
	final static String ENTRY_PAST_3_MONTH_LABEL= "3 months";
	final static String ENTRY_PAST_6_MONTH_LABEL= "6 months";
	final static String ENTYR_PAST_YEAR_LABEL	= "year";
	
	final static String ENTRY_ANYTIME_TAG			= "99999";
	final static String ENTRY_PAST_WEEK_DAYS_TAG	= "7";
	final static String ENTRY_PAST_MONTH_DAYS_TAG	= "30";
	final static String ENTRY_PAST_3_MONTH_DAYS_TAG	= "90";
	final static String ENTRY_PAST_6_MONTH_DAYS_TAG	= "180";
	final static String ENTRY_PAST_YEAR_DAYS_TAG	= "365";	
	
	final static String ANYWHERE_IN_BULLETIN_KEY= "anywhere";
	final static String IN_TITLE_KEY			= "title";
	final static String IN_KEYWORDS_KEY			= "keywords";
	final static String IN_SUMMARY_KEY			= "summary";
	final static String IN_AUTHOR_KEY			= "author";
	final static String IN_DETAIL_KEY			= "detail of event";
	final static String IN_ORGANIZATION_KEY		= "organization";
	final static String IN_LOCATION_KEY			= "location of event";	
	final static String IN_ALL_FIELDS			= "all";	
	
	final static String LANGUAGE_ANYLANGUAGE_LABEL	= "any language";
	final static String LANGUAGE_ENGLISH_LABEL		= "English";
	final static String LANGUAGE_FRENCH_LABEL		= "French";
	final static String LANGUAGE_GERMAN_LABEL		= "German";
	final static String LANGUAGE_INDONESIAN_LABEL	= "Indonesian";
	final static String LANGUAGE_RUSSIAN_LABEL		= "Russian";
	final static String LANGUAGE_SPANISH_LABEL		= "Spanish";
	
	final static String SORT_BY_TITLE_TAG			= "title";
	final static String SORT_BY_AUTHOR_TAG			= "author";
	final static String SORT_BY_LOCATION_TAG		= "location";
	final static String SORT_BY_EVENTDATE_TAG		= "event date";
	final static String SORT_BY_ORGANIZATION_TAG	= "organization";
	final static String SORT_BY_ENTRYDATE_TAG		= "date created";
		
	
	final String[] ADVANCED_KEYS = new String[] {
		RESULT_START_YEAR_KEY, 
		RESULT_START_MONTH_KEY, 
		RESULT_START_DAY_KEY,
		RESULT_END_YEAR_KEY, 
		RESULT_END_MONTH_KEY, 
		RESULT_END_DAY_KEY,
		RESULT_INCLUDE_UNKNOWNS_KEY, 
		RESULT_FIELDS_KEY, 
		RESULT_LANGUAGE_KEY, 
		RESULT_ENTRY_DATE_KEY, 
		RESULT_SORTBY_KEY, 
		THESE_WORD_TAG,ANYWORD_TAG,
		EXACTPHRASE_TAG, 
		WITHOUTWORDS_TAG, 
		RESULT_SORTBY_KEY
 };		
}
