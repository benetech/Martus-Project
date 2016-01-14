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
package org.martus.meta;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.martus.util.TestCaseEnhanced;
import org.martus.util.xml.TestSimpleXmlParser;

public class TestMetaQuick extends TestCaseEnhanced
{

	public TestMetaQuick(String name)
	{
		super(name);
	}

	public static void main (String[] args)
	{
		runTests();
	}

	public static void runTests ()
	{
		junit.textui.TestRunner.run (suite());
	}

	public static Test suite()
	{
		TestSuite suite= new TestSuite("Meta Tests");

		suite.addTestSuite(TestBackgroundUploader.class);
		suite.addTestSuite(TestStreamableBase64.class);
		suite.addTestSuite(TestDatabase.class);
		suite.addTestSuite(TestDatabaseHiddenRecords.class);
		suite.addTestSuite(TestDeleteDraftsTableModel.class);
		suite.addTestSuite(TestHeadquarterEntry.class);
		suite.addTestSuite(TestHeadquartersManagementTableModel.class);
		suite.addTestSuite(TestHeadquartersEditorTableModel.class);
		suite.addTestSuite(TestHeadquartersListTableModel.class);
		suite.addTestSuite(TestLoggerUtil.class);
		suite.addTestSuite(TestMartusApp_WithServer.class);
		suite.addTestSuite(TestRetrieveHQDraftsTableModel.class);
		suite.addTestSuite(TestRetrieveHQTableModel.class);
		suite.addTestSuite(TestRetrieveMyDraftsTableModel.class);
		suite.addTestSuite(TestRetrieveMyTableModel.class);
		suite.addTestSuite(TestRetrieveTableModel.class);
		suite.addTestSuite(TestScrubFile.class);
		suite.addTestSuite(TestSimpleX509TrustManager.class);
		suite.addTestSuite(TestSSL.class);
		
		// from org.martus.utils
		suite.addTestSuite(TestSimpleXmlParser.class);
		
		return suite;
	}	
}
