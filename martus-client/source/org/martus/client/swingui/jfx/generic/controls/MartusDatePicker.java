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
package org.martus.client.swingui.jfx.generic.controls;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import org.martus.client.swingui.MartusLocalization;
import org.martus.common.MiniLocalization;
import org.martus.util.MultiCalendar;

public class MartusDatePicker extends DatePicker
{
	public MartusDatePicker(MartusLocalization localizationToUse)
	{
		MartusDateConverter converter = new MartusDateConverter(localizationToUse);
		setConverter(converter);
		setChronology(localizationToUse.getCurrentChronology());
		overallValueProperty = new SimpleStringProperty("");
		valueProperty().addListener((observable, oldValue, newValue) -> updateOverallValue());
	}
	
	public void setValue(String existingDateString)
	{
		LocalDate localDate = convertIsoDateStringToLocalDate(existingDateString);
		setValue(localDate);
	}

	public static LocalDate convertIsoDateStringToLocalDate(String existingDateString)
	{
		if(existingDateString.isEmpty())
			return null;
		
		MultiCalendar multiCalendar = MultiCalendar.createFromIsoDateString(existingDateString);
		return MartusDatePicker.getLocalDate(multiCalendar);
	}
	
	public ReadOnlyStringProperty overallValueProperty()
	{
		return overallValueProperty;
	}
	
	public String getLocalizedDateFormatted()
	{
		return getConverter().toString(getValue());
	}

	private void updateOverallValue()
	{
		LocalDate localDate = getValue();
		String isoDate = convertLocalDateToString(localDate);
		overallValueProperty.setValue(isoDate);
	}
	
	static protected String convertLocalDateToString(LocalDate localDate)
	{
		if(localDate == null)
			return "";
		
		MultiCalendar multiCalendar = MartusDatePicker.convertLocalDateToMultiCalendar(localDate);
		return multiCalendar.toIsoDateString();
	}

	public static LocalDate getLocalDate(MultiCalendar baseDate)
	{
		if(baseDate.isUnknown())
			return null;
		
		return LocalDate.parse(baseDate.toIsoDateString());
	}

	public static MultiCalendar convertLocalDateToMultiCalendar(LocalDate localDate)
	{
		int year = localDate.getYear();
		int month = localDate.getMonthValue();
		int day = localDate.getDayOfMonth();
		return MultiCalendar.createFromGregorianYearMonthDay(year, month, day);
	}
	
	static class MartusDateConverter extends StringConverter<LocalDate>
	{
		public MartusDateConverter(MiniLocalization localizationToUse)
		{
			localization = localizationToUse;
			String dateFormatCode = localizationToUse.getCurrentDateFormatCode();
			formatter = DateTimeFormatter.ofPattern(dateFormatCode);
		}

		@Override
		public LocalDate fromString(String dateInStringForm)
		{
			return LocalDate.parse(dateInStringForm, formatter);
		}

		@Override
		public String toString(LocalDate localDate)
		{
			if(localDate == null)
				return "";
			String storedDateFormat = convertLocalDateToString(localDate);
			return localization.convertStoredDateToDisplay(storedDateFormat);
		}
		
		private MiniLocalization localization;
		private DateTimeFormatter formatter;
	}

	private SimpleStringProperty overallValueProperty;
}
