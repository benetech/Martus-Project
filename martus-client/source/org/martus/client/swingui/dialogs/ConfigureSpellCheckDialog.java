/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2012, Beneficent
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
package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JDialog;

import org.martus.client.search.SaneCollator;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.spellcheck.SpellCheckerManager;
import org.martus.common.EnglishCommonStrings;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTextArea;
import org.martus.swing.Utilities;

public class ConfigureSpellCheckDialog extends JDialog
{
	public ConfigureSpellCheckDialog(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		
		setTitle(getLocalization().getWindowTitle("ConfigureSpellCheck"));
		setModal(true);
		
		getContentPane().setLayout(new BorderLayout());
		
		String instructions = getLocalization().getFieldLabel("SpellCheckUserDictionaryInstructions");
		String htmlInstructions = "<html>" + instructions.replaceAll("\\n", "<br/>");
		getContentPane().add(new UiLabel(htmlInstructions), BorderLayout.BEFORE_FIRST_LINE);

		dictionaryEditor = new UiTextArea(20, 40);
		dictionaryEditor.setText(getUserDictionaryWords());
		getContentPane().add(new UiScrollPane(dictionaryEditor), BorderLayout.CENTER);
		
		UiButton ok = new UiButton(getLocalization().getButtonLabel(EnglishCommonStrings.OK));
		ok.addActionListener(new OkHandler());

		UiButton cancel = new UiButton(getLocalization().getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(new CancelHandler());
		Box buttonBox = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(buttonBox, new Component[] {Box.createHorizontalGlue(), ok, cancel});
		
		getContentPane().add(buttonBox, BorderLayout.AFTER_LAST_LINE);
	}
	
	private String getUserDictionaryWords()
	{
		Vector<String> wordList = SpellCheckerManager.getUserDictionaryWords();
		Collections.sort(wordList, new SaneCollator(getLocalization().getCurrentLanguageCode()));
		
		StringBuffer words = new StringBuffer();
		for (String word : wordList)
		{
			words.append(word);
			words.append("\n");
		}
		
		return words.toString();
	}

	public MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}
	
	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			doOk();
		}

	}
	
	protected void doOk()
	{
		SpellCheckerManager.setUserWords(dictionaryEditor.getText());
		dispose();
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			dispose();
		}

	}

	private UiMainWindow mainWindow;
	private UiTextArea dictionaryEditor;
}
