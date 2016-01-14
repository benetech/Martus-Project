/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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
package org.martus.client.swingui.fields;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.AbstractDateOrientedFieldSpec;
import org.martus.swing.UiComboBox;
import org.martus.swing.Utilities;
import org.martus.util.MultiCalendar;

public class UiDateEditorComponent extends Box
{
	public UiDateEditorComponent(MiniLocalization localizationToUse, String minDate, String maxDate)
	{
		super(BoxLayout.X_AXIS);
		localization = localizationToUse;

		earliestAllowedDate = AbstractDateOrientedFieldSpec.getAsDate(minDate);
		latestAllowedDate = AbstractDateOrientedFieldSpec.getAsDate(maxDate);
		
		yearCombo = createYearCombo();
		monthCombo = new UiComboBox();
		dayCombo = new UiComboBox();
		
		configureMonthCombo();
		configureDayCombo();
	
		addComponentsToBox();
		
		yearCombo.addItemListener(new YearChangeListener());
	}

	private UiComboBox createYearCombo()	
	{
		UiComboBox yCombo = new UiComboBox();

		yCombo.addItem(new YearObject(localization.getFieldLabel("YearUnspecified")));
		
		if(THAI_AND_PERSIAN_TESTING)
		{
			System.out.println("WARNING: THAI_AND_PERSIAN Testing mode!!!");
			yCombo.addItem(Integer.toString(1385));
			yCombo.addItem(Integer.toString(2549));
		}
		
		int minYear = localization.getLocalizedYear(earliestAllowedDate);
		int maxYear = localization.getLocalizedYear(latestAllowedDate);
		
		for(int year = minYear; year <= maxYear; ++year)
			yCombo.addItem(new YearObject(year));
		
		yCombo.setSelectedIndex(0);
		return yCombo;
	}
	
	class YearChangeListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent e)
		{
			configureMonthCombo();
			configureDayCombo();
		}
		
	}
	
	static class YearObject
	{
		public YearObject(String unknownYearText)
		{
			year = MultiCalendar.YEAR_NOT_SPECIFIED;
			label = unknownYearText;
		}
		
		public YearObject(int yearToUse)
		{
			year = yearToUse;
			label = Integer.toString(year);
		}
		
		public String toString()
		{
			return label;
		}
		
		public int getYear()
		{
			return year;
		}
		
		public boolean isUnknown()
		{
			return (year == MultiCalendar.YEAR_NOT_SPECIFIED);
		}
		
		public int hashCode()
		{
			return year;
		}

		public boolean equals(Object rawOther)
		{
			if(! (rawOther instanceof YearObject))
				return false;
			
			YearObject other = (YearObject)rawOther;
			return (year == other.year);
		}
		
		
		
		int year;
		String label;
	}
	
	private boolean isUnknownYearSelected()
	{
		return (yearCombo.getSelectedIndex() == 0);
	}
	
	void configureMonthCombo()
	{
		boolean shouldBeEnabled = true;
		monthCombo.removeAllItems();
		if(isUnknownYearSelected())
		{
			monthCombo.addItem("?");
			shouldBeEnabled = false;
		}
		String[] months = localization.getMonthLabels();
		for(int i = 0; i < months.length; ++i)
			monthCombo.addItem(months[i]);
		monthCombo.setEnabled(shouldBeEnabled);
	}
	
	void configureDayCombo()
	{
		boolean shouldBeEnabled = true;
		dayCombo.removeAllItems();
		if(isUnknownYearSelected())
		{
			dayCombo.addItem("?");
			shouldBeEnabled = false;
		}
		for(int day=1; day <= 31; ++day)
			dayCombo.addItem(new Integer(day).toString());
		dayCombo.setEnabled(shouldBeEnabled);
			
	}
	
	private void addComponentsToBox()
	{
		JComponent[] dateInOrderLeftToRight = getComponentsInOrder();	
		Utilities.addComponentsRespectingOrientation(this, dateInOrderLeftToRight);
	}
	
	//	 On some platforms (Windows?), the dropdowns are not quite wide enough
	//	 and shows "..." unless we widen the whole component to give it enough space
	public Dimension getPreferredSize()
	{
		Dimension preferredSize = super.getPreferredSize();
		preferredSize.width += EXTRA_WIDTH_SO_FIELDS_DISPLAY_WHEN_COLAPSED;
		return preferredSize;
	}

	JComponent[] getComponentsInOrder()
	{
		JComponent[] dateInOrderLeftToRight = new JComponent[3];
		
		String mdyOrder = localization.getMdyOrder();
		for(int i = 0; i < mdyOrder.length(); ++i)
		{
			switch(mdyOrder.charAt(i))
			{
				case 'd': dateInOrderLeftToRight[i]=dayCombo;	break;
				case 'm': dateInOrderLeftToRight[i]=monthCombo;	break;
				case 'y': dateInOrderLeftToRight[i]=yearCombo;	break;
			}
		}
		return dateInOrderLeftToRight;
	}

	public JComponent[] getFocusableComponents()
	{
		return getComponentsInOrder();
	}

	public String getStoredDateText()
	{
		return getDate().toIsoDateString();
	}

	public MultiCalendar getDate()
	{
		YearObject yearObject = (YearObject)yearCombo.getSelectedItem();
		if(yearObject.isUnknown())
			return MultiCalendar.UNKNOWN;
		
		int year = yearObject.getYear();
		int month = monthCombo.getSelectedIndex()+1;
		int day = dayCombo.getSelectedIndex()+1;

		return localization.createCalendarFromLocalizedYearMonthDay(year, month, day);
	}

	public void setStoredDateText(String newText)
	{
		if(newText.isEmpty())
		{
			setUnknownDate();
			return;
		}
		try
		{
			MultiCalendar cal = localization.createCalendarFromIsoDateString(newText);
			setDate(cal);
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			setUnknownDate();
		}
	}
	
	private void setUnknownDate()
	{
		try
		{
			yearCombo.setSelectedIndex(0);
			monthCombo.setSelectedIndex(0);
			dayCombo.setSelectedIndex(0);
		}
		catch(RuntimeException e)
		{
			e.printStackTrace();
			throw(e);
		}
	}

	public void setDate(MultiCalendar cal)
	{
		if(cal.getGregorianYear() == MultiCalendar.YEAR_NOT_SPECIFIED)
		{
			setUnknownDate();
			return;
		}
		
		yearCombo.setSelectedItem(new YearObject(localization.getLocalizedYear(cal)));
		monthCombo.setSelectedIndex((localization.getLocalizedMonth(cal) - 1));
		dayCombo.setSelectedIndex((localization.getLocalizedDay(cal) - 1));
	}
	
	public void requestFocus()
	{
		getComponentsInOrder()[0].requestFocus();
	}
	
	// Enable the following to add a Persian year and a 
	// Thai year to the Date Editor year dropdowns
	static final boolean THAI_AND_PERSIAN_TESTING = false;
	
	static final int EXTRA_WIDTH_SO_FIELDS_DISPLAY_WHEN_COLAPSED = 20;

	private MiniLocalization localization;
	private MultiCalendar earliestAllowedDate;
	private MultiCalendar latestAllowedDate;
	private UiComboBox yearCombo;	
	private UiComboBox dayCombo;
	private UiComboBox monthCombo;
}
