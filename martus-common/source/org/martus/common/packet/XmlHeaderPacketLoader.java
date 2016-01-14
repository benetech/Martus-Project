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

package org.martus.common.packet;

import java.util.Vector;

import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusXml;
import org.martus.util.StreamableBase64;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlStringLoader;
import org.martus.util.xml.SimpleXmlVectorLoader;
import org.xml.sax.SAXParseException;


public class XmlHeaderPacketLoader extends XmlPacketLoader
{
	public XmlHeaderPacketLoader(BulletinHeaderPacket bhpToFill)
	{
		super(bhpToFill);
		bhp = bhpToFill;
	}

	public SimpleXmlDefaultLoader startElement(String tag)
		throws SAXParseException
	{
		if(getTagsContainingStrings().contains(tag))
			return new SimpleXmlStringLoader(tag);
		else if(tag.equals(MartusXml.AccountsAuthorizedToReadElementName))
			return new AuthorizedToReadLoader();
		else if(tag.equals(MartusXml.AccountsAuthorizedToReadPendingElementName))
			return new AuthorizedToReadPendingLoader();
		else if(tag.equals(MartusXml.HistoryElementName))
			return new SimpleXmlVectorLoader(tag, MartusXml.AncestorElementName);
		else if(tag.equals(MartusXml.ExtendedHistorySectionName))
			return new ExtendedHistoryLoader();
		return super.startElement(tag);
	}

	public void addText(char[] ch, int start, int length)
		throws SAXParseException
	{
	}

	public void endElement(String tag, SimpleXmlDefaultLoader ended)
		throws SAXParseException
	{
		if(getTagsContainingStrings().contains(tag))
			endStringElement(ended);
		else if(tag.equals(MartusXml.AccountsAuthorizedToReadElementName))
			bhp.setAuthorizedToReadKeys(new HeadquartersKeys(((AuthorizedToReadLoader)ended).authorizedKeys));
		else if(tag.equals(MartusXml.AccountsAuthorizedToReadPendingElementName))
			bhp.setAuthorizedToReadKeysPending(new HeadquartersKeys(((AuthorizedToReadPendingLoader)ended).authorizedKeysPending));
		else if(tag.equals(MartusXml.HistoryElementName))
		{
			SimpleXmlVectorLoader loader = (SimpleXmlVectorLoader)ended;
			BulletinHistory history = new BulletinHistory(loader.getVector());
			bhp.setHistory(history);
		}
		else if(tag.equals(MartusXml.ExtendedHistorySectionName))
		{
			ExtendedHistoryLoader loader = (ExtendedHistoryLoader)ended;
			bhp.setExtendedHistory(loader.getHistory());
		}
		else
			super.endElement(tag, ended);
	}
	
	private void endStringElement(SimpleXmlDefaultLoader ended)
		throws SAXParseException
	{
		try
		{
			String tag = ended.getTag();
			String value = ((SimpleXmlStringLoader)ended).getText();
			if(tag.equals(MartusXml.BulletinStatusElementName))
				bhp.setStatus(value);
			else if(tag.equals(MartusXml.LastSavedTimeElementName))
				bhp.setLastSavedTime(Long.parseLong(value));
			else if(tag.equals(MartusXml.AllPrivateElementName))
				bhp.setAllPrivateFromXmlTextValue(value);
			else if(tag.equals(MartusXml.DataPacketIdElementName))
				bhp.setFieldDataPacketId(value);
			else if(tag.equals(MartusXml.DataPacketSigElementName))
				bhp.setFieldDataSignature(StreamableBase64.decode(value));
			else if(tag.equals(MartusXml.PrivateDataPacketIdElementName))
				bhp.setPrivateFieldDataPacketId(value);
			else if(tag.equals(MartusXml.PrivateDataPacketSigElementName))
				bhp.setPrivateFieldDataSignature(StreamableBase64.decode(value));
			else if(tag.equals(MartusXml.PublicAttachmentIdElementName))
				bhp.addPublicAttachmentLocalId(value);
			else if(tag.equals(MartusXml.PrivateAttachmentIdElementName))
				bhp.addPrivateAttachmentLocalId(value);
			else if(tag.equals(MartusXml.AllHQSProxyUploadName))
				bhp.setAllHQsProxyUploadFromXmlTextValue(value);
			else if(tag.equals(MartusXml.HQPublicKeyElementName))
				bhp.setAuthorizedToReadKeys(new HeadquartersKeys(new HeadquartersKey(value)));
			else if(tag.equals(MartusXml.StatusSnapshotName))
				bhp.setStatusSnapshotFromXmlTextValue(value);
			else if(tag.equals(MartusXml.ImmutableOnServerName))
				bhp.setImmutableOnServerFromXmlTextValue(value);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SAXParseException(e.getMessage(), null);
		}
	}
	
