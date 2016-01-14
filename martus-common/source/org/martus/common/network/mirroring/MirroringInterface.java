/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2007, Beneficent
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

package org.martus.common.network.mirroring;

import java.util.Vector;

import org.martus.common.network.NetworkInterfaceConstants;

public interface MirroringInterface
{
	public static final int MARTUS_PORT_FOR_MIRRORING = 986;

	public final static String RESULT_OK = NetworkInterfaceConstants.OK;

	public final static String DEST_OBJECT_NAME = "MartusMirror";
	public final static String CMD_MIRRORING_PING = "mirroringPing";
	public final static String CMD_MIRRORING_LIST_ACCOUNTS = "mirroringListAccounts";
	public final static String CMD_MIRRORING_LIST_SEALED_BULLETINS = "mirroringListSealedBulletins";
	public final static String CMD_MIRRORING_GET_BULLETIN_UPLOAD_RECORD = "mirroringGetBulletinUploadRecord";
	public final static String CMD_MIRRORING_GET_BULLETIN_CHUNK = "mirroringGetBulletinChunk";
	public final static String CMD_MIRRORING_GET_BULLETIN_CHUNK_TYPO = "mirrorintGetBulletinChunk";
	public final static String CMD_MIRRORING_LIST_AVAILABLE_IDS = "mirroringListAvailableIds";
	public final static String CMD_MIRRORING_GET_LIST_OF_FORM_TEMPLATES = "mirroringGetListOfFormTemplates";
	public final static String CMD_MIRRORING_GET_FORM_TEMPLATE = "mirroringGetFormTemplate";


	public Vector request(String callerAccountId, Vector parameters, String signature);
}
