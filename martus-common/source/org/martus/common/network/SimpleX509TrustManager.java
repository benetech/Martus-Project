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

package org.martus.common.network;

import java.security.Key;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusJceKeyPair;
import org.martus.util.StreamableBase64;


public class SimpleX509TrustManager implements X509TrustManager
{

	public SimpleX509TrustManager()
	{
		super();
	}

	public void checkClientTrusted(X509Certificate[] chain, String authType)
		throws CertificateException
	{
		// WORKAROUND for a bug in Sun JSSE that shipped with 1.4.1_01 and earlier
		// where it would invoke this method instead of checkServerTrusted!
		checkServerTrusted(chain, authType);
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType)
		throws CertificateException
	{
		calledCheckServerTrusted = true;
		if(!authType.equals("RSA") && !authType.equals("DHE_RSA"))
			throw new CertificateException("Only RSA and DHE_RSA supported, not " + authType);
		
		if(chain.length != 3)
			throw new CertificateException("Need three certificates");
		
		X509Certificate cert0 = chain[0];
		X509Certificate cert1 = chain[1];
		X509Certificate cert2 = chain[2];
		int failedCert = 0;
		try
		{
			// NOTE: cert0 is a self-signed key used only for SSL
			failedCert = 0;
			cert0.verify(cert0.getPublicKey());
			// NOTE: cert1 is the SSL key signed by the server's Martus key
			failedCert = 1;
			cert1.verify(cert2.getPublicKey());
			// NOTE: cert2 is a self-signed key of and by the server's Martus key
			failedCert = 2;
			cert2.verify(cert2.getPublicKey());
			failedCert = -1;

			PublicKey tryPublicKey = expectedPublicKey;
			if(tryPublicKey == null)
			{
				if(expectedPublicCode == null)
					throw new CertificateException("No key or code is trusted");
				String certPublicKeyString = SimpleX509TrustManager.getKeyString(cert2.getPublicKey());
				String certPublicCode = MartusCrypto.computePublicCode(certPublicKeyString);
				if(expectedPublicCode.equals(certPublicCode))
				{
					tryPublicKey = cert2.getPublicKey();
				}
			}

			if(tryPublicKey == null)
				throw new CertificateException("Key is not trusted");
			cert1.verify(tryPublicKey);
			String keyString = SimpleX509TrustManager.getKeyString(tryPublicKey);
			setExpectedPublicKey(keyString);
		}
		catch (SignatureException e)
		{
			MartusLogger.logException(e);
			MartusLogger.log("Failed cert: " + failedCert);
			String key0 = SimpleX509TrustManager.getKeyString(cert0.getPublicKey());
			String key1 = SimpleX509TrustManager.getKeyString(cert1.getPublicKey());
			String key2 = SimpleX509TrustManager.getKeyString(cert2.getPublicKey());
			MartusLogger.log("Cert0 public: " + key0);
			if(!key0.equals(key1))
				MartusLogger.log("Cert1 public: " + key1);
			MartusLogger.log("Cert2 public: " + key2);
			MartusLogger.log("Cert2 public code: " + MartusCrypto.formatAccountIdForLog(key2));
			String expectedKeyString = SimpleX509TrustManager.getKeyString(expectedPublicKey);
			MartusLogger.log("Expected public code: " + MartusCrypto.formatAccountIdForLog(expectedKeyString));

			throw new CertificateException(e.toString());
		}
		catch (Exception e)
		{
			//Tests will cause this to fire
			MartusLogger.logException(e);
			throw new CertificateException(e.toString());
		}
	}

	public X509Certificate[] getAcceptedIssuers()
	{
		return null;
	}

	public void setExpectedPublicCode(String expectedPublicCodeToUse)
	{
		expectedPublicCode = expectedPublicCodeToUse;
		expectedPublicKey = null;
	}

	public void setExpectedPublicKey(String expectedPublicKeyToUse)
	{
		expectedPublicKey = MartusJceKeyPair.extractPublicKey(expectedPublicKeyToUse);
		expectedPublicCode = null;
	}
	
	public String getExpectedPublicKey()
	{
		return SimpleX509TrustManager.getKeyString(expectedPublicKey);
	}
	
	public boolean wasCheckServerTrustedCalled()
	{
		return calledCheckServerTrusted;
	}
	
	public void clearCalledCheckServerTrusted()
	{
		calledCheckServerTrusted = false;
	}
	
	public static String getKeyString(Key key) 
	{
		if(key == null)
			return null;
		return StreamableBase64.encode(key.getEncoded());
	}

	private PublicKey expectedPublicKey;
	private String expectedPublicCode;
	private boolean calledCheckServerTrusted;

}
