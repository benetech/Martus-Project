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
package org.martus.client.swingui.jfx.landing.general;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.Vector;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;

import javax.swing.SwingUtilities;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.templates.GenericFormTemplates;
import org.martus.client.search.SaneCollator;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiCustomFieldsDlg;
import org.martus.client.swingui.jfx.common.AbstractFxImportFormTemplateController;
import org.martus.client.swingui.jfx.common.FxImportFormTemplateFromMyContactsPopupController;
import org.martus.client.swingui.jfx.common.FxSetupFormTemplateFromNewContactPopupController;
import org.martus.client.swingui.jfx.common.TemplatePropertiesController;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.TableRowData;
import org.martus.client.swingui.jfx.generic.controls.FxButtonTableCellFactory;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.Exceptions.ServerNotCompatibleException;
import org.martus.common.FieldCollection.CustomFieldsParseException;
import org.martus.common.MartusLogger;
import org.martus.common.XmlFormTemplateLoader;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.fieldspec.CustomFieldError;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.util.TokenReplacement;
import org.martus.util.UnicodeReader;
import org.martus.util.xml.SimpleXmlParser;

public class ManageTemplatesController extends FxController
{
	public ManageTemplatesController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		
		try
		{
			initializeAvailableTab();
			initializeAddTab();
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private void initializeAvailableTab()
	{
		templateNameColumn.setEditable(false);
        templateNameColumn.setCellValueFactory(new PropertyValueFactory<ManageTemplatesTableRowData,String>(ManageTemplatesTableRowData.DISPLAYABLE_TEMPLATE_NAME));
		Comparator<String> sorter = new SaneCollator(getLocalization().getCurrentLanguageCode());
        templateNameColumn.setComparator(sorter);

        Image trashImage = new Image(TRASH_IMAGE_PATH);
        templateDeleteColumn.setCellFactory(FxButtonTableCellFactory.createNarrowButtonTableCell(trashImage, () -> deleteSelectedTemplate()));
        templateDeleteColumn.setCellValueFactory(new PropertyValueFactory<TableRowData,Boolean>(ManageTemplatesTableRowData.CAN_DELETE_NAME));
        
        Image uploadImage = new Image(UPLOAD_IMAGE_PATH);
        templateUploadColumn.setCellFactory(FxButtonTableCellFactory.createNarrowButtonTableCell(uploadImage, () -> uploadSelectedTemplate()));
        templateUploadColumn.setCellValueFactory(new PropertyValueFactory<TableRowData,Boolean>(ManageTemplatesTableRowData.CAN_UPLOAD_NAME));
        
        Image exportImage = new Image(EXPORT_IMAGE_PATH);
        templateExportColumn.setCellFactory(FxButtonTableCellFactory.createNarrowButtonTableCell(exportImage, () -> exportSelectedTemplate()));
        templateExportColumn.setCellValueFactory(new PropertyValueFactory<TableRowData,Boolean>(ManageTemplatesTableRowData.CAN_EXPORT_NAME));
        
        Image editImage = new Image(EDIT_IMAGE_PATH);
        templateEditColumn.setCellFactory(FxButtonTableCellFactory.createNarrowButtonTableCell(editImage, () -> editSelectedTemplate()));
        templateEditColumn.setCellValueFactory(new PropertyValueFactory<TableRowData,Boolean>(ManageTemplatesTableRowData.CAN_EDIT_NAME));
        
        populateAvailableTemplatesTable();

        availableTemplatesTable.getSortOrder().clear();
		availableTemplatesTable.getSortOrder().add(templateNameColumn);
	}
	
	protected void deleteSelectedTemplate()
	{
		try
		{
			ManageTemplatesTableRowData selected = availableTemplatesTable.getSelectionModel().getSelectedItem();
			String displayableName = selected.getDisplayableTemplateName();
			
			String messageTemplate = getLocalization().getFieldLabel("confirmDeleteTemplate");
			String message = TokenReplacement.replaceToken(messageTemplate, "#Name#", displayableName);
			if(!showConfirmationDialog("Templates", message))
				return;

			getBulletinStore().deleteFormTemplate(selected.getRawTemplateName());
			
			populateAvailableTemplatesTable();
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	protected void uploadSelectedTemplate()
	{
		try
		{
			ManageTemplatesTableRowData selected = availableTemplatesTable.getSelectionModel().getSelectedItem();
			String rawTitle = selected.getRawTemplateName();
			FormTemplate template = getBulletinStore().getFormTemplate(rawTitle);

			String displayableTitle = selected.getDisplayableTemplateName();
			String rawWhich = getLocalization().getFieldLabel("WhichTemplate");
			String which = TokenReplacement.replaceToken(rawWhich, "#TemplateTitle#", displayableTitle);
			String cause = getLocalization().getFieldLabel("confirmUploadPublicTemplateToServerWarningcause");
			String effect = getLocalization().getFieldLabel("confirmUploadPublicTemplateToServerWarningeffect");
			String message = which + "\n\n" + cause + "\n\n" + effect;
			if(!showConfirmationDialog("confirmUploadPublicTemplateToServerWarning", message))
				return;
			
			uploadTemplateToServer(template);
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	public void uploadTemplateToServer(FormTemplate template)
	{
		try
		{
			getApp().putFormTemplateOnServer(template);
		}
		catch (ServerNotCompatibleException e)
		{
			showNotifyDialog("ServerNotCompatible");
		}
		catch (ServerNotAvailableException e)
		{
			showNotifyDialog("ServerNotAvailable");
		} 
		catch (Exception e)
		{
			showNotifyDialog("ErrorSavingTemplateToServer");
		}
	}

	protected void exportSelectedTemplate()
	{
		try
		{
			ManageTemplatesTableRowData selected = availableTemplatesTable.getSelectionModel().getSelectedItem();
			String rawTitle = selected.getRawTemplateName();
			FormTemplate template = getBulletinStore().getFormTemplate(rawTitle);
			exportTemplate(getMainWindow(), template);
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	private void exportTemplate(UiMainWindow mainWindowToUse, FormTemplate template) throws Exception
	{
		doAction(new ExportTemplateAction(getMainWindow(), template));
	}

	protected void editSelectedTemplate()
	{
		try
		{
			ManageTemplatesTableRowData selected = availableTemplatesTable.getSelectionModel().getSelectedItem();
			String rawTitle = selected.getRawTemplateName();
			FormTemplate template = getBulletinStore().getFormTemplate(rawTitle);
			String emptyMessage = "";
			boolean keepExistingTemplate = false;
			if(ManageTemplatesController.editTemplate(template, emptyMessage, keepExistingTemplate, this))
				populateAvailableTemplatesTable();
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	static public boolean editTemplate(FormTemplate template, String message, boolean keepExistingTemplate, FxController mainController) throws Exception
	{
		TemplatePropertiesController controller = new TemplatePropertiesController(mainController.getMainWindow(), template);
		controller.setMessage(message);
		if(mainController.showModalYesNoDialog("TemplateEditor", EnglishCommonStrings.OK, EnglishCommonStrings.CANCEL, controller))
		{
			String oldTitle = template.getTitle();
			String newTitle = controller.getTemplateTitle();
			boolean willReplaceExistingCopy = !newTitle.equals(oldTitle) && !keepExistingTemplate;

			template.setTitle(newTitle);
			template.setDescription(controller.getTemplateDescription());

			ClientBulletinStore store = mainController.getApp().getStore();
			store.saveNewFormTemplate(template);
			if(willReplaceExistingCopy)
				store.deleteFormTemplate(oldTitle);
			return true;
		}
		return false;
	}

	private void populateAvailableTemplatesTable()
	{
		TableViewSelectionModel<ManageTemplatesTableRowData> selectionModel = availableTemplatesTable.selectionModelProperty().getValue();
		ManageTemplatesTableRowData selected = selectionModel.getSelectedItem();
		
		ClientBulletinStore store = getBulletinStore();
		ObservableSet<String> templateNamesSet = store.getAvailableTemplates();
		ObservableList<ManageTemplatesTableRowData> templateRows = FXCollections.observableArrayList();
		templateNamesSet.forEach(name -> templateRows.add(new ManageTemplatesTableRowData(name, getLocalization())));

		availableTemplatesTable.setItems(templateRows);
		availableTemplatesTable.sort();
		
		selectionModel.clearSelection();
		selectionModel.select(selected);
	}
	
	private void initializeAddTab() throws Exception
	{
		templateToAddProperty = new SimpleObjectProperty<FormTemplate>();
		ReadOnlyObjectProperty<FormTemplate> selectedGenericTemplateProperty = genericChoiceBox.getSelectionModel().selectedItemProperty();
		selectedGenericTemplateProperty.addListener(new GenericTemplateSelectedHandler());
		genericChoiceBox.visibleProperty().bind(genericRadioButton.selectedProperty());
		genericChoiceBox.setItems(GenericFormTemplates.getDefaultFormTemplateChoices(getSecurity()));

		ReadOnlyObjectProperty<AbstractFxImportFormTemplateController> selectedDownloadTypeProperty = downloadChoiceBox.getSelectionModel().selectedItemProperty();
		selectedDownloadTypeProperty.addListener(new DownloadTypeSelectedHandler());
		downloadChoiceBox.visibleProperty().bind(downloadRadioButton.selectedProperty());
		downloadChoiceBox.setItems(getImportTemplateChoices(getMainWindow()));
		
		chooseFileButton.visibleProperty().bind(importFileRadioButton.selectedProperty());
		
		genericRadioButton.setSelected(true);
		
		addTemplateButton.disableProperty().bind(Bindings.isNull(templateToAddProperty));

		templateNameToBeAdded.visibleProperty().bind(Bindings.isNull(templateToAddProperty).not());
		templateNameToBeAdded.textProperty().bind(templateToAddProperty.asString());
	}
	
	public static ObservableList<AbstractFxImportFormTemplateController> getImportTemplateChoices(UiMainWindow mainWindow) throws Exception
	{
		Vector<AbstractFxImportFormTemplateController> choices = new Vector<AbstractFxImportFormTemplateController>();
		if (!mainWindow.getApp().getAllHQKeys().isEmpty())
			choices.add(new FxImportFormTemplateFromMyContactsPopupController(mainWindow));
		
		choices.add(new FxSetupFormTemplateFromNewContactPopupController(mainWindow));

		return FXCollections.observableArrayList(choices);
	}
	
	protected class GenericTemplateSelectedHandler implements ChangeListener<FormTemplate>
	{
		@Override
		public void changed(ObservableValue<? extends FormTemplate> observable, FormTemplate oldTemplate, FormTemplate newTemplate)
		{
			updateTemplateFromGeneric();
		}
		
	}

	protected class DownloadTypeSelectedHandler implements ChangeListener<AbstractFxImportFormTemplateController>
	{
		@Override
		public void changed(ObservableValue<? extends AbstractFxImportFormTemplateController> observable, AbstractFxImportFormTemplateController oldValue, AbstractFxImportFormTemplateController newValue)
		{
			if(newValue == null)
				return;

			try
			{
				showControllerInsideModalDialog(newValue);
				FormTemplate downloadedTemplate = newValue.getSelectedFormTemplate();
				updateTemplateFromDownloaded(downloadedTemplate);
				clearDownloadSelection();
			}
			catch(Exception e)
			{
				logAndNotifyUnexpectedError(e);
			}
		}
	}

	protected void clearDownloadSelection()
	{
		downloadChoiceBox.getSelectionModel().clearSelection();
	}
	
	protected void updateTemplateFromDownloaded(FormTemplate downloadedTemplate)
	{
		templateToAddProperty.setValue(downloadedTemplate);
		logTemplateToBeAdded();
	}
	
	private ClientBulletinStore getBulletinStore()
	{
		return getApp().getStore();
	}
	
	private void logTemplateToBeAdded()
	{
		MartusLogger.log("Ready to add template: " + getTitleOfTemplateToBeAdded());
	}

	private StringProperty getTitleOfTemplateToBeAdded()
	{
		FormTemplate template = templateToAddProperty.getValue();
		String title = getLocalization().getFieldLabel("NoFormTemplateTitle");
		if(template != null)
			title = template.getTitle();
		
		return new SimpleStringProperty(title);
	}
	
	protected void updateTemplateFromGeneric()
	{
		FormTemplate selected = genericChoiceBox.getSelectionModel().getSelectedItem();
		templateToAddProperty.setValue(selected);
		logTemplateToBeAdded();
	}
		
	@Override
	public String getFxmlLocation()
	{
		return "landing/general/ManageTemplates.fxml";
	}
	
	@FXML
	private void onChooseGeneric(ActionEvent event)
	{
		updateTemplateFromGeneric();
	}

	@FXML
	private void onChooseFromServer(ActionEvent event)
	{
		templateToAddProperty.setValue(null);
		downloadChoiceBox.getSelectionModel().clearSelection();
		logTemplateToBeAdded();
	}
	
	@FXML
	private void onChooseFromFile(ActionEvent event)
	{
		templateToAddProperty.setValue(null);
		logTemplateToBeAdded();
	}
	
	@FXML
	private void onImportFromFile(ActionEvent event)
	{
		doAction(new ImportTemplateAction(this));
	}

	public void importFormTemplateFromMctFile(File templateFile) throws Exception
	{
		FormTemplate importedTemplate = new FormTemplate();
		if(!importedTemplate.importTemplate(templateFile, getSecurity()))
		{
			showNotifyDialog("ErrorImportingCustomizationTemplate");
			return;
		}
		
		templateToAddProperty.setValue(importedTemplate);
		logTemplateToBeAdded();
	}

	public void importXmlFormTemplate(File templateFile)
	{
		Vector errors = new Vector();
		try
		{
			String xmlAsString = importXmlAsString(templateFile);
			XmlFormTemplateLoader loader = new XmlFormTemplateLoader();
			SimpleXmlParser.parse(loader, xmlAsString);
			FormTemplate importedTemplate = loader.getFormTemplate();

			if (importedTemplate.isvalidTemplateXml())
			{
				templateToAddProperty.setValue(importedTemplate);
			}
			
			errors.addAll(importedTemplate.getErrors());
			logTemplateToBeAdded();
		}
		catch(IOException e)
		{
			errors.add(CustomFieldError.errorIO(e.getMessage()));
		}
		catch(MartusSignatureException e)
		{
			errors.add(CustomFieldError.errorSignature());
		}
		catch(AuthorizationFailedException e)
		{
			errors.add(CustomFieldError.errorUnauthorizedKey());
		}
		catch(CustomFieldsParseException e)
		{
			errors.add(CustomFieldError.errorParseXml(e.getMessage()));
		}
		catch (Exception e)
		{
			errors.add(CustomFieldError.errorParseXml(e.getMessage()));
		}
		
		if (!errors.isEmpty())
			SwingUtilities.invokeLater(() -> safelyPopulateView(errors));
	}
	
	private void safelyPopulateView(Vector errors)
	{
		UiFontEncodingHelper fontHelper = new UiFontEncodingHelper(getMainWindow().getDoZawgyiConversion());
		UiCustomFieldsDlg.displayXMLError(getMainWindow(), fontHelper, errors);
	}

	private String importXmlAsString(File tempFormTemplateFile) throws Exception 
	{
		UnicodeReader reader = new UnicodeReader(tempFormTemplateFile);
		try
		{
			return reader.readAll();
		}
		finally
		{
			reader.close();
		}
	}
	
	@FXML
	private void onAvailableTabOkButton(ActionEvent event)
	{
		getStage().close();
	}
	
	@FXML
	private void onAdd(ActionEvent event)
	{
		try
		{
			FormTemplate templateToAdd = templateToAddProperty.getValue();
			if(templateToAdd == null)
			{
				showNotifyDialog("NoTemplateSelectedToAdd");
				return;
			}
			ObservableSet<String> existingTemplateTitles = getBulletinStore().getAvailableTemplates();
			boolean doesTemplateExist = existingTemplateTitles.contains(templateToAdd.getTitle());
			if(doesTemplateExist)
			{
				boolean keepExistingTemplate = true;
				if(ManageTemplatesController.editTemplate(templateToAdd, getLocalization().getFieldLabel("ImportTemplateWhichAlreadyExists"), keepExistingTemplate, this))
					updateTable();
				return;
			}
			getBulletinStore().saveNewFormTemplate(templateToAdd);
			updateTable();
		}
		catch(Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	public void updateTable()
	{
		populateAvailableTemplatesTable();
		templateToAddProperty.setValue(null);
		
		tabPane.selectionModelProperty().get().select(availableTemplatesTab);
		availableTemplatesTable.selectionModelProperty().get().clearSelection();
		availableTemplatesTable.scrollTo(0);
	}

	final private String TRASH_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/trash.png";
	final private String UPLOAD_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/upload.png";
	final private String EXPORT_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/export.png";
	final private String EDIT_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/edit.png";
	
	@FXML
	private TabPane tabPane;
	
	@FXML
	private Tab availableTemplatesTab;
	
	@FXML
	private TableView<ManageTemplatesTableRowData> availableTemplatesTable;
	
	@FXML
	protected TableColumn<ManageTemplatesTableRowData, String> templateNameColumn;

	@FXML
	protected TableColumn<TableRowData, Boolean> templateDeleteColumn;
	
	@FXML
	protected TableColumn<TableRowData, Boolean> templateUploadColumn;
	
	@FXML
	protected TableColumn<TableRowData, Boolean> templateExportColumn;
	
	@FXML
	protected TableColumn<TableRowData, Boolean> templateEditColumn;
	
	@FXML
	private RadioButton genericRadioButton;
	
	@FXML
	private ChoiceBox<FormTemplate> genericChoiceBox;
	
	@FXML
	private RadioButton downloadRadioButton;
	
	@FXML
	private ChoiceBox downloadChoiceBox;
	
	@FXML
	private RadioButton importFileRadioButton;
	
	@FXML
	private Button chooseFileButton;
	
	@FXML
	private Button addTemplateButton;
	
	@FXML
	private Label templateNameToBeAdded;
	
	private SimpleObjectProperty<FormTemplate> templateToAddProperty;
}
