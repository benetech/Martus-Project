/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2013, Beneficent
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

package org.martus.server.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;

import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;

public class CreateAccountMap 
{
	public static void main(String[] args) throws Exception
	{
		new CreateAccountMap().createAccountMap(args);
	}
	
	private void createAccountMap(String[] args) throws Exception
	{
		processArgs(args);
		if(prompt)
			System.out.println("CreateAccountMap");

		File packetsDirectory = new File(packetDirName);
		if(noisy)
			MartusLogger.log("Packets directory: " + packetsDirectory);

		security = MartusServerUtilities.loadKeyPair(keyPairFileName, prompt);
		db = new ServerFileDatabase(packetsDirectory, security);
		File mapFile = db.getAccountMapFile();

		if(noisy)
			MartusLogger.log("Account map file: " + mapFile.getAbsolutePath());
		if(mapFile.exists())
		{
			System.err.println("Cannot create account map because it already exists");
			System.exit(1);
		}
		
		processPacketsDirectory(packetsDirectory);
		if(prompt)
			MartusLogger.log("Finished");
	}

	void processArgs(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			String value = args[i].substring(args[i].indexOf("=")+1);

			if(args[i].startsWith("--no-prompt"))
				prompt = false;

			if(args[i].startsWith("--noisy"))
				noisy = true;

			if(args[i].startsWith("--packet-directory="))
				packetDirName = value;
			
			if(args[i].startsWith("--keypair"))
				keyPairFileName = value;
			
		}

		if(packetDirName == null || keyPairFileName == null)
		{
			System.err.println("Incorrect arguments: CreateAccountMap [--no-prompt] [--noisy] --packet-directory=<packetdir> --keypair-file=<keypair>\n");
			System.exit(2);
		}
	}

	public CreateAccountMap()
	{
	}

	private void processPacketsDirectory(File packetsDirectory) throws Exception
	{
		FileSet accountBuckets = getFiles(packetsDirectory, getAccountBucketFilter());
		
		for (File accountBucket : accountBuckets) 
		{
			File[] accountDirectories = accountBucket.listFiles(getAccountDirectoryFilter());
			if(noisy)
				MartusLogger.log("Processing " + accountBucket.getName() + ": " + accountDirectories.length);
			
			for (File accountDirectory : accountDirectories) 
			{
				String accountId = getAccountIdFromAccountDirectory(accountDirectory);
				if(accountId != null)
				{
					writeAccountDirectoryIdentificationFile(accountDirectory, accountId);
					db.appendAccountToMapFile(accountId, accountDirectory.getAbsolutePath());
				}
			}
		}
	}

	private void writeAccountDirectoryIdentificationFile(File accountDirectory, String accountId) throws Exception
	{
		String publicCode = MartusSecurity.computeFormattedPublicCode(accountId);
		File metadataDirectory = new File(accountDirectory, "metadata");
		metadataDirectory.mkdirs();
		File identificationFile = new File(metadataDirectory, "acct-" + publicCode + ".txt");
		UnicodeWriter writer = new UnicodeWriter(identificationFile);
		try
		{
			writer.writeln(accountId);
		}
		finally
		{
			writer.close();
		}

		MartusServerUtilities.createSignatureFileFromFileOnServer(identificationFile, security);

	}

	private String getAccountIdFromAccountDirectory(File accountDirectory) throws Exception 
	{
		FileSet packetBucketDirectories = getAllPacketBucketDirectories(accountDirectory);
		
		for (File packetBucketDirectory : packetBucketDirectories) 
		{
			FileSet headerPackets = getFiles(packetBucketDirectory, getHeaderPacketFilter());
			if(headerPackets.size() > 0)
			{
				File firstHeaderPacket = headerPackets.iterator().next();
				String accountId = extractAccountId(firstHeaderPacket);
				return accountId;
			}
		}
		
		MartusLogger.logWarning("No packets in " + accountDirectory.getAbsolutePath());
		return null;
	}

	private String extractAccountId(File headerPacketFile) throws Exception 
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket();
		FileInputStreamWithSeek inputStream = new FileInputStreamWithSeek(headerPacketFile);
		try
		{
			bhp.loadFromXml(inputStream, getSecurity());
			return bhp.getAccountId();
		}
		finally
		{
			inputStream.close();
		}
	}

	private FileSet getAllPacketBucketDirectories(File accountDirectory) 
	{
		FileSet allBuckets = new FileSet();
		allBuckets.addAll(getFiles(accountDirectory, getSealedPacketBucketDirectoryFilter()));
		allBuckets.addAll(getFiles(accountDirectory, getDraftPacketBucketDirectoryFilter()));
		return allBuckets;
	}

	private FileSet getFiles(File packetsDirectory, FilenameFilter accountBucketFilter) 
	{
		File[] files = packetsDirectory.listFiles(accountBucketFilter);
		return new FileSet(files);
	}
	
	private MartusCrypto getSecurity()
	{
		return security;
	}

	private static FilenameFilter getAccountBucketFilter() 
	{
		class AccountBucketFilenameFilter implements FilenameFilter
		{
			public boolean accept(File file, String name) 
			{
				return (name.startsWith("ab"));
			}
		}
		
		return new AccountBucketFilenameFilter();
	}

	private static FilenameFilter getAccountDirectoryFilter() 
	{
		class AccountDirectoryFilenameFilter implements FilenameFilter
		{
			public boolean accept(File file, String name) 
			{
				return (name.startsWith("a"));
			}
		}
		
		return new AccountDirectoryFilenameFilter();
	}
	
	private static FilenameFilter getSealedPacketBucketDirectoryFilter() 
	{
		class SealedPacketBucketDirectoryFilenameFilter implements FilenameFilter
		{
			public boolean accept(File file, String name) 
			{
				return (name.startsWith("pb"));
			}
		}
		
		return new SealedPacketBucketDirectoryFilenameFilter();
	}
	
	private static FilenameFilter getDraftPacketBucketDirectoryFilter() 
	{
		class DraftPacketBucketDirectoryFilenameFilter implements FilenameFilter
		{
			public boolean accept(File file, String name) 
			{
				return (name.startsWith("dpb"));
			}
		}
		
		return new DraftPacketBucketDirectoryFilenameFilter();
	}
	
	private static FilenameFilter getHeaderPacketFilter() 
	{
		class HeaderPacketFilenameFilter implements FilenameFilter
		{
			public boolean accept(File file, String name) 
			{
				return (name.startsWith("B-"));
			}
		}
		
		return new HeaderPacketFilenameFilter();
	}
	
	private static class FileSet extends HashSet<File>
	{
		public FileSet()
		{
			this(new File[0]);
		}
		
		public FileSet(File[] files)
		{
			super(Arrays.asList(files));
		}
	}

	private static String packetDirName;
	private static String keyPairFileName;
	private static boolean prompt = true;
	private static boolean noisy;

	private ServerFileDatabase db;
	private MartusCrypto security;
}
