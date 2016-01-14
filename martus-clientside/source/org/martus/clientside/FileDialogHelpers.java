/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2013, Beneficent
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
package org.martus.clientside;

import java.awt.Component;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.martus.clientside.UiFileChooser.FileDialogResults;

public class FileDialogHelpers
{

	public static File doFileOpenDialog(Component owner, String title, String okButtonLabel, File directory, FileFilter filter)
	{
		if(!SwingUtilities.isEventDispatchThread())
			return UiFileChooser.displayFileOpenDialogOnEventThread(owner, title, directory, okButtonLabel, FileDialogHelpers.NO_FILTER).getChosenFile();
		
		File chosenFile = null;
		while(true)
		{
			FileDialogResults results = UiFileChooser.displayFileOpenDialog(owner, 
					title, directory, okButtonLabel, filter);
			if(results.wasCancelChoosen())
				break;
			
			chosenFile = results.getChosenFile();
			if(!chosenFile.exists())
				continue;
			
			if(!chosenFile.isDirectory())
				break;
			
			directory = chosenFile;
		}
		return chosenFile;
	}

	static public File doFileSaveDialog(JFrame owner, String title, File directory, String defaultFilename, FormatFilter filter, UiLocalization localization)
	{
		File file = null;
		while(true)
		{
			FileDialogResults results = UiFileChooser.displayFileSaveDialog(owner, 
					title, directory, defaultFilename, filter);
			if(results.wasCancelChoosen())
				return null;
			file = results.getChosenFile();
			
			boolean filterExists = filter != null;
			String lowerCaseFilename = file.getName().toLowerCase();
			if(filterExists && !lowerCaseFilename.endsWith(filter.getExtension()))
				file = new File(file.getAbsolutePath() + filter.getExtension());
			
			if(!file.exists())
				break;
			if(UiUtilities.confirmDlg(localization, owner, "OverWriteExistingFile"))
				break;
		}
		
		return file;
	}

	public static final FileFilter NO_FILTER = null;

}
