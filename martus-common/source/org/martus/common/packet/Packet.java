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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.martus.common.MartusXml;
import org.martus.common.VersionBuildDate;
import org.martus.common.XmlWriterFilter;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.SessionKey;
import org.martus.common.crypto.SignatureEngine;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.util.Stopwatch;
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.xml.SimpleXmlParser;
import org.martus.util.xml.XmlUtilities;
import org.xml.sax.SAXException;


public class Packet
{
	public static class InvalidPacketException extends SAXException
	{
		public InvalidPacketException(String message)
		{
			super(message);
		}

		public InvalidPacketException(Exception e)
		{
			super(e);
		}
	}

	public static class WrongPacketTypeException extends SAXException
	{
		public WrongPacketTypeException(String message)
		{
			super(message);
		}

	}

	public static class SignatureVerificationException extends SAXException
	{
		public SignatureVerificationException()
		{
			super("Signature verification exception");
		}

	}


	public static class WrongAccountException extends Exception
	{
	}

	public Packet()
	{
		this(UniversalId.createFromAccountAndLocalId("Packet()", ""));
	}

	public Packet(UniversalId universalIdToUse)
	{
		uid = universalIdToUse;
	}
	
	static public String createLocalId(MartusCrypto crypto, String prefix)
	{
		String noSuffix = "";
		return createLocalIdWithPrefixAndSuffix(crypto, prefix, noSuffix);
	}

	static public String createLocalIdWithPrefixAndSuffix(MartusCrypto crypto,
			String prefix, String suffix)
	{
		SessionKey sessionKey = crypto.createSessionKey();
		byte[] originalBytes = sessionKey.getBytes();
		return UniversalId.createLocalIdFromByteArray(prefix, originalBytes, suffix);
	}

	public UniversalId getUniversalId()
	{
		return uid;
	}

	public String getAccountId()
	{
		return uid.getAccountId();
	}

	public void setAccountId(String accountString)
	{
		uid.setAccountId(accountString);
	}

	public String getLocalId()
	{
		return uid.getLocalId();
	}

	public void setUniversalId(UniversalId newUid)
	{
		uid = newUid;
	}

	public void setPacketId(String newPacketId)
	{
		uid.setLocalId(newPacketId.replace(':', '-'));
	}

	public boolean isPublicData()
	{
		return false;
	}
	
	public boolean hasUnknownTags()
	{
		return hasUnknown;
	}
	
	public void setHasUnknownTags(boolean newState)
	{
		hasUnknown = newState;
	}

	public byte[] writeXml(OutputStream out, MartusCrypto signer) throws IOException
	{
		UnicodeWriter writer = new UnicodeWriter(out);
		byte[] sig = writeXml(writer, signer);
		writer.flush();
		return sig;
	}

	public byte[] writeXml(Writer writer, MartusCrypto signer) throws IOException
	{
		try
		{
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			XmlWriterFilter dest = new XmlWriterFilter(bufferedWriter);
			synchronized(signer)
			{
				dest.startSignature(signer);
				String startComment = 	MartusXml.packetStartCommentStart +
										VersionBuildDate.getVersionBuildDate() +
										MartusXml.packetFormatVersion +
										MartusXml.packetStartCommentEnd;
				dest.writeDirect(startComment + MartusXml.newLine);
				dest.writeStartTag(getPacketRootElementName());
				internalWriteXml(dest);
				dest.writeEndTag(getPacketRootElementName());

				byte[] sig = dest.getSignature();
				dest.writeDirect(MartusXml.packetSignatureStart);
				dest.writeDirect(StreamableBase64.encode(sig));
				dest.writeDirect(MartusXml.packetSignatureEnd + MartusXml.newLine);

				bufferedWriter.flush();
				return sig;
			}
		}
		catch(MartusCrypto.MartusSignatureException e)
		{
			e.printStackTrace();
			throw new IOException("Signature creation exception: " + e.getMessage());
		}
	}


