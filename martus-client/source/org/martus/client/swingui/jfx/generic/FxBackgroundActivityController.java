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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.martus.client.swingui.UiMainWindow;

abstract public class FxBackgroundActivityController extends FxPopupController
{
	
	public FxBackgroundActivityController(UiMainWindow mainWindowToUse, String messageToUse, Task taskToUse)
	{
		super(mainWindowToUse);
		message = messageToUse;
		task = taskToUse;
	}
  	
	@Override
	public void initialize()
	{
		fxLabel.setText(message);
		task.stateProperty().addListener(new TaskStateChangeHandler());	
		PureFxStage stage = getPureFxStage();
		stage.setOnCloseRequest(new CloseEventHandler());
		stage.initStyle(StageStyle.UNDECORATED);
		Thread thread = new Thread(task);
		thread.setDaemon(false);
		thread.start();
	}	

	private final class CloseEventHandler implements EventHandler<WindowEvent>
	{
		public CloseEventHandler()
		{
		}

		public void handle(final WindowEvent event) 
		{
			event.consume();
			forceCloseDialog();
		}
	}
	
	public void forceCloseDialog()
	{
		if(task != null)
		{
			task.cancel();
			task = null;
		}
		getStage().close();
	}
	
	@Override
	public String getDialogTitle()
	{
		return "";
	}

	@Override
	public String getFxmlLocation()
	{
		return "generic/FxBusy.fxml";
	}

	public void updateProgressBar(double currentProgress)
	{
		fxProgressBar.setProgress(currentProgress);
	}
	
	public void taskSucceeded()
	{
		forceCloseDialog();
	}

	protected class TaskStateChangeHandler implements ChangeListener<Task.State>
	{
		@Override
		public void changed(ObservableValue<? extends State> observable, State oldState, State newState)
		{
			if(newState.equals(State.SUCCEEDED))
			{
				taskSucceeded();
			}
			else if(newState.equals(State.FAILED))
			{
				if(task != null)
				{
					if(task.getException() instanceof Exception)
						setThrownException((Exception)task.getException());
					else
						setThrownThrowable(task.getException());
				}
				forceCloseDialog();
			}
		}
	}

	abstract public boolean didUserCancel();
	@FXML
	abstract public void cancelPressed();
	
	@FXML
	protected Label fxLabel;	
	@FXML
	protected ProgressBar fxProgressBar;
	@FXML
	protected Button cancelButton;
	
	protected Task task;
	private String message;
}
