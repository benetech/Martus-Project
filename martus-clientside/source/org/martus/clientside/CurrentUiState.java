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

package org.martus.clientside;

import java.awt.Dimension;
import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.martus.common.LanguageSettingsProvider;
import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.util.DatePreference;
import org.martus.util.language.LanguageOptions;

public class CurrentUiState implements LanguageSettingsProvider
{
	public CurrentUiState()
	{
		modifyingBulletin = false;
		currentFolderName = "";
		currentDateFormat = null;
		currentLanguage = null;
		currentSortTag = "";
		currentAppDimension = new Dimension();
		currentAppPosition = new Point();
		currentEditorDimension = new Dimension();
		currentEditorPosition = new Point();
		currentCalendarSystem = MiniLocalization.GREGORIAN_SYSTEM;
		searchFinalBulletinsOnly = false;
		searchString = "";
		searchSameRowsOnly = false;
	}
	
	public int getVersion()
	{
		return VERSION;
	}

	public void setCurrentFolder(String folderName)
	{
		currentFolderName = folderName;
	}

	public String getCurrentFolder()
	{
		return currentFolderName;
	}

	public void setCurrentSortTag(String sortTag)
	{
		currentSortTag = sortTag;
	}

	public String getCurrentSortTag()
	{
		return currentSortTag;
	}

	public void setCurrentSortDirection(int sortDirection)
	{
		currentSortDirection = sortDirection;
	}

	public int getCurrentSortDirection()
	{
		return currentSortDirection;
	}

	public void setCurrentBulletinPosition(int currentPosition)
	{
		currentBulletinPosition = currentPosition;
	}

	public int getCurrentBulletinPosition()
	{
		return currentBulletinPosition;
	}

	public boolean isCurrentDefaultKeyboardVirtual()
	{
		return currentDefaultKeyboardIsVirtual;
	}

	public void setCurrentDefaultKeyboardVirtual(boolean on)
	{
		currentDefaultKeyboardIsVirtual = on;
	}

	@Override
	public String getCurrentDateFormat()
	{
		return currentDateFormat;
	}

	@Override
	public String getCurrentLanguage()
	{
		return currentLanguage;
	}

	@Override
	public void setCurrentDateFormat(String currentDateFormat)
	{
		this.currentDateFormat = currentDateFormat;
	}

	@Override
	public void setCurrentLanguage(String currentLanguage)
	{
		this.currentLanguage = currentLanguage;
	}

	public int getCurrentFolderSplitterPosition()
	{
		return getNormalizedSplitterPosition(currentLeftToRightFolderSplitterPosition);
	}

	public void setCurrentFolderSplitterPosition(int newFolderSplitterPosition)
	{
		this.currentLeftToRightFolderSplitterPosition = getNormalizedSplitterPosition(newFolderSplitterPosition);
	}

	private int getNormalizedSplitterPosition(int splitterPosition)
	{
		if(!LanguageOptions.isRightToLeftLanguage())
			return splitterPosition;
		return currentAppDimension.width - splitterPosition;
	}
	
	public int getCurrentPreviewSplitterPosition()
	{
		return currentPreviewSplitterPosition;
	}

	public void setCurrentPreviewSplitterPosition(int currentPreviewSplitterPosition)
	{
		this.currentPreviewSplitterPosition = currentPreviewSplitterPosition;
	}

	public Dimension getCurrentAppDimension()
	{
		return currentAppDimension;
	}

	public boolean isCurrentAppMaximized()
	{
		return currentAppMaximized;
	}

	public Point getCurrentAppPosition()
	{
		return currentAppPosition;
	}

	public Dimension getCurrentEditorDimension()
	{
		return currentEditorDimension;
	}

	public boolean isCurrentEditorMaximized()
	{
		return currentEditorMaximized;
	}

	public Point getCurrentEditorPosition()
	{
		return currentEditorPosition;
	}
	
	@Override
	public String getCurrentCalendarSystem()
	{
		return currentCalendarSystem;
	}

