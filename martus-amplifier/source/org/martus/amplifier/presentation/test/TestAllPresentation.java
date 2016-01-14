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

package org.martus.amplifier.presentation.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestAllPresentation extends TestSuite
{
	public TestAllPresentation(String name)
	{
		super(name);
	}
	
	public static void main (String[] args) 
	{
		runTests();
	}

	public static void runTests () 
	{
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite ( ) 
	{
		TestSuite suite= new TestSuite("All Martus Amplifier Presentation Tests");
		
		suite.addTest(new TestSuite(TestAdvancedSearch.class));
		suite.addTest(new TestSuite(TestContactInfo.class));
		suite.addTest(new TestSuite(TestDoSearch.class));
		suite.addTest(new TestSuite(TestDownloadAttachment.class));
		suite.addTest(new TestSuite(TestFeedbackSubmitted.class));
		suite.addTest(new TestSuite(TestFoundBulletin.class));
		suite.addTest(new TestSuite(TestPrintFoundBulletin.class));
		suite.addTest(new TestSuite(TestSearchTips.class));
		suite.addTest(new TestSuite(TestSearchResults.class));
		suite.addTest(new TestSuite(TestSimpleSearch.class));
		suite.addTest(new TestSuite(TestUserFeedbackForm.class));
	
		return suite;
	}


}
