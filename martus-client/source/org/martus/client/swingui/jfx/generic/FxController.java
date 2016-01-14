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

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.jfx.generic.data.MartusResourceBundle;
import org.martus.client.swingui.jfx.setupwizard.tasks.AbstractAppTask;
import org.martus.client.swingui.jfx.setupwizard.tasks.TaskWithTimeout;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;

abstract public class FxController implements Initializable
{
	public FxController(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		FxController.useZawgyiFontProperty = mainWindowToUse.getApp().getConfigInfo().getUseZawgyiFontProperty();
	}
	
	abstract public String getFxmlLocation();
	
	protected String getCssName()
	{
		return null;
	}
	
	protected Dimension getPreferredDimension()
	{
		return null;
	}
	
	public void save()
	{
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
	}
	
	public FxShellController getShellController()
	{
		return shellController;
	}
	
	public void setShellController(FxShellController shellControllerToUse)
	{
		shellController = shellControllerToUse;
	}
	
	public VirtualStage getStage()
	{
		if(getShellController() == null)
			return null;
		
		return getShellController().getStage();
	}

	public Parent createContents() throws Exception
	{
		return (Parent)createLoader().load();
	}
	
	private FxmlLoaderWithController createLoader() throws Exception
	{
		URL resourceUrl = getBestFxmlLocation();
		
		return new FxmlLoaderWithController(this, resourceUrl);
	}

	private URL getBestFxmlLocation() throws Exception
	{
		String fxmlLocation = getFxmlLocation();
		return getBestFxmlLocation(fxmlLocation);
	}

	public URL getBestFxmlLocation(String fxmlLocation) throws Exception
	{
		File fxmlDir = getApp().getFxmlDirectory();
		URL fxmlURL = getBestFile(fxmlDir, fxmlLocation);
		if(fxmlURL == null)		
			throw new ResourceNotFoundException("Couldn't find " + fxmlLocation);
		return fxmlURL;
	}

	public static URL getBestFile(File fxmlDir, String fileLocation) throws Exception
	{
		File fxmlFile = new File(fxmlDir, fileLocation);
		if (fxmlFile.exists())
		{
			MartusLogger.log("Loading FX file from disk:" + fileLocation);
			return fxmlFile.toURI().toURL();
		}

		return FxController.class.getResource("/org/martus/client/swingui/jfx/" + fileLocation);
	}		
	
