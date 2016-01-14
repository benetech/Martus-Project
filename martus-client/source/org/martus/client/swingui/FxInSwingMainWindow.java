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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;

import javafx.application.Platform;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.martus.client.swingui.dialogs.FxInSwingBulletinModifyDialog;
import org.martus.client.swingui.dialogs.UiBulletinModifyDlg;
import org.martus.client.swingui.jfx.contacts.FxInSwingContactsStage;
import org.martus.client.swingui.jfx.generic.FxInSwingDialogStage;
import org.martus.client.swingui.jfx.generic.FxInSwingModalDialog;
import org.martus.client.swingui.jfx.generic.FxInSwingModalDialogStage;
import org.martus.client.swingui.jfx.generic.FxInSwingStage;
import org.martus.client.swingui.jfx.generic.FxRunner;
import org.martus.client.swingui.jfx.generic.FxShellController;
import org.martus.client.swingui.jfx.generic.FxStatusBar;
import org.martus.client.swingui.jfx.generic.VirtualStage;
import org.martus.client.swingui.jfx.landing.FxInSwingMainStage;
import org.martus.client.swingui.jfx.landing.FxMainStage;
import org.martus.client.swingui.jfx.setupwizard.FxInSwingSetupWizardStage;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.Utilities;

public class FxInSwingMainWindow extends UiMainWindow
{
	public FxInSwingMainWindow() throws Exception
	{
		super();
	}
	
	@Override
	protected void initializeFrame()
	{

		swingFrame = new MainSwingFrame(this);
		UiMainWindow.updateIcon(getSwingFrame());
		setCurrentActiveFrame(this);
		getSwingFrame().setVisible(true);
		updateTitle();
		restoreWindowSizeAndState();

		if(UiSession.isJavaFx())
		{
			FxInSwingMainStage fxInSwingMainStage = new FxInSwingMainStage(this, getSwingFrame());
			mainStage = fxInSwingMainStage;
			setStatusBar(createStatusBar());
			FxRunner fxRunner = new FxRunner(fxInSwingMainStage);
			fxRunner.setAbortImmediatelyOnError();
			Platform.runLater(fxRunner);
			getSwingFrame().setContentPane(fxInSwingMainStage.getPanel());
		}
		else
		{
			setStatusBar(createStatusBar());
			mainPane = new UiMainPane(this, getUiState());
			getSwingFrame().setContentPane(mainPane);
		}

	}
	
	@Override
	protected void hideMainWindow()
	{
		getSwingFrame().setEnabled(false);
		getSwingFrame().setVisible(false);
	}
	
	@Override
	public void restoreWindowSizeAndState()
	{
		Dimension screenSize = Utilities.getViewableScreenSize();
		Dimension appDimension = getUiState().getCurrentAppDimension();
		Point appPosition = getUiState().getCurrentAppPosition();
		boolean showMaximized = false;
		if(Utilities.isValidScreenPosition(screenSize, appDimension, appPosition))
		{
			getSwingFrame().setLocation(appPosition);
			getSwingFrame().setSize(appDimension);
			if(getUiState().isCurrentAppMaximized())
				showMaximized = true;
		}
		else
			showMaximized = true;
		
		if(showMaximized)
		{
			getSwingFrame().setSize(screenSize.width - 50 , screenSize.height - 50);
			Utilities.maximizeWindow(getSwingFrame());
		}
		
		getUiState().setCurrentAppDimension(getMainWindowSize());
	}

	public StatusBar createStatusBar()
	{
		if(UiSession.isJavaFx())
			return new FxStatusBar(getLocalization());
		
		return new UiStatusBar(getLocalization());
	}

	@Override
	public JFrame getSwingFrame()
	{
		return swingFrame;
	}

	@Override
	public UiMainPane getMainPane()
	{
		return mainPane;
	}
	
	@Override
	public FxMainStage getMainStage()
	{
		return mainStage;
	}

	private void updateTitle() 
	{
		getSwingFrame().setTitle(getLocalization().getWindowTitle("main"));
	}
	
