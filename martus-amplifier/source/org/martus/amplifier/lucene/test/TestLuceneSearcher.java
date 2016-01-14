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
package org.martus.amplifier.lucene.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import junit.framework.Assert;

import org.martus.amplifier.common.AmplifierLocalization;
import org.martus.amplifier.common.SearchParameters;
import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.lucene.LuceneBulletinSearcher;
import org.martus.amplifier.lucene.LuceneSearchConstants;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.presentation.SearchResults;
import org.martus.amplifier.search.AttachmentInfo;
import org.martus.amplifier.search.BulletinField;
import org.martus.amplifier.search.BulletinIndexException;
import org.martus.amplifier.search.BulletinIndexer;
import org.martus.amplifier.search.BulletinInfo;
import org.martus.amplifier.search.BulletinSearcher;
import org.martus.amplifier.search.Results;
import org.martus.amplifier.search.SearchConstants;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UnicodeConstants;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.common.utilities.DateUtilities;
import org.martus.util.MultiCalendar;

public class TestLuceneSearcher extends CommonSearchTest
{
	public TestLuceneSearcher(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		MartusAmplifier.localization = new AmplifierLocalization();
		
		bulletinId1 = UniversalIdForTesting.createDummyUniversalId();
		bulletinId2 = UniversalIdForTesting.createDummyUniversalId();
		bulletinIdForeign = UniversalIdForTesting.createDummyUniversalId();
		oldVersionId = UniversalIdForTesting.createDummyUniversalId();

		fdp1 	= generateSampleData(bulletinId1);		
		fdp2 	= generateSampleFlexiData(bulletinId2);
		fdpForeign = generateSampleForeignCharData(bulletinIdForeign);
		
		history = new BulletinHistory();
		history.add(oldVersionId.getLocalId());
		
		indexer = openBulletinIndexer();
		indexer.clearIndex();
	}
	
	public void testGetDefaultSearchValues() throws Exception
	{
		HashMap values = LuceneBulletinSearcher.getDefaultSearchValues();
		assertEquals("Entry date start year too restrictive?", LuceneSearchConstants.EARLIEST_POSSIBLE_DATE, values.get(SearchConstants.SEARCH_ENTRY_DATE_INDEX_FIELD));
		assertEquals("Event date start year too restrictive?", LuceneSearchConstants.EARLIEST_POSSIBLE_DATE, values.get(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD));
		assertEquals("Event date end year too restrictive?", LuceneSearchConstants.LATEST_POSSIBLE_DATE, values.get(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD));
	}
	
	public void testExtractLeafDocuments() throws Exception
	{
		indexBulletin1And2();
		
		LuceneBulletinSearcher searcher = (LuceneBulletinSearcher)openBulletinSearcher();
		assertEquals("not 2 leafs?", 2, searcher.getAllLeafUids().size());
		

		HashMap fields = new HashMap();
		fields.put(RESULT_BASIC_QUERY_KEY, "Lunch");								
		Results leafs = searcher.search( fields);
		Vector leafUids = new Vector();
		for(int i=0; i < leafs.getCount(); ++i)
			leafUids.add(leafs.getBulletinInfo(i).getBulletinId());

		assertEquals(2, leafUids.size());
		assertContains("id1 not a leaf?", bulletinId1, leafUids);
		assertContains("id2 not a leaf?", bulletinId2, leafUids);
		searcher.close();
	}

	public void testNewSearcherWithNoIndexDirectory() throws Exception
	{
		deleteIndexDir();
		BulletinSearcher searcher = openBulletinSearcher();
		searcher.close();
	}
	
	
	public void testLookup() throws Exception
	{
		indexBulletin1();
		BulletinSearcher searcher = openBulletinSearcher();

		try 
		{
			BulletinInfo found = searcher.lookup(bulletinId1);
			assertNotNull("Didn't find indexed bulletin", found);
			assertEquals("Didn't find the fdp id?", fdp1.getUniversalId(), found.getFieldDataPacketUId());
		} 
		finally 
		{
			searcher.close();
		}
		
	}
	
	public void testIndexAndSearch() throws Exception
	{
		indexBulletin1();

		Results foundAuthors = simpleSearch(fdp1.get(SEARCH_AUTHOR_INDEX_FIELD));
		Assert.assertEquals(1, foundAuthors.getCount());
		
		Results foundKeywords = simpleSearch(fdp1.get(SEARCH_KEYWORDS_INDEX_FIELD));
		Assert.assertEquals(1,foundKeywords.getCount());
		
		Results foundDetails = simpleSearch(fdp1.get(SEARCH_DETAILS_INDEX_FIELD));
		Assert.assertEquals(1, foundDetails.getCount());
	}
	
