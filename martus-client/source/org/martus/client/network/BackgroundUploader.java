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

package org.martus.client.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiSession;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileTooLargeException;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.util.StreamableBase64;

public class BackgroundUploader
{
	public BackgroundUploader(MartusApp appToUse, ProgressMeterInterface progressMeterToUse)
	{
		this(appToUse, null, progressMeterToUse);
	}
	
	public BackgroundUploader(MartusApp appToUse, UiMainWindow mainWindowForFxToUse, ProgressMeterInterface progressMeterToUse)
	{
		app = appToUse;
		mainWindow = mainWindowForFxToUse;
		progressMeter = progressMeterToUse;
	}

	public UploadResult backgroundUpload()
	{
		UploadResult uploadResult = new UploadResult();
		uploadResult.result = NetworkInterfaceConstants.UNKNOWN;
	
		ClientBulletinStore store = app.getStore();
		BulletinFolder folderSealedOutbox = app.getFolderSealedOutbox();
		BulletinFolder folderDraftOutbox = app.getFolderDraftOutbox();
		if(store.hasAnyNonDiscardedBulletins(folderSealedOutbox))
			uploadResult = uploadOneBulletin(folderSealedOutbox);
		else if(store.hasAnyNonDiscardedBulletins(folderDraftOutbox))
			uploadResult = uploadOneBulletin(folderDraftOutbox);
		return uploadResult;
	}

	public String uploadBulletin(Bulletin b) throws Exception
	{
		ClientBulletinStore store = app.getStore();
		// FIXME: is it safe to skip if it's "probably" on the server???
		if(b.isImmutable() && store.isProbablyOnServer(b.getUniversalId()))
			return NetworkInterfaceConstants.DUPLICATE;
		File tempFile = File.createTempFile("$$$MartusUploadBulletin", null);
		try
		{
			tempFile.deleteOnExit();
			UniversalId uid = b.getUniversalId();

			ReadableDatabase db = store.getDatabase();
			DatabaseKey headerKey = DatabaseKey.createKey(uid, b.getStatus());
			MartusCrypto security = app.getSecurity();
			BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, headerKey, tempFile, security);
			
			String tag = getUploadProgressTag(b);
			if(progressMeter != null)
				progressMeter.setStatusMessage(tag);
			return uploadBulletinZipFile(uid, tempFile);
		}
		finally
		{
			tempFile.delete();
		}
	}
	
	private String getUploadProgressTag(Bulletin b)
	{
		if(b.isMutable())
			return "UploadingDraftBulletin";
		return "UploadingSealedBulletin";
	}
	
	private String uploadBulletinZipFile(UniversalId uid, File tempFile)
		throws
			FileTooLargeException,
			FileNotFoundException,
			IOException,
			MartusSignatureException
	{
		int totalSize = MartusUtilities.getCappedFileLength(tempFile);
		int offset = getOffsetToStartUploading(uid, tempFile);
		byte[] rawBytes = new byte[app.serverChunkSize];
		FileInputStream inputStream = new FileInputStream(tempFile);
		inputStream.skip(offset);
		String result = null;
		while(true)
		{
			if(progressMeter != null)
				progressMeter.updateProgressMeter(offset, totalSize);
			int chunkSize = inputStream.read(rawBytes);
			if(chunkSize <= 0)
				break;
			byte[] chunkBytes = new byte[chunkSize];
			System.arraycopy(rawBytes, 0, chunkBytes, 0, chunkSize);
		
			String authorId = uid.getAccountId();
			String bulletinLocalId = uid.getLocalId();
			String encoded = StreamableBase64.encode(chunkBytes);
		
			NetworkResponse response = app.getCurrentNetworkInterfaceGateway().putBulletinChunk(app.getSecurity(),
								authorId, bulletinLocalId, totalSize, offset, chunkSize, encoded);
			result = response.getResultCode();
			if(!result.equals(NetworkInterfaceConstants.CHUNK_OK) && !result.equals(NetworkInterfaceConstants.OK))
				break;
			offset += chunkSize;
		}
		inputStream.close();
		return result;
	}

	public int getOffsetToStartUploading(UniversalId uid, File tempFile)
	{
		ClientSideNetworkGateway gateway = app.getCurrentNetworkInterfaceGateway();
		return gateway.getOffsetToStartUploading(uid, tempFile, app.getSecurity());
	}

	BackgroundUploader.UploadResult uploadOneBulletin(BulletinFolder uploadFromFolder)
	{
		UploadResult uploadResult = new UploadResult();
		ClientBulletinStore store = app.getStore();
	
		try
		{
			if(!app.isSSLServerAvailable())
				return uploadResult;
		
			int index = new Random().nextInt(uploadFromFolder.getBulletinCount());
			Bulletin b = store.chooseBulletinToUpload(uploadFromFolder, index);
			uploadResult.uid = b.getUniversalId();

			uploadResult.result = uploadBulletin(b);
			if(uploadResult.result == null)
				return uploadResult;
			
			if(uploadResult.result.equals(NetworkInterfaceConstants.OK) || 
					uploadResult.result.equals(NetworkInterfaceConstants.DUPLICATE))
			{
				store.setIsOnServer(b);
				if(UiSession.isJavaFx() && mainWindow != null)
					mainWindow.bulletinContentsHaveChanged(b);
				// TODO: Is the file this creates ever used???
				app.resetLastUploadedTime();
			}
			else
				uploadResult.bulletinNotSentAndRemovedFromQueue = true;
				
		}
		catch (Packet.InvalidPacketException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (Packet.WrongPacketTypeException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (Packet.SignatureVerificationException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (MartusCrypto.DecryptionException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (MartusUtilities.FileTooLargeException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			uploadResult.exceptionThrown = e.toString();
			uploadResult.bulletinNotSentAndRemovedFromQueue = true;
		}
		finally
		{
			if(uploadResult.isHopelesslyDamaged)
			{
				try
				{
					app.moveBulletinToDamaged(uploadFromFolder, uploadResult.uid);
				} 
				catch (IOException e)
				{
					uploadResult.exceptionThrown = e.toString();
					uploadResult.isHopelesslyDamaged = true;
				}
			}
			else
			{
				uploadFromFolder.remove(uploadResult.uid);
			}
			store.saveFolders();
		}
		return uploadResult;
	}

	public String putContactInfoOnServer(Vector info)  throws
			MartusCrypto.MartusSignatureException
	{
		ClientSideNetworkGateway gateway = app.getCurrentNetworkInterfaceGateway();
		NetworkResponse response = gateway.putContactInfo(app.getSecurity(), app.getAccountId(), info);
		return response.getResultCode();
	}

	public static class UploadResult
	{
		public UniversalId uid;
		public String result;
		public String exceptionThrown;
		public boolean isHopelesslyDamaged;
		public boolean bulletinNotSentAndRemovedFromQueue;
	}
	
	public static final String CONTACT_INFO_NOT_SENT="Contact Info Not Sent";

	private UiMainWindow mainWindow;
	private MartusApp app;
	private ProgressMeterInterface progressMeter;
}
