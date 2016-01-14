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
package org.martus.common.bulletin;

import org.martus.common.EnglishCommonStrings;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.MiniLocalization;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.MockBulletinStore;
import org.martus.common.test.TestGridData;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.language.LanguageOptions;
import org.martus.util.xml.XmlUtilities;


public class TestBulletinHtmlGenerator extends TestCaseEnhanced
{

	public TestBulletinHtmlGenerator(String name)
	{
		super(name);
	}
    public void setUp() throws Exception
    {
    	super.setUp();
		if(security == null)
			security = MockMartusSecurity.createClient();
		if(loc == null)
		{
			loc = new MiniLocalization(EnglishCommonStrings.strings);
			loc.setCurrentLanguageCode(MiniLocalization.ENGLISH);
		}
		if(store == null)
			store = new MockBulletinStore(this);

   }	
    
	public void testGetSectionHtmlString() throws Exception
	{
		FieldSpecCollection standardPublicFields = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection standardPrivateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		Bulletin b = new Bulletin(security, standardPublicFields, standardPrivateFields);
		String title = "My Title";
		b.set(Bulletin.TAGTITLE, title);
		
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(loc);
		String expectedHtml ="<tr><td align='right' valign='top'>Language</td><td align='left' valign='top'>-Other-</td></tr>\n" +
		"<tr><td align='right' valign='top'>Author</td><td align='left' valign='top'></td></tr>\n" +
		"<tr><td align='right' valign='top'>Organization</td><td align='left' valign='top'></td></tr>\n" +
		"<tr><td align='right' valign='top'>Title</td><td align='left' valign='top'><strong>My Title</strong></td></tr>\n" +
		"<tr><td align='right' valign='top'>Location</td><td align='left' valign='top'></td></tr>\n" +
		"<tr><td align='right' valign='top'>Keywords</td><td align='left' valign='top'></td></tr>\n" +
		"<tr><td align='right' valign='top'>Date of Event</td><td align='left' valign='top'>"+XmlUtilities.getXmlEncoded(loc.convertStoredDateToDisplay(b.get(Bulletin.TAGEVENTDATE)))+"</td></tr>\n" +
		"<tr><td align='right' valign='top'>Date Created</td><td align='left' valign='top'>"+XmlUtilities.getXmlEncoded(loc.convertStoredDateToDisplay(b.get(Bulletin.TAGENTRYDATE)))+"</td></tr>\n" +
		"<tr><td align='right' valign='top'>Summary</td><td align='left' valign='top'><p></p></td></tr>\n" +
		"<tr><td align='right' valign='top'>Details</td><td align='left' valign='top'><p></p></td></tr>\n";
		assertEquals("Public Section HTML not correct?", expectedHtml, generator.getSectionHtmlString(b.getFieldDataPacket()));
		expectedHtml = "<tr><td align='right' valign='top'>Additional Information</td><td align='left' valign='top'><p></p></td></tr>\n";
		assertEquals("Private Section HTML not correct?",expectedHtml, generator.getSectionHtmlString(b.getPrivateFieldDataPacket()));
	}

