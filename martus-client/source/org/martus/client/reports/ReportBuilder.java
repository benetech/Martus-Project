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
package org.martus.client.reports;

import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.common.MiniLocalization;
import org.martus.swing.FontHandler;
import org.martus.util.language.LanguageOptions;

public class ReportBuilder
{
	public ReportBuilder(MiniLocalization localizationToUse)
	{
		localization = localizationToUse;
		fontHelper = new UiFontEncodingHelper(FontHandler.isDoZawgyiConversion());
	}
	
	/*
	 * Escape template text to protect from velocity. 
	 * Problem characters: \ # $ '
	 */
	public static String bodyEscape(String raw)
	{
		String result = raw;
		result = result.replaceAll("\\\\", "\\\\\\\\");	// must be first!
		result = result.replaceAll("\\#([a-zA-Z])", "\\\\\\#$1");
		result = result.replaceAll("\\$([a-zA-Z])", "\\\\\\$$1");
		result = result.replaceAll("\\'", "\\\\\\'");
		return result;
	}
	
	/* 
	 * Escape text inside single quotes to protect from velocity.
	 * Problem characters: \ "
	 */
	public static String quotedEscape(String raw)
	{
		String result = raw;
		result = result.replaceAll("\\\\", "\\\\\\\\");	// must be first!
		result = result.replaceAll("\\\"", "\\\\042");
		return result;
	}
	
	String getTotalCountString()
	{
		return "$localization.getFieldLabelHtml('ReportNumberOfBulletins')";
	}
	
	protected String getTableRowStart(int columnCount)
	{
		String align = "left";
		if(LanguageOptions.isRightToLeftLanguage())
			align = "right";
		return "<tr><td align='"+align+"'" + "colspan='" + columnCount + "'><em>";
	}
	
	protected String getTableRowEnd()
	{
		return "</em></td></tr>\n";
	}

	protected String createTotalSection()
	{
		String totalSection = "<table>" +
				getTableRowStart(1)+ "<strong>" + getTotalCountString() + " $totals.count()</strong>" + getTableRowEnd() +
				"#foreach($summary1 in $totals.children())\n" +
				getTableRowStart(1) + getSumaryTotal("1") + getTableRowEnd() +
				"#foreach($summary2 in $summary1.children())\n" +
				getTableRowStart(1) + INDENT + getSumaryTotal("2")+ INDENT + getTableRowEnd() +
				"#foreach($summary3 in $summary2.children())\n" +
				getTableRowStart(1) + INDENT + INDENT + getSumaryTotal("3")+ INDENT + INDENT + getTableRowEnd() +
				"#end\n" +
				"#end\n" +
				"#end\n</table>";
		
		return totalSection;
	}
	
	private String getSumaryTotal(String summaryNumber)
	{
		String sumaryId = "$summary" + summaryNumber;
		String label = ".label()";
		String value = ".value()";
		String count = ".count()";
		String item1 = sumaryId + label + ": ";
		String item2 = sumaryId + value + " = ";
		String item3 = sumaryId + count;
		return item1+item2+item3;
	}

	protected static final String INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;";

	MiniLocalization localization;
	UiFontEncodingHelper fontHelper;
}
