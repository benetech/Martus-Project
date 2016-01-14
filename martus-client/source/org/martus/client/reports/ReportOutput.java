/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Vector;

import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.actions.ActionPrint;
import org.martus.swing.FontHandler;
import org.martus.swing.PrintUtilities;
import org.martus.swing.UiLabel;

public class ReportOutput extends Writer implements Printable
{
	public ReportOutput()
	{
		pages = new Vector();
		currentPage = new StringWriter();
		documentStart = "";
		documentEnd = "";
		fontHelper = new UiFontEncodingHelper(FontHandler.isDoZawgyiConversion());
	}
	
	public void close() throws IOException
	{
		pages.add(currentPage.toString());
		currentPage = null;
	}

	public void flush() throws IOException
	{
	}

	public void write(char[] cbuf, int off, int len) throws IOException
	{
		currentPage.write(cbuf, off, len);
	}
	
	public void setDocumentStart(String text)
	{
		documentStart = text;
	}
	
	public String getDocumentStart()
	{
		return documentStart;
	}
	
	public void setDocumentEnd(String text)
	{
		documentEnd = text;
	}
	
	public String getDocumentEnd()
	{
		return documentEnd;
	}
	
	public void setFakePageBreak(String text)
	{
		fakePageBreak = text;
	}
	
	public String getFakePageBreak()
	{
		return fakePageBreak;
	}
	
	public boolean isPageEmpty()
	{
		return (currentPage.toString().length() == 0);
	}
	
	public void startNewPage()
	{
		pages.add(currentPage.toString());
		currentPage = new StringWriter();
	}
	
	public int getPageCount()
	{
		return pages.size();
	}
	
	public String getPageText(int pageIndex)
	{
		return (String)pages.get(pageIndex);
	}
	
	public String getPrintableDocument()
	{
		StringBuffer text = new StringBuffer();
		text.append(getDocumentStart());
		for(int page = 0; page < getPageCount(); ++page)
		{
			text.append(getPageText(page));
			text.append(getFakePageBreak());
		}
		text.append(getDocumentEnd());
		return fontHelper.getDisplayable(text.toString());
	}
	
	public String getPrintablePage(int page)
	{
		StringBuffer text = new StringBuffer();
		text.append(getDocumentStart());
		text.append(getPageText(page));
		text.append(getDocumentEnd());
		return text.toString();
	}
	
	public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException
	{
		if(pagesPerStoredPage == null)
			pagesPerStoredPage = computeStartingPages(pf);
		
		int printedPage = 0;
		for(int page = 0; page < getPageCount(); ++page)
		{
			int pagesForThisStoredPage = pagesPerStoredPage[page];
			if(printedPage + pagesForThisStoredPage > pageIndex)
			{
				int pageWithinComponent = pageIndex - printedPage;
				printThisPage(g, pf, page, pageWithinComponent);
				return Printable.PAGE_EXISTS;
			}
			
			printedPage += pagesForThisStoredPage;
		}
		return Printable.NO_SUCH_PAGE;
	}

	void printThisPage(Graphics g, PageFormat pf, int storedPage, int pageWithinComponent)
	{
		String pageText = getPrintablePage(storedPage);
		UiLabel viewer = createPrintableComponent(pageText);

		// for faster printing, turn off double buffering
		PrintUtilities.disableDoubleBuffering(viewer);
		Graphics2D g2 = PrintUtilities.getTranslatedGraphics(g, pf, pageWithinComponent, viewer);
		viewer.paint(g2); // repaint the page for printing
		PrintUtilities.enableDoubleBuffering(viewer);
	}
	
	int[] computeStartingPages(PageFormat pf)
	{
		int[] pagesPer = new int[getPageCount()];
		for(int page = 0; page < getPageCount(); ++page)
		{
			String pageText = getPrintablePage(page);
			UiLabel viewer = createPrintableComponent(pageText);
			int pagesForThisStoredPage = PrintUtilities.computePageCountWithinComponent(pf, viewer);
			pagesPer[page] = pagesForThisStoredPage;
		}
		
		return pagesPer;
	}

	private UiLabel createPrintableComponent(String pageText)
	{
		UiLabel viewer = new UiLabel(pageText);
		ActionPrint.setReasonableSize(viewer);
		return viewer;
	}
	
	Vector pages;
	Writer currentPage;
	String documentStart;
	String documentEnd;
	String fakePageBreak;
	
	int[] pagesPerStoredPage;
	UiFontEncodingHelper fontHelper;
}