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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Vector;

import org.logi.crypto.Crypto;
import org.logi.crypto.secretshare.PolySecretShare;
import org.logi.crypto.secretshare.SecretSharingException;
import org.martus.common.MartusConstants;
import org.martus.common.crypto.MartusCrypto.KeyShareException;
import org.martus.util.StreamableBase64;

public class MartusSecretShare
{

	public static Vector buildShares(byte[] secretToShare, SecureRandom random) throws SecretSharingException
	{
		Vector shares = new Vector();
		Crypto.random = random;
		Crypto.initRandom();
		byte[] paddedSecret = new byte[secretToShare.length + 1];
		System.arraycopy(secretToShare,0,paddedSecret,1,secretToShare.length);
		//We need to pad the secret beginning with a 1 because of a bug found 
		//in the logi encryption algorithm that any secret
		//beginning with a 0 or beginning with a byte > 127 will fail.
		paddedSecret[0] = 1; 
		int minNumber = MartusConstants.minNumberOfFilesNeededToRecreateSecret;
		int numberShares = MartusConstants.numberOfFilesInShare;
		PolySecretShare[] polyShares = PolySecretShare.share(minNumber, numberShares, paddedSecret, 512);
		for (int i = 0 ; i < numberShares; ++i)
		{
			shares.add(polyShares[i].toString());
		}
		return shares;
	}

	public static byte[] recoverShares(Vector shares) throws MartusCrypto.KeyShareException
	{
		try 
		{
			int numShares = shares.size();
			PolySecretShare[] polyShares = new PolySecretShare[numShares];
			for(int i = 0; i < numShares; ++i)
			{
				polyShares[i] = (PolySecretShare)PolySecretShare.fromString((String)shares.get(i));
			}
			byte[] recoveredSecret = PolySecretShare.retrieve(polyShares);
			//We needed to pad the secret beginning with a 1 because of a bug found 
			//in the logi encryption algorithm that any secret
			//beginning with a 0 or beginning with a byte > 127 will fail.
			int unpaddedLength = recoveredSecret.length - 1;
			byte[] unpaddedSecret = new byte[unpaddedLength];
			System.arraycopy(recoveredSecret,1,unpaddedSecret,0,unpaddedLength);
			return unpaddedSecret;
		} 
		catch (Exception e) 
		{
			throw new MartusCrypto.KeyShareException(e.toString());
		}
	}

	static byte[] getEncryptedKeyPairFromBundles(Vector bundles)
		throws KeyShareException
	{
		try 
		{
			KeyShareBundle bundle = new KeyShareBundle((String) bundles.get(0));
			return StreamableBase64.decode(bundle.payload);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new KeyShareException();
		}
	}

	static Vector getSharesFromBundles(Vector bundles)
		throws  UnsupportedEncodingException, 
				IOException, 
				KeyShareException 
	{
		Vector shares = new Vector();
		for(int i = 0; i < bundles.size(); ++i)
		{
			KeyShareBundle bundle = new KeyShareBundle((String) bundles.get(i));
			shares.add(bundle.sharePiece);
		}
		return shares;
}

}
