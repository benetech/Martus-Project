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

package org.martus.common;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.martus.util.LoggerUtil;


public class LoggerToConsole implements LoggerInterface
{
	public LoggerToConsole()
	{
	}
	
	private void log(String message)
	{
		Timestamp stamp = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat formatDate = new SimpleDateFormat(LOG_DATE_FORMAT);
		String logEntry = formatDate.format(stamp) + " " + message;
		System.out.println(logEntry);
	}

	public void logError(String message)
	{
		log("ERROR: " + message);
	}
	
	public void logError(Exception e)
	{
		logError(LoggerUtil.getStackTrace(e));
	}
	
	public void logError(String message, Exception e)
	{
		String errorMessage = message +" : " + LoggerUtil.getStackTrace(e);
		logError(errorMessage);
	}

	public void logNotice(String message)
	{
		log("Notice: " + message);
	}

	public void logWarning(String message)
	{
		log("Warning: " + message);
	}

	public void logInfo(String message)
	{
		log("Info: " + message);
	}

	public void logDebug(String message)
	{
		log("Debug: " + message);
	}

	static public String LOG_DATE_FORMAT = "EE MM/dd HH:mm:ss z";
}
