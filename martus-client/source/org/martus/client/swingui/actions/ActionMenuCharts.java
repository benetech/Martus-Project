/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2012, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.client.swingui.actions;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.text.AttributedString;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.labels.AbstractPieItemLabelGenerator;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.DateTitle;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.ShortTextTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.martus.client.core.SafeReadableBulletin;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.reports.ChartAnswers;
import org.martus.client.reports.MartusChartTheme;
import org.martus.client.search.SaneCollator;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiChartPreviewDlg;
import org.martus.clientside.FormatFilter;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.packet.UniversalId;
import org.martus.swing.FontHandler;
import org.martus.swing.PrintUtilities;
import org.martus.swing.Utilities;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class ActionMenuCharts extends UiMenuAction implements ActionDoer
{
	public ActionMenuCharts(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "Charts");
		fontHelper = new UiFontEncodingHelper(mainWindowToUse.getDoZawgyiConversion());
	}

	public void actionPerformed(ActionEvent events)
	{
		doAction();
	}

	public void doAction()
	{
		try
		{
			// Re-enable the following when we allow saving chart templates
//			MartusLocalization localization = mainWindow.getLocalization();
//			
////			String runButtonLabel = localization.getButtonLabel("RunChart");
//			String createChartButtonLabel = localization.getButtonLabel("CreateChart");
//			String cancelButtonLabel = localization.getButtonLabel(EnglishCommonStrings.CANCEL);
//			String[] buttonLabels = {/*runButtonLabel,*/ createChartButtonLabel, cancelButtonLabel, };
//			String title = mainWindow.getLocalization().getWindowTitle("RunOrCreateChart");
//			UiPushbuttonsDlg runOrCreate = new UiPushbuttonsDlg(mainWindow, title, buttonLabels);
//			runOrCreate.setVisible(true);
//			String pressed = runOrCreate.getPressedButtonLabel();
//			if(pressed == null || pressed.equals(cancelButtonLabel))
//				return;
//			
			ChartAnswers answers = null;
////			if(pressed.equals(runButtonLabel))
////			{
////				answers = chooseAndLoad();
////			}
////			if(pressed.equals(createChartButtonLabel))
////			{
				answers = createAndSave();
////			}

			if(answers == null)
				return;
			
			runChart(answers);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			mainWindow.notifyDlgBeep("UnexpectedError");
		}
	}
	
	private ChartAnswers createAndSave()
	{
		CreateChartDialog dialog = new CreateChartDialog(getMainWindow());
		Utilities.packAndCenterWindow(dialog);
		dialog.setVisible(true);
		if(!dialog.getResult())
			return null;
		
		return dialog.getAnswers();
	}

	private void runChart(ChartAnswers answers)
	{
		try
		{
			SearchTreeNode searchTree = getMainWindow().askUserForSearchCriteria();
			if(searchTree == null)
				return;
			
			MiniFieldSpec fieldToCount = answers.getFieldToCount();
			MiniFieldSpec[] extraSpecs = new MiniFieldSpec[] { fieldToCount };
			SortableBulletinList sortableList = doSearch(searchTree, extraSpecs, new MiniFieldSpec[]{}, "ReportSearchProgress");
			if(sortableList == null)
				return;

			HashMap<String, Integer> counts = extractBulletinCounts(fieldToCount, sortableList);


			// TODO: Use or delete these
//			ChartRenderingInfo info = new ChartRenderingInfo();
//			EntityCollection entities = new StandardEntityCollection();
			
//			JFreeChart bar3dChart = create3DBarChart(counts, labelText);
			
			JFreeChart chart = createChart(answers, fieldToCount, counts);
			
			chart.removeSubtitle(new DateTitle());
			
			UiChartPreviewDlg preview = new UiChartPreviewDlg(getMainWindow(), chart);
			preview.setVisible(true);		
			if(preview.wasCancelButtonPressed())
				return;			
			boolean sendToDisk = preview.wantsPrintToDisk();
			
			boolean didPrint = false;
			if(sendToDisk)
				didPrint = printToDisk(chart);
			else
				didPrint = printToPrinter(chart);
				
			if(didPrint)
				mainWindow.notifyDlg("ChartCompleted");
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			getMainWindow().notifyDlg("ChartUnknownError");
		}
	}

	private JFreeChart createChart(ChartAnswers answers,
			MiniFieldSpec fieldToCount, HashMap<String, Integer> counts)
			throws Exception, TokenInvalidException
	{
		String selectedFieldLabel = fieldToCount.getLabel();
		if(selectedFieldLabel.equals(""))
			selectedFieldLabel = getLocalization().getFieldLabel(fieldToCount.getTag());

		JFreeChart chart = createRawChart(answers, counts, selectedFieldLabel);
		new MartusChartTheme().apply(chart);
		TextTitle subtitle = new TextTitle(answers.getSubtitle());
		subtitle.setFont(FontHandler.getDefaultFont());
		chart.addSubtitle(subtitle);
		chart.addSubtitle(createLegend(chart));
		
		String today = getLocalization().formatDateTime(new Date().getTime());
		String chartCreatedOnLabel = getLocalization().getFieldLabel("ChartCreatedOn");
		chartCreatedOnLabel = TokenReplacement.replaceToken(chartCreatedOnLabel, "#Date#", today);
		chart.addSubtitle(new ShortTextTitle(chartCreatedOnLabel));
		return chart;
	}

	private LegendTitle createLegend(JFreeChart chart)
	{
        LegendTitle legend = new LegendTitle(chart.getPlot());
        legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
        legend.setFrame(new LineBorder());
        legend.setBackgroundPaint(Color.white);
        legend.setPosition(RectangleEdge.BOTTOM);
        legend.addChangeListener(chart);
		legend.setItemFont(FontHandler.getDefaultFont());
        return legend;
	}

	private JFreeChart createRawChart(ChartAnswers answers,
			HashMap<String, Integer> counts, String selectedFieldLabel) throws Exception
	{
		if(answers.isBarChart())
			return createBarChart(counts, selectedFieldLabel);
		if(answers.isLineChart())
			return createCumulativeLineChart(counts, selectedFieldLabel);
		if(answers.is3DBarChart())
			return create3DBarChart(counts, selectedFieldLabel);
		if(answers.isPieChart())
			return createPieChart(counts, selectedFieldLabel);
		
		throw new RuntimeException("Unsupported chart type: " + answers.getChartType());
	}

	private HashMap<String, Integer> extractBulletinCounts(MiniFieldSpec selectedSpec, SortableBulletinList sortableList)
	{
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		
		UniversalId[] uids = sortableList.getUniversalIds();
		for (UniversalId uid : uids)
		{
			Bulletin b = getStore().getBulletinRevision(uid);
			SafeReadableBulletin srb = new SafeReadableBulletin(b, getLocalization());
			MartusField selectedField = srb.getPossiblyNestedField(selectedSpec);
			MartusField fieldToCount = selectedField;
			
			int relevantLevel = 0;
			if(selectedField.getType().isDropdown())
			{
				DropDownFieldSpec dropDownFieldSpec = (DropDownFieldSpec)selectedField.getFieldSpec();
				if(dropDownFieldSpec.hasReusableCodes())
				{
					String thisLevelCode = dropDownFieldSpec.getReusableChoicesCodes()[0];
					String[] allLevelCodes = selectedSpec.getReusableChoicesCodes();
					relevantLevel = new Vector(Arrays.asList(allLevelCodes)).indexOf(thisLevelCode);
					
					MartusField topLevelField = srb.getPossiblyNestedField(selectedSpec.getTopLevelTag());
					fieldToCount = topLevelField;
				}
			}
			
			String[] data = getSortableHumanReadableData(fieldToCount);
			String value = "";
			boolean hasAnyData = false;
			for (int level = 0; level < data.length && level <= relevantLevel; ++level)
			{
				String dataForLevel = data[level].trim();
				if(dataForLevel.length() > 0)
					hasAnyData = true;
				if(level > 0)
					value += " / ";
				value += dataForLevel;
			}
			if(!hasAnyData)
				value = "";
			
			Integer oldCount = counts.get(value);
			if(oldCount == null)
				oldCount = 0;
			int newCount = oldCount + 1;
			counts.put(value, newCount);
		}
		return counts;
	}

	public String[] getSortableHumanReadableData(MartusField fieldToCount)
	{
		MartusLocalization localization = getLocalization();
		if(fieldToCount.getType().isDate())
			localization.setMdyOrder(DATE_FORMAT_SORTABLE_YMD);
		return fieldToCount.getHumanReadableData(localization);
	}

	private boolean printToDisk(JFreeChart chart) throws IOException
	{
		File destFile = chooseDestinationFile();
		if(destFile == null)
			return false;

		int CHART_WIDTH_IN_PIXELS = 800;
		int CHART_HEIGHT_IN_PIXELS = 600;
		ChartUtilities.saveChartAsJPEG(destFile, chart, CHART_WIDTH_IN_PIXELS, CHART_HEIGHT_IN_PIXELS);
		return true;
	}

	File chooseDestinationFile()
	{
		String defaultFilename = getLocalization().getFieldLabel("DefaultPrintChartToDiskFileName");
		FormatFilter jpegFilter = new JPEGFilter();
		File destination = mainWindow.showFileSaveDialog("PrintToFile", defaultFilename, jpegFilter);
		return destination;
	}
	
	class JPEGFilter extends FormatFilter
	{
		@Override
		public String getExtension()
		{
			return JPEG_EXTENSION;
		}
		
		@Override
		public String[] getExtensions()
		{
			return new String[] {JPEG_EXTENSION, JPG_EXTENSION};
		}

		@Override
		public String getDescription()
		{
			return getLocalization().getFieldLabel("JPEGFileFilter");
		}
		
	}
	
	private boolean printToPrinter(JFreeChart chart) throws PrinterException
	{
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(new PrintableChart(chart));
		HashPrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
		removeJavaLogoFromTitle(attributes);  
		if(!printJob.printDialog(attributes))
			return false;
		
		printJob.print(attributes);
		return true;
	}

	private void removeJavaLogoFromTitle(HashPrintRequestAttributeSet attributes)
	{
		attributes.add(javax.print.attribute.standard.DialogTypeSelection.NATIVE);	
		Frame f = new Frame();
		attributes.add(new sun.print.DialogOwner(f));
	}
	
	class PrintableChart implements Printable
	{
		public PrintableChart(JFreeChart chartToWrap)
		{
			chart = chartToWrap;
		}

		public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException
		{
			if(pageIndex != 0)
				return Printable.NO_SUCH_PAGE;
			
			JComponent viewer = createPrintableComponent();

			// for faster printing, turn off double buffering
			PrintUtilities.disableDoubleBuffering(viewer);
			Graphics2D g2 = PrintUtilities.getTranslatedGraphics(graphics, pageFormat, 0, viewer);
			viewer.paint(g2); // repaint the page for printing
			PrintUtilities.enableDoubleBuffering(viewer);

			return Printable.PAGE_EXISTS;
		}
		
		private JComponent createPrintableComponent()
		{
			JLabel viewer = UiChartPreviewDlg.createChartComponent(chart);
			ActionPrint.setReasonableSize(viewer);
			return viewer;
		}
		

		
		private JFreeChart chart;
	}

	private JFreeChart createBarChart(HashMap<String, Integer> counts, String selectedFieldLabel) throws Exception
	{
		DefaultCategoryDataset dataset = createBarChartDataset(counts);

		boolean showLegend = false;
		boolean showTooltips = true;
		boolean showUrls = false;
		JFreeChart barChart = ChartFactory.createBarChart(
			getChartTitle(selectedFieldLabel), selectedFieldLabel, getYAxisTitle(), 
			dataset, PlotOrientation.VERTICAL,
			showLegend, showTooltips, showUrls);
		
		configureBarChartPlot(barChart);
		
		return barChart;
	}

	private JFreeChart createCumulativeLineChart(HashMap<String, Integer> counts, String selectedFieldLabel) throws Exception
	{
		DefaultCategoryDataset dataset = createCumulativeChartDataset(counts);

		boolean showLegend = false;
		boolean showTooltips = true;
		boolean showUrls = false;
		JFreeChart lineChart = ChartFactory.createLineChart(
			getChartTitle(selectedFieldLabel), selectedFieldLabel, getYAxisTitle(), 
			dataset, PlotOrientation.VERTICAL,
			showLegend, showTooltips, showUrls);
		
		configureBarChartPlot(lineChart);
		
		return lineChart;
	}

	private JFreeChart create3DBarChart(HashMap<String, Integer> counts, String selectedFieldLabel) throws Exception
	{
		DefaultCategoryDataset dataset = createBarChartDataset(counts);

		boolean showLegend = false;
		boolean showTooltips = true;
		boolean showUrls = false;
		JFreeChart barChart = ChartFactory.createBarChart3D(
			getChartTitle(selectedFieldLabel), selectedFieldLabel, getYAxisTitle(), 
			dataset, PlotOrientation.VERTICAL,
			showLegend, showTooltips, showUrls);
		
		configureBarChartPlot(barChart);
		
		return barChart;
	}

	private String getYAxisTitle()
	{
		return getLocalization().getFieldLabel("ChartYAxisTitle");
	}

	private DefaultCategoryDataset createBarChartDataset(HashMap<String, Integer> counts)
	{
		String seriesTitle = getLocalization().getFieldLabel("ChartSeriesTitle");
		seriesTitle = fontHelper.getDisplayable(seriesTitle);
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		Vector<String> keys = new Vector<String>(counts.keySet());
		Collections.sort(keys, new SaneCollator(getLocalization().getCurrentLanguageCode()));
		for (String value : keys)
		{
			Integer count = counts.get(value);
			if(value.length() == 0)
				value = getLocalization().getFieldLabel("ChartItemLabelBlank");
			else
				value = fontHelper.getDisplayable(value);
			dataset.addValue(count, seriesTitle, value);
		}
		return dataset;
	}
	
	private DefaultCategoryDataset createCumulativeChartDataset(HashMap<String, Integer> counts)
	{
		String seriesTitle = getLocalization().getFieldLabel("ChartSeriesTitle");
		seriesTitle = fontHelper.getDisplayable(seriesTitle);
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		Vector<String> keys = new Vector<String>(counts.keySet());
		Collections.sort(keys, new SaneCollator(getLocalization().getCurrentLanguageCode()));
		Integer totalCount = 0;
		int tickCount = 0;
		int skipElementsModulous = getSkipModulous(keys.size());
		 
		for (String value : keys)
		{
			totalCount += counts.get(value);
			if(value.length() == 0)
				value = getLocalization().getFieldLabel("ChartItemLabelBlank");
			else
				value = fontHelper.getDisplayable(value);
			if(shouldDisplayElement(tickCount, skipElementsModulous))
				dataset.addValue(totalCount, seriesTitle, value);
			++tickCount;
		}
		return dataset;
	}

	private boolean shouldDisplayElement(int tickCount, int skipElementsModulous)
	{
		return tickCount % skipElementsModulous == 0;
	}

	private int getSkipModulous(int size)
	{
		if(size < 25)
			return 1;
		int maxItemsAccrossAxis = size / 25;
		return maxItemsAccrossAxis + 1;
	}

	private void configureBarChartPlot(JFreeChart barChart)
	{
		CategoryPlot plot = (CategoryPlot) barChart.getPlot();
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		TickUnitSource units = NumberAxis.createIntegerTickUnits();
		rangeAxis.setStandardTickUnits(units);
		
		CategoryAxis domainAxis = plot.getDomainAxis();
		CategoryLabelPositions newPositions = CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 2.0);
		domainAxis.setCategoryLabelPositions(newPositions);

		barChart.addSubtitle(new TextTitle(getLocalization().getFieldLabel("ChartSelectedBulletinsDisclaimerBar"), 
				FontHandler.getDefaultFont(), TextTitle.DEFAULT_TEXT_PAINT, RectangleEdge.BOTTOM,
				TextTitle.DEFAULT_HORIZONTAL_ALIGNMENT, TextTitle.DEFAULT_VERTICAL_ALIGNMENT, 
				TextTitle.DEFAULT_PADDING));
	}

	private JFreeChart createPieChart(HashMap<String, Integer> counts, String selectedFieldLabel) throws Exception
	{
		DefaultPieDataset pieDataset = createPieDataset(counts);
		
		JFreeChart pieChart = ChartFactory.createPieChart(
		        getChartTitle(selectedFieldLabel),   // Title
		        pieDataset,           // Dataset
		        false,                 // Show legend
		        true,					// tooltips
		        new Locale(getLocalization().getCurrentLanguageCode())
		        );
		
		pieChart.addSubtitle(new TextTitle(getLocalization().getFieldLabel("ChartSelectedBulletinsDisclaimerPie"), 
				TextTitle.DEFAULT_FONT, TextTitle.DEFAULT_TEXT_PAINT, RectangleEdge.BOTTOM,
				TextTitle.DEFAULT_HORIZONTAL_ALIGNMENT, TextTitle.DEFAULT_VERTICAL_ALIGNMENT, 
				TextTitle.DEFAULT_PADDING));

		PiePlot piePlot = (PiePlot) pieChart.getPlot();
		piePlot.setLabelGenerator(new MartusPieSectionLabelGenerator(getLocalization()));
		return pieChart;
	}
	
	public static class MartusPieSectionLabelGenerator extends AbstractPieItemLabelGenerator implements PieSectionLabelGenerator
	{
		public MartusPieSectionLabelGenerator(MartusLocalization localizationToUse)
		{
			super(getFormat(localizationToUse), NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance());
		}

		public static String getFormat(MartusLocalization localization)
		{
			String template = localization.getFieldLabel("ChartPieSliceLabel");
			Map tokenReplacement = new HashMap();
			tokenReplacement.put("#DataValue#", "{0}");
			tokenReplacement.put("#Count#", "{1}");
			tokenReplacement.put("#Percent#", "{2}");
			try
			{
				String result = TokenReplacement.replaceTokens(template, tokenReplacement);
				return result;
			} 
			catch (TokenInvalidException e)
			{
				MartusLogger.logException(e);
				throw new RuntimeException(e);
			}
			
		}
		
		@Override
		public String generateSectionLabel(PieDataset dataset, Comparable key)
		{
			return super.generateSectionLabel(dataset, key);
		}

		public AttributedString generateAttributedSectionLabel(
				PieDataset dataset, Comparable key)
		{
			// NOTE: Not required; safe to return null
			return null;
		}
		
	}

	private String getChartTitle(String selectedFieldLabel) throws TokenInvalidException
	{
		String title = getLocalization().getFieldLabel("ChartTitle");
		title = TokenReplacement.replaceToken(title, "#SelectedField#", selectedFieldLabel);
		return title;
	}

	private DefaultPieDataset createPieDataset(HashMap<String, Integer> counts)
	{
		DefaultPieDataset pieDataset = new DefaultPieDataset();
		Vector<String> keys = new Vector<String>(counts.keySet());
		Collections.sort(keys, new SaneCollator(getLocalization().getCurrentLanguageCode()));
		for (String value : keys)
		{
			Integer count = counts.get(value);
			if(value.length() == 0)
				value = getLocalization().getFieldLabel("ChartItemLabelBlank");
			else
				value = fontHelper.getDisplayable(value);
			pieDataset.setValue(value, count);
		}
		return pieDataset;
	}

	// FIXME: Enable or delete these not-yet-used methods
