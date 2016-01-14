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
package org.martus.jarverifier;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;


public class JarVerifier
{
	public static void main(String args[])
	{
		keyStoreFile = null;
		printErrors = true;
		processCommandLine(args);
		File jarFile = new File(jarFileNameToVerify); 
		System.exit(JarVerifier.verify(jarFile, printErrors, keyStoreFile));
	}
	
	public static void processCommandLine(String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("Incorrect arguments: JarVerifier [--no-error-messages] [--keystore=<path to keystore>] fileToVerify.jar\n");
			System.exit(JarVerifier.ERROR_INVALID_ARGUMENT);
		}
		
		String NoErrorMessagesTag = "--no-error-messages";
		String KeyStoreTag = "--keystore=";
		for(int arg = 0; arg < (args.length -1); ++arg)
		{
			String argument = args[arg];
			if(argument.equals(NoErrorMessagesTag))
				printErrors = false;
			if(argument.startsWith(KeyStoreTag))
				keyStoreFile = argument.substring(KeyStoreTag.length());
		}
		jarFileNameToVerify = args[args.length -1];
	}
	
	public static int verify(File jarFile, boolean shouldPrintErrorsToConsol)
	{
		return verify(jarFile, shouldPrintErrorsToConsol, null);
	}

	public static int verify(File jarFile, boolean shouldPrintErrorsToConsol, String keystoreToUse)
	{
		try
		{
			return verify(jarFile.toURI().toURL(), shouldPrintErrorsToConsol, keystoreToUse);
		}
		catch (MalformedURLException e)
		{
			if(printErrors)
				System.out.println("Error loading jar file " + jarFile.getAbsolutePath());
			return(ERROR_INVALID_JAR);
		}
	}
	
	public static int verify(URL jarUrl, boolean shouldPrintErrorsToConsol, String keystoreToUse)
	{
		return verifyJarFile(jarUrl, shouldPrintErrorsToConsol, keystoreToUse);
	}
	
	public static int verify(URL jarUrl, boolean shouldPrintErrorsToConsol)
	{
		return verifyJarFile(jarUrl, shouldPrintErrorsToConsol, null);
	}
	
