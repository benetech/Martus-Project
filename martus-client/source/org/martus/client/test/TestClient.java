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

package org.martus.client.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.martus.client.bulletinstore.TestBulletinFolder;
import org.martus.client.bulletinstore.TestClientBulletinStore;
import org.martus.client.bulletinstore.TestKnownFieldSpecCache;
import org.martus.client.core.TestAttachmentProxyFile;
import org.martus.client.core.TestBulletinFromXFormsLoader;
import org.martus.client.core.TestConfigInfo;
import org.martus.client.core.TestCustomFieldsDuplicateLabelChecker;
import org.martus.client.core.TestFxBulletin;
import org.martus.client.core.TestFxBulletinField;
import org.martus.client.core.TestFxBulletinGridField;
import org.martus.client.core.TestPartialBulletin;
import org.martus.client.core.TestSafeReadableBulletin;
import org.martus.client.core.TestSortableBulletinList;
import org.martus.client.core.templates.TestFormTemplateManager;
import org.martus.client.reports.TestPageReportBuilder;
import org.martus.client.reports.TestReportAnswers;
import org.martus.client.reports.TestReportBuilder;
import org.martus.client.reports.TestReportFormat;
import org.martus.client.reports.TestReportRunner;
import org.martus.client.reports.TestSummaryCount;
import org.martus.client.reports.TestTabularReportBuilder;
import org.martus.client.search.TestBulletinSearcher;
import org.martus.client.search.TestChoiceItemSorterByLabelTagType;
import org.martus.client.search.TestFancySearchHelper;
import org.martus.client.search.TestFancySearchTableModel;
import org.martus.client.search.TestFieldChoicesByLabel;
import org.martus.client.search.TestFieldChooserSpecBuilder;
import org.martus.client.search.TestSearchParser;
import org.martus.client.search.TestSearchSpec;
import org.martus.client.search.TestSearchTreeNode;
import org.martus.client.swingui.grids.TestGridTableModel;
import org.martus.client.swingui.jfx.landing.bulletins.TestBulletinTableData;
import org.martus.client.swingui.jfx.setupwizard.TestContactTableData;
import org.martus.common.utilities.TestMartusFlexidate;

public class TestClient
{
	public static void main(String[] args)
	{
		runTests();
	}

	public static void runTests ()
	{
		junit.textui.TestRunner.run (suite());
	}

	public static Test suite ( )
	{
		TestSuite suite= new TestSuite("All Client Martus Tests");
		
		suite.addTest(new TestSuite(TestAttachmentProxyFile.class));
		suite.addTest(new TestSuite(TestBackgroundRetriever.class));
		suite.addTest(new TestSuite(TestBulletinFolder.class));
		suite.addTest(new TestSuite(TestBulletinSearcher.class));
		suite.addTest(new TestSuite(TestBulletinTableModel.class));
		suite.addTest(new TestSuite(TestBulletinXmlExporter.class));
		suite.addTest(new TestSuite(TestChoiceItemSorterByLabelTagType.class));
		suite.addTest(new TestSuite(TestClientBulletinStore.class));
		suite.addTest(new TestSuite(TestConfigInfo.class));
		suite.addTest(new TestSuite(TestContactTableData.class));
		suite.addTest(new TestSuite(TestBulletinTableData.class));
		suite.addTest(new TestSuite(TestCustomFieldsDuplicateLabelChecker.class));
		suite.addTest(new TestSuite(TestFancySearchHelper.class));
		suite.addTest(new TestSuite(TestFancySearchTableModel.class));
		suite.addTest(new TestSuite(TestFieldChoicesByLabel.class));
		suite.addTest(new TestSuite(TestFieldChooserSpecBuilder.class));
		suite.addTest(new TestSuite(TestFolderList.class));
		suite.addTest(new TestSuite(TestFormTemplateManager.class));
		suite.addTest(new TestSuite(TestFxBulletin.class));
		suite.addTest(new TestSuite(TestFxBulletinField.class));
		suite.addTest(new TestSuite(TestFxBulletinGridField.class));
		suite.addTest(new TestSuite(TestGridTableModel.class));
		suite.addTest(new TestSuite(TestImporterOfXmlFilesOfBulletins.class));
		suite.addTest(new TestSuite(TestKnownFieldSpecCache.class));
		suite.addTest(new TestSuite(TestLocalization.class));
		suite.addTest(new TestSuite(TestMartusApp_NoServer.class));
		suite.addTest(new TestSuite(TestMartusFlexidate.class));
		suite.addTest(new TestSuite(TestMartusJarVerification.class));
		suite.addTest(new TestSuite(TestMartusUserNameAndPassword.class));
		suite.addTest(new TestSuite(TestPageReportBuilder.class));
		suite.addTest(new TestSuite(TestPartialBulletin.class));
		suite.addTest(new TestSuite(TestReportAnswers.class));
		suite.addTest(new TestSuite(TestReportBuilder.class));
		suite.addTest(new TestSuite(TestRetrieveCommand.class));
		suite.addTest(new TestSuite(TestTabularReportBuilder.class));
		suite.addTest(new TestSuite(TestReportFormat.class));
		suite.addTest(new TestSuite(TestReportRunner.class));
		suite.addTest(new TestSuite(TestSafeReadableBulletin.class));
		suite.addTest(new TestSuite(TestSearchParser.class));
		suite.addTest(new TestSuite(TestSearchSpec.class));
		suite.addTest(new TestSuite(TestSearchTreeNode.class));
		suite.addTest(new TestSuite(TestSortableBulletinList.class));
		suite.addTest(new TestSuite(TestSummaryCount.class));
		suite.addTest(new TestSuite(TestTokenReplacement.class));
		suite.addTest(new TestSuite(TestTransferableAttachments.class));
		suite.addTest(new TestSuite(TestTransferableBulletin.class));
		suite.addTest(new TestSuite(TestRandomAccessFileOverwrite.class));
		suite.addTest(new TestSuite(TestUiSession.class));
		suite.addTest(new TestSuite(TestBulletinFromXFormsLoader.class));
		
	    return suite;
	}
}
