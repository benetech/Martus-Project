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
package org.martus.client.search;

import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeAnyField;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;

public class SearchFieldChooserSpecBuilder extends FieldChooserSpecBuilder
{
	public SearchFieldChooserSpecBuilder(MiniLocalization localizationToUse)
	{
		super(localizationToUse);
	}

	public void addSpecialFields(FieldChoicesByLabel fields)
	{
		fields.add(createAnyFieldChoice());
	}
	
	private ChoiceItem createAnyFieldChoice()
	{
		return createAnyFieldChoice(getLocalization());
	}

	public static SearchableFieldChoiceItem createAnyFieldChoice(MiniLocalization localization)
	{
		String tag = "";
		String label = localization.getFieldLabel("SearchAnyField");
		FieldType type = new FieldTypeAnyField();
		FieldSpec spec = FieldSpec.createCustomField(tag, label, type);
		SearchableFieldChoiceItem allFieldsSearchableChoiceItem = new SearchableFieldChoiceItem(tag, spec);
		return allFieldsSearchableChoiceItem;
	}

}
