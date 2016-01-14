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

*/

package org.martus.client.swingui.fields;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.JComponent;

public class FocusManager extends FocusAdapter
{
	public FocusManager(UiField fieldToWatch)
	{
		field = fieldToWatch;
		listeners = new Vector();
	}
	
	public void addFocusListener(FocusListener listener)
	{
		listeners.add(listener);
	}
	
	public void addFocusableComponents()
	{
		JComponent[] focusableComponents = field.getFocusableComponents();
		for(int i = 0 ; i < focusableComponents.length; ++i)
			focusableComponents[i].addFocusListener(this);
	}
	
	public void focusGained(FocusEvent event) 
	{
		Rectangle rect = field.getComponent().getBounds();
		JComponent parent = (JComponent)field.getComponent().getParent();
		parent.scrollRectToVisible(rect);
		for(int i=0; i < listeners.size(); ++i)
			((FocusListener)listeners.get(i)).focusGained(event);
	}

	public void focusLost(FocusEvent event)
	{
		Component nowHasFocus = event.getOppositeComponent();
		while(true)
		{
			if(nowHasFocus == null)
			{
				//System.out.println("FocusManager Really lost focus from " + field);
				for(int i=0; i < listeners.size(); ++i)
				{
					FocusListener listener = ((FocusListener)listeners.get(i));
					//System.out.println("Notifying " + listener);
					listener.focusLost(event);
				}
				break;
			}
			if(nowHasFocus.equals(field.getComponent()))
			{
				break;
			}
			nowHasFocus = nowHasFocus.getParent();
		}
	}
	
	UiField field;
	Vector listeners;
}
