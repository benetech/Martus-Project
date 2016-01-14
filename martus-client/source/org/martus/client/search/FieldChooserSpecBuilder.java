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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.field.MartusDateRangeField;
import org.martus.common.field.MartusGridField;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeDropdown;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.SearchFieldTreeModel;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class FieldChooserSpecBuilder
{
	public FieldChooserSpecBuilder(MiniLocalization localizationToUse)
	{
		localization = localizationToUse;
	}
	
	public PopUpTreeFieldSpec createSpec(ClientBulletinStore storeToUse)
	{
		return createSpec(storeToUse, null);
	}
	
	public PopUpTreeFieldSpec createSpec(ClientBulletinStore storeToUse, MiniFieldSpec[] specsToInclude)
	{
		FieldChoicesByLabel allAvailableFields = buildFieldChoicesByLabel(storeToUse, specsToInclude);
		
		SearchFieldTreeModel fieldChoiceModel = new SearchFieldTreeModel(allAvailableFields.asTree(getLocalization()));
		PopUpTreeFieldSpec fieldColumnSpec = new PopUpTreeFieldSpec(fieldChoiceModel);
		fieldColumnSpec.setLabel(getLocalization().getFieldLabel("SearchGridHeaderField"));
		return fieldColumnSpec;
	}

	FieldChoicesByLabel buildFieldChoicesByLabel(ClientBulletinStore storeToUse, MiniFieldSpec[] specsToInclude)
	{
		FieldChoicesByLabel allAvailableFields = new FieldChoicesByLabel(localization);
		if(shouldIncludeLastSaved())
			allAvailableFields.add(createLastSavedDateChoice());
		allAvailableFields.addAll(convertToChoiceItems(storeToUse.getAllKnownFieldSpecs(), storeToUse.getAllReusableChoiceLists()));
		if(specsToInclude != null)
		{
			allAvailableFields.onlyKeep(specsToInclude);
		}
		addSpecialFields(allAvailableFields);
		return allAvailableFields;
	}

	protected boolean shouldIncludeLastSaved()
	{
		return true;
	}
	
	public FieldSpec[] createFieldSpecArray(ClientBulletinStore storeToUse)
	{
		FieldChoicesByLabel allAvailableFields = buildFieldChoicesByLabel(storeToUse, null);
		return allAvailableFields.asArray(getLocalization());
	}
	
	public void addSpecialFields(FieldChoicesByLabel fields)
	{
	}
	
	private ChoiceItem createLastSavedDateChoice()
	{
		return createLastSavedDateChoice(getLocalization());
	}

	public static SearchableFieldChoiceItem createLastSavedDateChoice(MiniLocalization localization)
	{
		String tag = Bulletin.PSEUDOFIELD_LAST_SAVED_DATE;
		String label = localization.getFieldLabel(Bulletin.TAGLASTSAVED);
		FieldType type = new FieldTypeDate();
		FieldSpec spec = FieldSpec.createCustomField(tag, label, type);
		return new SearchableFieldChoiceItem(spec);
	}

	public Vector convertToChoiceItems(Collection specs, PoolOfReusableChoicesLists reusableChoiceLists)
	{
		Vector allChoices = new Vector();
		Iterator iter = specs.iterator();
		while(iter.hasNext())
		{
			FieldSpec spec = (FieldSpec)iter.next();
			allChoices.addAll(getChoiceItemsForThisField(spec, reusableChoiceLists));
		}
			
		return allChoices;
	}

	public Set getChoiceItemsForThisField(FieldSpec spec, PoolOfReusableChoicesLists reusableChoiceLists)
	{
		return getChoiceItemsForThisField(null, spec, spec.getTag(), "", reusableChoiceLists);
	}
	
	public Set getChoiceItemsForThisField(FieldSpec parent, FieldSpec spec, String possiblySanitizedTag, String displayPrefix, PoolOfReusableChoicesLists reusableChoiceLists)
	{

		Set choicesForThisField = new HashSet();
		final FieldType thisType = spec.getType();
		
		if(shouldOmitType(thisType))
			return choicesForThisField;
		
		String tag = possiblySanitizedTag;
		String displayString = spec.getLabel();
		if(StandardFieldSpecs.isStandardFieldTag(tag))
			displayString = getLocalization().getFieldLabel(tag);
		else if(displayString.trim().equals(""))
			displayString = tag;

		displayString = displayPrefix + displayString;

		// unknown types (Lewis had one) should not appear in the list at all
		if(thisType.isUnknown())
			return choicesForThisField;

		// dateranges create multiple entries
		if(thisType.isDateRange())
		{
			FieldSpec specWithParentAndTag = FieldSpec.createSubField(parent, tag, displayString, new FieldTypeDateRange());
			choicesForThisField.addAll(getDateRangeChoiceItem(specWithParentAndTag, MartusDateRangeField.SUBFIELD_BEGIN));
			choicesForThisField.addAll(getDateRangeChoiceItem(specWithParentAndTag, MartusDateRangeField.SUBFIELD_END));
			return choicesForThisField;
		}
		
		// dropdowns MUST be a DropDownFieldSpec, not a plain FieldSpec
		if(thisType.isDropdown())
		{
			DropDownFieldSpec originalSpec = (DropDownFieldSpec)spec;

			CustomDropDownFieldSpec specWithBetterLabel = (CustomDropDownFieldSpec) FieldSpec.createSubField(parent, tag, displayString, new FieldTypeDropdown());
			specWithBetterLabel.pullDynamicChoiceSettingsFrom(originalSpec);
			
			return getChoicesForDropdownSpec(specWithBetterLabel, reusableChoiceLists, displayString);
		}

		// add one choice per column
		if(thisType.isGrid())
		{
			GridFieldSpec gridSpec = (GridFieldSpec)spec;
			for(int i=0; i < gridSpec.getColumnCount(); ++i)
			{
				final FieldSpec columnSpec = gridSpec.getFieldSpec(i);
				String sanitizedTag = MartusGridField.sanitizeLabel(columnSpec.getLabel());
				String columnDisplayPrefix = displayString + ": ";
				choicesForThisField.addAll(getChoiceItemsForThisField(gridSpec, columnSpec, sanitizedTag, columnDisplayPrefix, reusableChoiceLists));
			}
			return choicesForThisField;
		}

		// many types just create a choice with their own type,
		// but we need to default to NORMAL for safety
		FieldType choiceSpecType = new FieldTypeNormal();
		if(shouldSearchSpecTypeBeTheFieldSpecType(thisType))
			choiceSpecType = thisType;

		FieldSpec thisSpec = FieldSpec.createSubField(parent, tag, displayString, choiceSpecType);
		ChoiceItem choiceItem = new SearchableFieldChoiceItem(thisSpec);
		choicesForThisField.add(choiceItem);
		return choicesForThisField;
	}

	protected Set getChoicesForDropdownSpec(CustomDropDownFieldSpec specWithBetterLabel, PoolOfReusableChoicesLists reusableChoiceLists, String displayString)
	{
		Set choicesForDropdown = new HashSet();
		SearchableFieldChoiceItem masterDropdownSpecChoice = new SearchableFieldChoiceItem(specWithBetterLabel);
		if(specWithBetterLabel.getReusableChoicesCodes().length > 1)
			choicesForDropdown.addAll(createPerLevelChoicesForNestedDropdown(specWithBetterLabel, displayString, reusableChoiceLists));
		else
			choicesForDropdown.add(masterDropdownSpecChoice);

		return choicesForDropdown;
	}
	
	private Vector createPerLevelChoicesForNestedDropdown(CustomDropDownFieldSpec spec, String displayPrefix, PoolOfReusableChoicesLists reusableChoicesPool)
	{
		Vector choices = new Vector();
		
		String[] reusableChoicesListsCodes = spec.getReusableChoicesCodes();
		for(int level = 0; level < reusableChoicesListsCodes.length; ++level)
		{
			ReusableChoices reusableChoices = reusableChoicesPool.getChoices(reusableChoicesListsCodes[level]);
			String levelTag = reusableChoices.getCode();
			String levelLabel = reusableChoices.getLabel();
			CustomDropDownFieldSpec subFieldSpec = (CustomDropDownFieldSpec) FieldSpec.createSubField(spec, levelTag, displayPrefix + ": " + levelLabel, new FieldTypeDropdown());
			for(int someLevels = 0; someLevels <= level; ++someLevels)
				subFieldSpec.addReusableChoicesCode(reusableChoicesListsCodes[someLevels]);
			choices.add(new SearchableFieldChoiceItem(subFieldSpec));
		}
		return choices;
	}

	public boolean shouldOmitType(FieldType type)
	{
		return false;
	}

	private boolean shouldSearchSpecTypeBeTheFieldSpecType(final FieldType thisType)
	{
		if(thisType.isDate())
			return true;
		
		if(thisType.isLanguageDropdown())
			return true;
		
		if(thisType.isBoolean())
			return true;
		
		if(thisType.isMultiline())
			return true;
		
		if(thisType.isMessage())
			return true;
		
		return false;
	}
	
	private Set getDateRangeChoiceItem(FieldSpec spec, String subfield) 
	{
		String baseDisplayString = spec.getLabel();
		Set itemIfAny = new HashSet();
		try
		{
			String fullDisplayString = buildDateRangeSubfieldString(baseDisplayString, subfield, getLocalization());
			FieldSpec dateSpec = FieldSpec.createSubField(spec, subfield, fullDisplayString, new FieldTypeDate());
			itemIfAny.add(new SearchableFieldChoiceItem(dateSpec));
		}
		catch (TokenInvalidException e)
		{
			// bad translation--not much we can do about it
			e.printStackTrace();
		}
		
		return itemIfAny;
	}

	public static String buildDateRangeSubfieldString(String baseDisplayString, String subfield, MiniLocalization localization) throws TokenInvalidException
	{
		String displayTemplate = localization.getFieldLabel("DateRangeTemplate" + subfield);
		String fullDisplayString = TokenReplacement.replaceToken(displayTemplate, "#FieldLabel#", baseDisplayString);
		return fullDisplayString;
	}
	
	MiniLocalization getLocalization()
	{
		return localization;
	}

	MiniLocalization localization;
}
