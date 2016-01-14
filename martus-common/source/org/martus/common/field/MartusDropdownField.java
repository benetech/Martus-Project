/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2010, Beneficent
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
package org.martus.common.field;

import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.ReusableChoices;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeDropdown;
import org.martus.common.fieldspec.StandardFieldSpecs;

public class MartusDropdownField extends MartusField
{
	public MartusDropdownField(FieldSpec specToUse, PoolOfReusableChoicesLists reusableChoicesToUse)
	{
		super(specToUse, reusableChoicesToUse);
		relevantLevelCount = -1;
	}

	public MartusField createClone() throws Exception
	{
		MartusDropdownField clone = new MartusDropdownField(getFieldSpec(), getReusableChoicesLists());
		clone.relevantLevelCount = relevantLevelCount;
		clone.setData(getData());
		return clone;
	}
	
	public String getDataForSubtotals()
	{
		if(relevantLevelCount < 0)
			return getData();
		
		return truncateData(getData(), relevantLevelCount);
	}
	
	protected String internalGetHtml(MiniLocalization localization) throws Exception
	{
		CustomDropDownFieldSpec dropDownSpec = getDropDownSpec();
		String[] reusableChoicesCodes = dropDownSpec.getReusableChoicesCodes();
		if(reusableChoicesCodes.length == 0)
			return super.internalGetHtml(localization);
		
		return dropDownSpec.convertStoredToHtml(this, localization);
	}
	
	protected String internalGetHtmlForSubtotals(MiniLocalization localization) throws Exception
	{
		CustomDropDownFieldSpec dropDownSpec = getDropDownSpec();
		String[] reusableChoicesCodes = dropDownSpec.getReusableChoicesCodes();
		if(reusableChoicesCodes.length == 0)
			return super.internalGetHtml(localization);
		
		return dropDownSpec.convertStoredToHtml(this, getDataForSubtotals(), localization);
	}
	
	@Override
	public String[] getHumanReadableData(MiniLocalization localization)
	{
		CustomDropDownFieldSpec dropDownSpec = getDropDownSpec();
		String[] reusableChoicesCodes = dropDownSpec.getReusableChoicesCodes();
		if(reusableChoicesCodes.length == 0)
			return super.getHumanReadableData(localization);
		
		return dropDownSpec.convertStoredToHumanReadable(getData(), getReusableChoicesLists(), localization);
	}

	public boolean contains(String value, MiniLocalization localization)
	{
		// NOTE: this type doesn't support contains searching at all!
		return false;
	}

	public MartusField getSubField(String tag, MiniLocalization localization)
	{
		if(StandardFieldSpecs.isStandardFieldTag(getTag()))
			return null;
		
		CustomDropDownFieldSpec outerSpec = getDropDownSpec();
		String[] reusableChoicesCodes = outerSpec.getReusableChoicesCodes();
		if(reusableChoicesCodes.length < 2)
			return new EmptyMartusFieldWithInfiniteSubFields(tag);

		int level = outerSpec.findReusableLevelByCode(tag);
		if(level < 0)
			return new EmptyMartusFieldWithInfiniteSubFields(tag);

		ReusableChoices reusableChoices = getReusableChoicesLists().getChoices(reusableChoicesCodes[level]);
		CustomDropDownFieldSpec subSpec = (CustomDropDownFieldSpec) FieldSpec.createSubField(outerSpec, tag, reusableChoices.getLabel(), new FieldTypeDropdown());
		subSpec.addReusableChoicesCode(tag);
		MartusDropdownField subField = new MartusDropdownField(subSpec, getReusableChoicesLists());
		subField.relevantLevelCount = level + 1;
		subField.setData(getData());
		return subField;
	}

	public boolean doesMatch(int compareOp, String searchForValue, MiniLocalization localization)
	{
		boolean doesEqual = doesEqual(searchForValue);
		switch(compareOp)
		{
			case EQUAL:		return doesEqual;
			case NOT_EQUAL: return !doesEqual;
			default:		return false;
		}
	}
	
	private boolean doesEqual(String searchForValue)
	{
		CustomDropDownFieldSpec dropDownSpec = getDropDownSpec();
		if(StandardFieldSpecs.isStandardFieldTag(getTag()))
		{
			int found = dropDownSpec.findCode(searchForValue);
			return (found >= 0);
		}
		
		String trimmedFieldData = getData().trim();
		boolean isExactMatch = trimmedFieldData.equals(searchForValue.trim());

		// Empty search always needs an exact match
		if(searchForValue.length() == 0)
			return isExactMatch;
		
		// Multi-level or non-reusable dropdown always needs an exact match
		if(dropDownSpec.getReusableChoicesCodes().length != 1)
			return isExactMatch;
		
		// anything other than a subfield of a dropdown always needs an exact match
		FieldSpec rawParentSpec = dropDownSpec.getParent();
		if(rawParentSpec == null || !rawParentSpec.getType().isDropdown())
			return isExactMatch;

		// if the code is not a valid reusable list in the parent (unlikely), fail
		String thisReusableChoicesListCode = dropDownSpec.getReusableChoicesCodes()[0];
		CustomDropDownFieldSpec parentSpec = (CustomDropDownFieldSpec) rawParentSpec;
		int foundReusableCode = parentSpec.findReusableLevelByCode(thisReusableChoicesListCode);
		if(foundReusableCode == -1)
			return false;

		// if field is State/City/Neighborhood
		//  State=CA should match CA
		//  State=CA should match CA.SF 
		//  City=CA should match CA 
		//  City=CA should NOT match CA.SF
		//  City=CA.SF should match CA.SF
		String searchOn = truncateData(trimmedFieldData, relevantLevelCount);
		return searchOn.equals(searchForValue);
	}

	private String truncateData(String dataToTruncate, int maxLevels)
	{
		int truncateAt = -1;
		while(maxLevels > 0)
		{
			int nextDot = dataToTruncate.indexOf('.', truncateAt+1);
			if(nextDot < 0)
				return dataToTruncate;
			truncateAt = nextDot;
			--maxLevels;
		}
		
		return dataToTruncate.substring(0, truncateAt);
	}

	private CustomDropDownFieldSpec getDropDownSpec()
	{
		return (CustomDropDownFieldSpec) getFieldSpec();
	}
	
	private int relevantLevelCount;
}
