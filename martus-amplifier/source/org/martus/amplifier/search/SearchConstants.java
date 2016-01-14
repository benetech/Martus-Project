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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.martus.common.bulletin.BulletinConstants;

public interface SearchConstants
{
	// Index field ids
	final String SEARCH_AUTHOR_INDEX_FIELD = BulletinConstants.TAGAUTHOR;
	final String SEARCH_KEYWORDS_INDEX_FIELD = BulletinConstants.TAGKEYWORDS;
	final String SEARCH_TITLE_INDEX_FIELD = BulletinConstants.TAGTITLE;
	final String SEARCH_EVENT_DATE_INDEX_FIELD = BulletinConstants.TAGEVENTDATE;
	final String SEARCH_DETAILS_INDEX_FIELD = BulletinConstants.TAGPUBLICINFO;
	final String SEARCH_SUMMARY_INDEX_FIELD = BulletinConstants.TAGSUMMARY;
	final String SEARCH_LOCATION_INDEX_FIELD = BulletinConstants.TAGLOCATION;
	final String SEARCH_ENTRY_DATE_INDEX_FIELD = BulletinConstants.TAGENTRYDATE;
	final String SEARCH_LANGUAGE_INDEX_FIELD = BulletinConstants.TAGLANGUAGE;
	final String SEARCH_ORGANIZATION_INDEX_FIELD = BulletinConstants.TAGORGANIZATION;
	final String SEARCH_DATE_FORMAT_PATTERN = "yyyy-MM-dd";
	final DateFormat SEARCH_DATE_FORMAT = new SimpleDateFormat(SEARCH_DATE_FORMAT_PATTERN);

}
