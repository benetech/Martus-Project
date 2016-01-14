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

package org.martus.common.crypto;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import org.martus.common.MartusConstants;

public class SignatureEngine
{
	public static SignatureEngine createSigner(MartusKeyPair keyPair) throws Exception
	{
		SignatureEngine engine = new SignatureEngine();
		engine.prepareToSign(keyPair.getPrivateKey());
		return engine;
	}
	
	public static SignatureEngine createVerifier(String signedByPublicKey) throws Exception
	{
		SignatureEngine engine = new SignatureEngine();
		engine.prepareToVerify(signedByPublicKey);
		return engine;
	}
	
	public void digest(byte b) throws Exception
	{
		engine.update(b);
	}
	
	public void digest(byte[] bytes) throws Exception
	{
		engine.update(bytes);
	}
	
	public void digest(byte[] buffer, int off, int len) throws Exception
	{
		engine.update(buffer, off, len);
	}

	public void digest(InputStream in) throws Exception
	{
		int got;
		byte[] bytes = new byte[MartusConstants.streamBufferCopySize];
		while ((got = in.read(bytes)) >= 0)
			engine.update(bytes, 0, got);
	}
	
	public byte[] getSignature() throws Exception
	{
		return engine.sign();
	}
	
	public boolean isValidSignature(byte[] sig) throws Exception
	{
		return engine.verify(sig);
	}
	
	
	
	
	private SignatureEngine() throws Exception
	{
		engine = Signature.getInstance(SIGN_ALGORITHM, "BC");	
	}
	
	private void prepareToSign(PrivateKey key) throws Exception
	{
		engine.initSign(key);
	}
	
	private void prepareToVerify(String signedByPublicKey) throws Exception
	{
		PublicKey key = MartusJceKeyPair.extractPublicKey(signedByPublicKey);
		engine.initVerify(key);
	}

	Signature engine;


	private static final String SIGN_ALGORITHM = "SHA1WithRSA";
}
