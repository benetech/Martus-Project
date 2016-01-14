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

import org.spongycastle.jce.X509Principal;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.x509.X509V1CertificateGenerator;

/**
 * @author roms
 *         Date: 6/17/13
 */
public class MobileSecurityContext implements SecurityContext
{

	@Override
	public String getSecurityProviderName()
	{
		return SECURITY_PROVIDER_SPONGYCASTLE;
	}

	@Override
	public Provider createSecurityProvider()
	{
		return new BouncyCastleProvider();
	}

	@Override
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

	public static final String SECURITY_PROVIDER_SPONGYCASTLE = "SC";
}
