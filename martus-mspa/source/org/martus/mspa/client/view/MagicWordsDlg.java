/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2004-2007, Beneficent
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


package org.martus.mspa.client.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.martus.common.MagicWords;
import org.martus.mspa.main.UiMainWindow;
import org.martus.swing.MartusParagraphLayout;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.Utilities;


public class MagicWordsDlg extends JDialog
{
	public MagicWordsDlg(UiMainWindow owner, Vector magicWords)
	{
		super(owner, "Magic Words", true);		
		parent = owner;				
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());		
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
	
		JPanel centerPanel = viewMagicPanel(magicWords);
		JPanel northPanel = buildMagicWordPanel();
								
		mainPanel.add(northPanel, BorderLayout.NORTH);
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		mainPanel.add(buildButtonsPanel(), BorderLayout.SOUTH);						

		getContentPane().add(mainPanel);
		Utilities.centerDlg(this);
		setResizable(true);
		
		resetFields();		
	}	
	
	private JPanel buildMagicWordPanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Magic Word and Group"));
		panel.setLayout(new MartusParagraphLayout());
				
		addMagicWordsField = new JTextField(20);
		addMagicWordsField.requestFocus();	
		
		groupComboField = new GroupComboBox(fData.getGroupList());		
		groupComboField.setEditable(true);	
	
		addMagicWordButton = new UiButton("Add");
		addMagicWordButton.addActionListener(new MagicWordButtonHandler());	
		updateMagicWordButton = new UiButton("Update");
		updateMagicWordButton.addActionListener(new MagicWordButtonHandler());						
				
		panel.add(new UiLabel(""), MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(new UiLabel("Magic word:"));		
		panel.add(addMagicWordsField);
		
		panel.add(Box.createHorizontalGlue());
		panel.add(new UiLabel("Assign to which group:"));
		panel.add(groupComboField);				
								
		panel.add(new UiLabel(""), MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(new UiLabel(""));		
		panel.add(addMagicWordButton);	
		panel.add(updateMagicWordButton);
			
		return panel;
	}

	private JPanel viewMagicPanel(Vector items)
	{
		JPanel panel = new JPanel();
			
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.setPreferredSize(new Dimension(550,200));	
		
		fData = new MagicWordTableData(items);		
		fTable = new JTable();
		fTable.setAutoCreateColumnsFromModel(false);
		fTable.setModel(fData); 
		fTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);			
		fTable.getSelectionModel().addListSelectionListener(new SelectionListener(fTable));    		

		for (int k = 0; k < MagicWordColumnInfo.m_columns.length; k++) 
		{		
			TableCellRenderer renderer;
			
			if (k==MagicWordColumnInfo.COL_STATUS)
				renderer = new CheckCellRenderer("active");
			else 
			{
				DefaultTableCellRenderer otherRenderer = new DefaultTableCellRenderer();			
				otherRenderer.setHorizontalAlignment(MagicWordColumnInfo.m_columns[k].fAlignment);
				renderer = otherRenderer;
			}
			
			TableCellEditor editor;

			if (k==MagicWordColumnInfo.COL_STATUS)
				editor = new DefaultCellEditor(new JCheckBox());
			else
				editor = new DefaultCellEditor(new JTextField());

			TableColumn column = new TableColumn(k, MagicWordColumnInfo.m_columns[k].fWidth, renderer, editor);
			fTable.addColumn(column);   
   		}

		JTableHeader header = fTable.getTableHeader();
		header.setUpdateTableInRealTime(false);

		JScrollPane ps = new JScrollPane();
		ps.setSize(500, 150);
		ps.getViewport().add(fTable);
							
		panel.add(ps, BorderLayout.CENTER);

		return panel;
	}
	
	private JPanel buildButtonsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());		
		
		saveButton = new UiButton("Save");	
		saveButton.addActionListener(new CommitButtonHandler());	
		cancelButton = new UiButton("Cancel");
		cancelButton.addActionListener(new CommitButtonHandler());

		panel.add(saveButton);
		panel.add(cancelButton);

		return panel;
	}		
	
	void resetFields()
	{
		addMagicWordsField.setText("");
		groupComboField.setSelectedItem(null);		
	}
	
	private boolean isValidMagicWord(String word)
	{	
		return fData.containMagicWord(word);
	}

	boolean containGroupName(String groupName)
	{
		return fData.getGroupList().contains(groupName);
	}
	
	boolean verifyMagicWord(String word)
	{
		if (word.startsWith(MagicWords.INACTIVE_SIGN))
		{			
			JOptionPane.showMessageDialog(parent, MagicWords.INACTIVE_SIGN+" denotes an inactive magic words", "Invalid character", JOptionPane.ERROR_MESSAGE);				
			resetFields();					
			return false;
		}
			
		if (isValidMagicWord(word))
		{
			JOptionPane.showMessageDialog(parent, "The magic word already exists. Please try other magic word ..", "Duplicate Magic word", JOptionPane.ERROR_MESSAGE);				
			resetFields();					
			return false;
		}	
		return true;
	}		
	
	
	public static int displayOptionsDialog(Component parent, String message, String title, Object[] options)
	{
		JOptionPane pane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE, 
					JOptionPane.DEFAULT_OPTION, null, options);	
		JDialog dialog = pane.createDialog(parent, title);
		dialog.setVisible(true);
		Object obj = pane.getValue();
		int result = -1;
		for (int k = 0; k < options.length; k++)
		{	
			if (options[k].equals(obj))
				result = k;
		}
		
		return result;    
	}
	
	class SelectionListener implements ListSelectionListener 
	{		
		SelectionListener(JTable table) 
		{
			this.table = table;
		}
		public void valueChanged(ListSelectionEvent e) 
		{			
			if (e.getSource() == table.getSelectionModel()&& table.getRowSelectionAllowed()) 
			{	
				int row = table.getSelectedRow();
				String word = (String) fData.getValueAt(row, MagicWordColumnInfo.COL_WORD);
				String group = (String) fData.getValueAt(row, MagicWordColumnInfo.COL_GROUPNAME);
				addMagicWordsField.setText(word);
				groupComboField.setSelectedItem(group);
			}
		}
		JTable table;		
	}
	
	class MagicWordButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			
			if (ae.getSource().equals(addMagicWordButton))
				handleAddNewMagicWord();			
			else if (ae.getSource().equals(updateMagicWordButton))
				handleUpdateMagicWords();	
		}
		
		private void handleUpdateMagicWords()
		{
			String word = addMagicWordsField.getText();
			String assignedGroup = (String) groupComboField.getSelectedItem();							
			int row = fTable.getSelectedRow();
			
			if (row < 0)
			{
				JOptionPane.showMessageDialog(parent, "There is no magic word selected from list.", "Update Magic Word", JOptionPane.INFORMATION_MESSAGE);
				return;
			}				

			if (!fData.update(row, word, assignedGroup))
			{
				JOptionPane.showMessageDialog(parent, "Modify the existing magic word is not allow at this time.", "Update Magic Word", JOptionPane.WARNING_MESSAGE);
			}	
			
			fTable.tableChanged(new TableModelEvent(
			  fData, row, row, TableModelEvent.ALL_COLUMNS,TableModelEvent.UPDATE)); 
			fTable.repaint();

			if (!containGroupName(assignedGroup))
				groupComboField.add(assignedGroup);
														
			resetFields();									
		}		
		
		private void handleAddNewMagicWord()
		{						
			String word = addMagicWordsField.getText();
			String assignedGroup = (String) groupComboField.getSelectedItem();		

			if (!verifyMagicWord(word))
				return;

			if (assignedGroup == null || assignedGroup.length()<=1)
				assignedGroup = word;				

			int row = fTable.getRowCount();
			fData.insert(row+1, word, assignedGroup);
			fTable.tableChanged(new TableModelEvent(
			  fData, row+1, row+1, TableModelEvent.ALL_COLUMNS,TableModelEvent.INSERT)); 
			fTable.repaint();

			if (!containGroupName(assignedGroup))
				groupComboField.add(assignedGroup);
									
			resetFields();	
		}		

	}
	
	class CommitButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource().equals(cancelButton))				
				dispose();
			else if (ae.getSource().equals(saveButton))
				handleUpdateMagicWords();
			
		}

		private void handleUpdateMagicWords()
		{			
			try
			{
				parent.getMSPAApp().updateMagicWords(fData.populatedDataFromTable());
				parent.setStatusText("Update Magicwords: "+ parent.getMSPAApp().getStatus());
				parent.remindNeedsRestart();
			} 
			catch (Exception e)
			{
				parent.exceptionDialog(e);
			}	
			dispose();			
		}
	}
		
	
	JButton saveButton;
	JButton cancelButton;
	JButton addMagicWordButton;
	JButton updateMagicWordButton;
	JTable fTable;
	MagicWordTableData fData;

	JTextField addMagicWordsField;
	GroupComboBox groupComboField;

	UiMainWindow parent;
}

