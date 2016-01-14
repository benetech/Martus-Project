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

package org.martus.common.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.martus.common.TestExceptions;
import org.martus.common.bulletin.TestAttachmentProxy;
import org.martus.common.bulletin.TestBulletin;
import org.martus.common.bulletin.TestBulletinHtmlGenerator;
import org.martus.common.bulletin.TestBulletinLoader;
import org.martus.common.bulletin.TestBulletinZipImporter;
import org.martus.common.bulletin.TestBulletinZipUtilities;
import org.martus.common.bulletinstore.TestBulletinStore;
import org.martus.common.bulletinstore.TestBulletinStoreCache;
import org.martus.common.bulletinstore.TestLeafNodeCache;
import org.martus.common.field.TestMartusDropdownField;
import org.martus.common.field.TestMartusField;
import org.martus.common.field.TestMartusSearchableGridColumnField;
import org.martus.common.fieldspec.TestCustomDropDownFieldSpec;
import org.martus.common.fieldspec.TestCustomFieldSpecValidator;
import org.martus.common.fieldspec.TestFormTemplate;
import org.martus.common.fieldspec.TestDateFieldSpec;
import org.martus.common.fieldspec.TestDateRangeFieldSpec;
import org.martus.common.fieldspec.TestDropDownFieldSpec;
import org.martus.common.fieldspec.TestFieldSpec;
import org.martus.common.fieldspec.TestMiniFieldSpec;
import org.martus.common.network.TestServerBulletinSummary;
import org.martus.common.network.TestShortServerBulletinSummary;
import org.martus.common.utilities.TestBurmeseUtilities;
import org.martus.common.utilities.TestDateUtilities;
import org.martus.common.utilities.TestJpegGeoTagReader;
import org.martus.common.utilities.TestMartusFlexidate;
import org.miradi.utils.TestEnhancedJsonObject;


public class TestCommon
{
	public static void main (String[] args)
	{
		runTests();
	}

	public static void runTests ()
	{
		junit.textui.TestRunner.run (suite());
	}

	public static Test suite ( )
	{
		TestSuite suite= new TestSuite("All Common Martus Tests");

		// common stuff
		suite.addTest(new TestSuite(TestAttachmentPacket.class));
		suite.addTest(new TestSuite(TestAttachmentProxy.class));
		suite.addTest(new TestSuite(TestAuthorizedSessionKeys.class));
		suite.addTest(new TestSuite(TestBase64.class));
		suite.addTest(new TestSuite(TestBase64XmlOutputStream.class));
		suite.addTest(new TestSuite(TestBulletin.class));
		suite.addTest(new TestSuite(TestBulletinHeaderPacket.class));
		suite.addTest(new TestSuite(TestBulletinHistory.class));
		suite.addTest(new TestSuite(TestBulletinHtmlGenerator.class));
		suite.addTest(new TestSuite(TestBulletinLoader.class));
		suite.addTest(new TestSuite(TestBulletinStore.class));
		suite.addTest(new TestSuite(TestBulletinStoreCache.class));
		suite.addTest(new TestSuite(TestBulletinStoreSaveBulletin.class));
		suite.addTest(new TestSuite(TestBulletinSummary.class));
		suite.addTest(new TestSuite(TestBulletinZipImporter.class));
		suite.addTest(new TestSuite(TestBulletinZipUtilities.class));
		suite.addTest(new TestSuite(TestBurmeseUtilities.class));
		suite.addTest(new TestSuite(TestChoiceItem.class));
		suite.addTest(new TestSuite(TestClientFileDatabase.class));
		suite.addTest(new TestSuite(TestContactKey.class));		
		suite.addTest(new TestSuite(TestContactKeys.class));		
		suite.addTest(new TestSuite(TestCustomDropDownFieldSpec.class));
		suite.addTest(new TestSuite(TestCustomFields.class));
		suite.addTest(new TestSuite(TestCustomFieldSpecValidator.class));
		suite.addTest(new TestSuite(TestFormTemplate.class));
		suite.addTest(new TestSuite(TestDammCheckDigitAlgorithm.class));
		suite.addTest(new TestSuite(TestDatabaseKey.class));
		suite.addTest(new TestSuite(TestDateFieldSpec.class));
		suite.addTest(new TestSuite(TestDateRangeFieldSpec.class));
		suite.addTest(new TestSuite(TestDateUtilities.class));
		suite.addTest(new TestSuite(TestDefaultLanguageSettingsProvider.class));
		suite.addTest(new TestSuite(TestDropDownFieldSpec.class));
		suite.addTest(new TestSuite(TestEnhancedJsonObject.class));
		suite.addTest(new TestSuite(TestFieldDataPacket.class));
		suite.addTest(new TestSuite(TestFieldDeskKeys.class));
		suite.addTest(new TestSuite(TestFieldSpec.class));
		suite.addTest(new TestSuite(TestFileDatabase.class));
		suite.addTest(new TestSuite(TestFileInputStreamWithSeek.class));
		suite.addTest(new TestSuite(TestFileOutputStreamViaTemp.class));
		suite.addTest(new TestSuite(TestGridData.class));
		suite.addTest(new TestSuite(TestGridFieldSpec.class));
		suite.addTest(new TestSuite(TestGridRow.class));
		suite.addTest(new TestSuite(TestHeadquartersKeys.class));
		suite.addTest(new TestSuite(TestJpegGeoTagReader.class));
		suite.addTest(new TestSuite(TestLeafNodeCache.class));
		suite.addTest(new TestSuite(TestKeyShareSaveRestore.class));
		suite.addTest(new TestSuite(TestMagicWordEntry.class));
		suite.addTest(new TestSuite(TestMagicWords.class));
		suite.addTest(new TestSuite(TestMartusAccountAccessToken.class));
		suite.addTest(new TestSuite(TestMartusDropdownField.class));
		suite.addTest(new TestSuite(TestMartusField.class));
		suite.addTest(new TestSuite(TestMartusFlexidate.class));
		suite.addTest(new TestSuite(TestMartusKeyPair.class));
		suite.addTest(new TestSuite(TestMartusSearchableGridColumnField.class));
		suite.addTest(new TestSuite(TestMartusSecurity.class));
		suite.addTest(new TestSuite(TestMartusUtilities.class));
		suite.addTest(new TestSuite(TestMartusXml.class));
		suite.addTest(new TestSuite(TestMessageFieldSpec.class));
		suite.addTest(new TestSuite(TestMiniFieldSpec.class));
		suite.addTest(new TestSuite(TestMiniLocalization.class));
		suite.addTest(new TestSuite(TestPacket.class));
		suite.addTest(new TestSuite(TestServerBulletinSummary.class));
		suite.addTest(new TestSuite(TestServerFileDatabase.class));
		suite.addTest(new TestSuite(TestShortServerBulletinSummary.class));
		suite.addTest(new TestSuite(TestUnicodeFileReader.class));
		suite.addTest(new TestSuite(TestUnicodeFileWriter.class));
		suite.addTest(new TestSuite(TestUniversalId.class));
		suite.addTest(new TestSuite(TestXmlWriterFilter.class));
		suite.addTest(new TestSuite(TestZipEntryInputStream.class));
		suite.addTest(new TestSuite(TestExceptions.class));
		return suite;
	}
}
