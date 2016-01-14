/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2007, Beneficent
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

package org.martus.common.utilities;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.CryptoInitializationException;
import org.martus.common.crypto.MartusCrypto.InvalidKeyPairFileVersionException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.packet.UniversalId;
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;

public class MartusServerUtilities
{
	public static MartusCrypto loadCurrentMartusSecurity(File keyPairFile, char[] passphrase)
		throws CryptoInitializationException, FileNotFoundException, IOException, InvalidKeyPairFileVersionException, AuthorizationFailedException
	{
		MartusCrypto security = new MartusSecurity();
		FileInputStream in = new FileInputStream(keyPairFile);
		security.readKeyPair(in, passphrase);
		in.close();
		return security;
	}

	public static File getSignatureDirectoryForFile(File originalFile)
	{
		return new File(originalFile.getParent() + File.separatorChar + MARTUS_SIGNATURE_FILE_DIRECTORY_NAME);
	}
	
	public static Date getDateOfSignatureFile(File signatureFile)
		throws IOException
	{
		UnicodeReader reader = new UnicodeReader(signatureFile);
		String identifier = reader.readLine();
		String timestampDate = reader.readLine();
		reader.close();
		
		if(identifier.compareTo(MARTUS_SIGNATURE_FILE_IDENTIFIER) != 0)
		{
			return null;
		}
		
		SimpleDateFormat formatDate = new SimpleDateFormat(MARTUS_SIGNATURE_FILE_DATE_FORMAT);
		formatDate.setCalendar(Calendar.getInstance());
		
		Date date;
		try
		{
			date = formatDate.parse(timestampDate);
		}
		catch (ParseException e)
		{
			return null;
		}
		
		return date;
	}
	
	public static File getLatestSignatureFileFromFile(File originalFile)
		throws IOException, ParseException, MartusSignatureFileDoesntExistsException
	{
		Vector signatureFiles =  getSignaturesForFile(originalFile);
		
		if(signatureFiles.size() == 0)
		{
			throw new MartusSignatureFileDoesntExistsException();
		}
		
		Date latestSigDate = null;
		File latestSignatureFile = null;
		
		for(int x = 0; x < signatureFiles.size(); x++)
		{
			File nextSignatureFile = (File) signatureFiles.get(x);

			Date nextSigDate = getDateOfSignatureFile(nextSignatureFile);
			if(isDateLatest(nextSigDate, latestSigDate))
			{
				latestSigDate = nextSigDate;
				latestSignatureFile = nextSignatureFile;
			}
		}
		
		if(latestSignatureFile == null)
		{
			throw new MartusSignatureFileDoesntExistsException();
		}
		 
		return latestSignatureFile;
	}

	public static boolean isMatchingSigFile(File originalFile, File nextSignatureFile)
	{
		String orginalFilename = originalFile.getName();
		String nextFilename = nextSignatureFile.getName();
		
		boolean isMatchingSigFile = nextFilename.endsWith(".sig") && nextFilename.startsWith(orginalFilename);
		
		return isMatchingSigFile;
	}
	
	public static boolean isDateLatest(Date nextSigDate, Date latestSigDate)
	{
		if(nextSigDate == null)
		{
			return false;
		}
		
		if(latestSigDate == null)
		{
			return true;
		}

		return nextSigDate.after(latestSigDate);
	}

	public synchronized static File createSignatureFileFromFileOnServer(File fileToSign, MartusCrypto signer)
		throws IOException, MartusSignatureException, InterruptedException, MartusSignatureFileAlreadyExistsException
	{
		Thread.sleep(1000);
		String dateStamp = createTimeStamp();
		
		File sigDir = getSignatureDirectoryForFile(fileToSign);
		File signatureFile = new File(sigDir.getPath() + File.separatorChar + fileToSign.getName() + "." + dateStamp + ".sig");
		
		if(signatureFile.exists() )
		{
			throw new MartusSignatureFileAlreadyExistsException();
		}

		if(! sigDir.exists())
		{
			sigDir.mkdir();
		}
		
		writeSignatureFileWithDatestamp(signatureFile, dateStamp, fileToSign, signer);


		return signatureFile;
	}

	public static String createTimeStamp()
	{
		long millisSince1970 = System.currentTimeMillis();
		return getFormattedTimeStamp(millisSince1970);
	}

	public static String getFormattedTimeStamp(long millisSince1970)
	{
		Timestamp stamp = new Timestamp(millisSince1970);
		SimpleDateFormat formatDate = new SimpleDateFormat(MARTUS_SIGNATURE_FILE_DATE_FORMAT);
		String dateStamp = formatDate.format(stamp);
		return dateStamp;
	}
	
