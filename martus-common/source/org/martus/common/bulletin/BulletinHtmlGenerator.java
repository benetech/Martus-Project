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

package org.martus.common.bulletin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import org.martus.common.EnglishCommonStrings;
import org.martus.common.FieldCollection;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MiniLocalization;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.FieldDataPacket;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.language.LanguageOptions;
import org.martus.util.xml.XmlUtilities;

public class BulletinHtmlGenerator
{
	public BulletinHtmlGenerator(MiniLocalization localizationToUse)
	{
		this(80, localizationToUse);
	}
	
	public BulletinHtmlGenerator(int widthToUse, MiniLocalization localizationToUse)
	{
		width = widthToUse;
		localization = localizationToUse;
	}

	public String getHtmlString(Bulletin b, ReadableDatabase database, boolean includePrivateData, boolean yourBulletin) throws Exception
	{
		StringBuffer result = new StringBuffer();
		result.append("<html>");
		result.append(getHtmlFragment(b, database, includePrivateData, yourBulletin));
		result.append("</html>");
		return result.toString();
	}

	public String getHtmlFragment(Bulletin b, ReadableDatabase database, boolean includePrivateData, boolean yourBulletin) throws Exception
	{
		StringBuffer html = new StringBuffer(1000);
		appendTableStart(html, width);
		appendHeadHtml(html, b, localization);
		if(!yourBulletin)
		{
			html.append("<tr></tr>\n");
			html.append(getHtmlEscapedFieldHtmlString(localization.getFieldLabel("BulletinNotYours"),""));		
		}

		boolean showNonPrivateParts = (includePrivateData || !b.isAllPrivate());
		if(showNonPrivateParts)
		{
			appendTitleOfSection(html, getPublicSectionTitle(b.isAllPrivate()));
			
			String allPrivateFieldLabel = localization.getFieldLabel("allprivate");
			String allPrivateFieldValue = getAllPrivateValue(b.isAllPrivate());
			html.append(getHtmlEscapedFieldHtmlString(allPrivateFieldLabel, allPrivateFieldValue));
			
			html.append(getSectionHtmlString(b.getFieldDataPacket()));
			html.append(getAttachmentsHtmlString(b, b.getPublicAttachments(), database));
		}
	
		if (includePrivateData)
		{	
			appendTitleOfSection(html, localization.getFieldLabel("privatesection"));
			html.append(getSectionHtmlString(b.getPrivateFieldDataPacket()));
			html.append(getAttachmentsHtmlString(b, b.getPrivateAttachments(), database));
		}
			
		if(showNonPrivateParts)
		{
			appendHQs(html, b);
		}

		appendTableEnd(html, b);
		return html.toString();
	}

	private String getAllPrivateValue(boolean isAllPrivate)
	{
		return localization.getButtonLabel(getAllPrivateValueTag(isAllPrivate));
	}

	private String getPublicSectionTitle(boolean isAllPrivate)
	{
		String tag = "publicsection";
		if(isAllPrivate)
			tag = "privatesection";
		
		return localization.getFieldLabel(tag);
	}
	
	private String getAllPrivateValueTag(boolean isAllPrivate)
	{
		if(isAllPrivate)
			return "yes";
		
		return "no";
	}

	private void appendTableEnd(StringBuffer html, Bulletin b)
	{
		html.append("<tr></tr>\n");
		html.append(getHtmlEscapedFieldHtmlString(localization.getFieldLabel("BulletinId"),b.getLocalId()));
		html.append("</table>");
	}

	public static void appendTableStart(StringBuffer html, int width)
	{
		String widthString = Integer.toString(width);
		appendTableStart(html, widthString);
	}

	public static void appendTableStart(StringBuffer html, String widthString)
	{
		html.append("<table width='");
		html.append(widthString);
		html.append("'>\n");
		int leftColumnWidthPercentage = LABEL_COLUMN_WIDTH_PERCENTAGE;
		if(LanguageOptions.isRightToLeftLanguage())
			leftColumnWidthPercentage = (100-leftColumnWidthPercentage);
		int rightColumnWidthPercentage = (100-leftColumnWidthPercentage);
		html.append("<tr>");
		html.append("<td width='" + leftColumnWidthPercentage + "%'></td>");
		html.append("<td width='" + rightColumnWidthPercentage + "%'></td>");
		html.append("</tr>\n");
	}
	
