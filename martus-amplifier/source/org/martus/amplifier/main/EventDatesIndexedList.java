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

package org.martus.amplifier.main;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.MiniLocalization;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;


public class EventDatesIndexedList extends IndexedValuesList
{
	public EventDatesIndexedList(File fileToUse)
	{
		super(fileToUse);
	}
	
	public int getEarliestYear()
	{
		MiniLocalization localization = MartusAmplifier.localization;
		int earliestYear = getThisYear();
		Vector dateStrings = getIndexedValues();
		for(int i = 0; i < dateStrings.size(); ++i)
		{
			MartusFlexidate flex = localization.createFlexidateFromStoredData((String)dateStrings.get(i));
			MultiCalendar calendar = flex.getBeginDate();
			if(calendar.isUnknown())
				continue;
			
			int thisYear = calendar.getGregorianYear();
			if(thisYear < earliestYear)
				earliestYear = thisYear;
		}
		
		return earliestYear;
	}
	
	public int getLatestYear()
	{
		MiniLocalization localization = MartusAmplifier.localization;
		int latestYear = 0;
		Vector dateStrings = getIndexedValues();
		for(int i = 0; i < dateStrings.size(); ++i)
		{
			MartusFlexidate flex = localization.createFlexidateFromStoredData((String)dateStrings.get(i));
			MultiCalendar calendar = flex.getEndDate();
			if(calendar.isUnknown())
				continue;

			int thisYear = calendar.getGregorianYear();
			if(thisYear > latestYear)
				latestYear = thisYear;
		}
		
		if(latestYear == 0)
			latestYear = getThisYear();
		
		return latestYear;
	}
	
	private int getThisYear()
	{
		return new MultiCalendar().getGregorianYear();
	}

	
	
	
	static public void initialize(File datesIndexedFile) throws IOException
	{
		EventDatesIndexedList.eventDatesIndexedSingleton = new EventDatesIndexedList(datesIndexedFile);
		EventDatesIndexedList.eventDatesIndexedSingleton.loadFromFile();
	}
	
	// NOTE: calling this without first calling initialize() will 
	// automatically create an empty but valid object 
	public static EventDatesIndexedList getEventDatesIndexedList()
	{
		if(eventDatesIndexedSingleton == null)
		{
			eventDatesIndexedSingleton = new EventDatesIndexedList(null);
		}
		return eventDatesIndexedSingleton;
	}
		
	private static EventDatesIndexedList eventDatesIndexedSingleton;
	
}
