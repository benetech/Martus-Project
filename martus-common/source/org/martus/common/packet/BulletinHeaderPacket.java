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

package org.martus.common.packet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusXml;
import org.martus.common.XmlWriterFilter;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.Bulletin.BulletinType;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.DatabaseKey;
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeReader;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeek;
import org.martus.util.xml.SimpleXmlParser;


public class BulletinHeaderPacket extends Packet
{
	public BulletinHeaderPacket(MartusCrypto accountSecurity)
	{
		super(createUniversalId(accountSecurity));
		initialize();
	}

	public BulletinHeaderPacket(MartusCrypto accountSecurity, Bulletin.BulletinType bulletinType)
	{
		super(createUniversalId(accountSecurity, bulletinType));
		initialize();
	}

	public BulletinHeaderPacket(UniversalId universalIdToUse)
	{
		super(universalIdToUse);
		initialize();
	}

	public BulletinHeaderPacket()
	{
		initialize();
	}

	protected void initialize()
	{
		status = "";
		allPrivate = true;
		allHQsCanProxyUpload = false;
		fieldDataPacketSig = new byte[1];
		privateFieldDataPacketSig = new byte[1];
		publicAttachments = new Vector();
		privateAttachments = new Vector();
		legacyHqPublicKey = "";
		lastSavedTime = TIME_UNKNOWN;
		authorizedToReadKeys = new HeadquartersKeys();
		authorizedToReadKeysPending = new HeadquartersKeys();
		history = new BulletinHistory();
		extendedHistory = new ExtendedHistoryList();
	}

	public static UniversalId createUniversalId(MartusCrypto accountSecurity)
	{
		return createUniversalId(accountSecurity, BulletinType.LEGACY_BULLETIN);
	}
	
	public static UniversalId createUniversalId(MartusCrypto accountSecurity, BulletinType bulletinType)
	{
		String suffix = "";
		switch(bulletinType)
		{
			case NOTE:
				suffix = suffixNote;
				break;
			case RECORD:
				suffix = suffixRecord;
				break;
		}
		String authorId = accountSecurity.getPublicKeyString();
		String localId = createLocalIdWithPrefixAndSuffix(accountSecurity, prefix, suffix);
		return UniversalId.createFromAccountAndLocalId(authorId, localId);
	}

	public static boolean isValidLocalId(String localId)
	{
		return localId.startsWith(prefix);
	}
	
	public BulletinType getBulletinType()
	{
		String localId = getUniversalId().getLocalId();
		if(localId.endsWith(suffixNote))
			return Bulletin.BulletinType.NOTE;
		if(localId.endsWith(suffixRecord))
			return Bulletin.BulletinType.RECORD;
		return Bulletin.BulletinType.LEGACY_BULLETIN;
	}

	public void clearAllUserData()
	{
		clearAttachments();
		clearAllAuthorizedToReadIncludingPending();
	}

	public void clearAllAuthorizedToReadIncludingPending()
	{
		clearAuthorizedToRead();
		clearAuthorizedToReadPending();
	}

	private void clearAttachments()
	{
		publicAttachments.clear();
		privateAttachments.clear();
	}
	
	private void clearAuthorizedToRead()
	{
		authorizedToReadKeys = new HeadquartersKeys();
		legacyHqPublicKey = "";
	}
	
	private void clearAuthorizedToReadPending()
	{
		authorizedToReadKeysPending = new HeadquartersKeys();
	}

	public boolean hasAllPrivateFlag()
	{
		return knowsWhetherAllPrivate;
	}

	public void setStatus(String newStatus)
	{
		status = newStatus;
	}

	public String getStatus()
	{
		return status;
	}

	public void setSnapshot(boolean isSnapshot)
	{
		snapshot = isSnapshot;
	}
	
	public boolean isSnapshot()
	{
		return snapshot;
	}
	
	public void setImmutableOnServer(boolean immutable)
	{
		immutableOnServer = immutable;
	}
	
	public boolean getImmutableOnServer()
	{
		return immutableOnServer;
	}
	
	public long getLastSavedTime()
	{
		return lastSavedTime;
	}
	
	void setLastSavedTime(long timeToUse)
	{
		lastSavedTime = timeToUse;
	}

