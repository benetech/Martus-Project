/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;

public class UiVBox extends Box
{
	public UiVBox()
	{
		super(BoxLayout.Y_AXIS);
		setComponentOrientation(UiLanguageDirection.getComponentOrientation());		
	}

	public Component add(Component componentToAddRespectingOrientation)
	{
		return addRespectingOrientation(new Component[] {componentToAddRespectingOrientation});
	}

	public Component add(Component[] componentsToAddRespectingOrientation)
	{
		return addRespectingOrientation(componentsToAddRespectingOrientation);
	}

	public Component addCentered(Component component)
	{
		return super.add(component);
	}

	public Component addSpace()
	{
		return addCentered(new UiLabel(" "));
	}
	private Component addRespectingOrientation(Component[] component)
	{
		Box componentBox = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(componentBox, component);
		Box aligningBox = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(aligningBox, new Component[] {componentBox,Box.createHorizontalGlue()});
		return super.add(aligningBox);
	}
}
