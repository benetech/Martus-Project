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
package org.martus.client.swingui.dialogs;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.JFreeChart;
import org.martus.client.swingui.UiMainWindow;
import org.martus.swing.UiScrollPane;

public class UiChartPreviewDlg extends UiPreviewDlg
{
	public UiChartPreviewDlg(UiMainWindow mainWindowToUse, JFreeChart chart)
	{
		super(mainWindowToUse);
		initialize(createScrollablePreview(chart));	
	}
	
	private JComponent createScrollablePreview(JFreeChart chart)
	{
		JLabel label = createChartComponent(chart);
		JComponent scrollablePreview = new UiScrollPane(label);
		scrollablePreview.setBorder(new EmptyBorder(5,5,5,5));
		return scrollablePreview;
	}

	public static JLabel createChartComponent(JFreeChart chart)
	{
		BufferedImage image = chart.createBufferedImage(800, 600);
		ImageIcon imageIcon = new ImageIcon(image);
		JLabel label = new JLabel(imageIcon);
		return label;
	}
}
