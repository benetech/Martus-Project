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

import java.util.List;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XFormsModule;
import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeMessage;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FieldTypeSectionStart;
import org.martus.common.fieldspec.GridFieldSpec;

abstract public class AbtractXFormsLoader
{
	protected static final void initializeJavaRosa() 
	{
		new XFormsModule().registerModule();
	}
	
	protected FieldSpecCollection createFieldSpecsFromXForms(FormEntryController formEntryController) throws Exception
	{
		FormDef formDef = formEntryController.getModel().getForm();
		List<IFormElement> children = formDef.getChildren();
		unGroupedUniqueId = 0;
		return recursivelyConvertXFormsFieldsToFieldSpecs(formEntryController, children, false);
	}

	private FieldSpecCollection recursivelyConvertXFormsFieldsToFieldSpecs(FormEntryController formEntryController, List<IFormElement> children, boolean inGroup) throws Exception
	{
		FieldSpecCollection fieldsFromXForms = new FieldSpecCollection();
		for (IFormElement child : children)
		{
			if (child instanceof GroupDef)
			{
				GroupDef groupDef = (GroupDef) child;
				List<IFormElement> groupChildren = groupDef.getChildren();
				FieldSpecCollection gridChildrenFieldSpecs = recursivelyConvertXFormsFieldsToFieldSpecs(formEntryController, groupChildren, true);
				if (isRepeatGroup(groupDef))
				{
					GridFieldSpec gridSpec = new GridFieldSpec();
					TreeReference thisTreeReference = (TreeReference) groupDef.getBind().getReference();
					gridSpec.setTag(createGridTag(thisTreeReference));
					gridSpec.addColumns(gridChildrenFieldSpecs);
					gridSpec.setLabel(getNonNullLabel(groupDef));
					String sectionTag = createSectionTag(thisTreeReference.toString());
					fieldsFromXForms.add(FieldSpec.createCustomField(sectionTag, getNonNullLabel(groupDef), new FieldTypeSectionStart()));
					fieldsFromXForms.add(gridSpec);
				}
				else
				{
					String sectionTag = createSectionTag(groupDef);
					fieldsFromXForms.add(FieldSpec.createCustomField(sectionTag, getNonNullLabel(groupDef), new FieldTypeSectionStart()));
					fieldsFromXForms.addAll(gridChildrenFieldSpecs);
				}
				inGroup = false;
			}
			
			if (child instanceof QuestionDef)
			{
				if(!inGroup)
				{
					++unGroupedUniqueId;
					String sectionTag = X_FORM_UN_GROUPED_BASE_TAG + unGroupedUniqueId;
					fieldsFromXForms.add(FieldSpec.createCustomField(sectionTag, UN_GROUPED_SECTION_LABEL, new FieldTypeSectionStart()));
					inGroup = true;
				}
				QuestionDef questionDef = (QuestionDef) child;
				FormEntryPrompt questionPrompt = findQuestion(formEntryController, (TreeReference) questionDef.getBind().getReference());
				FieldSpec fieldSpec = convertToFieldSpec(questionPrompt);
				if (fieldSpec != null)
					fieldsFromXForms.add(fieldSpec);
			}
		}
		
		return fieldsFromXForms;
	}

	private String createSectionTag(GroupDef groupDef)
	{
		TreeReference thisTreeReference = (TreeReference) groupDef.getBind().getReference();
		String referenceLabel = createLabelFromNodeReference(thisTreeReference);
		String sectionLabel = getNonNullLabel(groupDef);
		
		return createSectionTag(referenceLabel + " " + sectionLabel);
	}
	
	private String createSectionTag(String sectionLabel)
	{
		final String SECTION_TAG_POSTFIX = "Section";
		return createTag(sectionLabel.toString()) + SECTION_TAG_POSTFIX;
	}
	
	protected String createGridTag(TreeReference treeReference)
	{
		final String GRID_TAG_POSTFIX = "Grid";
		String nodeReferenceAsString = createLabelFromNodeReference(treeReference);
		return createTag(nodeReferenceAsString) + GRID_TAG_POSTFIX;
	}
	
	private String createLabelFromNodeReference(TreeReference treeReference)
	{
		StringBuilder nameBuilder = new StringBuilder();
		for (int index = 0; index < treeReference.size(); ++index)
		{
			String name = treeReference.getName(index);
			if (index > 0 && !name.isEmpty())
				nameBuilder.append("_");
		
			nameBuilder.append(name);
		}
		
		return nameBuilder.toString();
	}

	private String createTag(String label)
	{
		final String TAG_POSTFIX = "Tag";
		String rawtext = label.replaceAll(" ", "_");
		if (rawtext.startsWith("/"))
			rawtext = rawtext.replaceFirst("/", "");
		
		rawtext = rawtext.replaceAll("/", "_");
		rawtext = rawtext.replaceAll("\\(", "_");
		rawtext = rawtext.replaceAll("\\)", "_");
		return rawtext + TAG_POSTFIX;
	}

