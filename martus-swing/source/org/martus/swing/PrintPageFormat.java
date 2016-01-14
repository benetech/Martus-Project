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

package org.martus.swing;

import java.awt.print.PageFormat;
import java.awt.print.Paper;

import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

public class PrintPageFormat extends PageFormat
{

	public void setFromAttributes(HashPrintRequestAttributeSet attributes)
	{
		boolean otherMediaSet = false;
		boolean paperSizeSet = false;
		final int FRACTIONS_INCH = 72;
		Paper paper = new Paper();
		Attribute all[] = attributes.toArray();
		for(int i=0; i < all.length; ++i)
		{
			if(all[i].getCategory().equals(MediaPrintableArea.class))
			{
				MediaPrintableArea area = (MediaPrintableArea)(all[i]);
				paper.setImageableArea(	area.getX(MediaPrintableArea.INCH) * FRACTIONS_INCH,
										area.getY(MediaPrintableArea.INCH) * FRACTIONS_INCH,
										area.getWidth(MediaPrintableArea.INCH) * FRACTIONS_INCH,
										area.getHeight(MediaPrintableArea.INCH) * FRACTIONS_INCH);
			}
			if(all[i].getCategory().equals(Media.class))
			{
				try
				{
					MediaSizeName mediaSizeName = (MediaSizeName)(all[i]);
					MediaSize size = MediaSize.getMediaSizeForName(mediaSizeName);
					paper.setSize(	size.getX(Size2DSyntax.INCH) * FRACTIONS_INCH,
									size.getY(Size2DSyntax.INCH) * FRACTIONS_INCH);
					paperSizeSet = true;
				} catch (RuntimeException e)
				{
					otherMediaSet = true;
					//Not a MediaSizeName
				}
			}
			if(all[i].getCategory().equals(OrientationRequested.class))
			{
				OrientationRequested orientation = (OrientationRequested)(all[i]);
				if(orientation.equals(OrientationRequested.LANDSCAPE))
					setOrientation(PageFormat.LANDSCAPE);
				if(orientation.equals(OrientationRequested.PORTRAIT))
					setOrientation(PageFormat.PORTRAIT);
				if(orientation.equals(OrientationRequested.REVERSE_LANDSCAPE))
					setOrientation(PageFormat.REVERSE_LANDSCAPE);
			}
		}
		setPaper(paper);
		if(otherMediaSet && !paperSizeSet)
			possiblePaperSizeAndTrayMismatch = true;
		else
			possiblePaperSizeAndTrayMismatch = false;
	}
	public boolean possiblePaperSizeAndTrayMismatch;
}