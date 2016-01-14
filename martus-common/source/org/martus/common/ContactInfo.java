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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.util.StreamableBase64;
import org.martus.util.StreamableBase64.InvalidBase64Exception;


public class ContactInfo extends Vector
{
	public ContactInfo(String author, String organization, String email, 
				String webPage, String phone, String address)
	{
		data = new Vector();
		data.add(author);
		data.add(organization);
		data.add(email);
		data.add(webPage);
		data.add(phone);
		data.add(address);
	}

	public Vector getSignedEncodedVector(
		MartusCrypto signer)
		throws MartusSignatureException, UnsupportedEncodingException
	{
		Vector signedVector = getRawSignedVector(signer);
		Vector encodedContactInfo = ContactInfo.encodeContactInfoVector(signedVector);
		return encodedContactInfo;
	}

	private Vector getRawSignedVector(
		MartusCrypto signer)
		throws MartusCrypto.MartusSignatureException
	{
		final int FIELD_COUNT = data.size();
		
		Vector rawInfo = new Vector();
		rawInfo.add(signer.getPublicKeyString());
		rawInfo.add(new Integer(FIELD_COUNT));
		rawInfo.addAll(data);
		String signature = signer.createSignatureOfVectorOfStrings(rawInfo);
		rawInfo.add(signature);
		return rawInfo;
	}



	public static Vector encodeContactInfoVector(Vector unencodedContactInfo) throws UnsupportedEncodingException
	{
		Vector encoded = new Vector();
		encoded.add(NetworkInterfaceConstants.BASE_64_ENCODED);
		encoded.add(unencodedContactInfo.get(0));
		encoded.add(unencodedContactInfo.get(1));
		int start = 2;
		int i = start;
		int stringsToEncode = ((Integer)(unencodedContactInfo.get(1))).intValue();
		for(; i < start + stringsToEncode ; ++i)
			encoded.add(StreamableBase64.encode((String)unencodedContactInfo.get(i)));
		encoded.add(unencodedContactInfo.get(i));
		return encoded;
	}
	
	private static boolean isEncoded(Vector possiblyEncodedContactInfo)
	{
		return possiblyEncodedContactInfo.get(0).equals(NetworkInterfaceConstants.BASE_64_ENCODED);
	}
	
	static public Vector decodeContactInfoVectorIfNecessary(Vector possiblyEncodedContactInfo) throws UnsupportedEncodingException, StreamableBase64.InvalidBase64Exception
	{
		if (!isEncoded(possiblyEncodedContactInfo))
			return possiblyEncodedContactInfo;
		return decodeContactInfoVector(possiblyEncodedContactInfo);
		
	}

	private static Vector decodeContactInfoVector(Vector possiblyEncodedContactInfo)
		throws UnsupportedEncodingException, InvalidBase64Exception
	{
		Vector decodedContactInfo = new Vector();
		decodedContactInfo.add(possiblyEncodedContactInfo.get(1));
		decodedContactInfo.add(possiblyEncodedContactInfo.get(2));
		int start = 3;
		int i = start;
		int stringsToDecode = ((Integer)(possiblyEncodedContactInfo.get(2))).intValue();
		for(; i < start + stringsToDecode ; ++i)
		{	
			String encodedData = (String)possiblyEncodedContactInfo.get(i);
			decodedContactInfo.add(new String(StreamableBase64.decode(encodedData),"UTF-8"));
		}
		decodedContactInfo.add(possiblyEncodedContactInfo.get(i));
		return decodedContactInfo;
	}

	public static Vector loadFromFile(File contactFile) throws FileNotFoundException, IOException
	{
		Vector contactInfo = new Vector();
		FileInputStream contactFileInputStream = new FileInputStream(contactFile);
		DataInputStream in = new DataInputStream(contactFileInputStream);
	
		contactInfo.add(in.readUTF());
		int inputDataCount = in.readInt();
		contactInfo.add(new Integer(inputDataCount));
		for(int i = 0; i < inputDataCount + 1; ++i)
		{
			contactInfo.add(in.readUTF());
		}			
		in.close();
		return contactInfo;
	}

	Vector data; 
}
