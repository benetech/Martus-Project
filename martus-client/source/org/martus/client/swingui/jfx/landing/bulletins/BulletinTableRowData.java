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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.jfx.generic.TableRowData;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;
import org.martus.swing.FontHandler;

public class BulletinTableRowData implements TableRowData
{
	public BulletinTableRowData(Bulletin bulletin, boolean onServer, Integer authorsValidation, MiniLocalization localization)
	{
		UiFontEncodingHelper fontHelper = new UiFontEncodingHelper(FontHandler.isDoZawgyiConversion());

		uid = bulletin.getUniversalId();
		String storableTitle = bulletin.get(Bulletin.TAGTITLE);
		String displayableTitle = fontHelper.getDisplayable(storableTitle);
		title = new SimpleStringProperty(displayableTitle);
		String storableAuthor = bulletin.get(Bulletin.TAGAUTHOR);
		String displayableAuthor = fontHelper.getDisplayable(storableAuthor);
		author = new SimpleStringProperty(displayableAuthor);
		long dateLastSaved = bulletin.getBulletinHeaderPacket().getLastSavedTime();
		dateSaved = new SimpleLongProperty(dateLastSaved);
		this.onServer = new SimpleBooleanProperty(onServer);
		canView = new SimpleBooleanProperty(true);
		canEdit = new SimpleBooleanProperty(true);
		authorVerified = new SimpleIntegerProperty(authorsValidation);
	}
	
	public UniversalId getUniversalId()
	{
		return uid;
	}
	
	public String getTitle()
	{
		return title.get();
	}

    public SimpleStringProperty titleProperty() 
    { 
        return title; 
    }
	
	public String getAuthor()
	{
		return author.get();
	}

    public SimpleStringProperty authorProperty() 
    { 
        return author; 
    }

    public Long getDateSaved()
	{
		return dateSaved.get();
	}
	
    public SimpleLongProperty dateSavedProperty() 
    { 
        return dateSaved; 
    }

	public boolean isOnServer()
	{
		return onServer.get();
	}

    public SimpleBooleanProperty onServerProperty() 
    {
    		return onServer;
    }

    public SimpleIntegerProperty authorVerifiedProperty() 
    {
    		return authorVerified;
    }

    public Boolean getCanView()
	{
		return canViewProperty().getValue();
	}
	
    public Property<Boolean> canViewProperty() 
    { 
        return canView; 
    }

    public Boolean getEditBulletin()
 	{
 		return canEditProperty().getValue();
 	}
 	
     public Property<Boolean> canEditProperty() 
     { 
         return canEdit;
     }

    static public final String TITLE_PROPERTY_NAME = "title";
    static public final String AUTHOR_PROPERTY_NAME = "author";
    static public final String DATE_SAVED_PROPERTY_NAME = "dateSaved";
    static public final String ON_SERVER_PROPERTY_NAME = "onServer";
    static public final String CAN_VIEW_PROPERTY_NAME = "canView";
    static public final String CAN_EDIT_PROPERTY_NAME = "canEdit";
    static public final String AUTHOR_VERIFIED_PROPERTY_NAME = "authorVerified";
    
    private final SimpleStringProperty title;
	private final SimpleStringProperty author;
	private final SimpleLongProperty dateSaved;
	private final SimpleBooleanProperty onServer;
	private final SimpleBooleanProperty canView;
	private final SimpleBooleanProperty canEdit;
	private final SimpleIntegerProperty authorVerified;
	
	private final UniversalId uid;
}
