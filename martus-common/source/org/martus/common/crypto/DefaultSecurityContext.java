/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2013, Beneficent
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

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V1CertificateGenerator;

public class DefaultSecurityContext implements SecurityContext
{
	public String getSecurityProviderName()
	{
		return MartusCrypto.SECURITY_PROVIDER_BOUNCYCASTLE;
	}

	public Provider createSecurityProvider()
	{
		return new BouncyCastleProvider();
	}

	public X509Certificate createCertificate(RSAPublicKey publicKey, RSAPrivateCrtKey privateKey, SecureRandom secureRandom)
			throws SecurityException, SignatureException, InvalidKeyException,
			CertificateEncodingException, IllegalStateException, NoSuchAlgorithmException
	{
			Hashtable attrs = new Hashtable();

			Vector ord = new Vector();
			Vector values = new Vector();

			ord.addElement(X509Principal.C);
			ord.addElement(X509Principal.O);
			ord.addElement(X509Principal.L);
			ord.addElement(X509Principal.ST);
			ord.addElement(X509Principal.EmailAddress);

			final String certificateCountry = "US";
			final String certificateOrganization = "Benetech";
			final String certificateLocation = "Palo Alto";
			final String certificateState = "CA";
			final String certificateEmail = "martus@benetech.org";

			values.addElement(certificateCountry);
			values.addElement(certificateOrganization);
			values.addElement(certificateLocation);
			values.addElement(certificateState);
			values.addElement(certificateEmail);

			attrs.put(X509Principal.C, certificateCountry);
			attrs.put(X509Principal.O, certificateOrganization);
			attrs.put(X509Principal.L, certificateLocation);
			attrs.put(X509Principal.ST, certificateState);
			attrs.put(X509Principal.EmailAddress, certificateEmail);

			// create a certificate
			X509V1CertificateGenerator certGen1 = new X509V1CertificateGenerator();

			certGen1.setSerialNumber(new BigInteger(128, secureRandom));
			certGen1.setIssuerDN(new X509Principal(ord, attrs));
			certGen1.setNotBefore(new Date(System.currentTimeMillis() - 50000));
			certGen1.setNotAfter(new Date(System.currentTimeMillis() + 50000));
			certGen1.setSubjectDN(new X509Principal(ord, values));
			certGen1.setPublicKey( publicKey );
			certGen1.setSignatureAlgorithm("MD5WithRSAEncryption");

			// self-sign it
			X509Certificate cert = certGen1.generate( privateKey );
			return cert;
		}
}
