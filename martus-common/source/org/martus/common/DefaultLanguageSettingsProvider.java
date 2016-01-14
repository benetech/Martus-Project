/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.common;

import org.martus.util.DatePreference;

public class DefaultLanguageSettingsProvider implements LanguageSettingsProvider
{
	public DefaultLanguageSettingsProvider()
	{
		calendarSystem = MiniLocalization.GREGORIAN_SYSTEM;
	}

	@Override
	public String getCurrentDateFormat()
	{
		return new DatePreference().getRawDateTemplate();
	}
	
	@Override
	public void setCurrentDateFormat(String currentDateFormat)
	{
	}

	@Override
	public String getCurrentLanguage()
	{
		return MiniLocalization.ENGLISH;
	}

	@Override
	public void setCurrentLanguage(String currentLanguage)
	{
	}

	@Override
	public String getCurrentCalendarSystem()
	{
		return calendarSystem;
	}
	
	@Override
	public void setCurrentCalendarSystem(String newCalendarSystem)
	{
		calendarSystem = newCalendarSystem;
	}

	@Override
	public boolean getAdjustThaiLegacyDates()
	{
		return true;
	}

	@Override
	public boolean getAdjustPersianLegacyDates()
	{
		return true;
	}

	@Override
	public void setDateFormatFromLanguage()
	{
		DatePreference preference = MiniLocalization.getDefaultDatePreferenceForLanguage(getCurrentLanguage());
		setCurrentDateFormat(preference.getDateTemplate());
	}
	
	private String calendarSystem;
}