	public void updateLastSavedTime()
	{
		lastSavedTime = System.currentTimeMillis();
	}
	
	public void setHistory(BulletinHistory newHistory)
	{
		history = newHistory;
	}
	
	public BulletinHistory getHistory()
	{
		return history;
	}
	
	public void setExtendedHistory(ExtendedHistoryList newHistoryList)
	{
		extendedHistory = newHistoryList;
	}
	
	public ExtendedHistoryList getExtendedHistory()
	{
		return extendedHistory;
	}
	
	public int getVersionNumber()
	{
		return history.size() + 1;
	}
	
	public String getOriginalRevisionId()
	{
		if(getVersionNumber() == 1)
			return getLocalId();
		return history.get(0);
	}
	

	public void setFieldDataPacketId(String id)
	{
		fieldDataPacketId = id;
	}

	public String getFieldDataPacketId()
	{
		return fieldDataPacketId;
	}

	public void setPrivateFieldDataPacketId(String id)
	{
		privateFieldDataPacketId = id;
	}

	public String getPrivateFieldDataPacketId()
	{
		return privateFieldDataPacketId;
	}

	public String getLegacyHQPublicKey()
	{
		return legacyHqPublicKey;
	}

	public HeadquartersKeys getAuthorizedToReadKeys()
	{
		return authorizedToReadKeys;
	}
	
	public HeadquartersKeys getAuthorizedToReadKeysPending()
	{
		return authorizedToReadKeysPending;
	}
	
	public boolean canAllHQsProxyUpload()
	{
		return allHQsCanProxyUpload;
	}
	
	void setAllHQsProxyUploadFromXmlTextValue(String data)
	{
		if(data.equals(ALL_HQS_PROXY_UPLOAD))
			allHQsCanProxyUpload = true;
	}
	
	void setStatusSnapshotFromXmlTextValue(String data)
	{
		if(data.equals(STATUS_SNAPSHOT))
			snapshot = true;
	}
	
	void setImmutableOnServerFromXmlTextValue(String data)
	{
		immutableOnServer = true;
	}

	public HeadquartersKeys getAuthorizedToUploadKeys()
	{
		return authorizedToReadKeys;
	}
	
	public void setAuthorizedToReadKeys(HeadquartersKeys accountsKeys)
	{
		authorizedToReadKeys = accountsKeys;
		if(!accountsKeys.isEmpty())
			legacyHqPublicKey = accountsKeys.get(0).getPublicKey();
	}
	
	public void setAuthorizedToReadKeysPending(HeadquartersKeys accountsKeys)
	{
		authorizedToReadKeysPending = accountsKeys;
	}

	public boolean isHQAuthorizedToRead(String publicKey)
	{
		return authorizedToReadKeys.containsKey(publicKey);
	}

	public boolean isAuthorizedToUpload(String publicKey)
	{
		if(publicKey.equals(getAccountId()))
			return true;
		if(!allHQsCanProxyUpload)
			return false;
		return isHQAuthorizedToRead(publicKey);
	}
	
	public void setAllPrivate(boolean newValue)
	{
		allPrivate = newValue;
		knowsWhetherAllPrivate = true;
	}

	public boolean isAllPrivate()
	{
		return allPrivate;
	}

	public void setFieldDataSignature(byte[] sig)
	{
		fieldDataPacketSig = sig;
	}

	public byte[] getFieldDataSignature()
	{
		return fieldDataPacketSig;
	}

	public void setPrivateFieldDataSignature(byte[] sig)
	{
		privateFieldDataPacketSig = sig;
	}

	public byte[] getPrivateFieldDataSignature()
	{
		return privateFieldDataPacketSig;
	}

	public String[] getPublicAttachmentIds()
	{
		String[] result = new String[publicAttachments.size()];
		for(int i = 0; i < result.length; ++i)
			result[i] = (String)publicAttachments.get(i);

		Arrays.sort(result);
		return result;
	}

	public String[] getPrivateAttachmentIds()
	{
		String[] result = new String[privateAttachments.size()];
		for(int i = 0; i < result.length; ++i)
			result[i] = (String)privateAttachments.get(i);

		Arrays.sort(result);
		return result;
	}

