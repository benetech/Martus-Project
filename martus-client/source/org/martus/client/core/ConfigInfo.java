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

package org.martus.client.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import org.martus.common.FieldSpecCollection;
import org.martus.common.LegacyCustomFields;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusLogger;
import org.martus.common.MartusAccountAccessToken.TokenInvalidException;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.StandardFieldSpecs;

public class ConfigInfo
{
	public ConfigInfo()
	{
		martusAccountAccessTokens = new Vector();
		clear();
	}

	public void setAuthor(String newSource)		{ author = newSource; }
	public void setOrganization(String newOrg)		{ organization = newOrg; }
	public void setEmail(String newEmail)			{ email = newEmail; }
	public void setWebPage(String newWebPage)		{ webPage = newWebPage; }
	public void setPhone(String newPhone)			{ phone = newPhone; }
	public void setAddress(String newAddress)		{ address = newAddress; }
	public void setServerName(String newServerName){ serverName = newServerName; }
	public void setServerPublicKey(String newServerPublicKey){serverPublicKey = newServerPublicKey; }
	public void setTemplateDetails(String newTemplateDetails){ templateDetails = newTemplateDetails; }
	public void setLegacyHQKey(String newHQKey)			{ deprecatedLegacyHQKey = newHQKey; }
	public void setServerCompliance(String newCompliance) {serverCompliance = newCompliance;}
	public void setCustomFieldLegacySpecs(String newSpecs)	{customFieldLegacySpecs = newSpecs;}
	public void setForceBulletinsAllPrivate(boolean newForceBulletinsAllPrivate)	{forceBulletinsAllPrivate = newForceBulletinsAllPrivate; }
	public void setBackedUpKeypairEncrypted(boolean newBackedUpKeypairEncrypted)	{backedUpKeypairEncrypted = newBackedUpKeypairEncrypted; }
	public void setBackedUpKeypairShare(boolean newBackedUpKeypairShare)	{backedUpKeypairShare = newBackedUpKeypairShare; }
	public void setAllHQKeysXml(String allHQKeysXml){this.deprecatedAllHQKeysXml = allHQKeysXml;}
	public void setBulletinVersioningAware(boolean newBulletinVersioningAware){this.bulletinVersioningAware = newBulletinVersioningAware;}
	public void setDefaultHQKeysXml(String defaultHQKeysXml){this.deprecatedDefaultHQKeysXml = defaultHQKeysXml;}
	public void deprecatedSetCustomFieldTopSectionXml(String newXml)	{customFieldTopSectionXml = newXml;}
	public void deprecatedSetCustomFieldBottomSectionXml(String newXml)	{customFieldBottomSectionXml = newXml;}
	public void setUseZawgyiFont(boolean newUseZawgyiFont){useZawgyiFontProperty.setValue(newUseZawgyiFont);}
	public void setFieldDeskKeysXml(String newFieldDeskKeysXml) { deprecatedFieldDeskKeysXml = newFieldDeskKeysXml; }
	public void setBackedUpImprovedKeypairShare(boolean newBackedUpImprovedKeypairShare) {backedUpImprovedKeypairShare = newBackedUpImprovedKeypairShare;}
	public void setUseInternalTor(boolean newUseInternalTor) { useInternalTorProperty.setValue(newUseInternalTor);}
	public void setMartusAccountAccessTokens(Vector newTokens) { martusAccountAccessTokens = newTokens;} 
	public void setCurrentMartusAccountAccessToken(MartusAccountAccessToken newToken) 
	{
		Vector tokenList = new Vector();
		tokenList.add(newToken);
		setMartusAccountAccessTokens(tokenList);
	}
	public void setContactKeysXml(String contactKeysXml){this.contactKeysXml = contactKeysXml;}
	public void deprecatedSetCurrentFormTemplateTitle(String netFormTemplateTitle) { currentFormTemplateTitle = netFormTemplateTitle; }
	public void deprecatedSetCurrentFormTemplateDescription(String netFormTemplateDescription) { currentFormTemplateDescription = netFormTemplateDescription; }
	public void setOnStartupServerOnlineStatus(boolean newState) { onStartupServerOnlineStatus = newState; }
	public void setFolderLabelCode(String newCode) { folderLabelCode = newCode; }
	public void setFolderLabelCustomName(String newName) { folderLabelCustomName = newName; }
	public void setSyncStatusJson(String newSyncStatusJson) { syncStatusJson = newSyncStatusJson; }
	public void setSyncFrequencyMinutes(String newSyncFrequency) { syncFrequencyMinutes = newSyncFrequency; }
	public void setDidTemplateMigration(boolean newValue) { didTemplateMigrationProperty.setValue(newValue); }
	public void setAlwaysImmutableOnServer(boolean newValue) { alwaysImmutableOnServer.setValue(newValue); }
	public void setDateLastAskedUserToBackupKeypair(String newDate) {dateLastAskedUserToBackupKeypair.setValue(newDate); }

