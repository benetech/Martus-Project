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

import java.util.NoSuchElementException;
import java.util.Vector;

import org.json.JSONObject;
import org.martus.client.network.RetrieveCommand;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.TestCaseEnhanced;

public class TestRetrieveCommand extends TestCaseEnhanced
{
	public TestRetrieveCommand(String name)
	{
		super(name);
	}
	
	public void testEmpty()
	{
		RetrieveCommand empty = new RetrieveCommand();
		assertEquals("not zero to retrieve?", 0, empty.getRemainingToRetrieveCount());
		assertEquals("not zero already retrieved?", 0, empty.getRetrievedCount());
		try
		{
			empty.getNextToRetrieve();
			fail("should have thrown since nothing to retrieve");
		}
		catch(RuntimeException ignoreExpected)
		{
		}
	}
	
	public void testBasics()
	{
		String sampleFolderName = "Destination";
		Vector sampleUidList = new Vector();
		sampleUidList.add(UniversalIdForTesting.createDummyUniversalId());
		sampleUidList.add(UniversalIdForTesting.createDummyUniversalId());
		sampleUidList.add(UniversalIdForTesting.createDummyUniversalId());
		RetrieveCommand rc = new RetrieveCommand(sampleFolderName, sampleUidList);
		assertEquals("wrong folder name?", sampleFolderName, rc.getFolderName());
		assertEquals("didn't start with something to retrieve?", sampleUidList.size(), rc.getRemainingToRetrieveCount());
		assertEquals("didn't start with nothing retrieved?", 0, rc.getRetrievedCount());
		assertEquals("total count wrong?", sampleUidList.size(), rc.getTotalCount());
		
		assertEquals("wrong next to retrieve?", sampleUidList.get(0), rc.getNextToRetrieve());
		rc.markAsRetrieved((UniversalId)sampleUidList.get(0));
		assertEquals("didn't reduce retrieve count?", sampleUidList.size() - 1, rc.getRemainingToRetrieveCount());
		assertEquals("didn't increase retrieved count?", 1, rc.getRetrievedCount());
		
		try
		{
			rc.markAsRetrieved(UniversalIdForTesting.createDummyUniversalId());
			fail("Should have thrown trying to remove anything other than the first item");
		}
		catch(RuntimeException ignoreExpected)
		{
		}
		
		rc.markAsRetrieved((UniversalId)sampleUidList.get(1));
		assertEquals("didn't reduce retrieve count again?", sampleUidList.size() - 2, rc.getRemainingToRetrieveCount());
		assertEquals("didn't increase retrieved count again?", 2, rc.getRetrievedCount());
	}
	
	public void testJson() throws Exception
	{
		String folderName = "Folder";
		String accountId1 = "Pretend this is an account";
		String accountId2 = "Another fake account id";
		Vector uids = new Vector();
		uids.add(UniversalIdForTesting.createFromAccountAndPrefix(accountId1, "B-"));
		uids.add(UniversalIdForTesting.createFromAccountAndPrefix(accountId1, "B-"));
		uids.add(UniversalIdForTesting.createFromAccountAndPrefix(accountId2, "B-"));
		
		RetrieveCommand rc = new RetrieveCommand(folderName, uids);
		JSONObject json = rc.toJson();
		assertEquals("Wrong json type?", "MartusRetrieveCommand", json.getString("_Type"));
		assertEquals("Wrong data version?", 1, json.getInt("_DataVersion"));
		RetrieveCommand got = new RetrieveCommand(json);
		assertEquals("bad folder?", rc.getFolderName(), got.getFolderName());
		assertEquals("bad remaining count?", rc.getRemainingToRetrieveCount(), got.getRemainingToRetrieveCount());
		assertEquals("bad done count?", rc.getRetrievedCount(), got.getRetrievedCount());
		assertContains("missing 1", got.getNextToRetrieve(), uids);
		rc.markAsRetrieved(rc.getNextToRetrieve());
		assertContains("missing 2", got.getNextToRetrieve(), uids);
		rc.markAsRetrieved(rc.getNextToRetrieve());
		assertContains("missing 3", got.getNextToRetrieve(), uids);
		rc.markAsRetrieved(rc.getNextToRetrieve());
		
		RetrieveCommand gotAfterMarking = new RetrieveCommand(rc.toJson());
		assertEquals("bad folder after marking?", rc.getFolderName(), gotAfterMarking.getFolderName());
		assertEquals("bad remaining count after marking?", rc.getRemainingToRetrieveCount(), gotAfterMarking.getRemainingToRetrieveCount());
		assertEquals("bad done count after marking?", rc.getRetrievedCount(), gotAfterMarking.getRetrievedCount());
		
	}
	
	public void testBadJson() throws Exception
	{
		JSONObject bad = new JSONObject();
		verifyConstructorThrows("No type or version", bad, NoSuchElementException.class);
		bad.put(RetrieveCommand.TAG_JSON_TYPE, "not what we wanted");
		verifyConstructorThrows("Bad type", bad, RuntimeException.class);
		bad.put(RetrieveCommand.TAG_JSON_TYPE, "MartusRetrieveCommand");
		verifyConstructorThrows("No version", bad, NoSuchElementException.class);
		bad.put(RetrieveCommand.TAG_DATA_VERSION, 0);
		verifyConstructorThrows("Older version", bad, RetrieveCommand.OlderDataVersionException.class);
		bad.put(RetrieveCommand.TAG_DATA_VERSION, 2);
		verifyConstructorThrows("Newer version", bad, RetrieveCommand.NewerDataVersionException.class);
	}

	private void verifyConstructorThrows(String label, JSONObject bad, Class expectedClass)
	{
		try
		{
			new RetrieveCommand(bad);
			fail("Should have thrown: " + label);
		}
		catch(Exception ignoreExpected)
		{
			assertEquals("wrong exception class?", expectedClass, ignoreExpected.getClass());
		}
	}
}
