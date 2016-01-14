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

import java.awt.Component;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javafx.application.Platform;
import javafx.stage.Stage;

import org.martus.client.swingui.TranslucentWindowObscurer;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusLogger;

public abstract class FxInSwingController extends FxController
{
	public FxInSwingController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);

	}
	
	protected void showModalPopupStage(Stage popupStage)
	{
		FxInSwingStage swingStage = (FxInSwingStage) getStage();
		Window window = swingStage.getWindow();

		GlassPaneInstaller glassPaneInstaller = new GlassPaneInstaller(window);
		Runnable fronter = new Fronter(popupStage);
	
		DialogWindowHandler windowHandler = new DialogWindowHandler(fronter);
		window.addWindowListener(windowHandler);
		window.addWindowFocusListener(windowHandler);
		
		Component glassPane = new TranslucentWindowObscurer();
		glassPaneInstaller.installGlassPane(glassPane);
		GlassPaneMouseHandler glassPaneMouseHandler = new GlassPaneMouseHandler(fronter);
		glassPane.addMouseListener(glassPaneMouseHandler);
		
		glassPane.setVisible(true);
		try
		{
			popupStage.showAndWait();
		    MartusLogger.log("Back from showAndWait");
		}
		finally
		{
			glassPane.removeMouseListener(glassPaneMouseHandler);
	
			window.removeWindowFocusListener(windowHandler);
			window.removeWindowListener(windowHandler);
			
			glassPane.setVisible(false);
		    MartusLogger.log("Glass pane is now invisible");
		}
	}

	protected static class DialogWindowHandler extends WindowAdapter
	{
		public DialogWindowHandler(Runnable runOnFocusGained)
		{
			task = runOnFocusGained;
		}
		
		@Override
		public void windowDeiconified(WindowEvent e)
		{
			Platform.runLater(task);
		}

		@Override
		public void windowActivated(WindowEvent e)
		{
			Platform.runLater(task);
		}

		@Override
		public void windowOpened(WindowEvent e)
		{
			Platform.runLater(task);
		}

		@Override
		public void windowGainedFocus(WindowEvent e)
		{
			Platform.runLater(task);
		}

		private Runnable task;
	}

	protected static class GlassPaneMouseHandler extends MouseAdapter
	{
		public GlassPaneMouseHandler(Runnable runOnClick)
		{
			task = runOnClick;
		}
		
		@Override
		public void mouseClicked(MouseEvent e)
		{
			super.mouseClicked(e);
			Platform.runLater(task);
		}
		
		@Override
		public void mouseMoved(MouseEvent e)
		{
			super.mouseMoved(e);
			Platform.runLater(task);
		}
		
		private Runnable task;
	}

	protected static class Fronter implements Runnable
	{
		public Fronter(Stage popupStageToUse)
		{
			popupStage = popupStageToUse;
		}
		
		@Override
		public void run()
		{
			popupStage.toFront();
		}
		
		private Stage popupStage;
	}

}
