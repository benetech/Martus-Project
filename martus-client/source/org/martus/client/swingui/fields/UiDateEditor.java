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

package org.martus.client.swingui.fields;

import javax.swing.JComponent;

import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.DateFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.util.MultiCalendar;

public class UiDateEditor extends UiField
{
	public UiDateEditor(DateFieldSpec spec, MiniLocalization localizationToUse)
	{
		super(localizationToUse);

		component = new UiDateEditorComponent(localizationToUse, spec.getEarliestAllowedDate(), spec.getLatestAllowedDate());
	}

	public JComponent getComponent()
	{
		return component;
	}

	public JComponent[] getFocusableComponents()
	{
		return component.getFocusableComponents();
	}

	public static class DateFutureException extends DataInvalidException
	{
		public DateFutureException()
		{
			super();
		}
		public DateFutureException(String tag)
		{
			super(tag);
		}

	}
	
	public void validate(FieldSpec spec, String fullLabel) throws DataInvalidException 
	{
		super.validate(spec, fullLabel);
		
		if(allowFutureDates)
			return;
	
		MultiCalendar value = component.getDate();
		if (value.after(new MultiCalendar()))
		{
			component.requestFocus();	
			throw new DateFutureException();
		}
	}

	public String getText()
	{
		return component.getStoredDateText();
	}

	public void setText(String newText)
	{
		component.setStoredDateText(newText);			
	}
	
	private boolean allowFutureDates;
	private UiDateEditorComponent component;
}

