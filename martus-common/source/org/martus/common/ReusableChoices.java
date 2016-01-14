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
package org.martus.common;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.martus.common.bulletin.BulletinXmlExportImportConstants;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.util.xml.XmlUtilities;

public class ReusableChoices
{
	public ReusableChoices(String codeToUse, String labelToUse)
	{
		code = codeToUse;
		label = labelToUse;
		choices = new Vector();
	}
	
	public String getCode()
	{
		return code;
	}

	public String getLabel()
	{
		return label;
	}

	public int size()
	{
		return choices.size();
	}

	public ChoiceItem[] getChoices()
	{
		return (ChoiceItem[]) choices.toArray(new ChoiceItem[0]);
	}

	public ChoiceItem get(int i)
	{
		return (ChoiceItem) choices.get(i);
	}

	public void add(ChoiceItem choice)
	{
		choices.add(choice);
	}

	public void addAll(ChoiceItem[] newChoices)
	{
		for(int i = 0; i < newChoices.length; ++i)
			add(newChoices[i]);
	}

	public void insertAtTop(ChoiceItem choiceItem)
	{
		choices.insertElementAt(choiceItem, 0);
	}

	public void set(int level, ChoiceItem choiceItem)
	{
		choices.set(level, choiceItem);
	}

	public ChoiceItem remove(int i)
	{
		return (ChoiceItem) choices.remove(i);
	}

	public ChoiceItem findByCode(String codeToFind)
	{
		for(int i = 0; i < choices.size(); ++i)
		{
			ChoiceItem choice = get(i);
			if(choice.getCode().equals(codeToFind))
				return choice;
		}
		
		return null;
	}
	
	public ChoiceItem findByFullOrPartialCode(String fullOrPartialCode)
	{
		for(int i = 0; i < choices.size(); ++i)
		{
			ChoiceItem choice = get(i);
			String thisCode = choice.getCode();
			if(thisCode.length() > 0 && thisCode.startsWith(fullOrPartialCode))
				return choice;
		}
		
		return null;
	}
	class ChoiceItemByLabelSorter implements Comparator
	{
		public int compare(Object rawChoiceItem1, Object rawChoiceItem2)
		{
			// NOTE: ChoiceItem.toString returns the label, 
			// which is exactly what we want to sort on
			if(rawChoiceItem1 == null || rawChoiceItem2 == null)
				return 0;
			
			return rawChoiceItem1.toString().compareTo(rawChoiceItem2.toString());
		}
		
	}

	public void sortChoicesByLabel()
	{
		ChoiceItemByLabelSorter sorter = new ChoiceItemByLabelSorter();
		Collections.sort(choices, sorter);
	}

	public int hashCode()
	{
		return choices.hashCode();
	}
	
	public boolean equals(Object rawOther)
	{
		if(rawOther == this)
			return true;
		
		if(! (rawOther instanceof ReusableChoices))
			return false;
		
		ReusableChoices other = (ReusableChoices) rawOther;
		if(!getCode().equals(other.getCode()))
			return false;
		
		if(!getLabel().equals(other.getLabel()))
			return false;
		
		return choices.equals(other.choices);
	}

	public String toExportedXml() throws Exception
	{
		StringWriter xml = new StringWriter();
		XmlWriterFilter out = new XmlWriterFilter(xml);
		
		String listTag = BulletinXmlExportImportConstants.REUSABLE_CHOICES_LIST;
		out.writeStartTag(getTagWithCodeAndLabelAttributes(listTag, getCode(), getLabel()));

		for(int i = 0; i < choices.size(); ++i)
		{
			ChoiceItem choice = get(i);
			String choiceTag = BulletinXmlExportImportConstants.REUSABLE_CHOICE_ITEM;
			String xmlTag = getTagWithCodeAndLabelAttributes(choiceTag, choice.getCode(), choice.toString());
			out.writeStartTag(xmlTag);
			out.writeEndTag(choiceTag);
		}
		
		out.writeEndTag(listTag);
		
		return xml.toString();
	}
	
	private String getTagWithCodeAndLabelAttributes(String tag, String codeValue, String labelValue)
	{
		String element = tag + 
		  " " + BulletinXmlExportImportConstants.CHOICE_ITEM_CODE_ATTRIBUTE + "='" + XmlUtilities.getXmlEncoded(codeValue) + "'" + 
		  " " + BulletinXmlExportImportConstants.CHOICE_ITEM_LABEL_ATTRIBUTE + "='" + XmlUtilities.getXmlEncoded(labelValue) + "'";
		return element;
	}

	private String code;
	private String label;
	private Vector choices;
}
