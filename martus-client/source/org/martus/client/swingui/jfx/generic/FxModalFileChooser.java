/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.swingui.jfx.generic;

import java.io.File;

import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

public class FxModalFileChooser
{
	public FxModalFileChooser(PureFxStage parentStageToUse)
	{
		if (parentStageToUse == null)
			throw new RuntimeException("Cannot display modal file chooser without a parent window");

		parentStage = parentStageToUse;
		fileChooser = new FileChooser();
	}
	
	public File showSaveDialog()
	{
		return getFileChooser().showSaveDialog(getParentWindow());
	}
	
	public File showOpenDialog()
	{
		return getFileChooser().showOpenDialog(getParentWindow());
	}

	public void setTitle(String windowTitle)
	{
		getFileChooser().setTitle(windowTitle);
	}

	public void setInitialDirectory(File initialDir)
	{
		getFileChooser().setInitialDirectory(initialDir);
	}

	public void setInitialFileName(String name)
	{
		getFileChooser().setInitialFileName(name);
	}

	public ObservableList<ExtensionFilter> getExtensionFilters()
	{
		return getFileChooser().getExtensionFilters();
	}
	
	private FileChooser getFileChooser()
	{
		return fileChooser;
	}

	private Window getParentWindow()
	{
		return parentStage.getActualStage();
	}

	public ExtensionFilter getSelectedExtensionFilter()
	{
		return getFileChooser().getSelectedExtensionFilter();
	}
	
	private PureFxStage parentStage;
	private FileChooser fileChooser;
}
