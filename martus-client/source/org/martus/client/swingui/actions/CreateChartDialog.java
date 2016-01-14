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
package org.martus.client.swingui.actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.reports.ChartAnswers;
import org.martus.client.search.FieldChooserSpecBuilder;
import org.martus.client.search.SortFieldChooserSpecBuilder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiPopUpFieldChooserEditor;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.swing.UiButton;
import org.martus.swing.UiComboBox;
import org.martus.swing.UiLabel;
import org.martus.swing.UiTextField;
import org.martus.swing.Utilities;

import com.jhlabs.awt.GridLayoutPlus;

public class CreateChartDialog extends JDialog
{
	public CreateChartDialog(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		
		setTitle(getLocalization().getWindowTitle("CreateChart"));
		setModal(true);
		setIconImage(Utilities.getMartusIconImage());
		getContentPane().setLayout(new BorderLayout());
		String disclaimerText = getLocalization().getFieldLabel("ChartPrivateFieldsNotice");
		String htmlDisclaimerText = "<html><b>" + disclaimerText.replaceAll("\\n", "<br/>");
		UiLabel disclaimer = new UiLabel(htmlDisclaimerText);
		disclaimer.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		getContentPane().add(disclaimer, BorderLayout.BEFORE_FIRST_LINE);

		JPanel panel = new JPanel(new GridLayoutPlus(0, 2));
		Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
		Border marginBorder = BorderFactory.createEmptyBorder(8,8,8,8);
		Border mainPanelBorder = BorderFactory.createCompoundBorder(lineBorder, marginBorder);
		panel.setBorder(mainPanelBorder);
		
		chartTypeComponent = createChartTypeComponent();
		Component[] typeRow = new Component[] {createLabel("ChartType"), chartTypeComponent};
		Utilities.addComponentsRespectingOrientation(panel, typeRow);
		
		Component[] fieldRow = new Component[] {createLabel("ChartFieldToCount"), createFieldChooserButton()};
		Utilities.addComponentsRespectingOrientation(panel, fieldRow);
		
		subtitleComponent = new UiTextField(40);
		Component[] subtitleRow = new Component[] {createLabel("ChartSubtitle"), subtitleComponent};
		Utilities.addComponentsRespectingOrientation(panel, subtitleRow);
		getContentPane().add(panel, BorderLayout.CENTER);
		
		ok = new UiButton(getLocalization().getButtonLabel(EnglishCommonStrings.OK));
		ok.addActionListener(new OkHandler());
		ok.setEnabled(false);

		UiButton cancel = new UiButton(getLocalization().getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(new CancelHandler());
		Box buttonBox = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(buttonBox, new Component[] {Box.createHorizontalGlue(), ok, cancel});
		
		getContentPane().add(buttonBox, BorderLayout.AFTER_LAST_LINE);
		pack();
	}
	
	private UiComboBox createChartTypeComponent()
	{
		ChoiceItem[] choices = new ChoiceItem[] {
			createChartTypeChoiceItem(ChartAnswers.CHART_TYPE_BAR),
			createChartTypeChoiceItem(ChartAnswers.CHART_TYPE_3DBAR),
			createChartTypeChoiceItem(ChartAnswers.CHART_TYPE_PIE),
			createChartTypeChoiceItem(ChartAnswers.CHART_TYPE_LINE),
		};
		chartTypeComponent = new UiComboBox(choices);
		chartTypeComponent.addActionListener(new ChartChooserActionHandler());
		return chartTypeComponent;
	}

	private ChoiceItem createChartTypeChoiceItem(String chartType)
	{
		return new ChoiceItem(chartType, getChartTypeLabel(chartType));
	}

	private String getChartTypeLabel(String chartType)
	{
		return getLocalization().getFieldLabel("ChartType" + chartType);
	}

	public boolean getResult()
	{
		return result;
	}

	private Component createFieldChooserButton()
	{
		chooser = new UiPopUpFieldChooserEditor(getMainWindow());
		FieldChooserSpecBuilder specBuilder = new SortFieldChooserSpecBuilder(getLocalization());
		PopUpTreeFieldSpec treeSpec = specBuilder.createSpec(getStore());
		chooser.setSpec(treeSpec);
		clearField();
		
		chooser.addActionListener(new FieldChooserActionHandler());

		return chooser.getComponent();
	}
	
	class FieldChooserActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			boolean isFieldSelected = isFieldSelected();
			setOkEnabled(isFieldSelected);
		}
	}
	
	
	class ChartChooserActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			boolean isLineChartSelected = isLineChartSelected();
			if(isLineChartSelected)
			{
		        selectField(Bulletin.TAGENTRYDATE);			
				setOkEnabled(true);
				enableFieldChooser(false);
			}
			else
			{
				clearField();
				setOkEnabled(isFieldSelected());
				enableFieldChooser(true);
			}			
		}
	}

	protected void enableFieldChooser(boolean enabled)
	{
		chooser.enableButton(enabled);
	}
	
	protected void clearField()
	{
		chooser.setText("");
	}
	
	protected void selectField(String fieldTagToSelect)
	{
		FieldSpec entryDateSpec = StandardFieldSpecs.findStandardFieldSpec(fieldTagToSelect);
		SearchableFieldChoiceItem choiceItem = new SearchableFieldChoiceItem(entryDateSpec);
		chooser.setText(choiceItem.getCode());
	}
	
	public boolean isFieldSelected()
	{
		return chooser.getText().length() > 0;
	}

	public void setOkEnabled(boolean isFieldSelected)
	{
		ok.setEnabled(isFieldSelected);
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
		result = true;
		dispose();
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			doCancel();
		}

	}

	protected void doCancel()
	{
		result = false;
		dispose();
	}
	
	private ClientBulletinStore getStore()
	{
		return getMainWindow().getApp().getStore();
	}

	public ChartAnswers getAnswers()
	{
		MiniFieldSpec fieldToCount = getCurrentSelectedFieldToCount();
		ChartAnswers answers = new ChartAnswers(fieldToCount, getLocalization());
		answers.setChartType(getChartTypeCode());
		answers.setSubtitle(subtitleComponent.getText());
		
		return answers;
	}

	private MiniFieldSpec getCurrentSelectedFieldToCount()
	{
		return chooser.getSelectedMiniFieldSpec();
	}
	
	private String getChartTypeCode()
	{
		ChoiceItem selected = (ChoiceItem) chartTypeComponent.getSelectedItem();
		return selected.getCode();
	}
	
	protected boolean isLineChartSelected()
	{
		String code = getChartTypeCode();
		return(code.equals("Line"));
	}

	private Component createLabel(String fieldName)
	{
		return new UiLabel(getLabel(fieldName));
	}

	private String getLabel(String fieldName)
	{
		return getLocalization().getFieldLabel(fieldName);
	}

	public MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}

	private UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	private UiMainWindow mainWindow;
	private UiComboBox chartTypeComponent;
	private UiPopUpFieldChooserEditor chooser;
	private JTextComponent subtitleComponent;
	private UiButton ok;
	
	private boolean result;
}