	public void testSearchForWords() throws Exception
	{
		indexBulletin1();
		
		verifyWordFoundCount(1, "lunch");
		verifyWordFoundCount(0, "blowout");
		verifyWordFoundCount(1, "2nd");
		verifyWordFoundCount(0, "nd");
		verifyWordFoundCount(0, "2");
	}
	
	public void testSearchForeignWords() throws Exception
	{
		indexBulletinForeign();
		verifyWordFoundCount(1, "ni" + UnicodeConstants.TILDE_N_LOWER + "os");
		verifyWordFoundCount(1, "b" + UnicodeConstants.ACCENT_E_LOWER);		
		verifyWordFoundCount(1, "b" + UnicodeConstants.ACCENT_E_UPPER);		
	}
	
	private void verifyWordFoundCount(int expectedCount, String searchWord) throws Exception, BulletinIndexException
	{
		Results foundWord = simpleSearch(searchWord);
		Assert.assertEquals(expectedCount, foundWord.getCount());
	}

	private Results simpleSearch(String searchValue) throws Exception
	{
		HashMap fields = new HashMap();			
		fields.put(RESULT_BASIC_QUERY_KEY, searchValue );
		BulletinSearcher searcher = openBulletinSearcher();
		try 
		{
			Results results = searcher.search(fields);
			return results;
		} 
		finally 
		{
			searcher.close();
		}
	}

	public void testHistory() throws Exception
	{
		indexBulletin1And2();
		BulletinSearcher searcher = openBulletinSearcher();
		try
		{
			BulletinInfo noHistory = searcher.lookup(bulletinId1);
			assertNotNull("didn't find 1?", noHistory);
			assertEquals("1", noHistory.getVersion());

			BulletinInfo hasHistory = searcher.lookup(bulletinId2);
			assertNotNull("didn't find 2?", hasHistory);
			assertEquals("2", hasHistory.getVersion());
		}
		finally
		{
			searcher.close();
		}
	}
	
	public void testReconstructFieldDataPacket()  throws Exception
	{
		indexBulletin1();
		BulletinSearcher searcher = openBulletinSearcher();
		try {
			BulletinInfo found = searcher.lookup(bulletinId1);
			Assert.assertNotNull(
				"Didn't find indexed bulletin", 
				found);
			
			AttachmentProxy[] origProxies = fdp1.getAttachments();
			List foundAttachments = found.getAttachments();
			Assert.assertEquals(
				origProxies.length, foundAttachments.size());
			for (int i = 0; i < origProxies.length; i++) {
				Assert.assertEquals(
					origProxies[i].getUniversalId().getLocalId(), 
					((AttachmentInfo) foundAttachments.get(i)).getLocalId());	
				Assert.assertEquals(
					origProxies[i].getLabel(), 
					((AttachmentInfo) foundAttachments.get(i)).getLabel());
			}
			
			Assert.assertEquals(
				bulletinId1, found.getBulletinId());
			Collection fields = BulletinField.getSearchableFields();
			for (Iterator iter = fields.iterator(); iter.hasNext();) 
			{
				BulletinField field = (BulletinField) iter.next();
				if(field.isDateRangeField())
					Assert.assertEquals(
						fdp1.get(field.getXmlId()), 
						found.get(field.getIndexId()+"-start"));
					
				else
					Assert.assertEquals(
						fdp1.get(field.getXmlId()), 
						found.get(field.getIndexId()));
			}
		} 
		finally 
		{
			searcher.close();
		}
	}
	
	public void testInterleavedAccess()  throws Exception
	{
		BulletinSearcher searcher = null;
		BulletinIndexException closeException = null;
		
		try {
			searcher = openBulletinSearcher();
		} finally {
			if (indexer != null) {
				try {
					indexer.close();
				} catch (BulletinIndexException e) {
					closeException = e;
				}
			}
			if (searcher != null) {
				try {
					searcher.close();
				} catch (BulletinIndexException e) {
					closeException = e;
				}
			}
		}
		if (closeException != null) {
			throw closeException;
		}
			
		
	}
	