	@Override
	public boolean getAdjustThaiLegacyDates()
	{
		return true;
	}

	@Override
	public boolean getAdjustPersianLegacyDates()
	{
		return true;
	}

	@Override
	public void setDateFormatFromLanguage()
	{
		DatePreference preference = MiniLocalization.getDefaultDatePreferenceForLanguage(getCurrentLanguage());
		setCurrentDateFormat(preference.getDateTemplate());
	}

	public boolean searchFinalBulletinsOnly()
	{
		return searchFinalBulletinsOnly;
	}
	
	public String getSearchString()
	{
		return searchString;
	}
	
	public boolean searchSameRowsOnly()
	{
		return searchSameRowsOnly;
	}
	
	public void setSearchFinalBulletinsOnly(boolean searchFinalBulletinsOnly)
	{
		this.searchFinalBulletinsOnly = searchFinalBulletinsOnly;
	}
	
	public void setSearchSameRowsOnly(boolean searchSameRowsOnly)
	{
		this.searchSameRowsOnly = searchSameRowsOnly;
	}
	
	public void setSearchString(String search)
	{
		this.searchString = search;
	}
	
	public void setCurrentAppDimension(Dimension currentAppDimension)
	{
		this.currentAppDimension = currentAppDimension;
	}

	public void setCurrentAppMaximized(boolean currentAppMaximized)
	{
		this.currentAppMaximized = currentAppMaximized;
	}

	public void setCurrentAppPosition(Point currentAppPosition)
	{
		this.currentAppPosition = currentAppPosition;
	}

	public void setCurrentEditorDimension(Dimension currentEditorDimension)
	{
		this.currentEditorDimension = currentEditorDimension;
	}

	public void setCurrentEditorMaximized(boolean currentEditorMaximized)
	{
		this.currentEditorMaximized = currentEditorMaximized;
	}

	public void setCurrentEditorPosition(Point currentEditorPosition)
	{
		this.currentEditorPosition = currentEditorPosition;
	}
	
	@Override
	public void setCurrentCalendarSystem(String calendarSystem)
	{
		this.currentCalendarSystem = calendarSystem;
	}
	
	public void save()
	{
		save(currentUiStateFile);
	}

	public void save(File file)
	{
		try
		{
			currentUiStateFile = file;
			FileOutputStream outputStream = new FileOutputStream(file);
			DataOutputStream out = new DataOutputStream(outputStream);
			out.writeInt(uiStateFirstIntegerInFile);
			out.writeShort(getVersion());
			out.writeUTF(currentFolderName);
			out.writeUTF(currentSortTag);
			out.writeInt(currentSortDirection);
			out.writeInt(currentBulletinPosition);
			out.writeBoolean(currentDefaultKeyboardIsVirtual);
			out.writeUTF(currentDateFormat);
			out.writeUTF(currentLanguage);

			out.writeInt(currentPreviewSplitterPosition);
			out.writeInt(currentLeftToRightFolderSplitterPosition);

			out.writeInt(currentAppDimension.height);
			out.writeInt(currentAppDimension.width);
			out.writeInt(currentAppPosition.x);
			out.writeInt(currentAppPosition.y);
			out.writeBoolean(currentAppMaximized);

			out.writeInt(currentEditorDimension.height);
			out.writeInt(currentEditorDimension.width);
			out.writeInt(currentEditorPosition.x);
			out.writeInt(currentEditorPosition.y);
			out.writeBoolean(currentEditorMaximized);

			out.writeUTF(OPERATING_STATE_OK);
			
			out.writeUTF(currentCalendarSystem);
			
			out.writeBoolean(true); //Removed currentAdjustThaiLegacyDates
			out.writeBoolean(true); //Removed currentAdjustPersianLegacyDates

			out.writeBoolean(searchFinalBulletinsOnly);
			
			out.writeUTF(searchString);
			
			out.writeBoolean(searchSameRowsOnly);

			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
		}
	}


