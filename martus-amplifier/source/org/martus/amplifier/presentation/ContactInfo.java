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
package org.martus.amplifier.presentation;

import java.util.List;
import java.util.Vector;

import org.apache.velocity.context.Context;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.search.BulletinInfo;
import org.martus.amplifier.velocity.AmplifierServlet;
import org.martus.amplifier.velocity.AmplifierServletRequest;
import org.martus.amplifier.velocity.AmplifierServletResponse;
import org.martus.amplifier.velocity.AmplifierServletSession;

public class ContactInfo extends AmplifierServlet
{
	public String selectTemplate(AmplifierServletRequest request, AmplifierServletResponse response, Context context)
			throws Exception
	{
		super.selectTemplate(request, response, context);
		
		AmplifierServletSession session = request.getSession();
		List bulletins = (List)session.getAttribute("foundBulletins");
		int index = Integer.parseInt(request.getParameter("index"));
		BulletinInfo info = (BulletinInfo)bulletins.get(index - 1);
		if(!info.hasContactInfo())
			return "InternalError.vm";
		Vector contactInfoData = MartusAmplifier.dataManager.getContactInfo(info.getAccountId());
		formatDataForHtmlDisplay(contactInfoData);
		context.put("contactInfo", contactInfoData);
		return "ContactInfo.vm";
	}

}
