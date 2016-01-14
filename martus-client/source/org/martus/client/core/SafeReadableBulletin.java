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

package org.martus.client.core;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.martus.client.search.FieldChooserSpecBuilder;
import org.martus.common.FieldSpecCollection;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.field.EmptyMartusFieldWithInfiniteSubFields;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeMessage;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.UniversalId;
import org.martus.swing.FontHandler;


/*
 * This class wraps a Bulletin object to allow the report runner
 * or a search/filter query to safely pull any of its data. 
 * Exposing Bulletin itself to a user-created Velocity report 
 * or search query would certainly allow users to obtain
 * non-helpful data (such as attachments), and might even allow 
 * them to somehow modify the bulletin.
 * 
 * This provides a safe, read-only, limited set of getters
 * 
 */
public class SafeReadableBulletin
{
	public SafeReadableBulletin(Bulletin bulletinToWrap, MiniLocalization localizationToUse)
	{
		realBulletin = bulletinToWrap;
		localization = localizationToUse;
	}
	
	public MartusField field(MiniFieldSpec miniSpec)
	{
		return field(miniSpec.getTag(), miniSpec.getLabel(), miniSpec.getType().getTypeName());
	}
	
	public MartusField field(String tag, String label, String typeString)
	{
		MartusField candidate = field(tag);
		if(doesLabelMatch(candidate, label) && doesTypeMatch(candidate, typeString))
			return candidate;
		
		return createEmptyField(tag);
	}

	private boolean doesTypeMatch(MartusField candidate, String typeString)
	{
		String candidateTypeName = candidate.getType().getTypeName();
		if(candidateTypeName.equals(typeString))
			return true;
		
		String[] stringTypes = new String[] {
			new FieldTypeNormal().getTypeName(),
			new FieldTypeMultiline().getTypeName(),
			new FieldTypeMessage().getTypeName(),
		};
		
		List stringTypeList = Arrays.asList(stringTypes);
		if(stringTypeList.contains(candidateTypeName) && 
				stringTypeList.contains(typeString))
			return true;
		
		return false;
	}

	private boolean doesLabelMatch(MartusField candidate, String label)
	{
		if(StandardFieldSpecs.isStandardFieldTag(candidate.getTag()))
			return true;
		
		if(isPseudofield(candidate))
			return true;
		
		return candidate.getLabel().equals(label);
	}

	private boolean isPseudofield(MartusField candidate)
	{
		return candidate.getTag().startsWith("_");
	}
	
	public MartusField field(String tag)
	{
		try
		{
			MartusField original = realBulletin.getField(tag);
			if(original == null)
			{
				return createEmptyField(tag);
			}
			
			MartusField result = original.createClone();
			
			if(omitPrivate)
			{
				if(realBulletin.isAllPrivate() || realBulletin.isFieldInPrivateSection(tag))
					result.setData("");
			}
			
			return result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return createEmptyField(tag);
		}
	}

	private MartusField createEmptyField(String tag)
	{
		MartusField empty = new EmptyMartusFieldWithInfiniteSubFields(tag);
		return empty;
	}
	
	public UniversalId getUniversalId()
	{
		return realBulletin.getUniversalId();
	}
	
	public String getLocalId()
	{
		return realBulletin.getLocalId();
	}
	
	public boolean contains(String lookFor)
	{
		return realBulletin.contains(lookFor, localization);
	}
	
	public FieldType getFieldType(String tag)
	{
		return realBulletin.getFieldType(tag);
	}
	
	public MartusField getPossiblyNestedField(FieldSpec nestedFieldTag)
	{
		return getPossiblyNestedField(nestedFieldTag.getTag());
	}
	
	public MartusField getPossiblyNestedField(MiniFieldSpec nestedFieldMiniSpec)
	{
		MartusField field = getPossiblyNestedField(nestedFieldMiniSpec.getTag());
		
		// TODO: The following call to setLabel is a necessary hack which should be improved
		if(field != null)
			field.setLabel(nestedFieldMiniSpec.getLabel());
		
		return field;
	}

	public MartusField getPossiblyNestedField(String tag)
	{
		String[] tags = parseNestedTags(tag);
		MartusField field = null;
		
		for(int i=0; i < tags.length; ++i)
		{
			if(field == null)
				field = field(tags[0]);
			else
				field = field.getSubField(tags[i], localization);
			if(field == null)
				return null;
		}
			
		return field;
	}
	
	public void removePrivateData()
	{
		omitPrivate = true;
	}
	
	public static String[] parseNestedTags(String tagsToParse)
	{
		return tagsToParse.split("\\.");
	}
	
	public Vector getTopFields()
	{
		return getFieldsFromSpecs(realBulletin.getTopSectionFieldSpecs());
	}

	public Vector getBottomFields()
	{
		return getFieldsFromSpecs(realBulletin.getBottomSectionFieldSpecs());
	}
	
	private Vector getFieldsFromSpecs(FieldSpecCollection topLevelFieldSpecs) 
	{
		FieldChooserSpecBuilder builder = new FieldChooserSpecBuilder(localization);
		Set topLevelFieldSpecSet = topLevelFieldSpecs.asSet();
		Vector choices = builder.convertToChoiceItems(topLevelFieldSpecSet, topLevelFieldSpecs.getAllReusableChoiceLists());

		Vector fields = new Vector();
		Iterator iter = choices.iterator();
		while(iter.hasNext())
		{
			ChoiceItem choice = (ChoiceItem)iter.next();
			MiniFieldSpec miniSpec = new MiniFieldSpec(choice.getSpec());
			MartusField field = getPossiblyNestedField(miniSpec);
			field.setConvertStandardLabelToStorable(FontHandler.isDoZawgyiConversion());
			fields.add(field);
		}
		return fields;
	}
	
	Bulletin realBulletin;
	MiniLocalization localization;
	boolean omitPrivate;
}
