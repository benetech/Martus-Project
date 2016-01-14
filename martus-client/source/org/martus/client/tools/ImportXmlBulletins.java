/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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
package org.martus.client.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.tools.XmlBulletinsImporter.FieldSpecVerificationException;
import org.martus.clientside.PasswordHelper;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.CryptoInitializationException;
import org.martus.common.crypto.MartusCrypto.InvalidKeyPairFileVersionException;
import org.martus.common.fieldspec.CustomFieldError;
import org.martus.common.fieldspec.CustomFieldSpecValidator;
import org.martus.util.UnicodeReader;

public class ImportXmlBulletins
{
	public static void main(String[] args)
	{
		File importDirectory = null;
		File accountDirectory = null;
		boolean prompt = true;
		System.out.println("Martus Bulletin XML Importer");
		
		for (int i = 0; i < args.length; i++)
		{
			if(args[i].startsWith("--account-directory"))
			{
				accountDirectory = new File(args[i].substring(args[i].indexOf("=")+1)); 
			}
			
			if(args[i].startsWith("--import-directory"))
			{
				importDirectory = new File(args[i].substring(args[i].indexOf("=")+1));
			}

			if( args[i].startsWith("--no-prompt") )
			{
				prompt = false;
			}
		}

		if(importDirectory == null || accountDirectory == null )
		{
			System.err.println("\nUsage: ImportXmlBulletin --import-directory=<pathToXmlFiles> --account-directory=<pathToYourMartusAccount> [--no-prompt]");
			System.exit(2);
		}
		
		if(!importDirectory.exists() || !importDirectory.isDirectory())
		{
			System.err.println("Cannot find import directory: " + importDirectory);
			System.exit(3);
		}
		
		File[] bulletinXmlFilesToImport = getXmlFiles(importDirectory);
		if(bulletinXmlFilesToImport.length == 0)
		{
			System.err.println("Error No XML bulletins found.");
			System.exit(4);
		}
		
		MartusCrypto security = createSecurityObject(accountDirectory, prompt);
		ClientBulletinStore clientStore = createBulletinStore(security, accountDirectory);
		BulletinFolder importFolder = createImportFolder(security, clientStore, prompt);

		ImporterOfXmlFilesOfBulletins importer = new ImporterOfXmlFilesOfBulletins(bulletinXmlFilesToImport, clientStore, importFolder, System.out);
		try
		{
			importer.importFiles();
			clientStore.prepareToExitNormally();
		}

		catch(FieldSpecVerificationException e)
		{
			System.err.println(getValidationErrorMessage(e.getErrors()));
			System.exit(6);
		}
		catch(XmlFileVersionTooOld e)
		{
			System.err.println("XML file version too old");
			System.exit(8);
		}
		catch(XmlFileVersionTooNew e)
		{
			System.err.println("XML file version too new");
			System.exit(9);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(7);
		}
		System.out.println("Finished!  " + importer.getNumberOfBulletinsImported() + " bulletins imported into Martus.");
		System.exit(0);
	}

	static private File[] getXmlFiles(File startingDir)
	{
		File[] filesToImport = startingDir.listFiles(new FileFilter()
		{
			public boolean accept(File fileName)
			{
				return fileName.getName().toUpperCase().endsWith(".XML");	
			}
		});
		return filesToImport;
	}
	
