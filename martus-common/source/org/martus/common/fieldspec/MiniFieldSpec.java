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
package org.martus.common.fieldspec;

import java.util.Vector;

import org.json.JSONObject;


public class MiniFieldSpec implements Comparable
{
	public MiniFieldSpec(FieldSpec basedOn)
	{
		FieldSpec topLevel = basedOn;
		while(topLevel.getParent() != null)
			topLevel = topLevel.getParent();
		topLevel = standardize(topLevel);
		topLevelLabel = topLevel.getLabel();
		topLevelType = topLevel.getType();

		basedOn = standardize(basedOn);
		tag = basedOn.getTag();
		label = basedOn.getLabel();
		type = basedOn.getType();
		dropdownReusableChoicesCodes = basedOn.getReusableChoicesCodes();
	}
	
	public MiniFieldSpec(JSONObject json)
	{
		tag = json.getString(TAG_TAG);
		label = json.getString(TAG_LABEL);
		type = FieldType.createFromTypeName(json.getString(TAG_TYPE));
		topLevelLabel = json.getString(TAG_TOP_LEVEL_LABEL);
		topLevelType = FieldType.createFromTypeName(json.getString(TAG_TOP_LEVEL_TYPE));
		dropdownReusableChoicesCodes = split(json.optString(TAG_REUSABLE_CHOICES_CODES));
	}
	
	private String[] split(String codesAsString)
	{
		Vector codes = new Vector();
		int nextCodeStart = 0;
		while(nextCodeStart < codesAsString.length())
		{
			int nextSpace = codesAsString.indexOf(' ', nextCodeStart);
			if(nextSpace < 0)
				nextSpace = codesAsString.length();
			String code = codesAsString.substring(nextCodeStart, nextSpace);
			codes.add(code);
			nextCodeStart = nextSpace + 1;
		}
		
		return (String[]) codes.toArray(new String[0]);
	}

	public String getTag()
	{
		return tag;
	}
	
	public String getTopLevelTag()
	{
		int dotAt = getTag().indexOf('.');
		if(dotAt < 0)
			return getTag();
		
		return getTag().substring(0, dotAt);
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public void setLabel(String storableLabel)
	{
		label = storableLabel;
	}

	public void setTopLevelLabel(String storableLabel)
	{
		topLevelLabel = storableLabel;
	}

	public FieldType getType()
	{
		return type;
	}
	
	public String getTopLevelLabel()
	{
		return topLevelLabel;
	}
	
	public FieldType getTopLevelType()
	{
		return topLevelType;
	}
	
	public String[] getReusableChoicesCodes()
	{
		return dropdownReusableChoicesCodes;
	}
	
	public String getReusableChoicesCodesAsString()
	{
		String result = "";
		for(int level = 0; level < getReusableChoicesCodes().length; ++level)
		{
			if(level > 0)
				result += " ";
			result += getReusableChoicesCodes()[level];
		}
		
		return result;
	}
	
	public MiniFieldSpec cloneInReportStyle()
	{
		MiniFieldSpec clone = new MiniFieldSpec(toJson());
		int reusableChoicesCodeCount = clone.getReusableChoicesCodes().length;
		if(reusableChoicesCodeCount > 1)
		{
			String lastCode = clone.getReusableChoicesCodes()[reusableChoicesCodeCount-1];
			String[] newReusableChoicesCodes = dropdownReusableChoicesCodes = new String[] {lastCode};
			clone.dropdownReusableChoicesCodes = newReusableChoicesCodes;
		}

		return clone;
	}

	private FieldSpec standardize(FieldSpec spec)
	{
		if(spec.getParent() != null)
			return spec;
		
		String candidateTag = spec.getTag();
		if(StandardFieldSpecs.isCustomFieldTag(candidateTag))
			return spec;
		
		return StandardFieldSpecs.findStandardFieldSpec(candidateTag);
	}

	public boolean equals(Object rawOther)
	{
		if(! (rawOther instanceof MiniFieldSpec))
			return false;
			
		return compareTo((MiniFieldSpec)rawOther) == 0;
	}

	public int hashCode()
	{
		return tag.hashCode();
	}
	
	public int compareTo(Object rawOther)
	{
		if(rawOther == null)
			throw new NullPointerException();
		
		if(! (rawOther instanceof MiniFieldSpec))
			return 0;
		
		return compareTo((MiniFieldSpec)rawOther);
	}
	
	public int compareTo(MiniFieldSpec other)
	{
		if(label.length() > 0 && other.label.length() > 0)
		{
			int labelResult = label.compareTo(other.label);
			if(labelResult != 0)
				return labelResult;
		}
		
		int tagResult = tag.compareTo(other.tag);
		if(tagResult != 0)
			return tagResult;
		
		int typeResult = type.getTypeName().compareTo(other.type.getTypeName());
		if(typeResult != 0)
			return typeResult;
		
		int topLevelLabelResult = topLevelLabel.compareTo(other.topLevelLabel);
		if(topLevelLabelResult != 0)
			return topLevelLabelResult;
		
		int topLevelTypeResult = topLevelType.getTypeName().compareTo(other.topLevelType.getTypeName());
		if(topLevelTypeResult != 0)
			return topLevelTypeResult;
		
		int reusableCodesResult = getReusableChoicesCodesAsString().compareTo(other.getReusableChoicesCodesAsString());
		if(reusableCodesResult != 0)
			return reusableCodesResult;
		
		return 0;
	}
	
	public String getCodeString()
	{
		return toJson().toString();
	}

	public String toString()
	{
		String result = label + "(" + FieldSpec.getTypeString(type) + ", " + tag;
		if(getReusableChoicesCodes().length > 0)
			result += ", [" + getReusableChoicesCodesAsString() + "]";
		result += ")";
		return result;
	}
	
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		json.put(TAG_TAG, getTag());
		json.put(TAG_LABEL, getLabel());
		json.put(TAG_TYPE, type.getTypeName());
		json.put(TAG_TOP_LEVEL_LABEL, getTopLevelLabel());
		json.put(TAG_TOP_LEVEL_TYPE, getTopLevelType().getTypeName());
		if(getReusableChoicesCodes().length > 0)
			json.put(TAG_REUSABLE_CHOICES_CODES, getReusableChoicesCodesAsString());
		return json;
	}
	
	private static final String TAG_TAG = "Tag";
	private static final String TAG_LABEL = "Label";
	private static final String TAG_TYPE = "Type";
	private static final String TAG_TOP_LEVEL_LABEL = "TopLevelLabel";
	private static final String TAG_TOP_LEVEL_TYPE = "TopLevelType";
	private static final String TAG_REUSABLE_CHOICES_CODES = "ReusableChoicesCodes";

	String tag;
	String label;
	FieldType type;
	String topLevelLabel;
	FieldType topLevelType;
	String[] dropdownReusableChoicesCodes;
}
