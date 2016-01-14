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

import java.util.Vector;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

import org.martus.client.core.FxBulletin;
import org.martus.client.core.FxBulletinField;
import org.martus.client.swingui.MartusLocalization;
import org.martus.common.fieldspec.FieldSpec;

abstract public class FxFormCreator
{
	public FxFormCreator(MartusLocalization localizationToUse)
	{
		localization = localizationToUse;
	}
	
	public Node createFormFromBulletin(FxBulletin bulletinToShow, Node attachments)
	{
		return createFormFromBulletin(bulletinToShow, attachments, null, null);
	}
	
	public Node createFormFromBulletin(FxBulletin bulletinToShow, Node attachments, Node details, Node contacts)
	{
		bulletin = bulletinToShow;
		sections = new Vector<BulletinEditorSection>();
		
		Vector<FieldSpec> fieldSpecs = bulletin.getFieldSpecs();
		fieldSpecs.forEach(fieldSpec -> addField(bulletin.getField(fieldSpec)));

		Accordion accordion = new Accordion();
		ObservableList<TitledPane> panes = accordion.getPanes();
		sections.forEach(section -> panes.add(createTitledPane(section)));
		panes.add(createTitledPane(attachments, "Attachments"));
		if(contacts != null)
			panes.add(createTitledPane(contacts, "Contacts"));
		if(details != null)
			panes.add(createTitledPane(details, "Details"));
		
		if(!bulletin.isValidBulletin())
			return warningBulletinForm(accordion, "MayBeDamaged");
		if(bulletin.notAuthorizedToRead())
			return warningBulletinForm(accordion, "NotAuthorizedToViewPrivate");

		TitledPane firstPane = panes.get(0);
	        accordion.setExpandedPane(firstPane);
	 
		return accordion;
	}

	private Node warningBulletinForm(Node accordion, String warningTag)
	{
		VBox vBox = new VBox();
		Label warningMessage = new Label(localization.getFieldLabel(warningTag));
		warningMessage.getStyleClass().add("bulletin-warning-message"); 
		vBox.getChildren().add(warningMessage);
		vBox.getChildren().add(accordion);
		return vBox;
	}

	private TitledPane createTitledPane(Node attachments, String titleTag)
	{
		String title = getLocalization().getWindowTitle(titleTag);
		return new TitledPane(title, attachments);
	}
	private TitledPane createTitledPane(BulletinEditorSection section)
	{
		String title = section.getTitle();
		return new TitledPane(title, section);
	}

	private void addField(FxBulletinField field)
	{
		if(shouldOmitField(field))
			return;

		boolean isSectionStart = field.isSectionStart();
		
		if(isSectionStart || currentSection == null)
		{
			if(currentSection != null)
				currentSection.endCurrentRow();
			
			String sectionTitle = "";
			if(isSectionStart)
				sectionTitle = field.getLabel();
			currentSection = new BulletinEditorSection(fieldCreator, bulletin, getLocalization(), sectionTitle);
			sections.add(currentSection);
		}

		if(isSectionStart)
			return;
		
		try
		{
			currentSection.addField(field);
		}
		catch(Exception e)
		{
			currentSection.addUnexpectedErrorMessage(e, field.getLabel());
		}
	}

	abstract protected boolean shouldOmitField(FxBulletinField field);

	protected MartusLocalization getLocalization()
	{
		return localization;
	}

	private MartusLocalization localization;
	protected FxFieldCreator fieldCreator;
	private FxBulletin bulletin;
	private BulletinEditorSection currentSection;
	private Vector<BulletinEditorSection> sections;
}