	public static Date getDateFromFormattedTimeStamp(String formattedTimeStamp) throws ParseException
	{
		SimpleDateFormat date = new SimpleDateFormat(MARTUS_SIGNATURE_FILE_DATE_FORMAT);
		return date.parse(formattedTimeStamp);
	}
	
	public synchronized static void writeSignatureFileWithDatestamp(File signatureFile, String date, File fileToSign, MartusCrypto signer)
	throws IOException, MartusSignatureException
	{	
		long filesize = fileToSign.length();
		long lineCount = getLineCountForFile(fileToSign);
		byte[] signature = MartusUtilities.createSignatureFromFile(fileToSign, signer);
		String sigString = StreamableBase64.encode(signature);

		signatureFile.delete();
		UnicodeWriter writer = new UnicodeWriter(signatureFile, UnicodeWriter.APPEND);
		try
		{
			writer.writeln(MARTUS_SIGNATURE_FILE_IDENTIFIER);
			writer.writeln(date);
			writer.writeln(Long.toString(filesize));
			writer.writeln(Long.toString(lineCount));
			writer.writeln(signer.getPublicKeyString());
			writer.writeln(sigString);
			writer.flush();
		}
		finally
		{
			writer.close();
		}
	}
	
	static long getLineCountForFile(File file)
		throws IOException
	{
		LineNumberReader in = new LineNumberReader(new FileReader(file));
		long numLines = 0;
		while(in.readLine() != null)
			 ;
		 numLines = in.getLineNumber();
		 in.close();
		 
		return numLines;
		
	}
	
	public static void verifyFileAndLatestSignatureOnServer(File fileToVerify, MartusCrypto verifier)
			throws FileVerificationException, IOException, ParseException, MartusSignatureFileDoesntExistsException
	{
		File signatureFile = getLatestSignatureFileFromFile(fileToVerify);
		MartusServerUtilities.verifyFileAndSignatureOnServer(fileToVerify, signatureFile, verifier, verifier.getPublicKeyString());
	}
	
	public static void verifyFileAndSignatureOnServer(File fileToVerify, File signatureFile, MartusCrypto verifier, String accountId)
		throws FileVerificationException
	{
		FileInputStream inData = null;
		try
		{
			UnicodeReader reader = new UnicodeReader(signatureFile);
			// get Identifier
			reader.readLine();
			// get signature date
			reader.readLine();
			long filesize = Long.parseLong(reader.readLine());
			// get lineCount
			reader.readLine();
			String key = reader.readLine();
			String signature = reader.readLine();
			reader.close();
			
			long verifyFileSize = fileToVerify.length();
			if(filesize != verifyFileSize)
				throw new FileVerificationException();

			if(!key.equals(accountId))
				throw new FileVerificationException();
			inData = new FileInputStream(fileToVerify);
			if( !verifier.isValidSignatureOfStream(key, inData, StreamableBase64.decode(signature)) )
				throw new FileVerificationException();
		}
		catch(Exception e)
		{
			MartusLogger.log("Signature Verification error using " + signatureFile.getAbsolutePath());
			throw new FileVerificationException();
		}
		finally
		{
			try
			{
				if(inData != null)
					inData.close();
			}
			catch (IOException ignoredException)
			{
			}
		}
	}
	
	public static Vector getSignaturesForFile(File originalFile)
	{
		File sigDir = getSignatureDirectoryForFile(originalFile);
		
		File[] filesAvailable = sigDir.listFiles();
		Vector signatureFiles = new Vector();
		
		if(filesAvailable != null)
		{
			for (int i = 0; i < filesAvailable.length; i++)
			{
				File file = filesAvailable[i];
				if(isMatchingSigFile(originalFile, file))
				{
					signatureFiles.add(file);
				}
			}
		}
		return signatureFiles;
	}
	
	public static void deleteSignaturesForFile(File originalFile)
	{
		Vector sigFiles = MartusServerUtilities.getSignaturesForFile(originalFile);

		for (Iterator iter = sigFiles.iterator(); iter.hasNext();)
		{
			File signature = (File) iter.next();
			signature.delete();
		}
		
		File sigDir = MartusServerUtilities.getSignatureDirectoryForFile(originalFile);
		sigDir.delete();
	}
	
