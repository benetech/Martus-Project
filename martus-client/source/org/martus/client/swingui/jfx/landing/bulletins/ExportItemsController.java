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
package org.martus.client.swingui.jfx.landing.bulletins;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import org.martus.client.core.MartusApp;
import org.martus.client.core.TransferableBulletinList;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.filefilters.XmlFileFilter;
import org.martus.client.swingui.filefilters.MartusBulletinArchiveFileFilter;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.FxModalDirectoryChooser;
import org.martus.client.swingui.jfx.generic.FxModalFileChooser;
import org.martus.client.swingui.jfx.generic.PureFxStage;
import org.martus.clientside.FormatFilter;

public class ExportItemsController extends FxController
{
	public ExportItemsController(UiMainWindow mainWindowToUse, String initialExportFilenameOnly, int numberOfItemsToExport)
	{
		super(mainWindowToUse);
		this.exportFilenameOnly = initialExportFilenameOnly;
		this.exportFolder = getApp().getMartusDataRootDirectory();
		this.multipleBulletinBeingExported = numberOfItemsToExport > 1;
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		
		BooleanProperty encryptingExport = encryptExportFile.selectedProperty();
		includeAttachments.disableProperty().bind(encryptingExport);
		encryptExportFile.selectedProperty().addListener(new EncryptedStatusChanged());
		encryptExportFile.setSelected(true);
		updateControls(shouldExportEncrypted());
	}

	public boolean shouldExportEncrypted()
	{
		return encryptExportFile.isSelected();
	}
	
	private class EncryptedStatusChanged implements ChangeListener<Boolean>
	{
		public EncryptedStatusChanged()
		{
		}

		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
		{
			updateControls(newValue);
		}
	}
	
	protected void updateControls(Boolean exportEncrypted)
	{
		String hintMessage = "";
		if(!exportEncrypted)
			hintMessage = getLocalization().getFieldLabel("ExportBulletinDetails");
		textMessageArea.setText(hintMessage);
		includeAttachments.setSelected(exportEncrypted);
		updateExportFilename();
	}
	
	private String getExportFilenameBasedOnEncryptionStatus()
	{
		if(exportingMultipleFiles())
			return "";
		String currentExportFilename = exportFilenameOnly;
		int positionExtension = currentExportFilename.lastIndexOf('.');
		String fileNameOnly = currentExportFilename;
		if(positionExtension > 0)
			fileNameOnly = currentExportFilename.substring(0, positionExtension);
		return getExportFilenameBasedOnEncryptionStatus(fileNameOnly);
	}
	
	private String getExportFilenameBasedOnEncryptionStatus(String fileNameOnly)
	{
		String fullNameWithCorrectFileExtension = fileNameOnly;
		if(shouldExportEncrypted())
			fullNameWithCorrectFileExtension += TransferableBulletinList.BULLETIN_FILE_EXTENSION;			
		else
			fullNameWithCorrectFileExtension += MartusApp.XML_EXTENSION;
		return fullNameWithCorrectFileExtension;
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/FxExportItems.fxml";
	}
	
	private void updateExportFilename()
	{
		String absolutePathToFileOrFolder = exportFolder.getAbsolutePath();
		if(!showDirectoryOnly())
		{
			exportFilenameOnly = getExportFilenameBasedOnEncryptionStatus();
			File combinedExportFile = new File(exportFolder, exportFilenameOnly);
			absolutePathToFileOrFolder = combinedExportFile.getAbsolutePath();
		}
		setExportFile(absolutePathToFileOrFolder);
	}

	private void setExportFile(String absolutePathToFileOrFolder)
	{
		String uniqueAbsolutePathToFileOrFolder = absolutePathToFileOrFolder;
		File currentFileOrFolder = new File(absolutePathToFileOrFolder);
		if(currentFileOrFolder.isFile())
		{
			File uniqueFile = getUniqueFile(currentFileOrFolder);
			uniqueAbsolutePathToFileOrFolder = uniqueFile.getAbsolutePath();
		}	
		fileLocation.setText(uniqueAbsolutePathToFileOrFolder);
	}

