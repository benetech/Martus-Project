/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

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
package org.martus.common.bulletin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.martus.common.Exceptions.ImportXFormsException;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.GridRow;
import org.martus.common.MartusXml;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;
import org.martus.util.xml.XmlUtilities;

public class BulletinFromXFormsLoader extends AbtractXFormsLoader
{
	private BulletinFromXFormsLoader(Bulletin bulletinToLoadFromToUse)
	{
		bulletinToLoadFrom = bulletinToLoadFromToUse;
	}
	
	public static Bulletin createNewBulletinFromXFormsBulletin(Bulletin bulletinToLoadFromToUse) throws Exception
	{
		return new BulletinFromXFormsLoader(bulletinToLoadFromToUse).createNewBulletinFromXFormsBulletin();
	}

	private Bulletin createNewBulletinFromXFormsBulletin() throws Exception
	{
		String xFormsModelXmlAsString = getXformsModelWithoutRootElement();
		String xFormsInstanceXmlAsString = getXFormsInstanceWithoutRootElement();
        initializeJavaRosa();    	
		
        FormEntryController formEntryController = importXFormsData(xFormsModelXmlAsString, xFormsInstanceXmlAsString);
		if (formEntryController == null)
			throw new ImportXFormsException();
		
		FieldSpecCollection fieldSpecsFromXForms = createFieldSpecsFromXForms(formEntryController);
		
		return createBulletin(bulletinToLoadFrom.getSignatureGenerator(), formEntryController, fieldSpecsFromXForms);
	}
	
	private String getXFormsInstanceWithoutRootElement()
	{
		String xFormsInstanceXmlAsString = bulletinToLoadFrom.getFieldDataPacket().getXFormsInstanceAsString();
		
		return stripRootElement(xFormsInstanceXmlAsString, MartusXml.XFormsInstanceElementName);
	}

	private String getXformsModelWithoutRootElement()
	{
		String xFormsModelXmlAsString = bulletinToLoadFrom.getFieldDataPacket().getXFormsModelAString();
		
		return stripRootElement(xFormsModelXmlAsString, MartusXml.XFormsModelElementName);
	}

	private String stripRootElement(String xml, String elementNameToStrip)
	{
		return XmlUtilities.stripXmlStartEndElements(xml, elementNameToStrip);
	}

