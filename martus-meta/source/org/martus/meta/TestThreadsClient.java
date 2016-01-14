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
package org.martus.meta;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.ZipFile;

import junit.framework.TestSuite;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.test.MockBulletinStore;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.swing.Utilities;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;


public class TestThreadsClient extends TestCaseEnhanced
{
	public static void main(String[] args)
	{
		if(args.length ==1)
			threadTestLoadScaleFactor = Integer.parseInt(args[0]);
		junit.textui.TestRunner.run (new TestSuite(TestThreadsClient.class));
	}		

	public TestThreadsClient(String name)
	{
		super(name);
	}
	
	public void setUp()
	{
		threads = 50;
		if(Utilities.isMSWindows())
			threads = 3;
		
		iterations = 5;
		if(Utilities.isMSWindows())
			iterations = 3;
	}

	public void testThreadedBulletinActivity() throws Exception
	{
		class BulletinTester extends TestingThread
		{
			BulletinTester(ClientBulletinStore storeToUse, int copiesToDo) throws Exception
			{
				store = storeToUse;
				copies = copiesToDo;
			}
			
			public void run()
			{
				try 
				{
					for(int i=0; i < copies; ++i)
					{
						Bulletin b = store.createEmptyBulletin();
						DatabaseKey key = b.getDatabaseKeyForLocalId(b.getLocalId());

						store.saveBulletin(b);
						assertTrue("not found after save?", store.doesBulletinRevisionExist(key));
						store.destroyBulletin(b);
						assertFalse("found after remove?", store.doesBulletinRevisionExist(key));
					}
				}
				catch (Exception e)
				{
	System.out.println(folderName + ": " + e);
	System.out.flush();
					result = e;
				}
			}
			
			ClientBulletinStore store;
			int copies;
			String folderName;
		}

		class BulletinThreadFactory extends ClientThreadFactory implements ThreadFactory
		{
			BulletinThreadFactory() throws Exception
			{
				
				for (int i = 0; i < 10; i++)
				{
					Bulletin b = store.createEmptyBulletin();
					store.saveBulletin(b);
				}
			}
			
			public TestingThread createThread(int copies) throws Exception
			{
				return new BulletinTester(store, copies);
			}

		}
		
		doThreadTests(new BulletinThreadFactory());
	}

	public void testThreadedPacketWriting() throws Exception
	{
		class PacketWriter extends TestingThread
		{
			PacketWriter(ClientBulletinStore storeToUse, int copiesToDo) throws Exception
			{
				
				copies = copiesToDo;
				bulletin = storeToUse.createEmptyBulletin();
				security = storeToUse.getSignatureGenerator();
			}
			
			public void run()
			{
				try 
				{
					for(int i=0; i < copies; ++i)
					{
						Writer writer = new StringWriter();
						bulletin.getBulletinHeaderPacket().writeXml(writer, security);
						InputStreamWithSeek in = new StringInputStreamWithSeek(writer.toString());
						Packet.validateXml(in, bulletin.getAccount(), bulletin.getLocalId(), null, security);
					}
				} 
				catch (Exception e) 
				{
					result = e;
				}
			}
			
			Bulletin bulletin;
			MartusCrypto security;
			int copies;
		}

		class PacketWriteThreadFactory extends ClientThreadFactory implements ThreadFactory
		{
			PacketWriteThreadFactory() throws Exception
			{
			}
			
			public TestingThread createThread(int copies) throws Exception
			{
				return new PacketWriter(store, copies);
			}
		}
		
		doThreadTests(new PacketWriteThreadFactory());
	}
	
	public void testThreadedExporting() throws Exception
	{
		class Exporter extends TestingThread
		{
			Exporter(ClientBulletinStore store, Bulletin bulletinToExport, int copiesToExport) throws Exception
			{
				bulletin = bulletinToExport;
				file = createTempFile();
				copies = copiesToExport;
				db = store.getDatabase();
				security = store.getSignatureVerifier();
				headerKey = DatabaseKey.createKey(bulletin.getUniversalId(), bulletin.getStatus());
			}
			
			public void run()
			{
				try 
				{
					for(int i=0; i < copies; ++i)
						BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, headerKey, file, security);
					} 
				catch (Exception e) 
				{
					result = e;
				}
			}
			
			Bulletin bulletin;
			File file;
			int copies;
			ReadableDatabase db;
			MartusCrypto security;
			DatabaseKey headerKey;
		}

		class ExportThreadFactory extends ClientThreadFactory implements ThreadFactory
		{
			ExportThreadFactory() throws Exception
			{
				b = store.createEmptyBulletin();
				store.saveBulletin(b);
			}
			
			public TestingThread createThread(int copies) throws Exception
			{
				return new Exporter(store, b, copies);
			}
			
			Bulletin b;
		}
		
