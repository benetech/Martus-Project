/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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

package org.martus.client.swingui.bulletincomponent;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;

import org.martus.client.core.BulletinLanguageChangeListener;
import org.martus.client.core.MartusApp;
import org.martus.client.core.ZawgyiLabelUtilities;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.client.swingui.fields.UiField;
import org.martus.client.swingui.fields.UiFieldContext;
import org.martus.client.swingui.fields.UiFieldCreator;
import org.martus.client.swingui.fields.UiGrid;
import org.martus.common.FieldSpecCollection;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.DateRangeInvertedException;
import org.martus.common.fieldspec.DateTooEarlyException;
import org.martus.common.fieldspec.DateTooLateException;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.RequiredFieldIsBlankException;
import org.martus.common.packet.FieldDataPacket;
import org.martus.swing.UiLabel;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


abstract public class UiBulletinComponentDataSection extends UiBulletinComponentSection
{
	UiBulletinComponentDataSection(UiMainWindow mainWindowToUse, String sectionNameToUse)
	{
		super(mainWindowToUse, sectionNameToUse);
		sectionName = sectionNameToUse;
		context = new UiFieldContext();
	}
	
	protected UiFieldContext getContext()
	{
		return context;
	}
	
	void setFieldCreator(UiFieldCreator creatorToUse)
	{
		fieldCreator = creatorToUse;
	}

	public void createLabelsAndFields(FieldSpecCollection specs, BulletinLanguageChangeListener listener)
	{
		context.setSectionFieldSpecs(specs);
		languageChangeListener = listener;

		fields = new UiField[specs.size()];
		FieldRow fieldRow = null;
		for(int fieldNum = 0; fieldNum < specs.size(); ++fieldNum)
		{
			FieldSpec spec = specs.get(fieldNum);
			fields[fieldNum] = createField(spec, listener);
			
			if(spec.getType().isSectionStart())
			{
				String label = ZawgyiLabelUtilities.getDisplayableLabel(spec, getLocalization());
				startNewGroup("_Section" + spec.getTag(), label);
			} 
			else
			{
				// FIXME: ask spec whether to keep on line or not
				if(fieldRow == null || !spec.keepWithPrevious())
				{
					fieldRow = new FieldRow(getMainWindow());
					fieldRow.setSpec(spec);
				}
				else
				{
					fieldRow.addComponent(new LabelWithinFieldRow(spec));
				}
				fieldRow.addComponent(fields[fieldNum].getComponent());
				addFieldRow(fieldRow);
			}
		}
		
		JComponent attachmentTable = createAttachmentTable();
		String tag = "_Attachments" + sectionName;
		fieldRow = new FieldRow(getMainWindow());
		fieldRow.setTag(tag);
		fieldRow.addComponent(attachmentTable);
		addFieldRow(fieldRow);
	}
	
	class LabelWithinFieldRow extends UiLabel
	{
		public LabelWithinFieldRow(FieldSpec spec)
		{
			setVerticalAlignment(UiLabel.TOP);
			setFont(new UiWrappedTextArea("").getFont());
			setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

			String labelText = spec.getLabel();
			labelText = fontHelper.getDisplayable(labelText);
			if(labelText.equals(""))
				labelText = getLocalization().getFieldLabel(spec.getTag());
			setText(labelText);
		}
	}

	UiField createField(FieldSpec spec, BulletinLanguageChangeListener listener)
	{
		UiField field = fieldCreator.createField(spec);
		field.initalize();
		if(spec.getType().isGrid())
		{
			UiGrid grid = (UiGrid)field;
			grid.getGridTableModel().addTableModelListener(new GridChangeHandler(grid, context));
		}
		return field;
	}
	
	UiField createAllPrivateField()
	{
		FieldSpec allPrivateFieldSpec = FieldSpec.createStandardField("allprivate", new FieldTypeBoolean());
		UiField field = createField(allPrivateFieldSpec, null);
		FieldRow fieldRow = new FieldRow(getMainWindow());
		fieldRow.setSpec(allPrivateFieldSpec);
		fieldRow.addComponent(field.getComponent());
		addFieldRow(fieldRow);
		return field;
	}

	void addFieldRow(FieldRow row)
	{
		addComponents(row.getLabel(), row.getFieldHolder());
	}
	
	public void copyDataFromPacket(FieldDataPacket fdp)
	{
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{
			String text = "";
			if(fdp != null)
				text = fdp.get(context.getFieldSpec(fieldNum).getTag());
			text = fontHelper.getDisplayable(text);
            fields[fieldNum].setText(text);
		}

		if(fdp == null)
			return;

		AttachmentProxy[] attachments = fdp.getAttachments();
		for(int i = 0 ; i < attachments.length ; ++i)
			addAttachment(attachments[i]);
	}

    static class FieldRow
	{
		public FieldRow(UiMainWindow mainWindowToUse)
		{
			app = mainWindowToUse.getApp();
			localization = mainWindowToUse.getLocalization();
			fieldHolder = new FieldHolder(localization);
		}
		
