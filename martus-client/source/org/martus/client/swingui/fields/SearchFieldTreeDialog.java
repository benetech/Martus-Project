/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2011, Beneficent
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
package org.martus.client.swingui.fields;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;

import org.martus.client.search.FieldValuesLoader;
import org.martus.client.search.SaneCollator;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiProgressWithCancelDlg;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.swing.UiCheckBox;
import org.martus.swing.UiTextArea;
import org.martus.swing.Utilities;
import org.martus.util.TokenReplacement;

public class SearchFieldTreeDialog extends FieldTreeDialog
{
	public SearchFieldTreeDialog(UiMainWindow mainWindowToUse, JDialog owner, Point location, PopUpTreeFieldSpec specToUse)
	{
		super(owner, location, specToUse, mainWindowToUse.getLocalization());
		mainWindow = mainWindowToUse;
		foundValues = new Vector();
	}
	
	protected boolean canSaveAndExit(FieldSpec selectedSpec)
	{
		foundValues = new Vector();
		if(!super.canSaveAndExit(selectedSpec))
			return false;
		
		if(selectedSpec == null)
			return true;
		
		if(!loadValuesCheckBox.isSelected())
			return true;

		if(selectedSpec.getType().isDropdown())
		{
			DropDownFieldSpec ddSpec = (DropDownFieldSpec)selectedSpec;
			if(!ddSpec.hasDataSource())
				return true;
		}

		try
		{
			foundValues = loadFieldValuesWithProgressDialog(mainWindow, selectedSpec);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public Vector getFoundValues()
	{
		return foundValues;
	}
	
	public void valueChanged(TreeSelectionEvent e)
	{
		super.valueChanged(e);
		FieldSpec selectedSpec = getSelectedSpec();
		if(selectedSpec == null)
			return;
		
		boolean isDropDown = selectedSpec.getType().isDropdown();
		boolean canLoad = canUseMemorizedPossibleValues(selectedSpec);
		boolean mustLoadValues = canLoad && isDropDown;
		loadValuesCheckBox.setSelected(mustLoadValues);
		loadValuesCheckBox.setEnabled(canLoad && !mustLoadValues);
	}
	
	protected Component[] getButtonBoxComponents(MiniLocalization localization)
	{
		if(loadValuesCheckBox == null)
			loadValuesCheckBox = new UiCheckBox(localization.getButtonLabel("LoadFieldValuesFromAllBulletins"));
		Vector components = new Vector(Arrays.asList(super.getButtonBoxComponents(localization)));
		components.insertElementAt(loadValuesCheckBox, 0);
		return (Component[]) components.toArray(new Component[0]);
	}
	
	public static boolean canUseMemorizedPossibleValues(FieldSpec selectedFieldSpec)
	{
		if(selectedFieldSpec.getType().isString())
			return true;
		
		if(selectedFieldSpec.getType().isDropdown())
		{
			DropDownFieldSpec spec = (DropDownFieldSpec) selectedFieldSpec;
			if(spec.getDataSourceGridTag() != null)
				return true;
		}
		return false;
	}
	
	public static Vector loadFieldValuesWithProgressDialog(UiMainWindow mainWindow, FieldSpec spec) throws Exception
	{
		UiProgressWithCancelDlg progressDlg = new LoadValuesProgressDlg(mainWindow, spec.getLabel());
		LoadValuesThread thread = new LoadValuesThread(mainWindow, progressDlg, spec);
		thread.start();
		progressDlg.setVisible(true);
		// NOTE: by the time we get here, the thread has terminated
		if(thread.errorOccured)
			throw new RuntimeException(thread.exception);
		HashSet loadedValues = thread.getLoadedValues();
		if(loadedValues == null)
			return new Vector();
		Vector sortedValues = new Vector(loadedValues);
		Collections.sort(sortedValues, new SaneCollator(mainWindow.getLocalization().getCurrentLanguageCode()));
		boolean needToInsertBlank = true;
		if(sortedValues.size() > 0)
		{
			ChoiceItem firstChoice = (ChoiceItem) sortedValues.get(0);
			String code = firstChoice.getCode();
			// NOTE: I saw "" as an empty code that otherwise seems to work
			final String UNKNOWN_ALTERNATIVE_EMPTY_CODE = "\"\"";
			boolean isCodeEmpty = code.equals("");
			boolean isCodeAlternateEmpty = code.equals(UNKNOWN_ALTERNATIVE_EMPTY_CODE);
			if(isCodeEmpty || isCodeAlternateEmpty)
				needToInsertBlank = false;
			else
				needToInsertBlank = true;
		}
		if(needToInsertBlank)
		{
			ChoiceItem blank = new ChoiceItem("", "");
			sortedValues.insertElementAt(blank, 0);
		}
		return sortedValues;
	}		
	
	static class LoadValuesProgressDlg extends UiProgressWithCancelDlg
	{
		public LoadValuesProgressDlg(UiMainWindow mainWindowToUse, String fieldName) throws Exception
		{
			super(mainWindowToUse, "LoadingFieldValuesFromAllBulletins");

			getContentPane().setLayout(new BorderLayout());
			UiTextArea explanation = new UiTextArea(4, 50);
			explanation.setEditable(false);
			MartusLocalization localization = mainWindowToUse.getLocalization();
			String template = localization.getFieldLabel("LoadingFieldValuesFromAllBulletinsExplanation");
			String fullDisplayString = TokenReplacement.replaceToken(template, "#FieldName#", fieldName);
			explanation.setText(fullDisplayString);
			
			JPanel cancelPanel = new JPanel();
			cancelPanel.add(cancel);
			
			JPanel meterPanel = new JPanel();
			meterPanel.add(progressMeter);
			
			getContentPane().add(explanation, BorderLayout.NORTH);
			getContentPane().add(meterPanel, BorderLayout.CENTER);
			getContentPane().add(cancelPanel, BorderLayout.SOUTH);
			Utilities.packAndCenterWindow(this);
		}
		
	}
	
	static class LoadValuesThread extends Thread
	{
		public LoadValuesThread(UiMainWindow mainWindowToUse, UiProgressWithCancelDlg progressRetrieveDlgToUse, FieldSpec specToUse)
		{
			mainWindow = mainWindowToUse;
			progressMeter = progressRetrieveDlgToUse;
			spec = specToUse;
		}

		public void run()
		{
			try
			{
				doWork();
			}
			catch (Exception e)
			{
				errorOccured = true;
				exception = e;
			}
			finally
			{
				progressMeter.finished();
			}
		}

		private synchronized void doWork()
		{
			FieldValuesLoader loader = new FieldValuesLoader(mainWindow.getApp());
			loadedValues = loader.loadFieldValuesFromAllBulletinRevisions(progressMeter, spec);
		}
		
		public synchronized HashSet getLoadedValues()
		{
			return loadedValues;
		}

		private UiMainWindow mainWindow;
		private UiProgressWithCancelDlg progressMeter;
		boolean errorOccured;
		Exception exception;
		private FieldSpec spec;
		private HashSet loadedValues;
	}



	private UiMainWindow mainWindow;
	private Vector foundValues;
	private UiCheckBox loadValuesCheckBox;
}
