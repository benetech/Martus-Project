/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2013, Beneficent
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
package org.martus.common.crypto;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.martus.common.crypto.MartusCrypto.MartusSignatureException;

public class SignedBundleInputStream extends DataInputStream
{
	public SignedBundleInputStream(InputStream in, MartusCrypto securityToUse) throws IOException, MartusSignatureException
	{
		super(in);
		security = securityToUse;
		
		if(readInt() != MartusSecurity.BUNDLE_VERSION)
			throw new IOException("Unsupported version:");
		signedByPublicKey = readUTF();
		readSignedBundleData();
	}

	public void readSignedBundleData() throws IOException, MartusSignatureException
	{
		byte[] sig = new byte[readInt()];
		read(sig);
		dataBytes = new byte[readInt()];
		read(dataBytes);
		if(!security.isValidSignatureOfStream(signedByPublicKey, new ByteArrayInputStream(dataBytes), sig))
			throw new MartusSignatureException();
	}
	
	public String getSignedByPublicKey()
	{
		return signedByPublicKey;
	}
	
	public byte[] getDataBytes()
	{
		return dataBytes;
	}

	private MartusCrypto security;
	private String signedByPublicKey;
	private byte[] dataBytes;
}
