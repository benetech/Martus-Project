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

package org.martus.common;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.zip.ZipFile;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.util.StreamFilter;
import org.martus.util.StreamableBase64;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


public class MartusUtilities
{
	public static class FileTooLargeException extends Exception 
	{
	}

	public static class FileVerificationException extends Exception 
	{
	}

	public static class FileSigningException extends Exception 
	{
	}

	public static class ServerErrorException extends Exception
	{
		public ServerErrorException(String message)
		{
			super(message);
		}

		public ServerErrorException()
		{
			this("");
		}

	}

	public static class NotYourBulletinErrorException extends Exception
	{
		public NotYourBulletinErrorException(String message)
		{
			super(message);
		}

		public NotYourBulletinErrorException()
		{
			this("");
		}

	}
	
	public static class BulletinNotFoundException extends Exception
	{
		public BulletinNotFoundException()
		{
			
		}
		
	}

	public static void deleteAllFiles(Vector filesToDelete)
	{
		for (int i = 0; i < filesToDelete.size(); i++)
		{
			((File)filesToDelete.get(i)).delete();
		}
	}
	
	public static int getCappedFileLength(File file) throws FileTooLargeException
	{
		long rawLength = file.length();
		if(rawLength >= Integer.MAX_VALUE || rawLength < 0)
			throw new FileTooLargeException();

		return (int)rawLength;
	}

	public static byte[] createSignatureFromFile(File fileToSign, MartusCrypto signer)
		throws IOException, MartusSignatureException
	{
		FileInputStream in = null;
		try
		{
			in = new FileInputStream(fileToSign);
			byte[] signature = signer.createSignatureOfStream(in);
			return signature;
		}
		finally
		{
			if(in != null)
				in.close();
		}
	}

	public static File getSignatureFileFromFile(File originalFile)
	{
		return new File(originalFile.getAbsolutePath() + ".sig");
	}

	public static void deleteInterimFileAndSignature(File tempFile)
	{
		File tempFileSignature = MartusUtilities.getSignatureFileFromFile(tempFile);
		tempFile.delete();
		tempFileSignature.delete();
	}

	public static File createSignatureFileFromFile(File fileToSign, MartusCrypto signer)
		throws IOException, MartusSignatureException
	{
		File newSigFile = new File(fileToSign.getAbsolutePath() + ".sig.new");
		File existingSig = getSignatureFileFromFile(fileToSign);

		if( newSigFile.exists() )
			newSigFile.delete();

		byte[] signature = createSignatureFromFile(fileToSign, signer);
		String sigString = StreamableBase64.encode(signature);

		UnicodeWriter writer = new UnicodeWriter(newSigFile, UnicodeWriter.APPEND);
		writer.writeln(signer.getPublicKeyString());
		writer.writeln(sigString);
		writer.flush();
		writer.close();

		if(existingSig.exists() )
		{
			existingSig.delete();
		}

		if(!newSigFile.renameTo(existingSig))
			throw new IOException("renameTo failed: " + newSigFile + " -> " + existingSig);


		return existingSig;
	}

