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

import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import org.martus.client.core.FxBulletin;
import org.martus.client.core.FxBulletinField;
import org.martus.client.core.FxBulletinGridField;
import org.martus.client.swingui.MartusLocalization;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;

public class BulletinEditorSection extends GridPane
{
	public BulletinEditorSection(FxFieldCreator fieldCreatorToUse, FxBulletin bulletinToUse, MartusLocalization localizationToUse, String sectionTitle)
	{
		fieldCreator = fieldCreatorToUse;
		bulletin = bulletinToUse;
		localization = localizationToUse;
		title = sectionTitle;
		
		getStyleClass().add("bulletin-editor-grid");
		
		rows = new Vector<BulletinEditorRow>();
		
		ColumnConstraints labelColumnConstraints= new ColumnConstraints();
		labelColumnConstraints.fillWidthProperty().setValue(true);
		labelColumnConstraints.setMinWidth(200);

		ColumnConstraints fieldColumnConstraints = new ColumnConstraints();
		fieldColumnConstraints.fillWidthProperty().setValue(true);
		fieldColumnConstraints.hgrowProperty().set(Priority.ALWAYS);

		getColumnConstraints().add(labelColumnConstraints);
		getColumnConstraints().add(fieldColumnConstraints);
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void addField(FxBulletinField field) throws Exception
	{
		FieldSpec fieldSpec = field.getFieldSpec();
		
		boolean wantsKeepWithPrevious = false;//TODO fix this (causes issues with editing groupings and view bulletin) fieldSpec.keepWithPrevious();
		boolean canKeepWithPrevious = canKeepWithNextOrPrevious(fieldSpec);
		boolean keepWithPrevious = (wantsKeepWithPrevious && canKeepWithPrevious);
		if(!keepWithPrevious)
			endCurrentRow();
			
		if(currentRow == null)
			startNewRow();
		
		if(fieldSpec.getType().isGrid())
		{
			currentRow.addGridFieldToRow((FxBulletinGridField) field);
		}
		else
		{
			currentRow.addNormalFieldToRow(field);
		}
		
		if(!canKeepWithNextOrPrevious(fieldSpec))
			endCurrentRow();
	}

	public void startNewRow()
	{
		currentRow = new BulletinEditorRow(fieldCreator, bulletin, getLocalization());
		rows.add(currentRow);
	}
	
	public void addUnexpectedErrorMessage(Exception e, String label)
	{
		MartusLogger.logException(e);
		String errorMessage = getLocalization().getFieldLabel("notifyUnexpectedErrorcause");
		addErrorMessage(label, errorMessage);
	}
	
	public void addErrorMessage(String label, String errorMessage)
	{
		endCurrentRow();
		startNewRow();
		currentRow.addErrorMessage(label, errorMessage);
		endCurrentRow();
	}

	void endCurrentRow()
	{
		if(currentRow == null)
			return;

		boolean isGrid = currentRow.isGrid();
		Node label = currentRow.getLabelNode();
		Node fields = currentRow.getFieldsNode();
		currentRow = null;

		int currentRowIndex = rows.size();
		if(isGrid)
		{
			final int BOTH_COLUMNS = 2;
			final int ONE_ROW = 1;
			add(fields, LABEL_COLUMN, currentRowIndex, BOTH_COLUMNS, ONE_ROW);
		}
		else
		{
			add(label, LABEL_COLUMN, currentRowIndex);
			add(fields, DATA_COLUMN, currentRowIndex);
		}
	}
	
	private boolean canKeepWithNextOrPrevious(FieldSpec fieldSpec)
	{
		FieldType type = fieldSpec.getType();
		
		if(type.isBoolean() || type.isDate() || type.isDateRange())
			return true;
		
		if(type.isDropdown() || type.isLanguageDropdown() || type.isNestedDropdown())
			return true;
		
		return false;
	}

	private MartusLocalization getLocalization()
	{
		return localization;
	}

	private static final int LABEL_COLUMN = 0;
	private static final int DATA_COLUMN = 1;

	private FxFieldCreator fieldCreator;
	private FxBulletin bulletin;
	private MartusLocalization localization;
	private String title;
	private BulletinEditorRow currentRow;
	private Vector<BulletinEditorRow> rows;
}