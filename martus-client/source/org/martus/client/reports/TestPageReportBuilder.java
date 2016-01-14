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
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.util.TestCaseEnhanced;

public class TestPageReportBuilder extends TestCaseEnhanced
{
	public TestPageReportBuilder(String name)
	{
		super(name);
	}

	public void testCreatePageReport()
	{
		MiniLocalization localization = new MiniLocalization();
		PageReportBuilder builder= new PageReportBuilder(localization);
		ReportFormat rf = builder.createPageReport(new MiniFieldSpec[0]);
		assertTrue("Not a page report?", rf.getBulletinPerPage());
		assertContains("Start not html?", "<html>", rf.getDocumentStartSection());
		assertContains("End not end html?", "</html>", rf.getDocumentEndSection());
		assertContains("<table", rf.getHeaderSection());
		assertContains("</table>", rf.getFooterSection());
		assertContains("#foreach", rf.getTotalSection());
		assertContains("<hr", rf.getFakePageBreakSection());
	}
}
