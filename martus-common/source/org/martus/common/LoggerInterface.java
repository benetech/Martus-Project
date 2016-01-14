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

public interface LoggerInterface
{
	/**logError - always logged, indicates an error which requires attention*/
	public abstract void logError(String message);

	/**logError - always logged, indicates an error which requires attention, prints the stack trace as well*/
	public abstract void logError(Exception e);
	
	/**logError - always logged, indicates an error which requires attention, prints error message followed by the stack trace*/
	public abstract void logError(String message, Exception e);

	/**logWarning - always logged, could potentially be a problem*/
	public abstract void logWarning(String message);
	
	/**logNotice - always logged, describes changes to files or summary of periodic event*/
	public abstract void logNotice(String message);

	/**logInfo - deleted from archival logs, usually used to ensure system calls have finished*/
	public abstract void logInfo(String message);

	/**logDebug - may not always be logged, turned on when system is behaving incorrectly*/
	public abstract void logDebug(String message);
}
