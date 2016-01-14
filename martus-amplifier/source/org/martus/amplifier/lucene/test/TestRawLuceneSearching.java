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

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.martus.amplifier.lucene.LuceneBulletinIndexer;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;

public class TestRawLuceneSearching extends TestCaseEnhanced
{
	public TestRawLuceneSearching(String name)
	{
		super(name);
	}

	public void testRawLuceneSearches() throws Exception
	{
		File indexDir = createTempDirectory();
		
		String sampleId1 = "blister";
		String sampleDate1 = "1996-05-11";
		String sampleDetails1 = "This is a test with keywords fun";

		String sampleId2 = "pucker";
		String sampleDate2 = "2003-09-29";
		String sampleDetails2 = "More keywords that can be found fun";

		String sampleId3 = "snooty";
		String sampleDate3 = "1933-10-14";
		String sampleDetails3 = "This must be really old data! fun";

		boolean createIfNotThere = true;
		Analyzer analyzer = LuceneBulletinIndexer.getAnalyzer();
		IndexWriter writer = new IndexWriter(indexDir, analyzer, createIfNotThere);
		writeDocument(writer, sampleId1, sampleDate1, sampleDetails1);
		writeDocument(writer, sampleId2, sampleDate2, sampleDetails2);
		writeDocument(writer, sampleId3, sampleDate3, sampleDetails3);
		writer.close();
		
		IndexSearcher searcher = new IndexSearcher(indexDir.getPath());

		verifyLookupById(searcher, sampleId1);
		verifyLookupById(searcher, sampleId2);
		
		verifyTextSearch(searcher, "none", new String[] {});
		verifyTextSearch(searcher, "test", new String[] {sampleId1});
		verifyTextSearch(searcher, "keywords", new String[] {sampleId1, sampleId2});

		verifyTextSearch(searcher, "fun NOT keywords", new String[] {sampleId3});
		verifyTextSearch(searcher, "+fun-keywords", new String[] {sampleId3});
		
		verifyTextSearch(searcher, "-keywords", new String[] {});


		verifyDateRangeSearch(searcher, "1995-01-01", "1998-12-31", new String[] {sampleId1});
		verifyDateRangeSearch(searcher, "1900-01-01", "2037-12-31", 
					new String[] {sampleId1, sampleId2, sampleId3});

		searcher.close();
		
		DirectoryUtils.deleteEntireDirectoryTree(indexDir);
		assertFalse("didn't delete?", indexDir.exists());
	}
	
	private void verifyDateRangeSearch(IndexSearcher searcher, String beginDate, String endDate, String[] expectedIds)
		throws Exception
	{
		String searchFor = "[" + beginDate + " TO " + endDate + "]";
		String[] fields = {TAG_DATE};
		Analyzer analyzer = LuceneBulletinIndexer.getAnalyzer();
		Query query = MultiFieldQueryParser.parse(searchFor, fields, analyzer);
		Hits hits = searcher.search(query);
		assertEquals(expectedIds.length, hits.length());
		Vector foundIds = new Vector();
		for(int i=0; i < hits.length(); ++i)
		{
			Document foundDoc = hits.doc(i);
			foundIds.add(foundDoc.get(TAG_ID));
		}
	
		for(int i=0; i < expectedIds.length; ++i)
			assertContains("missing id?", expectedIds[i], foundIds);
	}


	private void verifyTextSearch(IndexSearcher searcher, String searchFor, String[] expectedIds)
		throws Exception
	{
		String[] fields = {TAG_DETAILS};
		Analyzer analyzer = LuceneBulletinIndexer.getAnalyzer();
		Query query = MultiFieldQueryParser.parse(searchFor, fields, analyzer);
		Hits hits = searcher.search(query);
		assertEquals(expectedIds.length, hits.length());
		Vector foundIds = new Vector();
		for(int i=0; i < hits.length(); ++i)
		{
			Document foundDoc = hits.doc(i);
			foundIds.add(foundDoc.get(TAG_ID));
		}

		for(int i=0; i < expectedIds.length; ++i)
			assertContains("missing id?", expectedIds[i], foundIds);
	}

	private void verifyLookupById(IndexSearcher searcher, String sampleId1)
		throws IOException
	{
		Term term = new Term(TAG_ID, sampleId1);
		Query idQuery = new TermQuery(term);
		Hits hitsId = searcher.search(idQuery);
		assertEquals(1, hitsId.length());
	}

	private void writeDocument(
		IndexWriter writer,
		String sampleId,
		String sampleDate,
		String sampleDetails)
		throws IOException
	{
		Document docToWrite = new Document();
		docToWrite.add(Field.Keyword(TAG_ID, sampleId));	
		docToWrite.add(Field.Keyword(TAG_DATE, sampleDate));
		docToWrite.add(Field.Text(TAG_DETAILS, sampleDetails));
		writer.addDocument(docToWrite);
	}
	
	static String TAG_ID = "id";
	static String TAG_DATE = "date";
	static String TAG_DETAILS = "details";
		

}
