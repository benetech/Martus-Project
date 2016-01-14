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

import java.util.Vector;

import org.martus.amplifier.search.SearchConstants;


public class FindBulletinsFields implements SearchConstants, SearchResultConstants
{
	public static Vector getFindWordFilterDisplayNames()
	{
		Vector filters = new Vector();
		
		filters.add(new ChoiceEntry(ANYWORD_TAG, ANYWORD_LABEL));
		filters.add(new ChoiceEntry(THESE_WORD_TAG, THESE_WORD_LABEL));
		filters.add(new ChoiceEntry(EXACTPHRASE_TAG, EXACTPHRASE_LABEL));		
//		filters.add(new ChoiceEntry(WITHOUTWORDS_LABEL, WITHOUTWORDS_KEY));

		return filters;	
	}
	
	public static Vector getFindEntryDatesDisplayNames()
	{
		Vector dates = new Vector();
		dates.add(new ChoiceEntry(ENTRY_ANYTIME_TAG, ENTRY_ANYTIME_LABEL));
		dates.add(new ChoiceEntry(ENTRY_PAST_WEEK_DAYS_TAG, ENTRY_PAST_WEEK_LABEL));
		dates.add(new ChoiceEntry(ENTRY_PAST_MONTH_DAYS_TAG, ENTRY_PAST_MONTH_LABEL));
		dates.add(new ChoiceEntry(ENTRY_PAST_3_MONTH_DAYS_TAG,ENTRY_PAST_3_MONTH_LABEL ));
		dates.add(new ChoiceEntry(ENTRY_PAST_6_MONTH_DAYS_TAG, ENTRY_PAST_6_MONTH_LABEL));
		dates.add(new ChoiceEntry(ENTRY_PAST_YEAR_DAYS_TAG, ENTYR_PAST_YEAR_LABEL));
		
		return dates;		
	}
	
	public static Vector getBulletinFieldDisplayNames()
	{
		Vector fields = new Vector();
		fields.add(new ChoiceEntry(IN_ALL_FIELDS, ANYWHERE_IN_BULLETIN_KEY));
		fields.add(new ChoiceEntry(SEARCH_TITLE_INDEX_FIELD, IN_TITLE_KEY));
		fields.add(new ChoiceEntry(SEARCH_KEYWORDS_INDEX_FIELD, IN_KEYWORDS_KEY));
		fields.add(new ChoiceEntry(SEARCH_SUMMARY_INDEX_FIELD,IN_SUMMARY_KEY ));
		fields.add(new ChoiceEntry(SEARCH_AUTHOR_INDEX_FIELD, IN_AUTHOR_KEY));
		fields.add(new ChoiceEntry(SEARCH_DETAILS_INDEX_FIELD, IN_DETAIL_KEY ));
		fields.add(new ChoiceEntry(SEARCH_LOCATION_INDEX_FIELD, IN_LOCATION_KEY ));
		fields.add(new ChoiceEntry(SEARCH_ORGANIZATION_INDEX_FIELD, IN_ORGANIZATION_KEY));
		
		return fields;
	}
	
	public static Vector getSortByFieldDisplayNames()
	{
		Vector fields = new Vector();
											
		fields.add(new ChoiceEntry(SEARCH_ENTRY_DATE_INDEX_FIELD, SORT_BY_ENTRYDATE_TAG));
		fields.add(new ChoiceEntry(SEARCH_TITLE_INDEX_FIELD, SORT_BY_TITLE_TAG));
		fields.add(new ChoiceEntry(SEARCH_AUTHOR_INDEX_FIELD, SORT_BY_AUTHOR_TAG));
		fields.add(new ChoiceEntry(SEARCH_EVENT_DATE_INDEX_FIELD, SORT_BY_EVENTDATE_TAG ));
		fields.add(new ChoiceEntry(SEARCH_LOCATION_INDEX_FIELD, SORT_BY_LOCATION_TAG ));
		fields.add(new ChoiceEntry(SEARCH_ORGANIZATION_INDEX_FIELD, SORT_BY_ORGANIZATION_TAG));
		
		return fields;
	}
	
	public static Vector getMonthFieldDisplayNames()
	{
		Vector fields = new Vector();
		for (int i=0;i< MONTH_NAMES.length;i++)	
			fields.add(new ChoiceEntry(new Integer(i).toString(), MONTH_NAMES[i]));
					
		return fields;
	}
	
	private static final String[] MONTH_NAMES = new String[] {
		"January", "February", "March", "April", "May", "June",
		"July", "August", "September", "October", "November", "December"
	};
	
}
