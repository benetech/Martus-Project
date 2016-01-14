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

import org.martus.client.core.SafeReadableBulletin;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.language.LanguageOptions;

public class TabularReportBuilder extends ReportBuilder
{
	public TabularReportBuilder(MiniLocalization localizationToUse)
	{
		super(localizationToUse);
	}
	
	public ReportFormat createTabular(MiniFieldSpec[] specs)
	{
		ReportFormat rf = new ReportFormat();
		rf.setDocumentStartSection(createStartSection());
		rf.setHeaderSection(createHeaderSection(specs));
		rf.setDetailSection(createDetailSection(specs));
		rf.setBreakSection(createBreakSection(specs));
		rf.setTotalBreakSection(createTotalBreakSection(specs));
		rf.setTotalSection(createTotalSection());
		rf.setDocumentEndSection(createEndSection());

		return rf;
	}

	private String createStartSection()
	{
		StringBuilder startBuffer = new StringBuilder();
		startBuffer.append("<html>");
		startBuffer.append("<meta http-equiv='Content-Type' content='text/html;charset=UTF-8'>\n");
		return startBuffer.toString();
	}

	private String createHeaderSection(MiniFieldSpec[] specs)
	{
		StringBuilder headerBuffer = new StringBuilder();
		headerBuffer.append("<table border='3' cellpadding='5' cellspacing='0'>\n");
		headerBuffer.append("<tr>\n");

		int start = 0;
		int end = specs.length;
		int increment = 1;
		if(LanguageOptions.isRightToLeftLanguage())
		{
			start = specs.length -1;
			end = -1;
			increment = -1;
		}
		for(int i = start; i !=  end; i += increment)
		{
			headerBuffer.append("<th>");
			MiniFieldSpec spec = specs[i];
			String specLabel = spec.getLabel();
			String label = StandardFieldSpecs.getLocalizedLabelHtml(spec.getTag(), specLabel, localization);
			//NOTE: Custom spec labels are already in storable (unicode), but standard spec
			// labels are in displayable
			if (StandardFieldSpecs.isStandardFieldTag(spec.getTag()))
				label = fontHelper.getStorable(label);
			headerBuffer.append(bodyEscape(label));
			headerBuffer.append("</th>");
		}
		headerBuffer.append("</tr>\n");
		return headerBuffer.toString();
	}

	private String getColumnStart()
	{
		String align = "left";
		if(LanguageOptions.isRightToLeftLanguage())
			align = "right";
		return "<td align='"+align+"'>";
	}

	
	private String createDetailSection(MiniFieldSpec[] specs)
	{
		StringBuilder detailBuffer = new StringBuilder();
		detailBuffer.append("<tr>\n");
		int start = 0;
		int end = specs.length;
		int increment = 1;
		if(LanguageOptions.isRightToLeftLanguage())
		{
			start = specs.length -1;
			end = -1;
			increment = -1;
		}
		for(int i = start; i !=  end; i += increment)
		{
			detailBuffer.append(getColumnStart());
			detailBuffer.append(getFieldCall(specs[i]));
			detailBuffer.append(".html($localization)");
			detailBuffer.append("</td>");
		}
		detailBuffer.append("</tr>\n");
		return detailBuffer.toString();
	}

	public String getFieldCall(MiniFieldSpec spec)
	{
		String[] tags = SafeReadableBulletin.parseNestedTags(spec.getTag());
		String topLevelTag = tags[0];
		
		StringBuilder result = new StringBuilder();
		result.append("$bulletin.field(\"");
		result.append(quotedEscape(topLevelTag));
		result.append("\", \"");
		result.append(quotedEscape(spec.getTopLevelLabel()));
		result.append("\", \"");
		result.append(quotedEscape(spec.getTopLevelType().getTypeName()));
		result.append("\")");
		
		for(int i = 1; i < tags.length; ++i)
		{
			result.append(".getSubField(\"");
			result.append(quotedEscape(tags[i]));
			result.append("\", $localization)");
		}
			
		return result.toString();
	}
	
	private String createBreakSection(MiniFieldSpec[] specs)
	{
		int columnCount = specs.length;
		StringBuilder breakSection = new StringBuilder();
		breakSection.append(getTableRowStart(columnCount));
		String localizedLabel = "$BreakFields.get($BreakLevel).getLocalizedLabelHtml($localization): ";
		String breakItem = "$BreakFields.get($BreakLevel).html($localization)";
		String breakCount = " = $BreakCount";
		if(LanguageOptions.isRightToLeftLanguage())
		{
			breakSection.append("<table border='0'><tr>");
			breakSection.append("<td align='right'><em>").append(breakCount).append("</em></td>");
			breakSection.append("<td align='right'><em>").append(breakItem).append("</em></td>");
			breakSection.append("<td align='right'<em>");
			breakSection.append(getIndent());
			breakSection.append(localizedLabel);
			breakSection.append(getTableRowEnd());
			breakSection.append("</table>");
		}
		else
		{
			breakSection.append(getIndent());
			breakSection.append(localizedLabel);
			breakSection.append(breakItem);
			breakSection.append(breakCount);
		}
		breakSection.append(getTableRowEnd());
		
		return breakSection.toString();
	}

	
	private String getIndent()
	{
		return "#foreach( $foo in [0..$BreakLevel] )\n" +
				INDENT + "\n" +
				"#end ";
	}
	
	private String createTotalBreakSection(MiniFieldSpec[] specs)
	{
		int columnCount = specs.length;
		return getTableRowStart(columnCount) +
				"<strong>"+getTotalCountString() + " $TotalBulletinCount</strong>" +
			   getTableRowEnd();
	}
	
	private String createEndSection()
	{
		StringBuilder endBuffer = new StringBuilder();
		endBuffer.append("</table></html>\n");
		return endBuffer.toString();
	}
}