	public void testSearchResultsAfterClose() throws Exception
	{
		indexBulletin1();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;
		try {
			HashMap fields = new HashMap();			
			fields.put(RESULT_BASIC_QUERY_KEY, fdp1.get(SEARCH_AUTHOR_INDEX_FIELD) );
			results = searcher.search(fields);
		} finally {
			searcher.close();
		}
		
		results.getBulletinInfo(0);
	}
	
	public void testSearchAllFields() throws Exception
	{
		indexBulletin1();
			
		verifyHitCount("author", 1, fdp1.get(BulletinField.SEARCH_AUTHOR_INDEX_FIELD));
		verifyHitCount("details", 1, fdp1.get(BulletinField.SEARCH_DETAILS_INDEX_FIELD));
		verifyHitCount("keyword", 1, fdp1.get(BulletinField.SEARCH_KEYWORDS_INDEX_FIELD));
		verifyHitCount("location", 1, fdp1.get(BulletinField.SEARCH_LOCATION_INDEX_FIELD));
		verifyHitCount("summary", 1, fdp1.get(BulletinField.SEARCH_SUMMARY_INDEX_FIELD));
		verifyHitCount("title", 1, fdp1.get(BulletinField.SEARCH_TITLE_INDEX_FIELD));
		verifyHitCount("Lunch", 1, "Lunch");
		verifyHitCount("Luch", 0, "Luch");
		verifyHitCount("Attachment Label", 1, "Eggs.gif");
		verifyHitCount("Custom Field", 1, "custom");
	}
	
	private void verifyHitCount(String field, int expectedCount, String searchFor) throws Exception
	{
		HashMap fields = new HashMap();
		fields.put(RESULT_BASIC_QUERY_KEY, searchFor);								
		BulletinSearcher searcher = openBulletinSearcher();
		try
		{
			Results results = searcher.search(fields);							
			assertEquals("wrong hit count for " + field, expectedCount, results.getCount());
		}
		finally 
		{
			searcher.close();
		}
	}

	public void testSearchForStopWords() throws Exception
	{
		indexBulletin1();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;
		try 
		{			
			HashMap fields = new HashMap();	
			fields.put(RESULT_BASIC_QUERY_KEY, "for");								
					
			results = searcher.search(fields);
			assertEquals("Should have found 1 result for stopword 'for'", 1, results.getCount());
		} 
		finally 
		{
			searcher.close();
		}
	}

//	public void testSearchForWildCards() throws Exception
//	{
//		UniversalId bulletinId1 = UniversalId.createDummyUniversalId();
//		FieldDataPacket fdp1 	= generateSampleData(bulletinId1);		
//		UniversalId bulletinId2 = UniversalId.createDummyUniversalId();
//		FieldDataPacket fdp2 	= generateSampleFlexiData(bulletinId2);		
//		BulletinIndexer indexer = openBulletinIndexer();
//		try 
//		{
//			indexer.clearIndex();
//			indexer.indexFieldData(bulletinId1, fdp1);
//			indexer.indexFieldData(bulletinId2, fdp2);
//		} 
//		finally 
//		{
//			indexer.close();
//		}
//		
//		BulletinSearcher searcher = openBulletinSearcher();
//		Results results = null;
//		try 
//		{
//			HashMap fields = new HashMap();
//			fields.put(SEARCH_AUTHOR_INDEX_FIELD, fdp1.get(BulletinField.SEARCH_AUTHOR_INDEX_FIELD));				
//			fields.put(SEARCH_DETAILS_INDEX_FIELD, fdp1.get(BulletinField.SEARCH_DETAILS_INDEX_FIELD));
//			fields.put(SEARCH_KEYWORDS_INDEX_FIELD, fdp1.get(BulletinField.SEARCH_KEYWORDS_INDEX_FIELD));
//			fields.put(SEARCH_LOCATION_INDEX_FIELD, fdp1.get(BulletinField.SEARCH_LOCATION_INDEX_FIELD));
//			fields.put(SEARCH_SUMMARY_INDEX_FIELD, fdp1.get(BulletinField.SEARCH_SUMMARY_INDEX_FIELD));
//			fields.put(SEARCH_TITLE_INDEX_FIELD, fdp1.get(BulletinField.SEARCH_TITLE_INDEX_FIELD));
//		
//			fields.put(RESULT_BASIC_QUERY_KEY, "lun??");						
//			results = searcher.search(fields);
//			assertEquals("Should have found 2 result lun??", 2, results.getCount());
//			
//			
//			fields.remove(RESULT_BASIC_QUERY_KEY);
//			fields.put(RESULT_BASIC_QUERY_KEY, "sal*");	
//			results = searcher.search(fields);
//			assertEquals("Should have found 2 result sal* salad and salad2", 2, results.getCount());
//			
//			
//			fields.remove(RESULT_BASIC_QUERY_KEY);
//			fields.put(RESULT_BASIC_QUERY_KEY, "sa?ad");	
//			results = searcher.search(fields);		
//			assertEquals("Should have found 1 result sa?ad just salad", 1 , results.getCount());
//			
//			
///*			results = searcher.search(null, "");
//			assertEquals("Should have found 2 result for nothing entered", 2, results.getCount());
//			results = searcher.search(null, null);
//			assertEquals("Should have found 2 result for null entered", 2, results.getCount());
//			results = searcher.search(null, "*");
//			assertEquals("Should have found 2 result for * entered", 2, results.getCount());
//			results = searcher.search(null, "?");
//			assertEquals("Should have found 2 result for ? entered", 2, results.getCount());
//*/		}
//		finally 
//		{
//			searcher.close();
//		}
//	}

