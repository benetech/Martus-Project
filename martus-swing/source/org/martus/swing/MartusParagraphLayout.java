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



/* used with the permission of Jerry Huxtable */
package org.martus.swing;


import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

import com.jhlabs.awt.ConstraintLayout;

/*
 * NOTE: The Martus team added several features, including 
 * requestedFirstColumnWidth and isFirstFieldOutdented
 */
public class MartusParagraphLayout extends ConstraintLayout {

	public final static Integer NEW_PARAGRAPH = new Integer(0x01);
	public final static Integer NEW_PARAGRAPH_TOP = new Integer(0x02);
	public final static Integer NEW_LINE = new Integer(0x03);

	protected int hGapMajor, vGapMajor;
	protected int hGapMinor, vGapMinor;
	protected int rows;
	protected int colWidth1;
	protected int colWidth2;
	protected int requestedFirstColumnWidth;
	protected boolean isFirstFieldOutdented;

	public MartusParagraphLayout() {
		this(10, 10, 12, 11, 4, 4);
	}

	public MartusParagraphLayout(int hMargin, int vMargin, int hGapMajor, int vGapMajor, int hGapMinor, int vGapMinor) {
		this.hMargin = hMargin;
		this.vMargin = vMargin;
		this.hGapMajor = hGapMajor;
		this.vGapMajor = vGapMajor;
		this.hGapMinor = hGapMinor;
		this.vGapMinor = vGapMinor;
	}

	public void outdentFirstField()	{
		isFirstFieldOutdented = true;
	}

	public void setFirstColumnWidth(int firstColumnWidth)	{
		requestedFirstColumnWidth = firstColumnWidth;
	}

	public int getFirstColumnMaxWidth(Container target) {
		int maxWidth = 0;
		int count = target.getComponentCount();
		for (int i = 0; i < count; i++) {
			Component c = target.getComponent(i);
			if (includeComponent(c)) {
				Integer n = (Integer)getConstraint(c);
				if (i == 0 || n == NEW_PARAGRAPH || n == NEW_PARAGRAPH_TOP) {
					Dimension d = getComponentSize(c, PREFERRED);
					maxWidth = Math.max(maxWidth, d.width);
				}
			}
		}

		return maxWidth;
	}

	public void measureLayout(Container target, Dimension dimension, int type)
	{
		int count = target.getComponentCount();
		if (count > 0)
		{
			int y = 0;
			int rowHeight = 0;
			int colWidth = 0;
			boolean lastWasParagraph = false;

			Dimension[] sizes = new Dimension[count];

			// First pass: work out the column widths and row heights
			for (int i = 0; i < count; i++) {
				Component c = target.getComponent(i);
				if (includeComponent(c)) {
					Dimension d = getComponentSize(c, type);
					int w = d.width;
					int h = d.height;
					sizes[i] = d;
					Integer n = (Integer)getConstraint(c);

					if (i == 0 || n == NEW_PARAGRAPH || n == NEW_PARAGRAPH_TOP) {
						if (i != 0)
							y += rowHeight+vGapMajor;
						colWidth1 = Math.max(colWidth1, w);
						colWidth = 0;
						rowHeight = 0;
						lastWasParagraph = true;
					} else if (n == NEW_LINE || lastWasParagraph) {
						if (!lastWasParagraph)
							y += rowHeight+vGapMinor;
						colWidth = w;
						colWidth2 = Math.max(colWidth2, colWidth);
						if (!lastWasParagraph)
							rowHeight = 0;
						lastWasParagraph = false;
					} else {
						colWidth += w+hGapMinor;
						colWidth2 = Math.max(colWidth2, colWidth);
						lastWasParagraph = false;
					}
					rowHeight = Math.max(h, rowHeight);
				}
			}

			if(requestedFirstColumnWidth > 0)
			{
				colWidth1 = requestedFirstColumnWidth;
			}

			if (dimension != null) {
				dimension.width = colWidth1 + hGapMajor + colWidth2;
				dimension.height = y + rowHeight;
			} else {
				y = 0;
				lastWasParagraph = false;
				int start = 0;
				Integer paragraphType = NEW_PARAGRAPH;

				boolean firstLine = true;
				for (int i = 0; i < count; i++) {
					Component c = target.getComponent(i);
					if (includeComponent(c)) {
						Dimension d = sizes[i];
						int h = d.height;
						Integer n = (Integer)getConstraint(c);

						if (i == 0 || n == NEW_PARAGRAPH || n == NEW_PARAGRAPH_TOP) {
							paragraphType = n;
							if (i != 0)
								layoutRow(target, sizes, start, i-1, y, rowHeight, firstLine, type, paragraphType);
							start = i;
							firstLine = true;
							if (i != 0)
								y += rowHeight+vGapMajor;
							rowHeight = 0;
							lastWasParagraph = true;
						} else if (n == NEW_LINE || lastWasParagraph) {
							if (!lastWasParagraph) {
								layoutRow(target, sizes, start, count-1, y, rowHeight, firstLine, type, paragraphType);
								start = i;
								firstLine = false;
							}
							if (!lastWasParagraph)
								y += rowHeight+vGapMinor;
							if (!lastWasParagraph)
								rowHeight = 0;
							lastWasParagraph = false;
						} else
							lastWasParagraph = false;
						rowHeight = Math.max(h, rowHeight);
					}
				}
				layoutRow(target, sizes, start, count-1, y, rowHeight, firstLine, type, paragraphType);
			}
		}

	}

	protected void layoutRow(Container target, Dimension[] sizes, int start, int end, int y, int rowHeight, boolean paragraph, int type, Integer paragraphType) {
		int x = 0;
		Insets insets = target.getInsets();
		for (int i = start; i <= end; i++) {
			Component c = target.getComponent(i);
			if (includeComponent(c)) {
				Dimension d = sizes[i];
				int w = d.width;
				int h = d.height;

				if (i > 0 || !isFirstFieldOutdented) {
					if (i == start) {
						if (paragraph)
							x = colWidth1-w;
						else
							x = colWidth1 + hGapMajor;
					} else if (paragraph && i == start+1) {
						x = colWidth1 + hGapMajor;
					}
				}
				int yOffset = paragraphType == NEW_PARAGRAPH_TOP ? 0 : (rowHeight-h)/2;
				c.setBounds(insets.left+hMargin+x, insets.top+vMargin+y+yOffset, w, h);
				x += w + hGapMinor;
			}
		}
	}
}
