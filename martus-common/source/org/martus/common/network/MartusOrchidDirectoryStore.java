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
package org.martus.common.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;

import com.subgraph.orchid.DirectoryStore;
import com.subgraph.orchid.Document;

public class MartusOrchidDirectoryStore
{
	public MartusOrchidDirectoryStore()
	{
		contentsByFile = new HashMap<String, byte[]>();
		actualStore = new ActualDirectoryStore();
	}
	
	public DirectoryStore getActualStore()
	{
		return actualStore;
	}

	public void saveStore(File martusOrchidCacheFile, MartusCrypto security) throws Exception
	{
		MartusLogger.logBeginProcess("Saving Orchid cache");
		FileOutputStream fileOut = new FileOutputStream(martusOrchidCacheFile);
		try
		{
			DataOutputStream out = new DataOutputStream(fileOut);
			try
			{
				writeStoreToStream(out);
			}
			finally
			{
				out.close();
			}
		}
		finally
		{
			fileOut.close();
		}
		MartusUtilities.createSignatureFileFromFile(martusOrchidCacheFile, security);
		MartusLogger.logEndProcess("Saving Orchid cache");
	}

	public void writeStoreToStream(DataOutputStream out) throws IOException
	{
		out.writeUTF(FILE_TYPE_IDENTIFIER);
		out.writeInt(FILE_VERSION);
		out.writeInt(contentsByFile.size());
		for (String cacheFilename : contentsByFile.keySet())
		{
			byte[] fileContents = contentsByFile.get(cacheFilename);
			out.writeUTF(cacheFilename);
			out.writeInt(fileContents.length);
			out.write(fileContents);
		}
	}

	public synchronized void loadStore(File martusOrchidCacheFile, MartusCrypto security) throws Exception
	{
		if(!martusOrchidCacheFile.exists())
			return;
		
		try
		{
			File sigFile = MartusUtilities.getSignatureFileFromFile(martusOrchidCacheFile);
			MartusUtilities.verifyFileAndSignature(martusOrchidCacheFile, sigFile, security, security.getPublicKeyString());
		} 
		catch (Exception e)
		{
			MartusLogger.log("Orchid file signature failed.");
			return;
		}

		MartusLogger.logBeginProcess("Loading Orchid cache");
		FileInputStream fileIn = new FileInputStream(martusOrchidCacheFile);
		try
		{
			DataInputStream in = new DataInputStream(fileIn);
			try
			{
				readStoreFromStream(in);
			}
			finally
			{
				in.close();
			}
		}
		finally
		{
			fileIn.close();
		}
		MartusLogger.logEndProcess("Loading Orchid cache");
	}

	private synchronized void readStoreFromStream(DataInputStream in) throws IOException
	{
		String fileTypeIdentifier = in.readUTF();
		if(!fileTypeIdentifier.equals(FILE_TYPE_IDENTIFIER))
			throw new IOException("File not valid type");
		int version = in.readInt();
		if(version < FILE_VERSION)
		{
			MartusLogger.log("Ignoring older orchid cache file");
			return;
		}
		if(version > FILE_VERSION)
		{
			MartusLogger.log("Ignoring newer orchid cache file");
			return;
		}
		int fileCount = in.readInt();
		for(int i = 0; i < fileCount; ++i)
		{
			String cacheFilename = in.readUTF();
			int length = in.readInt();
			byte[] bytes = new byte[length];
			in.read(bytes);
			contentsByFile.put(cacheFilename, bytes);
		}
	}

	class ActualDirectoryStore implements DirectoryStore
	{
		@Override
		public ByteBuffer loadCacheFile(CacheFile cacheFile)
		{
			byte[] fileContents = contentsByFile.get(cacheFile.getFilename());
			if(fileContents == null)
				fileContents = new byte[0];
			byte[] result = new byte[fileContents.length];
			System.arraycopy(fileContents, 0, result, 0, result.length);
			MartusLogger.log("MODS.loadCacheFile(" + cacheFile.getFilename() + ") -> " + result.length);
			return ByteBuffer.wrap(result);
		}
	
		@Override
		public void writeData(CacheFile cacheFile, ByteBuffer data)
		{
			MartusLogger.log("MODS.writeData(" + cacheFile.getFilename() + ") -> " + data.remaining());
			rawRemoveCacheFile(cacheFile);
			rawAppendData(cacheFile, data);
		}
	
		@Override
		public void writeDocument(CacheFile cacheFile, Document document)
		{
			ByteBuffer rawDocumentBytes = document.getRawDocumentBytes();
			MartusLogger.log("MODS.writeDocument(" + cacheFile.getFilename() + ") -> " + rawDocumentBytes.remaining());
			rawRemoveCacheFile(cacheFile);
			rawAppendDocument(cacheFile, document);
		}
	
		@Override
		public void writeDocumentList(CacheFile cacheFile,
				List<? extends Document> documents)
		{
			MartusLogger.log("MODS.writeDocumentList(" + cacheFile.getFilename() + ") -> " + documents.size());
			rawRemoveCacheFile(cacheFile);
			rawAppendDocumentList(cacheFile, documents);
		}
	
		@Override
		public void appendDocumentList(CacheFile cacheFile,
				List<? extends Document> documents)
		{
			MartusLogger.log("MODS.appendDocumentList(" + cacheFile.getFilename() + ") -> " + documents.size());
			rawAppendDocumentList(cacheFile, documents);
		}
	
		public void rawAppendDocumentList(CacheFile cacheFile,
				List<? extends Document> documents)
		{
			for (Document document : documents)
			{
				rawAppendDocument(cacheFile, document);
			}
		}
	
		@Override
		public void removeCacheFile(CacheFile cacheFile)
		{
			MartusLogger.log("MODS.removeCacheFile(" + cacheFile.getFilename() + ")");
			rawRemoveCacheFile(cacheFile);
		}
	
		@Override
		public synchronized void removeAllCacheFiles()
		{
			MartusLogger.log("MODS.removeAllCacheFiles()");
			contentsByFile.clear();
		}
	
		private synchronized void rawRemoveCacheFile(CacheFile cacheFile)
		{
			contentsByFile.remove(cacheFile);
		}
	
		private void rawAppendDocument(CacheFile cacheFile, Document document)
		{
			ByteBuffer rawDocumentBytes = document.getRawDocumentBytes();
			rawAppendData(cacheFile, rawDocumentBytes);
		}
	
		private synchronized void rawAppendData(CacheFile cacheFile, ByteBuffer rawDocumentBytes)
		{
			byte[] existingBytes = contentsByFile.get(cacheFile.getFilename());
			if(existingBytes == null)
				existingBytes = new byte[0];
			ByteBuffer existing = ByteBuffer.wrap(existingBytes);
			int newByteCount = rawDocumentBytes.remaining();
			ByteBuffer combined = ByteBuffer.allocate(existingBytes.length + newByteCount);
			combined.put(existing);
			combined.put(rawDocumentBytes);
			byte[] combinedBytes = combined.array();
			contentsByFile.put(cacheFile.getFilename(), combinedBytes);
		}
	}
	
	private final static String FILE_TYPE_IDENTIFIER = "Martus Orchid Cache";
	private final static int FILE_VERSION = 3;
	
	protected HashMap<String, byte[]> contentsByFile;
	private ActualDirectoryStore actualStore;
}
