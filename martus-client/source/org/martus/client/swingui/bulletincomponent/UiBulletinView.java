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

package org.martus.client.swingui.bulletincomponent;

import java.io.IOException;

import javax.swing.event.ChangeEvent;

import org.martus.client.core.BulletinLanguageChangeListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.fieldspec.DataInvalidException;

public class UiBulletinView extends UiBulletinComponent
{
	public UiBulletinView(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		// ensure that attachmentViewer gets initialized
	}

	public void setLanguageChangeListener(BulletinLanguageChangeListener listener)
	{
		// read-only view cannot change encryption status		
	}
	
	public UiBulletinComponentDataSection createBulletinComponentDataSection(String sectionName)
	{
		return new UiBulletinComponentViewSection(getMainWindow(), sectionName);
	}

	public void copyDataToBulletin(Bulletin bulletin) throws
			IOException,
			MartusCrypto.EncryptionException
	{
		// read-only view cannot update the bulletin
	}		

	public void validateData() throws DataInvalidException 
	{
		// read-only view is always valid
	}
	
	public boolean isBulletinModified() throws
			IOException,
			MartusCrypto.EncryptionException
	{
		return false;
	}	

	// ChangeListener interface
	public void stateChanged(ChangeEvent event)
	{
		// read-only view cannot change state
	}
	
	protected UiBulletinComponentHeaderSection createHeaderSection()
	{
		return new UiBulletinComponentHeaderSection(getMainWindow(), "View");
	}

	protected UiBulletinComponentHeadQuartersSection createHeadQuartersSection()
	{
		return new UiBulletinComponentHeadQuartersViewer(getMainWindow(), currentBulletin, "View");
	}

}
