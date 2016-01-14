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

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.martus.client.core.FxBulletin;
import org.martus.client.core.FxBulletinField;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.jfx.generic.controls.MartusDatePicker;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.FieldSpec;

abstract public class FxFieldCreator
{
	public FxFieldCreator(MartusLocalization localizationToUse)
	{
		localization = localizationToUse;
		this.fieldWidthProperty = new SimpleDoubleProperty(DEFAULT_TEXT_VIEW_WIDTH);
	}
	
	public Node createFieldNode(FxBulletin bulletin, FxBulletinField field) throws Exception
	{
		FieldSpec spec = field.getFieldSpec();
		Property<String> property = field.valueProperty();
		
		if(spec.getType().isString())
			return createStringField(property);
		
		if(spec.getType().isMultiline())
			return createMultilineField(property);
		
		if(spec.getType().isMessage())
			return createMessageField(spec);
		
		if(spec.getType().isBoolean())
			return createBooleanField(property);
		
		if(spec.getType().isDropdown() || spec.getType().isLanguageDropdown())
			return createDropdownField(bulletin, field);
		
		if(spec.getType().isDate())
		{
			if(field.getTag().equals(Bulletin.TAGENTRYDATE))
				return createReadOnlyDateField(field);
			return createDateField(field);
		}
		
		if(spec.getType().isDateRange())
			return createDateRangeField(field);
		
		return createFieldNotAvailable();
	}

	abstract protected Node createStringField(Property<String> property);
	abstract protected Node createMultilineField(Property<String> property);
	abstract protected Node createMessageField(FieldSpec spec);
	abstract protected Node createBooleanField(Property<String> property);
	abstract protected Node createDropdownField(FxBulletin bulletin, FxBulletinField field) throws Exception;
	abstract protected Node createDateField(FxBulletinField field);
	abstract protected Node createDateRangeField(FxBulletinField field);
	abstract protected Node createFieldNotAvailable();
	abstract public boolean isFieldEditable();

	protected Node createReadOnlyDateField(FxBulletinField field)
	{
		Property<String> property = field.valueProperty();
		String existingDateString = property.getValue();
		String formattedDate = "";
		if(existingDateString.length() > 0)
		{
			MartusDatePicker picker = new MartusDatePicker(localization);
			picker.setValue(existingDateString);
			formattedDate = picker.getLocalizedDateFormatted();
		}
		Text localDate = new Text(formattedDate);
		return responsiveTextFlowNode(localDate);
	}

	protected Node responsiveTextFlowNode(ReadOnlyStringProperty property)
	{
		return responsiveTextFlowNode(getText(property));
	}

	protected Node responsiveTextFlowNode(Text mainContent)
	{
		Text contentWithNewLineAdded = getContentWithNewLineAdded(mainContent);
		TextFlow flow = new TextFlow(contentWithNewLineAdded);
		flow.getStyleClass().add("systemTextField");
		double width = fieldWidthProperty.doubleValue();
		flow.setMinWidth(width); 
		flow.prefWidthProperty().bind(fieldWidthProperty);
		return flow;
	}

	protected Text getContentWithNewLineAdded(Text mainContent)
	{
		String stringContentWithNewLine = mainContent.getText();
		stringContentWithNewLine += NEW_LINE;
		return getDisplayableText(stringContentWithNewLine);
	}

	private Text getDisplayableText(String text)
	{
		return new Text(text);
	}

	protected Text getText(ReadOnlyStringProperty property)
	{
		String textStringValue = property.getValue();
		return getDisplayableText(textStringValue);
	}

	protected static final int MINIMUM_REASONABLE_COLUMN_COUNT = 10;
	protected static final int MULTILINE_FIELD_HEIGHT_IN_ROWS = 5;
	protected static final double DEFAULT_TEXT_VIEW_WIDTH = 650.0;
	protected static final String NEW_LINE = "\n";
	
	protected MartusLocalization localization;
	protected ReadOnlyDoubleProperty fieldWidthProperty;
}