	public void testSearchForLanguageReturned() throws Exception
	{
		indexBulletin1();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;
		try 
		{
			HashMap fields = new HashMap();
			fields.put(RESULT_BASIC_QUERY_KEY, "ate");						
			results = searcher.search(fields);
			assertEquals("Should have found 1 result en", 1, results.getCount());
			BulletinInfo info = results.getBulletinInfo(0);
			assertEquals("The Language returned not correct?", "en", info.get(SEARCH_LANGUAGE_INDEX_FIELD));
		}
		finally 
		{
			searcher.close();
		}
	}
//TODO remove this comment once we can index and search for -other- languages
/*	public void testSearchForOtherLanguage() throws Exception
	{
		UniversalId bulletinId1 = UniversalId.createDummyUniversalId();
		FieldDataPacket fdp1 	= generateOtherLanguageData(bulletinId1);		
		BulletinIndexer indexer = openBulletinIndexer();
		try 
		{
			indexer.clearIndex();
			indexer.indexFieldData(bulletinId1, fdp1);
		} 
		finally 
		{
			indexer.close();
		}
		
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;
		try 
		{
			String defaultStartDate	= "1970-01-01";
			String todayDate 		= "2003-09-24";
			
			HashMap fields = new HashMap();
			//For a complex search we need the event date added to the search or nothing is returned
			fields.put(SEARCH_LANGUAGE_INDEX_FIELD, "?");
			fields.put(BulletinField.SEARCH_EVENT_START_DATE_INDEX_FIELD, defaultStartDate);
			fields.put(BulletinField.SEARCH_EVENT_END_DATE_INDEX_FIELD, todayDate);		

			results = searcher.search(fields);
			assertEquals("Should have found 1 result ?", 1, results.getCount());
			BulletinInfo info = results.getBulletinInfo(0);
			assertEquals("The Language returned not correct?", "?", info.get(SEARCH_LANGUAGE_INDEX_FIELD));
		}
		finally 
		{
			searcher.close();
		}
	}
	
*/	
	public void testSearchEmptyField() throws Exception
	{
		indexBulletin2();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;
		try 
		{
			HashMap fields = new HashMap();	
			fields.put(RESULT_BASIC_QUERY_KEY, "Chuck");		
			
			results = searcher.search(fields);
			assertEquals("Should have found 1 result Chuck", 1, results.getCount());
			
			
			BulletinInfo info =results.getBulletinInfo(0);
			assertNotNull("Bulletin Info null?", info);
			assertNotNull("Sumary should not be null",info.get(SEARCH_SUMMARY_INDEX_FIELD));
			assertNotNull("Location should not be  null",info.get(SEARCH_LOCATION_INDEX_FIELD));
			assertEquals("Location should be ''", "", info.get(SEARCH_LOCATION_INDEX_FIELD));
		}
		finally 
		{
			searcher.close();
		}
	}
	
