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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.martus.client.core.MartusApp;
import org.martus.client.core.SafeReadableBulletin;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.field.MartusField;
import org.martus.common.field.MartusSearchableGridColumnField;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.UniversalId;
import org.martus.swing.FontHandler;


public class ReportRunner
{
	public ReportRunner(MartusApp app) throws Exception
	{
		signatureVerifier = app.getSecurity();
		localization = app.getLocalization();
		localization.setSpecialZawgyiFlagForReportRunner(FontHandler.isDoZawgyiConversion());
		fontHelper = new UiFontEncodingHelper(FontHandler.isDoZawgyiConversion());
		
		File logFile = new File(app.getCurrentAccountDirectory(), "velocity.log");
		engine = new VelocityEngine();
		engine.setProperty(RuntimeConstants.RUNTIME_LOG, logFile.getAbsolutePath());
		engine.init();
	}
	
	public void runReport(ReportFormat rf, ReadableDatabase db, SortableBulletinList bulletins, ReportOutput destination, RunReportOptions options, PoolOfReusableChoicesLists reusableChoicesLists) throws Exception
	{
		UniversalId[] uids = bulletins.getSortedUniversalIds();
		ReportOutput breakDestination = destination;
		if(options.hideDetail)
			breakDestination = new NullReportOutput();
		SummaryBreakHandler breakHandler = new SummaryBreakHandler(rf, breakDestination, options, bulletins.getSortSpecs());

		context = new VelocityContext();
		context.put("localization", localization);
		context.put("specsToInclude", new ReportRunnerMiniSpecsToInclude(rf.getSpecsToInclude()));
		
		StringWriter pageBreak = new StringWriter();
		performMerge(rf.getFakePageBreakSection(), pageBreak);
		destination.setFakePageBreak(pageBreak.toString());

		StringWriter documentStart = new StringWriter();
		performMerge(rf.getDocumentStartSection(), documentStart);
		destination.setDocumentStart(documentStart.toString());

		boolean isOneBulletinPerPage = rf.getBulletinPerPage();
		for(int bulletin = 0; bulletin < uids.length; ++bulletin)
		{
			SafeReadableBulletin safeReadableBulletin = getCensoredBulletin(db, uids[bulletin], options);

			boolean isFirstBulletin = bulletin == 0;
			boolean isLastBulletin = bulletin == uids.length - 1;
			boolean isTopOfPage = isFirstBulletin || isOneBulletinPerPage;

			if(isTopOfPage)
			{
				String section = rf.getHeaderSection();
				ReportOutput headerDestination = destination;
				if(options.hideDetail)
					headerDestination = new ReportOutput();
				performMerge(section, headerDestination);
			}

			breakHandler.doBreak(safeReadableBulletin);
			doDetail(rf, destination, options, bulletin, safeReadableBulletin);
			breakHandler.incrementCounts();

			if(isLastBulletin && !isOneBulletinPerPage)
				breakHandler.doFinalBreak();
			
			if(isLastBulletin || isOneBulletinPerPage)
			{
				ReportOutput footerDestination = destination;
				if(options.hideDetail)
					footerDestination = new ReportOutput();
				performMerge(rf.getFooterSection(), footerDestination);
			}
			
			if(isOneBulletinPerPage && 
					!isLastBulletin)
			{
				startNewPage(destination);
			}
		}

		boolean needsSummaryTotalSection = options.printBreaks && (isOneBulletinPerPage || options.hideDetail);
		if(needsSummaryTotalSection)
		{
			if(isOneBulletinPerPage)
				startNewPage(destination);
			context.put("totals", breakHandler.getSummaryTotals());
			performMerge(rf.getTotalSection(), destination);
		}
		
		StringWriter documentEnd = new StringWriter();
		performMerge(rf.getDocumentEndSection(), documentEnd);
		destination.setDocumentEnd(documentEnd.toString());
		context = null;
	}

	private void startNewPage(ReportOutput destination)
	{
		if(!destination.isPageEmpty())
			destination.startNewPage();
	}

	private void doDetail(ReportFormat rf, ReportOutput destination, RunReportOptions options, int bulletin, SafeReadableBulletin safeReadableBulletin) throws Exception
	{
		context.put("i", new Integer(bulletin+1));
		context.put("bulletin", safeReadableBulletin);
		
		ReportOutput detailDestination = destination;
		if(options.hideDetail)
			detailDestination = new ReportOutput();
		
		performMerge(rf.getDetailSection(), detailDestination);
		context.remove("bulletin");
	}

	private SafeReadableBulletin getCensoredBulletin(ReadableDatabase db, UniversalId uid, RunReportOptions options) throws Exception
	{
		DatabaseKey key = DatabaseKey.createLegacyKey(uid);
		Bulletin b = BulletinLoader.loadFromDatabase(db, key, signatureVerifier);
		SafeReadableBulletin safeReadableBulletin = new SafeReadableBulletin(b, localization);
		if(!options.includePrivate)
			safeReadableBulletin.removePrivateData();
		return safeReadableBulletin;
	}

	static class NullReportOutput extends ReportOutput
	{
		public void close() throws IOException
		{
		}

		public void flush() throws IOException
		{
		}

		public void write(char[] cbuf, int off, int len) throws IOException
		{
		}
	
	}
	
