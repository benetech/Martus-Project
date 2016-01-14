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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.core.MartusApp;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.test.MockMartusApp;
import org.martus.common.FieldSpecCollection;
import org.martus.common.LegacyCustomFields;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.Bulletin.DamagedBulletinException;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeLanguage;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.UniversalId;
import org.martus.util.TestCaseEnhanced;


public class TestReportRunner extends TestCaseEnhanced
{
	public TestReportRunner(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		MartusApp app = MockMartusApp.create(MockMartusSecurity.createClient(), getName());
		rr = new ReportRunner(app);
		context = new VelocityContext();
	}	
	
	public void testNoVariables() throws Exception
	{
		String templateWithoutVariables = "no variables";
		assertEquals(templateWithoutVariables, performMerge(templateWithoutVariables));
	}
	
	public void testOneVariable() throws Exception
	{
		String name = "test";
		String value = "hello";
		String templateWithVariable = "$" + name;
		context.put(name, value);
		assertEquals(value, performMerge(templateWithVariable));
	}
	
	public void testComplex() throws Exception
	{
		String[] array = {"dog", "cat", "monkey", };
		context.put("array", array);
		String template = 
			"#* multi-line comment\n" +
			"*#\n" +
			"#foreach ($x in $array)\n" +
			"  $x\n" +
			"#end\n" +
			"$nosuchvariable";
		assertEquals("\n  dog\n  cat\n  monkey\n$nosuchvariable", performMerge(template));
	}
	
	public void testSpecialCharacters() throws Exception
	{
		char[] allAscii = new char[128];
		for(int i =0; i < allAscii.length; ++i)
			allAscii[i] = (char)i;
		allAscii['\''] = ' ';
		allAscii['"'] = ' ';
		allAscii['<'] = ' ';
		allAscii['>'] = ' ';
		
		String nastyLabel = new String(allAscii);
		String template = nastyLabel + ": $field('" + nastyLabel + "')";
		assertEquals("Didn't handle bad characters properly?", template, performMerge(template));
		
	}
	
	public void testRunReport() throws Exception
	{
		MockMartusApp app = MockMartusApp.create(getName());
		app.getLocalization().setCurrentLanguageCode(MiniLocalization.ENGLISH);
		app.loadSampleData();
		BulletinStore store = app.getStore();
		ReportFormat rf = new ReportFormat();
		rf.setDetailSection("$i. $bulletin.localId\n");
		ReportOutput result = new ReportOutput();
		Set leafUids = store.getAllBulletinLeafUids();
		SortableBulletinList list = new SortableBulletinList(app.getLocalization(), new MiniFieldSpec[0]);
		Iterator it = leafUids.iterator();
		while(it.hasNext())
		{
			DatabaseKey key = DatabaseKey.createLegacyKey((UniversalId)it.next());
			list.add(BulletinLoader.loadFromDatabase(store.getDatabase(), key, app.getSecurity()));
		}
		
		RunReportOptions options = new RunReportOptions();
		rr.runReport(rf, store.getDatabase(), list, result, options, PoolOfReusableChoicesLists.EMPTY_POOL);
		result.close();
		StringBuffer expected = new StringBuffer();
		UniversalId[] uids = list.getSortedUniversalIds();
		for(int i=0; i < uids.length; ++i)
		{
			expected.append(Integer.toString(i+1));
			expected.append(". ");
			expected.append(uids[i].getLocalId());
			expected.append("\n");
		}
		assertEquals(new String(expected), result.getPageText(0));
	}
	
	public void testCustomField() throws Exception
	{
		FieldSpec[] specs = new FieldSpec[] 
		{
			FieldSpec.createStandardField("date", new FieldTypeDate()),
			FieldSpec.createStandardField("text", new FieldTypeNormal()),
			FieldSpec.createStandardField("multi", new FieldTypeMultiline()),
			FieldSpec.createStandardField("range", new FieldTypeDateRange()),
			FieldSpec.createStandardField("bool", new FieldTypeBoolean()),
			FieldSpec.createStandardField("language", new FieldTypeLanguage()),
			LegacyCustomFields.createFromLegacy("custom,Custom <label>"),
		};
		
		MockMartusApp app = MockMartusApp.create(getName());
		app.getLocalization().setCurrentLanguageCode(MiniLocalization.ENGLISH);
		Bulletin b = new Bulletin(app.getSecurity(), new FieldSpecCollection(specs), new FieldSpecCollection());
		String sampleCustomData = "Robert Plant";
		b.set("custom", sampleCustomData);
		b.setAllPrivate(false);
		app.saveBulletin(b, app.getFolderDraftOutbox());
		
		SortableBulletinList list = new SortableBulletinList(app.getLocalization(), new MiniFieldSpec[0]);
		list.add(b);
		ReportFormat rf = new ReportFormat();
		rf.setDetailSection("$bulletin.field('custom')");
		ReportOutput result = new ReportOutput();
		RunReportOptions options = new RunReportOptions();
		rr.runReport(rf, app.getStore().getDatabase(), list, result, options, PoolOfReusableChoicesLists.EMPTY_POOL);
		result.close();
		
		assertEquals(sampleCustomData, result.getPageText(0));
	}
	