	public void addPublicAttachmentLocalId(String id)
	{
		if(publicAttachments.contains(id))
			return;
		publicAttachments.add(id);
	}

	public void addPrivateAttachmentLocalId(String id)
	{
		if(privateAttachments.contains(id))
			return;
		privateAttachments.add(id);
	}

	public void removeAllPublicAttachments()
	{
		publicAttachments.clear();
	}

	public void removeAllPrivateAttachments()
	{
		privateAttachments.clear();
	}

	public void loadFromXml(InputStreamWithSeek inputStream, byte[] expectedSig, MartusCrypto verifier) throws
		IOException,
		InvalidPacketException,
		WrongPacketTypeException,
		SignatureVerificationException,
		MartusCrypto.DecryptionException,
		MartusCrypto.NoKeyPairException
	{
		if(verifier != null)
			verifyPacketSignature(inputStream, expectedSig, verifier);
		XmlHeaderPacketLoader loader = new XmlHeaderPacketLoader(this);
		try
		{
			knowsWhetherAllPrivate = false;
			SimpleXmlParser.parse(loader, new UnicodeReader(inputStream));
		}
		catch (Exception e)
		{
			// TODO: Be more specific with exceptions!
			e.printStackTrace();
			System.out.println(e.getCause());
			System.out.println(e.getClass());
			System.out.println(e.getMessage());
			throw new InvalidPacketException(e.getMessage());
		}
	}

	public static BulletinHeaderPacket loadFromZipFile(ZipFile zip, MartusCrypto verifier)
		throws IOException,
		SignatureVerificationException
	{
		BulletinHeaderPacket header = new BulletinHeaderPacket(verifier);
		ZipEntry headerZipEntry = getBulletinHeaderEntry(zip);

		InputStreamWithSeek headerIn = new ZipEntryInputStreamWithSeek(zip, headerZipEntry);
		try
		{
			header.loadFromXml(headerIn, verifier);
			if(!header.getLocalId().equals(headerZipEntry.getName()))
				throw new IOException("Misnamed header entry");
		}
		catch(Packet.SignatureVerificationException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new IOException(e.getMessage());
		}
		finally
		{
			headerIn.close();
		}
		return header;
	}

	public DatabaseKey[] getPublicPacketKeys()
	{
		String accountId = getAccountId();
		String[] publicAttachmentIds = getPublicAttachmentIds();
	
		int corePacketCount = 2;
		int publicAttachmentCount = publicAttachmentIds.length;
		int totalPacketCount = corePacketCount + publicAttachmentCount;
		DatabaseKey[] keys = new DatabaseKey[totalPacketCount];
	
		int next = 0;
		UniversalId dataUid = UniversalId.createFromAccountAndLocalId(accountId, getFieldDataPacketId());
		keys[next++] = createKeyWithHeaderStatus(dataUid);
	
		for(int i=0; i < publicAttachmentIds.length; ++i)
		{
			UniversalId attachmentUid = UniversalId.createFromAccountAndLocalId(accountId, publicAttachmentIds[i]);
			keys[next++] = createKeyWithHeaderStatus(attachmentUid);
		}
		keys[next++] = createKeyWithHeaderStatus(getUniversalId());
	
		return keys;
	}

	public DatabaseKey createKeyWithHeaderStatus(UniversalId uidToUse)
	{
		if(Bulletin.isMutable(getStatus()))
			return DatabaseKey.createMutableKey(uidToUse);
		return DatabaseKey.createImmutableKey(uidToUse);
	}

	static ZipEntry getBulletinHeaderEntry(ZipFile zip) throws IOException
	{
		Enumeration entries = zip.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry headerEntry = (ZipEntry)entries.nextElement();
			if(isValidLocalId(headerEntry.getName()))
				return headerEntry;
		}

