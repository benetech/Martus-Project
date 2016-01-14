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

public interface BulletinConstants
{
	public static final String STATUSMUTABLE = "draft"; //NOTE: Must stay as "draft" for legacy reasons
	public static final String STATUSIMMUTABLE = "sealed"; //NOTE: Must stay as "sealed" for legacy reasons

	public static final String TAGSTATUS = "status";
	public static final String TAGWASSENT = "BulletinWasSent";
	public static final String TAGLANGUAGE = "language";
	public static final String TAGAUTHOR = "author";
	public static final String TAGORGANIZATION = "organization";
	public static final String TAGTITLE = "title";
	public static final String TAGLOCATION = "location";
	public static final String TAGEVENTDATE = "eventdate";
	public static final String TAGENTRYDATE = "entrydate";
	public static final String TAGKEYWORDS = "keywords";
	public static final String TAGSUMMARY = "summary";
	public static final String TAGPUBLICINFO = "publicinfo";
	public static final String TAGPRIVATEINFO = "privateinfo";
	public static final String TAGLASTSAVED = "BulletinLastSaved";
}