	private File getUniqueFile(File existingFile)
	{
		File currentFile = existingFile;
		do
		{
			currentFile = new File(currentFile.getParentFile(), getNextFileName(currentFile.getName()));
		} while (currentFile.exists());

		return currentFile;
	}

	private String getNextFileName(String name)
	{
		int lastIndexOfFileNameOnly = name.lastIndexOf('.');
		String fileExtension = name.substring(lastIndexOfFileNameOnly);
		Pattern p = Pattern.compile("\\([0-9]+\\)");
		Matcher m = p.matcher(name);
		int newIndex = 1;
		if(m.find())
		{
			int currentIndex = Integer.parseInt(m.group().replaceAll( "[^\\d]", "" ));
			newIndex = ++currentIndex;
			lastIndexOfFileNameOnly = m.start();
		}
		String FileNameWithoutIndex = name.substring(0, lastIndexOfFileNameOnly);
		String newFileNameWithIndex = FileNameWithoutIndex +"(" + Integer.toString(newIndex) + ")" + fileExtension;
		return newFileNameWithIndex;
	}

	@FXML
	public void onChangeFileLocation(ActionEvent event)
	{
		File saveLocationFileOrFolder = getFileSaveLocation();
		if(saveLocationFileOrFolder == null)
			return;
		if(showDirectoryOnly())
		{
			exportFolder = saveLocationFileOrFolder;
		}
		else
		{
			exportFolder = saveLocationFileOrFolder.getParentFile();
			exportFilenameOnly = saveLocationFileOrFolder.getName();
			existingFileToBeReplaced = saveLocationFileOrFolder;
		}
		fileLocation.setText(saveLocationFileOrFolder.getAbsolutePath());
	}

	private FormatFilter getFormatFilter()
	{
		MartusLocalization localization = getLocalization();
		if(shouldExportEncrypted())
			return new MartusBulletinArchiveFileFilter(localization);
		return new XmlFileFilter(localization);
	}
	
	private boolean showDirectoryOnly()
	{
		return exportingMultipleFiles();
	}

	private boolean exportingMultipleFiles()
	{
		return (multipleBulletinBeingExported && shouldExportEncrypted());
	}
	
	public boolean includeAttachments()
	{
		return includeAttachments.isSelected();
	}
	
	public File getExportFileOrFolder()
	{
		return new File(fileLocation.getText());
	}
	
	public boolean didUserApproveOverwritingExistingFile()
	{
		return getExportFileOrFolder().equals(existingFileToBeReplaced);
	}

	protected File getFileSaveLocation()
	{
		MartusLocalization localization = getLocalization();
		PureFxStage parentWindow = getParentWindow();
		
		//NOTE: DirectoryChooser and FileChooser are unfortunately derived from Object.
		if(showDirectoryOnly())
		{
			FxModalDirectoryChooser directoryChooser = new FxModalDirectoryChooser(parentWindow);
			directoryChooser.setTitle(localization.getWindowTitle("FolderSelectDialogExport"));
			directoryChooser.setInitialDirectory(exportFolder);
			
			return directoryChooser.showDialog();
		}
		
		FxModalFileChooser fileChooser = new FxModalFileChooser(parentWindow);
		fileChooser.setTitle(localization.getWindowTitle("FileSaveDialogExport"));
		fileChooser.setInitialDirectory(exportFolder);
		File currentUniqueFile = getExportFileOrFolder();
		fileChooser.setInitialFileName(currentUniqueFile.getName());
		FormatFilter fileFilter = getFormatFilter();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter(fileFilter.getDescription(), fileFilter.getWildCardExtension()));

		return fileChooser.showSaveDialog();
	}

	@FXML
	private TextArea textMessageArea;
	
	@FXML 
	private CheckBox encryptExportFile;

	@FXML
	private CheckBox includeAttachments;
	
	@FXML
	private TextField fileLocation;
	
	private String exportFilenameOnly;
	private File exportFolder;
	private boolean multipleBulletinBeingExported;
	private File existingFileToBeReplaced;
}