	public static void appendTitleOfSection(StringBuffer html, String title)
	{
		html.append("<tr></tr>\n");
		String align = "left";
		if(LanguageOptions.isRightToLeftLanguage())
			align = "right";
		html.append("<tr><td colspan='2' align='" + align + "'>");
		html.append("<u><b>"+title+"</b></u>");
		html.append("</td></tr>");
		html.append("\n");
	}	
	
	public static void appendHeadHtml(StringBuffer html, Bulletin b, MiniLocalization localization)
	{
		html.append(getHtmlEscapedFieldHtmlString(localization.getFieldLabel("BulletinLastSaved"), localization.formatDateTime(b.getLastSavedTime())));
		html.append(getHtmlEscapedFieldHtmlString(localization.getFieldLabel("BulletinVersionNumber"), (new Integer(b.getVersion())).toString()));
		html.append(getHtmlEscapedFieldHtmlString(localization.getFieldLabel("BulletinStatus"), localization.getStatusLabel(b.getStatus())));
	}
	
	private void appendHQs(StringBuffer html, Bulletin b )
	{
		appendTitleOfSection(html, localization.getFieldLabel("HQSummaryLabel"));

		HeadquartersKeys keys = b.getAuthorizedToReadKeys();
		int size = keys.size();
		if(size==0)
		{
			html.append(getFieldHtmlString("",localization.getFieldLabel("NoHQsConfigured")));
			return;
		}

		for(int i = 0; i < size; ++i)
		{
			String label = keys.get(i).getLabel();
			if(label.length() == 0)
			{
				try 
				{
					label = keys.get(i).getFormattedPublicCode();
				} 
				catch (InvalidBase64Exception e) 
				{
					e.printStackTrace();
				}
			}
			html.append(getHtmlEscapedFieldHtmlString("",label));
			html.append("<p></p>");
		}
	}
	

	public String getSectionHtmlString(FieldDataPacket fdp) throws Exception
	{
		FieldSpecCollection fieldTags = fdp.getFieldSpecs();
		String sectionHtml = "";
		Vector pendingValues = new Vector();
		for(int fieldNum = 0; fieldNum < fdp.getFieldCount(); ++fieldNum)
		{
			FieldSpec spec = fieldTags.get(fieldNum);
			String tag = spec.getTag();
			MartusField field = fdp.getField(tag);

			String label = getHTMLEscaped(spec.getLabel());			
			String value = field.getData();
			FieldType fieldType = spec.getType();

			if(tag.equals(Bulletin.TAGTITLE))
				value = "<strong>" + getHTMLEscaped(value) + "</strong>";
			else if(fieldType.isSectionStart())
			{
				String horizontalRuler = "<HR></HR>";
				label = horizontalRuler+ "<b><i>" + label + "</i></b>";
				value = horizontalRuler;
			}
			else if(fieldType.isGrid())
				value = getGridHTML(fdp, spec, tag);
			else
				value = getFieldDataAsHtml(field);
			
			if(StandardFieldSpecs.isStandardFieldTag(tag))
				label = getHTMLEscaped(localization.getFieldLabel(tag));
			
			if(!spec.keepWithPrevious() && pendingValues.size() > 0)
			{
				String fieldHtml = getFieldRowHtmlString((String[])pendingValues.toArray(new String[0]));
				sectionHtml += fieldHtml;
				pendingValues.clear();
			}
			pendingValues.add(label);
			pendingValues.add(value);
		}
		
		if(pendingValues.size() > 0)
		{
			String fieldHtml = getFieldRowHtmlString((String[])pendingValues.toArray(new String[0]));
			sectionHtml += fieldHtml;
			pendingValues.clear();
		}

		return sectionHtml;
	}

	private String getFieldDataAsHtml(MartusField field) throws Exception
	{
		String value = field.getData();
		FieldSpec spec = field.getFieldSpec();
		
		FieldType fieldType = spec.getType();
		if(fieldType.isDate())
			return getHTMLEscaped(localization.convertStoredDateToDisplay(value));
		else if(fieldType.isLanguageDropdown())
			return getHTMLEscaped(localization.getLanguageName(value));
		else if(fieldType.isMultiline())
			return insertNewlines(getHTMLEscaped(value));
		else if(fieldType.isDateRange())
			return getHTMLEscaped(localization.getViewableDateRange(value));
		else if(fieldType.isBoolean())
			return getPrintableBooleanValue(value);
		else if(fieldType.isDropdown())
			return field.html(localization);

		return getHTMLEscaped(value);
	}