		doThreadTests(new ExportThreadFactory());
	}

	public void testThreadedImporting() throws Exception
	{
		class Importer extends TestingThread
		{
			Importer(ClientBulletinStore storeToUse, int copiesToDo) throws Exception
			{
				copies = copiesToDo;
				store = storeToUse;

				file = createTempFile();
				ReadableDatabase db = store.getDatabase();
				security = store.getSignatureVerifier();

				Bulletin b = store.createEmptyBulletin();
				store.saveBulletin(b);
				headerKey = DatabaseKey.createKey(b.getUniversalId(), b.getStatus());
				BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, headerKey, file, security);
				store.destroyBulletin(b);
			}
			
			public void run()
			{
				try 
				{
					for(int i=0; i < copies; ++i)
					{
						ZipFile zip = new ZipFile(file);
						store.importBulletinZipFile(zip);
						zip.close();

						Bulletin b = store.getBulletinRevision(headerKey.getUniversalId());
						assertTrue("import didn't work?", store.doesBulletinRevisionExist(headerKey));
						store.destroyBulletin(b);
					}
				} 
				catch (Exception e) 
				{
					result = e;
				}
			}
			
			ClientBulletinStore store;
			File file;
			int copies;
			MartusCrypto security;
			DatabaseKey headerKey;
		}

		class ImportThreadFactory extends ClientThreadFactory implements ThreadFactory
		{
			ImportThreadFactory() throws Exception
			{
			}
			
			public TestingThread createThread(int copies) throws Exception
			{
				return new Importer(store, copies);
			}
		}
		
		doThreadTests(new ImportThreadFactory());
	}
	
	public void testThreadedFolderListActivity() throws Exception
	{
		class FolderListTester extends TestingThread
		{
			FolderListTester(ClientBulletinStore storeToUse, int copiesToDo, int id) throws Exception
			{
				store = storeToUse;
				copies = copiesToDo;
				folderName = Integer.toString(id);
			}
			
			public void run()
			{
				try 
				{
					for(int i=0; i < copies; ++i)
					{
//	System.out.println("delete " + folderName);
//	System.out.flush();
						store.deleteFolder(folderName);
						assertNull("found after delete1?", store.findFolder(folderName));
//	System.out.println("create " + folderName);
//	System.out.flush();
						store.createFolder(folderName);
						assertNotNull("not found after create?", store.findFolder(folderName));
//	System.out.println("save " + folderName);
//	System.out.flush();
						store.saveFolders();
						assertNotNull("not found after save?", store.findFolder(folderName));
//	System.out.println("delete " + folderName);
//	System.out.flush();
						store.deleteFolder(folderName);
						assertNull("found after delete2?", store.findFolder(folderName));
					}
				}
				catch (Exception e)
				{
	System.out.println(folderName + ": " + e);
	System.out.flush();
					result = e;
				}
			}
			
			ClientBulletinStore store;
			int copies;
			String folderName;
		}

		class FolderListThreadFactory extends ClientThreadFactory implements ThreadFactory
		{
			FolderListThreadFactory() throws Exception
			{
			}
			
			public TestingThread createThread(int copies) throws Exception
			{
				return new FolderListTester(store, copies, nextId++);
			}
			
			int nextId;	
		}
		
		doThreadTests(new FolderListThreadFactory());
	}

	public void testThreadedFolderContentsActivity() throws Exception
	{
		class FolderContentsTester extends TestingThread
		{
			FolderContentsTester(ClientBulletinStore storeToUse, int copiesToDo) throws Exception
			{
				store = storeToUse;
				copies = copiesToDo;
				folderName = "test";
				store.createFolder(folderName);
				bulletins= new Bulletin[copies];
				for (int i = 0; i < bulletins.length; i++)
				{
					bulletins[i] = store.createEmptyBulletin();
					store.saveBulletin(bulletins[i]);
				}
			}
			
			public void run()
			{
				try 
				{
					for(int i=0; i < copies; ++i)
					{
						Bulletin b = bulletins[i];
						UniversalId uid = b.getUniversalId();
						BulletinFolder f = store.findFolder(folderName);
						assertEquals("Already in?", false, f.contains(b));
						store.addBulletinToFolder(f, uid);
						assertEquals("Not added?", true, f.contains(b));
						store.discardBulletin(f, b.getUniversalId());
						assertEquals("Not discarded?", false, f.contains(b));
						store.moveBulletin(b, store.getFolderDiscarded(), f);
						assertEquals("Not moved back?", true, f.contains(b));
						store.removeBulletinFromFolder(f, b);
						assertEquals("Not removed?", false, f.contains(b));
						assertEquals("Not orphan?", true, store.isOrphan(b.getUniversalId()));
						store.addBulletinToFolder(f, uid);
					}
				}
				catch (Exception e)
				{
	System.out.println(folderName + ": " + e);
	System.out.flush();
					result = e;
				}
			}
			ClientBulletinStore store;
			int copies;
			String folderName;
			Bulletin[] bulletins;
		}

		class FolderContentsThreadFactory extends ClientThreadFactory implements ThreadFactory
		{
			FolderContentsThreadFactory() throws Exception
			{
			}
			
			public TestingThread createThread(int copies) throws Exception
			{
				return new FolderContentsTester(store, copies);
			}
		}
		
		doThreadTests(new FolderContentsThreadFactory());
	}
	
	class ClientThreadFactory
	{
		ClientThreadFactory() throws Exception
		{
			store = new MockBulletinStore();
			//store.maxCachedBulletinCount = 10;
		}

		public void tearDown() throws Exception
		{
			store.deleteAllData();
		}

		public int getThreadCount()
		{
			return threads;
		}
		
		public int getIterations()
		{
			return iterations;
		}

		ClientBulletinStore store;
	}

	// Under Java 1.4.2_03 Iterations of 20 with a threadCount of 20 causes a hotspot error
	// Under Java 1.5 with Iterations of 9 with a threadCount of 9 causes the hotspot error
	// Under Java 1.6 with Iterations of 4 with a threadCount of 4 causes the hotspot error
	// Under Linux no hotspot errors occur at any Iteration # or threadCount #, 
	// Since threads are more important for a server than a client, 
	// we want lots of threads and iterations on Linux, 
	// but we want the tests to pass on developer and build machines running MS Windows
	
	public int threads;
	public int iterations;
}