	public void testLuceneSearchQueries() throws Exception
	{
		indexBulletin1And2();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;				
		
		try 
		{		
			HashMap fields = new HashMap();				
			String query = "-(lunch)"+ " AND \"What's for\"";			
			fields.put(RESULT_BASIC_QUERY_KEY,  query);		
			assertEquals("-(lunch) AND \"What's for\"", query);				
			results = searcher.search(fields);					
			assertEquals("Combine without these words and exactphrase? ", 0, results.getCount());	
			
			query = "+(lunch)"+ " AND \"What's for\"";
			assertEquals("+(lunch) AND \"What's for\"", query);
			fields.remove(RESULT_BASIC_QUERY_KEY);	
			fields.put(RESULT_BASIC_QUERY_KEY,  query);	
			results = searcher.search(fields);	
			assertEquals("Combine witt these words and exactphrase? ", 1, results.getCount());	
			
			query = "+(Francisco) AND +(Sample)";
			fields.remove(RESULT_BASIC_QUERY_KEY);	
			fields.put(RESULT_BASIC_QUERY_KEY,  query);	
			results = searcher.search(fields);	
			assertEquals("and across multiple fields failed? ", 1, results.getCount());	
		}
		finally 
		{
			searcher.close();
		}
	}
	
	public void testAdvancedSearchEventDateOnly() throws Exception
	{
		indexBulletin1And2();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;				
		
		try 
		{
			String startDate 	= "2003-08-01";
			String endDate 	= "2003-08-25";
		
			HashMap fields = new HashMap();
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, startDate);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, endDate);
			
