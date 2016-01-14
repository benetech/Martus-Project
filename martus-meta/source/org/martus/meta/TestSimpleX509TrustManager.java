/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003-2007, Beneficent
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

package org.martus.meta;

import java.io.PrintStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;

import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.network.MartusSecureWebServer;
import org.martus.common.network.SimpleX509TrustManager;
import org.martus.util.TestCaseEnhanced;

public class TestSimpleX509TrustManager extends TestCaseEnhanced 
{

	public TestSimpleX509TrustManager(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		if(securityForSSL == null)
		{
			securityForSSL = MockMartusSecurity.createClient();
			martusServerSecurity = MockMartusSecurity.createServer();
			MartusSecureWebServer.security = MockMartusSecurity.createOtherServer();
			
			RSAPublicKey sslPublicKey = (RSAPublicKey)securityForSSL.getKeyPair().getPublicKey();
			RSAPrivateCrtKey sslPrivateKey = (RSAPrivateCrtKey)securityForSSL.getKeyPair().getPrivateKey();
			RSAPublicKey serverPublicKey = (RSAPublicKey)martusServerSecurity.getKeyPair().getPublicKey();
			RSAPrivateCrtKey serverPrivateKey = (RSAPrivateCrtKey)martusServerSecurity.getKeyPair().getPrivateKey();
			assertNotEquals("reused same key?", sslPrivateKey, serverPrivateKey);
			
			cert0 = securityForSSL.createCertificate(sslPublicKey, sslPrivateKey);
			cert1 = securityForSSL.createCertificate(sslPublicKey, serverPrivateKey);
			cert2 = securityForSSL.createCertificate(serverPublicKey, serverPrivateKey);
			
			assertNotEquals("reused serial number?", cert0.getSerialNumber(), cert1.getSerialNumber());
		}			
	}
	
	public void testCheckServerTrustedNullChain()
	{
		X509Certificate[] nullChain0 = {null, cert1, cert2};
		verifyCheckServerTrustedThrows(nullChain0, "RSA");
		X509Certificate[] nullChain1 = {cert0, null, cert2};
		verifyCheckServerTrustedThrows(nullChain1, "RSA");
		X509Certificate[] nullChain2 = {cert0, cert1, null};
		verifyCheckServerTrustedThrows(nullChain2, "RSA");
	}
	
	public void testCheckServerTrustedBadAuthType()
	{
		X509Certificate[] chain = {cert0};
		verifyCheckServerTrustedThrows(chain, "RSA2");
	}

	public void testCheckServerTrustedNotThreeCerts()
	{
		X509Certificate[] chain = {cert0};
		verifyCheckServerTrustedThrows(chain, "RSA");
	}

	public void testCheckServerTrustedIncorrectCerts()
	{
		X509Certificate[] chain0 = {cert1, cert1, cert2};
		verifyCheckServerTrustedThrows(chain0, "RSA");
		X509Certificate[] chain1 = {cert0, cert0, cert2};
		verifyCheckServerTrustedThrows(chain1, "RSA");
		X509Certificate[] chain2 = {cert0, cert1, cert1};
		verifyCheckServerTrustedThrows(chain2, "RSA");
	}

	public void testCheckServerTrustedUnknownMartusKey() throws Exception
	{
		X509Certificate[] chain0 = {cert0, cert0, cert0};
		verifyCheckServerTrustedThrows(chain0, "RSA");

		SimpleX509TrustManager tm = new SimpleX509TrustManager();
		tm.setExpectedPublicCode(MartusCrypto.computePublicCode(martusServerSecurity.getPublicKeyString()));
		PrintStream loggingDestination = MartusLogger.getDestination();
		MartusLogger.disableLogging();
		try 
		{
			tm.checkServerTrusted(chain0, "RSA");
			fail("Didn't throw?");
		} 
		catch (CertificateException expectedException) 
		{
		}
		finally
		{
			MartusLogger.setDestination(loggingDestination);
		}
		
	}


	public void testCheckServerTrustedValidChain() throws Exception
	{
		X509Certificate[] validChain = {cert0, cert1, cert2};
		SimpleX509TrustManager tm = new SimpleX509TrustManager();
		tm.setExpectedPublicKey(martusServerSecurity.getPublicKeyString());
		tm.checkServerTrusted(validChain, "RSA");

		tm.setExpectedPublicCode(MartusCrypto.computePublicCode(martusServerSecurity.getPublicKeyString()));
		tm.checkServerTrusted(validChain, "RSA");
	}

	void verifyCheckServerTrustedThrows(X509Certificate[] chain, String authType) 
	{
		SimpleX509TrustManager tm = new SimpleX509TrustManager();
		tm.setExpectedPublicKey(martusServerSecurity.getPublicKeyString());
		PrintStream loggingDestination = MartusLogger.getDestination();
		MartusLogger.disableLogging();
		try 
		{
			tm.checkServerTrusted(chain, authType);
			fail("Didn't throw?");
		} 
		catch (CertificateException expectedException) 
		{
		}
		finally
		{
			MartusLogger.setDestination(loggingDestination);
		}
		
		verifyCheckClientTrustedThrowsForSunBugIn141_01(chain, authType);
	}

	void verifyCheckClientTrustedThrowsForSunBugIn141_01(X509Certificate[] chain, String authType)
	{
		SimpleX509TrustManager tm = new SimpleX509TrustManager();
		tm.setExpectedPublicKey(martusServerSecurity.getPublicKeyString());
		PrintStream loggingDestination = MartusLogger.getDestination();
		MartusLogger.disableLogging();
		try
		{
			tm.checkClientTrusted(chain, authType);
			fail("Didn't throw?");
		}
		catch (CertificateException expectedException)
		{
		}
		finally
		{
			MartusLogger.setDestination(loggingDestination);
		}
	}

	static MockMartusSecurity securityForSSL;
	static MockMartusSecurity martusServerSecurity;
	static X509Certificate cert0;
	static X509Certificate cert1;
	static X509Certificate cert2;
}
