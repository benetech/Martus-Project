/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.swingui.jfx.generic;

import java.io.File;

import javafx.scene.Parent;
import javafx.scene.Scene;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;

public abstract class VirtualStage
{
	public VirtualStage(UiMainWindow mainWindowToUse, String cssNameToUse)
	{
		mainWindow = mainWindowToUse;

		setCssName(cssNameToUse);
	}
	
	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	abstract public void show();
	abstract public void close();
	abstract public void doAction(ActionDoer doer);
	abstract public void logAndNotifyUnexpectedError(Exception e);
	abstract public double getWidthAsDouble();
	abstract public void showCurrentPage() throws Exception;
	abstract public void setScene(Scene scene);
	abstract public Scene getScene();
	abstract public void runOnFxThreadMaybeLater(Runnable toRun);

	protected FxScene createEmptyShellScene() throws Exception
	{
		return new FxScene(getExternalFxmlDirectory(), getCssName());
	}

	public FxScene getFxScene()
	{
		return (FxScene)getScene();
	}
	
	public void setShellController(FxShellController shellController)
	{
		this.shellController = shellController;
	}

	public FxShellController getShellController()
	{
		return shellController;
	}

	public MartusApp getApp()
	{
		return getMainWindow().getApp();
	}

	public MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}

	public void unexpectedErrorDlg(Exception e)
	{
		getMainWindow().unexpectedErrorDlg(e);
	}

	public File getExternalFxmlDirectory()
	{
		return getMainWindow().getApp().getFxmlDirectory();
	}

	public void loadAndShowShell() throws Exception
	{
		if(getScene() == null)
		{
			FxScene scene = createEmptyShellScene();
			setScene(scene);
		}
		
		getShellController().setStage(this);
		Parent shellContents = getShellController().createContents();
	
		getScene().setRoot(shellContents);
		getFxScene().applyStyleSheet(getLocalization().getCurrentLanguageCode());
	}

	private void setCssName(String cssName)
	{
		this.cssFile = cssName;
	}
	
	protected String getCssName()
	{
		return cssFile;
	}

	private UiMainWindow mainWindow;
	private FxShellController shellController;
	private String cssFile;
}
