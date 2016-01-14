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
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Vector;

import org.martus.amplifier.attachment.AttachmentStorageException;
import org.martus.amplifier.attachment.FileSystemDataManager;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.presentation.DownloadAttachment;
import org.martus.amplifier.search.AttachmentInfo;
import org.martus.amplifier.search.BulletinIndexException;
import org.martus.amplifier.search.BulletinInfo;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;



public class TestDownloadAttachment extends TestCaseEnhanced
{
	public TestDownloadAttachment(String name) throws Exception
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		File testBasePath = createTempDirectory();
		MartusAmplifier.setStaticSecurity(new MockMartusSecurity());
		MartusAmplifier.getSecurity().createKeyPair();
		MartusAmplifier.dataManager = new FileSystemDataManager(testBasePath.getAbsolutePath());
	}
	
	public void tearDown() throws Exception
	{
		MartusAmplifier.dataManager.clearAllAttachments();
		super.tearDown();
	}
	
	public void testGetAttachment() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = new MockAmplifierResponse();
		createSampleSearchResults(request, response);
		request.parameters.put("bulletinIndex","1");
		request.parameters.put("attachmentIndex","1");
		DownloadAttachment servlet = new DownloadAttachment(basePath);
		servlet.internalDoGet(request, response);
		
		String attachment1String = response.getDataString();
		assertEquals("Attachment 1's data not the same?", data1, attachment1String);
		assertTrue("response Should have Content-Type", response.containsHeader("Content-Type"));
		assertTrue("response Should have Content-Disposition", response.containsHeader("Content-Disposition"));
		assertTrue("response Should have Content-Length", response.containsHeader("Content-Length"));
		 
		request.parameters.put("bulletinIndex","1");
		request.parameters.put("attachmentIndex","2");
		MockAmplifierResponse response2 = new MockAmplifierResponse();
		servlet.internalDoGet(request, response2);
		String attachment2String = response2.getDataString();
		assertEquals("Attachment 2's data not the same?", data2, attachment2String);
		assertTrue("response2 Should have Content-Type", response2.containsHeader("Content-Type"));
		assertTrue("response2 Should have Content-Disposition", response2.containsHeader("Content-Disposition"));
		assertTrue("response2 Should have Content-Length", response2.containsHeader("Content-Length"));

		request.parameters.put("bulletinIndex","2");
		request.parameters.put("attachmentIndex","1");
		MockAmplifierResponse response3 = new MockAmplifierResponse();
		servlet.internalDoGet(request, response3);
		String attachment3String = response3.getDataString();
		assertEquals("Attachment 3's data not the same?", data3, attachment3String);
		assertTrue("response3 Should have Content-Type", response3.containsHeader("Content-Type"));
		assertTrue("response3 Should have Content-Disposition", response3.containsHeader("Content-Disposition"));
		assertTrue("response3 Should have Content-Length", response3.containsHeader("Content-Length"));
	}
	
	public void testSetHeaders() throws Exception
	{
		MockAmplifierResponse response = new MockAmplifierResponse();
		assertFalse("Should not already have the header Content-Type", response.containsHeader("Content-Type"));				
		assertFalse("Should not already have the header Content-Disposition", response.containsHeader("Content-Disposition"));				
		assertFalse("Should have already have the header Content-Length", response.containsHeader("Content-Length"));

		response.addHeader( "Content-Type", "application/octet-stream" );
		response.addHeader( "Content-Disposition","attatchment; filename=some name" );
		response.addHeader( "Content-Length","100" );

		assertTrue("Should now have the header Content-Type", response.containsHeader("Content-Type"));				
		assertTrue("Should now have the header Content-Disposition", response.containsHeader("Content-Disposition"));				
		assertTrue("Should now have the header Content-Length", response.containsHeader("Content-Length"));
	}	

	private void createSampleSearchResults(MockAmplifierRequest request, MockAmplifierResponse response) throws Exception
	{
		List infos = getFoundBulletins();
		request.getSession().setAttribute("foundBulletins", infos);
		request.putParameter("query", "test");
		request.parameters.put("bulletinIndex","1");
		request.parameters.put("attachmentIndex","1");
	}

	String accountId = "This would be an account id";
	long unknownSize = -1;
	final UniversalId uid1 = UniversalIdForTesting.createFromAccountAndPrefix(accountId, "A");
	final String label1 = "attachment 1";
	final String data1 = "this is attachment 1";
	final AttachmentInfo attachment1 = new AttachmentInfo(uid1, label1, unknownSize);

	final UniversalId uid2 = UniversalIdForTesting.createFromAccountAndPrefix(accountId, "A");
	final String label2 = "attachment 2";
	final String data2 = "this is attachment 2";
	final AttachmentInfo attachment2 =  new AttachmentInfo(uid2, label2, unknownSize);

	final UniversalId uid3 = UniversalIdForTesting.createFromAccountAndPrefix(accountId, "A");
	final String label3 = "attachment 3";
	final String data3 = "this is attachment 3";
	final AttachmentInfo attachment3 =  new AttachmentInfo(uid3, label3, unknownSize);

	public List getFoundBulletins()
		throws Exception, BulletinIndexException
	{
		Vector infos = new Vector();
		BulletinInfo bulletinInfo1 = new BulletinInfo(uid1);
		bulletinInfo1.addAttachment(attachment1);
		bulletinInfo1.addAttachment(attachment2);
		infos.add(bulletinInfo1);
		writeAttachment(bulletinInfo1, 0, data1);
		writeAttachment(bulletinInfo1, 1, data2);
		
		BulletinInfo bulletinInfo2 = new BulletinInfo(uid2);
		bulletinInfo2.addAttachment(attachment3);
		infos.add(bulletinInfo2);
		writeAttachment(bulletinInfo2, 0, data3);
		
		return infos;
	}

	private void writeAttachment(BulletinInfo bulletinInfo1,int index, String data) throws AttachmentStorageException, UnsupportedEncodingException
	{
		AttachmentInfo attachInfo = (AttachmentInfo)bulletinInfo1.getAttachments().get(index);
		UniversalId uid = UniversalId.createFromAccountAndLocalId(attachInfo.getAccountId(), attachInfo.getLocalId());
		MartusAmplifier.dataManager.putAttachment(uid, new StringInputStreamWithSeek(data));
	}
	
	final String basePath = createTempDirectory().getPath();
}
