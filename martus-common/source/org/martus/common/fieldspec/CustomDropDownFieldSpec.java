/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.common.fieldspec;

import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

import org.martus.common.ListOfReusableChoicesLists;
import org.martus.common.MartusXml;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.field.MartusDropdownField;
import org.martus.common.field.MartusField;
import org.martus.util.xml.SimpleXmlMapLoader;
import org.martus.util.xml.SimpleXmlVectorLoader;
import org.martus.util.xml.XmlUtilities;
import org.xml.sax.SAXParseException;

public class CustomDropDownFieldSpec extends DropDownFieldSpec
{
	public CustomDropDownFieldSpec()
	{
		reusableChoicesCodes = new Vector();
	}
	
	protected boolean allowUserDefaultValue()
	{
		return true;
	}

	public void addReusableChoicesCode(String reusableChoicesCodeToAdd)
	{
		reusableChoicesCodes.add(reusableChoicesCodeToAdd);
	}
	
	public String[] getReusableChoicesCodes()
	{
		return (String[]) reusableChoicesCodes.toArray(new String[0]);
	}
	
	public ChoiceItem[] createValidChoiceItemArrayFromStrings(Vector stringChoicesToUse)
	{
		boolean hasEmptyCode = false;
		Vector choices = new Vector();
		for(int i = 0; i < stringChoicesToUse.size(); i++)
		{
			String item = (String)stringChoicesToUse.get(i);
			choices.add(new ChoiceItem(item,item));
			if(item.length() == 0)
				hasEmptyCode = true;
		}
		
		if(!hasEmptyCode)
			choices.insertElementAt(new ChoiceItem("", ""), 0);
		
		return (ChoiceItem[])choices.toArray(new ChoiceItem[0]);
	}

	protected String getSystemDefaultValue()
	{
		return "";
	}
	
	
	public String getDetailsXml()
	{
		if(getDataSourceGridTag() != null)
		{
			String xml = MartusXml.getTagStartWithNewline(DROPDOWN_SPEC_DATA_SOURCE) + 
				MartusXml.getTagStart(DROPDOWN_SPEC_DATA_SOURCE_GRID_TAG_TAG) + 
				getDataSourceGridTag() + 
				MartusXml.getTagEnd(DROPDOWN_SPEC_DATA_SOURCE_GRID_TAG_TAG) +

				MartusXml.getTagStart(DROPDOWN_SPEC_DATA_SOURCE_GRID_COLUMN_TAG) + 
				getDataSourceGridColumn() + 
				MartusXml.getTagEnd(DROPDOWN_SPEC_DATA_SOURCE_GRID_COLUMN_TAG) +
				
				MartusXml.getTagEnd(DROPDOWN_SPEC_DATA_SOURCE);
				
			return xml;
		}


		if(reusableChoicesCodes.size() > 0)
		{
			StringBuffer details = new StringBuffer();
			for (int i = 0; i < reusableChoicesCodes.size(); ++i)
			{
				details.append("<" + USE_REUSABLE_CHOICES_TAG + " ");
				details.append("code='" + reusableChoicesCodes.get(i) + "'>");
				details.append(MartusXml.getTagEnd(USE_REUSABLE_CHOICES_TAG));
			}
			return details.toString();
		}

		return super.getDetailsXml();
	}

	public void setDataSource(String gridTagToUse, String gridColumnToUse)
	{
		gridTag = gridTagToUse;
		gridColumn = gridColumnToUse;
		updateDetailsXml();
	}

	public Object getDataSource()
	{
		if(getDataSourceGridTag() == null || getDataSourceGridColumn() == null)
			return null;
		
		return getDataSourceGridTag() + "." + getDataSourceGridColumn();
	}

	public String getDataSourceGridTag()
	{
		return gridTag;
	}
	
	public String getDataSourceGridColumn()
	{
		return gridColumn;
	}
	
	public int findReusableLevelByCode(String tag)
	{
		for(int level = 0; level < getReusableChoicesCodes().length; ++level)
		{
			if(getReusableChoicesCodes()[level].equals(tag))
				return level;
		}
		return -1;
	}

	public void pullDynamicChoiceSettingsFrom(DropDownFieldSpec other)
	{
		String[] otherReusableChoicesCodes = other.getReusableChoicesCodes();
		String dataSourceGridTag = other.getDataSourceGridTag();
		if(otherReusableChoicesCodes.length > 0)
		{
			for(int i = 0; i < otherReusableChoicesCodes.length; ++i)
				addReusableChoicesCode(otherReusableChoicesCodes[i]);
		} 
		else if(dataSourceGridTag != null)
		{
			setDataSource(dataSourceGridTag, other.getDataSourceGridColumn());
		}

		Vector allChoices = new Vector();
		allChoices.addAll(Arrays.asList(getAllChoices()));
		allChoices.addAll(Arrays.asList(other.getAllChoices()));
		setChoices((ChoiceItem[]) allChoices.toArray(new ChoiceItem[0]));
	}

