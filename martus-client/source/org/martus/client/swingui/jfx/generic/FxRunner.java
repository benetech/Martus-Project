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

import java.awt.Window;

import org.martus.client.swingui.FxInSwingMainWindow;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusLogger;

public class FxRunner implements Runnable
{
	public FxRunner(VirtualStage stageToUse)
	{
		stage = stageToUse;
	}
	
	public void run()
	{
		try
		{
			stage.showCurrentPage();
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			if(!shouldAbortImmediatelyOnError)
			{
				stage.unexpectedErrorDlg(e);
			}
			System.exit(1);
		}
	}
	
	public void setAbortImmediatelyOnError()
	{
		shouldAbortImmediatelyOnError = true;
	}

	public static FxInSwingStage createAndActivateEmbeddedStage(UiMainWindow observerToUse, Window windowToUse, FxShellController shellController)
	{
		FxInSwingStage stage = FxInSwingMainWindow.createGenericStage(observerToUse, windowToUse, shellController, shellController.getCssName());
		
		FxRunner fxRunner = new FxRunner(stage);
		fxRunner.setAbortImmediatelyOnError();
		stage.runOnFxThreadMaybeLater(fxRunner);
		
		return stage;
	}

	private VirtualStage stage;
	private boolean shouldAbortImmediatelyOnError;
}