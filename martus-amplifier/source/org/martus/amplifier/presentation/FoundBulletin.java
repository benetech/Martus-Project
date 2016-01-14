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

import org.apache.velocity.context.Context;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.search.BulletinInfo;
import org.martus.amplifier.velocity.AmplifierServlet;
import org.martus.amplifier.velocity.AmplifierServletRequest;
import org.martus.amplifier.velocity.AmplifierServletResponse;
import org.martus.common.bulletin.BulletinHtmlGenerator;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.packet.FieldDataPacket;

public class FoundBulletin extends AmplifierServlet
{
	public String selectTemplate(AmplifierServletRequest request, AmplifierServletResponse response, Context context)
			throws Exception
	{
		super.selectTemplate(request, response, context);
		updateFoundBulletinContext(request, context);
		updateNextPreviousContext(request, context);
		return "FoundBulletin.vm";
	}

	public static void updateFoundBulletinContext(AmplifierServletRequest request, Context context) throws Exception
	{
		List bulletins = getFoundBulletins(request);
		int index = getIndex(request);
		BulletinInfo info = (BulletinInfo)bulletins.get(index - 1);
		context.put("bulletin", info);
		FieldDataPacket fdp = MartusAmplifier.dataManager.getFieldDataPacket(info.getFieldDataPacketUId());
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(MartusAmplifier.localization);
		String htmlRepresentation = generator.getSectionHtmlString(fdp);
		context.put("htmlRepresntation", htmlRepresentation);
		context.put("currentBulletin", new Integer(index));
		context.put("searchedFor", request.getParameter("searchedFor"));
		context.put("totalBulletins", new Integer(bulletins.size()));
		context.put("accountPublicCode", MartusCrypto.computeFormattedPublicCode(info.getAccountId()));
		context.put("bulletinLocalId", info.getLocalId());
		if(info.hasContactInfo())
			context.put("contactInfo", "true");
	}

	private void updateNextPreviousContext(AmplifierServletRequest request, Context context)
	{
		List bulletins = getFoundBulletins(request);
		int index = getIndex(request);
		int previousIndex = index - 1;
		int nextIndex = index + 1;
		if(previousIndex <= 0)
			previousIndex = -1;
		if(nextIndex > bulletins.size())
			nextIndex = -1;
		context.put("previousBulletin", new Integer(previousIndex));
		context.put("nextBulletin", new Integer(nextIndex));
	}

	private static int getIndex(AmplifierServletRequest request)
	{
		return Integer.parseInt(request.getParameter("index"));
	}

	private static List getFoundBulletins(AmplifierServletRequest request)
	{
		return (List)request.getSession().getAttribute("foundBulletins");
	}

}
