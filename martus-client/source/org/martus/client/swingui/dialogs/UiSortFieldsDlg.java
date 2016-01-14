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
package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JDialog;

import org.martus.client.search.SortFieldChooserSpecBuilder;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiPopUpFieldChooserEditor;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.swing.UiButton;
import org.martus.swing.UiComboBox;
import org.martus.swing.UiVBox;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;

public class UiSortFieldsDlg extends JDialog implements ActionListener
{
	public UiSortFieldsDlg(UiMainWindow mainWindow, MiniFieldSpec[] specsToAllow)
	{
		super(mainWindow.getSwingFrame());
		
		if(sortMiniSpecs == null)
			sortMiniSpecs = new Vector();
		
		setModal(true);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		UiLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle("ReportChooseSortFields"));

		String text = localization.getFieldLabel("ReportChooseSortFields");

		SortFieldChooserSpecBuilder builder = new SortFieldChooserSpecBuilder(localization);
		PopUpTreeFieldSpec spec = builder.createSpec(mainWindow.getStore(), specsToAllow);
		
		contentPane.add(new UiWrappedTextArea(text), BorderLayout.BEFORE_FIRST_LINE);

		UiVBox multiSortBox = new UiVBox();
		
		sortChooser = new UiPopUpFieldChooserEditor[MAX_SORT_LEVELS];
		for(int i = 0; i < sortChooser.length; ++i)
		{
			sortChooser[i] = createSortChooser(mainWindow, spec);
			multiSortBox.add(sortChooser[i].getComponent());
		}
		
		for(int i = 0; i < sortChooser.length; ++i)
		{
			MiniFieldSpec selectedSpec = null;
			String codeString = "";
			if(i < sortMiniSpecs.size())
			{
				selectedSpec = (MiniFieldSpec)sortMiniSpecs.get(i);
				codeString = selectedSpec.getCodeString();
			}
			sortChooser[i].select(codeString);
		}
		
		detailOnlyChoice = createChoiceItem("ReportDetailOnly", localization);
		detailAndBreaksChoice = createChoiceItem("ReportDetailWithSummaries", localization);
		breaksOnlyChoice = createChoiceItem("ReportSummariesOnly", localization);
		ChoiceItem[] breakChoices = {
			detailOnlyChoice,
			detailAndBreaksChoice,
			breaksOnlyChoice,
		};
		breakChoice = new UiComboBox(breakChoices);

		if(savedBreakChoice == null)
			breakChoice.setSelectedIndex(0);
		else
			breakChoice.setSelectedItem(savedBreakChoice);
			
		Box mainArea = Box.createVerticalBox();
		mainArea.add(multiSortBox);
		mainArea.add(breakChoice);
		
		contentPane.add(mainArea, BorderLayout.CENTER);
		okButton = new UiButton(localization.getButtonLabel(EnglishCommonStrings.OK));
		okButton.addActionListener(this);
		UiButton cancelButton = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancelButton.addActionListener(this);
		Box buttonBar = Box.createHorizontalBox();
		Component[] buttons = new Component[] {Box.createHorizontalGlue(), okButton, cancelButton};
		Utilities.addComponentsRespectingOrientation(buttonBar, buttons);
		contentPane.add(buttonBar, BorderLayout.AFTER_LAST_LINE);
		getRootPane().setDefaultButton(okButton);
		
		pack();
		Utilities.packAndCenterWindow(this);
	}
	
	private ChoiceItem createChoiceItem(String tag, MiniLocalization localization)
	{
		return new ChoiceItem(tag, localization.getFieldLabel(tag));
	}

	private UiPopUpFieldChooserEditor createSortChooser(UiMainWindow mainWindow, PopUpTreeFieldSpec spec)
	{
		UiPopUpFieldChooserEditor chooser = new UiPopUpFieldChooserEditor(mainWindow);
		chooser.setSpec(spec);
		return chooser;
	}
	
	void memorizeSortFields()
	{			
		sortMiniSpecs.clear();
		for(int i = 0; i < sortChooser.length; ++i)
		{
			MiniFieldSpec spec = sortChooser[i].getSelectedMiniFieldSpec();
			if(spec.getTag().length() == 0)
				break;
			sortMiniSpecs.add(spec);
		}
		savedBreakChoice = (ChoiceItem)breakChoice.getSelectedItem();
	}
	
	public MiniFieldSpec[] getSelectedMiniFieldSpecs() throws Exception
	{
		return (MiniFieldSpec[])sortMiniSpecs.toArray(new MiniFieldSpec[0]);
	}
	
	public boolean getPrintBreaks()
	{
		return !detailOnlyChoice.equals(breakChoice.getSelectedItem());
	}
	
	public boolean getHideDetail()
	{
		return breaksOnlyChoice.equals(breakChoice.getSelectedItem());
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(okButton))
		{
			memorizeSortFields();
			hitOk = true;
		}
		dispose();
	}
	
	public boolean ok()
	{
		return hitOk;
	}

	UiPopUpFieldChooserEditor[] sortChooser;
	boolean hitOk;
	UiButton okButton;
	UiComboBox breakChoice;
	ChoiceItem detailOnlyChoice;
	ChoiceItem detailAndBreaksChoice;
	ChoiceItem breaksOnlyChoice;

	private static Vector sortMiniSpecs;
	private static ChoiceItem savedBreakChoice;
	private final int MAX_SORT_LEVELS = 3;
}

