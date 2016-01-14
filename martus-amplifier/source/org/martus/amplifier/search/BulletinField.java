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
package org.martus.amplifier.search;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.StandardFieldSpecs;

public class BulletinField implements BulletinConstants, SearchConstants
{
	public String getDisplayName()
	{
		return displayName;
	}
	
	public String getXmlId()
	{
		return xmlId;
	}
	
	public String getIndexId()
	{
		return indexId;
	}
	
	public boolean isLanguageField()
	{
		return (xmlId.equals(TAGLANGUAGE));
	}
	
	public boolean isTitleField()
	{
		return (xmlId.equals(TAGTITLE));
	}		

	public boolean isDateField()
	{
		return (StandardFieldSpecs.getStandardType(xmlId).isDate());
	}		
	
	public boolean isDateRangeField()
	{
		return (StandardFieldSpecs.getStandardType(xmlId).isDateRange());
	}	
		
	public static BulletinField getFieldByXmlId(String xmlId)
	{
		return (BulletinField) getFields().get(xmlId);
	}
	
	public static Collection getSearchableFields()
	{
		return getFields().values();
	}
	
	public static String[] getSearchableXmlIds()
	{
		return (String[]) getFields().keySet().toArray(new String[0]);
	}
	
	public static FieldSpec[] getDefaultSearchFieldSpecs()
	{
		String[] ids = getSearchableXmlIds();
		int length = ids.length;

		FieldSpec[] defaultSearchFieldSpecs = new FieldSpec[length];
		for(int i = 0; i < length; ++i)
		{
			String tag = ids[i].toString();
			FieldType type = StandardFieldSpecs.getStandardType(tag);
			defaultSearchFieldSpecs[i] = FieldSpec.createStandardField(tag, type);
		}
		return defaultSearchFieldSpecs;
	}	
	
	private BulletinField(String xmlId, String indexId, String displayName)
	{
		this.xmlId = xmlId;
		this.indexId = indexId;
		this.displayName = displayName;
	}
	
	private static void addField(String xmlId, String indexId, String displayName)
	{
		getFields().put(xmlId, new BulletinField(xmlId, indexId, displayName));
	}

	private static Map getFields()
	{
		if(fields == null)
			initializeFields();
		
		return fields;
	}

	private static void initializeFields()
	{
		fields = new LinkedHashMap();
	
		// NOTE paul 8-Apr-2003 -- The display names should at some
		// point be i18n'ed strings.
		addField(TAGAUTHOR, SEARCH_AUTHOR_INDEX_FIELD, "Author");
		addField(TAGKEYWORDS, SEARCH_KEYWORDS_INDEX_FIELD, "Keywords");
		addField(TAGTITLE, SEARCH_TITLE_INDEX_FIELD, "Title");
		addField(TAGEVENTDATE, SEARCH_EVENT_DATE_INDEX_FIELD, "Event Date");
		addField(TAGPUBLICINFO, SEARCH_DETAILS_INDEX_FIELD, "Details");
		addField(TAGSUMMARY, SEARCH_SUMMARY_INDEX_FIELD, "Summary");
		addField(TAGLOCATION, SEARCH_LOCATION_INDEX_FIELD, "Location");
		addField(TAGENTRYDATE, SEARCH_ENTRY_DATE_INDEX_FIELD, "Entry Date");
		addField(TAGLANGUAGE, SEARCH_LANGUAGE_INDEX_FIELD, "Language");
		addField(TAGORGANIZATION, SEARCH_ORGANIZATION_INDEX_FIELD, "Organization");	
	}
	
	private String xmlId;
	private String indexId;
	private String displayName;
	private static Map fields;
}
