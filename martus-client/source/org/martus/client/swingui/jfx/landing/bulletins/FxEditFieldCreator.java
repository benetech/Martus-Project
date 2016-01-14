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

import java.util.Vector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.martus.client.core.FxBulletin;
import org.martus.client.core.FxBulletinField;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.jfx.generic.controls.DateRangePicker;
import org.martus.client.swingui.jfx.generic.controls.MartusDatePicker;
import org.martus.client.swingui.jfx.generic.controls.NestedChoiceBox;
import org.martus.client.swingui.jfx.generic.controls.ScrollFreeTextArea;
import org.martus.client.swingui.jfx.generic.controls.TextAreaWithBetterTabHandling;
import org.martus.client.swingui.jfx.generic.data.BooleanStringConverter;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MessageFieldSpec;

public class FxEditFieldCreator extends FxFieldCreator
{
	public FxEditFieldCreator(MartusLocalization localizationToUse)
	{
		super(localizationToUse);
	}

	@Override
	protected Node createDateField(FxBulletinField field)
	{
		MartusDatePicker picker = new MartusDatePicker(localization);
	
		Property<String> property = field.valueProperty();
		String existingDateString = property.getValue();
		picker.setValue(existingDateString);
		property.bind(picker.overallValueProperty());
		return picker;
	}

	@Override
	protected Node createDateRangeField(FxBulletinField field)
	{
		DateRangePicker picker = new DateRangePicker(localization);
	
		Property<String> property = field.valueProperty();
		String existingDateRangeString = property.getValue();
		picker.setValue(existingDateRangeString);
		property.bind(picker.valueProperty());
		
		return picker;
	}

	@Override
	protected Node createDropdownField(FxBulletin bulletin, FxBulletinField field) throws Exception
	{
		Vector<ObservableChoiceItemList> listOfChoiceItemLists = field.getChoiceItemLists();
		if(listOfChoiceItemLists.size() == 0)
			return createFieldNotAvailable();
	
		NestedChoiceBox choiceBoxes = new NestedChoiceBox();
		choiceBoxes.setChoiceItemLists(listOfChoiceItemLists);
	
		Property<String> property = field.valueProperty();
		choiceBoxes.setValue(property.getValue());
		property.bind(choiceBoxes.valueProperty());
	
		return choiceBoxes;
	}

	@Override
	protected Node createBooleanField(Property<String> property)
	{
		CheckBox checkBox = new CheckBox();
		BooleanStringConverter converter = new BooleanStringConverter();
		checkBox.selectedProperty().setValue(converter.fromString(property.getValue()));
	
		BooleanProperty selectedStateProperty = checkBox.selectedProperty();
		selectedStateProperty.addListener((observable, oldValue, newValue) -> property.setValue(converter.toString(newValue)));
		return checkBox;
	}

	@Override
	protected Node createMessageField(FieldSpec spec)
	{
		String messageText = ((MessageFieldSpec)(spec)).getMessage();
		Text text = new Text(messageText);
		TextFlow flow = new TextFlow(text);
		flow.getStyleClass().add("messageText");
		return flow;
	}

	@Override
	protected Node createStringField(Property<String> property)
	{
		ScrollFreeTextArea textField = new ScrollFreeTextArea();
		textField.textProperty().bindBidirectional(property);
		HBox.setHgrow(textField, Priority.SOMETIMES);
		
		return textField;
	}

	@Override
	protected Node createMultilineField(Property<String> property)
	{
		TextArea textArea = new TextAreaWithBetterTabHandling();
		textArea.setPrefColumnCount(MINIMUM_REASONABLE_COLUMN_COUNT);
		textArea.setPrefRowCount(MULTILINE_FIELD_HEIGHT_IN_ROWS);
		textArea.setWrapText(true);
		textArea.textProperty().bindBidirectional(property);
		HBox.setHgrow(textArea, Priority.SOMETIMES);
		
		return textArea;
	}
	
	@Override
	protected Node createFieldNotAvailable()
	{
		return new Label("(n/a)");
	}

	@Override
	public boolean isFieldEditable()
	{
		return true;
	}
}
