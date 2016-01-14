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

To the extent this copyrighted software code is used in the 
Miradi project, it is subject to a royalty-free license to 
members of the Conservation Measures Partnership when 
used with the Miradi software as specified in the agreement 
between Benetech and WCS dated 5/1/05.
*/



/* used with the permission of Richard Blanchard */
package org.martus.swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JComponent;
public class JComponentVista extends Vista implements Printable
{
	private double mScaleX;
	private double mScaleY;

	/**
	 * The Swing component to print.
	 */
	private JComponent mComponent;

	/**
	 * Create a Pageable that can print a
	 * Swing JComponent over multiple pages.
	 *
	 * @param c The swing JComponent to be printed.
	 *
	 * @param format The size of the pages over which
	 * the componenent will be printed.
	 */
	public JComponentVista(JComponent c, PageFormat format)
	{
		setPageFormat(format);
		setPrintable(this);
		setComponent(c);
		/* Tell the Vista we subclassed the size of the canvas.
		 */
		Rectangle componentBounds = c.getBounds(null);
		setSize(componentBounds.width, componentBounds.height);
		setScale(1, 1);
	}

	protected void setComponent(JComponent c)
	{
		mComponent = c;
	}

	protected void setScale(double scaleX, double scaleY)
	{
		mScaleX = scaleX;
		mScaleY = scaleY;
	}

	public void scaleToFitX()
	{
		PageFormat format = getPageFormat();
		Rectangle componentBounds = mComponent.getBounds(null);
		double scaleX = format.getImageableWidth() /componentBounds.width;
		double scaleY = scaleX;
		if (scaleX < 1)
		{
			setSize( (float) format.getImageableWidth(),
			(float) (componentBounds.height * scaleY));
			setScale(scaleX, scaleY);
		}
	}

	public void scaleToFitY()
	{
				PageFormat format = getPageFormat();
		Rectangle componentBounds = mComponent.getBounds(null);
		double scaleY = format.getImageableHeight() /componentBounds.height;
		double scaleX = scaleY;
		if (scaleY < 1)
		{
			setSize( (float) (componentBounds.width * scaleX),(float) format.getImageableHeight());
			setScale(scaleX, scaleY);
		}
	}

	public void scaleToFit(boolean useSymmetricScaling)
	{
				PageFormat format = getPageFormat();
		Rectangle componentBounds = mComponent.getBounds(null);
		double scaleX = format.getImageableWidth() /componentBounds.width;
		double scaleY = format.getImageableHeight() /componentBounds.height;
		System.out.println("Scale: " + scaleX + " " + scaleY);
		if (scaleX < 1 || scaleY < 1) {
			if (useSymmetricScaling) {
				if (scaleX < scaleY) {
					scaleY = scaleX;
				} else {
					scaleX = scaleY;
				}
			}
			setSize( (float) (componentBounds.width * scaleX), (float) (componentBounds.height * scaleY) );
			setScale(scaleX, scaleY);
		}
	}

	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException
	{
		Graphics2D g2 = (Graphics2D) graphics;
		g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		Rectangle componentBounds = mComponent.getBounds(null);
		g2.translate(-componentBounds.x, -componentBounds.y);
		g2.scale(mScaleX, mScaleY);
		boolean wasBuffered = mComponent.isDoubleBuffered();
		mComponent.print(g2);
		mComponent.setDoubleBuffered(wasBuffered);
		return PAGE_EXISTS;
	}
}
