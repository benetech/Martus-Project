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
package org.martus.client.swingui.jfx.landing.bulletins;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;

public class FxCopyItemNewNameController extends FxController
{

	public FxCopyItemNewNameController(UiMainWindow mainWindowToUse, String originalBulletinsNameToUse)
	{
		super(mainWindowToUse);
		originalBulletinsName = originalBulletinsNameToUse;
	}

	public String getNewItemName()
	{
		return itemName.getText();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		itemName.setText(originalBulletinsName);
		initializeOkButtonBindings();		
	}
	
	private void initializeOkButtonBindings()
	{
		FxController topLevelController = getTopLevelController();
		Button okButton = topLevelController.getOkButton();
		BooleanBinding sameNameBinding = itemName.textProperty().isEqualTo(originalBulletinsName);
		BooleanBinding emptyBinding = itemName.textProperty().isEmpty();
		BooleanBinding emptyOrSameNameBinding = Bindings.or(sameNameBinding, emptyBinding);
		okButton.disableProperty().bind(emptyOrSameNameBinding);
	}
	
	@Override
	public Parent createContents() throws Exception
	{
		return super.createContents();
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/FxCopyItemNewName.fxml";
	}
	
	@FXML
	TextField itemName;

	private String originalBulletinsName;
}
