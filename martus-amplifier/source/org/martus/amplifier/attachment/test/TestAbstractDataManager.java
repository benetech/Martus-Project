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
package org.martus.amplifier.attachment.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.martus.amplifier.attachment.AttachmentNotFoundException;
import org.martus.amplifier.attachment.AttachmentStorageException;
import org.martus.amplifier.attachment.DataManager;
import org.martus.amplifier.test.AbstractAmplifierTestCase;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.DirectoryUtils;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;


public abstract class TestAbstractDataManager 
	extends AbstractAmplifierTestCase
{
	protected TestAbstractDataManager(String name)
	{
		super(name);
	}

	protected void tearDown() throws Exception 
	{
		DirectoryUtils.deleteEntireDirectoryTree(new File(basePath));
		super.tearDown();
	}

	public void testClearAllAttachments() 
		throws AttachmentStorageException, IOException
	{
		DataManager attachmentManager = getAttachmentManager();
		UniversalId id = UniversalIdForTesting.createDummyUniversalId();
		String testString = "ClearAll";
		InputStream sin = new StringInputStreamWithSeek(testString);
		try {
			attachmentManager.putAttachment(id, sin);
		} finally {
			sin.close();
		}
		InputStream in = null;
		try {
			in = attachmentManager.getAttachment(id);
		} catch (AttachmentStorageException e) {
			Assert.fail("Expected an attachment for id: " + id);
		} finally {
			if (in != null) {
				in.close();
			}
		}
		attachmentManager.clearAllAttachments();
		in = null;
		try {
			in = attachmentManager.getAttachment(id);
		} catch (AttachmentNotFoundException expected) {
		} finally {
			if (in != null) {
				in.close();
				Assert.fail(
					"Found something after clearing all attachments");
			}
		}
	}
	
	public void testSimplePutAndGetAttachment() 
		throws AttachmentStorageException, IOException
	{
		DataManager attachmentManager = getAttachmentManager();
		attachmentManager.clearAllAttachments();
		UniversalId id = UniversalIdForTesting.createDummyUniversalId();
		String testString = "SimplePutAndGet";
		InputStream sin = new StringInputStreamWithSeek(testString);
		try {
			attachmentManager.putAttachment(id, sin);
		} finally {
			sin.close();
		}
		InputStream in = null;
		try {
			in = attachmentManager.getAttachment(id);
			Assert.assertEquals(testString, inputStreamToString(in));
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
	public void testPutAndGetTwoSameAccount() 
		throws AttachmentStorageException, IOException
	{
		DataManager attachmentManager = getAttachmentManager();
		attachmentManager.clearAllAttachments();
		UniversalId id = UniversalIdForTesting.createDummyUniversalId();
		String testString = "PutAndGetTwoSameAccount";
		InputStream sin = new StringInputStreamWithSeek(testString);
		try {
			attachmentManager.putAttachment(id, sin);
		} finally {
			sin.close();
		}
		InputStream in = null;
		try {
			in = attachmentManager.getAttachment(id);
			Assert.assertEquals(testString, inputStreamToString(in));
		} finally {
			if (in != null) {
				in.close();
			}
		}
		
		UniversalId id2 = UniversalIdForTesting.createDummyUniversalId();
		String testString2 = "PutAndGetTwoSameAccount2";
		sin = new StringInputStreamWithSeek(testString2);
		try {
			attachmentManager.putAttachment(id2, sin);
		} finally {
			sin.close();
		}
		in = null;
		try {
			in = attachmentManager.getAttachment(id2);
			Assert.assertEquals(testString2, inputStreamToString(in));
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
	public void testPutAndGetTwoDifferentAccounts() 
		throws AttachmentStorageException, IOException
	{
		DataManager attachmentManager = getAttachmentManager();
		attachmentManager.clearAllAttachments();
		UniversalId id = 
			UniversalId.createFromAccountAndLocalId("Account1", "Test");
		String testString = "PutAndGetTwoDifferentAccounts";
		InputStream sin = new StringInputStreamWithSeek(testString);
		try {
			attachmentManager.putAttachment(id, sin);
		} finally {
			sin.close();
		}
		InputStream in = null;
		try {
			in = attachmentManager.getAttachment(id);
			Assert.assertEquals(testString, inputStreamToString(in));
		} finally {
			if (in != null) {
				in.close();
			}
		}
		
		UniversalId id2 = 
			UniversalId.createFromAccountAndLocalId("Account2", "Test");
		String testString2 = "PutAndGetTwoDifferentAccounts2";
		sin = new StringInputStreamWithSeek(testString2);
		try {
			attachmentManager.putAttachment(id2, sin);
		} finally {
			sin.close();
		}
		in = null;
		try {
			in = attachmentManager.getAttachment(id2);
			Assert.assertEquals(testString2, inputStreamToString(in));
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public void testSizeOfFile() 
		throws AttachmentStorageException, IOException
	{
		DataManager attachmentManager = getAttachmentManager();
		attachmentManager.clearAllAttachments();
		UniversalId id = 
			UniversalId.createFromAccountAndLocalId("Account1", "Test");
		String testString = "PutAndGetTwoDifferentAccounts";
		InputStream sin = new StringInputStreamWithSeek(testString);
		try 
		{
			attachmentManager.putAttachment(id, sin);
		} 
		finally 
		{
			sin.close();
		}
		assertEquals("Size not correct?", testString.length(), (int)attachmentManager.getAttachmentSizeInBytes(id));
		assertEquals("Size of file <1K should be 1Kb?",1,(int)attachmentManager.getAttachmentSizeInKb(id));
	}
	
	public void testOverwriteExistingAttachment() 
		throws AttachmentStorageException, IOException
	{
		DataManager attachmentManager = getAttachmentManager();
		attachmentManager.clearAllAttachments();
		UniversalId id = UniversalIdForTesting.createDummyUniversalId();
		String testString = "OverwriteExisting";
		InputStream sin = new StringInputStreamWithSeek(testString);
		try {
			attachmentManager.putAttachment(id, sin);
		} finally {
			sin.close();
		}
		InputStream in = null;
		try {
			in = attachmentManager.getAttachment(id);
			Assert.assertEquals(testString, inputStreamToString(in));
		} finally {
			if (in != null) {
				in.close();
			}
		}
		
		String testString2 = "OverwriteExisting2";
		sin = new StringInputStreamWithSeek(testString2);
		try {
			attachmentManager.putAttachment(id, sin);
		} finally {
			sin.close();
		}
		in = null;
		try {
			in = attachmentManager.getAttachment(id);
			Assert.assertEquals(testString2, inputStreamToString(in));
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
	protected abstract DataManager getAttachmentManager();
}