	protected String getNonNullLabel(GroupDef groupDef)
	{
		String potentialLabel = groupDef.getLabelInnerText();
		if (potentialLabel == null)
			return "";

		return potentialLabel;
	}

	private boolean isRepeatGroup(GroupDef groupDef)
	{
		return groupDef.getRepeat();
	}
	
	private FormEntryPrompt findQuestion(FormEntryController formEntryContorller, TreeReference treeReferenceToMatch)
	{
		formEntryContorller.jumpToIndex(FormIndex.createBeginningOfFormIndex());
		int event;
		while ((event = formEntryContorller.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM) 
		{
			if (event == FormEntryController.EVENT_QUESTION) 
			{
				FormEntryPrompt questionPrompt = formEntryContorller.getModel().getQuestionPrompt();
				QuestionDef thisQuestionDef = questionPrompt.getQuestion();
				TreeReference thisTreeReference = (TreeReference) thisQuestionDef.getBind().getReference();
				if (thisTreeReference.equals(treeReferenceToMatch))
					return questionPrompt;
			} 
		}

		return null;
	}
	
	private FieldSpec convertToFieldSpec(FormEntryPrompt questionPrompt)
	{
		QuestionDef question = questionPrompt.getQuestion();
		final int dataType = questionPrompt.getDataType();
		TreeReference reference = (TreeReference) question.getBind().getReference();
		String tag = reference.getNameLast();
		String questionLabel = questionPrompt.getQuestion().getLabelInnerText();

		if(questionPrompt.isReadOnly())
			return FieldSpec.createCustomField(tag, questionLabel, new FieldTypeMessage());

		if (isNormalFieldType(dataType))
			return FieldSpec.createCustomField(tag, questionLabel, new FieldTypeNormal());
		
		if (dataType == Constants.DATATYPE_DATE)
			return FieldSpec.createCustomField(tag, questionLabel, new FieldTypeDate());
		
		if (shouldTreatSingleItemChoiceListAsBooleanField(dataType, question))
			return FieldSpec.createCustomField(tag, questionLabel, new FieldTypeBoolean());
		
		if (dataType == Constants.DATATYPE_CHOICE)
		{
			Vector<ChoiceItem> convertedChoices = new Vector<ChoiceItem>();
			List<SelectChoice> choicesToConvert = question.getChoices();
			for (SelectChoice choiceToConvert : choicesToConvert)
			{
				//String choiceItemCode = choiceToConvert.getValue();
				String choiceItemLabel = choiceToConvert.getLabelInnerText();
				//Martus doesn't keep Code's when exporting so use Label twice instead
				convertedChoices.add(new ChoiceItem(choiceItemLabel, choiceItemLabel));
			}
			
			FieldSpec fieldSpec = new CustomDropDownFieldSpec(convertedChoices);
			fieldSpec.setTag(tag);
			fieldSpec.setLabel(questionLabel);
			return fieldSpec;
		}
		MartusLogger.log("Warning: BulletinFromXFormsLoader.convertToFieldSpec unknown Field Type:" + String.valueOf(dataType));
		return null;
	}
	
	public static boolean isNormalFieldType(final int dataType)
	{
		if(dataType == Constants.DATATYPE_TEXT)
			return true;
		if(dataType == Constants.DATATYPE_INTEGER)
			return true;
		if(dataType == Constants.DATATYPE_DECIMAL)
			return true;
		if(dataType == Constants.DATATYPE_TIME)
			return true;
		if(dataType == Constants.DATATYPE_DATE_TIME)
			return true;
		if(dataType == Constants.DATATYPE_GEOPOINT)
			return true;
		if(dataType == Constants.DATATYPE_BARCODE)
			return true;
		if(dataType == Constants.DATATYPE_BINARY)
			return true;
		if(dataType == Constants.DATATYPE_LONG)
			return true;
		if(dataType == Constants.DATATYPE_GEOSHAPE)
			return true;
		if(dataType == Constants.DATATYPE_GEOTRACE)
			return true;
		return false;
	}

	protected boolean shouldTreatSingleItemChoiceListAsBooleanField(int xFormsDataType, QuestionDef question)
	{
		if (xFormsDataType != Constants.DATATYPE_CHOICE_LIST)
			return false;
		
		if (question.getChoices().size() != 1)
			return false;
		
		List<SelectChoice> choices = question.getChoices();
		SelectChoice onlyChoice = choices.get(0);
		if (onlyChoice.getValue().equals(FieldSpec.TRUESTRING))
			return true;
		
		return false;
	}
	
	private int unGroupedUniqueId;

	private static final String UN_GROUPED_SECTION_LABEL = "+";
	private static final String X_FORM_UN_GROUPED_BASE_TAG = "XFormUnGrouped";
}
