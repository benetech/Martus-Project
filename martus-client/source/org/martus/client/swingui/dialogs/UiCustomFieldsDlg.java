/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2014, Beneficent
Technology, Inc. (The Benetech Initiative).

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

package org.martus.client.swingui.dialogs;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.martus.client.core.CustomFieldsDuplicateLabelChecker;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.filefilters.MCTFileFilter;
import org.martus.client.swingui.jfx.common.ExportTemplateDoer;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.Exceptions.ServerNotCompatibleException;
import org.martus.common.FieldCollection;
import org.martus.common.FieldDeskKeys;
import org.martus.common.FieldSpecCollection;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.fieldspec.CustomFieldError;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.FormTemplate.FutureVersionException;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTextArea;
import org.martus.swing.UiTextField;
import org.martus.swing.UiVBox;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.TokenReplacement;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;


public class UiCustomFieldsDlg extends JDialog
{
	public UiCustomFieldsDlg(UiMainWindow owner, FormTemplate existingTemplate)
	{
		super(owner.getSwingFrame(), "", true);
		mainWindow = owner; 
		security = mainWindow.getApp().getSecurity();		
		String baseTag = "CustomFields";
		MartusLocalization localization = owner.getLocalization();
		setTitle(localization.getWindowTitle("input" + baseTag));
		fontHelper = new UiFontEncodingHelper(mainWindow.getDoZawgyiConversion());

		UiWrappedTextArea label = new UiWrappedTextArea(localization.getFieldLabel("input" + baseTag + "Info"));

		JButton defaults = new UiButton(localization.getButtonLabel("customDefault"));
		defaults.addActionListener(new CustomDefaultHandler());
		JButton importTemplate = new UiButton(localization.getButtonLabel("customImport"));
		importTemplate.addActionListener(new ImportTemplateHandler());
		JButton exportTemplate = new UiButton(localization.getButtonLabel("customExport"));
		exportTemplate.addActionListener(new ExportTemplateHandler());
		JButton sendTemplateToServer = new UiButton(localization.getButtonLabel("customSendToServer"));
		sendTemplateToServer.addActionListener(new SendTemplateToServerHandler());
		
		

		
		JButton ok = new UiButton(localization.getButtonLabel("input" + baseTag + "ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(new CancelHandler());
		JButton help = new UiButton(localization.getButtonLabel("customHelp"));
		help.addActionListener(new CustomHelpHandler());

		Box vBox = new UiVBox();
		vBox.add(defaults);
		vBox.add(exportTemplate);
		vBox.add(importTemplate);
		vBox.add(sendTemplateToServer);
		
		Box buttons = Box.createHorizontalBox();
		Component buttonsToAdd[] = {vBox, Box.createHorizontalGlue(), ok, cancel, help};  
		Utilities.addComponentsRespectingOrientation(buttons, buttonsToAdd);
		
		topSectionXmlTextArea = createXMLTextArea(existingTemplate.getTopFields());
		topSectionXmlTextArea.setCaretPosition(0);
		UiScrollPane topSectionTextPane = new UiScrollPane(topSectionXmlTextArea);

		bottomSectionXmlTextArea = createXMLTextArea(existingTemplate.getBottomFields());
		bottomSectionXmlTextArea.setCaretPosition(0);
		UiScrollPane bottomSectionTextPane = new UiScrollPane(bottomSectionXmlTextArea);

		UiLabel titleLabel = new UiLabel(localization.getFieldLabel("inputCustomFieldsTitle"));
		titleField = new UiTextField();
		titleField.setText(existingTemplate.getTitle());
		Box titleBox = createLabelAndTextFieldBox(titleLabel, titleField);

		UiLabel descriptionLabel = new UiLabel(localization.getFieldLabel("inputCustomFieldsDescription"));
		descriptionField = new UiTextField();
		descriptionField.setText(existingTemplate.getDescription());
		Box descriptionBox = createLabelAndTextFieldBox(descriptionLabel, descriptionField);
		
		JPanel customFieldsPanel = new JPanel();
		customFieldsPanel.setBorder(new EmptyBorder(10,10,10,10));
		customFieldsPanel.setLayout(new BoxLayout(customFieldsPanel, BoxLayout.Y_AXIS));

		customFieldsPanel.add(titleBox);
		customFieldsPanel.add(descriptionBox);
		customFieldsPanel.add(label);
		Box topBox = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(topBox, new Component[] {new UiLabel(localization.getFieldLabel("CustomXMLTopSection")), Box.createHorizontalGlue()});
		customFieldsPanel.add(topBox);
		customFieldsPanel.add(topSectionTextPane);
		Box bottomBox = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(bottomBox, new Component[] {new UiLabel(localization.getFieldLabel("CustomXMLBottomSection")), Box.createHorizontalGlue()});
		customFieldsPanel.add(bottomBox);
		customFieldsPanel.add(bottomSectionTextPane);
		customFieldsPanel.add(new UiLabel(" "));
		customFieldsPanel.add(buttons);
	
		getContentPane().add(customFieldsPanel);
		getRootPane().setDefaultButton(ok);
		Utilities.packAndCenterWindow(this);
		setResizable(true);
	}

	private Box createLabelAndTextFieldBox(UiLabel label, UiTextField textField)
	{
		Box box = Box.createHorizontalBox();
		box.add(label);
		box.add(Box.createHorizontalGlue());
		box.add(new UiLabel(" "));
		box.add(Box.createHorizontalGlue());
		box.add(textField);
		return box;
	}
	
	public void setFocusToInputField()
	{
		topSectionXmlTextArea.requestFocus();
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			try 
			{
				titleResult = titleField.getText();
				if(titleResult.length() == 0)
				{
					mainWindow.notifyDlg("Cannot save template without a title (JavaFX won't have this error dialog");
					return;
				}

				String topText = topSectionXmlTextArea.getText();
				String bottomText = bottomSectionXmlTextArea.getText();
				topText = fontHelper.getStorable(topText);
				bottomText = fontHelper.getStorable(bottomText);
				if(!validateXml(topText, bottomText))
				 	return;
				if(!checkForDuplicateLabels())
					return;
				topSectionXmlResult = topText;
				bottomSectionXmlResult = bottomText;
				descriptionResult = descriptionField.getText();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				mainWindow.notifyDlg("UnexpectedError");
				return;
			}
			dispose();
		}
		
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}
	
	
	class CustomDefaultHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			titleResult = "";
			descriptionResult = "";
			topSectionXmlResult = "";
			bottomSectionXmlResult = "";
			dispose();
		}
	}
	
	class ImportTemplateHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			FileFilter filter = new MCTFileFilter(mainWindow.getLocalization());
			File importFile = mainWindow.showFileOpenDialog("ImportCustomization", filter);
			if(importFile == null)
				return;

			FormTemplate template = new FormTemplate();
			
			
			try
			{
				boolean imported = importFormTemplate(importFile, template);
				if(imported)
				{
					Vector authorizedKeys = getAuthorizedKeys();
					String signedBy = template.getSignedBy();
					if(!authorizedKeys.contains(signedBy))
						if(!mainWindow.confirmDlg("ImportingCustomizationUnknownSigner"))
							return;
				}
				
				if(imported)
				{
					topSectionXmlTextArea.setText(template.getTopSectionXml());
					bottomSectionXmlTextArea.setText(template.getBottomSectionXml());
					titleField.setText(template.getTitle());
					descriptionField.setText(template.getDescription());
					String signedBy = template.getSignedBy();
					HashMap replacement = new HashMap();
					replacement.put("#CreatedBy#", getCreatedBy(signedBy));
					mainWindow.notifyDlg("ImportingCustomizationTemplateSuccess", replacement);
					return;
				}
				displayXMLError(template);
			}
			catch(FutureVersionException e)
			{
				mainWindow.notifyDlg("ErrorImportingCustomizationTemplateFuture");
			}
			catch(Exception e)
			{
				mainWindow.unexpectedErrorDlg(e);
			}
			mainWindow.notifyDlg("ErrorImportingCustomizationTemplate");
		}

		private boolean importFormTemplate(File importFile, FormTemplate template) throws IOException, FutureVersionException
		{
			FileInputStreamWithSeek inputStream = new FileInputStreamWithSeek(importFile);
			try
			{
				return template.importTemplate(security, inputStream);
			}
			finally
			{
				inputStream.close();
			}
		}

		private Object getCreatedBy(String signedBy) throws Exception
		{
			MiniLocalization localization = mainWindow.getLocalization();
			
			if(signedBy.equals(security.getPublicKeyString()))
				return localization.getFieldLabel("TemplateCreatedByThisAccount");
			
			HeadquartersKeys hqKeys = getHeadquartersKeys();
			if(hqKeys.containsKey(signedBy))
			{
				String label = hqKeys.getLabelIfPresent(signedBy);
				String template = localization.getFieldLabel("TemplateCreatedByHeadquarters");
				return TokenReplacement.replaceToken(template, "#Name#", label);
			}
			
			FieldDeskKeys fdKeys = getFieldDeskKeys();
			if(fdKeys.containsKey(signedBy))
			{
				String label = fdKeys.getLabelIfPresent(signedBy);
				String template = localization.getFieldLabel("TemplateCreatedByFieldDesk");
				return TokenReplacement.replaceToken(template, "#Name#", label);
			}
			
			return localization.getFieldLabel("TemplateCreatedByUnknown");
		}

		private Vector getAuthorizedKeys()
		{
			Vector authorizedKeys = new Vector();
			authorizedKeys.add(security.getPublicKeyString());
			HeadquartersKeys hqKeys = getHeadquartersKeys();
			for(int i = 0; i < hqKeys.size(); ++i)
			{
				authorizedKeys.add(hqKeys.get(i).getPublicKey());
			}
			FieldDeskKeys fdKeys = getFieldDeskKeys();
			for(int i = 0; i < fdKeys.size(); ++i)
			{
				authorizedKeys.add(fdKeys.get(i).getPublicKey());
			}
			
			return authorizedKeys;
		}

		public HeadquartersKeys getHeadquartersKeys()
		{
			return mainWindow.getApp().getAllHQKeysWithFallback();
		}

		public FieldDeskKeys getFieldDeskKeys()
		{
			return mainWindow.getApp().getFieldDeskKeys();
		}
	}

	
	
	class ExportTemplateHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			try
			{
				String title = titleField.getText();
				String description = descriptionField.getText();
				String topXml = topSectionXmlTextArea.getText();
				String bottomXml = bottomSectionXmlTextArea.getText();
	
				if(!validateXml(topXml, bottomXml))
				{
					mainWindow.notifyDlg("ErrorExportingCustomizationTemplate");
					return;
				}
				if(!checkForDuplicateLabels())
					return;
				
				FormTemplate template = createTemplate(title, description, topXml, bottomXml);

				ExportTemplateDoer doer = new ExportTemplateDoer(mainWindow, template);
				doer.doAction();
			} 
			catch (Exception e)
			{
				MartusLogger.logException(e);
				mainWindow.notifyDlg("ErrorExportingCustomizationTemplate");
			}
		}

		private FormTemplate createTemplate(String title, String description, String topXml, String bottomXml) throws Exception
		{
			FieldSpecCollection top = FieldCollection.parseXml(topXml);
			FieldSpecCollection bottom = FieldCollection.parseXml(bottomXml);
			FormTemplate template = new FormTemplate(title, description, top, bottom);
			return template;
		}
	}

	class SendTemplateToServerHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			String topXml = topSectionXmlTextArea.getText();
			String bottomXml = bottomSectionXmlTextArea.getText();
			if(!validateXml(topXml, bottomXml))
			{
				mainWindow.notifyDlg("ErrorExportingCustomizationTemplate");
				return;
			}
			if(!checkForDuplicateLabels())
				return;
			if(!mainWindow.confirmDlg("UploadPublicTemplateToServerWarning"))
				return;
			try
			{
				FieldSpecCollection specTop = new FieldCollection(FieldCollection.parseXml(topXml)).getSpecs();
				FieldSpecCollection specBottom = new FieldCollection(FieldCollection.parseXml(bottomXml)).getSpecs();
				FormTemplate template1 = new FormTemplate(titleField.getText(), descriptionField.getText(), specTop, specBottom);
				mainWindow.getApp().putFormTemplateOnServer(template1);
				mainWindow.notifyDlg("TemplateSavedToServer");
			} 
			catch (ServerNotCompatibleException e)
			{
				mainWindow.notifyDlgBeep("ServerNotCompatible");
			}
			catch (ServerNotAvailableException e)
			{
				mainWindow.notifyDlgBeep("ServerNotAvailable");
			} 
			catch (Exception e)
			{
				mainWindow.notifyDlgBeep("ErrorSavingTemplateToServer");
			}
		}
	}
	
	
	class CustomHelpHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			MartusLocalization localization = mainWindow.getLocalization();
			String message = localization.getFieldLabel("CreateCustomFieldsHelp1");
			message += localization.getFieldLabel("CreateCustomFieldsHelp2");
			message += localization.getFieldLabel("CreateCustomFieldsHelp2b");
			String examples = localization.getFieldLabel("CreateCustomFieldsHelp3");
			UiTextArea xmlExamples = createXMLTextArea(examples);
			formatTextArea(xmlExamples);
			UiScrollPane pane = createScrollPane(xmlExamples);

			new UiShowScrollableTextDlg(mainWindow, "CreateCustomFieldsHelp", EnglishCommonStrings.OK, MtfAwareLocalization.UNUSED_TAG, MtfAwareLocalization.UNUSED_TAG, message, pane);
		}
	}
	
	public boolean validateXml(String xmlToValidateTopSection, String xmlToValidateBottomSection)
	{
		FormTemplate template = new FormTemplate();
		if(template.isvalidTemplateXml(xmlToValidateTopSection, xmlToValidateBottomSection))
			return true;

		displayXMLError(template); 
		return false;
	}
	
	
	

	void displayXMLError(FormTemplate template)
	{
		Vector errors = template.getErrors();
		displayXMLError(mainWindow, fontHelper, errors);
	}
	
	public static void displayXMLError(UiMainWindow mainWindow, UiFontEncodingHelper fontHelper, Vector errors)
	{
		if(errors == null)
			return;
		
		StringBuilder errorMessage = createErrorMessage(mainWindow, fontHelper, errors);

		String errorDescription = mainWindow.getLocalization().getFieldLabel("ErrorCustomFields");
		UiTextArea specificErrors = createXMLTextArea(errorMessage.toString());
		formatTextArea(specificErrors);
		UiScrollPane specificErrorsPane = createScrollPane(specificErrors);

		new UiShowScrollableTextDlg(mainWindow,"ErrorCustomFields", EnglishCommonStrings.OK, MtfAwareLocalization.UNUSED_TAG, MtfAwareLocalization.UNUSED_TAG, errorDescription, specificErrorsPane);
	}
	
	public static StringBuilder createErrorMessage(UiMainWindow mainWindow, Vector errors)
	{
		UiFontEncodingHelper fontHelper = new UiFontEncodingHelper(mainWindow.getDoZawgyiConversion());

		return createErrorMessage(mainWindow, fontHelper, errors);
	}
	

	private static StringBuilder createErrorMessage(UiMainWindow mainWindow, UiFontEncodingHelper fontHelper, Vector errors)
	{
		String header1 = mainWindow.getLocalization().getFieldLabel("ErrorCustomFieldHeader1");
		String header2 = mainWindow.getLocalization().getFieldLabel("ErrorCustomFieldHeader2");
		String header3 = mainWindow.getLocalization().getFieldLabel("ErrorCustomFieldHeader3");
		String header4 = mainWindow.getLocalization().getFieldLabel("ErrorCustomFieldHeader4");
		StringBuilder errorMessage = new StringBuilder(GetDataAndSpacing(header1, HEADER_SPACING_1));
		errorMessage.append(GetDataAndSpacing(header2, HEADER_SPACING_2));
		errorMessage.append(GetDataAndSpacing(header3, HEADER_SPACING_3));
		errorMessage.append(header4);
		errorMessage.append('\n');
		for(int i = 0; i<errors.size(); ++i)
		{
			CustomFieldError thisError = (CustomFieldError)errors.get(i);
			StringBuilder thisErrorMessage = new StringBuilder(GetDataAndSpacing(thisError.getCode(), HEADER_SPACING_1));
			thisErrorMessage.append(GetDataAndSpacing(thisError.getType(), HEADER_SPACING_2));
			thisErrorMessage.append(GetDataAndSpacing(thisError.getTag(), HEADER_SPACING_3));
			String label = fontHelper.getDisplayable(thisError.getLabel());
			thisErrorMessage.append(label);
			errorMessage.append(thisErrorMessage);
			errorMessage.append('\n');
		}
		return errorMessage;
	}

	protected static void formatTextArea(UiTextArea textArea)
	{
		textArea.setCaretPosition(0);
		textArea.setBackground(new JFrame().getBackground());
		textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		textArea.setEditable(false);
	}

	private static String GetDataAndSpacing(String data, int columnSpacing)
	{
		if(data == null)
			return " ";
		String columnData = data;
		for(int i = data.length(); i < columnSpacing; ++i)
		{
			columnData += " ";
		}
		columnData += " ";
		return columnData;
	}
	
	UiTextArea createXMLTextArea(FieldSpecCollection fieldSpecs)
	{
		String xmlRepresentationFieldSpecs = fieldSpecs.toXml();
		xmlRepresentationFieldSpecs = fontHelper.getDisplayable(xmlRepresentationFieldSpecs);
		return createXMLTextArea(xmlRepresentationFieldSpecs);
	}

	static UiTextArea createXMLTextArea(String initialText)
	{
		UiTextArea msgArea = new UiTextArea(20, 80);
		
		msgArea.setText(initialText);
		msgArea.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		msgArea.setLineWrap(true);
		msgArea.setWrapStyleWord(true);
		return msgArea;
	}

	static UiScrollPane createScrollPane(UiTextArea textArea)
	{
		UiScrollPane textPane = new UiScrollPane(textArea, UiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				UiScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		textPane.getVerticalScrollBar().setFocusable(false);
		return textPane;
	}

	public String getTopSectionXml()
	{
		return topSectionXmlResult;
	}

	public String getBottomSectionXml()
	{
		return bottomSectionXmlResult;
	}

	public String getFormTemplateTitle()
	{
		return titleResult;
	}

	public String getFormTemplateDescription()
	{
		return descriptionResult;
	}

	boolean checkForDuplicateLabels() 
	{
		CustomFieldsDuplicateLabelChecker checker = new CustomFieldsDuplicateLabelChecker();
		String topSectionXml = topSectionXmlTextArea.getText();
		String bottomSectionXml = bottomSectionXmlTextArea.getText();
		Vector duplicateLabelsFound = checker.getDuplicatedLabels(topSectionXml, bottomSectionXml);
		if(duplicateLabelsFound.size() == 0)
			return true;

		MartusLocalization localization = mainWindow.getLocalization();
		String duplicateTitle = localization.getWindowTitle("DuplicateLabelsInCustomTemplate");
		String duplicateWarnging = localization.getFieldLabel("DuplicateLabelsInCustomTemplate");
		String duplicateContinue = localization.getFieldLabel("DuplicateLabelsInCustomTemplateContinue");
		StringBuffer duplicates = new StringBuffer(localization.getFieldLabel("DuplicateLabels"));
		duplicates.append(": ");
		for(Iterator iter = duplicateLabelsFound.iterator(); iter.hasNext();)
		{
			duplicates.append("\"");
			duplicates.append((String) iter.next());
			duplicates.append("\" ");
		}
		String[] duplicateWarningMessage = {duplicateWarnging, duplicates.toString(), duplicateContinue};
		if(mainWindow.confirmDlg(duplicateTitle, duplicateWarningMessage))
			return true;
		return false;
	}

	UiTextField titleField;
	UiTextField descriptionField;
	UiTextArea topSectionXmlTextArea;
	UiTextArea bottomSectionXmlTextArea;
	UiFontEncodingHelper fontHelper;
	String topSectionXmlResult = null;
	String bottomSectionXmlResult = null;
	String titleResult = null;
	String descriptionResult = null;
	UiMainWindow mainWindow;
	MartusCrypto security;


	private static final int HEADER_SPACING_1 = 6;
	private static final int HEADER_SPACING_2 = 11;
	private static final int HEADER_SPACING_3 = 14;
	
}
