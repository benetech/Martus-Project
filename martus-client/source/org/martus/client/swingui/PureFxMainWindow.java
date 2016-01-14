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
package org.martus.client.swingui;

import java.awt.Dimension;
import java.awt.Point;

import javafx.scene.Cursor;
import javafx.stage.Stage;

import javax.swing.JFrame;

import org.martus.client.swingui.dialogs.PureFxBulletinModifyDialog;
import org.martus.client.swingui.dialogs.UiBulletinModifyDlg;
import org.martus.client.swingui.jfx.contacts.PureFxContactsStage;
import org.martus.client.swingui.jfx.generic.FxShellController;
import org.martus.client.swingui.jfx.generic.FxStatusBar;
import org.martus.client.swingui.jfx.generic.PureFxDialogStage;
import org.martus.client.swingui.jfx.generic.PureFxStage;
import org.martus.client.swingui.jfx.generic.VirtualStage;
import org.martus.client.swingui.jfx.landing.FxMainStage;
import org.martus.client.swingui.jfx.landing.PureFxMainStage;
import org.martus.client.swingui.jfx.setupwizard.PureFxSetupWizardStage;
import org.martus.common.bulletin.Bulletin;

public class PureFxMainWindow extends UiMainWindow
{
	public PureFxMainWindow() throws Exception
	{
		super();
	}

	public StatusBar createStatusBar()
	{
		return new FxStatusBar(getLocalization());
	}

	@Override
	public JFrame getSwingFrame()
	{
		return null;
	}

	@Override
	public UiMainPane getMainPane()
	{
		return null;
	}
	
	@Override
	public FxMainStage getMainStage()
	{
		return null;
	}

	@Override
	protected void hideMainWindow()
	{
		realStage.hide();
	}
	
	@Override
	protected void initializeFrame() throws Exception
	{
		setStatusBar(createStatusBar());
		PureFxMainStage fxStage = new PureFxMainStage(this, realStage);
		fxStage.showCurrentPage();
		restoreWindowSizeAndState();
		realStage.setTitle(getLocalization().getWindowTitle("main"));
		realStage.show();
	}
	
	@Override
	public void restoreWindowSizeAndState()
	{
		Dimension appDimension = getUiState().getCurrentAppDimension();
		Point appPosition = getUiState().getCurrentAppPosition();
		boolean showMaximized = getUiState().isCurrentAppMaximized();

		realStage.setX(appPosition.getX());
		realStage.setY(appPosition.getY());
		realStage.setWidth(appDimension.getWidth());
		realStage.setHeight(appDimension.getHeight());
		realStage.setMaximized(showMaximized);
		
		getUiState().setCurrentAppDimension(getMainWindowSize());
	}

	@Override
	public void rawError(String errorText)
	{
		// FIXME: PureFX: We need to support this
	}

	@Override
	public void rawSetCursor(Object newCursor)
	{
		realStage.getScene().setCursor((Cursor) newCursor);
	}

	@Override
	public Object getWaitCursor()
	{
		return Cursor.WAIT;
	}

	@Override
	public Object getExistingCursor()
	{
		return realStage.getScene().getCursor();
	}

	@Override
	protected void showMainWindow()
	{
		realStage.show();
	}
	
	@Override
	protected void obscureMainWindow()
	{
		// FIXME: PureFX: We need to support this
	}
	
	public static void setStage(Stage stage)
	{
		PureFxMainWindow.realStage = stage;
	}
	
	@Override
	public void createAndShowLargeModalDialog(VirtualStage stageToShow) throws Exception
	{
		PureFxStage fxStage = (PureFxStage)stageToShow;
		fxStage.showCurrentPage();
		fxStage.showAndWait();
	}

	@Override
	public void createAndShowModalDialog(FxShellController controller, Dimension preferedDimension, String titleTag) throws Exception
	{
		PureFxDialogStage dialogStage = new PureFxDialogStage(this, controller); 
		dialogStage.showCurrentPage();
		dialogStage.showAndWait();
	}
	
	@Override
	public void createAndShowContactsDialog() throws Exception
	{
		createAndShowLargeModalDialog(new PureFxContactsStage(this));
	}

	@Override
	public void createAndShowSetupWizard() throws Exception
	{
		createAndShowLargeModalDialog(new PureFxSetupWizardStage(this));
	}
	
	public Dimension getMainWindowSize()
	{
		int width = (int)realStage.getWidth();
		int height = (int)realStage.getHeight();
		return new Dimension(width, height);
	}

	public Point getMainWindowLocation()
	{
		int x = (int)realStage.getX();
		int y = (int)realStage.getY();
		return new Point(x, y);
	}

	public boolean isMainWindowMaximized()
	{
		return realStage.isMaximized();
	}
	
	@Override
	public void modifyBulletin(Bulletin b) throws Exception
	{
		getCurrentUiState().setModifyingBulletin(true);
		UiBulletinModifyDlg dlg = null;
		try
		{
			dlg = new PureFxBulletinModifyDialog(b, this);
			hideMainWindow();
			setCurrentActiveFrame(dlg);
			dlg.restoreFrameState();
			dlg.setVisible(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(dlg != null)
				dlg.dispose();
			doneModifyingBulletin();
			throw(e);
		}
	}


	private static Stage realStage;
}
