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

To the extent this copyrighted software code is used in the 
Miradi project, it is subject to a royalty-free license to 
members of the Conservation Measures Partnership when 
used with the Miradi software as specified in the agreement 
between Benetech and WCS dated 5/1/05.

 * This file is based on one downloaded on 2006-07-26 from:
 *   http://www.developerdotstar.com/community/node/124
 * Based on the text on that page, the author (Rob MacGrogan) 
 * clearly expects and permits this code to be reused without any limitations.
 * I found this email address: robertmacgrogan@yahoo.com
 *
 * MacGrogan says the code was based on source from two other sources:
 * 1. The tutorial at www.apl.jhu.edu (still available on 2006-07-26), 
 * which has this copyright:
 *   (C) 1999 Marty Hall. All source code freely available for unrestricted use.
 *
 * 2. An anonymous forum post (the domain he lists is not valid)
 *
 * There is little enough code here that it could be re-invented from scratch
 * pretty easily, if necessary. 
 * 
 * Here is the copyright comment as it appears at developerdotstar:
 * ------------------------------------------------------------------------------------
 * Copied from this tutorial:
 *
 * http://www.apl.jhu.edu/~hall/java/Swing-Tutorial/Swing-Tutorial-Printing.html
 *
 * And also from a post on the forums at java.swing.com. My apologies that do not have
 * a link to that post, by my hat goes off to the poster because he/she figured out the
 * sticky problem of paging properly when printing a Swing component.
 */

package org.martus.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.swing.RepaintManager;

public class PrintUtilities implements Printable
{
	private Component componentToBePrinted;
	
	public static void printComponent(Component c)
	{
		new PrintUtilities(c).print();
	}
	
	public PrintUtilities(Component componentToBePrinted)
	{
		this.componentToBePrinted = componentToBePrinted;
	}
	
	public void print()
	{
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this);
		HashPrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
		if (printJob.printDialog(attributes))
			try
			{
				printJob.print(attributes);
			} 
			catch (PrinterException pe)
			{
				System.out.println("Error printing: " + pe);
			}
	}
	
	public int print(Graphics g, PageFormat pf, int pageIndex)
	{
		return print(g, pf, pageIndex, componentToBePrinted);
	}
	
	public static int print(Graphics g, PageFormat pf, int pageIndex, Component component)
	{
		// make sure not print empty pages
		if (pageIndex >= computePageCountWithinComponent(pf, component))
			return NO_SUCH_PAGE;

		// for faster printing, turn off double buffering
		disableDoubleBuffering(component);
		Graphics2D g2 = getTranslatedGraphics(g, pf, pageIndex, component);
		component.paint(g2);
		enableDoubleBuffering(component);
		return Printable.PAGE_EXISTS;
	}

	public static int computePageCountWithinComponent(PageFormat pf, Component component)
	{
		double scale = getScale(pf, component);
		int componentHeight = component.getHeight();
		double imageableHeight = pf.getImageableHeight();
		return (int) Math.ceil(scale * componentHeight / imageableHeight);
	}

	public static Graphics2D getTranslatedGraphics(Graphics g, PageFormat pf, int pageIndex, Component component)
	{
		Graphics2D g2 = (Graphics2D) g;
		// shift Graphic to line up with beginning of print-imageable region
		g2.translate(pf.getImageableX(), pf.getImageableY());
		// shift Graphic to line up with beginning of next page to print
		g2.translate(0f, -pageIndex * pf.getImageableHeight());
		// scale the page so the width fits...
		if(needsScaling(pf, component))
		{
			double scale = getScale(pf, component);
			g2.scale(scale, scale);
		}
		return g2;
	}

	private static double getScale(PageFormat pf, Component component)
	{
		if(needsScaling(pf, component))
			return pf.getImageableWidth() / component.getWidth();
		return 1.0;
	}

	private static boolean needsScaling(PageFormat pf, Component component)
	{
		return pf.getImageableWidth() < component.getWidth();
	}
	
	public static void disableDoubleBuffering(Component c)
	{
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(false);
	}
	
	public static void enableDoubleBuffering(Component c)
	{
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(true);
	}
}
