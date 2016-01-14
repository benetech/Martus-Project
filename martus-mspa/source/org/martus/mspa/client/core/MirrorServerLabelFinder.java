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
package org.martus.mspa.client.core;

import java.util.Vector;

import org.martus.mspa.common.ManagingMirrorServerConstants;


public class MirrorServerLabelFinder implements ManagingMirrorServerConstants
{	
	
	static {		
		loadLabels();	
	}
	
	public static MirrorServerLabelInfo getMessageInfo(int manageTag)
	{
		MirrorServerLabelInfo msg=null;
		for (int i=0; i<labelList.size();i++)
		{
			msg = (MirrorServerLabelInfo) labelList.get(i);
			if (msg.getId() == manageTag)
				return msg; 
		}	
		
		return msg;
	}
	
	private static void loadLabels()
	{
		labelList = new Vector();	
		labelList.add(new MirrorServerLabelInfo(SERVERS_WHOSE_DATA_WE_BACKUP, SERVERS_WHOES_DATA_WE_BACKUP_TITLE,
					AVAILABLE_SERVER_LABEL,DATA_WE_BACKUP_LABEL));
		labelList.add(new MirrorServerLabelInfo(SERVERS_WHOSE_DATA_WE_AMPLIFY, SERVERS_WHOSE_DATA_WE_AMPLIFY_TITLE,
					AVAILABLE_SERVER_LABEL,DATA_WE_AMPLIFY_LABEL));
		labelList.add(new MirrorServerLabelInfo(SERVERS_WHO_AMPLIFY_OUR_DATA, SERVERS_WHO_AMPLIFY_OUR_DATA_TITLE,
					AVAILABLE_SERVER_LABEL, CAN_AMPLIFY_OUR_DATA_LABEL));
		labelList.add(new MirrorServerLabelInfo(SERVERS_WHO_BACKUP_OUR_DATA, SERVERS_WHO_BACKUP_OUR_DATA_TITLE,
					AVAILABLE_SERVER_LABEL,CAN_UPLOAD_OUR_DATA_LABEL));								
	}	
	
	static Vector labelList;
}
