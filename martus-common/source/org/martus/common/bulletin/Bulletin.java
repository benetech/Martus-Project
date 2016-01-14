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

package org.martus.common.bulletin;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.martus.common.Exceptions.InvalidBulletinStateException;
import org.martus.common.FieldSpecCollection;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.SessionKey;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FieldTypeUnknown;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.ExtendedHistoryEntry;
import org.martus.common.packet.ExtendedHistoryList;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.DateUtilities;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


public class Bulletin implements BulletinConstants
{
	public static class DamagedBulletinException extends Exception
	{
	}

	public Bulletin(MartusCrypto securityToUse) throws Exception
	{
		this(securityToUse, StandardFieldSpecs.getDefaultTopSectionFieldSpecs(), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
	}
	
	public Bulletin(MartusCrypto securityToUse, BulletinType bulletinType, FieldSpecCollection publicFieldSpecs, FieldSpecCollection privateFieldSpecs) throws Exception
	{
		this(securityToUse, BulletinHeaderPacket.createUniversalId(securityToUse, bulletinType), FieldDataPacket.createUniversalId(securityToUse), FieldDataPacket.createUniversalId(securityToUse), publicFieldSpecs, privateFieldSpecs);
	}

	public Bulletin(MartusCrypto securityToUse, FieldSpecCollection publicFieldSpecs, FieldSpecCollection privateFieldSpecs) throws Exception
	{
		this(securityToUse, BulletinHeaderPacket.createUniversalId(securityToUse), FieldDataPacket.createUniversalId(securityToUse), FieldDataPacket.createUniversalId(securityToUse), publicFieldSpecs, privateFieldSpecs);
	}

	public Bulletin(MartusCrypto securityToUse, UniversalId headerUid, UniversalId publicDataUid, UniversalId privateDataUid, FieldSpecCollection publicFieldSpecs, FieldSpecCollection privateFieldSpecs) throws Exception
	{
		security = securityToUse;
		isNonAttachmentDataValidFlag = true;

		header = createHeaderPacket(headerUid);

		fieldData = createPublicFieldDataPacket(publicDataUid, publicFieldSpecs);
		fieldData.setEncrypted(true);
		header.setFieldDataPacketId(publicDataUid.getLocalId());
		
		privateFieldData = createPrivateFieldDataPacket(privateDataUid, privateFieldSpecs);
		privateFieldData.setEncrypted(true);
		header.setPrivateFieldDataPacketId(privateDataUid.getLocalId());
		
		clearAllUserData();
	}

	public MartusCrypto getSignatureGenerator()
	{
		return security;
	}

	public UniversalId getUniversalId()
	{
		return getBulletinHeaderPacket().getUniversalId();
	}

	public String getUniversalIdString()
	{
		return getUniversalId().toString();
	}

	public String getAccount()
	{
		String accountId = getBulletinHeaderPacket().getAccountId();
		if(accountId == null)
		{
			MartusLogger.logWarning("No account Id found for: " + getLocalId());
		}
		return accountId;
	}

	public String getLocalId()
	{
		return getBulletinHeaderPacket().getLocalId();
	}

	public void setIsNonAttachmentDataValid(boolean isValid)
	{
		isNonAttachmentDataValidFlag = isValid;
	}

	public boolean isNonAttachmentDataValid()
	{
		return isNonAttachmentDataValidFlag;
	}
	
	public String toFileName()
	{
		String bulletinTitle = get(Bulletin.TAGTITLE);
		return MartusUtilities.toFileName(bulletinTitle, MartusUtilities.DEFAULT_FILE_NAME);
	}
	
	public boolean hasUnknownTags()
	{
		if(getBulletinHeaderPacket().hasUnknownTags())
			return true;
		if(getFieldDataPacket().hasUnknownTags())
			return true;
		if(getPrivateFieldDataPacket().hasUnknownTags())
			return true;
		return false;
	}
	
	public boolean hasUnknownCustomField()
	{
		FieldDataPacket fdp = getFieldDataPacket();
		FieldSpec[] specs = fdp.getFieldSpecs().asArray();
		for(int i=0; i < specs.length; ++i)
		{
			if(specs[i].hasUnknownStuff())
				return true;
		}
		
		return false;
	}

	public long getLastSavedTime()
	{
		return getBulletinHeaderPacket().getLastSavedTime();
	}
	
	public String getLastSavedDate()
	{
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date(getLastSavedTime()));
		return new MultiCalendar(cal).toIsoDateString();
	}
	
