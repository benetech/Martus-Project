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

import java.util.Stack;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.client.swingui.jfx.setupwizard.FxSetupWizardShellController;
import org.martus.common.MartusLogger;

abstract public class PureFxWizardStage extends PureFxDialogStage implements WizardStageInterface
{
	public PureFxWizardStage(UiMainWindow mainWindowToUse) throws Exception
	{
		super(mainWindowToUse, new FxSetupWizardShellController(mainWindowToUse));
		
		serverAvailibilityState = SERVER_STATE_NOT_INITIALIZED;
		visitedWizardPagesStack = new Stack<FxContentController>();

		setCurrentController(getFirstController());
	}
	
	public FxContentController getCurrentController()
	{
		return currentContentController;
	}

	public void setCurrentController(FxContentController contentControllerToUse)
	{
		currentContentController = contentControllerToUse;
	}

	@Override
	public void showCurrentPage() throws Exception
	{
		loadAndShowCurrentWizardPage(getCurrentController());
		getWizardShellController().getNextButton().setDefaultButton(true);
	}

	public void loadAndShowCurrentWizardPage(FxController contentPaneController) throws Exception
	{
		loadAndShowShell();
		
		contentPaneController.setShellController(getShellController());
		getWizardShellController().loadAndIntegrateContentPane(contentPaneController);
	}
	
	public void next()
	{
		try
		{
			FxContentController currentController = getCurrentController();
			AbstractFxSetupWizardContentController contentPaneController = (AbstractFxSetupWizardContentController) currentController;
			FxContentController nextController = contentPaneController.getNextController();
			visitedWizardPagesStack.push(currentController);
			if(nextController == null)
			{
				close();
			}
			else
			{
				setCurrentController(nextController);
				showCurrentPage();
			}
		}
		catch(Exception e)
		{
			handleNavigationException(e);
		}
	}

	public void back()
	{
		try
		{
			if(visitedWizardPagesStack.isEmpty())
			{
				close();
			}
			else
			{
				setCurrentController(visitedWizardPagesStack.pop());
				showCurrentPage();
			}
		}
		catch(Exception e)
		{
			handleNavigationException(e);
		}
	}

	public void handleNavigationException(Exception e)
	{
		MartusLogger.logException(e);
		try
		{
			getCurrentController().showNotifyDialog("UnexpectedError");
		}
		catch(Exception exceptionShowingErrorDialog)
		{
			MartusLogger.logException(exceptionShowingErrorDialog);
			getMainWindow().exitWithoutSavingState();
		}
	}

	public boolean checkIfCurrentServerIsAvailable()
	{
		if(serverAvailibilityState == SERVER_STATE_NOT_INITIALIZED)
			return false;
		return serverAvailibilityState.equals(SERVER_STATE_AVAILABLE);
	}
	
	public void setCurrentServerIsAvailable(boolean isServerAvailable)
	{
		serverAvailibilityState = SERVER_STATE_NOT_AVAILABLE;
		if (isServerAvailable)
			serverAvailibilityState = SERVER_STATE_AVAILABLE;
	}
	
	public boolean hasServerAvailabilityBeenInitialized()
	{
		return serverAvailibilityState != SERVER_STATE_NOT_INITIALIZED;
	}
	
	public FxWizardShellController getWizardShellController()
	{
		return (FxWizardShellController)getShellController();
	}
	
	abstract protected FxContentController getFirstController();

	private Stack<FxContentController> visitedWizardPagesStack;

	private String serverAvailibilityState;
	private FxContentController currentContentController;

	private static final String SERVER_STATE_NOT_INITIALIZED = null;
	private static final String SERVER_STATE_NOT_AVAILABLE = "IsNotAvailable";
	private static final String SERVER_STATE_AVAILABLE = "IsAvailable";
}
