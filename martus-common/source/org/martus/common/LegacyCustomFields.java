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

package org.martus.common;

import java.util.Vector;

import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.StandardFieldSpecs;


public class LegacyCustomFields
{

	static public String buildFieldListString(FieldSpecCollection fieldSpecs)
	{
		String fieldList = "";
		for(int i = 0; i < fieldSpecs.size(); ++i)
		{
			if(i > 0)
				fieldList += FIELD_SPEC_DELIMITER;
			FieldSpec spec = fieldSpecs.get(i);
			fieldList += spec.getTag();
			if(spec.getLabel().length() != 0)
				fieldList += FIELD_SPEC_ELEMENT_DELIMITER + spec.getLabel();
		}
		return fieldList;
	}
	
	static public FieldSpecCollection parseFieldSpecsFromString(String delimitedTags)
	{
		Vector fieldSpecs = new Vector();
		int tagStart = 0;
		while(tagStart >= 0 && tagStart < delimitedTags.length())
		{
			int delimiter = delimitedTags.indexOf(FIELD_SPEC_DELIMITER, tagStart);
			if(delimiter < 0)
				delimiter = delimitedTags.length();
			String thisFieldDescription = delimitedTags.substring(tagStart, delimiter);
			FieldSpec newFieldSpec = LegacyCustomFields.createFromLegacy(thisFieldDescription);
	
			fieldSpecs.add(newFieldSpec);
			tagStart = delimiter + 1;
		}

		return new FieldSpecCollection((FieldSpec[])fieldSpecs.toArray(new FieldSpec[0]));
	}
	
	public static FieldSpec createFromLegacy(String legacyDescription)
	{
		String extractedTag = extractFieldSpecElement(legacyDescription, TAG_ELEMENT_NUMBER);
		String extractedLabel = extractFieldSpecElement(legacyDescription, LABEL_ELEMENT_NUMBER);
		String extractedUnknown = extractFieldSpecElement(legacyDescription, UNKNOWN_ELEMENT_NUMBER);
		boolean extractedHasUnknown = false;
		if(!extractedUnknown.equals(""))
		{
			//System.out.println("FieldSpec.initializeFromDescription unknown: " + extractedTag + ": " + extractedUnknown);
			extractedHasUnknown = true;
		}
	
		FieldType extractedType = StandardFieldSpecs.getStandardType(extractedTag);
		if(extractedType.isUnknown() && !extractedHasUnknown)
			extractedType = new FieldTypeNormal();
	
		char[] cleansedTag = extractedTag.toCharArray();
		for(int i=0; i < cleansedTag.length; ++i)
		{
			char c = cleansedTag[i];
			boolean isValid = false;
			if(Character.isLetterOrDigit(c) || c == '_')
				isValid = true;
			if(i > 0 && (c == '-' || c == '.' || c >= 256) )
				isValid = true;

			if(!isValid)
				cleansedTag[i] = '_';				
		}
		String tag = new String(cleansedTag);
		return FieldSpec.createCustomField(tag, extractedLabel, extractedType, extractedHasUnknown);
	}
	
	static private String extractFieldSpecElement(String fieldDescription, int elementNumber)
	{
		int elementStart = 0;
		for(int i = 0; i < elementNumber; ++i)
		{
			int comma = fieldDescription.indexOf(FIELD_SPEC_ELEMENT_DELIMITER, elementStart);
			if(comma < 0)
				return "";
			elementStart = comma + 1;
		}
		
		int trailingComma = fieldDescription.indexOf(FIELD_SPEC_ELEMENT_DELIMITER, elementStart);
		if(trailingComma < 0)
			trailingComma = fieldDescription.length();
		return fieldDescription.substring(elementStart, trailingComma);
	}

	private static final char FIELD_SPEC_DELIMITER = ';';
	private static final char FIELD_SPEC_ELEMENT_DELIMITER = ',';
	static final int TAG_ELEMENT_NUMBER = 0;
	static final int LABEL_ELEMENT_NUMBER = 1;
	static final int UNKNOWN_ELEMENT_NUMBER = 2;
}
