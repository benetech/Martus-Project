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

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import org.martus.client.swingui.jfx.generic.TableRowData;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FormTemplate;

public class ManageTemplatesTableRowData implements TableRowData
{
	public ManageTemplatesTableRowData(String rawTemplateNameToUse, MiniLocalization localizationToUse)
	{
		localization = localizationToUse;

		rawTemplateNameProperty = new SimpleStringProperty(rawTemplateNameToUse);
		String displayableTemplateName = FormTemplate.getDisplayableTemplateName(rawTemplateNameToUse, localization);
		displayableTemplateNameProperty = new SimpleStringProperty(displayableTemplateName);
		
		boolean isDefaultTemplate = rawTemplateNameToUse.isEmpty();
		canDeleteProperty = new SimpleBooleanProperty(!isDefaultTemplate);
		canUploadProperty = new SimpleBooleanProperty(!isDefaultTemplate);
		canEditProperty = new SimpleBooleanProperty(!isDefaultTemplate);
		
		canExportProperty = new SimpleBooleanProperty(true);
	}
	
    public String getRawTemplateName()
    {
    	return rawTemplateNameProperty().getValue();
    }
    
	public Property<String> rawTemplateNameProperty()
	{
		return rawTemplateNameProperty;
	}
	
    public String getDisplayableTemplateName()
    {
    	return displayableTemplateNameProperty().getValue();
    }
    
    public Property<String> displayableTemplateNameProperty() 
    { 
        return displayableTemplateNameProperty; 
    }
    
    public Boolean getCanDelete()
    {
    	return canDeleteProperty().getValue();
    }
    
    public Property<Boolean> canDeleteProperty()
	{
		return canDeleteProperty;
	}
    
    public Boolean getCanUpload()
    {
    	return canUploadProperty().getValue();
    }
    
    public Property<Boolean> canUploadProperty()
	{
		return canUploadProperty;
	}
    
    public Boolean getCanExport()
    {
    	return canExportProperty().getValue();
    }
    
    public Property<Boolean> canExportProperty()
	{
		return canExportProperty;
	}
    
    public Boolean getCanEdit()
    {
    	return canEditProperty().getValue();
    }
    
    public Property<Boolean> canEditProperty()
	{
		return canEditProperty;
	}
    
    // NOTE: This is required in order to be sortable using SaneComparator
    @Override
    public String toString()
    {
    	return getDisplayableTemplateName();
    }

	public static final String RAW_TEMPLATE_NAME = "rawTemplateName";
	public static final String DISPLAYABLE_TEMPLATE_NAME = "displayableTemplateName";
	public static final String CAN_DELETE_NAME = "canDelete";
	public static final String CAN_UPLOAD_NAME = "canUpload";
	public static final String CAN_EXPORT_NAME = "canExport";
	public static final String CAN_EDIT_NAME = "canEdit";

	private Property<String> rawTemplateNameProperty;
	private Property<String> displayableTemplateNameProperty;
	private Property<Boolean> canDeleteProperty;
	private Property<Boolean> canUploadProperty;
	private Property<Boolean> canExportProperty;
	private Property<Boolean> canEditProperty;
	private MiniLocalization localization;
}
