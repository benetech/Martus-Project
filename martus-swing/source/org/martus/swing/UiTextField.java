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

import java.awt.Dimension;

import javax.swing.JTextField;


public class UiTextField extends JTextField
{
	public UiTextField()
	{
		super();
		initalize();
	}
	
	public UiTextField (int columns)
	{
		super(columns);
		initalize();
	}
	
	public UiTextField(String text)
	{
		super(text);
		initalize();
	}

	private void initalize()
	{
		setFont(FontHandler.getDefaultFont());
		setComponentOrientation(UiLanguageDirection.getComponentOrientation());
		setHorizontalAlignment(UiLanguageDirection.getHorizontalAlignment());
	}
	
	/* 
	 * NOTE: This is a horrible hack to work around the fact that a JTextArea
	 * seems to completely ignore any calls to setBorder or setMargin.
	 * We need to add some space to the bottom of the field to avoid Arabic
	 * (and even English) characters from being chopped off. 
	 * The number of pixels is arbitrary and may need to be adjusted.
	 */
	
	public Dimension getPreferredSize()
	{
		return Utilities.addCushionToHeightIfRequired(super.getPreferredSize(), EXTRA_PIXELS);
	}

	/* Another horible hack to deal with "tall" letters
	 * getMaximumSize() instead of getPreferredSize()
	 * is being called from Box's horizontal and vertical.
	 */
	
	public Dimension getMaximumSize()
	{
		return Utilities.addCushionToHeightIfRequired(super.getMaximumSize(), EXTRA_PIXELS);
	}

	final int EXTRA_PIXELS = 14;
}