	public String convertStoredToHtml(MartusField field, MiniLocalization localization)
	{
		return convertStoredToHtml((MartusDropdownField)field, field.getData(), localization);
	}
	
	public String convertStoredToHtml(MartusDropdownField field, String rawData, MiniLocalization localization)
	{
		if(getDataSourceGridTag() != null)
			return XmlUtilities.getXmlEncoded(rawData);
		
		String[] reusableChoicesListsCodes = getReusableChoicesCodes();
		if(reusableChoicesListsCodes.length == 0)
		{
			int found = findCode(rawData);
			if(found < 0)
				return XmlUtilities.getXmlEncoded(rawData);
			return XmlUtilities.getXmlEncoded(getChoice(found).toString());
		}
		
		ListOfReusableChoicesLists reusableChoicesLists = new ListOfReusableChoicesLists(field.getReusableChoicesLists(), reusableChoicesListsCodes);
		String[] values = reusableChoicesLists.getDisplayValuesAtAllLevels(rawData);
		
		if(values.length == 1)
		{
			String result = values[0];
			if(reusableChoicesLists.get(0).getCode().equals(INTERNAL_CHOICES_FOR_BREAK_CODE))
				return result;

			return XmlUtilities.getXmlEncoded(result);
		}
		
		StringBuffer result = new StringBuffer();
		result.append("<table border='1'><tr>");
		for(int i = 0; i < values.length; ++i)
		{
			result.append("<td>");
			result.append(XmlUtilities.getXmlEncoded(values[i]));
			result.append("</td>");
		}
		result.append("</tr></table>");
		return result.toString();
	}
	
	@Override
	public String[] convertStoredToHumanReadable(String rawData, PoolOfReusableChoicesLists poolOfReusableChoicesLists, MiniLocalization localization)
	{
		if(getDataSourceGridTag() != null)
			return new String[] { rawData };
		
		String[] reusableChoicesListsCodes = getReusableChoicesCodes();
		if(reusableChoicesListsCodes.length == 0)
		{
			int found = findCode(rawData);
			if(found < 0)
				return new String[] { rawData };
			
			return new String[] { getChoice(found).toString() };
		}
		
		ListOfReusableChoicesLists reusableChoicesLists = new ListOfReusableChoicesLists(poolOfReusableChoicesLists, reusableChoicesListsCodes);
		String[] values = reusableChoicesLists.getDisplayValuesAtAllLevels(rawData);
		
		if(values.length == 1)
		{
			String result = values[0];
			if(reusableChoicesLists.get(0).getCode().equals(INTERNAL_CHOICES_FOR_BREAK_CODE))
				return new String[] { result };

			return new String[] { result };
		}
		
		return values;
	}
	
	static class DropDownSpecLoader extends SimpleXmlVectorLoader
	{
		public DropDownSpecLoader(CustomDropDownFieldSpec spec)
		{
			super(DROPDOWN_SPEC_CHOICES_TAG, DROPDOWN_SPEC_CHOICE_TAG);
			this.spec = spec;
		}

		public void endDocument() throws SAXParseException
		{
			Vector stringChoices = getVector();
			spec.setChoices(spec.createValidChoiceItemArrayFromStrings(stringChoices));
		}

		CustomDropDownFieldSpec spec;
	}
	
	static class DropDownDataSourceLoader extends SimpleXmlMapLoader
	{
		public DropDownDataSourceLoader(CustomDropDownFieldSpec spec)
		{
			super(DROPDOWN_SPEC_DATA_SOURCE);
			this.spec = spec;
		}

		public void endDocument() throws SAXParseException
		{
			Map map = getMap();
			String gridTag = (String)map.get(CustomDropDownFieldSpec.DROPDOWN_SPEC_DATA_SOURCE_GRID_TAG_TAG);
			String gridColumn = (String)map.get(CustomDropDownFieldSpec.DROPDOWN_SPEC_DATA_SOURCE_GRID_COLUMN_TAG);
			spec.setDataSource(gridTag, gridColumn);
		}

		CustomDropDownFieldSpec spec;
	}

	public static final String DROPDOWN_SPEC_DATA_SOURCE_GRID_TAG_TAG = "GridFieldTag";
	public static final String DROPDOWN_SPEC_DATA_SOURCE_GRID_COLUMN_TAG = "GridColumnLabel";

	private Vector reusableChoicesCodes;
	private String gridTag;
	private String gridColumn;
	public static String INTERNAL_CHOICES_FOR_BREAK_CODE = "**__InternalChoicesForBreak__**";
}
