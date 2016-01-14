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

package org.martus.amplifier.presentation.test;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.apache.velocity.context.Context;
import org.martus.amplifier.attachment.FileSystemDataManager;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.presentation.ContactInfo;
import org.martus.amplifier.presentation.DoSearch;
import org.martus.amplifier.search.BulletinIndexException;
import org.martus.amplifier.search.BulletinInfo;
import org.martus.amplifier.velocity.AmplifierServletRequest;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;


public class TestContactInfo extends TestCaseEnhanced
{
	public TestContactInfo(String name)
	{
		super(name);
	}

	public void testBasics() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = createSampleSearchResults(request, response);
		ContactInfo servlet = new ContactInfo();
		String templateName = servlet.selectTemplate(request, response, context);
		assertEquals("InternalError.vm", templateName);
	}
	public void testCorrectContactInfoPageRetrieved() throws Exception
	{
		MockMartusSecurity client = new MockMartusSecurity();
		client.createKeyPair();
		MartusAmplifier.setStaticSecurity(client);
		
		File basePath = createTempDirectory();
		MartusAmplifier.dataManager = new FileSystemDataManager(basePath.getAbsolutePath());
		String accountId = client.getPublicKeyString();		
		contactInfo2 = MartusAmplifier.dataManager.getContactInfoFile(accountId);

		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = createSampleSearchResults(request, response);
		ContactInfo servlet = new ContactInfo();

		String data1 = "data 1";
		String data2 = "data 2";
		Vector contactInfo = new Vector();
		contactInfo.add(accountId);
		contactInfo.add(new Integer(2));
		contactInfo.add(data1);
		contactInfo.add(data2);
		String signature = client.createSignatureOfVectorOfStrings(contactInfo);
		contactInfo.add(signature);		
		MartusAmplifier.dataManager.writeContactInfoToFile(accountId, contactInfo);

		request.parameters.put("index","2");
		String templateName = servlet.selectTemplate(request, response, context);
		assertEquals("ContactInfo.vm", templateName);

		contactInfo2.delete();
		DirectoryUtils.deleteEntireDirectoryTree(basePath);
	}

	private Context createSampleSearchResults(MockAmplifierRequest request, MockAmplifierResponse response) throws Exception
	{
		Context context = new MockContext();
		SearchResultsForTesting sr = new SearchResultsForTesting();
		request.putParameter("query", "title");
		request.parameters.put("index","1");
		request.parameters.put("searchedFor","title");
		sr.selectTemplate(request, response, context);
		clearContextSetBySearchResults(context);
		return context;
	}

	private void clearContextSetBySearchResults(Context context)
	{
		context.put("searchedFor", null);
		context.put("previousBulletin", null);
		context.put("nextBulletin", null);
		context.put("currentBulletin", null);
		context.put("totalBulletins", null);
	}

	final UniversalId uid1 = UniversalIdForTesting.createDummyUniversalId();
	final UniversalId uid2 = UniversalIdForTesting.createDummyUniversalId();
	final UniversalId uid3 = UniversalIdForTesting.createDummyUniversalId();
	final String bulletin1Title = "title 1";
	final String bulletin2Title = "title 2";
	final String bulletin3Title = "title 3";


	class SearchResultsForTesting extends DoSearch
	{
		public List getSearchResults(AmplifierServletRequest request)
			throws Exception, BulletinIndexException
		{
			if(request.getParameter("query")==null)
				throw new Exception("malformed query");
			
			Vector infos = new Vector();
			BulletinInfo bulletinInfo1 = new BulletinInfo(uid1);
			bulletinInfo1.set("title", bulletin1Title);
			infos.add(bulletinInfo1);
			
			BulletinInfo bulletinInfo2 = new BulletinInfo(uid2);
			bulletinInfo2.set("title", bulletin2Title);
			bulletinInfo2.setContactInfoFile(contactInfo2);
			infos.add(bulletinInfo2);
			
			BulletinInfo bulletinInfo3 = new BulletinInfo(uid3);
			bulletinInfo3.set("title", bulletin3Title);
			infos.add(bulletinInfo3);
			return infos;
		}

	}
	
	File contactInfo2;
}
