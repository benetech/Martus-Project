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

public interface LuceneSearchConstants
{
	String BULLETIN_UNIVERSAL_ID_INDEX_FIELD = "bulletin_uid";
	String HISTORY_INDEX_FIELD = "$$$history";
	String ATTACHMENT_LIST_INDEX_FIELD = "attachments";
	String ATTACHMENT_LIST_SEPARATOR = "\n";	
	String FIELD_DATA_PACKET_LOCAL_ID_INDEX_FIELD = "field_data_packet_local_id";
	public static final String EARLIEST_POSSIBLE_DATE = "0000-00-00";
	public static final String LATEST_POSSIBLE_DATE = "9999-99-99";
	final String SEARCH_EVENT_START_DATE_INDEX_FIELD = "$$$eventStartDate";
	final String SEARCH_EVENT_END_DATE_INDEX_FIELD = "$$$eventEndDate";
	final String SEARCH_UNKNOWN_DATES_FIELD = "$$$includeUnknowns";
	
	public static final String UNKNOWN_DATE = "UNKNOWN";
}
