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
package org.martus.amplifier.search;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;

public class BulletinInfo implements Serializable
{
	public BulletinInfo(UniversalId bulletinIdToUse)
	{
		fields = new HashMap();
		attachments = new ArrayList();
		bulletinId = bulletinIdToUse;
		fieldDataPacketLocalId = null;
		contactInfoFile = null;
	}
	
	public void set(String field, String value)
	{
		fields.put(field, value);
	}
	
	public String get(String field)
	{
		String value = (String) fields.get(field);
		if (value == null)
			value = "";
		return value;
	}
	
	public void addAttachment(AttachmentInfo attachment)
	{
		attachments.add(attachment);
	}
	
	public void setHistory(String historyString)
	{
		if(historyString == null)
			historyString = "";
		history = BulletinHistory.createFromHistoryString(historyString);
	}
	
	public String getVersion()
	{
		return Integer.toString(1 + history.size());
	}
	
	public Map getFields()
	{
		return fields;
	}
	
	public List getAttachments()
	{
		return attachments;
	}
	
	public UniversalId getBulletinId()
	{
		return bulletinId;
	}
	
	public void setFieldDataPacketUId(String localId)
	{
		fieldDataPacketLocalId = localId;
	}
	
	public UniversalId getFieldDataPacketUId()
	{
		return UniversalId.createFromAccountAndLocalId(bulletinId.getAccountId(), fieldDataPacketLocalId);
	}
	
	public String getAccountId()
	{
		return bulletinId.getAccountId();
	}

	public String getLocalId()
	{
		return bulletinId.getLocalId();
	}
	
	public void setContactInfoFile(File infoFile)
	{
		contactInfoFile = infoFile;
	}
	
	public File getContactInfoFile()
	{
		return contactInfoFile;
	}
	
	public boolean hasContactInfo()
	{
		return(contactInfoFile != null && contactInfoFile.exists());
	}


	private Map fields;
	private List attachments;
	private UniversalId bulletinId;
	private File contactInfoFile;
	BulletinHistory history;
	private String fieldDataPacketLocalId;

}