	public void clearLegacyHQKey()						{ deprecatedLegacyHQKey = ""; }

	public short getVersion()			{ return version; }
	public String getAuthor()			{ return author; }
	public String getOrganization()	{ return organization; }
	public String getEmail()			{ return email; }
	public String getWebPage()			{ return webPage; }
	public String getPhone()			{ return phone; }
	public String getAddress()			{ return address; }
	public String getServerName()		{ return serverName; }
	public String getServerPublicKey()	{ return serverPublicKey; }
	public String getTemplateDetails() { return templateDetails; }
	public String getLegacyHQKey() 			{ return deprecatedLegacyHQKey; }
	public String getServerCompliance() {return serverCompliance;}
	public String getCustomFieldLegacySpecs() {return customFieldLegacySpecs;}
	public boolean shouldForceBulletinsAllPrivate()	{ return forceBulletinsAllPrivate;}
	public boolean hasUserBackedUpKeypairEncrypted()	{ return backedUpKeypairEncrypted;}
	public boolean hasUserBackedUpKeypairShare()	{ return backedUpKeypairShare;}
	public String getAllHQKeysXml()		{return deprecatedAllHQKeysXml;}
	public boolean isBulletinVersioningAware()	{return bulletinVersioningAware;}
	public String getDefaultHQKeysXml()		{return deprecatedDefaultHQKeysXml;}
	public String getNoLongerUsedCustomFieldTopSectionXml()	{return customFieldTopSectionXml;}
	public String getNoLongerUsedCustomFieldBottomSectionXml() {return customFieldBottomSectionXml;}
	public boolean getUseZawgyiFont() {return useZawgyiFontProperty.getValue();}
	public Property<Boolean> getUseZawgyiFontProperty() {return useZawgyiFontProperty;}
	public String getFieldDeskKeysXml() { return deprecatedFieldDeskKeysXml; }
	public boolean hasBackedUpImprovedKeypairShare() {return backedUpImprovedKeypairShare;}
	public boolean getDoZawgyiConversion() {return true;}
	public boolean useInternalTor() {return useInternalTorProperty.getValue();}
	public Property<Boolean> useInternalTorProperty() {return useInternalTorProperty;}
	public Vector getMartusAccountAccessTokens() { return martusAccountAccessTokens;}
	public boolean hasMartusAccountAccessToken() 
	{
		int numTokens = martusAccountAccessTokens.size();
		return (numTokens == 1);
	}
	public MartusAccountAccessToken getCurrentMartusAccountAccessToken() throws TokenInvalidException
	{ 
		if(!hasMartusAccountAccessToken())
			throw new TokenInvalidException();
		return (MartusAccountAccessToken)martusAccountAccessTokens.get(0);
	} 
	public String getContactKeysXml() {return contactKeysXml;}
	public String getNoLongerUsedCurrentFormTemplateTitle()  { return currentFormTemplateTitle;}
	public String getNoLongerUsedCurrentFormTemplateDescription()  { return currentFormTemplateDescription;}
	public boolean getOnStartupServerOnlineStatus() { return onStartupServerOnlineStatus; }
	public String getFolderLabelCode() { return folderLabelCode; }
	public String getFolderLabelCustomName() { return folderLabelCustomName; }
	public String getSyncStatusJson() {	return syncStatusJson; }
	public String getSyncFrequencyMinutes() {	return syncFrequencyMinutes; }
	public boolean getDidTemplateMigration() { return didTemplateMigrationProperty.getValue(); }
	public boolean getAlwaysImmutableOnServer() { return alwaysImmutableOnServer.getValue(); }
	public String getDateLastAskedUserToBackupKeypair() { return dateLastAskedUserToBackupKeypair.getValue(); }
	
	public boolean isServerConfigured()
	{
		return (serverName.length()>0 && serverPublicKey.length()>0);
	}
	
	public boolean isNewVersion()
	{
		return version > VERSION;
	}
	