	public static void verifyFileAndSignature(File fileToVerify, File signatureFile, MartusCrypto verifier, String accountId)
		throws FileVerificationException
	{
		FileInputStream inData = null;
		try
		{
			UnicodeReader reader = new UnicodeReader(signatureFile);
			String key = reader.readLine();
			String signature = reader.readLine();
			reader.close();

			if(!key.equals(accountId))
				throw new FileVerificationException();

			inData = new FileInputStream(fileToVerify);
			if( !verifier.isValidSignatureOfStream(key, inData, StreamableBase64.decode(signature)) )
				throw new FileVerificationException();
		}
		catch(Exception e)
		{
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

	public static class InvalidPublicKeyFileException extends Exception 
	{
	}
	
	public static Vector importServerPublicKeyFromFile(File file, MartusCrypto verifier) throws 
		IOException, InvalidPublicKeyFileException, PublicInformationInvalidException
	{
		Vector result = new Vector();

		UnicodeReader reader = new UnicodeReader(file);
		try
		{
			String fileType = reader.readLine();
			String keyType = reader.readLine();
			String publicKey = reader.readLine();
			String signature = reader.readLine();

			if(!fileType.startsWith(PUBLIC_KEY_FILE_IDENTIFIER))
				throw new InvalidPublicKeyFileException();
			if(!keyType.equals(PUBLIC_KEY_TYPE_SERVER))
				throw new InvalidPublicKeyFileException();

			validatePublicInfo(publicKey, signature, verifier);

			result.add(publicKey);
			result.add(signature);
		}
		finally
		{
			reader.close();
		}

		return result;
	}
	
	public static void exportServerPublicKey(MartusCrypto security, File outputfile)
		throws MartusSignatureException, InvalidBase64Exception, IOException
	{
		String publicKeyString = security.getPublicKeyString();
		String sigString = security.getSignatureOfPublicKey();

		UnicodeWriter writer = new UnicodeWriter(outputfile);
		try
		{
			writeServerPublicKey(writer, publicKeyString, sigString);
		}
		finally
		{
			writer.close();
		}
	}

	public static void writeServerPublicKey(UnicodeWriter writer, String publicKeyString, String sigString)
		throws IOException
	{
		writer.writeln(PUBLIC_KEY_FILE_IDENTIFIER + "1.0");
		writer.writeln(PUBLIC_KEY_TYPE_SERVER);
		writer.writeln(publicKeyString);
		writer.writeln(sigString);
	}
	
	public static Vector importClientPublicKeyFromFile(File file) throws IOException
	{
		Vector result = new Vector();

		UnicodeReader reader = new UnicodeReader(file);
		String publicKey = reader.readLine();
		String signature = reader.readLine();
		reader.close();

		result.add(publicKey);
		result.add(signature);

		return result;
	}

	public static void exportClientPublicKey(MartusCrypto security, File outputfile)
		throws MartusSignatureException, InvalidBase64Exception, IOException
	{
		String publicKeyString = security.getPublicKeyString();
		String sigString = security.getSignatureOfPublicKey();

		UnicodeWriter writer = new UnicodeWriter(outputfile);
		try
		{
			writer.writeln(publicKeyString);
			writer.writeln(sigString);
		}
		finally
		{
			writer.close();
		}
	}

	public static int getBulletinSize(ReadableDatabase db, BulletinHeaderPacket bhp)
	{
		int size = 0;
		DatabaseKey[] bulletinPacketKeys  = BulletinZipUtilities.getAllPacketKeys(bhp);
		for(int i = 0 ; i < bulletinPacketKeys.length ; ++i)
		{
			try
			{
				size += db.getRecordSize(bulletinPacketKeys[i]);
			}
			catch (IOException e)
			{
				System.out.println("MartusUtilities:bulletinPacketKeys error= " + e);
				return 0;
			} 
			catch (RecordHiddenException e)
			{
				e.printStackTrace();
				return 0;
			}
		}
		return size;
	}

	public static void copyStreamWithFilter(InputStream in, OutputStream rawOut,
									StreamFilter filter) throws IOException
	{
		BufferedOutputStream out = (new BufferedOutputStream(rawOut));
		try
		{
			filter.copyStream(in, out);
		}
		finally
		{
			out.flush();
			rawOut.flush();

			// TODO: We really want to do a sync here, so the server does not
			// have to journal all written data. But under Windows, the unit
			// tests pass, but the actual app throws an exception here. We
			// can't figure out why.
			//rawOut.getFD().sync();
			out.close();
		}
	}

	public static boolean doesPacketNeedLocalEncryption(String packetLocalId, BulletinHeaderPacket bhp, InputStreamWithSeek fdpInputStream) throws IOException
	{
		if(bhp.hasAllPrivateFlag() && bhp.isAllPrivate())
			return false;
		
		if(AttachmentPacket.isValidLocalId(packetLocalId))
			return false;

		int firstByteIsZeroIfEncrypted = fdpInputStream.read();
		fdpInputStream.seek(0);
		if(firstByteIsZeroIfEncrypted == 0)
			return false;

		final String encryptedTag = MartusXml.getTagStart(MartusXml.EncryptedFlagElementName);
		BufferedReader reader = new BufferedReader(new UnicodeReader(fdpInputStream));
		String thisLine = null;
		while( (thisLine = reader.readLine()) != null)
		{
			if(thisLine.indexOf(encryptedTag) >= 0)
			{
				fdpInputStream.seek(0);
				return false;
			}
		}
		fdpInputStream.seek(0);
		return true;
	}

	public static boolean isStringInArray(String[] array, String lookFor)
	{
		for(int newIndex = 0; newIndex < array.length; ++newIndex)
		{
			if(lookFor.equals(array[newIndex]))
				return true;
		}

		return false;
	}

	public static class PublicInformationInvalidException extends Exception 
	{
	}


	public static void validatePublicInfo(String accountId, String sig, MartusCrypto verifier) throws
		PublicInformationInvalidException
	{
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(StreamableBase64.decode(accountId));
			if(!verifier.isValidSignatureOfStream(accountId, in, StreamableBase64.decode(sig)))
				throw new PublicInformationInvalidException();
	
		}
		catch(Exception e)
		{
			//System.out.println("MartusApp.getServerPublicCode: " + e);
			throw new PublicInformationInvalidException();
		}
	}

	public static SSLSocketFactory createSocketFactory(X509TrustManager tm) throws Exception
	{
		SSLContext sslContext = createSSLContext(tm);
		return sslContext.getSocketFactory();
	
	}

	public static SSLContext createSSLContext(TrustManager tm) throws Exception
	{
		TrustManager []tma = {tm};
		SSLContext sslContext = SSLContext.getInstance( "TLS" );
		SecureRandom secureRandom = new SecureRandom();
		sslContext.init( null, tma, secureRandom);
		return sslContext;
	}

	public static void startTimer(TimerTask task, long interval)
	{
		final long IMMEDIATELY = 0;
		startTimerWithDelayInMillis(task, IMMEDIATELY, interval);
	}
	
	public static void startTimerWithDelayInMillis(TimerTask task, long millisToDelay, long intervalInMillis)
	{
		Timer timer = new Timer(true);
		timer.schedule(task, millisToDelay, intervalInMillis);
	}

	public static boolean isValidCharInFolder(char c)
	{
		if (Character.isLetterOrDigit(c) || 
			c > 128 || c == 32)
			return true;

		return false;		
	}

	static private boolean isCharOkInFileName(char c)
	{
		if(Character.isLetterOrDigit(c))
			return true;
		return false;
	}
	
	static public String toFileName(String text)
	{
		return toFileName(text, DEFAULT_FILE_NAME);
	}
	
	static public String toFileName(String text, String defaultFileName)
	{
		final int maxLength = 20;
		final int minLength = 3;
	
		text = new String(text).trim();
		if(text.length() > maxLength)
			text = text.substring(0, maxLength);
	
		text = createValidFileName(text);
		if(text.length() < minLength)
			text = defaultFileName + text;
	
		return text;
	}

	public static String createValidFileName(String text) 
	{
		char[] chars = text.toCharArray();
		for(int i = 0; i < chars.length; ++i)
		{
			if(!MartusUtilities.isCharOkInFileName(chars[i]))
				chars[i] = ' ';
		}
		
		text = new String(chars).trim();
		return text;
	}

	public static boolean isFileNameValid(String originalFileName)
	{
		String newFileName = createValidFileName(originalFileName);
		return newFileName.equals(originalFileName);
	}

	public static String extractIpFromFileName(String fileName) throws 
		MartusUtilities.InvalidPublicKeyFileException 
	{
		final String ipStartString = "ip=";
		int ipStart = fileName.indexOf(ipStartString);
		if(ipStart < 0)
			throw new MartusUtilities.InvalidPublicKeyFileException();
		ipStart += ipStartString.length();
		int ipEnd = ipStart;
		for(int i=0; i < 3; ++i)
		{
			ipEnd = fileName.indexOf(".", ipEnd+1);
			if(ipEnd < 0)
				throw new MartusUtilities.InvalidPublicKeyFileException();
		}
		++ipEnd;
		while(ipEnd < fileName.length() && Character.isDigit(fileName.charAt(ipEnd)))
			++ipEnd;
		String ip = fileName.substring(ipStart, ipEnd);
		return ip;
	}

	public static synchronized Vector loadListFromFile(File listFile) throws IOException
	{
		UnicodeReader reader = new UnicodeReader(listFile);
		Vector list = loadListFromFile(reader);
		reader.close();
		return list;
	}
	
	public static synchronized void writeListToFile(File outFile, Vector outList) throws IOException
	{		
		UnicodeWriter writer = new UnicodeWriter(outFile);
		for (int i=0;i<outList.size();++i)
		{
			writer.writeln((String)outList.get(i));
		}								
		writer.close();			
	}
	
	public static synchronized Vector loadListFromFile(BufferedReader readerInput)
		throws IOException
	{
		Vector result = new Vector();
		try
		{
			while(true)
			{
				String currentLine = readerInput.readLine();
				if(currentLine == null)
					break;
				if(currentLine.length() == 0)
					continue;
					
				if( result.contains(currentLine) )
					continue;
	
				result.add(currentLine);				
			}
			
			return result;
		}
		catch(IOException e)
		{
			throw new IOException(e.getMessage());
		}
	}

	public static String byteArrayToHexString(byte[] bytes)
	{
		int oldLength = bytes.length;
		byte[] positiveBytes = new byte[oldLength+1];
		System.arraycopy(bytes,0,positiveBytes,1,oldLength);
		positiveBytes[0] = 1;
		BigInteger bigInt = new BigInteger(positiveBytes);
		String hex = bigInt.toString(16);
		return hex.substring(1);
	}
	
	public static Vector loadClientListAndExitOnError(File clientFile)
	{
		try
		{
			return LoadList(clientFile);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(12);
			return new Vector();
		}
	}
	
	public static Vector loadClientList(File clientFile)
	{
		try
		{
			return LoadList(clientFile);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return new Vector();
		}
	}	
	
	private static Vector LoadList(File clientFile) throws IOException
	{
		if (!clientFile.exists())
			return new Vector();
		return MartusUtilities.loadListFromFile(clientFile);
	}

	public static BulletinHeaderPacket extractHeaderPacket(String authorAccountId, ZipFile zip, MartusCrypto security) throws Exception
	{
		BulletinZipUtilities.validateIntegrityOfZipFilePackets(authorAccountId, zip, security);
		BulletinHeaderPacket header = BulletinHeaderPacket.loadFromZipFile(zip, security);
		return header;
	}

	public static final String DEFAULT_FILE_NAME = "Martus-";
	static final String PUBLIC_KEY_FILE_IDENTIFIER = "Martus Public Key:";
	static final String PUBLIC_KEY_TYPE_SERVER = "Server";
}