	public void setHistory(BulletinHistory newHistory)
	{
		getBulletinHeaderPacket().setHistory(newHistory);
	}
	
	public BulletinHistory getHistory()
	{
		return getBulletinHeaderPacket().getHistory();
	}
	
	public int getVersion()
	{
		return getBulletinHeaderPacket().getVersionNumber();
	}

	public boolean isMutable()
	{
		return isMutable(getStatus());	
	}
	
	public static boolean isMutable(String status)
	{
		return status.equals(STATUSMUTABLE);	
	}

	public boolean isImmutable()
	{
		return isImmutable(getStatus());
	}

	public static boolean isImmutable(String status)
	{
		return status.equals(STATUSIMMUTABLE);	
	}

	public void setMutable()
	{
		setStatus(STATUSMUTABLE);
	}

	public void setImmutable()
	{
		setStatus(STATUSIMMUTABLE);
	}

	public void setStatus(String newStatus)
	{
		getBulletinHeaderPacket().setStatus(newStatus);
	}

	public String getStatus()
	{
		return getBulletinHeaderPacket().getStatus();
	}
	
	//NOTE: getState() used in unit tests and for Documentation purposes only.
	public BulletinState getState()
	{
		boolean hasAuthorizedHQs = hasAuthorizedHQs();
		if(isSnapshot())
		{
			if(hasAuthorizedHQs)
				return BulletinState.STATE_SHARED;
			return BulletinState.STATE_SNAPSHOT;

		}
		if(isMutable())
		{
			if(hasAuthorizedHQs)
				return BulletinState.STATE_LEGACY_DRAFT;
			return BulletinState.STATE_SAVE;
		}
		return BulletinState.STATE_LEGACY_SEALED;
	}

	private boolean hasAuthorizedHQs()
	{
		return getAuthorizedToReadKeys().size() > 0;
	}
	
	public void changeState(BulletinState state) throws InvalidBulletinStateException
	{
		BulletinHeaderPacket bulletinHeaderPacket = getBulletinHeaderPacket();
		
		if( state.equals(BulletinState.STATE_LEGACY_DRAFT) ||
			state.equals(BulletinState.STATE_LEGACY_SEALED))
				throw new InvalidBulletinStateException();
			
		if(bulletinHeaderPacket.isSnapshot())
			throw new InvalidBulletinStateException();
		
		if(state.equals(BulletinState.STATE_SNAPSHOT) || 
				state.equals(BulletinState.STATE_SHARED))
			bulletinHeaderPacket.setSnapshot(true);
		
		HeadquartersKeys keys = new HeadquartersKeys(getAuthorizedToReadKeysIncludingPending());
		clearAuthorizedToReadKeys();

		if(state.equals(BulletinState.STATE_SHARED))
			setAuthorizedToReadKeys(keys);
		else
			bulletinHeaderPacket.setAuthorizedToReadKeysPending(keys);
	}
	
	public boolean isSnapshot()
	{
		return getBulletinHeaderPacket().isSnapshot();
	}
	
	public boolean requiresNewCopyToEdit()
	{
		if(isImmutable() || isSnapshot())
			return true;
		return false;
	}
	
	public void setImmutableOnServer(boolean immutable)
	{
		getBulletinHeaderPacket().setImmutableOnServer(immutable);
	}
	
