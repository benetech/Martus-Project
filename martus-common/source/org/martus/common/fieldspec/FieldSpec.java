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


import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.common.field.MartusField;
import org.martus.util.xml.AttributesOnlyXmlLoader;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlStringLoader;
import org.martus.util.xml.XmlUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;


public class FieldSpec
{
	public static FieldSpec createStandardField(String tagToUse, FieldType typeToUse)
	{
		return createCustomField(tagToUse, "", typeToUse);
	}
	
	public static FieldSpec createCustomField(String tagToUse, String labelToUse, FieldType typeToUse)
	{
		return createCustomField(tagToUse, labelToUse, typeToUse, false);
	}
	
	public static FieldSpec createCustomField(String tagToUse, String labelToUse, FieldType typeToUse, boolean hasUnknownToUse)
	{
		FieldSpec spec = typeToUse.createEmptyFieldSpec();
		spec.setTag(tagToUse);
		spec.setLabel(labelToUse);
		spec.hasUnknown = hasUnknownToUse;
		return spec;
	}
	
	public static FieldSpec createFieldSpec(FieldType typeToUse)
	{
		return typeToUse.createEmptyFieldSpec();
	}

	public static FieldSpec createFieldSpec(String labelToUse, FieldType typeToUse)
	{
		return createCustomField("", labelToUse, typeToUse);
	}
	
	public static FieldSpec createSubField(FieldSpec parentToUse, String tagToUse, String labelToUse, FieldType typeToUse)
	{
		FieldSpec spec = typeToUse.createEmptyFieldSpec();
		spec.setParent(parentToUse);
		spec.setTag(tagToUse);
		spec.setLabel(labelToUse);
		return spec;
	}

	protected FieldSpec(FieldType typeToUse)
	{
		this("", "", typeToUse, false);
	}
	
	private FieldSpec(String tagToUse, String labelToUse, FieldType typeToUse, boolean hasUnknownToUse)
	{
		this(null, tagToUse, labelToUse, typeToUse, hasUnknownToUse);
	}
	
	private FieldSpec(FieldSpec parentToUse, String tagToUse, String labelToUse, FieldType typeToUse, boolean hasUnknownToUse)
	{
		parent = parentToUse;
		this.tag = tagToUse;
		this.label = labelToUse;
		this.type = typeToUse;
		hasUnknown = hasUnknownToUse;
	}
	
	public String toString()
	{
		return toXml();
	}

	public String toXml()
	{
		String rootTag = FIELD_SPEC_XML_TAG;
		return toXml(rootTag);
	}

	public String toXml(String rootTag)
	{
		// NOTE: Optimized for speed because this was a BIG bottleneck!
		String typeString = XmlUtilities.getXmlEncoded(getTypeString(getType()));
		StringBuffer rootTagLine = new StringBuffer();
		rootTagLine.append((("<" + rootTag + " " + FIELD_SPEC_TYPE_ATTR + "='" + typeString + "'>") + "\n"));
		rootTagLine.append(("<" + FIELD_SPEC_TAG_XML_TAG + ">"));
		rootTagLine.append(XmlUtilities.getXmlEncoded(getTag()));
		rootTagLine.append((("</" + FIELD_SPEC_TAG_XML_TAG + ">") + "\n"));
		rootTagLine.append(("<" + FIELD_SPEC_LABEL_XML_TAG + ">")); 
		rootTagLine.append(XmlUtilities.getXmlEncoded(getLabel()));
		rootTagLine.append((("</" + FIELD_SPEC_LABEL_XML_TAG + ">") + "\n"));
		if(keepWithPrevious())
			rootTagLine.append("<" + FIELD_SPEC_KEEP_WITH_PREVIOUS_TAG + "/>\n");
		if(isRequiredField())
			rootTagLine.append("<" + FIELD_SPEC_REQUIRED_FIELD_TAG + "/>\n");
		if(defaultValue != null)
		{
			rootTagLine.append("<" + FIELD_SPEC_DEFAULT_VALUE_TAG + ">");
			rootTagLine.append(XmlUtilities.getXmlEncoded(defaultValue));
			rootTagLine.append("</" + FIELD_SPEC_DEFAULT_VALUE_TAG + ">");
		}
		rootTagLine.append(getDetailsXml());
		rootTagLine.append((("</" + rootTag + ">") + "\n"));
		
		return rootTagLine.toString();
	}
	
