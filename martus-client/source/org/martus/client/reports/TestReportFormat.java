/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.util.TestCaseEnhanced;


public class TestReportFormat extends TestCaseEnhanced
{
	public TestReportFormat(String name)
	{
		super(name);
	}

	public void testDetailSection()
	{
		String sampleDetailSection = "blah blah blah";
		ReportFormat rf = new ReportFormat();
		rf.setDetailSection(sampleDetailSection);
		assertEquals(sampleDetailSection, rf.getDetailSection());
	}
	
	public void testToJson()
	{
		ReportFormat rf = new ReportFormat();
		rf.setDocumentStartSection("start");
		rf.setDocumentEndSection("end");
		rf.setDetailSection("detail");
		rf.setBreakSection("break");
		rf.setHeaderSection("header");
		rf.setTotalBreakSection("totalbreak");
		rf.setTotalSection("total");
		rf.setBulletinPerPage(true);
		
		ReportFormat got = new ReportFormat(rf.toJson());
		assertEquals("didn't save start?", rf.getDocumentStartSection(), got.getDocumentStartSection());
		assertEquals("didn't save detail?", rf.getDetailSection(), got.getDetailSection());
		assertEquals("didn't save end?", rf.getDocumentEndSection(), got.getDocumentEndSection());
		assertEquals("didn't save break?", rf.getBreakSection(), got.getBreakSection());
		assertEquals("didn't save header?", rf.getHeaderSection(), got.getHeaderSection());
		assertEquals("didn't save totalbreak?", rf.getTotalBreakSection(), got.getTotalBreakSection());
		assertEquals("didn't save total?", rf.getTotalSection(), got.getTotalSection());
		assertEquals("didn't save bulletin-per-page?", rf.getBulletinPerPage(), got.getBulletinPerPage());
	}
	
	public void testSpecs() throws Exception
	{
		MiniFieldSpec[] specs = new MiniFieldSpec[] {
			new MiniFieldSpec(FieldSpec.createCustomField("a", "A: ", new FieldTypeNormal())),	
			new MiniFieldSpec(FieldSpec.createCustomField("b", "B: ", new FieldTypeDate())),
		};
		
		ReportFormat rf = new ReportFormat();
		rf.setSpecsToInclude(specs);
		ReportFormat got = new ReportFormat(rf.toJson());
		assertTrue("Didn't save/load specs?", Arrays.equals(specs, got.getSpecsToInclude()));
	}
	
}