	public boolean getImmutableOnServer()
	{
		return getBulletinHeaderPacket().getImmutableOnServer();
	}
	
	public FieldSpecCollection getTopSectionFieldSpecs()
	{
		return fieldData.getFieldSpecs();
	}
	
	public FieldSpecCollection getBottomSectionFieldSpecs()
	{
		return getPrivateFieldDataPacket().getFieldSpecs();
	}

	public void set(String fieldTag, String value)
	{
		MartusField field = getField(fieldTag);
		if(field == null)
			return;
		
		field.setData(value);
				
	}
	
	public MartusField getField(MiniFieldSpec miniSpec)
	{
		return getField(miniSpec.getTag());
	}
	
	public MartusField getField(String fieldTag)
	{
		PoolOfReusableChoicesLists noRelevantReusableChoices = PoolOfReusableChoicesLists.EMPTY_POOL;
		if(fieldTag.equals(PSEUDOFIELD_LOCAL_ID))
		{
			MartusField localIdField = new MartusField(FieldSpec.createStandardField(fieldTag, new FieldTypeNormal()), noRelevantReusableChoices);
			localIdField.setData(getLocalId());
			return localIdField;
		}
		
		if(fieldTag.equals(PSEUDOFIELD_LAST_SAVED_DATE))
		{
			MartusField lastSavedDateField = new MartusField(FieldSpec.createStandardField(fieldTag, new FieldTypeDate()), noRelevantReusableChoices);
			lastSavedDateField.setData(getLastSavedDate());
			return lastSavedDateField;
		}
		
		if (fieldTag.equals(Bulletin.TAGLASTSAVED) || fieldTag.equals(Bulletin.PSEUDOFIELD_LAST_SAVED_TIMESTAMP))
		{
			MartusField lastSavedTimestampField = new MartusField(FieldSpec.createStandardField(fieldTag, new FieldTypeNormal()), noRelevantReusableChoices);
			lastSavedTimestampField.setData(Long.toString(getLastSavedTime()));
			return lastSavedTimestampField;
		}
		
		// FIXME: Rename TAGSTATUS to PSEUDOFIELD_STATUS (globally)
		if(fieldTag.equals(TAGSTATUS))
		{
			MartusField statusField = new MartusField(FieldSpec.createStandardField(fieldTag, new FieldTypeNormal()), noRelevantReusableChoices);
			statusField.setData(getStatus());
			return statusField;
		}
		
		if(fieldTag.equals(PSEUDOFIELD_ALL_PRIVATE))
		{
			MartusField allPrivateField = new MartusField(FieldSpec.createStandardField(fieldTag, new FieldTypeBoolean()), noRelevantReusableChoices);
			if(isAllPrivate())
				allPrivateField.setData(FieldSpec.TRUESTRING);
			else
				allPrivateField.setData(FieldSpec.FALSESTRING);
			return allPrivateField;
		}
		
		if(fieldTag.equals(BulletinConstants.TAGWASSENT) || fieldTag.equals(Bulletin.PSEUDOFIELD_WAS_SENT))
			throw new RuntimeException("Bulletin doesn't know if it was sent or not");
		
		if(isFieldInPublicSection(fieldTag))
			return fieldData.getField(fieldTag);
		return getPrivateFieldDataPacket().getField(fieldTag);
	}

	public String get(String fieldTag)
	{
		MartusField field = getField(fieldTag);
		if(field == null)
			return "";
		
		return field.getData();
	}
	
	public FieldType getFieldType(String fieldTag)
	{
		MartusField field = getField(fieldTag);
		if(field == null)
			return new FieldTypeUnknown();
		
		return field.getType();
	}
	
	public BulletinType getBulletinType()
	{
		return getBulletinHeaderPacket().getBulletinType();
	}

