/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

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
package org.martus.client.bulletinstore.converters;

import java.io.File;
import java.io.StringWriter;
import java.util.Vector;

import org.martus.client.core.BulletinXmlExporter;
import org.martus.client.test.MockMartusApp;
import org.martus.common.FieldSpecCollection;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.DatePreference;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;

abstract public class AbstractTestBulletinAsXmlConverters extends TestCaseEnhanced
{
	public AbstractTestBulletinAsXmlConverters(String name)
	{
		super(name);
	}
	
	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		
		miniLocalization = new MiniLocalization();
		miniLocalization.setCurrentLanguageCode(MiniLocalization.ENGLISH);
		miniLocalization.setCurrentDateFormatCode(new DatePreference().getDateTemplate());
		miniLocalization.setCurrentCalendarSystem(MiniLocalization.GREGORIAN_SYSTEM);
		miniLocalization.setDateFormatFromLanguage();

		
		if(security == null)
		{
			security = MockMartusSecurity.createClient();
		}
		
		if(app == null)
		{
			app = MockMartusApp.create(getName());
			attachmentDirectory = createTempDirectory();
			store = app.getStore();
		}
	}
	
	@Override
	public void tearDown() throws Exception
	{
		DirectoryUtils.deleteAllFilesOnlyInDirectory(attachmentDirectory);
		app.deleteAllFiles();
		store.deleteAllData();
	}
	
	protected Bulletin getBulletinWithoutData() throws Exception
	{
		FieldSpecCollection standardPublicFields = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection standardPrivateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		Bulletin bulletin = new Bulletin(security, standardPublicFields, standardPrivateFields);
		
		return bulletin;
	}
	
	protected Bulletin getBulletinWithBasicData()  throws Exception
	{
		FieldSpecCollection standardPublicFields = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection standardPrivateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		Bulletin bulletin = new Bulletin(security, standardPublicFields, standardPrivateFields);
		String title = "Some Random Title";
		bulletin.setAllPrivate(true);
		bulletin.set(Bulletin.TAGTITLE,title);
		bulletin.setImmutable();
		
		return bulletin;
	}
	
	protected String exportBulletinAsXml(Bulletin bulletinToExport) throws Exception
	{
		Vector<Bulletin> singleItemList = new Vector<>();
		singleItemList.add(bulletinToExport);
		
		return exportBulletinsAsXml(singleItemList);
	}
	
	protected String exportBulletinsAsXml(Vector<Bulletin> listOfBulletinsToExport) throws Exception
	{
		StringWriter writer = new StringWriter();
		
		miniLocalization.addEnglishTranslations(new String[]{"status:draft="+draftTranslation, "status:sealed="+sealedTranslation});
		miniLocalization.setCurrentLanguageCode(MiniLocalization.ENGLISH);
		BulletinXmlExporter exporter = new BulletinXmlExporter(app, miniLocalization, null);
		exporter.exportBulletins(writer, listOfBulletinsToExport, true, false, false, attachmentDirectory);

		return writer.toString();
	}
	
	static final String draftTranslation = "Draft";
	static final String sealedTranslation = "Sealed";
	static MockMartusApp app;
	static File attachmentDirectory;
	
	protected MiniLocalization miniLocalization;
	protected static BulletinStore store;
	protected static MartusCrypto security;
}