	public void testStartSection() throws Exception
	{
		ReportFormat rf = new ReportFormat();
		String startSection = "start";
		rf.setDocumentStartSection(startSection);
		ReportOutput result = runReportOnSampleData(rf);
		assertEquals("didn't output start section?", startSection, result.getDocumentStart());
	}
	
	public void testBreakSection() throws Exception
	{
		String sampleDate = "2004-06-19";
		MockMartusApp app = createAppWithBulletinsForBreaks(sampleDate);
		ReportFormat rf = new ReportFormat();
		String breakSection = "$BreakLevel had $BreakCount\n" +
				"#foreach($x in [0..$BreakLevel])\n" +
				"$BreakFields.get($x).getLocalizedLabelHtml($localization): " +
				"$BreakFields.get($x).html($localization) " +
				"#end\n\n";
		rf.setBreakSection(breakSection);
		
		RunReportOptions options = new RunReportOptions();
		options.includePrivate = true;
		options.hideDetail = false;
		options.printBreaks = true;
		
		MiniLocalization localization = new MiniLocalization();
		String authorLabel = localization.getFieldLabelHtml(Bulletin.TAGAUTHOR);
		String summaryLabel = localization.getFieldLabelHtml(Bulletin.TAGSUMMARY);
		
		
		ReportOutput sortByAuthorSummary = runReportOnAppData(rf, app, options);
		assertEquals("1 had 2\n" + authorLabel + ": a " + summaryLabel + ": 1 \n" +
				"1 had 1\n" + authorLabel + ": a " + summaryLabel + ": 2 \n" +
				"0 had 3\n" + authorLabel + ": a \n" +
				"1 had 1\n" + authorLabel + ": b " + summaryLabel + ": 2 \n" +
				"0 had 1\n" + authorLabel + ": b \n", 
				sortByAuthorSummary.getPageText(0));
		
		MiniFieldSpec[] entryDateSorting = {
			new MiniFieldSpec(StandardFieldSpecs.findStandardFieldSpec(Bulletin.TAGENTRYDATE)),
		};
		
		String entryDateLabel = localization.getFieldLabelHtml(Bulletin.TAGENTRYDATE);
		String formattedDate = localization.convertStoredDateToDisplay(sampleDate);
		ReportOutput sortedByEntryDate = runReportOnAppData(rf, app, options, entryDateSorting);
		assertEquals("0 had 4\n" + entryDateLabel + ": " + formattedDate + " \n", sortedByEntryDate.getPageText(0));
		
		options.printBreaks = false;
		assertEquals("Still had output?", "", runReportOnAppData(rf, app, options).getPageText(0));
	}

	private MockMartusApp createAppWithBulletinsForBreaks(String sampleDate) throws Exception
	{
		MockMartusApp app = MockMartusApp.create(getName());
		createAndSaveSampleBulletin(app, "a", "1", sampleDate);
		createAndSaveSampleBulletin(app, "a", "1", sampleDate);
		createAndSaveSampleBulletin(app, "a", "2", sampleDate);
		createAndSaveSampleBulletin(app, "b", "2", sampleDate);
		return app;
	}
	
	public void testBreaksOnReusableDropdowns() throws Exception
	{
		verifySummaryBreaksOnReusableDropdowns(true);
		verifySummaryBreaksOnReusableDropdowns(false);
	}

