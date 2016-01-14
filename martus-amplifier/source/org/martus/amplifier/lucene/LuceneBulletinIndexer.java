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
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.search.BulletinField;
import org.martus.amplifier.search.BulletinIndexException;
import org.martus.amplifier.search.BulletinIndexer;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;

public class LuceneBulletinIndexer 
	implements BulletinIndexer, LuceneSearchConstants
{
	
	public LuceneBulletinIndexer(String baseDirName) 
		throws BulletinIndexException
	{
		indexDir = getIndexDir(baseDirName);
		try {
			createIndexIfNecessary(indexDir);
			writer = new IndexWriter(indexDir, getAnalyzer(), false);
		} catch (IOException e) {
			throw new BulletinIndexException(
				"Could not create LuceneBulletinIndexer", e);
		}		
	}
	
	public void close() throws BulletinIndexException
	{
		try {
			// TODO pdalbora 23-Apr-2003 -- Don't call this unnecessarily.
			writer.optimize();
		} catch (IOException e) {
			throw new BulletinIndexException("Unable to close the index", e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				throw new BulletinIndexException(
					"Unable to close the index", e);
			}		
		}		
	}
	
	public void clearIndex() throws BulletinIndexException
	{
		try {
			writer.close();
			writer = new IndexWriter(indexDir, ANALYZER, true);
		} catch (IOException e) {
			throw new BulletinIndexException("Unable to clear the index", e);
		}		
	}
	
	public void indexFieldData(UniversalId bulletinId, FieldDataPacket fdp, BulletinHistory history) 
		throws BulletinIndexException
	{
		Document doc = new Document();
		addBulletinId(doc, bulletinId);
		try
		{
			addFields(doc, fdp);
		}
		catch (IOException e1)
		{
			throw new BulletinIndexException(
				"Unable to index field data for " + bulletinId, e1);
		}
		addAttachmentIds(doc, fdp.getAttachments());
		addHistory(doc, history);
		addFieldDataPacketId(doc, fdp.getLocalId());
		
		try {
			writer.addDocument(doc);
		} catch (IOException e) {
			throw new BulletinIndexException(
				"Unable to index field data for " + bulletinId, e);
		}
	}
	
	public static boolean isIndexObsolete(File indexDir) throws IOException
	{
		if (!IndexReader.indexExists(indexDir)) 
			return false;

		File indexTypeFile = new File(indexDir, INDEX_TYPE_FILENAME);
		UnicodeReader reader = new UnicodeReader(indexTypeFile);
		try
		{
			String builtWithClassName = reader.readLine();
			return(!getAnalyzerName().equals(builtWithClassName));
		}
		finally
		{
			reader.close();
		}
	}
	
	/* package */ 
	static void createIndexIfNecessary(File indexDir)
		throws IOException
	{
		if (!IndexReader.indexExists(indexDir)) {
			IndexWriter writer = 
				new IndexWriter(indexDir, getAnalyzer(), true);
			writer.close();
			
			updateIndexTypeFile(indexDir);
		}
	}
	
	static void updateIndexTypeFile(File indexDir) throws IOException
	{
		File file = new File(indexDir, INDEX_TYPE_FILENAME);
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.writeln(getAnalyzerName());
		writer.close();
	}
	
	static String getAnalyzerName()
	{
		return ANALYZER.getClass().getName();
	}
	
	/* package */
	public static Analyzer getAnalyzer()
	{
		return ANALYZER;
	}
	
	/* package */
	public static File getIndexDir(String basePath)
		throws BulletinIndexException
	{
		File f = new File(basePath, INDEX_DIR_NAME);
		if (!f.exists() && !f.mkdirs()) {
			throw new BulletinIndexException(
				"Unable to create path: " + f);
		}
		return f;
	}
	
	private static void addBulletinId(Document doc, UniversalId bulletinId)
	{
		doc.add(Field.Keyword(
			BULLETIN_UNIVERSAL_ID_INDEX_FIELD, bulletinId.toString()));	
	}
	
	private static void addFields(Document doc, FieldDataPacket fdp) 
		throws BulletinIndexException, IOException 
	{
		Collection fields = BulletinField.getSearchableFields();
		for (Iterator iter = fields.iterator(); iter.hasNext();) 
		{
			BulletinField field = (BulletinField) iter.next();
			String value = fdp.get(field.getXmlId());
			if ((value != null) && (value.length() > 0)) 
			{
				addField(doc, field, value);
			}
			
		}
		
		StringBuffer allFieldData = new StringBuffer();
		FieldSpec[] specs = fdp.getFieldSpecs().asArray();
		for(int i=0; i < specs.length; ++i)
		{
			String tag = specs[i].getTag();
			String value = fdp.get(tag);
			allFieldData.append(ALL_FIELD_VALUE_SEPARATOR);
			allFieldData.append(value);
		}
		
		AttachmentProxy[] attachments = fdp.getAttachments();
		for(int i = 0; i < attachments.length; ++i)
		{
			allFieldData.append(ALL_FIELD_VALUE_SEPARATOR);
			allFieldData.append(attachments[i].getLabel());
		}
		
		doc.add(Field.Text(SearchResultConstants.IN_ALL_FIELDS, new String(allFieldData)));
	}
	
	private static void addField(Document doc, BulletinField field, String value)
		throws BulletinIndexException
	{
		if (field.isDateField()) 
		{
			doc.add(Field.Keyword(field.getIndexId(), value));
		}
		else if (field.isDateField())
		{
			convertDateToSearchableString(doc, field, value);
		}
		else if (field.isDateRangeField())
		{
			convertDateRangeToSearchableString(doc, field, value);
		}
		else 
		{
			doc.add(Field.Text(field.getIndexId(), value));
		}
	}
	
	private static void addAttachmentIds(
		Document doc, AttachmentProxy[] proxies)
	{
		if (proxies.length > 0) {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < proxies.length; i++) {
				AttachmentProxy proxy = proxies[i];
				buf.append(proxy.getUniversalId().getLocalId());
				buf.append(ATTACHMENT_LIST_SEPARATOR);
				buf.append(proxy.getLabel());
				buf.append(ATTACHMENT_LIST_SEPARATOR);
			}
			doc.add(Field.UnIndexed(
				ATTACHMENT_LIST_INDEX_FIELD, buf.toString()));
		}
	}
	
	private static void addHistory(Document doc, BulletinHistory history)
	{
		doc.add(Field.UnIndexed(HISTORY_INDEX_FIELD, history.toString()));
	}
	
	private static void addFieldDataPacketId(Document doc, String fieldDataPacketLocalId)
	{
		doc.add(Field.UnIndexed(FIELD_DATA_PACKET_LOCAL_ID_INDEX_FIELD, fieldDataPacketLocalId));		
	}
	
	private static void convertDateToSearchableString(Document doc, BulletinField field, String value) throws BulletinIndexException
	{
		MiniLocalization localization = MartusAmplifier.localization;
		String convertedDate = localization.convertStoredDateToDisplay(value);							
	
		doc.add(Field.Text(field.getIndexId(), convertedDate));				
	}
	
	private static void convertDateRangeToSearchableString(Document doc, BulletinField field, String value) throws BulletinIndexException
	{
		MiniLocalization localization = MartusAmplifier.localization;
		MartusFlexidate mfd = localization.createFlexidateFromStoredData(value);
	
		MultiCalendar beginDate = mfd.getBeginDate();
		String isoBeginDate = localization.convertStoredDateToDisplay(beginDate.toIsoDateString());
		if(beginDate.isUnknown())
			isoBeginDate = LuceneSearchConstants.UNKNOWN_DATE;
		doc.add(Field.Keyword(LuceneSearchConstants.SEARCH_EVENT_START_DATE_INDEX_FIELD, isoBeginDate)); 			

		MultiCalendar endDate = mfd.getEndDate();
		String isoEndDate = localization.convertStoredDateToDisplay(endDate.toIsoDateString());
		if(endDate.isUnknown())
			isoEndDate = LuceneSearchConstants.UNKNOWN_DATE;
		doc.add(Field.Keyword(LuceneSearchConstants.SEARCH_EVENT_END_DATE_INDEX_FIELD, isoEndDate));
					
		doc.add(Field.Text(field.getIndexId(), value));				
	}
	
	// NOTE: if the external behavior changes, this class MUST be renamed,
	// so the search engine can force the index to be rebuilt
	static class AlphanumericAnalyzer extends Analyzer
	{
		public TokenStream tokenStream(String fieldNameUNUSED, Reader reader)
		{
			return new AlphanumericTokenizer(reader);
		}
	}
	
	
	private File indexDir;
	private IndexWriter writer;
	private final static Analyzer ANALYZER = new AlphanumericAnalyzer();
	
	private static final String INDEX_DIR_NAME = "ampIndex";
	private static final String ALL_FIELD_VALUE_SEPARATOR = "    |    ";
	private static final String INDEX_TYPE_FILENAME = "indexType.txt";
}