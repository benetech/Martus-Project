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

import java.util.Arrays;

import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.util.TestCaseEnhanced;

public class TestReportAnswers extends TestCaseEnhanced
{
	public TestReportAnswers(String name)
	{
		super(name);
	}

	public void testJson()
	{
		MiniFieldSpec[] specs = new MiniFieldSpec[] {
			new MiniFieldSpec(FieldSpec.createCustomField("a", "A", new FieldTypeNormal())),
			new MiniFieldSpec(FieldSpec.createCustomField("b", "B", new FieldTypeNormal())),
			
		};
		MiniLocalization localization = new MiniLocalization();
		localization.setCurrentLanguageCode("xy");
		ReportAnswers page = new ReportAnswers(ReportAnswers.PAGE_REPORT, specs, localization);
		assertEquals("Wrong version?", 9, page.getVersion());
		assertTrue("Not page report?", page.isPageReport());
		assertFalse("Was tabular?", page.isTabularReport());
		assertTrue("Can't get specs back?", Arrays.equals(specs, page.getSpecs()));
		assertEquals("Wrong language?", localization.getCurrentLanguageCode(), page.getLanguageCode());
		
		ReportAnswers gotPage = new ReportAnswers(page.toJson());
		assertEquals("Didn't save version?", ReportAnswers.EXPECTED_VERSION, gotPage.getVersion());
		assertTrue("Didn't save page-ness?", gotPage.isPageReport());
		assertTrue("Didn't save/load specs?", Arrays.equals(page.getSpecs(), gotPage.getSpecs()));
		assertEquals("Didn't save/load language?", localization.getCurrentLanguageCode(), gotPage.getLanguageCode());
		
		ReportAnswers tabular = new ReportAnswers(ReportAnswers.TABULAR_REPORT, specs, localization);
		assertTrue("Not tabular report?", tabular.isTabularReport());
		assertFalse("Was page?", tabular.isPageReport());
	}
}
