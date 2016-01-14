/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2015, Beneficent
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

import java.awt.Dimension;
import java.awt.Point;

import javafx.stage.Stage;

import javax.swing.JFrame;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.PureFxDialogStage;
import org.martus.client.swingui.jfx.landing.bulletins.FxBulletinEditorShellController;
import org.martus.common.bulletin.Bulletin;

public class PureFxBulletinModifyDialog extends UiBulletinModifyDlg
{
	public PureFxBulletinModifyDialog(Bulletin b, UiMainWindow observerToUse) throws Exception
	{
		super(b, observerToUse);

		FxBulletinEditorShellController bulletinEditorShellController = new FxBulletinEditorShellController(observerToUse, this);
		setView(bulletinEditorShellController);
		
		dialogStage = new PureFxDialogStage(getMainWindow(), bulletinEditorShellController); 
		dialogStage.showCurrentPage();
		safelyPopulateView();
		
		dialogStage.getActualStage().setOnCloseRequest((event) -> closeWindowIfUserConfirms());
	}

	@Override
	public JFrame getSwingFrame()
	{
		return null;
	}

	@Override
	protected void setFrameLocation(Point bulletinEditorPosition)
	{
		Stage actualStage = dialogStage.getActualStage();
		actualStage.setX(bulletinEditorPosition.getX());
		actualStage.setY(bulletinEditorPosition.getY());
	}

	@Override
	protected void setFrameSize(Dimension bulletinEditorDimension)
	{
		Stage actualStage = dialogStage.getActualStage();
		actualStage.setWidth(bulletinEditorDimension.getWidth());
		actualStage.setHeight(bulletinEditorDimension.getHeight());
	}

	@Override
	protected void setFrameMaximized(boolean bulletinEditorMaximized)
	{
		dialogStage.getActualStage().setMaximized(bulletinEditorMaximized);
	}

	@Override
	protected Point getFrameLocation()
	{
		Stage actualStage = dialogStage.getActualStage();
		int x = (int)actualStage.getX();
		int y = (int)actualStage.getY();
		return new Point(x, y);
	}
	
	@Override
	protected Dimension getFrameSize() 
	{
		Stage actualStage = dialogStage.getActualStage();
		int width = (int) actualStage.getWidth();
		int height = (int) actualStage.getHeight();
		return new Dimension(width, height);
	}
	
	@Override
	protected boolean isFrameMaximized()
	{
		return dialogStage.getActualStage().isMaximized();
	}
	
	@Override
	public void dispose()
	{
		dialogStage.close();
	}

	@Override
	public void setVisible(boolean newState)
	{
		dialogStage.show();
	}

	private PureFxDialogStage dialogStage;
}