	public static byte [] getFileContents(File plainTextFile) throws IOException
	{
		long size = plainTextFile.length();
		
		if(size > MAX_ALLOWED_ENCRYPTED_FILESIZE)
		{
			throw new FileTooLargeException();
		}
		
		byte [] result = new byte[(int) size];
		
		FileInputStream inputStream = new FileInputStream(plainTextFile);
		inputStream.read(result);
		inputStream.close();
		
		return result;
	}

	public static MartusCrypto loadKeyPair(String keyPairFileName, boolean showPrompt)
	{
		File keyPairFile = new File(keyPairFileName);
		if(!keyPairFile.exists())
		{
			System.out.println("Error missing keypair");
			System.exit(3);
		}
		
		if(showPrompt)
		{
			System.out.print("Enter server passphrase:");
			System.out.flush();
		}
	
		try
		{
			UnicodeReader reader = new UnicodeReader(System.in);
			try
			{
				//TODO security issue here password is a string
				String passphrase = reader.readLine();
				return MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase.toCharArray());
			}
			finally
			{
				reader.close();
			}
		}
		catch (MartusCrypto.AuthorizationFailedException e)
		{
			System.err.println("Error probably bad passphrase: " + e + "\n");
			System.exit(1);
		}
		catch(Exception e)
		{
			System.err.println("Error loading keypair: " + e + "\n");
			System.exit(3);
		}
		return null;
	}

	public static void writeContatctInfo(String accountId, Vector contactInfo, File contactInfoFile) throws IOException
	{
		contactInfoFile.getParentFile().mkdirs();
		FileOutputStream contactFileOutputStream = new FileOutputStream(contactInfoFile);
		DataOutputStream out = new DataOutputStream(contactFileOutputStream);
		out.writeUTF((String)contactInfo.get(0));
		out.writeInt(((Integer)(contactInfo.get(1))).intValue());
		for(int i = 2; i<contactInfo.size(); ++i)
		{
			out.writeUTF((String)contactInfo.get(i));
		}
		out.close();
	}
	

	public static void writeAccessTokenData(String accountId, String tokenData, File accessTokenFile) throws IOException
	{
		accessTokenFile.getParentFile().mkdirs();
		FileOutputStream accessTokenFileOutputStream = new FileOutputStream(accessTokenFile);
		UnicodeWriter out = new UnicodeWriter(accessTokenFileOutputStream);
		out.write(tokenData);
		out.flush();
		out.close();
	}
	
	public static void loadHiddenPacketsFile(File hiddenFile, BulletinStore store, LoggerInterface logger)
	{	
		try
		{
			UnicodeReader reader = new UnicodeReader(hiddenFile);
			Vector hiddenPackets = getHiddenPacketsList(reader);
			store.hidePackets(hiddenPackets, logger);
			logger.logInfo(hiddenPackets.size() + " bulletins hidden.");
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
			logger.logInfo("Deleted packets file not found: " + hiddenFile.getName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.logError("loading Deleted Packets file: " + hiddenFile.getName());
		}		
	}

	public static Vector getHiddenPacketsList(UnicodeReader reader) throws IOException
	{
		Vector hiddenPackets = new Vector();
		String accountId = null;
		try
		{
			while(true)
			{
				String thisLine = reader.readLine();
				if(thisLine == null)
					return hiddenPackets;
				if(thisLine.startsWith(" "))
				{
					hiddenPackets.addAll(getListOfhiddenPacketsForAccount(accountId, thisLine));
				}
				else
				{
					accountId = thisLine;
				}
			}				
		}
		finally
		{
			reader.close();
		}
	}

	private static Vector getListOfhiddenPacketsForAccount(String accountId, String packetList)
	{
		Vector uids = new Vector();
		String[] packetIds = packetList.trim().split("\\s+");
		for (int i = 0; i < packetIds.length; i++)
		{
			String localId = packetIds[i].trim();
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);
			uids.add(uid);
		}
		return uids;
	}

	public static class MartusSignatureFileAlreadyExistsException extends Exception 
	{
	}
	
	public static class MartusSignatureFileDoesntExistsException extends Exception 
	{
	}

	public static class FileTooLargeException extends IOException 
	{
	}

	private static final String MARTUS_SIGNATURE_FILE_DATE_FORMAT = "yyyyMMdd-HHmmss";
	private static final String MARTUS_SIGNATURE_FILE_IDENTIFIER = "Martus Signature File";
	private static final String MARTUS_SIGNATURE_FILE_DIRECTORY_NAME = "signatures";
	private static final int MAX_ALLOWED_ENCRYPTED_FILESIZE = 1000*1000;

	
}
