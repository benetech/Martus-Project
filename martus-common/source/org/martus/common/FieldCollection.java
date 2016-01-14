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

import java.util.HashMap;
import java.util.Vector;

import org.martus.common.field.MartusDateField;
import org.martus.common.field.MartusDateRangeField;
import org.martus.common.field.MartusDropdownField;
import org.martus.common.field.MartusField;
import org.martus.common.field.MartusGridField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.InvalidIsoDateException;
import org.martus.util.xml.SimpleXmlParser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class FieldCollection
{
	public FieldCollection(FieldSpecCollection specsToUse) throws Exception
	{
		specs = reuseExistingSpecCollectionIfPossible(specsToUse);
		fields = new Vector();
		for(int i=0; i < specs.size(); ++i)
			add(specs.get(i), specsToUse.getAllReusableChoiceLists());
	}

	private FieldSpecCollection reuseExistingSpecCollectionIfPossible(FieldSpecCollection specsToUse) throws Exception
	{
		StringBuffer key = new StringBuffer();
		for(int i = 0; i < specsToUse.size(); ++i)
			key.append(specsToUse.get(i).getId());
		key.append(specsToUse.getAllReusableChoiceLists().toXml());
		if(existingFieldSpecTemplates.containsKey(specsToUse))
		{
			specsToUse = (FieldSpecCollection)existingFieldSpecTemplates.get(specsToUse);
		}
		else
		{
			existingFieldSpecTemplates.put(specsToUse, specsToUse);
		}
		return specsToUse;
	}
	
	public FieldCollection(FieldSpec[] specsToUse) throws Exception
	{
		this(new FieldSpecCollection(specsToUse));
	}
	
	private void add(FieldSpec newSpec, PoolOfReusableChoicesLists reusableChoicesLists)
	{
		FieldType type = newSpec.getType();
		if(type.isDateRange())
			fields.add(new MartusDateRangeField(newSpec));
		else if(type.isDate())
			fields.add(new MartusDateField(newSpec));
		else if(type.isDropdown())
			fields.add(new MartusDropdownField(newSpec, reusableChoicesLists));
		else if(type.isGrid())
			fields.add(new MartusGridField(newSpec, reusableChoicesLists));
		else
			fields.add(new MartusField(newSpec, reusableChoicesLists));
	}
	
	public int count()
	{
		return fields.size();
	}
	
	public MartusField getField(int i)
	{
		return ((MartusField)fields.get(i));
	}
	
	public MartusField findByTag(String fieldTag)
	{
		for(int i=0; i < count(); ++i)
		{
			MartusField thisField = getField(i);
			if(thisField.getTag().equals(fieldTag))
				return thisField;
		}
		
		return null;
	}
	
	public FieldSpecCollection getSpecs()
	{
		return specs;
	}
	
	public boolean isEmpty()
	{
		for(int i=0; i < count(); ++i)
			if(getField(i).getData().length() != 0)
				return false;
		
		return true;
	}
	
	public void clearAllData()
	{
		for(int i=0; i < count(); ++i)
			getField(i).clearData();
	}
	
	public String toString()
	{
		return getSpecsXml();
	}

	public String getSpecsXml()
	{
		return specs.toXml();
	}
	
	public static class CustomFieldsParseException extends SAXException 
	{
		public CustomFieldsParseException()
		{
			this("Custom fields parse exception");
		}

		public CustomFieldsParseException(String message)
		{
			super(message);
		}
		
		public CustomFieldsParseException(Exception causedBy)
		{
			super(causedBy);
		}
	}
	
	public static FieldSpecCollection parseXml(String xml) throws CustomFieldsParseException
	{
		XmlCustomFieldsLoader loader = new XmlCustomFieldsLoader();
		try
		{
			SimpleXmlParser.parse(loader, xml);
			return loader.getFieldSpecs();
		}
		catch(SAXParseException e)
		{
			throw new CustomFieldsParseException(e);
		}
		catch (InvalidIsoDateException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new CustomFieldsParseException();
		}
	}
	
	public static HashMap existingFieldSpecTemplates = new HashMap();
	
	private Vector fields;
	private FieldSpecCollection specs;
}
