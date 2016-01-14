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
package org.martus.client.swingui.jfx.setupwizard.step6;

import java.io.File;

import javafx.application.Platform;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.filefilters.KeyPairFormatFilter;
import org.martus.common.MartusLogger;
import org.martus.util.FileTransfer;
import org.martus.util.FileVerifier;
import org.martus.util.TokenReplacement;

public class BackupKeyAction implements ActionDoer
{
	public BackupKeyAction(FxSetupBackupYourKeyController backupKeyControllerToUse)
	{
		backupKeyController = backupKeyControllerToUse;
	}
	
	@Override
	public void doAction()
	{
		try
		{
			doBackupKeyPairToSingleEncryptedFile();
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			getMainWindow().unexpectedErrorDlg(e);
		}
	}
	
	private void doBackupKeyPairToSingleEncryptedFile() throws Exception 
	{
		setControllerMessageLabel("");
		File keypairFile = getApp().getCurrentKeyPairFile();
		MartusLocalization localization = getLocalization();
		if(keypairFile.length() > UiMainWindow.MAX_KEYPAIRFILE_SIZE)
		{
			setControllerMessageLabel(localization.getFieldLabel("KeypairTooLarge"));
			return;
		}

		KeyPairFormatFilter keyPairFilter = getMainWindow().getKeyPairFormatFilter();
		File newBackupFile = getMainWindow().showFileSaveDialog("SaveKeyPair", MartusApp.KEYPAIR_FILENAME, keyPairFilter);
		if(newBackupFile == null)
			return;
		
		if(!newBackupFile.getName().contains("."))
			newBackupFile = new File(newBackupFile.getAbsolutePath() + keyPairFilter.getExtension());
		
		FileTransfer.copyFile(keypairFile, newBackupFile);
		if(FileVerifier.verifyFiles(keypairFile, newBackupFile))
		{
			String message = TokenReplacement.replaceToken(localization.getFieldLabel("SingleEncryptedKeyBackupCreated"), "#backupFileName", newBackupFile.getName());
			setControllerMessageLabel(message);
			getApp().getConfigInfo().setBackedUpKeypairEncrypted(true);
			getApp().saveConfigInfo();
		}
	}

	private void setControllerMessageLabel(String translatedMessage)
	{
		Platform.runLater(new MessageLabelUpdater(translatedMessage));
	}

	private MartusApp getApp()
	{
		return getBackupKeyController().getApp(); 
	}
	
	private UiMainWindow getMainWindow()
	{
		return getBackupKeyController().getMainWindow();
	}

	private MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}
	
	protected FxSetupBackupYourKeyController getBackupKeyController()
	{
		return backupKeyController;
	}
	
	protected class MessageLabelUpdater implements Runnable
	{
		protected MessageLabelUpdater(String translatedMessageToUse)
		{
			translatedMessage = translatedMessageToUse;
		}
		
		public void run()
		{
			getBackupKeyController().updateBackupKeyMessageLabelWithTranslatedMessage(translatedMessage);
		}
		
		private String translatedMessage;
	}
	
	private FxSetupBackupYourKeyController backupKeyController;
}