	class AuthorizedToReadLoader extends SimpleXmlDefaultLoader
	{
		public AuthorizedToReadLoader()
		{
			super(MartusXml.AccountsAuthorizedToReadElementName);
		}

		public SimpleXmlDefaultLoader startElement(String tag)
			throws SAXParseException
		{
			if(tag.equals(HeadquartersKeys.HQ_KEYS_TAG))
				return HeadquartersKeys.createLoader(authorizedKeys);
			return super.startElement(tag);
		}
		Vector authorizedKeys = new Vector();
	}
	
	class AuthorizedToReadPendingLoader extends SimpleXmlDefaultLoader
	{
		public AuthorizedToReadPendingLoader()
		{
			super(MartusXml.AccountsAuthorizedToReadPendingElementName);
		}

		public SimpleXmlDefaultLoader startElement(String tag)
			throws SAXParseException
		{
			if(tag.equals(HeadquartersKeys.HQ_KEYS_TAG))
				return HeadquartersKeys.createLoader(authorizedKeysPending);
			return super.startElement(tag);
		}
		Vector authorizedKeysPending = new Vector();
	}
	
	class ExtendedHistoryLoader extends SimpleXmlDefaultLoader
	{
		public ExtendedHistoryLoader()
		{
			super(MartusXml.ExtendedHistorySectionName);
			history = new ExtendedHistoryList();
		}
		
		public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
		{
			if(tag.equals(MartusXml.ExtendedHistoryEntryName))
				return new ExtendedHistoryEntryLoader();
			return super.startElement(tag);
		}
		
		public void endElement(String tag, SimpleXmlDefaultLoader ended) throws SAXParseException
		{
			if(tag.equals(MartusXml.ExtendedHistoryEntryName))
				history.add(((ExtendedHistoryEntryLoader)ended).getAccountId(), ((ExtendedHistoryEntryLoader)ended).getHistory());
			else 
				super.endElement(tag, ended);
		}
		
		public ExtendedHistoryList getHistory()
		{
			return history;
		}
		
		private ExtendedHistoryList history;
	}
	
	class ExtendedHistoryEntryLoader extends SimpleXmlDefaultLoader
	{
		public ExtendedHistoryEntryLoader()
		{
			super(MartusXml.ExtendedHistoryEntryName);
		}
		
		public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
		{
			if(tag.equals(MartusXml.ExtendedHistoryClonedFromAccountName))
				return new SimpleXmlStringLoader(tag);
			if(tag.equals(MartusXml.HistoryElementName))
				return new SimpleXmlVectorLoader(tag, MartusXml.AncestorElementName); 
			
			return super.startElement(tag);
		}
		
		public void endElement(String tag, SimpleXmlDefaultLoader ended) throws SAXParseException
		{
			if(tag.equals(MartusXml.ExtendedHistoryClonedFromAccountName))
			{
				accountId = ((SimpleXmlStringLoader)ended).getText();
			}
			else if(tag.equals(MartusXml.HistoryElementName))
			{
				SimpleXmlVectorLoader loader = (SimpleXmlVectorLoader)ended;
				history = new BulletinHistory(loader.getVector());
			}
			else
				super.endElement(tag, ended);
		}
		
		public String getAccountId()
		{
			return accountId;
		}
		
		public BulletinHistory getHistory()
		{
			return history;
		}
		
		private String accountId;
		private BulletinHistory history;
	}
	

	private Vector getTagsContainingStrings()
	{
		if(stringTags == null)
		{
			stringTags = new Vector();
			stringTags.add(MartusXml.BulletinStatusElementName);
			stringTags.add(MartusXml.LastSavedTimeElementName);
			stringTags.add(MartusXml.AllPrivateElementName);
			stringTags.add(MartusXml.DataPacketIdElementName);
			stringTags.add(MartusXml.DataPacketSigElementName);
			stringTags.add(MartusXml.PrivateDataPacketIdElementName);
			stringTags.add(MartusXml.PrivateDataPacketSigElementName);
			stringTags.add(MartusXml.PublicAttachmentIdElementName);
			stringTags.add(MartusXml.PrivateAttachmentIdElementName);
			stringTags.add(MartusXml.HQPublicKeyElementName);
			stringTags.add(MartusXml.AllHQSProxyUploadName);
			stringTags.add(MartusXml.StatusSnapshotName);
			stringTags.add(MartusXml.ImmutableOnServerName);
		}
		return stringTags;
	}

	BulletinHeaderPacket bhp;
	private static Vector stringTags;

}