	public byte[] writeXmlToClientDatabase(Database db, boolean mustEncrypt, MartusCrypto signer) throws
			IOException,
			MartusCrypto.CryptoException
	{
		DatabaseKey headerKey = DatabaseKey.createLegacyKey(getUniversalId());
		try
		{
			return writeXmlToDatabase(db, headerKey, mustEncrypt, signer);
		}
		catch (RecordHiddenException e)
		{
			e.printStackTrace();
			throw new IOException(e.toString());
		}
	}

	public byte[] writeXmlToDatabase(Database db, DatabaseKey headerKey, boolean mustEncrypt, MartusCrypto signer)
		throws IOException, Database.RecordHiddenException, CryptoException
	{
		StringWriter headerWriter = new StringWriter();
		byte[] sig = writeXml(headerWriter, signer);
		if(mustEncrypt && isPublicData())
		{
			ensureSignerIsAuthor(headerKey, signer); 
			db.writeRecordEncrypted(headerKey, headerWriter.toString(), signer);
		}
		else
		{
			db.writeRecord(headerKey, headerWriter.toString());
		}
		return sig;
	}
	
	private void ensureSignerIsAuthor(DatabaseKey headerKey, MartusCrypto signer) throws CryptoException
	{
		if(!headerKey.getAccountId().equals(signer.getPublicKeyString()))
			throw new CryptoException();
	}

	static public void validateXml(InputStreamWithSeek inputStream, String accountId, String localId, byte[] expectedSig, MartusCrypto verifier) throws
		Exception
	{
		verifyPacketSignature(inputStream, expectedSig, verifier);
		
		UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);
		Packet dummyPacket = PacketFactory.createEmptyPacket(uid);
		if(dummyPacket == null)
			throw new InvalidPacketException("Unknown local id type");
		XmlPacketLoader verifyLoader = new XmlPacketLoader(dummyPacket);
		try
		{
			SimpleXmlParser.parse(verifyLoader, new UnicodeReader(inputStream));
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			throw(new InvalidPacketException(e.getMessage()));
		}

