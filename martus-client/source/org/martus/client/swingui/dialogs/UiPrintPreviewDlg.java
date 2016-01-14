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
package org.martus.client.swingui.dialogs;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.martus.client.reports.ReportOutput;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionPrint;
import org.martus.swing.UiScrollPane;

public class UiPrintPreviewDlg extends UiPreviewDlg
{
	public UiPrintPreviewDlg(UiMainWindow mainWindowToUse, ReportOutput output)
	{
		super(mainWindowToUse);
		initialize(createScrollablePreview(output));	
	}
	
	private JComponent createScrollablePreview(ReportOutput output)
	{
		String html = output.getPrintableDocument();
		if (mainWindow.getUseZawgyiFont())
			html = html.replaceAll("<table", "<table style=\"font-family: Zawgyi-One, Sans-Serif; \" ");
		JComponent previewText = ActionPrint.getHtmlViewableComponent(html);
		JComponent scrollablePreview = new UiScrollPane(previewText);
		scrollablePreview.setBorder(new EmptyBorder(5,5,5,5));
		return scrollablePreview;
	}
}