	public void testGetHtmlString() throws Exception
	{
		FieldSpecCollection standardPublicFields = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection standardPrivateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		Bulletin b = new Bulletin(security, standardPublicFields, standardPrivateFields);
		String title = "My New Title";
		b.setAllPrivate(true);
		b.set(Bulletin.TAGTITLE,title);
		b.setImmutable();
		
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(loc);

		store.saveBulletinForTesting(b);
		String expectedHtml = "<html><table width='80'>\n<tr><td width='15%'></td><td width='85%'></td></tr>\n" +
			"<tr><td align='right' valign='top'>Last Saved</td><td align='left' valign='top'>"+loc.formatDateTime(b.getLastSavedTime())+"</td></tr>\n"+
			"<tr><td align='right' valign='top'>Version</td><td align='left' valign='top'>1</td></tr>\n"+
			"<tr><td align='right' valign='top'>Bulletin Status:</td><td align='left' valign='top'>Sealed</td></tr>\n"+
			"<tr></tr>\n" + 
			"<tr><td colspan='2' align='left'><u><b>Private Information</b></u></td></tr>\n"+
			"<tr><td align='right' valign='top'>Keep ALL Information Private</td><td align='left' valign='top'>Yes</td></tr>\n"+
			"<tr><td align='right' valign='top'>Language</td><td align='left' valign='top'>-Other-</td></tr>\n"+
			"<tr><td align='right' valign='top'>Author</td><td align='left' valign='top'></td></tr>\n"+
			"<tr><td align='right' valign='top'>Organization</td><td align='left' valign='top'></td></tr>\n"+
			"<tr><td align='right' valign='top'>Title</td><td align='left' valign='top'><strong>"+title+"</strong></td></tr>\n"+
			"<tr><td align='right' valign='top'>Location</td><td align='left' valign='top'></td></tr>\n"+
			"<tr><td align='right' valign='top'>Keywords</td><td align='left' valign='top'></td></tr>\n"+
			"<tr><td align='right' valign='top'>Date of Event</td><td align='left' valign='top'>"+XmlUtilities.getXmlEncoded(loc.convertStoredDateToDisplay(b.get(Bulletin.TAGEVENTDATE)))+"</td></tr>\n"+
			"<tr><td align='right' valign='top'>Date Created</td><td align='left' valign='top'>"+XmlUtilities.getXmlEncoded(loc.convertStoredDateToDisplay(b.get(Bulletin.TAGENTRYDATE)))+"</td></tr>\n"+
			"<tr><td align='right' valign='top'>Summary</td><td align='left' valign='top'><p></p></td></tr>\n"+
			"<tr><td align='right' valign='top'>Details</td><td align='left' valign='top'><p></p></td></tr>\n"+
			"<tr><td align='right' valign='top'>Attachments</td><td align='left' valign='top'></td></tr>\n"+
			"<tr></tr>\n" + 
			"<tr><td colspan='2' align='left'><u><b>Private Information</b></u></td></tr>\n"+
			"<tr><td align='right' valign='top'>Additional Information</td><td align='left' valign='top'><p></p></td></tr>\n"+
			"<tr><td align='right' valign='top'>Attachments</td><td align='left' valign='top'></td></tr>\n"+
			"<tr></tr>\n" + 
			"<tr><td colspan='2' align='left'><u><b>Contacts</b></u></td></tr>\n"+
			"<tr><td align='right' valign='top'></td><td align='left' valign='top'>No Contact accounts selected for this record.</td></tr>\n"+
			"<tr></tr>\n" + 
			"<tr><td align='right' valign='top'>Bulletin Id:</td><td align='left' valign='top'>"+b.getLocalId()+"</td></tr>\n"+
			"</table></html>";
		assertEquals("Entire Bulletin's HTML not correct", expectedHtml, generator.getHtmlString(b, store.getDatabase(), true, true));
	}

	public void testGetPublicOnlyHtmlString() throws Exception
	{

		FieldSpecCollection standardPublicFields = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection standardPrivateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		Bulletin b = new Bulletin(security, standardPublicFields, standardPrivateFields);
		String title = "My Title";
		b.setAllPrivate(false);
		b.set(Bulletin.TAGTITLE,title);
		b.setMutable();
		
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(loc);
		String expectedHtml = "<html><table width='80'>\n<tr><td width='15%'></td><td width='85%'></td></tr>\n" +
			"<tr><td align='right' valign='top'>Last Saved</td><td align='left' valign='top'>"+loc.formatDateTime(b.getLastSavedTime())+"</td></tr>\n"+
			"<tr><td align='right' valign='top'>Version</td><td align='left' valign='top'>1</td></tr>\n"+
			"<tr><td align='right' valign='top'>Bulletin Status:</td><td align='left' valign='top'>Draft</td></tr>\n"+
			"<tr></tr>\n" + 
			"<tr><td align='right' valign='top'>Contact Bulletin</td><td align='left' valign='top'></td></tr>\n"+
			"<tr></tr>\n" + 
			"<tr><td colspan='2' align='left'><u><b>Public Information</b></u></td></tr>\n"+
			"<tr><td align='right' valign='top'>Keep ALL Information Private</td><td align='left' valign='top'>No</td></tr>\n"+
			"<tr><td align='right' valign='top'>Language</td><td align='left' valign='top'>-Other-</td></tr>\n"+
			"<tr><td align='right' valign='top'>Author</td><td align='left' valign='top'></td></tr>\n"+
			"<tr><td align='right' valign='top'>Organization</td><td align='left' valign='top'></td></tr>\n"+
			"<tr><td align='right' valign='top'>Title</td><td align='left' valign='top'><strong>"+title+"</strong></td></tr>\n"+
			"<tr><td align='right' valign='top'>Location</td><td align='left' valign='top'></td></tr>\n"+
			"<tr><td align='right' valign='top'>Keywords</td><td align='left' valign='top'></td></tr>\n"+
			"<tr><td align='right' valign='top'>Date of Event</td><td align='left' valign='top'>"+XmlUtilities.getXmlEncoded(loc.convertStoredDateToDisplay(b.get(Bulletin.TAGEVENTDATE)))+"</td></tr>\n"+
			"<tr><td align='right' valign='top'>Date Created</td><td align='left' valign='top'>"+XmlUtilities.getXmlEncoded(loc.convertStoredDateToDisplay(b.get(Bulletin.TAGENTRYDATE)))+"</td></tr>\n"+
			"<tr><td align='right' valign='top'>Summary</td><td align='left' valign='top'><p></p></td></tr>\n"+
			"<tr><td align='right' valign='top'>Details</td><td align='left' valign='top'><p></p></td></tr>\n"+
			"<tr><td align='right' valign='top'>Attachments</td><td align='left' valign='top'></td></tr>\n"+
			"<tr></tr>\n" + 
			"<tr><td colspan='2' align='left'><u><b>Contacts</b></u></td></tr>\n"+
			"<tr><td align='right' valign='top'></td><td align='left' valign='top'>No Contact accounts selected for this record.</td></tr>\n"+
			"<tr></tr>\n" + 
			"<tr><td align='right' valign='top'>Bulletin Id:</td><td align='left' valign='top'>"+b.getLocalId()+"</td></tr>\n"+
			"</table></html>";
		assertEquals("Entire Bulletin's HTML not correct", expectedHtml, generator.getHtmlString(b, store.getDatabase(), false, false));
	}
	
