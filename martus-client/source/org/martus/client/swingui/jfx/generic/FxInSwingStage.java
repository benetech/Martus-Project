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

import java.awt.Container;
import java.awt.Window;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.SwingUtilities;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;

public class FxInSwingStage extends VirtualStage
{
	public FxInSwingStage(UiMainWindow mainWindowToUse, Window windowToUse, FxShellController shellController, String cssNameToUse)
	{
		this(mainWindowToUse, cssNameToUse);
	
		setWindow(windowToUse);
		setShellController(shellController);
	}

	public FxInSwingStage(UiMainWindow mainWindowToUse, String cssName)
	{
		super(mainWindowToUse, cssName);

		panel = new JFXPanel();
	}

	public void setWindow(Window dialogToUse)
	{
		window = dialogToUse;
	}

	public Window getWindow()
	{
		return window;
	}

	@Override
	public void setScene(Scene scene)
	{
		panel.setScene(scene);
	}
	
	@Override
	public void show()
	{
		getWindow().setVisible(true);
	}
	
	@Override
	public void showCurrentPage() throws Exception
	{
		loadAndShowShell();
	}
	
	@Override
	public void runOnFxThreadMaybeLater(Runnable toRun)
	{
		Platform.runLater(toRun);
	}

	public void doAction(ActionDoer doer)
	{
		try
		{
			SwingUtilities.invokeLater(new Doer(doer));
		} 
		catch (Exception e)
		{
			SwingUtilities.invokeLater(new ShowErrorDialogHandler(e));
		}
	}
	
	private class Doer implements Runnable
	{
		public Doer(ActionDoer doerToRun)
		{
			doer = doerToRun;
		}
		
		@Override
		public void run()
		{
			doer.doAction();
		}
		
		private ActionDoer doer;
	}

	private class ShowErrorDialogHandler implements Runnable
	{
		public ShowErrorDialogHandler(Exception e)
		{
			exceptionToReport = e;
		}

		public void run()
		{
			getMainWindow().unexpectedErrorDlg(exceptionToReport);
		}
		Exception exceptionToReport;
	}
	
	public void 	logAndNotifyUnexpectedError(Exception e)
	{
		SwingUtilities.invokeLater(new ShowErrorDialogHandler(e));
	}

	public Container getPanel()
	{
		return panel;
	}

	public Scene getScene()
	{
		return panel.getScene();
	}
	
	@Override
	public double getWidthAsDouble()
	{
		return getPanel().getWidth();
	}
	
	@Override
	public void close()
	{
		throw new RuntimeException("Close called for " + getClass().getName());
	}


	private JFXPanel panel;
	private Window window;
}
