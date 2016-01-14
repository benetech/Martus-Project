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
package org.martus.client.swingui.jfx.generic.controls;

import java.util.Iterator;
import java.util.Vector;
import java.util.function.Predicate;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;

import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.common.fieldspec.ChoiceItem;

public class NestedChoiceBox extends HBox
{
	public NestedChoiceBox()
	{
		overallValueProperty = new SimpleStringProperty("");
		overallValuesHumanReadable = new Vector();
	}
	
	public void setChoiceItemLists(Vector<ObservableChoiceItemList> lists)
	{
		getChildren().clear();
		ChoiceBox previous = null;
		for(int i = 0; i < lists.size(); ++i)
		{
			ObservableChoiceItemList choices = lists.get(i);
			FilteredList<ChoiceItem> filteredChoices = createFilteredList(choices);
			
			ChoiceBox<ChoiceItem> choiceBox = createSingleChoiceBox(filteredChoices);
			getChildren().add(choiceBox);

			if(previous != null)
			{
				ReadOnlyObjectProperty previousSelectedProperty = previous.getSelectionModel().selectedItemProperty();
				keepChoicesUpdatedBasedOnPreviousSelection(filteredChoices, previousSelectedProperty);
			}
			
			previous = choiceBox;
		}
		
		hasNestedDropdowns = (lists.size() > 1);
	}

	public Property<String> valueProperty()
	{
		return overallValueProperty;
	}
	
	public void setValue(String value)
	{
		getChildrenUnmodifiable().forEach(child -> updateFromValue((ChoiceBox)child, value));
	}
	
	private ChoiceBox<ChoiceItem> createSingleChoiceBox(FilteredList<ChoiceItem> filteredChoices)
	{
		ChoiceBox<ChoiceItem> choiceBox = new ChoiceBox<ChoiceItem>(filteredChoices);

		ReadOnlyObjectProperty<ChoiceItem> selectedItemProperty = choiceBox.getSelectionModel().selectedItemProperty();
		selectedItemProperty.addListener((observable, oldValue, newValue) -> updateOverallValue());

		return choiceBox;
	}

	private void keepChoicesUpdatedBasedOnPreviousSelection(FilteredList<ChoiceItem> filteredChoices, ReadOnlyObjectProperty previousSelectedProperty)
	{
		ChangeListener<ChoiceItem> filterer = (observable, oldValue, newValue) -> 
		{
			filteredChoices.setPredicate(new StartsWithFilter(newValue));
		};
		previousSelectedProperty.addListener(filterer);
	}
	
	private void updateOverallValue()
	{
		overallValueProperty.setValue("");
		overallValuesHumanReadable.clear();
		getChildrenUnmodifiable().forEach(child -> updateOverallValue((ChoiceBox)child));
	}

	private void updateOverallValue(ChoiceBox<ChoiceItem> child)
	{
		ChoiceItem selectedChoice = child.getSelectionModel().getSelectedItem();
		if(selectedChoice == null)
			return;
		
		String code = selectedChoice.getCode();
		if(code.length() == 0)
			return;
		
		overallValueProperty.setValue(code);
		if(overallValuesHumanReadable.size() > 0)
			overallValuesHumanReadable.add(SPACE);
		overallValuesHumanReadable.add(selectedChoice.getLabel());
	}

	private FilteredList<ChoiceItem> createFilteredList(ObservableChoiceItemList choices)
	{
		FilteredList<ChoiceItem> filteredChoices = new FilteredList(choices);
		// NOTE: It should default to all true, but doesn't appear to do so
		filteredChoices.setPredicate(item -> true);
		return filteredChoices;
	}

	private static class StartsWithFilter implements Predicate<ChoiceItem>
	{
		public StartsWithFilter(ChoiceItem startsWith)
		{
			lookFor = startsWith.getCode();
		}
		
		@Override
		public boolean test(ChoiceItem choice)
		{
			String code = choice.getCode();
			if(code.length() == 0)
				return true;
			
			boolean result = code.startsWith(lookFor);
			return result;
		}
		
		private String lookFor;
	}
	
	private void updateFromValue(ChoiceBox choiceBox, String value)
	{
		ObservableList<ChoiceItem> choices = choiceBox.getItems();
		for(int i = 0; i < choices.size(); ++i)
		{
			ChoiceItem choice = choices.get(i);
			String code = choice.getCode();
			boolean isExactMatch = value.equals(code);
			boolean startsWith = code.length() > 0 && value.startsWith(code);
			boolean isNestedPartialMatch = hasNestedDropdowns() && startsWith;
			if(isExactMatch || isNestedPartialMatch)
			{
				choiceBox.getSelectionModel().select(choice);
				return;
			}
		}
	}
	
	private boolean hasNestedDropdowns()
	{
		return hasNestedDropdowns;
	}
	
	public String convertStoredToHumanReadable()
	{
		StringBuilder combindData = new StringBuilder();
		for (Iterator iterator = overallValuesHumanReadable.iterator(); iterator.hasNext();)
		{
			combindData.append(iterator.next());
		}
		return combindData.toString();
	}

	private final String SPACE = " ";
	private SimpleStringProperty overallValueProperty;
	private Vector<String> overallValuesHumanReadable;
	private boolean hasNestedDropdowns;
}
