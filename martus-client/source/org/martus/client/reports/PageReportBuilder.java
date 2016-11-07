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

import java.util.ArrayList;

import org.martus.common.FieldSpecCollection;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.BulletinHtmlGenerator;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.language.LanguageOptions;

public class PageReportBuilder extends ReportBuilder
{
	public PageReportBuilder(MiniLocalization localizationToUse)
	{
		super(localizationToUse);
	}
	
	public ReportFormat createPageReport(MiniFieldSpec[] specs)
	{
		ReportFormat rf = new ReportFormat();
		rf.setBulletinPerPage(true);
		rf.setDocumentStartSection(createStartSection());
		rf.setHeaderSection(createHeaderSection());
		rf.setFakePageBreakSection("<hr></hr>\n");
		MiniFieldSpec[] fieldSpecsInDetailsSection = getOnlyFielSpecsInDetailsSection(specs);
		rf.setDetailSection(createDetailSection(fieldSpecsInDetailsSection));
		rf.setFooterSection("</table>");
		rf.setTotalSection(createTotalSection());
		rf.setDocumentEndSection(createEndSection());
		rf.setSpecsToInclude(specs);
		
		return rf;
	}
	
	public String createStartSection()
	{
		StringBuffer result = new StringBuffer();
		result.append("<html>");
		result.append("<meta http-equiv='Content-Type' content='text/html;charset=UTF-8'>\n");
		return result.toString();
	}

	public String createEndSection()
	{
		return "</html>";
	}
	
	public String createHeaderSection()
	{
		StringBuffer result = new StringBuffer();
		BulletinHtmlGenerator.appendTableStart(result, "width='100%'");
		return result.toString();
	}
	
	private String createDetailSection(MiniFieldSpec[] specs)
	{
		StringBuffer detailBuffer = new StringBuffer();
		BulletinHtmlGenerator.appendTitleOfSection(detailBuffer, "$localization.getStorableFieldLabel('privatesection')");
		
		addTopFields(detailBuffer);
		addBottomFields(detailBuffer);
		
		int start = 0;
		int end = specs.length;
		int increment = 1;
		if(LanguageOptions.isRightToLeftLanguage())
		{
			start = specs.length -1;
			end = -1;
			increment = -1;
		}
		
		for(int index = start; index !=  end; index += increment)
		{
			MiniFieldSpec fieldSpec = specs[index];
			detailBuffer.append("<tr>\n");
			detailBuffer.append(getHtmlColumnStartTag());
			detailBuffer.append(fieldSpec.getLabel());
			detailBuffer.append(getHtmlColumnEndTag());
			
			if (fieldSpec.getType().isMessage())
			{
				detailBuffer.append(getHtmlColumnStartTag());
				detailBuffer.append(getHtmlColumnEndTag());
			}
			else
			{
				detailBuffer.append(getHtmlColumnStartTag());
				detailBuffer.append(getFieldCall(fieldSpec));
				detailBuffer.append(".html($localization)");
				detailBuffer.append(getHtmlColumnEndTag());
			}
			detailBuffer.append("</tr>\n");
		}
		
		return detailBuffer.toString();
	}

	private MiniFieldSpec[] getOnlyFielSpecsInDetailsSection(MiniFieldSpec[] specs)
	{
		FieldSpecCollection topDefaultFieldSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		ArrayList<MiniFieldSpec> fieldSpecsInDetailsSection = new ArrayList<>();
		for (MiniFieldSpec miniFieldSpec : specs)
		{
			if (isDetailsSectionMiniFieldSpec(topDefaultFieldSpecs, miniFieldSpec))
				fieldSpecsInDetailsSection.add(miniFieldSpec);
		}
		
		return fieldSpecsInDetailsSection.toArray(new MiniFieldSpec[0]);
	}

	private boolean isDetailsSectionMiniFieldSpec(FieldSpecCollection topDefaultFieldSpecs,
			MiniFieldSpec miniFieldSpec)
	{
		return topDefaultFieldSpecs.findBytag(miniFieldSpec.getTag()) == null;
	}

	private void addBottomFields(StringBuffer detailBuffer)
	{
		detailBuffer.append("<tr>\n");
		detailBuffer.append("#foreach($field in $bulletin.getBottomFields())\n");
		detailBuffer.append(getFieldRow());
		detailBuffer.append("#end\n");
		detailBuffer.append("</tr>\n");
	}

	private void addTopFields(StringBuffer detailBuffer)
	{
		detailBuffer.append("<tr>\n");
		detailBuffer.append("#foreach($field in $bulletin.getTopFields())\n");
		detailBuffer.append(getFieldRow());
		detailBuffer.append("#end\n");
		detailBuffer.append("</tr>\n");
	}

	private String getHtmlColumnEndTag()
	{
		return "</td>";
	}
	
	private String getHtmlColumnStartTag()
	{
		String align = "left";
		if(LanguageOptions.isRightToLeftLanguage())
			align = "right";
		
		return "<td align='"+align+"'>";
	}
	
	private String getFieldRow()
	{
		String leftData = "$field.getLocalizedLabelHtml($localization)\n";
		String rightData = "$field.html($localization)\n";
		String align = "left";
		if(LanguageOptions.isRightToLeftLanguage())
		{
			String tmp = leftData;
			leftData = rightData;
			rightData = tmp;
			align = "right";
		}
		
		return "#if($specsToInclude.contains($field.getMiniSpec()))\n" +
				"<tr><td align='"+align+"' valign='top'>" +
				leftData +
				getHtmlColumnEndTag() +
				"<td valign='top'>" +
				rightData +
				"</td></tr>\n" +
				"#end\n";
	}
}