		if(!accountId.equals(dummyPacket.getAccountId()))
			throw new WrongAccountException();
		if(!localId.equals(dummyPacket.getLocalId()))
			throw new InvalidPacketException("Wrong Local ID: expected " + localId + " but was " + dummyPacket.getLocalId());
		
	}

	public static byte[] verifyPacketSignature(InputStreamWithSeek inputStream, MartusCrypto verifier) throws
			IOException,
			InvalidPacketException,
			SignatureVerificationException
	{
		return verifyPacketSignature(inputStream, null, verifier);
	}

	public static byte[] verifyPacketSignature(InputStreamWithSeek in, byte[] expectedSig, MartusCrypto verifier) throws
			IOException,
			InvalidPacketException,
			SignatureVerificationException
	{
		Stopwatch timer = new Stopwatch();
		UnicodeReader reader = new UnicodeReader(in);

		final String startComment = reader.readLine();
		if(!isValidStartComment(startComment))
			throw new InvalidPacketException("No start comment");

		final String packetType = reader.readLine();
		final String accountLine = reader.readLine();
		final String publicKey = extractPublicKeyFromXmlLine(accountLine);

		try
		{
			synchronized(verifier)
			{
				SignatureEngine engine = verifier.createSignatureVerifier(publicKey);

				digestOneLine(startComment, engine);
				digestOneLine(packetType, engine);
				digestOneLine(accountLine, engine);

				String sigLine = null;
				String line = null;
				while( (line=reader.readLine()) != null)
				{
					if(line.startsWith(MartusXml.packetSignatureStart))
					{
						sigLine = line;
						break;
					}

					digestOneLine(line, engine);
				}

				byte[] sigBytes = extractSigFromXmlLine(sigLine);
				if(expectedSig != null && !Arrays.equals(expectedSig, sigBytes))
					throw new SignatureVerificationException();

				if(!engine.isValidSignature(sigBytes))
					throw new SignatureVerificationException();

				in.seek(0);
				++callsToVerifyPacketSignature;
				millisInVerifyPacketSignature += timer.elapsed();
				return sigBytes;
			}
		}
		catch(InvalidPacketException e)
		{
			throw(e);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			throw new SignatureVerificationException();
		}
	}

	public static boolean isValidStartComment(final String startComment)
	{
		if( startComment == null )
			return false;
		if (!startComment.startsWith(MartusXml.packetStartCommentStart))
			return false;
		if (!startComment.endsWith(MartusXml.packetStartCommentEnd))
			return false;
		return true;
	}

	static void digestOneLine(final String packetType, SignatureEngine engine)
		throws Exception
	{
		engine.digest(packetType.getBytes("UTF-8"));
		engine.digest(newlineBytes);
	}

	static byte[] extractSigFromXmlLine(String sigLine)
		throws InvalidPacketException
	{
		if(sigLine == null)
			throw new InvalidPacketException("No signature start");
		if(!sigLine.endsWith(MartusXml.packetSignatureEnd))
			throw new InvalidPacketException("No signature end");

		final int sigOverhead = MartusXml.packetSignatureStart.length() +
								MartusXml.packetSignatureEnd.length();
		final int sigLen = sigLine.length() - sigOverhead;
		final int actualSigStart = sigLine.indexOf("=") + 1;
		final int actualSigEnd = actualSigStart + sigLen;

		byte[] sigBytes = null;
		try
		{
			sigBytes = StreamableBase64.decode(sigLine.substring(actualSigStart, actualSigEnd));
		}
		catch(StreamableBase64.InvalidBase64Exception e)
		{
			throw new InvalidPacketException("Signature not valid Base64");
		}
		return sigBytes;
	}

	static String extractPublicKeyFromXmlLine(final String accountLine)
		throws InvalidPacketException
	{
		if(accountLine == null)
			throw new InvalidPacketException("No Account Tag");

		final String accountTag = MartusXml.getTagStart(MartusXml.AccountElementName);
		if(!accountLine.startsWith(accountTag))
			throw new InvalidPacketException("No Account Tag");

		int startIndex = accountLine.indexOf(">");
		int endIndex = accountLine.indexOf("</");
		if(startIndex < 0 || endIndex < 0)
			throw new InvalidPacketException("Invalid Account Element");
		++startIndex;
		final String publicKey = accountLine.substring(startIndex, endIndex);
		return publicKey;
	}

	protected String getPacketRootElementName()
	{
		return null;
	}

	protected void internalWriteXml(XmlWriterFilter dest) throws IOException
	{
		writeElement(dest, MartusXml.PacketIdElementName, getLocalId());
		writeElement(dest, MartusXml.AccountElementName, getAccountId());
	}

	protected void writeElement(XmlWriterFilter dest, String tag, String data) throws IOException
	{
		String encodedData = XmlUtilities.getXmlEncoded(data);
		writeNonEncodedElement(dest, tag, encodedData);
	}

	protected void writeNonEncodedElement(XmlWriterFilter dest, String tag, String data) throws IOException
	{
		dest.writeStartTag(tag);
		dest.writeDirect(data);
		dest.writeEndTag(tag);
	}

	protected void writeNonEncodedXMLString(XmlWriterFilter dest, String data) throws IOException
	{
		dest.writeDirect(data);
	}
	
	public void loadFromXml(InputStreamWithSeek inputStream, MartusCrypto verifier) throws IOException, InvalidPacketException, WrongPacketTypeException, SignatureVerificationException, MartusCrypto.DecryptionException, MartusCrypto.NoKeyPairException
	{
		loadFromXml(inputStream, null, verifier);
	}

	public void loadFromXml(InputStreamWithSeek inputStream, byte[] expectedSig, MartusCrypto verifier) throws
		IOException,
		InvalidPacketException,
		WrongPacketTypeException,
		SignatureVerificationException,
		MartusCrypto.DecryptionException,
		MartusCrypto.NoKeyPairException
	{
		throw new WrongPacketTypeException("Can't call loadFromXml directly on a Packet object!");
	}

	final static byte[] newlineBytes = "\n".getBytes();
	UniversalId uid;
	boolean hasUnknown;

	// NOTE: The following variables are used for diagnostics
	// typically, we print the values to the console when Help/About is done
	public static int callsToVerifyPacketSignature;
	public static long millisInVerifyPacketSignature;

}
