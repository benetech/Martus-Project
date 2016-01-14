/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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

package org.martus.client.swingui;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

public abstract class WorkerThread extends Thread
{
	public void start(ModalBusyDialog dlgToNotify)
	{
		dlg = dlgToNotify;
		super.start();
	}
	
	public void start()
	{
		throw new RuntimeException("Must call start(dlgToNotify) instead!");
	}
	
	public void cleanup() throws Exception
	{
		if(thrown != null)
			throw thrown;
	}
	
	public void run()
	{
		try
		{
			doTheWorkWithNO_SWING_CALLS();
		} 
		catch (Exception e)
		{
			thrown = e;
		}
		dlg.workerFinished();
	}

	public boolean displayConfirmDlgAndWaitForResponse(UiMainWindow mainWindow, String title, String[] contents) throws InterruptedException, InvocationTargetException
	{
		ThreadedConfirmDlg confirm = new ThreadedConfirmDlg(mainWindow, title, contents);
		SwingUtilities.invokeAndWait(confirm);
		return confirm.getResult();
	}

	private static class ThreadedConfirmDlg implements Runnable
	{
		public ThreadedConfirmDlg(UiMainWindow mainWindowToUse, String titleToUse, String[] contentsToUse)
		{
			mainWindow = mainWindowToUse;
			title = titleToUse;
			contents = contentsToUse;
		}
		
		public void run()
		{
			result = mainWindow.confirmDlg(title, contents);
		}
		
		public boolean getResult()
		{
			return result;
		}
		
		UiMainWindow mainWindow;
		boolean result;
		String title;
		String[] contents;
	}

	public void displayNotifyDlg(UiMainWindow mainWindow, String resultMessageTag)
	{
		SwingUtilities.invokeLater(new ThreadedNotifyDlg(mainWindow, resultMessageTag));
	}

	public void displayNotifyDlgAndWaitForResponse(UiMainWindow mainWindow, String resultMessageTag) throws InterruptedException, InvocationTargetException
	{
		SwingUtilities.invokeAndWait(new ThreadedNotifyDlg(mainWindow, resultMessageTag));
	}

	public static class ThreadedNotifyDlg implements Runnable
	{
		public ThreadedNotifyDlg(UiMainWindow mainWindowToUse, String tagToUse)
		{
			tag = tagToUse;
			mainWindow = mainWindowToUse;
		}
		
		public void run()
		{
			mainWindow.notifyDlg(tag);
		}
		UiMainWindow mainWindow;
		String tag;
	}
	
	
	public abstract void doTheWorkWithNO_SWING_CALLS() throws Exception;
	
	ModalBusyDialog dlg;
	public Exception thrown;
}
