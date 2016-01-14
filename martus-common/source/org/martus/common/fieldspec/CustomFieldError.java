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



public class CustomFieldError
{
	static public CustomFieldError errorRequiredField(String fieldTagMissing)
	{
		return new CustomFieldError(CODE_REQUIRED_FIELD, fieldTagMissing, UNUSED_FIELD, UNUSED_FIELD);
	}
	
	static public CustomFieldError errorBlankTag(String label, String type)
	{
		return new CustomFieldError(CODE_MISSING_TAG, UNUSED_FIELD, label, type);
	}
	
	static public CustomFieldError errorMissingCustomSpec(String tag)
	{
		return new CustomFieldError(CODE_MISSING_CUSTOM_FIELD_IN_SPEC, tag, UNUSED_FIELD, UNUSED_FIELD);
	}

	static public CustomFieldError errorIllegalTag(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_ILLEGAL_TAG, tag, label, type);
	}
	
	public static Object errorIllegalReusableChoiceItemCode(String listCode, String itemCode, String itemLabel)
	{
		return new CustomFieldError(CODE_ILLEGAL_TAG, listCode + "." + itemCode, itemLabel, TYPE_STRING_FOR_REUSABLE_LISTS);
	}
	
	public static Object errorIllegalReusableChoiceListCode(String listCode, String listLabel)
	{
		return new CustomFieldError(CODE_ILLEGAL_TAG, listCode, listLabel, TYPE_STRING_FOR_REUSABLE_LISTS);
	}
	
	static public CustomFieldError errorDuplicateFields(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_DUPLICATE_FIELD, tag, label, type);
	}

	static public CustomFieldError errorMissingLabel(String tag, String type)
	{
		return new CustomFieldError(CODE_MISSING_LABEL, tag, UNUSED_FIELD, type);
	}
	
	static public CustomFieldError errorUnknownType(String tag, String label)
	{
		return new CustomFieldError(CODE_UNKNOWN_TYPE, tag, label, UNUSED_FIELD);
	}
	
	static public CustomFieldError errorLabelOnStandardField(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_LABEL_STANDARD_FIELD, tag, label, type);
	}
	
	static public CustomFieldError errorDuplicateDropDownEntry(String tag, String label)
	{
		return new CustomFieldError(CODE_DUPLICATE_DROPDOWN_ENTRY, tag, label, FieldSpec.getTypeString(new FieldTypeDropdown()));
	}

	static public CustomFieldError errorDuplicateDropDownEntryInReusableChoices(String tag, String label)
	{
		return new CustomFieldError(CODE_DUPLICATE_DROPDOWN_ENTRY, tag, label, TYPE_STRING_FOR_REUSABLE_LISTS);
	}

	static public CustomFieldError errorDuplicateReusableChoicesListLabel(String label)
	{
		return new CustomFieldError(CODE_DUPLICATE_REUSABLE_CHOICES_LIST_LABELS, "", label, TYPE_STRING_FOR_REUSABLE_LISTS);
	}

	static public CustomFieldError noDropDownEntries(String tag, String label)
	{
		return new CustomFieldError(CODE_NO_DROPDOWN_ENTRIES, tag, label, FieldSpec.getTypeString(new FieldTypeDropdown()));
	}

	static public CustomFieldError errorReservedTag(String reservedTag, String labelUsed)
	{
		return new CustomFieldError(CODE_RESERVED_TAG, reservedTag, labelUsed, UNUSED_FIELD);
	}

	static public CustomFieldError errorTopSectionFieldInBottomSection(String martusTag)
	{
		return new CustomFieldError(CODE_MARTUS_FIELD_IN_BOTTOM_SECTION, martusTag, UNUSED_FIELD, UNUSED_FIELD);
	}
	
	static  public CustomFieldError errorBottomSectionFieldInTopSection(String martusTag)
	{
		return new CustomFieldError(CODE_PRIVATE_FIELD_IN_TOP_SECTION, martusTag, UNUSED_FIELD, UNUSED_FIELD);
	}

	static public CustomFieldError errorParseXml(String message)
	{
		return new CustomFieldError(CODE_PARSE_XML, message, UNUSED_FIELD, UNUSED_FIELD);
	}
	
	static public CustomFieldError errorUnauthorizedKey()
	{
		return new CustomFieldError(CODE_UNAUTHORIZED_KEY, UNUSED_FIELD, UNUSED_FIELD, UNUSED_FIELD);
	}
	
	static public CustomFieldError errorSignature()
	{
		return new CustomFieldError(CODE_SIGNATURE_ERROR, UNUSED_FIELD, UNUSED_FIELD, UNUSED_FIELD);
	}

	static public CustomFieldError errorIO(String errorMessage)
	{
		return new CustomFieldError(CODE_IO_ERROR, UNUSED_FIELD, UNUSED_FIELD, errorMessage);
	}

	public static Object errorDataSourceNoGridTag(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_UNKNOWN_DATA_SOURCE_GRID_TAG, tag, label, type);
	}

	public static Object errorDataSourceNoGridColumn(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_UNKNOWN_DATA_SOURCE_GRID_COLUMN, tag, label, type);
	}

	public static Object errorDropDownHasChoicesAndDataSource(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_DROPDOWN_HAS_CHOICES_AND_DATA_SOURCE, tag, label, type);
	}

	public static Object errorInvalidIsoDate(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_INVALID_ISO_DATE, tag, label, type);
	}

	public static Object errorMissingReusableChoices(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_MISSING_REUSABLE_CHOICES, tag, label, type);
	}

	public static Object errorNullReusableChoices(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_NULL_REUSABLE_CHOICES, tag, label, type);
	}

	public static Object errorInvalidReusableChoice(String tag, int index)
	{
		return new CustomFieldError(CODE_INVALID_REUSABLE_CHOICE, tag, "", "");
	}

	public static Object errorDataSourceReusableDropdown(String tag, String label)
	{
		return new CustomFieldError(CODE_NESTED_DATA_SOURCE, tag, label, new FieldTypeDropdown().getTypeName());
	}

	public static Object errorInvalidDefaultValue(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_INVALID_DEFAULT_VALUE, tag, label, type);
	}

	public static Object errorImproperlyNestedDropdownCode(String fieldTag, String choiceCode, String fieldLabel, String choiceLabel)
	{
		return new CustomFieldError(CODE_IMPROPERLY_NESTED_CHOICE_CODE, fieldTag + ":" + choiceCode, fieldLabel + ":" + choiceLabel, new FieldTypeDropdown().getTypeName());
	}

	private CustomFieldError(String code, String tag, String label, String type)
	{
		this.code = code;
		this.tag = tag;
		this.label = label;
		this.type = type;
	}
	
	public String getCode()
	{
		return code;
	}
	public String getLabel()
	{
		return label;
	}
	public String getTag()
	{
		return tag;
	}
	public String getType()
	{
		return type;
	}

	public String toString() 
	{
		return "CustomFieldError: " + code + ", " + tag + ", " + label + ", " + type;
	}

	static public final String CODE_REQUIRED_FIELD = "100";
	static public final String CODE_MISSING_TAG = "101";
	static public final String CODE_DUPLICATE_FIELD = "102";
	static public final String CODE_MISSING_LABEL = "103";
	static public final String CODE_UNKNOWN_TYPE = "104";
	static public final String CODE_LABEL_STANDARD_FIELD = "105";
	static public final String CODE_PARSE_XML = "106";
	static public final String CODE_ILLEGAL_TAG = "107";
	static public final String CODE_DUPLICATE_DROPDOWN_ENTRY = "108";
	static public final String CODE_NO_DROPDOWN_ENTRIES = "109";
	static public final String CODE_RESERVED_TAG = "110";
	static public final String CODE_MARTUS_FIELD_IN_BOTTOM_SECTION = "111";
	static public final String CODE_PRIVATE_FIELD_IN_TOP_SECTION = "112";
	static public final String CODE_UNKNOWN_DATA_SOURCE_GRID_TAG = "113";
	static public final String CODE_UNKNOWN_DATA_SOURCE_GRID_COLUMN = "114";
	static public final String CODE_DROPDOWN_HAS_CHOICES_AND_DATA_SOURCE = "115";
	static public final String CODE_INVALID_ISO_DATE = "116";
	static public final String CODE_MISSING_REUSABLE_CHOICES = "117";
	static public final String CODE_NULL_REUSABLE_CHOICES = "118";
	static public final String CODE_INVALID_REUSABLE_CHOICE = "119";
	static public final String CODE_NESTED_DATA_SOURCE = "120";
	public static final String CODE_INVALID_DEFAULT_VALUE = "121";
	public static final String CODE_DUPLICATE_REUSABLE_CHOICES_LIST_LABELS = "122";
	public static final String CODE_IMPROPERLY_NESTED_CHOICE_CODE = "123";
	
	// Null specs is no longer a possibility
	//static public final String CODE_NULL_SPECS = "200";
	static public final String CODE_UNAUTHORIZED_KEY = "201";
	static public final String CODE_SIGNATURE_ERROR = "202";
	static public final String CODE_IO_ERROR = "203";
	static public final String CODE_MISSING_CUSTOM_FIELD_IN_SPEC = "204";
	
	static private final String UNUSED_FIELD = "";

	public static final String TYPE_STRING_FOR_REUSABLE_LISTS = "Reusable";

	private String code;
	private String tag;
	private String label;
	private String type;
}