		throw new IOException("Missing header entry");
	}

	protected String getPacketRootElementName()
	{
		return MartusXml.BulletinHeaderPacketElementName;
	}

	protected void internalWriteXml(XmlWriterFilter dest) throws IOException
	{
		super.internalWriteXml(dest);

		writeElement(dest, MartusXml.BulletinStatusElementName, getStatus());
		writeElement(dest, MartusXml.LastSavedTimeElementName, Long.toString(getLastSavedTime()));

		String allPrivateValue = ALL_PRIVATE;
		if(!isAllPrivate())
			allPrivateValue = NOT_ALL_PRIVATE;
		writeElement(dest, MartusXml.AllPrivateElementName, allPrivateValue);

		String hqPublicKey = getLegacyHQPublicKey();
		if(hqPublicKey.length() > 0)
			writeElement(dest, MartusXml.HQPublicKeyElementName, hqPublicKey);
		
		String dataId = getFieldDataPacketId();
		if(dataId != null)
		{
			writeElement(dest, MartusXml.DataPacketIdElementName, dataId);
			writeElement(dest, MartusXml.DataPacketSigElementName, StreamableBase64.encode(fieldDataPacketSig));
		}

		String privateId = getPrivateFieldDataPacketId();
		if(privateId != null)
		{
			writeElement(dest, MartusXml.PrivateDataPacketIdElementName, privateId);
			writeElement(dest, MartusXml.PrivateDataPacketSigElementName, StreamableBase64.encode(privateFieldDataPacketSig));
		}

		String[] publicAttachmentIds = getPublicAttachmentIds();
		for(int i = 0; i < publicAttachmentIds.length; ++i)
		{
			writeElement(dest, MartusXml.PublicAttachmentIdElementName, publicAttachmentIds[i]);
		}

		String[] privateAttachmentIds = getPrivateAttachmentIds();
		for(int i = 0; i < privateAttachmentIds.length; ++i)
		{
			writeElement(dest, MartusXml.PrivateAttachmentIdElementName, privateAttachmentIds[i]);
		}

		if(!authorizedToReadKeys.isEmpty())
		{
			String value = authorizedToReadKeys.toString();
			writeNonEncodedElement(dest, MartusXml.AccountsAuthorizedToReadElementName, value);			
		}
		writeElement(dest, MartusXml.AllHQSProxyUploadName, ALL_HQS_PROXY_UPLOAD);
		if(snapshot)
			writeElement(dest, MartusXml.StatusSnapshotName, STATUS_SNAPSHOT);
		else
			writeElement(dest, MartusXml.StatusSnapshotName, STATUS_NOT_A_SNAPSHOT);

		if(immutableOnServer)
			writeElement(dest, MartusXml.ImmutableOnServerName, IMMUTABLE_ON_SERVER);
			
		if(!authorizedToReadKeysPending.isEmpty())
		{
			String value = authorizedToReadKeysPending.toString();
			writeNonEncodedElement(dest, MartusXml.AccountsAuthorizedToReadPendingElementName, value);			
		}

		if(history.size() > 0)
			history.internalWriteXml(dest);
		
		if(extendedHistory.size() > 0)
			extendedHistory.internalWriteXml(dest);
	}

	void setAllPrivateFromXmlTextValue(String data)
	{
		if(data.equals(NOT_ALL_PRIVATE))
			setAllPrivate(false);
		else
			setAllPrivate(true);
	}

	private final static String ALL_PRIVATE = "1";
	private final static String NOT_ALL_PRIVATE = "0";
	private final static String ALL_HQS_PROXY_UPLOAD = "1";
	private final static String STATUS_SNAPSHOT = "1";
	private final static String STATUS_NOT_A_SNAPSHOT = "0";
	private final static String IMMUTABLE_ON_SERVER = "1";
	
	public static final long TIME_UNKNOWN = 0;

	boolean knowsWhetherAllPrivate;
	boolean allPrivate;
	String fieldDataPacketId;
	String privateFieldDataPacketId;
	String status;
	String legacyHqPublicKey;
	private long lastSavedTime;
	private byte[] fieldDataPacketSig;
	private byte[] privateFieldDataPacketSig;
	private Vector publicAttachments;
	private Vector privateAttachments;
	private static final String prefix = "B-";
	private static final String suffixNote = "_N";
	private static final String suffixRecord = "_R";
	private HeadquartersKeys authorizedToReadKeys;
	private boolean allHQsCanProxyUpload;
	private BulletinHistory history;
	private ExtendedHistoryList extendedHistory;
	private boolean snapshot;
	private HeadquartersKeys authorizedToReadKeysPending;
	private boolean immutableOnServer;
}
