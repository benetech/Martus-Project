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

*/

package org.martus.client.swingui.fields;

import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.martus.client.core.BulletinLanguageChangeListener;
import org.martus.client.swingui.fields.UiEditableGrid.EnterAction;
import org.martus.client.swingui.fields.UiEditableGrid.ShiftTabAction;
import org.martus.client.swingui.fields.UiEditableGrid.SpaceAction;
import org.martus.client.swingui.fields.UiEditableGrid.TabAction;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.FieldSpec;

abstract public class UiField
{
	public UiField(MiniLocalization localizationToUse)
	{
		focusManager = new FocusManager(this);
		localization = localizationToUse;
	}
	
	public void initalize()
	{
		focusManager.addFocusableComponents();
	}
	
	public void validate(FieldSpec spec, String labelToShow) throws DataInvalidException 
	{
		spec.validate(labelToShow, getText(), getLocalization());
	}
	
	public MiniLocalization getLocalization()
	{
		return localization;
	}

	public void setListener(ChangeListener listener)
	{
	}
	
	public void setBulletinLanguageListener(BulletinLanguageChangeListener listener)
	{
	}
	
	public void setActions(EnterAction enterActionToUse, SpaceAction spaceActionToUse, TabAction tabActionToUse, ShiftTabAction shiftTabActionToUse)
	{
		
	}

	public void addFocusListener(FocusListener listener)
	{
		focusManager.addFocusListener(listener);
	}
	
	public void updateSpellChecker(String bulletinLanguageCode)
	{
		// NOTE: Default behavior is no spell checking
	}

	abstract public JComponent getComponent();
	abstract public JComponent[] getFocusableComponents();
	abstract public String getText();
	abstract public void setText(String newText);

	FocusManager focusManager;
	private MiniLocalization localization;
}

