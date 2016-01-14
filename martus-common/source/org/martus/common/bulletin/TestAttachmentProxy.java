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

package org.martus.common.bulletin;

import java.io.File;
import java.util.Arrays;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.crypto.SessionKey;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.*;

public class TestAttachmentProxy extends TestCaseEnhanced
{
	public TestAttachmentProxy(String name)
	{
		super(name);
	}

	public void testFileProxy() throws Exception
	{
		File file = createTempFileFromName("$$$TestAttachmentProxy");
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.writeln("This is some text");
		writer.close();

		MartusCrypto security = MockMartusSecurity.createClient();
		SessionKey sessionKey = security.createSessionKey();

		AttachmentProxy a = new AttachmentProxy(file);
		assertEquals(file.getName(), a.getLabel());
		assertEquals("file", file, a.getFile());
		assertNull("not null key?", a.getSessionKey());

		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		assertNull("already has a uid?", a.getUniversalId());
		a.setUniversalIdAndSessionKey(uid, sessionKey);
		assertEquals("wrong uid?", uid, a.getUniversalId());
		assertEquals("wrong key?", true, Arrays.equals(sessionKey.getBytes(), a.getSessionKey().getBytes()));
		assertNull("still has file?", a.getFile());

		file.delete();
	}
	
	
	public void testSetPending() throws Exception
	{
		File file = createTempFile();
		AttachmentProxy proxy = new AttachmentProxy(file);
		MartusCrypto security = MockMartusSecurity.createClient();
		SessionKey sessionKey = new SessionKey(new byte[] {1,2,3});
		String accountId = security.getPublicKeyString();
		AttachmentPacket packet = new AttachmentPacket(accountId, sessionKey, file, security); 
		proxy.setPendingPacket(packet, sessionKey);
		assertEquals("didn't set packet?", packet, proxy.getPendingPacket());
		assertEquals("didn't set uid?", packet.getUniversalId(), proxy.getUniversalId());
		assertNull("didn't clear file?", proxy.getFile());
		assertEquals("didn't set session key?", sessionKey, proxy.getSessionKey());
	}

	public void testUidProxy() throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		String label = "label";
		AttachmentProxy a = new AttachmentProxy(uid, label, null);
		assertEquals("wrong uid?", uid, a.getUniversalId());
		assertEquals("wrong label?", label, a.getLabel());
		assertNull("has file?", a.getFile());

	}

	public void testStringProxy() throws Exception
	{
		String label = "label";
		AttachmentProxy a = new AttachmentProxy(label);
		assertEquals(label, a.getLabel());
		assertNull("file", a.getFile());
	}
	
	public void testEscapeFileNameForWindows() throws Exception
	{
		String original = "^ & = ( ) | , ; ' \" % ^";
		String expected = "\"^\"\" \"\"&\"\" \"\"=\"\" \"\"(\"\" \"\")\"\" \"\"|\"\" \"\",\"\" \"\";\"\" \"\"'\"\" \"\"\" \"\"%\"\" \"\"^\"";
		assertEquals("Didn't escape properly?", expected, AttachmentProxy.escapeFilenameForWindows(original));
	}
}