			results = searcher.search(fields);
			assertEquals("Should have found 1 match? ", 1, results.getCount());			
		}
		finally 
		{
			searcher.close();
		}
	}	
	
	public void testAdvancedSearchCombineEventDateAndBulletineField() throws Exception
	{
		indexBulletin1And2();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;				
		
		try 
		{
			String startDate 	= "2003-08-01";
			String endDate 	= "2003-08-22";
		
			HashMap fields = new HashMap();
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, startDate);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, endDate);
			fields.put(SearchResultConstants.RESULT_FIELDS_KEY, BulletinField.SEARCH_TITLE_INDEX_FIELD);
			fields.put(ANYWORD_TAG, "lunch");
			
			results = searcher.search(fields);
			assertEquals("Combine search for eventdate and field? ", 1, results.getCount());
		}
		finally 
		{
			searcher.close();
		}
	}
	
	public void testAdvancedSearchCombineEventDateAndEntryDate() throws Exception
	{
		indexBulletin1And2();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;				
		
		try 
		{
			String startDate		= "2003-08-01";
			String endDate 		= "2003-08-22";
			String defaultDate 	= "1970-01-01";			
			String entryStartDate = "2003-05-22";
			String nearToday = "2003-10-03";		
		
			HashMap fields = new HashMap();
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, defaultDate);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, nearToday);
			fields.put(BulletinField.SEARCH_ENTRY_DATE_INDEX_FIELD, entryStartDate);
			
			results = searcher.search(fields);
			assertEquals("search for entry date only? ", 1, results.getCount());
			
			fields.remove(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD);
			fields.remove(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, startDate);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, endDate);			
			
			results = searcher.search(fields);
			assertEquals("Combine search for eventdate and entry date? ", 1, results.getCount());
		}
		finally 
		{
			searcher.close();
		}
	}
	
	public void testAdvancedSearchCombineEventDateAndBulletineFieldAndLanguage() throws Exception
	{
		indexBulletin1And2();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;				
		
		try 
		{
			String startDate 			= "2003-08-01";
			String endDate 			= "2003-08-22";			
			String defaultStartDate	= "1970-01-01";
			String defaultEndDate		= "2003-09-24";
				
			HashMap fields = new HashMap();
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, defaultStartDate);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, defaultEndDate);		
		
			fields.put(BulletinField.SEARCH_LANGUAGE_INDEX_FIELD, "es");		
			results = searcher.search(fields);			
			assertEquals("search laguage with default event date? ", 1, results.getCount());
				
			fields = new HashMap();			
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, startDate);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, endDate);
			fields.put(SearchResultConstants.RESULT_FIELDS_KEY, BulletinField.SEARCH_TITLE_INDEX_FIELD);
			fields.put(BulletinField.SEARCH_LANGUAGE_INDEX_FIELD, "en");
			fields.put(ANYWORD_TAG, "lunch");
			
			results = searcher.search(fields);
			assertEquals("Combine search for eventdate, field, and laguage? ", 0, results.getCount());
			
			fields.remove(SEARCH_LANGUAGE_INDEX_FIELD);
			fields.put(BulletinField.SEARCH_LANGUAGE_INDEX_FIELD, "fr");
			results = searcher.search(fields);
			assertEquals("Combine search for eventdate, bulletin field, and language (not match)? ", 0, results.getCount());			
						
		}
		finally 
		{
			searcher.close();
		}
	}
	
	public void testAdvancedSearchCombineEventDateAndBulletineFieldAndLanguageAndEntryDate() throws Exception
	{
		indexBulletin1And2();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;				
		
		try 
		{
			String startDate 		= "2003-08-01";
			String endDate 			= "2003-08-22";			
			String defaultStartDate = "1970-01-01";		
			
			MultiCalendar today = MultiCalendar.createFromGregorianYearMonthDay(2003, 9, 24);
			SearchParameters.todaysDateUsedForTesting = today;

			String pastWeek = SearchParameters.getEntryDate(ENTRY_PAST_WEEK_DAYS_TAG);
			String pastMonth = SearchParameters.getEntryDate(ENTRY_PAST_MONTH_DAYS_TAG);
			String past3Month = SearchParameters.getEntryDate(ENTRY_PAST_3_MONTH_DAYS_TAG);
			String past6Month = SearchParameters.getEntryDate(ENTRY_PAST_6_MONTH_DAYS_TAG);
			String pastYear = SearchParameters.getEntryDate(ENTRY_PAST_YEAR_DAYS_TAG);
		
			HashMap fields = new HashMap();
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, defaultStartDate);
			
			//2003-05-11 and 2003-08-30
			fields.put(BulletinField.SEARCH_ENTRY_DATE_INDEX_FIELD, pastWeek);		
			results = searcher.search(fields);			
			assertEquals("search for entry date submitted in past 1 week? ", 0, results.getCount());
						
			fields.remove(SEARCH_ENTRY_DATE_INDEX_FIELD);
			fields.put(SEARCH_ENTRY_DATE_INDEX_FIELD, pastMonth);
			results = searcher.search(fields);			
			assertEquals("search for entry date submitted in past 1 month? ", 1, results.getCount());
			
			fields.remove(SEARCH_ENTRY_DATE_INDEX_FIELD);
			fields.put(SEARCH_ENTRY_DATE_INDEX_FIELD, past3Month);
			results = searcher.search(fields);			
			assertEquals("search for entry date submitted in past 3 month? ", 1, results.getCount());
			
			fields.remove(SEARCH_ENTRY_DATE_INDEX_FIELD);
			fields.put(SEARCH_ENTRY_DATE_INDEX_FIELD, past6Month);
			results = searcher.search(fields);			
			assertEquals("search for entry date submitted in past 6 month? ", 2, results.getCount());
			
			fields.remove(SEARCH_ENTRY_DATE_INDEX_FIELD);
			fields.put(SEARCH_ENTRY_DATE_INDEX_FIELD, pastYear);
			results = searcher.search(fields);			
			assertEquals("search for entry date submitted in past 1 year? ", 2, results.getCount());
			
									
			fields = new HashMap();			
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, startDate);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, endDate);
			fields.put(SearchResultConstants.RESULT_FIELDS_KEY, BulletinField.SEARCH_TITLE_INDEX_FIELD);			
			fields.put(BulletinField.SEARCH_LANGUAGE_INDEX_FIELD, "es");
			fields.put(SEARCH_ENTRY_DATE_INDEX_FIELD, past3Month);
			fields.put(ANYWORD_TAG, "lunch");
			
			results = searcher.search(fields);
			assertEquals("Combine search for eventdate, field, laguage, and event date? ", 1, results.getCount());
								
		}
		finally 
		{
			searcher.close();
		}
	}
	
	public void testAdvancedSearchCombineEventDateAndFilterWords() throws Exception
	{
		indexBulletin1And2();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;				
		
		try 
		{
			String defaultDate 	= "1970-01-01";
			String defaultEndDate = "2004-01-01";			
		
			HashMap fields = new HashMap();
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, defaultDate);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, defaultEndDate);
			fields.put(RESULT_FIELDS_KEY, IN_ALL_FIELDS);
			
			SearchParameters.FormatterForAllWordsSearch d = 
				new SearchParameters.FormatterForAllWordsSearch();
			SearchParameters.FormatterForExactPhraseSearch ed = 
				new SearchParameters.FormatterForExactPhraseSearch();				

			String query = d.getFormattedString("root sandwich");						
			fields.put(THESE_WORD_TAG, query);
			results = searcher.search(fields);
			assertEquals("search for all of these words? ", 2, results.getCount());
						
			query = d.getFormattedString("Paul");	
			clear4Fields(fields);
			fields.put(THESE_WORD_TAG, query);
			results = searcher.search(fields);
			assertEquals("search for all of these words? ", 1, results.getCount());
										
			query = ed.getFormattedString("ZZZ for 2nd Lunch?");
			clear4Fields(fields);		
			fields.put(EXACTPHRASE_TAG, query);			
			results = searcher.search(fields);
			assertEquals("search for exact phrase? ", 1, results.getCount());
						
			clear4Fields(fields);
			query = ed.getFormattedString("for lunch.");		
			fields.put(EXACTPHRASE_TAG, query);
			
			results = searcher.search(fields);
			assertEquals("search for exact phrase? ", 2, results.getCount());											
		}
		finally 
		{
			searcher.close();
		}
	}
	
	public void test4FieldsQuery() throws Exception
	{
		indexBulletin1And2();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;				
		
		try 
		{
			String defaultDate 	= "1970-01-01";
			String defaultEndDate = "2004-01-01";			
		
			HashMap fields = new HashMap();
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, defaultDate);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, defaultEndDate);
			fields.put(RESULT_FIELDS_KEY, IN_ALL_FIELDS);
			
			SearchParameters.FormatterForAllWordsSearch d = 
				new SearchParameters.FormatterForAllWordsSearch();
			SearchParameters.FormatterForExactPhraseSearch ed = 
				new SearchParameters.FormatterForExactPhraseSearch();	
			
			//combined these words and exactphrase
			String query = d.getFormattedString("root sandwich");						
			fields.put(THESE_WORD_TAG, query);		
						
			query = ed.getFormattedString("Paul");				
			fields.put(EXACTPHRASE_TAG, query);
			
			results = searcher.search(fields);
			assertEquals("search for these words and exactphrase? ", 1, results.getCount());		
			
			clear4Fields(fields);
			//test again with all match
			query = d.getFormattedString("root sandwich");						
			fields.put(THESE_WORD_TAG, query);		
						
			query = ed.getFormattedString("Today");				
			fields.put(EXACTPHRASE_TAG, query);
			
			results = searcher.search(fields);
			assertEquals("search for these words and exactphrase? ", 2, results.getCount());											
										
		}
		finally 
		{
			searcher.close();
		}
	}
	
	private void clear4Fields(HashMap fields)
	{
		fields.remove(ANYWORD_TAG);
		fields.remove(EXACTPHRASE_TAG);	
		fields.remove(THESE_WORD_TAG);
		fields.remove(WITHOUTWORDS_TAG);			
	}
	
	public void testForeignCharsQuery() throws Exception
	{
		indexBulletinForeign();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;				
		
		try 
		{
			String defaultDate 	= "1970-01-01";
			String defaultEndDate = "2004-01-01";			
			
			HashMap fields = new HashMap();
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, defaultDate);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, defaultEndDate);
			fields.put(RESULT_FIELDS_KEY, IN_ALL_FIELDS);
			
			SearchParameters.FormatterForAllWordsSearch d = 
				new SearchParameters.FormatterForAllWordsSearch();
			SearchParameters.FormatterForExactPhraseSearch ed = 
				new SearchParameters.FormatterForExactPhraseSearch();	
			
			//combined these words and exactphrase
			String query = d.getFormattedString("ni" + UnicodeConstants.TILDE_N_LOWER + "os");						
			fields.put(THESE_WORD_TAG, query);		
			
			query = ed.getFormattedString("ni" + UnicodeConstants.TILDE_N_LOWER + "os");				
			fields.put(EXACTPHRASE_TAG, query);
			
			results = searcher.search(fields);
			assertEquals("search for foreign char not found? ", 1, results.getCount());		
			clear4Fields(fields);
			//test again with all match
			query = d.getFormattedString("ninos");						
			fields.put(THESE_WORD_TAG, query);		
			
			query = ed.getFormattedString("ninos");				
			fields.put(EXACTPHRASE_TAG, query);
			
			//TODO this will change once "ninos" should find the real spanish spelling
			results = searcher.search(fields);
			assertEquals("search for ninos when there is a ni" + UnicodeConstants.TILDE_N_LOWER + "os", 0, results.getCount());											
			
		}
		finally 
		{
			searcher.close();
		}
	}
	
	public void testAdvancedSearchSortByTitle() throws Exception
	{
		indexBulletin1And2();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;				
		
		try 
		{
			String defaultDate 	= "1970-01-01";
			String defaultEndDate = "2004-01-01";
			
			HashMap fields = new HashMap();			
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, defaultDate);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, defaultEndDate);
			fields.put(RESULT_FIELDS_KEY, IN_ALL_FIELDS);
			fields.put(RESULT_SORTBY_KEY, SEARCH_TITLE_INDEX_FIELD);			
			fields.put(ANYWORD_TAG, "lunch");			
			
			results = searcher.search(fields);
			assertEquals("Should have found 2 matches? ", 2, results.getCount());
			
			int count = results.getCount();												
			ArrayList list = new ArrayList();
			for (int i = 0; i < count; i++)
			{
				BulletinInfo bulletin = results.getBulletinInfo(i);					
				list.add(bulletin);
			}

			SearchResults.sortBulletins(list, SEARCH_TITLE_INDEX_FIELD);
			
			String title1 = ((BulletinInfo)list.get(0)).get(SEARCH_TITLE_INDEX_FIELD);
			String title2 = ((BulletinInfo)list.get(1)).get(SEARCH_TITLE_INDEX_FIELD);
						
			assertEquals(fdp2.get(BulletinField.SEARCH_TITLE_INDEX_FIELD), title1);
			assertEquals(fdp1.get(BulletinField.SEARCH_TITLE_INDEX_FIELD), title2);
												
		}
		finally 
		{
			searcher.close();
		}
	}				
		
	public void testAdvancedSearchSortByEventDate() throws Exception
	{
		indexBulletin1And2();
		BulletinSearcher searcher = openBulletinSearcher();
		Results results = null;				
		
		try 
		{
			String defaultDate 	= "1970-01-01";
			String defaultEndDate = "2004-01-01";
			
			HashMap fields = new HashMap();			
			fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, defaultDate);
			fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, defaultEndDate);
			fields.put(RESULT_FIELDS_KEY, IN_ALL_FIELDS);
			fields.put(RESULT_SORTBY_KEY, SEARCH_EVENT_DATE_INDEX_FIELD);			
			fields.put(ANYWORD_TAG, "lunch");			
			
			results = searcher.search(fields);
			assertEquals("Should have found 2 matches? ", 2, results.getCount());
			
			int count = results.getCount();												
			ArrayList list = new ArrayList();
			for (int i = 0; i < count; i++)
			{
				BulletinInfo bulletin = results.getBulletinInfo(i);					
				list.add(bulletin);
			}
			
			SearchResults.sortBulletins(list, SEARCH_EVENT_DATE_INDEX_FIELD);
		
			String eventStartDate1 = ((BulletinInfo)list.get(0)).get(SEARCH_EVENT_DATE_INDEX_FIELD+"-start");
			String eventStartDate2 = ((BulletinInfo)list.get(1)).get(SEARCH_EVENT_DATE_INDEX_FIELD+"-start");
						
			MiniLocalization localization = new MiniLocalization();
			assertEquals(fdp1.get(BulletinField.SEARCH_EVENT_DATE_INDEX_FIELD), eventStartDate1);
			String startDate2 = DateUtilities.getStartDateRange(fdp2.get(BulletinField.SEARCH_EVENT_DATE_INDEX_FIELD), localization);
			assertEquals(startDate2, eventStartDate2);
												
		}
		finally 
		{
			searcher.close();
		}
	}
	
	private void indexBulletin1() throws Exception
	{
		try 
		{
			indexer.indexFieldData(bulletinId1, fdp1, new BulletinHistory());
		} 
		finally 
		{
			indexer.close();
		}
	}

	private void indexBulletin2() throws Exception
	{
		try 
		{
			indexer.indexFieldData(oldVersionId, fdp2, new BulletinHistory());
			indexer.indexFieldData(bulletinId2, fdp2, history);
		} 
		finally 
		{
			indexer.close();
		}
	}
	
	private void indexBulletin1And2() throws Exception
	{
		try 
		{
			indexer.indexFieldData(bulletinId1, fdp1, new BulletinHistory());
			indexer.indexFieldData(oldVersionId, fdp2, new BulletinHistory());
			indexer.indexFieldData(bulletinId2, fdp2, history);
		} 
		finally 
		{
			indexer.close();
		}
	}
	
	private void indexBulletinForeign() throws BulletinIndexException
	{
		try 
		{
			indexer.indexFieldData(bulletinIdForeign, fdpForeign, new BulletinHistory());
		} 
		finally 
		{
			indexer.close();
		}
	}

	UniversalId bulletinId1;
	UniversalId bulletinId2;
	UniversalId bulletinIdForeign;
	UniversalId oldVersionId;
	FieldDataPacket fdp1;	
	FieldDataPacket fdp2;
	FieldDataPacket fdpForeign;
	BulletinHistory history;
	BulletinIndexer indexer;
}
