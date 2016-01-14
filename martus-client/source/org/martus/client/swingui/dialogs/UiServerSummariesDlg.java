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

package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.tablemodels.RetrieveTableModel;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MiniLocalization;
import org.martus.common.packet.FieldDataPacket;
import org.martus.swing.MartusParagraphLayout;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiRadioButton;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;
import org.martus.swing.UiVBox;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.language.LanguageOptions;

public abstract class UiServerSummariesDlg extends JDialog
{
	public UiServerSummariesDlg(UiMainWindow owner, RetrieveTableModel tableModel, String windowTitleTag)
	{
		super(owner.getSwingFrame(), owner.getLocalization().getWindowTitle(windowTitleTag), true);
		mainWindow = owner;
		model = tableModel;
		displayBulletinVersionRadioButtons = true;
	}
	
	abstract public void initialize();

	void initialize(String topMessageTag, String okButtonTag)
	{
		MiniLocalization localization = mainWindow.getLocalization();

		disabledBackgroundColor = getBackground();

		String topMessageText = localization.getFieldLabel(topMessageTag);
		UiWrappedTextArea retrieveMessage = new UiWrappedTextArea(topMessageText);
		tableBox = new UiVBox();
		table = new RetrieveJTable(model);
		table.setRenderers(model);

		table.createDefaultColumnsFromModel();
		JTableHeader tableHeader = table.getTableHeader();
		tableHeader.addMouseListener(new HeaderMouseListener(table));
		tableBox.addCentered(tableHeader);
		tableBox.addCentered(new UiScrollPane(table));

		JPanel topPanel = new JPanel();
		topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		topPanel.setLayout(new BorderLayout());
		topPanel.add(retrieveMessage, BorderLayout.NORTH);
		topPanel.add(tableBox, BorderLayout.CENTER);
		topPanel.add(createActionsPanel(localization, okButtonTag), BorderLayout.SOUTH);

		getContentPane().add(topPanel);	
		setScreenSize();				
		Utilities.packAndCenterWindow(this);
		setVisible(true);
	}

	public void hideBulletinVersionButtons()
	{
		displayBulletinVersionRadioButtons = false;
	}

	private void setScreenSize()
	{
		Dimension dim = Utilities.getViewableScreenSize();
		double width = dim.getWidth()- (dim.getWidth()* 0.25);
		dim.setSize(width, getSize().getHeight());
		setSize(dim);
	}