	public void addPublicAttachment(AttachmentProxy a) throws
		IOException,
		MartusCrypto.EncryptionException
	{
		BulletinHeaderPacket bhp = getBulletinHeaderPacket();
		File rawFile = a.getFile();
		if(rawFile != null)
		{
			SessionKey sessionKey = getSignatureGenerator().createSessionKey();
			AttachmentPacket ap = new AttachmentPacket(getAccount(), sessionKey, rawFile, getSignatureGenerator());
			bhp.addPublicAttachmentLocalId(ap.getLocalId());
			a.setPendingPacket(ap, sessionKey);
		}
		else
		{
			bhp.addPublicAttachmentLocalId(a.getUniversalId().getLocalId());
		}

		getFieldDataPacket().addAttachment(a);
	}

	public void addPrivateAttachment(AttachmentProxy a) throws
		IOException,
		MartusCrypto.EncryptionException
	{
		BulletinHeaderPacket bhp = getBulletinHeaderPacket();
		File rawFile = a.getFile();
		if(rawFile != null)
		{
			SessionKey sessionKeyBytes = getSignatureGenerator().createSessionKey();
			AttachmentPacket ap = new AttachmentPacket(getAccount(), sessionKeyBytes, rawFile, getSignatureGenerator());
			bhp.addPrivateAttachmentLocalId(ap.getLocalId());
			a.setPendingPacket(ap, sessionKeyBytes);
		}
		else
		{
			bhp.addPrivateAttachmentLocalId(a.getUniversalId().getLocalId());
		}

		getPrivateFieldDataPacket().addAttachment(a);
	}

	public AttachmentProxy[] getPublicAttachments()
	{
		return getFieldDataPacket().getAttachments();
	}

	public AttachmentProxy[] getPrivateAttachments()
	{
		return getPrivateFieldDataPacket().getAttachments();
	}
	
	public void allowOnlyTheseAuthorizedKeysToRead(HeadquartersKeys authorizedKeys)
	{
		HeadquartersKeys keys = getAuthorizedToReadKeysIncludingPending();
		for(int i = 0; i < keys.size(); ++i)
		{
			HeadquartersKey oldKey = keys.get(i);
			if(!authorizedKeys.containsKey(oldKey.getPublicKey()))
				keys.remove(i);
		}
		setAuthorizedToReadKeys(keys);
	}

	public void addAuthorizedToReadKeys(HeadquartersKeys keysToAdd)
	{
		HeadquartersKeys keys = getAuthorizedToReadKeys();
		for(int i = 0; i < keysToAdd.size(); ++i)
		{
			HeadquartersKey keyToAdd = keysToAdd.get(i);
			if(!keys.containsKey(keyToAdd.getPublicKey()))
			{
				keys.add(keyToAdd);
			}
		}
		setAuthorizedToReadKeys(keys);
	}
	
	public void clearAllUserData()
	{
		getBulletinHeaderPacket().clearAllUserData();
		getFieldDataPacket().clearAll();
		getPrivateFieldDataPacket().clearAll();
		
		clearUserDataInSection(fieldData.getFieldSpecs());
		clearUserDataInSection(privateFieldData.getFieldSpecs());
		
		set(TAGENTRYDATE, DateUtilities.getTodayInStoredFormat());
		set(TAGEVENTDATE, MartusFlexidate.toStoredDateFormat(MultiCalendar.UNKNOWN));
		
		setMutable();
	}
	
	public void clearAuthorizedToReadKeys()
	{
		getBulletinHeaderPacket().clearAllAuthorizedToReadIncludingPending();
		getFieldDataPacket().clearAuthorizedToRead();
		getPrivateFieldDataPacket().clearAuthorizedToRead();
	}

	private void clearUserDataInSection(FieldSpecCollection specs) 
	{
		for(int i = 0; i < specs.size(); ++i)
			set(specs.get(i).getTag(), specs.get(i).getDefaultValue());
	}

	public void clearPublicAttachments()
	{
		getBulletinHeaderPacket().removeAllPublicAttachments();
		getFieldDataPacket().clearAttachments();
	}