	public void load(File file)
	{
		currentUiStateFile = file;
		try
		{
			FileInputStream inputStream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(inputStream);
			try
			{
				load(in);
			}
			finally
			{
				in.close();
			}
		}
		catch (Exception e)
		{
			//System.out.println("CurrentUiState.load " + e);
		}
	}

	private void load(DataInputStream in) throws IOException
	{
		if(!isCorrectFileFormat(in))
			return;
		
		short version = in.readShort();
		currentFolderName = in.readUTF();
		currentSortTag = in.readUTF();
		currentSortDirection = in.readInt();
		currentBulletinPosition = in.readInt();
		currentDefaultKeyboardIsVirtual = in.readBoolean();
		currentDateFormat = in.readUTF();
		currentLanguage = in.readUTF();

		if(version < 2)
			return;
		currentPreviewSplitterPosition = in.readInt();
		currentLeftToRightFolderSplitterPosition = in.readInt();

		if(version < 3)
			return;
		
		currentAppDimension.height = in.readInt();
		currentAppDimension.width = in.readInt();
		currentAppPosition.x = in.readInt();
		currentAppPosition.y = in.readInt();
		currentAppMaximized = in.readBoolean();

		currentEditorDimension.height = in.readInt();
		currentEditorDimension.width = in.readInt();
		currentEditorPosition.x = in.readInt();
		currentEditorPosition.y = in.readInt();
		currentEditorMaximized = in.readBoolean();

		if(version < 4)
			return;
		
		in.readUTF();//OPERATING_STATE_OK

		if(version < 6)
			return;
		
		currentCalendarSystem = in.readUTF();
		
		if(version < 7)
			return;
		
		in.readBoolean(); //Removed currentAdjustThaiLegacyDates
		in.readBoolean(); //Removed currentAdjustPersianLegacyDates
		
		if(version < 8)
			return;
		
		searchFinalBulletinsOnly = in.readBoolean();
		
		if(version < 9)
			return;
		
		searchString = in.readUTF();
		
		if(version < 10)
			return;
		
		searchSameRowsOnly = in.readBoolean();

	}

	private boolean isCorrectFileFormat(DataInputStream in) throws IOException
	{
		int firstIntegerIn = 0;
		firstIntegerIn = in.readInt();
		return (firstIntegerIn == uiStateFirstIntegerInFile);
	}

	//Non Persistant variables.
	public boolean isModifyingBulletin()
	{
		return modifyingBulletin;
	}

	public void setModifyingBulletin(boolean modifyingBulletin)
	{
		this.modifyingBulletin = modifyingBulletin;
	}
	
	File currentUiStateFile;
	boolean modifyingBulletin;
	
	
	public static final short VERSION = 10;
	
	//Version 1
	protected static int uiStateFirstIntegerInFile = 2002;
	protected String currentFolderName;
	protected String currentSortTag;
	protected int currentSortDirection;
	protected int currentBulletinPosition;
	protected boolean currentDefaultKeyboardIsVirtual = false;
	protected String currentDateFormat;
	protected String currentLanguage;

	//Version 2
	protected int currentPreviewSplitterPosition = 100;
	protected int currentLeftToRightFolderSplitterPosition = 180;

	//Version 3
	protected Dimension currentAppDimension;
	protected Point currentAppPosition;
	protected boolean currentAppMaximized;

	protected Dimension currentEditorDimension;
	protected Point currentEditorPosition;
	protected boolean currentEditorMaximized;

	//Version 4
	//protected String currentOperatingState;
	public static final String OPERATING_STATE_OK = "OK";
	//public static final String OPERATING_STATE_UNKNOWN = "UNKNOWN";
	//public static final String OPERATING_STATE_BAD = "BAD";
	
	//Version 5 
	//Removed currentOperatingState
	
	//Version 6
	public String currentCalendarSystem; 
	
	//Version 7
	//Removed private boolean currentAdjustThaiLegacyDates;
	//Removed private boolean currentAdjustPersianLegacyDates;
	
	//Version 8
	public boolean searchFinalBulletinsOnly;
	
	//Version 9
	public String searchString;

	//Version 10
	private boolean searchSameRowsOnly;


}