//	private JFreeChart createDateCountChart(HashMap<String, Integer> counts,
//			String labelText) throws IOException
//	{
//		TimeTableXYDataset dataset = new TimeTableXYDataset(); 
//		for (String value : counts.keySet())
//		{
//			MultiCalendar calendar = MultiCalendar.createFromIsoDateString(value);
//			TimePeriod timePeriod = new Day(calendar.getGregorianDay(), calendar.getGregorianMonth(), calendar.getGregorianYear());
//			dataset.add(timePeriod, counts.get(value), "Number of Martus bulletins by date entered");
//		}
//
//		JFreeChart chart = ChartFactory.createXYBarChart(
//		                     "Martus Bulletin Counts by " + labelText, // Title
//		                      labelText,              // X-Axis label
//		                      true,				// date axis
//		                      "Count",                 // Y-Axis label
//		                      dataset,         // Dataset
//		                      PlotOrientation.VERTICAL,
//		                      true,                     // Show legend
//		                      true,		// tooltips?
//		                      false		// urls
//		                     );
//		return chart;
//	}
//
	private final static String DATE_FORMAT_SORTABLE_YMD = "ymd";
	private final static String JPEG_EXTENSION = ".jpeg";
	private final static String JPG_EXTENSION = ".jpg";
	UiFontEncodingHelper fontHelper;
}