	public String getDetailsXml()
	{
		return "";
	}

	public String getTag()
	{
		if(getParent() == null)
			return getSubFieldTag();
		return getParent().getTag() + "." + getSubFieldTag();
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public FieldType getType()
	{
		return type;
	}
	
	public FieldSpec getParent()
	{
		return parent;
	}
	
	public String getSubFieldTag()
	{
		return tag;
	}
	
	public boolean keepWithPrevious() 
	{
		return keepWithPrevious;
	}

	public boolean isRequiredField() 
	{
		if(getType().isSectionStart())
			return false;
		
		return isRequired;
	}

	public String[] convertStoredToHumanReadable(String data, PoolOfReusableChoicesLists reusableChoicesLists, MiniLocalization localization)
	{
		return getType().convertStoredToHumanReadable(data, reusableChoicesLists, localization);
	}

	public String convertStoredToSearchable(String storedData, PoolOfReusableChoicesLists reusableChoicesLists, MiniLocalization localization)
	{
		return getType().convertStoredToSearchable(storedData, localization);
	}
	
	public String convertStoredToHtml(MartusField field, MiniLocalization localization)
	{
		return getType().convertStoredToHtml(field.getData(), localization);
	}
	
	public String convertStoredToExportable(String storedData, PoolOfReusableChoicesLists reusableChoicesLists, MiniLocalization localization)
	{
		return getType().convertStoredToExportable(storedData, localization);
	}
	
	public void setDefaultValue(String text)
	{
		defaultValue = text;
	}

	public String getDefaultValue()
	{
		if(defaultValue != null)
			return defaultValue;
		
		return getSystemDefaultValue();
	}
	
	protected boolean allowUserDefaultValue()
	{
		if(getType().isBoolean())
			return false;
		if(getType().isLanguageDropdown())
			return false;
		if(getType().isSectionStart())
			return false;
		
		return true;
	}

	protected String getSystemDefaultValue()
	{
		return(getType().getDefaultValue());
	}
	
	public boolean hasUnknownStuff()
	{
		return hasUnknown;
	}
	
	public void setParent(FieldSpec newParent)
	{
		parent = newParent;
		clearId();
	}
	
	public void setLabel(String label)
	{
		this.label = label;
		clearId();
	}

	public void setTag(String tag)
	{
		this.tag = tag;
		clearId();
	}
	
	public void setType(FieldType type)
	{
		this.type = type;
		clearId();
	}
	
	public void setKeepWithPrevious()
	{
		keepWithPrevious = true;
	}
	
	public void setRequired()
	{
		isRequired = true;
	}
	
	public String getId()
	{
		if(id == null)
			refreshId();
		return id;
	}
	
	protected void clearId()
	{
		id = null;
	}
	
	private void refreshId()
	{
		try
		{
			id = MartusCrypto.createDigestString(getStringRepresentationToComputeId());
		} 
		catch (CreateDigestException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	protected String getStringRepresentationToComputeId()
	{
		return toXml();
	}
	
	public void validate(String fullFieldLabel, String candidateValue, MiniLocalization localization) throws DataInvalidException 
	{
		if(isRequiredField())
		{
			validateRequiredValue(fullFieldLabel, candidateValue);
		}
	}

	protected void validateRequiredValue(String fieldLabel, String value) throws RequiredFieldIsBlankException
	{
		final String REGEXP_ONLY_SPACES = "\\s*";
		if(value.matches(REGEXP_ONLY_SPACES))
			throw new RequiredFieldIsBlankException(fieldLabel);
	}

	public int compareTo(Object other)
	{
		if(other == null)
			return 1;
		
		FieldSpec otherSpec = (FieldSpec)other;
		
		// NOTE: Speed optimization
		int tagComparison = getTag().compareTo(otherSpec.getTag());
		if(tagComparison != 0)
			return tagComparison;
		
		return getId().compareTo(otherSpec.getId());
	}
	
	public boolean equals(Object other)
	{
		if(!(other instanceof FieldSpec))
			return false;

		return (compareTo(other) == 0);
	}

	public int hashCode()
	{
		return getId().hashCode();
	}

	public boolean hasReusableCodes()
	{
		return (getReusableChoicesCodes().length > 0);
	}

	public String[] getReusableChoicesCodes()
	{
		return new String[0];
	}

	public static String getTypeString(FieldType type)
	{
		return type.getTypeName();
	}
	
	public static FieldType getTypeCode(String type)
	{
		return FieldType.createFromTypeName(type);
	}
	
	
	public static FieldSpec createFromXml(String xml) throws Exception
	{
		XmlFieldSpecLoader loader = new XmlFieldSpecLoader();
		loader.parse(xml);
		return loader.getFieldSpec();
	}

	public static class XmlFieldSpecLoader extends SimpleXmlDefaultLoader
	{
		public XmlFieldSpecLoader()
		{
			this(FieldSpec.FIELD_SPEC_XML_TAG);
		}
		
		public XmlFieldSpecLoader(String rootTag)
		{
			super(rootTag);
		}
		
		public FieldSpec getFieldSpec()
		{
			return spec;
		}
		
		public void startDocument(Attributes attrs) throws SAXParseException
		{
			FieldType type = getTypeCode(attrs.getValue(FieldSpec.FIELD_SPEC_TYPE_ATTR));
			spec = type.createEmptyFieldSpec();
			super.startDocument(attrs);
		}
	
		public SimpleXmlDefaultLoader startElement(String tag)
			throws SAXParseException
		{
			if(tag.equals(FieldSpec.FIELD_SPEC_TAG_XML_TAG) || tag.equals(FieldSpec.FIELD_SPEC_LABEL_XML_TAG))
				return new SimpleXmlStringLoader(tag);
			
			if(tag.equals(FieldSpec.FIELD_SPEC_KEEP_WITH_PREVIOUS_TAG))
				return new SimpleXmlDefaultLoader(tag);

			if(tag.equals(FieldSpec.FIELD_SPEC_REQUIRED_FIELD_TAG))
				return new SimpleXmlDefaultLoader(tag);
			
			if(tag.equals(FieldSpec.FIELD_SPEC_DEFAULT_VALUE_TAG))
			{
				if(!spec.allowUserDefaultValue())
					throw new SAXParseException("DefaultValue not allowed for " + spec.getType().getTypeName(), null);
				return new SimpleXmlStringLoader(tag);
			}

			if(spec.getType().isGrid())
			{
				if(tag.equals(GridFieldSpec.GRID_SPEC_DETAILS_TAG))
					return new GridFieldSpec.GridSpecDetailsLoader((GridFieldSpec)spec);
			}
			
			if(spec.getType().isDropdown())
			{
				CustomDropDownFieldSpec dropDownSpec = (CustomDropDownFieldSpec)spec;
				if(tag.equals(CustomDropDownFieldSpec.DROPDOWN_SPEC_CHOICES_TAG))
					return new CustomDropDownFieldSpec.DropDownSpecLoader(dropDownSpec);
				if(tag.equals(CustomDropDownFieldSpec.DROPDOWN_SPEC_DATA_SOURCE))
					return new CustomDropDownFieldSpec.DropDownDataSourceLoader(dropDownSpec);
				if(tag.equals(USE_REUSABLE_CHOICES_TAG))
					return new AttributesOnlyXmlLoader(tag);
			}
			
			if(spec.getType().isNestedDropdown())
			{
				if(tag.equals(USE_REUSABLE_CHOICES_TAG))
					return new AttributesOnlyXmlLoader(tag);
			}
			
			if(spec.getType().isMessage())
			{
				if(tag.equals(MessageFieldSpec.MESSAGE_SPEC_MESSAGE_TAG))
					return new MessageFieldSpec.MessageSpecLoader((MessageFieldSpec)spec);
			}
			
			if(spec.getType().isDate() || spec.getType().isDateRange())
			{
				AbstractDateOrientedFieldSpec dateFieldSpec = (AbstractDateOrientedFieldSpec)spec;
				if(tag.equals(AbstractDateOrientedFieldSpec.MINIMUM_DATE))
					return new DateFieldSpec.MinimumDateLoader(dateFieldSpec);
				if(tag.equals(AbstractDateOrientedFieldSpec.MAXIMUM_DATE))
					return new DateFieldSpec.MaximumDateLoader(dateFieldSpec);
				
			}
			
			return super.startElement(tag);
		}
	
		public void endElement(String thisTag, SimpleXmlDefaultLoader ended)
			throws SAXParseException
		{
			if(thisTag.equals(FieldSpec.FIELD_SPEC_TAG_XML_TAG))
				spec.setTag(getText(ended));
			else if(thisTag.equals(FieldSpec.FIELD_SPEC_LABEL_XML_TAG))
				spec.setLabel(getText(ended));
			else if(thisTag.equals(FieldSpec.FIELD_SPEC_KEEP_WITH_PREVIOUS_TAG))
				spec.setKeepWithPrevious();
			else if(thisTag.equals(FieldSpec.FIELD_SPEC_REQUIRED_FIELD_TAG))
				spec.setRequired();
			else if(thisTag.equals(FieldSpec.FIELD_SPEC_DEFAULT_VALUE_TAG))
				spec.setDefaultValue(getText(ended));
			else if(spec.getType().isDropdown() && thisTag.equals(USE_REUSABLE_CHOICES_TAG))
			{
				AttributesOnlyXmlLoader loader = (AttributesOnlyXmlLoader)ended;
				CustomDropDownFieldSpec dropDownSpec = (CustomDropDownFieldSpec)spec;
				String reusableChoicesCode = loader.getAttribute(REUSABLE_CHOICES_CODE_ATTRIBUTE);
				dropDownSpec.addReusableChoicesCode(reusableChoicesCode);
			}
			else
				super.endElement(thisTag, ended);
		}
	
		private String getText(SimpleXmlDefaultLoader ended)
		{
			return ((SimpleXmlStringLoader)ended).getText();
		}
		FieldSpec spec;
	}

	private String tag;
	private FieldType type;
	private String label;
	private boolean hasUnknown;
	private FieldSpec parent;
	private boolean keepWithPrevious;
	private boolean isRequired;
	private String defaultValue;
	
	private String id;

	public static final String FIELD_SPEC_XML_TAG = "Field";
	public static final String FIELD_SPEC_TAG_XML_TAG = "Tag";
	public static final String FIELD_SPEC_LABEL_XML_TAG = "Label";
	public static final String FIELD_SPEC_KEEP_WITH_PREVIOUS_TAG = "KeepWithPrevious";
	public static final String FIELD_SPEC_REQUIRED_FIELD_TAG = "RequiredField";
	public static final String FIELD_SPEC_DEFAULT_VALUE_TAG = "DefaultValue";
	public static final String USE_REUSABLE_CHOICES_TAG = "UseReusableChoices";
	public static final String REUSABLE_CHOICES_CODE_ATTRIBUTE = "code";

	public static final String FIELD_SPEC_TYPE_ATTR = "type";

	public static final String TRUESTRING = "1";
	public static final String FALSESTRING = "0";
}