	public boolean shouldShowOneTimeNoticeFortheRemovalOfPublicBulletins() 
	{
		return  getVersion() < VERSION_MIGRATE_TO_PRIVATE_ALWAYS;
	}
	
	public void clear()
	{
		version = VERSION;
		author = "";
		organization = "";
		email = "";
		webPage = "";
		phone = "";
		address = "";
		serverName = "";
		serverPublicKey="";
		templateDetails = "";
		deprecatedLegacyHQKey = "";
		deprecatedSendContactInfoToServer = false;
		serverCompliance = "";
		customFieldLegacySpecs = LegacyCustomFields.buildFieldListString(StandardFieldSpecs.getDefaultTopSectionFieldSpecs());
		forceBulletinsAllPrivate = false;
		backedUpKeypairEncrypted = false;
		backedUpKeypairShare = false;
		deprecatedAllHQKeysXml = "";
		bulletinVersioningAware = true;
		deprecatedDefaultHQKeysXml = "";
		customFieldTopSectionXml = "";
		customFieldBottomSectionXml = "";
		useZawgyiFontProperty = new SimpleBooleanProperty();
		deprecatedFieldDeskKeysXml = "";
		backedUpImprovedKeypairShare = false;
		useInternalTorProperty = new SimpleBooleanProperty();
		martusAccountAccessTokens.clear(); 
		contactKeysXml = "";
		currentFormTemplateTitle = "";
		currentFormTemplateDescription = "";
		onStartupServerOnlineStatus = true;
		folderLabelCode = "";
		folderLabelCustomName = "";
		syncStatusJson = "";
		syncFrequencyMinutes = "";
		didTemplateMigrationProperty = new SimpleBooleanProperty();
		alwaysImmutableOnServer = new SimpleBooleanProperty();
		dateLastAskedUserToBackupKeypair = new SimpleStringProperty("");
	}

	public static ConfigInfo load(InputStream inputStream) throws IOException
	{
		ConfigInfo loaded =  new ConfigInfo();

		DataInputStream in = new DataInputStream(inputStream);
		try
		{
			loaded.version = in.readShort();
			loaded.author = in.readUTF();
			loaded.organization = in.readUTF();
			loaded.email = in.readUTF();
			loaded.webPage = in.readUTF();
			loaded.phone = in.readUTF();
			loaded.address = in.readUTF();
			loaded.serverName = in.readUTF();
			loaded.templateDetails = in.readUTF();
			loaded.deprecatedLegacyHQKey = in.readUTF();
			loaded.serverPublicKey = in.readUTF();
						
			if(loaded.version >= 2)
				loaded.deprecatedSendContactInfoToServer = in.readBoolean();
				
			if(loaded.version >= 4)
				loaded.serverCompliance = in.readUTF();
				
			if(loaded.version >= 5)
				loaded.customFieldLegacySpecs = in.readUTF();
				
			if(loaded.version >= 6)
				loaded.customFieldTopSectionXml = in.readUTF(); //legacyCustomFieldTopSectionXml

			if(loaded.version >= 7)
				loaded.forceBulletinsAllPrivate = in.readBoolean();

			if(loaded.version >= 8)
			{
				loaded.backedUpKeypairEncrypted = in.readBoolean();
				loaded.backedUpKeypairShare = in.readBoolean();
			}
			if(loaded.version >= 9)
				loaded.deprecatedAllHQKeysXml = in.readUTF();
			
			if(loaded.version >= 10)
				loaded.bulletinVersioningAware = in.readBoolean();
			else
				loaded.bulletinVersioningAware = false;

			if(loaded.version >= 11)
				loaded.deprecatedDefaultHQKeysXml = in.readUTF();

			if(loaded.version >= 12)
				loaded.customFieldBottomSectionXml = in.readUTF(); //legacyCustomFieldBottomSectionXml

			if(loaded.version >= 13)
				in.readBoolean(); //checkForFieldOfficeBulletins not used

			if(loaded.version >= 14)
			{
				loaded.customFieldTopSectionXml = readLongString(in);
				loaded.customFieldBottomSectionXml = readLongString(in);
			}

            if(loaded.version >= 15)
                loaded.useZawgyiFontProperty.setValue(in.readBoolean());
            
            if(loaded.version >= 16)
            	loaded.deprecatedFieldDeskKeysXml = readLongString(in);

			if(loaded.version >= 17)
				loaded.backedUpImprovedKeypairShare = in.readBoolean();
			
			if(loaded.version >= 18)
				loaded.useInternalTorProperty.setValue(in.readBoolean());
			
			if(loaded.version >= 19)
			{
				int numTokens = in.readInt();
				Vector loadedTokens = new Vector();
				for(int i = 0 ; i < numTokens; ++i)
				{
					String rawToken = in.readUTF();
					try
					{
						loadedTokens.add(new MartusAccountAccessToken(rawToken));
					} 
					catch (TokenInvalidException e)
					{
						MartusLogger.log("ConfigInfo.Load MartusAccountAccessToken Invalid: " + rawToken);
						// NOTE: We would like to throw here, but that 
						// would have the side effect of wiping out all 
						// config info any time the token validity rules 
						// change, such as from beta pre-4.5 to the 
						// 4.5 release. So at least for now, we will just 
						// log the error and blank out the old token.
						loadedTokens = new Vector();
					}
				}
				loaded.setMartusAccountAccessTokens(loadedTokens);
			}
			if(loaded.version >= 20)
			{
				loaded.contactKeysXml = readLongString(in);
			}
			if(loaded.version >= 21)
			{
			 	loaded.currentFormTemplateTitle = readLongString(in);
			 	loaded.currentFormTemplateDescription = readLongString(in);
			}
			if(loaded.version >= 22)
			{
				loaded.onStartupServerOnlineStatus = in.readBoolean();
			}
			if(loaded.version >= 23)
			{
				loaded.folderLabelCode = in.readUTF();
				loaded.folderLabelCustomName = in.readUTF();
			}
			if(loaded.version >= 24)
			{
				loaded.syncStatusJson = readLongString(in);
			}
			if(loaded.version >= 25)
			{
				loaded.syncFrequencyMinutes = in.readUTF();
			}
			if(loaded.version >= 26)
			{
				loaded.didTemplateMigrationProperty.setValue(in.readBoolean());
			}
			if(loaded.version >= 27)
			{
				loaded.alwaysImmutableOnServer.setValue(in.readBoolean());
			}
			//Version 28 MIGRATE_TO_PRIVATE_ALWAYS
			if(loaded.version >= 29)
			{
				loaded.dateLastAskedUserToBackupKeypair.setValue(in.readUTF());
			}
		}
		finally
		{
			in.close();
		}

		return loaded;
	}

