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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.jfx.generic.TableRowData;
import org.martus.client.swingui.jfx.landing.bulletins.BulletinDetailsController;
import org.martus.client.swingui.tablemodels.RetrieveTableModel;
import org.martus.common.BulletinSummary;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;
import org.martus.swing.FontHandler;

public class ServerSyncTableRowData implements TableRowData
{
	public ServerSyncTableRowData(BulletinSummary summaryToUse, boolean canDelete, MartusApp app) throws Exception
	{
		uid = summaryToUse.getUniversalId();
		canDeleteFromServer = new SimpleBooleanProperty(canDelete);
		canUploadToServer = new SimpleBooleanProperty(false);
		setLocation(app, LOCATION_SERVER);
		isLocal = new SimpleBooleanProperty(false);
		isRemote = new SimpleBooleanProperty(true);
		UiFontEncodingHelper fontHelper = new UiFontEncodingHelper(FontHandler.isDoZawgyiConversion());
		String rawTitle = summaryToUse.getStorableTitle();
		setTitle(fontHelper.getDisplayable(rawTitle));
		setAuthor(app, summaryToUse.getAccountId());
		setLastSavedDate(app, summaryToUse.getDateTimeSaved());
		setSize(summaryToUse.getSize());
	}	
	
	public ServerSyncTableRowData(Bulletin bulletin, int sizeOfBulletinBytes, int locationOfBulletin, MartusApp app) throws Exception
	{
		canDeleteFromServer = new SimpleBooleanProperty(false);
		String ourAccountId = app.getAccountId();
		boolean authorizedToUpload = bulletin.getBulletinHeaderPacket().isAuthorizedToUpload(ourAccountId);
		canUploadToServer = new SimpleBooleanProperty(authorizedToUpload);
		
		isLocal = new SimpleBooleanProperty(true);
		isRemote = new SimpleBooleanProperty(false);
		uid = bulletin.getUniversalId();
		setLocation(app, locationOfBulletin);
		setTitle(bulletin.get(Bulletin.TAGTITLE));
		setAuthor(app, bulletin.getAccount());
		setLastSavedDate(app, bulletin.getBulletinHeaderPacket().getLastSavedTime());
		setSize(sizeOfBulletinBytes);
	}


	public void setSize(Integer sizeOfBulletinBytes)
	{
		size = new SimpleIntegerProperty(RetrieveTableModel.getSizeInKbytes(sizeOfBulletinBytes));
	}


	public void setLastSavedDate(MartusApp app, long dateLastSaved)
	{
		dateSaved = new SimpleStringProperty(app.getLocalization().formatDateTime(dateLastSaved));
	}


	public void setAuthor(MartusApp app, String account) throws Exception
	{
		String authorName = BulletinDetailsController.getAuthorName(app, account);
		author = new SimpleStringProperty(authorName);
	}

	public void setTitle(String titleToUse)
	{
		title = new SimpleStringProperty(titleToUse);
	}

	public void setLocation(MartusApp app, int locationOfBulletin)
	{
		rawLocation = locationOfBulletin;
		location = new SimpleStringProperty(getLocationString(locationOfBulletin, app.getLocalization()));
		boolean local = (locationOfBulletin == LOCATION_LOCAL || locationOfBulletin == LOCATION_BOTH);
		isLocal = new SimpleBooleanProperty(local);
		boolean remote = (locationOfBulletin == LOCATION_SERVER || locationOfBulletin == LOCATION_BOTH);
		isRemote = new SimpleBooleanProperty(remote);
	}
	
	private String getLocationString(int locationOfBulletin, MiniLocalization localization)
	{
		if(locationOfBulletin == LOCATION_LOCAL)
			return localization.getFieldLabel("RecordLocationLocal");
		if(locationOfBulletin == LOCATION_SERVER)
			return localization.getFieldLabel("RecordLocationServer");
		if(locationOfBulletin == LOCATION_BOTH)
			return localization.getFieldLabel("RecordLocationBothLocalAndServer");
		return localization.getFieldLabel("RecordLocationUnknown");
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

    public String getDateSaved()
	{
		return dateSaved.get();
	}
	
    public SimpleStringProperty dateSavedProperty() 
    { 
        return dateSaved; 
    }

	public String getLocation()
	{
		return location.getValue();
	}

	public SimpleStringProperty locationProperty()
	{
		return location;
	}
	
	public int getRawLocation()
	{
		return rawLocation;
	}

    public Integer getSize() 
    {
    		return size.getValue();
    }
 
    public SimpleIntegerProperty sizeProperty() 
    {
    		return size;
    }
    
    public BooleanProperty canDeleteFromServerProperty()
    {
    		return canDeleteFromServer;
    }
    
    public BooleanProperty canUploadToServerProperty()
    {
    		return canUploadToServer;
    }
    
    public BooleanProperty isLocal()
    {
    		return isLocal;
    }

    public BooleanProperty isRemote()
    {
    		return isRemote;
    }

    static public final int LOCATION_LOCAL = 0;
    static public final int LOCATION_SERVER = 1;
    static public final int LOCATION_BOTH = 2;
    static public final int LOCATION_ANY = 3;
       
    static public final String LOCATION_PROPERTY_NAME = "location";
    static public final String TITLE_PROPERTY_NAME = "title";
    static public final String AUTHOR_PROPERTY_NAME = "author";
    static public final String DATE_SAVDED_PROPERTY_NAME = "dateSaved";
    static public final String SIZE_PROPERTY_NAME = "size";
    
	private SimpleStringProperty location;
    private SimpleStringProperty title;
	private SimpleStringProperty author;
	private SimpleStringProperty dateSaved;
	private SimpleIntegerProperty size;
	
	private final UniversalId uid;
	private int 	rawLocation;
	private BooleanProperty canDeleteFromServer;
	private BooleanProperty canUploadToServer;
	private BooleanProperty isLocal;
	private BooleanProperty isRemote;
}