	private void verifySummaryBreaksOnReusableDropdowns(boolean withDetail)
			throws Exception, IOException
	{
		ReusableChoices choices = new ReusableChoices("choicescode", "Choices Label");
		String aLabel = "Fabulous A";
		choices.add(new ChoiceItem("a", aLabel));
		String bLabel = "Excellent B";
		choices.add(new ChoiceItem("b", bLabel));
		MockMartusApp app = MockMartusApp.create(getName());
		FieldSpecCollection defaultSpecs = app.getStore().getTopSectionFieldSpecs();
		FieldSpecCollection specs = new FieldSpecCollection();
		for(int i = 0; i < defaultSpecs.size(); ++i)
			specs.add(defaultSpecs.get(i));
		specs.addReusableChoiceList(choices);
		CustomDropDownFieldSpec dropdown = new CustomDropDownFieldSpec();
		dropdown.setTag("dd");
		dropdown.setLabel("Dropdown");
		dropdown.addReusableChoicesCode(choices.getCode());
		specs.add(dropdown);
		FormTemplate template = new FormTemplate("title", "", specs, StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		app.getStore().saveNewFormTemplate(template);
		app.getStore().setFormTemplate(template.getTitle());
		
		Bulletin b1 = app.createBulletin();
		b1.set(dropdown.getTag(), "a");
		app.saveBulletin(b1, app.getStore().getFolderSaved());
		Bulletin b2 = app.createBulletin();
		b2.set(dropdown.getTag(), "b");
		app.saveBulletin(b2, app.getStore().getFolderSaved());
		
		MiniLocalization localization = new MiniLocalization();
		MiniFieldSpec[] sortSpecs = new MiniFieldSpec[] {new MiniFieldSpec(dropdown)};
		localization.setCurrentLanguageCode("en");
		ReportFormat report = new TabularReportBuilder(localization).createTabular(sortSpecs);
		ReportOutput destination = new ReportOutput();
		RunReportOptions options = new RunReportOptions();
		options.hideDetail = withDetail;
		options.printBreaks = true;
		options.includePrivate = true;
		ReportRunner runner = new ReportRunner(app);
		SortableBulletinList bulletins = new SortableBulletinList(localization, sortSpecs);
		bulletins.add(b1);
		bulletins.add(b2);
		runner.runReport(report, app.getStore().getDatabase(), bulletins, destination, options, specs.getAllReusableChoiceLists());
		destination.close();
		String result = destination.getPrintableDocument();
		assertContains(withDetail + " Wrote code instead of label a?", aLabel, result);
		assertContains(withDetail + " Wrote code instead of label b?", bLabel, result);
	}

	public void testSummaryTotals() throws Exception
	{
		String sampleDate = "2004-06-19";
		MockMartusApp app = createAppWithBulletinsForBreaks(sampleDate);
		RunReportOptions options = new RunReportOptions();
		options.printBreaks = true;
		options.includePrivate = true;
		options.hideDetail = true;
		
		ReportFormat rf = new ReportFormat();
		rf.setTotalSection("TOTALS $totals.count()\n" +
				"#foreach($summary1 in $totals.children())\n" +
				"1. $summary1.label(): $summary1.value() = $summary1.count()\n" +
				"#foreach($summary2 in $summary1.children())\n" +
				"2. $summary2.label(): $summary2.value() = $summary2.count()\n" +
				"#foreach($summary3 in $summary2.children())\n" +
				"3. $summary3.label(): $summary3.value() = $summary3.count()\n" +
				"#end\n" + 
				"#end\n" + 
				"#end\n");
		
		MiniLocalization localization = new MiniLocalization();
		String authorLabel = localization.getFieldLabelHtml(Bulletin.TAGAUTHOR);
		String summaryLabel = localization.getFieldLabelHtml(Bulletin.TAGSUMMARY);
		ReportOutput totals = runReportOnAppData(rf, app, options);
		assertEquals("TOTALS 4\n" + 
				"1. " + authorLabel + ": a = 3\n" +
				"2. " + summaryLabel + ": 1 = 2\n" +
				"2. " + summaryLabel + ": 2 = 1\n" +
				"1. " + authorLabel + ": b = 1\n" +
				"2. " + summaryLabel + ": 2 = 1\n", totals.getPageText(0));
		
		options.printBreaks = false;
		ReportOutput noTotals = runReportOnAppData(rf, app, options);
		assertEquals("printed total section?", "", noTotals.getPageText(0));
		
		rf.setBreakSection("BREAK");
		options.printBreaks = true;
		options.hideDetail = true;
		ReportOutput totalsOnly = runReportOnAppData(rf, app, options);
		assertNotContains("Still printed breaks?", "BREAK", totalsOnly.getPageText(0));
	}
	
	public void testTotalsInPageReport() throws Exception
	{
		MockMartusApp app = MockMartusApp.create(getName());
		app.loadSampleData();

		ReportFormat rf = new ReportFormat();
		rf.setBulletinPerPage(true);
		rf.setTotalSection("Totals");
		
		RunReportOptions detailOnly = new RunReportOptions();
		detailOnly.hideDetail = false;
		detailOnly.printBreaks = false;
		ReportOutput details = runReportOnAppData(rf, app, detailOnly);
		assertEquals("", details.getPrintableDocument());
		
		RunReportOptions detailAndSummary = new RunReportOptions();
		detailAndSummary.hideDetail = false;
		detailAndSummary.printBreaks = true;
		ReportOutput both = runReportOnAppData(rf, app, detailAndSummary);
		assertEquals("Totals", both.getPrintableDocument());
		
		RunReportOptions summaryOnly = new RunReportOptions();
		summaryOnly.hideDetail = true;
		summaryOnly.printBreaks = true;
		ReportOutput summary = runReportOnAppData(rf, app, summaryOnly);
		assertEquals("Totals", summary.getPrintableDocument());
		
	}
	
	public void testOmitDetail() throws Exception
	{
		String sampleDate = "2004-06-19";
		MockMartusApp app = createAppWithBulletinsForBreaks(sampleDate);
		RunReportOptions options = new RunReportOptions();
		options.includePrivate = true;
		options.printBreaks = true;
		
		ReportFormat rf = new ReportFormat();
		rf.setDocumentStartSection("Start ");
		rf.setDetailSection("Detail ");
		rf.setBreakSection("Break ");
		rf.setHeaderSection("Header ");
		rf.setFooterSection("Footer ");
		rf.setTotalBreakSection("TotalBreak ");
		rf.setTotalSection("Total ");
		rf.setDocumentEndSection("End ");
		rf.setFakePageBreakSection(". ");
		
		ReportOutput sortByAuthorSummaryWithDetail = runReportOnAppData(rf, app, options);
		assertEquals("Start Header Detail Detail Break Detail Break Break Detail Break Break TotalBreak Footer . End ", sortByAuthorSummaryWithDetail.getPrintableDocument());
		
		options.hideDetail = true;
		ReportOutput sortByAuthorSummaryWithoutDetail = runReportOnAppData(rf, app, options);
		assertEquals("Start Total . End ", sortByAuthorSummaryWithoutDetail.getPrintableDocument());
		
		
		rf.setBulletinPerPage(true);
		options.hideDetail = false;
		ReportOutput pageWithDetail = runReportOnAppData(rf, app, options);
		assertEquals("Start Header Detail Footer . Header Detail Footer . Header Detail Footer . Header Detail Footer . Total . End ", pageWithDetail.getPrintableDocument());

		options.hideDetail = true;
		ReportOutput pageWithoutDetail = runReportOnAppData(rf, app, options);
		assertEquals("Start Total . End ", pageWithoutDetail.getPrintableDocument());
		
	}

	private void createAndSaveSampleBulletin(MockMartusApp app, String author, String summary, String entryDate) throws Exception
	{
		BulletinFolder outbox = app.getFolderDraftOutbox();
		Bulletin b = app.createBulletin();
		b.set(Bulletin.TAGAUTHOR, author);
		b.set(Bulletin.TAGSUMMARY, summary);
		b.set(Bulletin.TAGENTRYDATE, entryDate);
		app.saveBulletin(b, outbox);
	}
	
	public void testEndSection() throws Exception
	{
		ReportFormat rf = new ReportFormat();
		String endSection = "end";
		rf.setDocumentEndSection(endSection);
		ReportOutput result = runReportOnSampleData(rf);
		assertEquals("didn't output end section?", endSection, result.getDocumentEnd());
	}
	
	public void testPageReport() throws Exception
	{
		MockMartusApp app = MockMartusApp.create(getName());
		FieldSpec[] topFields = {
			FieldSpec.createStandardField(Bulletin.TAGAUTHOR, new FieldTypeNormal()),
			FieldSpec.createCustomField("tag2", "Label 2", new FieldTypeDate()),
		};
		Bulletin b = new Bulletin(app.getSecurity(), new FieldSpecCollection(topFields), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		b.set(topFields[0].getTag(), "First");
		b.set(topFields[1].getTag(), "2005-04-07");
		b.set(Bulletin.TAGPRIVATEINFO, "Secret");
		app.saveBulletin(b, app.getFolderDraftOutbox());
		
		Bulletin b2 = new Bulletin(app.getSecurity(), new FieldSpecCollection(topFields), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		b2.set(topFields[0].getTag(), "Second");
		b2.set(topFields[1].getTag(), "2003-03-29");
		b2.set(Bulletin.TAGPRIVATEINFO, "Another secret");
		app.saveBulletin(b2, app.getFolderDraftOutbox());
		

		ReportFormat rf = new ReportFormat();
		rf.setBulletinPerPage(true);
		rf.setHeaderSection("Header\n");
		rf.setFooterSection("Footer\n");
		rf.setFakePageBreakSection("----\n");
		rf.setDetailSection("TOP:\n" +
				"#foreach($field in $bulletin.getTopFields())\n" +
				"$field.getLocalizedLabel($localization) $field.html($localization)\n" +
				"#end\n" +
				"BOTTOM:\n" +
				"#foreach($field in $bulletin.getBottomFields())\n" +
				"$field.getLocalizedLabel($localization) $field.html($localization)\n" +
				"#end\n" +
				"");
		String expected0 = "Header\n" +
				"TOP:\n" +
				"<field:author> First\n" +
				"Label 2 04/07/2005\n" +
				"BOTTOM:\n" +
				"<field:privateinfo> Secret\n" +
				"Footer\n";
		String expected1 = "Header\n" +
				"TOP:\n" +
				"<field:author> Second\n" +
				"Label 2 03/29/2003\n" +
				"BOTTOM:\n" +
				"<field:privateinfo> Another secret\n" +
				"Footer\n";
		
		RunReportOptions options = new RunReportOptions();
		options.includePrivate = true;
		ReportOutput result = runReportOnAppData(rf, app, options);
		assertEquals("Wrong page report output?", expected0, result.getPageText(0));
		assertEquals("Wrong page report output?", expected1, result.getPageText(1));
		assertEquals("Didn't set fake page break?", "----\n", result.getFakePageBreak());
	}
	
	private ReportOutput runReportOnSampleData(ReportFormat rf) throws Exception
	{
		MockMartusApp app = MockMartusApp.create(getName());
		app.loadSampleData();
		return runReportOnAppData(rf, app);
	}

	private ReportOutput runReportOnAppData(ReportFormat rf, MockMartusApp app) throws Exception
	{
		RunReportOptions options = new RunReportOptions();
		return runReportOnAppData(rf, app, options);
	}

	private ReportOutput runReportOnAppData(ReportFormat rf, MockMartusApp app, RunReportOptions options) throws IOException, DamagedBulletinException, NoKeyPairException, Exception
	{
		MiniFieldSpec sortSpecs[] = {
				new MiniFieldSpec(StandardFieldSpecs.findStandardFieldSpec(Bulletin.TAGAUTHOR)), 
				new MiniFieldSpec(StandardFieldSpecs.findStandardFieldSpec(Bulletin.TAGSUMMARY)),
			};

		return runReportOnAppData(rf, app, options, sortSpecs);
	}

	private ReportOutput runReportOnAppData(ReportFormat rf, MockMartusApp app, RunReportOptions options, MiniFieldSpec[] sortSpecs) throws IOException, DamagedBulletinException, NoKeyPairException, Exception
	{
		BulletinStore store = app.getStore();
		MartusCrypto security = app.getSecurity();
		ReadableDatabase db = store.getDatabase();
		Set leafUids = store.getAllBulletinLeafUids();
		MiniLocalization localization = new MiniLocalization();
		localization.setCurrentLanguageCode(MiniLocalization.ENGLISH);
		SortableBulletinList list = new SortableBulletinList(localization, sortSpecs);
		Iterator it = leafUids.iterator();
		while(it.hasNext())
		{
			DatabaseKey key = DatabaseKey.createLegacyKey((UniversalId)it.next());
			Bulletin b = BulletinLoader.loadFromDatabase(db, key, security);
			list.add(b);
		}
		ReportOutput result = new ReportOutput();
		rr.runReport(rf, store.getDatabase(), list, result, options, PoolOfReusableChoicesLists.EMPTY_POOL);
		result.close();
		return result;
	}
	
	private String performMerge(String template) throws Exception
	{
		StringWriter result = new StringWriter();
		rr.context = context;
		rr.performMerge(template, result);
		return result.toString();
	}

	ReportRunner rr;
	VelocityContext context;
}