	class SummaryBreakHandler
	{
		public SummaryBreakHandler(ReportFormat reportFormatToUse, ReportOutput destination, RunReportOptions options, MiniFieldSpec[] breakSpecsToUse)
		{
			output = destination;
			
			rf = reportFormatToUse;
			breakSpecs = breakSpecsToUse;
			if(!options.printBreaks)
				breakSpecs = new MiniFieldSpec[0];
			
			previousBreakValues = new ReusableChoices("", "");
			for(int i = 0; i < breakSpecs.length; ++i)
				previousBreakValues.add(new ChoiceItem("", ""));
			breakCounts = new int[breakSpecs.length];
			Arrays.fill(breakCounts, 0);
			
			StringVector breakLabels = new StringVector();
			for(int i = 0; i < breakSpecsToUse.length; ++i)
			{
				MiniFieldSpec spec = breakSpecsToUse[i];
				String localizedLabelHtml = StandardFieldSpecs.getLocalizedLabelHtml(spec.getTag(), spec.getLabel(), localization);
				breakLabels.add(fontHelper.getStorable(localizedLabelHtml));
			}
			summaryCounts = new SummaryCount(breakLabels);
		}
		
		public void doBreak(SafeReadableBulletin upcomingBulletin) throws Exception
		{
			if(upcomingBulletin != null)
			{
				ReusableChoices values = new ReusableChoices("", "");
				for(int i = 0; i < breakSpecs.length; ++i)
				{
					values.add(getBreakData(upcomingBulletin, i));
				}
				summaryCounts.increment(values);
			}
			
			int breakLevel = computeBreakLevel(upcomingBulletin);
			
			if(breakLevel < 0)
				return;
			
			for(int level = breakSpecs.length - 1; level >= breakLevel; --level)
			{
				if(breakCounts[0] > 0)
					performBreak(level);
				ChoiceItem currentAsChoiceItem = getBreakData(upcomingBulletin, level);
				previousBreakValues.set(level, currentAsChoiceItem);
				breakCounts[level] = 0;
			}
		}

		private int computeBreakLevel(SafeReadableBulletin upcomingBulletin) throws Exception
		{
			int breakLevel = -1;
			for(int level = 0; level < breakSpecs.length; ++level)
			{
				ChoiceItem current = getBreakData(upcomingBulletin, level);
				if(current == null || !current.equals(previousBreakValues.get(level)))
				{
					breakLevel = level;
					break;
				}
			}
			return breakLevel;
		}
		
		public void doFinalBreak() throws Exception
		{
			doBreak(null);
			context.put("TotalBulletinCount", new Integer(getSummaryTotals().count()));
			performMerge(rf.getTotalBreakSection(), output);
		}
		
		public SummaryCount getSummaryTotals()
		{
			return summaryCounts;
		}

		private ChoiceItem getBreakData(SafeReadableBulletin upcomingBulletin, int breakLevel) throws Exception
		{
			if(upcomingBulletin == null)
				return null;
			
			MartusField thisField = upcomingBulletin.getPossiblyNestedField(breakSpecs[breakLevel]);
			if(thisField == null)
				return new ChoiceItem("", "");
			
			String currentCode = thisField.getDataForSubtotals();
			String currentValue = thisField.htmlForSubtotals(localization);
			return new ChoiceItem(currentCode, currentValue);
		}
		
		private void performBreak(int breakLevel) throws Exception
		{
			PoolOfReusableChoicesLists reusableChoicesForThisBreakOnly = new PoolOfReusableChoicesLists();
			BreakFields breakFields = new BreakFields();
			for(int i = 0; i < breakLevel + 1; ++i)
			{
				MiniFieldSpec miniSpec = breakSpecs[i];
				FieldSpec spec = miniSpec.getType().createEmptyFieldSpec();
				ChoiceItem previousValue = previousBreakValues.get(i);
				if(spec.getType().isDropdown())
				{
					CustomDropDownFieldSpec dropdownSpec = (CustomDropDownFieldSpec) spec;
					ChoiceItem choice = new ChoiceItem(previousValue.getCode(), previousValue.toString());
					ReusableChoices choicesForThisBreak = new ReusableChoices(CustomDropDownFieldSpec.INTERNAL_CHOICES_FOR_BREAK_CODE, "");
					choicesForThisBreak.add(choice);
					reusableChoicesForThisBreakOnly.add(choicesForThisBreak);
					dropdownSpec.addReusableChoicesCode(choicesForThisBreak.getCode());
				}
				spec.setTag(miniSpec.getTag());
				String displayableLabel = miniSpec.getLabel();
				String storableLabel = fontHelper.getStorable(displayableLabel);
				spec.setLabel(storableLabel);
				MartusField field = MartusSearchableGridColumnField.createMartusField(spec, reusableChoicesForThisBreakOnly);
				field.setData(previousValue.getCode());
				breakFields.add(field);
			}
			context.put("BreakLevel", new Integer(breakLevel));
			context.put("BreakCount", new Integer(breakCounts[breakLevel]));
			context.put("BreakFields", breakFields);
			
			ReportOutput breakDestination = output;
			if(rf.getBulletinPerPage())
				breakDestination = new ReportOutput();
			performMerge(rf.getBreakSection(), breakDestination);
		}
		
		public void incrementCounts()
		{
			for(int breakLevel = breakSpecs.length - 1; breakLevel >= 0; --breakLevel)
			{
				++breakCounts[breakLevel];
			}
		}
		
		ReportOutput output;
		ReportFormat rf;
		MiniFieldSpec[] breakSpecs;
		int[] breakCounts;
		ReusableChoices previousBreakValues;
		SummaryCount summaryCounts;
	}
	
	public void performMerge(String template, Writer result) throws Exception
	{
		try
		{
			engine.evaluate(context, result, "Martus", template);
		}
		catch(Exception e)
		{
			System.out.println("-----TEMPLATE-----");
			System.out.println(template);
			System.out.println("------------------");
			throw(e);
		}
	}
	
	public class BreakFields extends Vector
	{
	}

	VelocityEngine engine;
	MtfAwareLocalization localization;
	MartusCrypto signatureVerifier;
	VelocityContext context;
	UiFontEncodingHelper fontHelper;
}
