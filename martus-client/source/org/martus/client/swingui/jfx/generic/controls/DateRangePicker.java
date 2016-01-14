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

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import org.martus.client.swingui.MartusLocalization;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;

public class DateRangePicker extends HBox
{
	public DateRangePicker(MartusLocalization localizationToUse)
	{
		overallValueProperty = new SimpleStringProperty("");
		
		startPicker = new MartusDatePicker(localizationToUse);
		endPicker = new MartusDatePicker(localizationToUse);
		
		startPicker.valueProperty().addListener((observable, oldValue, newValue) -> updateOverallValue());
		endPicker.valueProperty().addListener((observable, oldValue, newValue) -> updateOverallValue());

		getChildren().add(startPicker);
		getChildren().add(new Label(DATE_RANGE_SEPARATOR));
		getChildren().add(endPicker);
	}
	
	public Property<String> valueProperty()
	{
		return overallValueProperty;
	}
	
	public void setValue(String value)
	{
		if(!MartusFlexidate.isFlexidateString(value))
		{
			setFromSingleDate(value);
			return;
		}
		
		String isoBaseDate = MartusFlexidate.extractIsoDateFromStoredDate(value);
		int numberOfDays = MartusFlexidate.extractRangeFromStoredDate(value);
		
		MartusFlexidate flexidate = new MartusFlexidate(isoBaseDate, numberOfDays);
		String isoStartDate = flexidate.getBeginDate().toIsoDateString();
		String isoEndDate = flexidate.getEndDate().toIsoDateString();
		
		startPicker.setValue(isoStartDate);
		endPicker.setValue(isoEndDate);
	}

	public void setFromSingleDate(String isoDateString)
	{
		startPicker.setValue(isoDateString);
		endPicker.setValue(isoDateString);
	}
	
	private void updateOverallValue()
	{
		LocalDate startLocalDate = startPicker.getValue();
		LocalDate endLocalDate = endPicker.getValue();
		
		String value = getDateRangeString(startLocalDate, endLocalDate);
		overallValueProperty.setValue(value);
	}
	
	public String getDateRangeString(LocalDate startLocalDate, LocalDate endLocalDate)
	{
		if(startLocalDate == null)
			startLocalDate = endLocalDate;
		
		if(endLocalDate == null)
			endLocalDate = startLocalDate;
		
		if(startLocalDate == null)
			return "";
		
		final MultiCalendar beginDate = MartusDatePicker.convertLocalDateToMultiCalendar(startLocalDate);
		final MultiCalendar endDate = MartusDatePicker.convertLocalDateToMultiCalendar(endLocalDate);
		String value = MartusFlexidate.toBulletinFlexidateFormat(beginDate, endDate);
		return value;
	}

	private static final String DATE_RANGE_SEPARATOR = " - ";

	private SimpleStringProperty overallValueProperty;
	private MartusDatePicker startPicker;
	private MartusDatePicker endPicker;
}
