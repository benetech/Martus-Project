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

package org.martus.client.test;

import java.io.File;
import java.util.Vector;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore.AddOlderVersionToFolderFailedException;
import org.martus.client.network.BackgroundRetriever;
import org.martus.client.network.RetrieveCommand;
import org.martus.clientside.UiLocalization;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.TestCaseEnhanced;

public class TestBackgroundRetriever extends TestCaseEnhanced
{
	public TestBackgroundRetriever(String name)
	{
		super(name);
	}

	public void testAppCurrentRetrieveCommand() throws Exception
	{
		MockMartusApp app = MockMartusApp.create(getName());
		RetrieveCommand shouldBeEmpty = app.getCurrentRetrieveCommand();
		assertEquals("not empty?", 0, shouldBeEmpty.getRemainingToRetrieveCount());
		
		BackgroundRetriever retriever = new BackgroundRetriever(app, null);
		assertFalse("empty but work to do?", retriever.hasWorkToDo());
		
		RetrieveCommand rc = createSampleRetrieveCommand();
		app.startBackgroundRetrieve(rc);
		RetrieveCommand got = app.getCurrentRetrieveCommand();
		assertEquals("didn't get it back?", rc.getRemainingToRetrieveCount(), got.getRemainingToRetrieveCount());
		app.deleteAllFiles();
	}
	
	public void testBasics() throws Exception
	{
		MockRetrievingApp app = MockRetrievingApp.createMockRetrievingApp(getName());
		RetrieveCommand rc = createSampleRetrieveCommand();
		app.startBackgroundRetrieve(rc);
		ProgressRecorder progressRecorder = new ProgressRecorder();
		BackgroundRetriever retriever = new BackgroundRetriever(app, progressRecorder);
		
		assertTrue("no work to do?", retriever.hasWorkToDo());
		
		UniversalId uid = rc.getNextToRetrieve();
		retriever.retrieveNext();
		assertEquals("didn't update progress current?", 1, progressRecorder.current);
		assertEquals("didn't set progress max?", rc.getTotalCount(), progressRecorder.max);
		assertEquals("didn't perform retrieve?", uid, app.getRetrievedUid());
		app.deleteAllFiles();
	}

	private RetrieveCommand createSampleRetrieveCommand()
	{
		String sampleFolderName = "Destination";
		Vector sampleUidList = new Vector();
		sampleUidList.add(UniversalIdForTesting.createDummyUniversalId());
		sampleUidList.add(UniversalIdForTesting.createDummyUniversalId());
		sampleUidList.add(UniversalIdForTesting.createDummyUniversalId());
		RetrieveCommand rc = new RetrieveCommand(sampleFolderName, sampleUidList);
		return rc;
	}

}

class ProgressRecorder implements ProgressMeterInterface
{
	public void setStatusMessage(String message)
	{
	}

	@Override
	public void updateProgressMeter(int currentValue, int maxValue)
	{
		current = currentValue;
		max = maxValue;
	}

	@Override
	public boolean shouldExit()
	{
		return false;
	}
	
	@Override
	public void hideProgressMeter()
	{
	}
	
	@Override
	public void finished()
	{
	}

	public int current;
	public int max;
}

class MockRetrievingApp extends MockMartusApp
{
	private MockRetrievingApp(MartusCrypto cryptoToUse, File dataDirectoryToUse, UiLocalization localizationToUse) throws Exception
	{
		super(cryptoToUse, dataDirectoryToUse, localizationToUse);
	}
	
	public static MockRetrievingApp createMockRetrievingApp(String testName) throws Exception 
	{
		File directory = createFakeDataDirectory(testName);
		MockRetrievingApp app = new MockRetrievingApp(createFakeSecurity(), directory, createFakeLocalization(directory));
		initializeMockApp(app, directory);
		return app;
	}
	
	public UniversalId getRetrievedUid()
	{
		return retrievedUid;
	}

	public void retrieveOneBulletinToFolder(UniversalId uid, BulletinFolder retrievedFolder, ProgressMeterInterface progressMeter) throws AddOlderVersionToFolderFailedException, Exception
	{
		retrievedUid = uid;
	}

	UniversalId retrievedUid;
}