	public void testGetHtmlStringWithGrids() throws Exception
	{
		GridData grid = TestGridData.createSampleGridWithData();
		GridFieldSpec gridSpec = TestGridData.createSampleGridSpec();

		FieldSpecCollection gridSpecs = new FieldSpecCollection(new FieldSpec[] {gridSpec});
		FieldSpecCollection standardPrivateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		Bulletin b = new Bulletin(security, gridSpecs, standardPrivateFields);
		b.set(gridSpec.getTag(), grid.getXmlRepresentation());
		
		
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(loc);
		String expectedHtml ="<tr><td align='right' valign='top'></td><td align='left' valign='top'>" +
				"<table border='1' align='left'>" +
				"<tr><th align='center'> </th><th align='center'>Column 1</th><th align='center'>Column 2</th></tr>" +
				"<tr><td align='left'>1</td><td align='left'>data1</td><td align='left'>&lt;&amp;data2&gt;</td></tr>" +
				"<tr><td align='left'>2</td><td align='left'>data3</td><td align='left'>data4</td></tr>" +
				"</table></td></tr>\n";
		assertEquals("HTML Grids not correct?", expectedHtml, generator.getSectionHtmlString(b.getFieldDataPacket()));
	}

	public void testGetHtmlStringWithDropDowns() throws Exception
	{
		CustomDropDownFieldSpec dropdownSpec = new CustomDropDownFieldSpec();
		ChoiceItem[] choices = new ChoiceItem[] {
			new ChoiceItem("green", "Green"),
			new ChoiceItem("blue", "Blue"),
		};
		
		dropdownSpec.setChoices(choices);
		dropdownSpec.setTag("myDropDownTag");
		dropdownSpec.setLabel("myDropdownLabel");
		FieldSpecCollection dropdownSpecs = new FieldSpecCollection(new FieldSpec[] {dropdownSpec});
		FieldSpecCollection standardPrivateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		Bulletin b = new Bulletin(security, dropdownSpecs, standardPrivateFields);
		b.set(dropdownSpec.getTag(), "blue");
		
		
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(loc);
		String expectedHtml ="<tr><td align='right' valign='top'>myDropdownLabel</td><td align='left' valign='top'>Blue</td></tr>\n";
		assertEquals("HTML Dropdowns not correct?", expectedHtml, generator.getSectionHtmlString(b.getFieldDataPacket()));
	}