	@Override
	public void rawError(String errorText)
	{
		JOptionPane.showMessageDialog(null, errorText, "ERROR", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void rawSetCursor(Object newCursor)
	{
		getSwingFrame().setCursor((Cursor)newCursor);
	}

	@Override
	public Object getWaitCursor()
	{
		return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	}

	@Override
	public Object getExistingCursor()
	{
		return getSwingFrame().getCursor();
	}
	
	@Override
	protected void showMainWindow()
	{
		getSwingFrame().setVisible(true);
		getSwingFrame().setEnabled(true);
		getSwingFrame().toFront();
	}

	@Override
	protected void obscureMainWindow()
	{
		getSwingFrame().setLocation(100000, 0);
		getSwingFrame().setSize(0,0);
		getSwingFrame().setEnabled(false);
	}
	
	public static FxInSwingStage createGenericStage(UiMainWindow observerToUse, Window windowToUse, FxShellController shellController, String cssName)
	{
		return new FxInSwingStage(observerToUse, windowToUse, shellController, cssName);
	}

	@Override
	public void createAndShowLargeModalDialog(VirtualStage stage) throws Exception
	{
		createAndShowDialog((FxInSwingDialogStage) stage, FxInSwingModalDialog.EMPTY_TITLE, LARGE_PREFERRED_DIALOG_SIZE);
	}

	@Override
	public void createAndShowModalDialog(FxShellController controller, Dimension preferedDimension, String titleTag)
	{
		FxInSwingModalDialogStage stage = new FxInSwingModalDialogStage(this, controller);
		createAndShowDialog(stage, titleTag, preferedDimension);
	}

	@Override
	public void createAndShowContactsDialog() throws Exception
	{
		createAndShowLargeModalDialog(new FxInSwingContactsStage(this));
	}

	@Override
	public void createAndShowSetupWizard() throws Exception
	{
		createAndShowLargeModalDialog(new FxInSwingSetupWizardStage(this));
	}
	
	private void createAndShowDialog(FxInSwingDialogStage stage, String titleTag, Dimension dimension)
	{
		if (dimension == null)
			dimension = LARGE_PREFERRED_DIALOG_SIZE;
		
		FxInSwingModalDialog dialog = createDialog(this);
		dialog.setIconImage(Utilities.getMartusIconImage());
		
		if (titleTag.length() > 0)
			dialog.setTitle(getLocalization().getWindowTitle(titleTag));
		
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.getContentPane().setPreferredSize(dimension);
		dialog.pack();
		dialog.getContentPane().add(stage.getPanel());
		stage.setDialog(dialog);
		stage.runOnFxThreadMaybeLater(new FxRunner(stage));
	
		Utilities.packAndCenterWindow(dialog);
		setCurrentActiveDialog(dialog);
		try
		{
			dialog.setVisible(true);
		}
		finally
		{
			setCurrentActiveDialog(null);
		}
	}

	@Override
	public Dimension getMainWindowSize()
	{
		return getSwingFrame().getSize();
	}

	@Override
	public Point getMainWindowLocation()
	{
		return getSwingFrame().getLocation();
	}

	@Override
	public boolean isMainWindowMaximized()
	{
		return getSwingFrame().getExtendedState()==JFrame.MAXIMIZED_BOTH;
	}


	private static FxInSwingModalDialog createDialog(UiMainWindow owner)
	{
		JFrame frame = owner.getSwingFrame();
		if(frame != null)
			return new FxInSwingModalDialog(frame);
	
		return new FxInSwingModalDialog();
	}

	@Override
	public void modifyBulletin(Bulletin b) throws Exception
	{
		getCurrentUiState().setModifyingBulletin(true);
		UiBulletinModifyDlg dlg = null;
		try
		{
			dlg = new FxInSwingBulletinModifyDialog(b, this);
			dlg.restoreFrameState();
			setCurrentActiveFrame(dlg);
			hideMainWindow();
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


	private JFrame swingFrame;
	private UiMainPane mainPane;
	private FxMainStage mainStage;
}