	private Bulletin createBulletin(MartusCrypto signatureGenerator, FormEntryController formEntryController, FieldSpecCollection fieldsFromXForms) throws Exception
	{
		FieldSpecCollection allFields = new FieldSpecCollection();
		FieldSpecCollection nonEmptytopSectionFieldSpecsFrom = getNonEmptyTopFieldSpecs();
		allFields.addAll(nonEmptytopSectionFieldSpecsFrom);
		allFields.addAll(fieldsFromXForms);
		
		Bulletin bulletinLoadedFromXForms = new Bulletin(signatureGenerator, allFields, new FieldSpecCollection());
		transferAllStandardFields(bulletinLoadedFromXForms, nonEmptytopSectionFieldSpecsFrom);
		
		resetFormEntryControllerIndex(formEntryController);
		int event;
		while ((event = formEntryController.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM) 
		{
			if (event == FormEntryController.EVENT_REPEAT)
				convertXFormRepeatToGridData(formEntryController, fieldsFromXForms, bulletinLoadedFromXForms);
			
			if (event != FormEntryController.EVENT_QUESTION) 
				continue;
		
			FormEntryPrompt questionPrompt = formEntryController.getModel().getQuestionPrompt();
			IAnswerData answer = questionPrompt.getAnswerValue();
			if (answer == null)
				continue;

			QuestionDef question = questionPrompt.getQuestion();
			final int dataType = questionPrompt.getDataType();
			TreeReference reference = (TreeReference) question.getBind().getReference();
			FieldDataPacket fieldDataPacket = bulletinLoadedFromXForms.getFieldDataPacket();
			String xFormsFieldTag = reference.getNameLast();
			String answerAsString = getMartusAnswerStringFromQuestion(answer, question, dataType);
			fieldDataPacket.set(xFormsFieldTag, answerAsString);
		}
		
		copyPrivateAttachmentProxies(bulletinLoadedFromXForms);
		copyPublicAttachmentProxies(bulletinLoadedFromXForms);
		
		return bulletinLoadedFromXForms;
	}

	public String getMartusAnswerStringFromQuestion(IAnswerData answer,
					QuestionDef question, final int dataType) throws Exception
	{
		String answerAsString = answer.getDisplayText();
		if (dataType == Constants.DATATYPE_DATE)
		{
			answerAsString = formatDateToMartusDateFormat(answerAsString);
		}
		else if (shouldTreatSingleItemChoiceListAsBooleanField(dataType, question) && answerAsString.isEmpty())
		{
			answerAsString = FieldSpec.FALSESTRING;
		}
		else if (dataType == Constants.DATATYPE_CHOICE)
		{
			List<SelectChoice> list = question.getChoices();
			for(int i = 0; i < list.size(); ++i)
			{
				SelectChoice choice = list.get(i);
				if(choice.getValue().equals(answerAsString))
				{
					answerAsString = choice.getLabelInnerText();
					break;
				}
			}
			
		}
		return answerAsString;
	}

	private FieldSpecCollection getNonEmptyTopFieldSpecs()
	{
		FieldSpecCollection topSectionFieldSpecsFrom = bulletinToLoadFrom.getTopSectionFieldSpecs();
		return getNonEmptyFieldSpecs(topSectionFieldSpecsFrom);
	}

	private FieldSpecCollection getNonEmptyFieldSpecs(FieldSpecCollection fieldSpecsFrom)
	{
		FieldSpecCollection nonEmptyFieldSpecs = new FieldSpecCollection();
		for(int i = 0; i < fieldSpecsFrom.size(); ++i)
		{
			FieldSpec spec = fieldSpecsFrom.get(i);
			if(shouldAddFieldSpec(spec))
				nonEmptyFieldSpecs.add(spec);
		}
		return nonEmptyFieldSpecs;
	}

	private boolean shouldAddFieldSpec(FieldSpec spec)
	{
		String tag = spec.getTag();
		if(tag.equals(Bulletin.TAGAUTHOR))
			return true;
		if(tag.equals(Bulletin.TAGLANGUAGE))
			return true;
		if(tag.equals(Bulletin.TAGENTRYDATE))
			return true;
		if(tag.equals(Bulletin.TAGTITLE))
			return true;	

		String data = bulletinToLoadFrom.get(spec.getTag());
		if(tag.equals(Bulletin.TAGEVENTDATE))
			return !data.equals(MartusFlexidate.toStoredDateFormat(MultiCalendar.UNKNOWN));

		return !data.isEmpty();
	}

	private void copyPrivateAttachmentProxies(Bulletin bulletinLoadedFromXForms) throws Exception
	{
		AttachmentProxy[] privateAttachmentProxies = getBulletinToLoadFrom().getPrivateAttachments();
		for (AttachmentProxy privateAttachmentProxy : privateAttachmentProxies)
		{
			bulletinLoadedFromXForms.addPrivateAttachment(privateAttachmentProxy);
		}
	}
	
	private void copyPublicAttachmentProxies(Bulletin bulletinLoadedFromXForms) throws Exception
	{
		AttachmentProxy[] publicAttachmentProxies = getBulletinToLoadFrom().getPublicAttachments();
		for (AttachmentProxy publicAttachmentProxy : publicAttachmentProxies)
		{
			bulletinLoadedFromXForms.addPublicAttachment(publicAttachmentProxy);
		}
	}

	private void transferAllStandardFields(Bulletin bulletinLoadedFromXForms, FieldSpecCollection standardFieldSpecs)
	{
		for (int index = 0; index < standardFieldSpecs.size(); ++index)
		{
			FieldSpec standardField = standardFieldSpecs.get(index);
			String tag = standardField.getTag();
			String originalValueToTransfer = bulletinToLoadFrom.get(tag);
			bulletinLoadedFromXForms.set(tag, originalValueToTransfer);
		}
	}

	private void convertXFormRepeatToGridData(FormEntryController formEntryController, FieldSpecCollection fieldsFromXForms, Bulletin bulletinLoadedFromXForms) throws Exception
	{
		FormEntryModel formModel = formEntryController.getModel();
		IFormElement repeatElement = formModel.getForm().getChild(formModel.getFormIndex());
		GroupDef castedRepeatDef = (GroupDef) repeatElement;
		TreeReference repeatTreeReference = (TreeReference) castedRepeatDef.getBind().getReference(); 
		String gridTag = createGridTag(repeatTreeReference);
		GridFieldSpec foundGridFieldSpec = (GridFieldSpec) fieldsFromXForms.findBytag(gridTag);
		foundGridFieldSpec.setLabel(getNonNullLabel(castedRepeatDef));
		PoolOfReusableChoicesLists allReusableChoiceLists = fieldsFromXForms.getAllReusableChoiceLists();
		GridData gridData = new GridData(foundGridFieldSpec, allReusableChoiceLists);
		handleRepeat(formEntryController, fieldsFromXForms, gridData);
		
		bulletinLoadedFromXForms.set(gridTag, gridData.getXmlRepresentation());
	}

	private void handleRepeat(FormEntryController formEntryController, FieldSpecCollection fieldsFromXForms, GridData gridData) throws Exception
	{
		int event = returnToPreivousEventToAvoidConsumingEvent(formEntryController);
		while ((event = formEntryController.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM) 
		{
			if (event == FormEntryController.EVENT_REPEAT)
			{
				FormEntryModel formModel = formEntryController.getModel();
				IFormElement element = formModel.getForm().getChild(formModel.getFormIndex());
		        if (element instanceof GroupDef) 
		        {
		        		GridRow gridRow = createGridRowWithData(formEntryController, gridData.getSpec(), fieldsFromXForms.getAllReusableChoiceLists());
		        		gridData.addRow(gridRow);
		        }
			}
			
			if (hasNoMoreUserFilledRepeats(event))
				return;
		}
	}

	private boolean hasNoMoreUserFilledRepeats(int event)
	{
		final int PROMPT_USER_TO_ADD_NEW_REPEAT = FormEntryController.EVENT_PROMPT_NEW_REPEAT;
		return event == PROMPT_USER_TO_ADD_NEW_REPEAT;
	}
	
	private GridRow createGridRowWithData(FormEntryController formEntryController, GridFieldSpec gridFieldSpec, PoolOfReusableChoicesLists allReusableChoiceLists) throws Exception
	{
		GridRow gridRow = new GridRow(gridFieldSpec, allReusableChoiceLists);
		int columnIndex = 0;
		int event;
		while ((event = formEntryController.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM) 
		{
			if (event == FormEntryController.EVENT_REPEAT || hasNoMoreUserFilledRepeats(event))
			{
				returnToPreivousEventToAvoidConsumingEvent(formEntryController);
				return gridRow;
			}
			
			if (event == FormEntryController.EVENT_QUESTION)
			{
				fillGridRow(formEntryController, gridRow, columnIndex);
				++columnIndex;
			}
		}
		
		return gridRow;
	}

	private void fillGridRow(FormEntryController formEntryController, GridRow gridRow, int columnIndex) throws Exception
	{
		FormEntryPrompt currentQuestionPrompt = formEntryController.getModel().getQuestionPrompt();
		IAnswerData currentAnswer = currentQuestionPrompt.getAnswerValue();
		if (currentAnswer == null)
			return;
				
		final int dataType = currentQuestionPrompt.getDataType();
		QuestionDef questionDef = currentQuestionPrompt.getQuestion();
		String answerAsString = getMartusAnswerStringFromQuestion(currentAnswer, questionDef, dataType);
		gridRow.setCellText(columnIndex, answerAsString);
	}

	private int returnToPreivousEventToAvoidConsumingEvent(FormEntryController formEntryController)
	{
		return formEntryController.stepToPreviousEvent();
	}

	private String formatDateToMartusDateFormat(String dateAsString) throws Exception
	{
		DateFormat incomingDateFormat = new SimpleDateFormat("dd/MM/yy");
		Date parsedDate = incomingDateFormat.parse(dateAsString);
		MultiCalendar multiCalendar = new MultiCalendar(parsedDate);
		
		return multiCalendar.toString();
	}

	public String getQuetionLabel(FormEntryPrompt questionPrompt)
	{
		return questionPrompt.getQuestion().getLabelInnerText();
	}

	private void resetFormEntryControllerIndex(FormEntryController formEntryController)
	{
		while (formEntryController.stepToPreviousEvent() != FormEntryController.EVENT_BEGINNING_OF_FORM);
	}

    private FormEntryController importXFormsData(String xFormsModelXmlAsString, String xFormsInstance) 
    {
    		InputStream xFormsModelInputStream = new ByteArrayInputStream(xFormsModelXmlAsString.getBytes(StandardCharsets.UTF_8));
		FormDef formDef = XFormUtils.getFormFromInputStream(xFormsModelInputStream);
		FormEntryModel formEntryModel = new FormEntryModel(formDef);
		FormEntryController formEntryController = new FormEntryController(formEntryModel);
		
	    	byte[] xFormsInstanceBytes = xFormsInstance.getBytes(StandardCharsets.UTF_8);
	    	TreeElement modelRootElement = formEntryController.getModel().getForm().getInstance().getRoot().deepCopy(true);
	    	TreeElement instanceRootElement = XFormParser.restoreDataModel(xFormsInstanceBytes, null).getRoot();
	    	String instanceRootName = instanceRootElement.getName();
	    	String modelRootName = modelRootElement.getName();
	    	if (!instanceRootName.equals(modelRootName))
	    		return null;
	    	
	    	if (instanceRootElement.getMult() != TreeReference.DEFAULT_MUTLIPLICITY)
	    		return null;
	    	
	    	populateDataModel(modelRootElement);
	    	modelRootElement.populate(instanceRootElement, formEntryController.getModel().getForm());
	    	populateFormEntryControllerModel(formEntryController, modelRootElement);
	    	fixLanguageIusses(formEntryController);
	    	
	    	return formEntryController;
    }

	private void populateFormEntryControllerModel(FormEntryController formEntryController, TreeElement modelRoot)
	{
		formEntryController.getModel().getForm().getInstance().setRoot(modelRoot);
	}

	private void fixLanguageIusses(FormEntryController formEntryController)
	{
		//NOTE: this comment is from Collect's java rosa seference
    	// fix any language issues
    	// : http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
		if (formEntryController.getModel().getLanguages() != null) 
    			formEntryController.getModel().getForm().localeChanged(formEntryController.getModel().getLanguage(), formEntryController.getModel().getForm().getLocalizer());
	}

	private void populateDataModel(TreeElement modelRootElement)
	{
		TreeReference treeReference = TreeReference.rootRef();
		treeReference.add(modelRootElement.getName(), TreeReference.INDEX_UNBOUND);
	}
	
	private Bulletin getBulletinToLoadFrom()
	{
		return bulletinToLoadFrom;
	}
	
	private Bulletin bulletinToLoadFrom;
}
