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

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;


abstract public class PureFxStage extends VirtualStage
{
	public PureFxStage(UiMainWindow mainWindowToUse, FxShellController controller, String cssNameToUse) throws Exception
	{
		this(mainWindowToUse, controller.getDialogTitle(), new Stage(), cssNameToUse);
		setShellController(controller);
		controller.setStage(this);
		getActualStage().initModality(Modality.APPLICATION_MODAL);
	}

	public PureFxStage(UiMainWindow mainWindowToUse, String title, Stage stageToUse, String cssNameToUse) throws Exception
	{
		super(mainWindowToUse, cssNameToUse);
		stage = stageToUse;
		
		stage.setTitle(title);

		Scene scene = createEmptyShellScene();
		setScene(scene);
	}

	@Override
	public void doAction(ActionDoer doer)
	{
		// NOTE: We are already on the JavaFX thread
		doer.doAction();
	}

	@Override
	public void logAndNotifyUnexpectedError(Exception e)
	{
		getMainWindow().unexpectedErrorDlg(e);
	}
	
	@Override
	public double getWidthAsDouble()
	{
		return stage.getWidth();
	}
	
	@Override
	public void show()
	{
		stage.show();
	}
	
	@Override
	public void showCurrentPage() throws Exception
	{
		loadAndShowShell();
	}

	@Override
	public void unexpectedErrorDlg(Exception e)
	{
		getMainWindow().unexpectedErrorDlg(e);
	}

	@Override
	public void close()
	{
		stage.close();
	}

	public void setOnCloseRequest(EventHandler<WindowEvent> closeEventHandler)
	{
		stage.setOnCloseRequest(closeEventHandler);
	}

	public void initStyle(StageStyle style)
	{
		stage.initStyle(style);
	}

	@Override
	public void setScene(Scene scene)
	{
		stage.setScene(scene);
	}
	
	@Override
	public Scene getScene()
	{
		return stage.getScene();
	};
	
	@Override
	public void runOnFxThreadMaybeLater(Runnable toRun)
	{
		toRun.run();
	}

	public Stage getActualStage()
	{
		return stage;
	}

	public void showAndWait()
	{
		stage.showAndWait();
	}
	
	private Stage stage;
}
