/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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
package org.martus.client.swingui.fields;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.border.LineBorder;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DateRangeFieldSpec;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.GridFieldSpec;

abstract public class UiFieldCreator
{
	public UiFieldCreator(UiMainWindow mainWindowToUse, UiFieldContext contextToUse)
	{
		mainWindow = mainWindowToUse;
		context = contextToUse;
	}
	
	abstract public UiField createNormalField(FieldSpec spec);
	abstract public UiField createMultilineField(FieldSpec spec);
	abstract public UiField createMessageField(FieldSpec spec);

	abstract public UiField createChoiceField(DropDownFieldSpec spec);
	abstract public UiField createLanguageField(DropDownFieldSpec spec);
	abstract public UiField createDateField(FieldSpec spec);
	abstract public UiField createFlexiDateField(DateRangeFieldSpec spec);
	abstract public UiField createUnknownField(FieldSpec spec);
	abstract public UiField createBoolField(FieldSpec spec);
	abstract public UiField createGridField(GridFieldSpec fieldSpec);

	protected UiFieldContext getContext()
	{
		return context;
	}
	
	public UiGrid getGridField(String tag)
	{
		return context.getGridField(tag);
	}

	public UiField createReadOnlyDateField()
	{
		return new UiDateViewer(getLocalization());
	}

	public MartusLocalization getLocalization()
	{
		return mainWindow.getLocalization();
	}
	
	public UiField createField(FieldSpec fieldSpec)
	{
		UiField field = null;

		if(fieldSpec.getTag().equals(Bulletin.TAGENTRYDATE))
			field = createReadOnlyDateField();
		else
			field = createRegularField(fieldSpec);
		field.getComponent().setBorder(new LineBorder(Color.black));
		context.registerField(fieldSpec, field);
		return field;
	}


	private UiField createRegularField(FieldSpec fieldSpec)
	{
		FieldType type = fieldSpec.getType();
		if(type.isMultiline())
			return createMultilineField(fieldSpec);
		if(type.isDate())
			return createDateField(fieldSpec);
		if(type.isDateRange())
			return createFlexiDateField((DateRangeFieldSpec) fieldSpec);
		if(type.isLanguageDropdown())
			return createLanguageField(fieldSpec);
		if(type.isDropdown())
			return createChoiceField((DropDownFieldSpec)fieldSpec);
		if(type.isString())
			return createNormalField(fieldSpec);
		if(type.isMessage())
			return createMessageField(fieldSpec);
		if(type.isBoolean())
			return createBoolField(fieldSpec);
		if(type.isGrid())
			return createGridField((GridFieldSpec)fieldSpec);
		
		return createUnknownField(fieldSpec);
	}

	private UiField createLanguageField(FieldSpec spec)
	{
		UiField field;
		ChoiceItem[] languageNameChoices = getLocalization().getLanguageNameChoices();
		Arrays.sort(languageNameChoices, new ChoiceItem.ChoiceItemSorterByLabel());
		DropDownFieldSpec dropdownSpec = new DropDownFieldSpec(languageNameChoices);
		dropdownSpec.setTag(spec.getTag());
		field = createLanguageField(dropdownSpec);
		return field;
	}
	
	UiMainWindow mainWindow;
	private UiFieldContext context;
}
