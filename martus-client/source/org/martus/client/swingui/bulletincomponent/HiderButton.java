/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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

package org.martus.client.swingui.bulletincomponent;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.border.BevelBorder;

import org.martus.client.core.MartusApp;
import org.martus.swing.UiButton;

class HiderButton extends UiButton implements ActionListener
{
	public HiderButton(MartusApp appToUse, String tagToUse, FieldHolder fieldToHide)
	{
		app = appToUse;
		tag = tagToUse;
		fieldHolder = fieldToHide;
		size = 11;
		
		//setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		setMargin(new Insets(0,0,0,0));
		setIconTextGap(0);
		setFocusPainted(false);
		setContentAreaFilled(false);
		addActionListener(this);
		
		forceState(app.isFieldExpanded(tag));
	}
	
	

	public Dimension getPreferredSize() 
	{
		return new Dimension(size, size);
	}

	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}
	
	public Dimension getMaximumSize()
	{
		return getPreferredSize();
	}

	public void actionPerformed(ActionEvent event) 
	{
		boolean shouldBeVisible = !fieldHolder.isShown();
		app.setFieldExpansionState(tag, shouldBeVisible);
		forceState(shouldBeVisible);
	}

	private void forceState(boolean newState) 
	{
		if(newState)
			fieldHolder.showField();
		else
			fieldHolder.hideField();
		refresh();
	}

	private void refresh() 
	{
		setIcon(getAppropriateIcon());
		Container topLevelAncestor = getTopLevelAncestor();
		if(topLevelAncestor != null)
			topLevelAncestor.validate();
	}
	
	public Icon getAppropriateIcon()
	{
		if(fieldHolder.isShown())
			return new MinusIcon(size);
		
		return new PlusIcon(size);
	}

	MartusApp app;
	String tag;
	FieldHolder fieldHolder;
	int size;
}