	public void clearPrivateAttachments()
	{
		getBulletinHeaderPacket().removeAllPrivateAttachments();
		getPrivateFieldDataPacket().clearAttachments();
	}
	
	public boolean containsXFormsData()
	{
		return getFieldDataPacket().containXFormsData();
	}
	
	public void clearXFormsData()
	{
		getFieldDataPacket().clearXFormsData();
	}

	public boolean contains(String lookFor, MiniLocalization localization)
	{
		if(doesSectionContain(getFieldDataPacket(), lookFor, localization))
			return true;
		if(doesSectionContain(getPrivateFieldDataPacket(), lookFor, localization))
			return true;
		if(doesAttachmentsContain(getPublicAttachments(), lookFor))
			return true;
		if(doesAttachmentsContain(getPrivateAttachments(), lookFor))
			return true;
		return false;
	}

	private boolean doesAttachmentsContain(AttachmentProxy[] attachments, String lookFor) 
	{
		for(int i = 0; i < attachments.length; ++i)
		{
			String lookForLowerCase = lookFor.toLowerCase();
			String label = attachments[i].getLabel().toLowerCase();
			if(label.indexOf(lookForLowerCase) >=0)
				return true;
		}
		return false;
	}

	private boolean doesSectionContain(FieldDataPacket section, String lookFor, MiniLocalization localization)
	{
		FieldSpec fields[] = section.getFieldSpecs().asArray();
		for(int f = 0; f < fields.length; ++f)
		{
			MartusField field = getField(fields[f].getTag());
			if(field.contains(lookFor, localization))
				return true;
		}
		return false;
	}

	public HeadquartersKeys getAuthorizedToReadKeys()
	{
		return getBulletinHeaderPacket().getAuthorizedToReadKeys();
	}
	
	public HeadquartersKeys getAuthorizedToReadKeysIncludingPending()
	{
		HeadquartersKeys pendingOnlyReadKeys = getBulletinHeaderPacket().getAuthorizedToReadKeysPending();
		HeadquartersKeys authorizedAndPendingReadKeys = getUniqueKeysOnly(getAuthorizedToReadKeys(), pendingOnlyReadKeys);
		return authorizedAndPendingReadKeys;
	}

	private HeadquartersKeys getUniqueKeysOnly(HeadquartersKeys authorizedKeys, HeadquartersKeys pendingKeys)
	{
		HeadquartersKeys authorizedAndPendingReadKeys = new HeadquartersKeys(authorizedKeys);
		for(int i = 0; i < pendingKeys.size(); ++i)
		{
			HeadquartersKey pendingKey = pendingKeys.get(i);
			if(!authorizedAndPendingReadKeys.contains(pendingKey))
				authorizedAndPendingReadKeys.add(pendingKey);
		}
		return authorizedAndPendingReadKeys;
	}

	public void setAuthorizedToReadKeys(HeadquartersKeys authorizedKeys)
	{
		if(!authorizedKeys.isEmpty())
			getBulletinHeaderPacket().clearAllAuthorizedToReadIncludingPending();
		HeadquartersKeys ourCopyKeys = new HeadquartersKeys(authorizedKeys);
		getBulletinHeaderPacket().setAuthorizedToReadKeys(ourCopyKeys);
		getFieldDataPacket().setAuthorizedToReadKeys(ourCopyKeys);
		getPrivateFieldDataPacket().setAuthorizedToReadKeys(ourCopyKeys);
	}

	public DatabaseKey getDatabaseKey()
	{
		return getDatabaseKeyForLocalId(getLocalId());
	}