	static private MartusCrypto createSecurityObject(File accountDirectory, boolean prompt)
	{
		File keyPairFile = new File(accountDirectory, "MartusKeyPair.dat");
		if(!keyPairFile.exists() || !keyPairFile.isFile())
		{
			System.err.println("Cannot find keypair file: " + keyPairFile);
			System.exit(8);
		}

		String userName = "";
		try
		{
			if(prompt)
			{
				System.out.print("Enter User Name:");
				System.out.flush();
			}

			BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));
			userName = reader.readLine();
			reader.close();
		}
		catch(Exception e)
		{
			System.err.println("ImportXmlBulletin.main UserName: " + e);
			System.exit(8);
		}
		
		String userPassPhrase = "";
		try
		{
			if(prompt)
			{
				System.out.print("Enter Password:");
				System.out.flush();
			}

			BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));
			//TODO security issue here password is a string.
			userPassPhrase = reader.readLine();
			reader.close();
		}
		catch(Exception e)
		{
			System.err.println("ImportXmlBulletin.main Password: " + e);
			System.exit(9);
		}
		
		MartusCrypto security = null;
		//TODO security issue here passphrase is a string.
		char[] passphrase = PasswordHelper.getCombinedPassPhrase(userName, userPassPhrase.toCharArray());
		try
		{
			security = loadCurrentMartusSecurity(keyPairFile, passphrase);
		}
		catch(Exception e)
		{
			System.err.println("Error username or password incorrect: " + e);
			System.exit(10);
		}
		finally
		{
			Arrays.fill(passphrase,'X');
		}
		
		if(security == null)
		{
			System.err.println("Error unable to create Security");
			System.exit(11);
		}
		return security;
	}

	static private MartusCrypto loadCurrentMartusSecurity(File keyPairFile, char[] passphrase) throws CryptoInitializationException, FileNotFoundException, IOException, InvalidKeyPairFileVersionException, AuthorizationFailedException
	{
		MartusCrypto security = new MartusSecurity();
		FileInputStream in = new FileInputStream(keyPairFile);
		security.readKeyPair(in, passphrase);
		in.close();
		return security;
	}
	
	static private ClientBulletinStore createBulletinStore(MartusCrypto security, File accountDirectory)
	{
		ClientBulletinStore clientStore = new ClientBulletinStore(security);
		try
		{
			clientStore.doAfterSigninInitialization(accountDirectory);
			clientStore.loadFolders();
			if(!clientStore.loadFieldSpecCache())
				clientStore.createFieldSpecCacheFromDatabase();
		}
		catch(Exception e)
		{
			System.err.println("Unable to create Bulletin Store:");
			e.printStackTrace();
			System.exit(12);
		}
		return clientStore;
	}

	static private BulletinFolder createImportFolder(MartusCrypto security, ClientBulletinStore store, boolean prompt)
	{
		String folderName = "";
		try
		{
			if(prompt)
			{
				System.out.print("Enter Martus folder name where you want these bulletins to be stored:");
				System.out.flush();
			}

			BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));
			folderName = reader.readLine();
			reader.close();
		}
		catch(Exception e)
		{
			System.err.println("ImportXmlBulletin.main Folder name: " + e);
			System.exit(13);
		}
		
		BulletinFolder importFolder = store.createOrFindFolder(folderName);
		if(importFolder == null)
		{
			System.err.println("Unable to create Import Folder:" + folderName);
			System.exit(14);
		}
		return importFolder;
	}

	static private String getValidationErrorMessage(Vector errors)
	{
		StringBuffer validationErrorMessages = new StringBuffer();
		for(int i = 0; i<errors.size(); ++i)
		{
			validationErrorMessages.append("\n\nBulletin " +(i+1)+"\n");
			CustomFieldSpecValidator currentValidator = (CustomFieldSpecValidator)errors.get(i);
			Vector validationErrors = currentValidator.getAllErrors();
			for(int j = 0; j<validationErrors.size(); ++j)
			{
				CustomFieldError thisError = (CustomFieldError)validationErrors.get(j);
				StringBuffer thisErrorMessage = new StringBuffer(thisError.getCode());
				thisErrorMessage.append(" : ");
				thisErrorMessage.append(thisError.getType());
				thisErrorMessage.append(" : ");
				thisErrorMessage.append(thisError.getTag());
				thisErrorMessage.append(" : ");
				thisErrorMessage.append(thisError.getLabel());
				validationErrorMessages.append(thisErrorMessage);
				validationErrorMessages.append('\n');
			}
		}		
		validationErrorMessages.append("\n\nTo see a list of the errors, please run Martus go to Options, Custom Fields and change <CustomFields> to <xCustomFields> and press OK.");
		return validationErrorMessages.toString();  
	}

}