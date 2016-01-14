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

package org.martus.client.core;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;


public class TransferableBulletinList implements Transferable
{
	public TransferableBulletinList(ClientBulletinStore store, Bulletin[] bulletinsToUse, BulletinFolder fromFolder)
	{
		db = store.getDatabase();
		sigVerifier = store.getSignatureVerifier();
		bulletins = bulletinsToUse;
		folder = fromFolder;
	}

	boolean createTransferableZipFiles()
	{
		if(files == null)
			files = new Vector();
		try
		{
			for(int i = 0 ; i < bulletins.length ; ++i)
			{
				Bulletin bulletin = bulletins[i];
				
				String summary = bulletin.toFileName();
				File file = File.createTempFile(summary, BULLETIN_FILE_EXTENSION);
				file.deleteOnExit();
				files.add(file);
				DatabaseKey headerKey = DatabaseKey.createKey(bulletin.getUniversalId(), bulletin.getStatus());
				BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, headerKey, file, sigVerifier);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("TransferableBulletin createTransferableZipFiles: " + e);
			return false;
		}
		return true;
	}

	public void dispose()
	{
		if(files == null)
			return;

		try
		{
			for(int i =0 ; i < files.size(); ++i)
			{
				File file = (File)files.get(i);
				file.delete();
			}
		}
		catch(Exception e)
		{
			System.out.println("TransferableBulletin.dispose ignoring: " + e);
		}
	}

	public BulletinFolder getFromFolder()
	{
		return folder;
	}

	public Bulletin[] getBulletins()
	{
		return bulletins;
	}

	// Transferable interface
	public Object getTransferData(DataFlavor flavor) throws
						UnsupportedFlavorException
	{
		if(flavor.equals(bulletinListDataFlavor))
			return this;

		if(!flavor.equals(DataFlavor.javaFileListFlavor))
			throw new UnsupportedFlavorException(flavor);

		if(files == null || files.size() == 0)
		{
			//System.out.println("TransferableBulletin.getTransferData : creatingZipFile");
			if (!createTransferableZipFiles())
				throw new UnsupportedFlavorException(flavor);
		}
		LinkedList list = new LinkedList();
		list.addAll(files);
		return list;
	}

	public DataFlavor[] getTransferDataFlavors()
	{
		DataFlavor[] flavorArray = {
					bulletinListDataFlavor,
					DataFlavor.javaFileListFlavor,
//					DataFlavor.stringFlavor,
//					mimeTextDataFlavor,
// TODO remove all trace of mime and string flavors
// Warning: adding when there was String and mimeText flavors
//			dragging to the desktop failed silently.
					};
		return flavorArray;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		DataFlavor[] flavors = getTransferDataFlavors();

		for(int i = 0; i < flavors.length; ++i)
			if(flavor.equals(flavors[i]))
				return true;
		return false;
	}

	static public DataFlavor getBulletinListDataFlavor()
	{
		return bulletinListDataFlavor;
	}

	static public File[] extractFilesFrom(Transferable t)
	{
		if(t==null || !t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			return null;
		try
		{
			Collection fileList = (Collection)t.getTransferData(DataFlavor.javaFileListFlavor);
			File[] files = null;
			Iterator iterator = fileList.iterator();
			files = new File[fileList.size()];
			int element = 0;
			while(iterator.hasNext())
			{	
				files[element++] = ((File)iterator.next());
			}
			return files;
		}
		catch (Exception e)
		{
			System.out.println("extractFileFrom :" + e);
			return null;
		}
	}

	static public TransferableBulletinList extractFrom(Transferable t)
	{
		if(t == null)
			return null;

		DataFlavor flavor = getBulletinListDataFlavor();

		if(!t.isDataFlavorSupported(flavor))
		{
			return null;
		}
		TransferableBulletinList tb = null;
		try
		{
			tb = (TransferableBulletinList)t.getTransferData(flavor);
		}
		catch(UnsupportedFlavorException e)
		{
			System.out.println("TransferableBulletin.extractFrom unsupported flavor");
		}
		catch(IOException e)
		{
			System.out.println("TransferableBulletin.extractFrom IOException");
		}
		catch(Exception e)
		{
			System.out.println("TransferableBulletin.extractFrom " + e);
		}
		return tb;
	}

	public static final String BULLETIN_FILE_EXTENSION = ".mba";
	static DataFlavor bulletinListDataFlavor = new DataFlavor(TransferableBulletinList.class, "Martus Bulletins");
	Vector files;
	MartusCrypto sigVerifier;
	ReadableDatabase db;
	BulletinFolder folder;
	Bulletin[] bulletins;
}