	public void save(OutputStream outputStream) throws IOException
	{
		DataOutputStream out = new DataOutputStream(outputStream);
		try
		{
			out.writeShort(VERSION);
			out.writeUTF(author);
			out.writeUTF(organization);
			out.writeUTF(email);
			out.writeUTF(webPage);
			out.writeUTF(phone);
			out.writeUTF(address);
			out.writeUTF(serverName);
			out.writeUTF(templateDetails);
			out.writeUTF(deprecatedLegacyHQKey);
			out.writeUTF(serverPublicKey);
			out.writeBoolean(deprecatedSendContactInfoToServer);
			out.writeUTF(serverCompliance);
			out.writeUTF(customFieldLegacySpecs);
			out.writeUTF("");//legacyCustomFieldTopSectionXml
			out.writeBoolean(forceBulletinsAllPrivate);
			out.writeBoolean(backedUpKeypairEncrypted);
			out.writeBoolean(backedUpKeypairShare);
			out.writeUTF(deprecatedAllHQKeysXml);
			out.writeBoolean(bulletinVersioningAware);
			out.writeUTF(deprecatedDefaultHQKeysXml);
			out.writeUTF(""); //legacyCustomFieldBottomSectionXml
			out.writeBoolean(false); //checkForFieldOfficeBulletins
			writeLongString(out, customFieldTopSectionXml);
			writeLongString(out, customFieldBottomSectionXml);
            out.writeBoolean(useZawgyiFontProperty.getValue());
            writeLongString(out, deprecatedFieldDeskKeysXml);
			out.writeBoolean(backedUpImprovedKeypairShare);
			out.writeBoolean(useInternalTorProperty.getValue());
			int numTokens = martusAccountAccessTokens.size(); 
			out.writeInt(numTokens);
			for(int i = 0; i < numTokens; ++i)
			{
				out.writeUTF(((MartusAccountAccessToken)martusAccountAccessTokens.get(i)).getToken());
			}
			writeLongString(out,contactKeysXml);
			writeLongString(out, currentFormTemplateTitle);
			writeLongString(out, currentFormTemplateDescription);
			out.writeBoolean(onStartupServerOnlineStatus);
			out.writeUTF(folderLabelCode);
			out.writeUTF(folderLabelCustomName);
			writeLongString(out, syncStatusJson);
			out.writeUTF(syncFrequencyMinutes);
			out.writeBoolean(didTemplateMigrationProperty.getValue());
			out.writeBoolean(alwaysImmutableOnServer.getValue());
			out.writeUTF(dateLastAskedUserToBackupKeypair.getValue());
		}
		finally
		{
			out.close();
		}
	}
	
