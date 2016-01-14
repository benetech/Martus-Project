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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.search.BulletinCatalog;
import org.martus.amplifier.search.BulletinIndexException;
import org.martus.amplifier.search.BulletinInfo;
import org.martus.amplifier.search.BulletinSearcher;
import org.martus.amplifier.search.Results;
import org.martus.common.LoggerInterface;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;

public class LuceneBulletinSearcher implements BulletinSearcher
{
	public LuceneBulletinSearcher(String baseDirName, LoggerInterface loggerToUse) throws Exception
	{
		logger = loggerToUse;
		
		File indexDir = LuceneBulletinIndexer.getIndexDir(baseDirName);
		LuceneBulletinIndexer.createIndexIfNecessary(indexDir);
		searcher = new IndexSearcher(indexDir.getPath());
	}	
	
	public Results search(Map fields) throws Exception 
	{	
		if (isComplexSearch(fields))
			return getComplexSearchResults(fields);

		return getSimpleSearchResults(fields);
	}

	public BulletinInfo lookup(UniversalId bulletinId) throws Exception 
	{
		Results results = getSingleBulletinResults(bulletinId);

		int numResults = results.getCount();
		if (numResults == 0)
			return null;

		if (numResults == 1)
			return results.getBulletinInfo(0);

		String message = "Found more than one field data set for the same bulletin id: " +
				bulletinId + "; found " + numResults + " results";
		throw new BulletinCatalog.DuplicateBulletinException(message);
	}

	public void close() throws Exception
	{
		searcher.close();
	}

	private boolean isComplexSearch(Map fields)
	{
		String queryString = (String) fields.get(SearchResultConstants.RESULT_BASIC_QUERY_KEY);
		return (queryString == null);
	}

	private Results getSingleBulletinResults(UniversalId bulletinId) throws Exception
	{
		String fieldToSearch = LuceneSearchConstants.BULLETIN_UNIVERSAL_ID_INDEX_FIELD;
		Term term = new Term(fieldToSearch, bulletinId.toString());
		TermQuery query = new TermQuery(term);
		return new LuceneResults(getRawResults(query));
	}

	private Results getComplexSearchResults(Map fields)
		throws Exception, IOException
	{
		Query query = new QueryBuilder(fields).getQuery();
		return getResults(query);
	}

	private Results getSimpleSearchResults(Map fields)
		throws Exception, IOException
	{
		String queryString = (String) fields.get(SearchResultConstants.RESULT_BASIC_QUERY_KEY);
		Query query = buildSimpleSearchQuery(queryString);				
		return getResults(query);
	}
	
	private Query buildSimpleSearchQuery(String queryString) throws Exception
	{
		Query query = new QueryBuilder(queryString).getQuery();
		return query;
	}
	
	private Query buildAllBulletinsQuery() throws Exception
	{
		HashMap fields = getDefaultSearchValues();
		return new QueryBuilder(fields).getQuery();
	}

	static public HashMap getDefaultSearchValues()
	{
		HashMap fields = new HashMap();
		fields.put("anyWordsQuery", "");
		fields.put("sortBy", "entrydate");
		fields.put("exactPhraseQuery", "");
		fields.put("allWordsQuery", "");
		fields.put(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, LuceneSearchConstants.EARLIEST_POSSIBLE_DATE);
		fields.put("fields", "all");
		fields.put(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, LuceneSearchConstants.LATEST_POSSIBLE_DATE);
		fields.put(LuceneSearchConstants.SEARCH_UNKNOWN_DATES_FIELD, new Boolean(true));
		fields.put("entrydate", LuceneSearchConstants.EARLIEST_POSSIBLE_DATE);
		return fields;
	}

	private Results getResults(Query query) throws Exception
	{
		Vector matchingDocs = getRawResults(query);
		Vector matchingLeafs = extractLeafDocuments(matchingDocs);
		return new LuceneResults(matchingLeafs);
	}
	
	private Vector getRawResults(Query query) throws IOException
	{
		Hits hits = searcher.search(query);
		Vector docs = new Vector();
		for(int i=0; i < hits.length(); ++i)
		{
			Document doc = hits.doc(i);
			docs.add(doc);
		}
		return docs;
	}

	public Vector extractLeafDocuments(Vector docs) throws Exception
	{
		Vector results = new Vector();
		
		Vector leafs = getAllLeafUids();
		for(int i=0; i < docs.size(); ++i)
		{
			Document doc = (Document)docs.get(i);
			UniversalId uid = LuceneResults.getBulletinId(doc);
			if(leafs.contains(uid))
				results.add(doc);
		}
		
		logger.logDebug("Final Version Bulletins = " + leafs.size());
		logger.logDebug("All Bulletins = " + docs.size());
		
		return results;
	}

	public Vector getAllLeafUids() throws IOException, Exception, BulletinIndexException
	{
		Query query = buildAllBulletinsQuery();
		Hits allBulletins = searcher.search(query);
		Vector leafCandidates = extractAllUidsFromHits(allBulletins);
		
		for(int i=0; i < allBulletins.length(); ++i)
		{
			Document doc = allBulletins.doc(i);
			String accountId = LuceneResults.getBulletinId(doc).getAccountId();
			String historyString = doc.get(LuceneSearchConstants.HISTORY_INDEX_FIELD);
			if(historyString == null)
				continue;

			BulletinHistory history = BulletinHistory.createFromHistoryString(historyString);
			for(int version = 0; version < history.size(); ++version)
			{
				String localId = history.get(version);
				UniversalId nonLeafUid = UniversalId.createFromAccountAndLocalId(accountId, localId);
				leafCandidates.remove(nonLeafUid);
			}
		}
		
		return leafCandidates;
	}

	private Vector extractAllUidsFromHits(Hits allBulletins) throws IOException, BulletinIndexException
	{
		Vector leafs = new Vector();
		for(int i=0; i < allBulletins.length(); ++i)
		{
			Document doc = allBulletins.doc(i);
			UniversalId uid = LuceneResults.getBulletinId(doc);
			leafs.add(uid);
		}
		return leafs;
	}

	private IndexSearcher searcher;
	private LoggerInterface logger;
}
