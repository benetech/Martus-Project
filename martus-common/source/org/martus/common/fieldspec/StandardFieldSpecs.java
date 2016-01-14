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

package org.martus.common.fieldspec;

import org.martus.common.FieldSpecCollection;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.util.xml.XmlUtilities;


public class StandardFieldSpecs
{
	public static FieldSpecCollection getDefaultTopSectionFieldSpecs()
	{
		return new FieldSpecCollection(new FieldSpec[] 
			{
				FieldSpec.createStandardField(BulletinConstants.TAGLANGUAGE, new FieldTypeLanguage()),
				FieldSpec.createStandardField(BulletinConstants.TAGAUTHOR, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGORGANIZATION, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGTITLE, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGLOCATION, new FieldTypeNormal()), 
				FieldSpec.createStandardField(BulletinConstants.TAGKEYWORDS, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGEVENTDATE, new FieldTypeDateRange()),
				FieldSpec.createStandardField(BulletinConstants.TAGENTRYDATE, new FieldTypeDate()),
				FieldSpec.createStandardField(BulletinConstants.TAGSUMMARY, new FieldTypeMultiline()),
				FieldSpec.createStandardField(BulletinConstants.TAGPUBLICINFO, new FieldTypeMultiline()),
			});
	}

	public static FieldSpecCollection getDefaultBottomSectionFieldSpecs()
	{
		return new FieldSpecCollection(new FieldSpec[]
			{
				FieldSpec.createStandardField(BulletinConstants.TAGPRIVATEINFO, new FieldTypeMultiline()),
			});
	}

	public static FieldType getStandardType(String tag)
	{
		FieldSpec thisSpec = findStandardFieldSpec(tag);
		if(thisSpec == null)
			return new FieldTypeUnknown();
		return thisSpec.getType();
	}

	public static boolean isCustomFieldTag(String tag)
	{
		FieldSpec thisSpec = findStandardFieldSpec(tag);
		if(thisSpec == null)
			return true;
		return false;
	}
	
	public static boolean isStandardFieldTag(String tag)
	{
		if (findStandardFieldSpec(tag) != null)
			return true;
		return false;	
	}
	
	public static FieldSpec findStandardFieldSpec(String tag)
	{
		FieldSpec[] publicSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray();
		for(int i=0; i < publicSpecs.length; ++i)
			if(publicSpecs[i].getTag().equals(tag))
			{
				return publicSpecs[i];
			}
				
		FieldSpec[] privateSpecs = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray();
		for(int i=0; i < privateSpecs.length; ++i)
			if(privateSpecs[i].getTag().equals(tag))
			{
				return privateSpecs[i];
			}
				
		return null;
	}
	
	public static String getLocalizedLabel(String tag, String proposedLabel, MiniLocalization localization)
	{
		if(isStandardFieldTag(tag))
			return localization.getFieldLabel(tag);
		
		return proposedLabel;
	}
	
	public static String getLocalizedLabelHtml(String tag, String proposedLabel, MiniLocalization localization)
	{
		if(isStandardFieldTag(tag))
			return XmlUtilities.getXmlEncoded(localization.getFieldLabel(tag));
		
		return XmlUtilities.getXmlEncoded(proposedLabel);
	}
}