	private String getPrintableBooleanValue(String value)
	{
		if(value.equals(FieldSpec.TRUESTRING))
			value = getHTMLEscaped(localization.getButtonLabel(EnglishCommonStrings.YES));
		else
			value = getHTMLEscaped(localization.getButtonLabel(EnglishCommonStrings.NO));
		return value;
	}

	private String getGridHTML(FieldDataPacket fdp, FieldSpec spec, String tag)
	{
		String gridXMLData = fdp.get(tag);
		if(gridXMLData.length()==0)
			return "";
		
		GridFieldSpec grid = (GridFieldSpec)spec;
		String tableAlignment = "left";
		if(LanguageOptions.isRightToLeftLanguage())
			tableAlignment = "right";
		String value = "<table border='1' align='" + tableAlignment + "'><tr>";
		String justification = "center";
		if(!LanguageOptions.isRightToLeftLanguage())
			value += buildElementWithAlignment(getHTMLEscaped(grid.getColumnZeroLabel()),TABLE_HEADER, justification);
		
		int columnCount = grid.getColumnCount();
		FieldSpecCollection columnSpecs = new FieldSpecCollection();
		for(int i = 0; i < columnCount; ++i)
		{
			String data = grid.getColumnLabel(i);
			if(LanguageOptions.isRightToLeftLanguage())
				data = grid.getColumnLabel((columnCount-1)-i);
			value += buildElementWithAlignment(getHTMLEscaped(data),TABLE_HEADER, justification);
			columnSpecs.add(grid.getFieldSpec(i));
		}
		if(LanguageOptions.isRightToLeftLanguage())
			value += buildElementWithAlignment(getHTMLEscaped(grid.getColumnZeroLabel()),TABLE_HEADER, justification);
		value += "</tr>";
		
		try
		{
			columnSpecs.addAllReusableChoicesLists(fdp.getFieldSpecs().getAllReusableChoiceLists());
			FieldCollection fieldsForColumns = new FieldCollection(columnSpecs);
			
			GridData gridData = new GridData(grid, fdp.getFieldSpecs().getAllReusableChoiceLists());
			gridData.setFromXml(gridXMLData);
			int rowCount = gridData.getRowCount();

			justification = "left";
			if(LanguageOptions.isRightToLeftLanguage())
				justification = "right";
			
			for(int r =  0; r<rowCount; ++r)
			{
				value += "<tr>";
				if(!LanguageOptions.isRightToLeftLanguage())
					value += buildElementWithAlignment(getHTMLEscaped(Integer.toString(r+1)),TABLE_DATA, justification);
				for(int i = 0; i<columnCount; ++i)
				{
				   int column = i;
				   if(LanguageOptions.isRightToLeftLanguage())
				     column = (columnCount - 1) - i;

					MartusField field = fieldsForColumns.getField(column);
					String rawData = gridData.getValueAt(r, column);
					field.setData(rawData);
					String printableData = getFieldDataAsHtml(field);
					value += buildElementWithAlignment(printableData, TABLE_DATA, justification);
				}
				
				if(LanguageOptions.isRightToLeftLanguage())
					value += buildElementWithAlignment(getHTMLEscaped(Integer.toString(r+1)),TABLE_DATA, justification);
				value += "</tr>";
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		value += "</table>";
		return value;
	}

	private String buildElementWithAlignment(String data, String type, String justification)
	{
		return "<"+type+" align='"+justification+"'>"+data+"</"+type+">";
	}

	private String getSizeInKb(int sizeBytes)
	{
		int sizeInKb = sizeBytes / 1024;
		if (sizeInKb == 0)
			sizeInKb = 1;
		return Integer.toString(sizeInKb);
	}

	private String getAttachmentSize(ReadableDatabase db, DatabaseKey key)
	{
		// TODO :This is a duplicate code from AttachmentTableModel.java. 
		// Ideally, the AttachmentProxy should self-describe of file size and file description.

		String size = "";
		try
		{
			int rawSize = db.getRecordSize(key);
			rawSize -= 1024;//Public code & overhead
			rawSize = rawSize * 3 / 4;//Base64 overhead
			size = getSizeInKb(rawSize);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (RecordHiddenException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return size;
	}

	private String getAttachmentsHtmlString(Bulletin b, AttachmentProxy[] attachments, ReadableDatabase db)
	{
		String attachmentList = "";
	
		for(int i = 0 ; i < attachments.length ; ++i)
		{
			AttachmentProxy aProxy = attachments[i];
			String label = aProxy.getLabel();
			DatabaseKey key = b.getDatabaseKeyForLocalId(aProxy.getUniversalId().getLocalId());
			String size = "( " + getAttachmentSize(db, key)+ " " + localization.getFieldLabel("attachmentSizeForPrinting")+ " )";
			if(LanguageOptions.isRightToLeftLanguage())
			{
				String tmp = label;
				label = size;
				size = tmp;
			}

			attachmentList += "<p>" + getHTMLEscaped(label) + "    " + getHTMLEscaped(size) + "</p>";
		}
		return getFieldHtmlString(localization.getFieldLabel("attachments"), attachmentList);
	}

	private static String getHtmlEscapedFieldHtmlString(String label, String value)
	{
		return getFieldHtmlString(getHTMLEscaped(label), getHTMLEscaped(value));
	}

	public static String getFieldHtmlString(String label, String value)
	{
		return getFieldRowHtmlString(new String[] {label, value, });
	}
	
	public static String getFieldRowHtmlString(String[] values)
	{
		int valueIndex = 0;
		int delta = 1;
		if(LanguageOptions.isRightToLeftLanguage())
		{
			valueIndex = values.length -1;
			delta = -1;
		}
		
		StringBuffer fieldHtml = new StringBuffer();
		fieldHtml.append("<tr>");

		if(delta > 0)
		{
			fieldHtml.append(getLabelHtml(values[valueIndex]));
			valueIndex += delta;
		}
		
		if(values.length > 2)
		{
			String alignment = LanguageOptions.isRightToLeftLanguage() ? "right" : "left";
			fieldHtml.append("<td align='" + alignment + "' valign='top'>");
			fieldHtml.append("<table cellpadding='0'><tr>");
			
			for(int i = 0; i < values.length - 1; ++i)
			{
				if(i > 0)
					fieldHtml.append(getPaddingCellHtml());
				fieldHtml.append(getCellHtml(values[valueIndex]));
				valueIndex += delta;
			}
	
			fieldHtml.append("</tr></table>");
			fieldHtml.append("</td>");
		}
		else
		{
			fieldHtml.append(getCellHtml(values[valueIndex]));
			valueIndex += delta;
		}

		if(delta < 0)
		{
			fieldHtml.append(getLabelHtml(values[valueIndex]));
			valueIndex += delta;
		}
		
		fieldHtml.append("</tr>\n");
		return new String(fieldHtml);
	}

	private static String getPaddingCellHtml()
	{
		return "<td width='10'></td>";
	}

	private static String getCellHtml(String value) 
	{
		String alignment = LanguageOptions.isRightToLeftLanguage() ? "right" : "left";
		return getCellHtmlWithAlignment(value, alignment);
	}

	private static String getLabelHtml(String label) 
	{
		String alignment = LanguageOptions.isRightToLeftLanguage() ? "left" : "right";
		return getCellHtmlWithAlignment(label, alignment);
	}
	
	private static String getCellHtmlWithAlignment(String value, String alignment)
	{
		return "<td align='" + alignment + "' valign='top'>" + value + "</td>";
	}

	private String insertNewlines(String value)
	{
		final String P_TAG_BEGIN = "<p>";
		final String P_TAG_END = "</p>";
		StringBuffer html = new StringBuffer(value.length() + 100);
		html.append(P_TAG_BEGIN);

		try
		{
			BufferedReader reader = new BufferedReader(new StringReader(getHTMLEscaped(value)));
			String thisParagraph = null;
			while((thisParagraph = reader.readLine()) != null)
			{
				html.append(thisParagraph);
				html.append(P_TAG_END);
				html.append(P_TAG_BEGIN);
			}
		}
		catch (IOException e)
		{
			html.append("...?");
		}

		html.append(P_TAG_END);
		return new String(html);
	}
	
	private static String getHTMLEscaped(String text)
	{
		return XmlUtilities.getXmlEncoded(text);
	}

	int width;
	MiniLocalization localization;

	private static final int LABEL_COLUMN_WIDTH_PERCENTAGE = 15;
	private static final String TABLE_HEADER = "th";
	private static final String TABLE_DATA = "td";
	
}
