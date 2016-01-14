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

package org.martus.client.swingui.bulletincomponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiWarningLabel;
import org.martus.swing.UiLabel;
import org.martus.swing.Utilities;
import org.martus.util.language.LanguageOptions;

import com.jhlabs.awt.Alignment;
import com.jhlabs.awt.GridLayoutPlus;

abstract public class UiBulletinComponentSection extends JPanel
{
	UiBulletinComponentSection(UiMainWindow mainWindowToUse, String groupTag)
	{
		GridLayoutPlus layout = new GridLayoutPlus(0, 1);
		layout.setFill(Alignment.FILL_NONE);
		if(LanguageOptions.isRightToLeftLanguage())
			layout.setAlignment(Alignment.EAST);
		setLayout(layout);
		mainWindow = mainWindowToUse;
		groups = new Vector();
		fontHelper = new UiFontEncodingHelper(mainWindow.getDoZawgyiConversion());
		
		setBorder(new EtchedBorder());
		
		sectionHeading = new UiLabel("", null, JLabel.LEFT);
		sectionHeading.setVerticalTextPosition(JLabel.TOP);
		sectionHeading.setFont(sectionHeading.getFont().deriveFont(Font.BOLD));
		
		warningIndicator = new UiWarningLabel();
		clearWarningIndicator();
		
		Box box = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(box, new Component[] {
				sectionHeading, 
				Box.createHorizontalStrut(20),
				warningIndicator,
				Box.createHorizontalGlue(),
				});
		add(box);

		groupTag = "_Section" + groupTag;
		startNewGroup(groupTag, getLocalization().getFieldLabel(groupTag));
	}
	
	public void addComponents(JComponent labelComponent, JComponent fieldComponent)
	{
		currentGroup.addComponents(labelComponent, fieldComponent);
	}

	public void startNewGroup(String tag, String title)
	{
		if(currentGroup != null && currentGroup.isEmpty())
		{
			groups.remove(currentGroup);
			remove(currentGroup);
		}

		currentGroup = new FieldGroup(tag, title);
		add(currentGroup);
		groups.add(currentGroup);
	}
	
	class FieldGroup extends JPanel
	{
		public FieldGroup(String tag, String title)
		{
			GridLayoutPlus layout = new GridLayoutPlus(0, 2, 5, 5, 5, 5);
			layout.setFill(Alignment.FILL_NONE);
			layout.setColAlignment(0, Alignment.EAST);
			layout.setColAlignment(1, Alignment.WEST);
			setLayout(layout);
				
			Border empty = BorderFactory.createEmptyBorder(1, 2, 1, 2);
			Border line = BorderFactory.createLineBorder(Color.BLACK, 2);
			setBorder(BorderFactory.createCompoundBorder(empty, line));

			GridLayoutPlus contentsLayout = new GridLayoutPlus(0, 2, 5, 5, 5, 5);
			contentsLayout.setFill(Alignment.FILL_NONE);
			contentsLayout.setColAlignment(0, Alignment.NORTHEAST);
			contentsLayout.setColAlignment(1, Alignment.NORTHWEST);
			contents = new JPanel(contentsLayout);
			MartusApp app = getMainWindow().getApp();
			MartusLocalization localization = getMainWindow().getLocalization();
			FieldHolder fieldHolder = new FieldHolder(localization);
			fieldHolder.addField(contents);

			JComponent[] firstRow = new JComponent[] {new HiderButton(app, "_Section" + tag, fieldHolder), new UiLabel(title)};
			JComponent[] secondRow = new JComponent[] {new UiLabel(""), fieldHolder};
			Utilities.addComponentsRespectingOrientation(this, firstRow);
			Utilities.addComponentsRespectingOrientation(this, secondRow);
			
			// NOTE: I'm pretty sure the following is NOT needed any more, 
			// but am not comfortable removing it 2 days before release
			avoidArabicAlignmentProblem();
			hasRealComponents = false;
		}

		private void avoidArabicAlignmentProblem()
		{
			// NOTE: Without this, in a RtoL language, if the first row of any section
			// has a narrow field (not a string), it will be left-aligned instead of 
			// right aligned like the rest of the fields in the section.
			// After spending 8 hours on it, this was the only quick hack that worked.
			if(LanguageOptions.isRightToLeftLanguage())
				addComponents(new JLabel(""), new JLabel(""));
		}
		
		public void addComponents(JComponent left, JComponent right)
		{
			Utilities.addComponentsRespectingOrientation(contents, new Component[] {left, right});
			hasRealComponents = true;
		}
		
		public boolean isEmpty()
		{
			return (!hasRealComponents);
		}
		
		private JPanel contents;
		boolean hasRealComponents;
	}

	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}
	
	public MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}

	void updateSectionBorder(boolean isEncrypted)
	{
		Color color = Color.lightGray;
		if(isEncrypted)
			color = Color.red;
		setBorder(new LineBorder(color, 5));
	}

	public void clearWarningIndicator()
	{
		warningIndicator.setVisible(false);
	}

	public void setWarningText(String text)
	{
		warningIndicator.setWarningText(text);
		warningIndicator.setVisible(true);
	}

	public void setInformationalText(String text)
	{
		warningIndicator.setInformationalText(text);
		warningIndicator.setVisible(true);
	}

	void matchFirstColumnWidth(UiBulletinComponentSection otherSection)
	{
// FIXME: Not clear if this is necessary or helpful...either delete it or make it work again
//		int thisWidth = currentGroup.getFirstColumnMaxWidth(this);
//		int otherWidth = otherSection.currentGroup.getFirstColumnMaxWidth(otherSection);
//		if(otherWidth > thisWidth)
//			currentGroup.setFirstColumnWidth(otherWidth);
	}

	protected void setSectionIconAndTitle(String iconFileName, String title)
	{
		Icon icon = new ImageIcon(UiBulletinComponentSection.class.getResource(iconFileName));
		sectionHeading.setIcon(icon);
		sectionHeading.setText(title);
	}

	protected UiMainWindow mainWindow;
	JLabel sectionHeading;
	UiWarningLabel warningIndicator;
	
	FieldGroup currentGroup;
	Vector groups;
	static UiFontEncodingHelper fontHelper;
}