	public void testGetHtmlStringWithEmptyGrids() throws Exception
	{
		GridData grid = TestGridData.createSampleGridWithOneEmptyRow();
		GridFieldSpec gridSpec = new GridFieldSpec();
		String label1 = "Column 1";
		FieldSpec column1 = FieldSpec.createFieldSpec(label1, new FieldTypeNormal());
		String label2 = "Column 2";
		FieldSpec column2 = FieldSpec.createFieldSpec(label2, new FieldTypeNormal());

		gridSpec.addColumn(column1);
		gridSpec.addColumn(column2);
		FieldSpecCollection gridSpecs = new FieldSpecCollection(new FieldSpec[] {gridSpec});
		FieldSpecCollection standardPrivateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		Bulletin b = new Bulletin(security, gridSpecs, standardPrivateFields);
		b.set(gridSpec.getTag(), grid.getXmlRepresentation());
		
		
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(loc);
		String expectedHtml ="<tr><td align='right' valign='top'></td><td align='left' valign='top'></td></tr>\n";
		assertEquals("HTML Empty Grids not correct?", expectedHtml, generator.getSectionHtmlString(b.getFieldDataPacket()));
	}
	
	public void testRightToLeft() throws Exception
	{
		FieldSpecCollection standardPublicFields = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection standardPrivateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();

		Bulletin b = new Bulletin(security, standardPublicFields, standardPrivateFields);
		b.set(BulletinConstants.TAGAUTHOR, "Bradbury");
		store.saveBulletinForTesting(b);
		
		LanguageOptions.setDirectionRightToLeft();
		try
		{
			BulletinHtmlGenerator generator = new BulletinHtmlGenerator(loc);
			String html = generator.getHtmlString(b, store.getDatabase(), true, true);
			assertStartsWith("column widths not reversed?", "<html><table width='80'>\n<tr><td width='85%'></td><td width='15%'></td></tr>\n", html);
			assertContains("column values not reversed?", "<tr><td align='right' valign='top'>Bradbury</td><td align='left' valign='top'>Author</td></tr>\n", html);
			assertContains("heading not aligned right?", "<tr><td colspan='2' align='right'><u><b>Contacts</b></u></td></tr>\n", html);
		}
		finally
		{
			LanguageOptions.setDirectionLeftToRight();
		}
	}
	
	public void testKeepWithPrevious() throws Exception
	{
		FieldSpec one = FieldSpec.createStandardField(Bulletin.TAGAUTHOR, new FieldTypeNormal());
		FieldSpec two = FieldSpec.createFromXml("<Field type='NORMAL'><Tag>" + 
				Bulletin.TAGKEYWORDS + 
				"</Tag><KeepWithPrevious/></Field>");
		FieldSpecCollection specs = new FieldSpecCollection(new FieldSpec[] {one, two, });
		final String SAMPLE_AUTHOR = "Bill Preston";
		final String SAMPLE_KEYWORDS = "blue green red";
		UniversalId uid = FieldDataPacket.createUniversalId(security);
		FieldDataPacket fdp = new FieldDataPacket(uid, specs);
		fdp.set(Bulletin.TAGAUTHOR, SAMPLE_AUTHOR);
		fdp.set(Bulletin.TAGKEYWORDS, SAMPLE_KEYWORDS);
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(loc);
		
		{
			String html = generator.getSectionHtmlString(fdp);
			String EXPECTED_HTML = SAMPLE_AUTHOR + "</td>" +
					"<td width='10'></td>" +
					"<td align='left' valign='top'>" + loc.getFieldLabel(Bulletin.TAGKEYWORDS) + "</td>" +
					"<td width='10'></td>" + 
					"<td align='left' valign='top'>" + SAMPLE_KEYWORDS;
			assertContains("Didn't create subtable for the row?", EXPECTED_HTML, html);
		}
		
		LanguageOptions.setDirectionRightToLeft();
		try
		{
			String html = generator.getSectionHtmlString(fdp);
			String EXPECTED_HTML = 
				SAMPLE_KEYWORDS + "</td>" +
				"<td width='10'></td>" +
				"<td align='right' valign='top'>" + loc.getFieldLabel(Bulletin.TAGKEYWORDS) + "</td>" +
				"<td width='10'></td>" +
				"<td align='right' valign='top'>" + SAMPLE_AUTHOR;
			assertContains("Right-to-left Didn't create subtable for the row?", EXPECTED_HTML, html);
		}
		finally
		{
			LanguageOptions.setDirectionLeftToRight();
		}
	}
	
	private static MockMartusSecurity security;
	private static MiniLocalization loc;
	private static MockBulletinStore store;
}
