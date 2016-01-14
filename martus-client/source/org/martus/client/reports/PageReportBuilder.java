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

import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.BulletinHtmlGenerator;
import org.martus.common.fieldspec.MiniFieldSpec;
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
		rf.setDetailSection(createDetailSection());
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
	
	public String createDetailSection()
	{
		//TODO Missing unit tests, changes made didn't flag a failing unit test
		StringBuffer result = new StringBuffer();
		BulletinHtmlGenerator.appendTitleOfSection(result, "$localization.getStorableFieldLabel('privatesection')");
		result.append("#foreach($field in $bulletin.getTopFields())\n");
		result.append(getFieldRow());
		result.append("#end\n");
		result.append("#foreach($field in $bulletin.getBottomFields())\n");
		result.append(getFieldRow());
		result.append("#end\n");

		return result.toString();
	}
	
	public String getFieldRow()
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
				"</td>" +
				"<td valign='top'>" +
				rightData +
				"</td></tr>\n" +
				"#end\n";
	}
}