	public boolean hasLegacyFormTemplate()
	{
		if(getDidTemplateMigration())
			return false;
		
		if(getNoLongerUsedCurrentFormTemplateTitle().length() > 0)
			return true;
		if(getNoLongerUsedCurrentFormTemplateDescription().length() > 0)
			return true;
		if(getNoLongerUsedCustomFieldTopSectionXml().length() > 0)
			return true;
		if(getNoLongerUsedCustomFieldBottomSectionXml().length() > 0)
			return true;
		
		return false;
	}

	public FormTemplate getLegacyFormTemplate() throws Exception
	{
		String title = getNoLongerUsedCurrentFormTemplateTitle();
		String description = getNoLongerUsedCurrentFormTemplateDescription();
		FieldSpecCollection top = MartusApp.getCustomFieldSpecsTopSection(this);
		FieldSpecCollection bottom = MartusApp.getCustomFieldSpecsBottomSection(this);
		FormTemplate existing = new FormTemplate(title, description, top, bottom);
		return existing;
	}

	public static void writeLongString(DataOutputStream out, String data) throws IOException
	{
		byte[] bytes = data.getBytes("UTF-8");
		out.writeInt(bytes.length);
		for(int i = 0; i < bytes.length; ++i)
			out.writeByte(bytes[i]);
	}
	
	public static String readLongString(DataInputStream in) throws IOException
	{
		int length = in.readInt();
		byte[] bytes = new byte[length];
		for(int i = 0; i < bytes.length; ++i)
			bytes[i] = in.readByte();
		return new String(bytes, "UTF-8");
	}
	
	public static final short VERSION = 29;

	//Version 1
	private short version;
	private String author;
	private String organization;
	private String email;
	private String webPage;
	private String phone;
	private String address;
	private String serverName;
	private String serverPublicKey;
	private String templateDetails;
	private String deprecatedLegacyHQKey;
	//Version 2
	private boolean deprecatedSendContactInfoToServer;
	//Version 3 flag to indicate AccountMap.txt is signed.
	//Version 4
	private String serverCompliance;
	//Version 5
	private String customFieldLegacySpecs;
	//Version 6
		// was: private String legacyCustomFieldTopSectionXml;
	//Version 7
	private boolean forceBulletinsAllPrivate;
	//Version 8
	private boolean backedUpKeypairEncrypted;
	private boolean backedUpKeypairShare;
	//Version 9
	private String deprecatedAllHQKeysXml;
	//Version 10 
	private boolean bulletinVersioningAware;
	//Version 11
	private String deprecatedDefaultHQKeysXml;
	//Version 12
		// was: private String legacyCustomFieldBottomSectionXml;
	//Version 13
		// was: private boolean checkForFieldOfficeBulletins;
	//Version 14
	private String customFieldTopSectionXml;
	private String customFieldBottomSectionXml;
    //Version 15
    private Property <Boolean>  useZawgyiFontProperty; 
    //Version 16
    private String deprecatedFieldDeskKeysXml;
	//Version 17
	private boolean backedUpImprovedKeypairShare;
	//Version 18
	private Property <Boolean> useInternalTorProperty;
	//Version 19
	private Vector martusAccountAccessTokens;
	//Version 20
	public static final short VERSION_WITH_CONTACT_KEYS = 20;
	private String contactKeysXml;
	//Version 21
	private String currentFormTemplateTitle;
	private String currentFormTemplateDescription;
	//Version 22
	private boolean onStartupServerOnlineStatus;
	//Version 23
	private String folderLabelCode;
	private String folderLabelCustomName;
	//Version 24
	private String syncStatusJson;
	//Version 25
	private String syncFrequencyMinutes;
	//Version 26
	private Property<Boolean> didTemplateMigrationProperty;
	//Version 27
	private Property<Boolean> alwaysImmutableOnServer;
	//Version 28
	public static final short VERSION_MIGRATE_TO_PRIVATE_ALWAYS = 28;
	//Version 29
	private Property<String> dateLastAskedUserToBackupKeypair;
}
