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
package org.martus.client.swingui;

import java.io.File;
import java.time.chrono.Chronology;
import java.util.Vector;

import org.martus.clientside.UiLocalization;

public class MartusLocalization extends UiLocalization 
{
	public MartusLocalization(File directoryToUse, String[] englishTranslations)
	{
		super(directoryToUse, englishTranslations);		
	}

	protected Vector getAllCompiledLanguageResources()
	{
		String filename = null;
		Vector internalLanguages = new Vector();
		for(int i = 0; i < ALL_LANGUAGE_CODES.length; ++i)
		{
			String languageCode = ALL_LANGUAGE_CODES[i];
			filename = getMtfFilename(languageCode);
			if(getClass().getResource(filename) != null)
			{
				internalLanguages.addElement(getLanguageChoiceItem(filename));
			}
		}
		return internalLanguages;
	}
	
	public String getProgramVersionLabel()
	{
		return UiConstants.versionLabel;
	}

	public Chronology getCurrentChronology()
	{
		String currentCalendarSystem = getCurrentCalendarSystem();
		return getChronology(currentCalendarSystem);
	}

	public static Chronology getChronology(String currentCalendarSystem)
	{
		final String JAVA_CHRONOLOGY_CODE_FOR_BUDDHIST_CALENDAR = "ThaiBuddhist";
		final String JAVA_CHRONOLOGY_CODE_FOR_ISLAMIC_CALENDAR = "Hijrah-umalqura";
		final String JAVA_CHRONOLOGY_CODE_FOR_GREGORIAN_CALENDAR = "ISO";
		if(currentCalendarSystem.equals(THAI_SYSTEM))
			return Chronology.of(JAVA_CHRONOLOGY_CODE_FOR_BUDDHIST_CALENDAR);
		if(currentCalendarSystem.equals(PERSIAN_SYSTEM) || currentCalendarSystem.equals(AFGHAN_SYSTEM))
			return Chronology.of(JAVA_CHRONOLOGY_CODE_FOR_ISLAMIC_CALENDAR);
		return Chronology.of(JAVA_CHRONOLOGY_CODE_FOR_GREGORIAN_CALENDAR);
	}
}