	public static class ResourceNotFoundException extends Exception
	{
		public ResourceNotFoundException(String message)
		{
			super(message);
		}
	}
	
	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}
	
	public MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}
	
	public MartusApp getApp()
	{
		return getMainWindow().getApp();
	}

	public MartusCrypto getSecurity()
	{
		return getApp().getSecurity();
	}

	public void showNotifyDialog(String baseTag)
	{
		String noExtraMessage = "";
		Map tokenReplacement = null;
		showNotifyDialog(baseTag, noExtraMessage, tokenReplacement);
	}
	
	public void showNotifyDialog(String baseTag, Map tokenReplacement)
	{
		String noExtraMessage = "";
		showNotifyDialog(baseTag, noExtraMessage, tokenReplacement);
	}

	public void showNotifyDialog(String baseTag, String extraMessage)
	{
		Map noTokenReplacement = null;
		showNotifyDialog(baseTag, extraMessage, noTokenReplacement);
	}

	public void showNotifyDialog(String baseTag, String extraMessage, Map tokenReplacement)
	{
		++notifyDialogDepth;
		try
		{
			PopupNotifyController popupController = new PopupNotifyController(getMainWindow(), baseTag);
			popupController.setExtraMessage(extraMessage);
			popupController.setTokenReplacement(tokenReplacement);
			showControllerInsideModalDialog(popupController);
			--notifyDialogDepth;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			if(notifyDialogDepth > 3)
			{
				getMainWindow().exitWithoutSavingState();
			}
			showNotifyDialog("UnexpectedError");
		}
	}
	
	public void showDialogWithNoButtons(String titleTag, FxController contentController)
	{
		DialogShellController dialogShellController = new DialogWithNoButtonsShellController(getMainWindow(), contentController, titleTag);
		doAction(dialogShellController);
	}
	
	public void showDialogWithClose(String titleTag, FxController contentController)
	{
		DialogWithCloseShellController dialogShellController = new DialogWithCloseShellController(getMainWindow(), contentController, titleTag);
		doAction(dialogShellController);
	}
	
	public boolean showConfirmationDialog(String tag)
	{
		String message = buildConfirmationMessage(tag);
		return showConfirmationDialog(tag, message);
	}

	public String buildConfirmationMessage(String tag)
	{
		String message = getLocalization().getFieldLabel("confirm" + tag + "cause");
		message += "\n\n";
		message += getLocalization().getFieldLabel("confirm" + tag + "effect");
		message += "\n\n";
		message += getLocalization().getFieldLabel("confirmquestion");
		return message;
	}

	public boolean showOkCancelConfirmationDialog(String titleTag, String messageTag)
	{
		String message = getLocalization().getFieldLabel(messageTag);
		
		return showConfirmationDialog(titleTag, EnglishCommonStrings.OK, EnglishCommonStrings.CANCEL, message);
	}
	
	public boolean showConfirmationDialog(String titleTag, String message)
	{
		return showConfirmationDialog(titleTag, EnglishCommonStrings.YES, EnglishCommonStrings.NO, message);
	}

	public boolean showConfirmationDialog(String titleTag, String yesButtonTag, String noButtonTag, String message)
	{
		FxController mainAreaController = new FxTextPaneController(getMainWindow(), message);
		return showModalYesNoDialog(titleTag,  yesButtonTag, noButtonTag, mainAreaController);
	}

	public boolean showConfirmationDialog(String dialogTag, FxController mainAreaController)
	{
		return showModalYesNoDialog(dialogTag,  EnglishCommonStrings.YES, EnglishCommonStrings.NO, mainAreaController);
	}

	public boolean showModalYesNoDialog(String titleTag, String yesButtonTag, String noButtonTag, FxController mainAreaController)
	{
		try
		{
			MartusLocalization localization = getLocalization();
			String titleText = localization.getWindowTitle(titleTag);
			String yesButtonText = localization.getButtonLabel(yesButtonTag);
			String noButtonText = localization.getButtonLabel(noButtonTag);
			PopupConfirmationController popupController = new PopupConfirmationController(getMainWindow(), titleText, yesButtonText, noButtonText, mainAreaController);
			showControllerInsideModalDialog(popupController);
			return popupController.wasYesPressed();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
		return false;
	}

	public void logAndNotifyUnexpectedError(Exception e)
	{
		VirtualStage stage = getStage();
		if(stage != null)
			stage.logAndNotifyUnexpectedError(e);
		else
			MartusLogger.logException(e);
	}
	
	public void showBusyDialog(String message, Task task) throws Exception
	{
		FxPopupController popupController = new FxBusyController(getMainWindow(), message, task);
		showControllerInsideModalDialog(popupController);
	}

	public void showBusyDialogWithCancel(String message, Task task) throws Exception
	{
		FxBusyControllerWithCancel popupController = new FxBusyControllerWithCancel(getMainWindow(), message, task);
		showControllerInsideModalDialog(popupController);
		if(popupController.didUserCancel())
			throw new UserCancelledException();
	}

	public void showTimeoutDialog(String message, TaskWithTimeout task) throws Exception
	{
		int maxSeconds = task.getMaxSeconds();
		FxTimeoutController popupController = new FxTimeoutController(getMainWindow(), message, task, maxSeconds);
		showControllerInsideModalDialog(popupController);
		if(popupController.didUserCancel())
			throw new UserCancelledException();
	}

	public void showProgressDialog(String message, AbstractAppTask task) throws Exception
	{
		FxProgressController popupController = new FxProgressController(getMainWindow(), message, task);
		showControllerInsideModalDialog(popupController);
		if(popupController.didUserCancel())
			throw new UserCancelledException();
	}

	public void showControllerInsideModalDialog(FxPopupController controller) throws Exception
	{
		// NOTE: Stage must be constructed before the fxml is loaded, 
		// so the controller has access to its stage during initialize()
		PureFxStage popupStage = new PureFxDialogStage(mainWindow, controller);

		FXMLLoader fl = new FXMLLoader();
		fl.setResources(new MartusResourceBundle(getLocalization()));
		fl.setController(controller);
		URL fxmlUrl = getBestFxmlLocation(controller.getFxmlLocation());
		fl.setLocation(fxmlUrl);
		fl.load();
		Parent root = fl.getRoot();
		
		Scene scene = new Scene(root);
		scene.setNodeOrientation(FxScene.getNodeOrientationBasedOnLanguage());
		File fxmlDir = getApp().getFxmlDirectory();
		FxController.applyStyleSheets(scene.getStylesheets(), fxmlDir, getLocalization().getCurrentLanguageCode(), POPUP_CSS);

		popupStage.setScene(scene);
		showModalPopupStage(popupStage);
		MartusLogger.log("Back from showModalPopupStage");
		if(controller.getThrownException() != null)
		{
		    MartusLogger.log("Re-throwing Exception");
			throw controller.getThrownException();
		}
		if(controller.getThrownThrowable() != null)
		{
		    MartusLogger.log("Re-throwing Throwable");
			throw new Exception(controller.getThrownThrowable());
		}
	}
	
	protected void showModalPopupStage(PureFxStage popupStage)
	{
		popupStage.showAndWait();
	}
	
	public void loadControllerAndEmbedInPane(FxController embeddedContentController, Pane destinationPane) throws Exception
	{
		embeddedContentController.setShellController(getShellController());
		embeddedContentController.setParentController(this);
		Parent createContents = embeddedContentController.createContents();
		destinationPane.getChildren().addAll(createContents);
	}

	private void setParentController(FxController parentControllerToUse)
	{
		parentController = parentControllerToUse;
	}
	
	public FxController getParentController()
	{
		return parentController;
	}
	
	public FxController getTopLevelController()
	{
		FxController top = this;
		while(top.getParentController() != null)
			top = top.getParentController();
		
		return top;
	}
	
	public Button getOkButton()
	{
		return null;
	}

	public void doAction(ActionDoer doer)
	{
		VirtualStage stage = getStage();
		if(stage == null)
			MartusLogger.log("Controller has no stage: " + getClass().getName());
		
		stage.doAction(doer);
	}

	public boolean isDoubleClick(MouseEvent mouseEvent)
	{
	    if(mouseEvent.getButton().equals(MouseButton.PRIMARY))
	    {
		    final int MOUSE_DOUBLE_CLICK = 2;
	    		if(mouseEvent.getClickCount() == MOUSE_DOUBLE_CLICK)
	    			return true;
	    }
	    return false;
	}
	
	static public void applyStyleSheets(ObservableList<String> stylesheets, File directory, String languageCode, String cssLocation) throws Exception
	{
		applyMasterMartusStyleSheets(stylesheets, directory, languageCode);
		if(cssLocation == null)
			return;
		applyPageSpecificStyleSheets(stylesheets, directory, languageCode, cssLocation);
	}
	
	public void updateBurmeseStyleSheets() throws Exception
	{
		ObservableList<String> stylesheets = getStage().getScene().getStylesheets();
		File directory = getApp().getFxmlDirectory();
		FxController.updateBurmeseStyleSheetsIfUsingZawgyi(stylesheets, directory);
	}

	private static void applyMasterMartusStyleSheets(ObservableList<String> stylesheets, File directory,
			String languageCode) throws Exception
	{
		applyPageSpecificStyleSheets(stylesheets, directory, languageCode, MARTUS_CSS);
	}

	private static void applyPageSpecificStyleSheets( ObservableList<String> stylesheets, File directory,
			String languageCode, String cssLocation) throws Exception
	{
		URL englishCssUrl = getBestCss(directory, MartusLocalization.ENGLISH, cssLocation);
		if(englishCssUrl == null)
			throw new ResourceNotFoundException(
					"Couldn't find :" + directory + "/" + cssLocation);
		
		stylesheets.add(englishCssUrl.toExternalForm());
		
		if(!languageCode.equals(MartusLocalization.ENGLISH))
		{
			URL languageCssUrl = getBestCss(directory, languageCode, cssLocation);
			if(languageCssUrl != null)
				stylesheets.add(languageCssUrl.toExternalForm());
		}
		if(!languageCode.equals(MartusLocalization.BURMESE))
			updateBurmeseStyleSheetsIfUsingZawgyi(stylesheets, directory);
	}

	private static void updateBurmeseStyleSheetsIfUsingZawgyi(ObservableList<String> stylesheets, File directory) throws Exception
	{
		updateBurmeseSytleSheetIfUsingZawgyi(stylesheets, directory, MARTUS_CSS);
		updateBurmeseSytleSheetIfUsingZawgyi(stylesheets, directory, POPUP_CSS);
	}

	private static void updateBurmeseSytleSheetIfUsingZawgyi(ObservableList<String> stylesheets, File directory, String styleSheet) throws Exception
	{
		URL burmeseLanguageCssUrl = getBestCss(directory, MartusLocalization.BURMESE, styleSheet);
		if(burmeseLanguageCssUrl == null)
			return;
		String burmeseCSS = burmeseLanguageCssUrl.toExternalForm();
		if(FxController.useZawgyiFontProperty.getValue())
			stylesheets.add(burmeseCSS);
		else
			stylesheets.remove(burmeseCSS);
	}
	
	public static URL getBestCss(File directory, String languageCode,
			String cssLocation) throws Exception
	{
		if(languageCode.equals(MartusLocalization.ENGLISH))
			return getBestFile(directory, "css/" + cssLocation);
		return getBestFile(directory, "css/" + languageCode + "/" + cssLocation);
	}
	
	protected void openLinkInDefaultBrowser(String url)
	{
		try
		{
			Desktop.getDesktop().browse(java.net.URI.create(url));
		} 
		catch (IOException e)
		{
			MartusLogger.logException(e);
		}
	}

	protected void openDefaultEmailApp(String email)
	{
		try
		{
			Desktop desktop = Desktop.getDesktop(); 
			desktop.mail(new URI(email));
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	public static class UserCancelledException extends Exception
	{
	}
	
	public PureFxStage getParentWindow()
	{
		return (PureFxStage)getStage();
	}

	private static final String POPUP_CSS = "Popup.css";
	private static final String MARTUS_CSS = "Martus.css";
	private static Property<Boolean> useZawgyiFontProperty;
	
	private UiMainWindow mainWindow;
	private static int notifyDialogDepth;
	private FxShellController shellController;
	private FxController parentController;
}