		public void setSpec(FieldSpec spec)
		{
			setTagAndLabel(spec.getTag(), spec.getLabel());
		}
		
		public void setTag(String tag)
		{
			setTagAndLabel(tag, "");
		}
		
		void addComponent(JComponent fieldComponent) 
		{
			fieldHolder.addField(fieldComponent);
		}
		
		public JComponent getLabel()
		{
			return label;
		}
		
		public FieldHolder getFieldHolder()
		{
			return fieldHolder;
		}
		
		private UiWrappedTextArea createLabelComponent(String tag, String labelText)
		{
			//TODO: For wrapped labels, we need to take into consideration font size, text alignment, rtol and printing. 
			//UiWrappedTextArea label = new UiWrappedTextArea(labelText, 30);
			//UiLabel label = new UiLabel(labelText);
			//return label;
			int fixedWidth = 14;
			UiWrappedTextArea labelComponent = new UiWrappedTextArea(labelText, fixedWidth, fixedWidth);
			labelComponent.setFocusable(false);
			return labelComponent;
		}

		private JComponent createLabelWithHider(String tag, JComponent labelComponent)
		{
			HiderButton hider = new HiderButton(app, tag, fieldHolder);
			Component spacer = Box.createHorizontalStrut(10);
				
			Box panel = Box.createHorizontalBox();
			hider.setAlignmentY(JComponent.TOP_ALIGNMENT);
			labelComponent.setAlignmentY(JComponent.TOP_ALIGNMENT);
			Utilities.addComponentsRespectingOrientation(panel, new Component[] {hider, spacer, labelComponent});
			return panel;
		}
		
		private void setTagAndLabel(String tag, String labelText)
		{
			if(labelText.equals(""))
				labelText = localization.getFieldLabel(tag);
			else
			{
				labelText = fontHelper.getDisplayable(labelText);
			}

			UiWrappedTextArea labelComponent = createLabelComponent(tag, labelText);
			label = createLabelWithHider(tag, labelComponent);
		}

		MartusApp app;
		MartusLocalization localization;
		JComponent label;
		FieldHolder fieldHolder;
	}
	
	public void updateEncryptedIndicator(boolean isEncrypted)
	{
		String iconFileName = "unlocked.jpg";
		String title = getLocalization().getFieldLabel("publicsection");
		if(isEncrypted)
		{
			iconFileName = "locked.jpg";
			title = getLocalization().getFieldLabel("privatesection");
		}

		setSectionIconAndTitle(iconFileName, title);

		updateSectionBorder(isEncrypted);
	}

	public void copyDataToBulletin(Bulletin bulletin)
	{	
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{
            String text = fields[fieldNum].getText();
			text = fontHelper.getStorable(text);
			bulletin.set(context.getFieldSpec(fieldNum).getTag(), text);
		}
	}

	
	public void validateData() throws DataInvalidException
	{
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{
			FieldSpec spec = context.getFieldSpec(fieldNum);
			String label = ZawgyiLabelUtilities.getDisplayableLabel(spec, getLocalization());
			try 
			{
				UiField thisField = fields[fieldNum];
				thisField.validate(spec, label);
			} 
			catch (UiDateEditor.DateFutureException e) 
			{
				throw new UiDateEditor.DateFutureException(label);
			}
			catch(DateRangeInvertedException e)
			{
				throw e; 
			}
			catch(DateTooEarlyException e)
			{
				throw e; 
			}
			catch(DateTooLateException e)
			{
				throw e; 
			}
			catch(RequiredFieldIsBlankException e)
			{
				throw e;
			}
		}
		
		validateAttachments();
	}

	public boolean isAnyFieldModified(Bulletin original, Bulletin newBulletin)
	{
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{			
			String fieldTag = context.getFieldSpec(fieldNum).getTag();			
			String oldFieldText = original.get(fieldTag);
			String newFieldText = newBulletin.get(fieldTag);

			if (!oldFieldText.equals(newFieldText))
			{									
				return true;
			}																
		}		
		
		return false;
	}
	
	public static class AttachmentMissingException extends DataInvalidException
	{
		public AttachmentMissingException(String localizedTag)
		{
			super(localizedTag);
		}

	}
	
	public UiField[] getFields()
	{
		return fields;
	}

	public void updateSpellChecker(String bulletinLanguageCode)
	{
		context.setCurrentBulletinLanguage(bulletinLanguageCode);
		for(int i = 0; i < fields.length; ++i)
		{
			UiField field = fields[i];
			field.updateSpellChecker(bulletinLanguageCode);
		}
	}


	abstract public JComponent createAttachmentTable();
	abstract public void addAttachment(AttachmentProxy a);
	abstract public void clearAttachments();
	abstract public void validateAttachments() throws DataInvalidException;

	BulletinLanguageChangeListener languageChangeListener;
	String sectionName;
	private UiField[] fields;
	UiFieldCreator fieldCreator;
	private UiFieldContext context;

}