	private JPanel createActionsPanel(MiniLocalization localization, String okButtonTag)
	{
		JButton ok = new UiButton(localization.getButtonLabel(okButtonTag));
		ok.addActionListener(new OkHandler());
		JButton cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(new CancelHandler());
		JButton preview = new UiButton(localization.getButtonLabel("Preview"));
		preview.addActionListener(new PreviewHandler());

		JButton checkAll = new UiButton(localization.getButtonLabel("checkall"));
		checkAll.addActionListener(new CheckAllHandler());
		JButton unCheckAll = new UiButton(localization.getButtonLabel("uncheckall"));
		unCheckAll.addActionListener(new UnCheckAllHandler());

  		JPanel panel = new JPanel();
		panel.setLayout(new MartusParagraphLayout());
		panel.add(new UiLabel(""), MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(createSummariesPanel(localization));
		panel.add(new UiLabel(""), MartusParagraphLayout.NEW_PARAGRAPH);
		Utilities.addComponentsRespectingOrientation(panel, new Component[]{checkAll, unCheckAll, preview});
		panel.add(new UiLabel(""), MartusParagraphLayout.NEW_PARAGRAPH);
		Utilities.addComponentsRespectingOrientation(panel, new Component[]{ok, cancel});

 		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		if(LanguageOptions.isRightToLeftLanguage())
			southPanel.add(panel, BorderLayout.EAST);
		else
			southPanel.add(panel, BorderLayout.WEST);
		
		getRootPane().setDefaultButton(ok);
		return southPanel;
	}

	private JPanel createSummariesPanel(MiniLocalization localization)
	{
		UiRadioButton downloadableSummaries = new UiRadioButton(localization.getButtonLabel("DownloadableSummaries"), true);
		downloadableSummaries.addActionListener(new ChangeDownloadableSummariesHandler());
		UiRadioButton allSummaries = new UiRadioButton(localization.getButtonLabel("AllSummaries"), false);
		allSummaries.addActionListener(new ChangeAllSummariesHandler());
		ButtonGroup summariesGroup = new ButtonGroup();
		summariesGroup.add(downloadableSummaries);
		summariesGroup.add(allSummaries);

		retrieveAllVersions = new UiRadioButton(localization.getButtonLabel("RetrieveAllVersions"));
		UiRadioButton retrieveLatestBulletinRevisionOnly = new UiRadioButton(localization.getButtonLabel("RetrieveLatestBulletinRevisionOnly"));		
		ButtonGroup bulletinVersionsGroup = new ButtonGroup();
		bulletinVersionsGroup.add(retrieveAllVersions);
		bulletinVersionsGroup.add(retrieveLatestBulletinRevisionOnly);
		retrieveAllVersions.setSelected(true);

		JPanel radioPanel = new JPanel();	
		GridLayout gridLayout = new GridLayout(0, 1);
		gridLayout.setVgap(3);
		radioPanel.setLayout(gridLayout);
		JPanel summaries = new JPanel();
		summaries.setLayout(gridLayout);
		summaries.add(downloadableSummaries);
		summaries.add(allSummaries);
		summaries.setBorder(new LineBorder(Color.BLACK));
		radioPanel.add(summaries);
		if(displayBulletinVersionRadioButtons)
		{
			JPanel versions = new JPanel();
			versions.setLayout(gridLayout);
			versions.add(retrieveAllVersions);
			versions.add(retrieveLatestBulletinRevisionOnly);
			versions.setBorder(new LineBorder(Color.BLACK));
			radioPanel.add(versions);
		}
		
		return radioPanel;
	}

	public boolean getResult()
	{
		return result;
	}

	public Vector getUniversalIdList()
	{
		if(retrieveAllVersions.isSelected())
			return model.getSelectedUidsFullHistory();
		return model.getSelectedUidsLatestVersion();
	}
	
	abstract String getNoneSelectedTag();
	
	boolean confirmIntentionsBeforeClosing()
	{
		Vector uidList = getUniversalIdList();
		if( uidList.size() == 0)
		{
			mainWindow.notifyDlg(getNoneSelectedTag());
			return false;
		}
		
		return true;
	}
	
	class RetrieveJTable extends UiTable
	{
		public RetrieveJTable(TableModel model)
		{
			super(model);
		}

		public void doLayout()
		{
			setMaxColumnWidthToHeaderWidth(0);
			int numberOfColumns = getColumnModel().getColumnCount();
			setMaxColumnWidthToHeaderWidth(numberOfColumns-1);
			super.doLayout();
		}

		public void sort(int column)
		{
			model.setCurrentSortColumn(column);
		}
	}


	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(!confirmIntentionsBeforeClosing())
				return;
			
			result = true;
			dispose();
		}
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}

	class PreviewHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{

			int[] row = table.getSelectedRows();
			if(row.length <= 0)
			{
				mainWindow.notifyDlg("PreviewNoBulletinsSelected");
			}
			else if(row.length==1)
			{
				FieldDataPacket fdp = model.getBulletinSummary(row[0]).getFieldDataPacket();
				if(fdp == null)
					mainWindow.notifyDlg("RetrievePreviewNotAvailableYet");
				else
					new UiBulletinPreviewDlg(mainWindow, fdp);
			}
			else
			{
				mainWindow.notifyDlg("PreviewOneBulletinOnly");
			}
		}
	}

	class CheckAllHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			model.setAllFlags(true);
		}
	}

	class UnCheckAllHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			model.setAllFlags(false);
		}
	}

	class ChangeDownloadableSummariesHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			model.changeToDownloadableSummaries();
			model.fireTableStructureChanged();
		}
	}

	class ChangeAllSummariesHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			model.changeToAllSummaries();
			model.fireTableStructureChanged();
		}
	}

	public class HeaderMouseListener extends MouseAdapter 
	{
		public HeaderMouseListener(RetrieveJTable tableToManage)
		{
			retrieveTable = tableToManage;
		}
		
		public void mouseClicked(MouseEvent e) 
		{
			JTableHeader header = (JTableHeader)e.getSource();
			int clickedColumn = header.columnAtPoint(e.getPoint());
			if (clickedColumn < 0)
				return;

			// NOTE: If we allow columns to be reordered, we must 
			// translate clickedColumn to the correct column ourselves
			retrieveTable.sort(clickedColumn);
		}
		
		RetrieveJTable retrieveTable;
	}

	private UiVBox tableBox;
	UiMainWindow mainWindow;
	boolean result;
	RetrieveJTable table;
	RetrieveTableModel model;
	Color disabledBackgroundColor;
	UiRadioButton retrieveAllVersions;
	boolean displayBulletinVersionRadioButtons;
}
