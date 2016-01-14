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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.martus.common.Base64XmlOutputStream;
import org.martus.common.MartusXml;
import org.martus.common.XmlWriterFilter;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.SessionKey;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlParser;
import org.xml.sax.SAXParseException;


public class AttachmentPacket extends Packet
{
	public AttachmentPacket(String account, SessionKey sessionKeyToUse, File fileToAttach, MartusCrypto crypto)
	{
		this(crypto);
		sessionKey = sessionKeyToUse;
		rawFile = fileToAttach;
	}

	public AttachmentPacket(UniversalId uId)
	{
		super(uId);
	}
	
	private AttachmentPacket(MartusCrypto crypto)
	{
		super(createUniversalId(crypto));
		security = crypto;
	}
	
	public long getFileSize()
	{
		return rawFile.length();
	}
	
	public File getRawFile()
	{
		return rawFile;
	}

	public static UniversalId createUniversalId(MartusCrypto accountSecurity)
	{
		return UniversalId.createFromAccountAndLocalId(accountSecurity.getPublicKeyString(), createLocalId(accountSecurity, prefix));
	}

	public static boolean isValidLocalId(String localId)
	{
		return localId.startsWith(prefix);
	}

	public byte[] writeXmlToClientDatabase(Database db, boolean mustEncrypt, MartusCrypto signer) throws
			IOException,
			MartusCrypto.CryptoException
	{
		File temp = db.createTempFile(signer);
		temp.deleteOnExit();
		
		byte[] sig = writeAttachmentXmlFile(signer, temp);

		DatabaseKey headerKey = DatabaseKey.createLegacyKey(getUniversalId());
		HashMap importMap = new HashMap();
		importMap.put(headerKey, temp);
		try
		{
			db.importFiles(importMap);
		}
		catch (RecordHiddenException e)
		{
			e.printStackTrace();
			throw new IOException(e.toString());
		}
		temp.delete();
		return sig;
	}

	public byte[] writeAttachmentXmlFile(MartusCrypto signer, File temp)
		throws IOException
	{
		UnicodeWriter writer = new UnicodeWriter(temp);
		byte[] sig = writeXml(writer, signer);
		writer.close();
		return sig;
	}

	public static void exportRawFileFromXml(InputStreamWithSeek xmlIn, SessionKey sessionKey, MartusCrypto security, OutputStream out) throws
		IOException,
		org.martus.common.packet.Packet.InvalidPacketException,
		org.martus.common.packet.Packet.SignatureVerificationException,
		org.martus.common.packet.Packet.WrongPacketTypeException,
		StreamableBase64.InvalidBase64Exception
	{
		File encryptedTempFile = null;
		try
		{
			if(security != null)
				verifyPacketSignature(xmlIn, null, security);
			
			encryptedTempFile = File.createTempFile("$$$MartusEncryptedAtt", null);
			encryptedTempFile.deleteOnExit();

			FileOutputStream outEncrypted = new FileOutputStream(encryptedTempFile);
		
			exportEncryptedFileContents(xmlIn, outEncrypted, security);
			decryptFile(encryptedTempFile, out, sessionKey, security);
		}
		finally
		{
			out.close();
			if(encryptedTempFile != null)
				encryptedTempFile.delete();
		}
	}

	private static void decryptFile(
		File encryptedFile,
		OutputStream out,
		SessionKey sessionKey,
		MartusCrypto security)
		throws IOException
	{
		InputStreamWithSeek inEncrypted = new FileInputStreamWithSeek(encryptedFile);
		try
		{
			security.decrypt(inEncrypted, out, sessionKey);
		}
		catch(Exception e)
		{
			throw new IOException(e.toString());
		}
		finally
		{
			inEncrypted.close();
		}
	}

	private static void exportEncryptedFileContents(
		InputStreamWithSeek xmlIn,
		FileOutputStream outEncrypted,
		MartusCrypto security)
		throws FileNotFoundException, InvalidPacketException, IOException
	{
		AttachmentPacket dummyPacket = new AttachmentPacket(security);
		try
		{
			XmlAttachmentExporter exporter = new XmlAttachmentExporter(dummyPacket, outEncrypted); 
			SimpleXmlParser.parse(exporter, new UnicodeReader(xmlIn));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new InvalidPacketException(e.toString()); 
		}
		finally
		{
			outEncrypted.close();
		}
	}

	protected String getPacketRootElementName()
	{
		return MartusXml.AttachmentPacketElementName;
	}

	protected void internalWriteXml(XmlWriterFilter dest) throws IOException
	{
		super.internalWriteXml(dest);

		dest.writeStartTag(MartusXml.AttachmentBytesElementName);

		InputStream inRaw = new BufferedInputStream(createFileInputStream());
		OutputStream outXml = new Base64XmlOutputStream(dest);
		try
		{
			security.encrypt(inRaw, outXml, sessionKey);
		}
		catch (Exception e)
		{
			throw new IOException(e.toString());
		}
		outXml.close();
		inRaw.close();

		dest.writeEndTag(MartusXml.AttachmentBytesElementName);
	}

	protected InputStream createFileInputStream() throws FileNotFoundException
	{
		return new FileInputStream(rawFile);
	}

	SessionKey sessionKey;
	File rawFile;
	MartusCrypto security;
	private static final String prefix = "A-";

}

class XmlAttachmentExporter extends XmlPacketLoader
{
	public XmlAttachmentExporter(Packet packetToExport, OutputStream destination)
	{
		super(packetToExport);
		out = destination;
	}
	
	public SimpleXmlDefaultLoader startElement(String tag)
		throws SAXParseException
	{
		if(tag.equals(MartusXml.AttachmentBytesElementName))
			return new XmlBase64Exporter(tag, out);
		return super.startElement(tag);
	}

	public void endElement(String tag, SimpleXmlDefaultLoader ended)
		throws SAXParseException
	{
		if(tag.equals(MartusXml.AttachmentBytesElementName))
			return;
		super.endElement(tag, ended);
	}

	OutputStream out;
}

