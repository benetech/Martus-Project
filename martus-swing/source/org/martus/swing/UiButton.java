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

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;


public class UiButton extends JButton
{
	public UiButton()
	{	
	}
	
	public UiButton(String text)
	{
		super(text);
	}

	public UiButton(Action action)
	{
		super(action);
	}
	
	public UiButton(Icon icon)
	{
		super(icon);
	}
	
	/* 
	 * This is copied from the horrible hack in JTextArea.
	 * I'm not sure if there is a better way, but somehow we 
	 * need to prevent Arabic letters from being chopped off 
	 * at the top and bottom.
	 * 
	 * Unfortunately, since my system didn't show the problem,
	 * I'm not sure this actually helped. Still, it seems like 
	 * a good idea to have a UiButton class, so it's not all 
	 * wasted, even if this method disappears. kbs.
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
