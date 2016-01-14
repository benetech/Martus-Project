/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2010, Beneficent
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
package org.martus.client.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.crypto.Cipher;

import org.bouncycastle.crypto.engines.RSAEngine;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.InvalidJarException;
import org.martus.common.crypto.MartusSecurity;

public class MartusJarVerification
{

	public static void verifyJars() throws MartusCrypto.InvalidJarException, IOException
	{
		// for bcprov, look for BCKEY.SF (BCKEY.SIG)

		URL jceJarURL = getJarURL(Cipher.class);
		String urlString = jceJarURL.toString();
		int foundAt = urlString.indexOf("bc-jce");
		boolean foundBcJce = (foundAt >= 0);
		MartusLogger.log("verifyJars Cipher: " + urlString);
		
		if(foundBcJce)
		{
			String hintsToSolve = "Make sure Xbootclasspath does not contain bc-jce.jar";
			throw new RuntimeException("bc-jce.jar cannot be used\n\n" + hintsToSolve);
		}
		
		URL bcprovJarURL = getJarURL(RSAEngine.class);
		MartusLogger.log("verifyJars RSA: " + bcprovJarURL.toString());
		String bcprovJarName = BCPROV_JAR_FILE_NAME;
		if(bcprovJarURL.toString().indexOf(bcprovJarName) < 0)
		{
			String hintsToSolve = "\n\nMake sure " + bcprovJarName + " is the only bcprov file in Martus/lib/ext";
			throw new InvalidJarException("Didn't load " + bcprovJarName + hintsToSolve);
		}
		verifySignedKeyFile(bcprovJarName, bcprovJarURL, "BCKEY");
	}

	public static void verifySignedKeyFile(String jarDescription, Class c, String keyFileNameWithoutExtension) throws MartusCrypto.InvalidJarException, IOException
	{
		URL jarURL = getJarURL(c);
		verifySignedKeyFile(jarDescription, jarURL, keyFileNameWithoutExtension);
	}

	private static void verifySignedKeyFile(String jarDescription, URL jarURL, String keyFileNameWithoutExtension) throws IOException, InvalidJarException
		{
			String keyFileOutsideOfMartus = keyFileNameWithoutExtension + SIGNATURE_FILE_EXTENSION;
			String keyFileInMartusJar = keyFileNameWithoutExtension + MARTUS_SIGNATURE_FILE_EXTENSION;
			
			String errorMessageStart = "Verifying " + jarDescription + ": ";
			JarURLConnection jarConnection = (JarURLConnection)jarURL.openConnection();
			JarFile jf = jarConnection.getJarFile();
			JarEntry entry = jf.getJarEntry("META-INF/" + keyFileOutsideOfMartus);
			if(entry == null)
			{
				String basicErrorMessage = "Missing: " + keyFileOutsideOfMartus + " from " + jarURL;
				String hintsToSolve = "\n\nA jar file may be damaged. Try re-installing Martus.";
				throw new MartusCrypto.InvalidJarException(errorMessageStart + basicErrorMessage + hintsToSolve);
			}
			int size = (int)entry.getSize();
			
			InputStream actualKeyFileIn = jf.getInputStream(entry);
			String actual = null;
			try
			{
				actual = readAll(size, actualKeyFileIn);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				String basicErrorMessage = "Error reading Actual Key in File";
				throw new MartusCrypto.InvalidJarException(errorMessageStart + basicErrorMessage);
			}
	
	//		System.out.println("***WARNING*** Skipping verifying signatures of jars");
			InputStream referenceKeyFileIn = MartusSecurity.class.getResourceAsStream(keyFileInMartusJar);
			if(referenceKeyFileIn == null)
			{
				String basicErrorMessage = "Couldn't open " + keyFileInMartusJar + " in Martus jar";
				throw new MartusCrypto.InvalidJarException(errorMessageStart + basicErrorMessage);
			}
			String expected = null;
			try
			{
				expected = readAll(size, referenceKeyFileIn);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				String basicErrorMessage = "Error reading Reference Key in File";
				throw new MartusCrypto.InvalidJarException(errorMessageStart + basicErrorMessage);
			}
			
			if(!actual.equals(expected))
			{
				String basicErrorMessage = "\nUnequal contents for: " + keyFileNameWithoutExtension;
				String hintsToSolve = "\nMight be wrong version of jar (" + jarURL + ")";
				throw new MartusCrypto.InvalidJarException(errorMessageStart + basicErrorMessage + hintsToSolve);
			}
		}

	static String readAll(int size, InputStream streamToRead) throws IOException
	{
		StringBuffer buffer = new StringBuffer(size);
		BufferedReader reader = new BufferedReader(new InputStreamReader(streamToRead));
		try
		{
			while(true)
			{
				String line = reader.readLine();
				if(line == null)
					break;
				buffer.append(line);
				buffer.append('\n');
			}
			streamToRead.close();
			return buffer.toString();
		}
		finally
		{
			reader.close();
		}
	}

	public static URL getJarURL(Class c) throws MartusCrypto.InvalidJarException, MalformedURLException
	{
		String name = c.getName();
		int lastDot = name.lastIndexOf('.');
		String classFileName = name.substring(lastDot + 1) + ".class";
		URL url = c.getResource(classFileName);
		String wholePath = url.toString();
		int bangAt = wholePath.indexOf('!');
		if(bangAt < 0)
			throw new MartusCrypto.InvalidJarException("Couldn't find ! in jar path: " + url);
		
		String jarPart = wholePath.substring(0, bangAt+2);
		URL jarURL = new URL(jarPart);
		return jarURL;
	}

	private static final String MARTUS_SIGNATURE_FILE_EXTENSION = ".SIG";
	private static final String SIGNATURE_FILE_EXTENSION = ".SF";
	public static final String BCPROV_JAR_FILE_NAME = "bcprov-jdk15on-148.jar";

}