	public DatabaseKey getDatabaseKeyForLocalId(String localId)
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(getAccount(), localId);
		return getBulletinHeaderPacket().createKeyWithHeaderStatus(uid);
	}

	public boolean isFieldInPublicSection(String fieldName)
	{
		return getFieldDataPacket().fieldExists(fieldName);
	}

	public boolean isFieldInPrivateSection(String fieldName)
	{
		return getPrivateFieldDataPacket().fieldExists(fieldName);
	}

	public boolean isAllPrivate()
	{

		BulletinHeaderPacket bhp = getBulletinHeaderPacket();
		if(!bhp.hasAllPrivateFlag())
		{
			FieldDataPacket fdp = getFieldDataPacket();
			bhp.setAllPrivate(fdp.isEncrypted());
		}
		return bhp.isAllPrivate();
	}

	public void setAllPrivate(boolean newValue)
	{
		getBulletinHeaderPacket().setAllPrivate(newValue);
	}

	public void createDraftCopyOf(Bulletin other, ReadableDatabase otherDatabase) throws
		CryptoException, 
		InvalidPacketException, 
		SignatureVerificationException, 
		WrongPacketTypeException, 
		IOException, 
		InvalidBase64Exception
	{
		clearAllUserData();
		
		boolean originalIsMine = other.getAccount().equals(getAccount());
		if(originalIsMine)
		{
			if(other.requiresNewCopyToEdit())
			{
				BulletinHistory history = new BulletinHistory(other.getHistory());
				history.add(other.getLocalId());
				setHistory(history);
	
				ExtendedHistoryList extendedHistory = other.getBulletinHeaderPacket().getExtendedHistory();
				getBulletinHeaderPacket().setExtendedHistory(new ExtendedHistoryList(extendedHistory));
			}
			
			getBulletinHeaderPacket().setAuthorizedToReadKeysPending(other.getAuthorizedToReadKeysIncludingPending());
		}		
		else
		{
			ExtendedHistoryList historyList = new ExtendedHistoryList();
			ExtendedHistoryList extendedHistory = other.getBulletinHeaderPacket().getExtendedHistory();
			for(int i = 0; i < extendedHistory.size(); ++i)
			{
				ExtendedHistoryEntry oldHistoryEntry = extendedHistory.getHistory(i);
				String accountId = oldHistoryEntry.getClonedFromAccountId();
				BulletinHistory localHistory = oldHistoryEntry.getClonedHistory();
				historyList.add(accountId, localHistory);
			}
			BulletinHistory localHistory = new BulletinHistory(other.getHistory());
			localHistory.add(other.getLocalId());
			historyList.add(other.getAccount(), localHistory);
			getBulletinHeaderPacket().setExtendedHistory(historyList);
			setHistory(new BulletinHistory());
		}

		setMutable();
		setAllPrivate(true);
		setImmutableOnServer(other.getImmutableOnServer());

		pullFields(other, getFieldDataPacket().getFieldSpecs());
		pullFields(other, getPrivateFieldDataPacket().getFieldSpecs());
		
		getFieldDataPacket().setXFormsModelAsString(other.getFieldDataPacket().getXFormsModelAString());
		getFieldDataPacket().setXFormsInstanceAsString(other.getFieldDataPacket().getXFormsInstanceAsString());
		
		AttachmentProxy[] attachmentPublicProxies = other.getPublicAttachments();
		for(int aIndex = 0; aIndex < attachmentPublicProxies.length; ++aIndex)
		{
			AttachmentProxy ap = attachmentPublicProxies[aIndex];
			ap = getAsFileProxy(ap, otherDatabase, Bulletin.STATUSMUTABLE);
			addPublicAttachment(ap);
		}

		AttachmentProxy[] attachmentPrivateProxies = other.getPrivateAttachments();
		for(int aIndex = 0; aIndex < attachmentPrivateProxies.length; ++aIndex)
		{
			AttachmentProxy ap = attachmentPrivateProxies[aIndex];
			ap = getAsFileProxy(ap, otherDatabase, Bulletin.STATUSMUTABLE);
			addPrivateAttachment(ap);
		}

	}
	
	public void pullFields(Bulletin other, FieldSpecCollection fields)
	{
		for(int f = 0; f < fields.size(); ++f)
		{
			set(fields.get(f).getTag(), other.get(fields.get(f).getTag()));
		}
	}

	public AttachmentProxy getAsFileProxy(AttachmentProxy ap, ReadableDatabase otherDatabase, String status)
		throws
			IOException,
			CryptoException,
			InvalidPacketException,
			SignatureVerificationException,
			WrongPacketTypeException,
			InvalidBase64Exception
	{
		if(ap.getFile() != null) 
			return ap;
		if(otherDatabase == null)
			return ap;

		DatabaseKey key = DatabaseKey.createKey(ap.getUniversalId(),status);
		InputStreamWithSeek packetIn = otherDatabase.openInputStream(key, security);
		if(packetIn == null)
			return ap;

		try
		{
			return AttachmentProxy.createFileProxyFromAttachmentPacket(packetIn, ap, security);
		}
		finally
		{
			packetIn.close();
		}
	}

	public BulletinHeaderPacket getBulletinHeaderPacket()
	{
		return header;
	}

	public FieldDataPacket getFieldDataPacket()
	{
		return fieldData;
	}

	public FieldDataPacket getPrivateFieldDataPacket()
	{
		return privateFieldData;
	}

	public PendingAttachmentList getPendingPublicAttachments()
	{
		return getPendingAttachments(getFieldDataPacket());
	}

	public PendingAttachmentList getPendingPrivateAttachments()
	{
		return getPendingAttachments(getPrivateFieldDataPacket());
	}
	
	private PendingAttachmentList getPendingAttachments(FieldDataPacket fdp)
	{
		AttachmentProxy[] proxies = fdp.getAttachments();
		PendingAttachmentList pending = new PendingAttachmentList(); 
		for(int i=0; i < proxies.length; ++i)
		{
			AttachmentPacket packet = proxies[i].getPendingPacket(); 
			if(packet != null)
				pending.add(packet);
		}
		return pending;
	}

	protected BulletinHeaderPacket createHeaderPacket(UniversalId headerUid)
	{
		return new BulletinHeaderPacket(headerUid);
	}

	protected FieldDataPacket createPrivateFieldDataPacket(
		UniversalId privateDataUid,
		FieldSpecCollection privateFieldSpecs) throws Exception
	{
		return createFieldDataPacket(privateDataUid, privateFieldSpecs);
	}

	protected FieldDataPacket createPublicFieldDataPacket(
		UniversalId dataUid,
		FieldSpecCollection publicFieldSpecs) throws Exception
	{
		return createFieldDataPacket(dataUid, publicFieldSpecs);
	}

	protected FieldDataPacket createFieldDataPacket(
		UniversalId dataUid,
		FieldSpecCollection publicFieldSpecs) throws Exception
	{
		return new FieldDataPacket(dataUid, publicFieldSpecs);
	}

	public boolean isXFormsBulletin()
	{
		return getFieldDataPacket().containXFormsData();
	}
	
	public enum BulletinState {STATE_SAVE, STATE_SNAPSHOT, STATE_SHARED, STATE_LEGACY_DRAFT, STATE_LEGACY_SEALED};
	
	public static final String PSEUDOFIELD_LOCAL_ID = "_localId";
	public static final String PSEUDOFIELD_LAST_SAVED_DATE = "_lastSavedDate";
	public static final String PSEUDOFIELD_LAST_SAVED_TIMESTAMP = "_lastSavedTimestamp";
	public static final String PSEUDOFIELD_ALL_PRIVATE = "_allPrivate";
	public static final String PSEUDOFIELD_WAS_SENT = "_wasSent";
	public static final String TOP_SECTION = "TopSection";
	public static final String BOTTOM_SECTION = "BottomSection";
	public enum BulletinType {LEGACY_BULLETIN, RECORD, NOTE};
	
	private boolean isNonAttachmentDataValidFlag;
	private MartusCrypto security;
	private BulletinHeaderPacket header;
	private FieldDataPacket fieldData;
	private FieldDataPacket privateFieldData;
}
