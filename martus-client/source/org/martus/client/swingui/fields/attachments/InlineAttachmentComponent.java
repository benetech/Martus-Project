/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.client.swingui.fields.attachments;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.AttachmentProxyFile;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.swing.UiLabel;

class InlineAttachmentComponent extends UiLabel
{
	public InlineAttachmentComponent(ClientBulletinStore store, AttachmentProxy proxy) throws Exception
	{
		AttachmentProxyFile apf = AttachmentProxyFile.extractAttachment(store, proxy);
		try
		{
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			File file = apf.getFile();
			Image image = toolkit.getImage(file.getAbsolutePath());
			ImageIcon icon = new ImageIcon(image);
			setIcon(icon);
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}
		finally
		{
			apf.release();
		}
	}

	
	public boolean isValid()
	{
		Icon icon = getIcon();
		if(icon == null)
			return false;
		
		return (icon.getIconHeight() > 0);
	}
}