/***********************************************************************
	Internal Private Functions
************************************************************************/
	
	private static KeyStore getKeyStore(String keyStoreToUse) throws Exception
	{
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		URL resource = null;
		if(keyStoreToUse == null)
			resource = JarVerifier.class.getResource("ssmartusclientks");
		else
			resource = new File(keyStoreToUse).toURI().toURL();
		ks.load(resource.openStream(), null); // client keystore
		return ks;
	}

	private static X509Certificate readKeystore(String keyStoreToUse)
	{
		X509Certificate x509 = null;
		try
		{
			KeyStore ks = getKeyStore(keyStoreToUse);
			if (ks.isCertificateEntry(MYKEY))
			{
				java.security.cert.Certificate c = ks.getCertificate(MYKEY);
				if (c instanceof X509Certificate)
				{
					x509 = (X509Certificate) c;
					MessageDigest md = MessageDigest.getInstance("SHA");
					md.update(x509.getEncoded());
					byte[] fingerprint = md.digest();
					byte[] martusDigest = {0x4e,0x65,0x12,0x2d,0x4b,0x1e,7,0x45,
						0x86-256,0xf7-256,0xa1-256,0xa7-256,0x96-256,0x7b,0x7b,0x27,
						0xf6-256,0x70,0,0x32};
					if (!Arrays.equals(fingerprint, martusDigest))
					{
						if(printErrors)
						{						
							System.out.println("Not a Martus keystore.");
							System.out.println("Keystore is signed by: " + x509.getIssuerDN());
						}
						return null;
					}
				}
				else
				{
					if(printErrors)
						System.out.println("not an x509 certificate in keystore ssmartusclientks");
					return null;
				}
			}
			else
			{
				if(printErrors)
					System.out.println("Failed to find certificate in keystore ssmartusclientks");
				return null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return x509;
	}

	private static JarInputStream getJarInputStream(final URL jarUrl)
	{
		JarInputStream jis;
		try
		{
			jis = new JarInputStream(jarUrl.openStream());
		}
		catch (IOException ioe)
		{
			if(printErrors)
				System.out.println("ERROR: Can't open jar URL " + jarUrl);
			return null;
		}

		return jis;
	}

	private static boolean checkJarSealed(final URL jarUrl)
	{
		try
		{
			URL terminatedJarUrl = new URL("jar", "", jarUrl + "!/");
			JarURLConnection uc = (JarURLConnection) terminatedJarUrl.openConnection();
			String sealed = uc.getMainAttributes().getValue(Attributes.Name.SEALED);
			uc.getJarFile().close();
			if (sealed == null || !sealed.equals("true"))
			{
				if(printErrors)
					System.out.println("SERIOUS WARNING! Jar not sealed " + jarUrl);
				return false;
			}
		}
		catch (MalformedURLException mue)
		{
			if(printErrors)
				System.out.println("Error loading jar file " + jarUrl);
			return false;
		}
		catch (IOException ioe)
		{
			if(printErrors)
				System.out.println("ERROR: Can't open jar " + jarUrl);
			return false;
		}

		return true;
	}

	private static boolean closeJis(final JarInputStream jis, final URL url)
	{
		try
		{
			jis.close();
			return true;
		}
		catch (Exception e)
		{
			if(printErrors)
				System.out.println("ERROR: Badly formatted jar file: " + url);
			return false;
		}
	}

	private static int checkForDeletedEntries(final JarInputStream jis, final URL url)
	{
		//  collect manifest entries

		Set manifestEntries = null;
		Manifest manifest = jis.getManifest();
		if (manifest != null)
			manifestEntries = manifest.getEntries().keySet();

		if (manifestEntries == null || manifestEntries.isEmpty()) // fixed: 8/15/02
		{
			if(printErrors)
			{			
				System.out.println("JAR not signed: " + url);
				System.out.println("WARNING: Invalid Martus JAR!");
			}
			return ERROR_JAR_NOT_SIGNED;
		}

		JarEntry je;
		HashSet jarEntries = new HashSet();

		try
		{
			while ((je = jis.getNextJarEntry()) != null) // enumerate found jar entries
			{
				String jarName = je.getName();
				if (!jarName.endsWith("/") && !jarName.endsWith(".SF") && !jarName.endsWith(".RSA"))
					jarEntries.add(jarName);
				jis.closeEntry();
			}
		}
		catch (IOException ioe)
		{
			if(printErrors)
				System.out.println("ERROR: Badly formatted jar file: " + url);
			return ERROR_IO;
		}
		catch (SecurityException se)
		{
			if(printErrors)
				System.out.println("DANGER! JAR entry has been tampered with in " + url);
			return ERROR_SIGNATURE_FAILED;
		}

		// compare the two sets
		if (!manifestEntries.equals(jarEntries)) // compare manifest with jar enumeration
		{
			if(printErrors)
			{
				System.out.println("SERIOUS WARNING! Invalid JAR -- entries added or removed from " + url);
				HashSet total = new HashSet();
				total.addAll(manifestEntries);
				total.addAll(jarEntries);
				String type = "Missing from the Jar ";
				if(jarEntries.size() < manifestEntries.size())
					total.removeAll(jarEntries);
				else
				{
					total.removeAll(manifestEntries);
					type = "Added To the Jar ";
				}
				System.out.println(type + total.size() + " Entries");
				for(Iterator iter = total.iterator(); iter.hasNext();)
				{
					String element = (String) iter.next();
					System.out.println(element);
				}
					
			}
			return ERROR_MISSING_ENTRIES;
		}

		return OK;
	}

	private static boolean validJarEntry(final JarInputStream jis, final String jarName, final JarEntry je,
				final java.security.cert.Certificate x509)
	{
		BufferedInputStream jarBuf = new BufferedInputStream(jis);
		try
		{
			while (jarBuf.read() != -1)
				; // read through entire entry to validate cert
			// returns with certificate if class signed

			java.util.List<? extends Certificate> certs = je.getCodeSigners()[0].getSignerCertPath().getCertificates();
			if (certs == null)
			{
				if(printErrors)
					System.out.println("Not signed: " + jarName);
			}

			java.security.cert.Certificate cert = certs.get(0);
			if (cert.equals(x509))
				return true;
			if(printErrors)
				System.out.println("Signed by someone other than Martus: " + jarName);
		}
		catch (SecurityException se)
		{
			if(printErrors)
				System.out.println("DANGER! JAR entry has been tampered with: " + jarName);
		}
		catch (IOException ioe)
		{
			if(printErrors)
				System.out.println("Error reading jar entry: " + jarName);
		}
		catch (Exception e)
		{
			if(printErrors)
				System.out.println("Cert Null? Not signed?: " + jarName);
		}
		return false;
	}

	private static int validateJarfiles(final JarInputStream jis, final URL url, final X509Certificate x509)
	{
		try
		{
			if (verifyEntriesInJar(jis, x509))
			{
				// NOTE***** The server code relies on the valid result string
				// being EXACTLY "Martus JAR verified.". If this is changed, 
				// the server will break! kbs 2003-07-10
				if(printErrors)
					System.out.println("Martus JAR verified.");
				return JAR_VERIFIED_TRUE;
			}
			if(printErrors)
				System.out.println("WARNING: Invalid Martus JAR!");
			return ERROR_INVALID_MARTUS_JAR;
		}
		catch(IOException e)
		{
			if(printErrors)
				System.out.println("ERROR: Badly formatted jar file: " + url);
			return ERROR_IO;
		}
	}

	private static boolean verifyEntriesInJar(final JarInputStream jis, final X509Certificate x509) throws IOException
	{
		JarEntry je;
		while ((je = jis.getNextJarEntry()) != null) // enumerate Jar entries
		{
			String jarName = je.getName();
			if (!jarName.endsWith("/") && !jarName.endsWith(".SF") && !jarName.endsWith(".RSA"))
			{
				if (validJarEntry(jis, jarName, je, x509))
				{
					jis.closeEntry();
				}
				else
				{
					jis.closeEntry();
					return false;
				}
			}
		}
		return true;
	}

	private static int verifyJarFile(URL jarUrl, boolean shouldPrintErrors, String keyStoreToUse)
	{
		printErrors = shouldPrintErrors;
		X509Certificate x509 = readKeystore(keyStoreToUse);

		JarInputStream jis = getJarInputStream(jarUrl);
		if (jis == null)
			return(ERROR_INVALID_JAR);

		int checkForDeletedEntriesResult = checkForDeletedEntries(jis, jarUrl);
		if (checkForDeletedEntriesResult != OK)
		{
			closeJis(jis, jarUrl);
			return(checkForDeletedEntriesResult);
		}

		// reset jar input stream
		if (!closeJis(jis, jarUrl))
			return(ERROR_IO);

		if (!checkJarSealed(jarUrl))
			return(ERROR_JAR_NOT_SEALED);

		jis = getJarInputStream(jarUrl);
		if (jis == null)
			return(ERROR_IO);

		int validateJarfilesResult = validateJarfiles(jis, jarUrl, x509);
		closeJis(jis, jarUrl);
		return validateJarfilesResult;
	}
	
	static public final int JAR_VERIFIED_TRUE = 0;
	static public final int OK = 0;
	static public final int ERROR_INVALID_ARGUMENT = 1;
	static public final int ERROR_INVALID_JAR = 2;
	static public final int ERROR_MISSING_ENTRIES = 3;
	static public final int ERROR_IO = 4;
	static public final int ERROR_JAR_NOT_SEALED = 5;
	static public final int ERROR_JAR_NOT_SIGNED = 6;
	static public final int ERROR_INVALID_MARTUS_JAR = 7;
	static public final int ERROR_SIGNATURE_FAILED = 8;
	
	static private final String MYKEY = "mykey"; // name of alias is keystore
	static private boolean printErrors;
	static private String keyStoreFile;
	static private String jarFileNameToVerify;
}
