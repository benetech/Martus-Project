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
package org.martus.mspa.client.view;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.martus.common.LoggerToConsole;
import org.martus.common.MagicWordEntry;
import org.martus.common.MagicWords;


public class MagicWordTableData extends AbstractTableModel 
{
	public MagicWordTableData(Vector magicLines) 
	{	  		
		rowDataList = new Vector();
		groupList = new Vector();
		loadMagicWords(magicLines);
	}

	private void loadMagicWords(Vector items) 
	{
	  rowDataList.removeAllElements();
	  MagicWords magicWordsInfo = new MagicWords(new LoggerToConsole());
	
	  for (int i=0; i<items.size();++i)
	  {			
		  String lineOfEntry = (String) items.get(i);
		  MagicWordEntry entry = magicWordsInfo.add(lineOfEntry);
		  String groupName = entry.getGroupName();		
		
		  rowDataList.addElement(new MagicWordData(entry.getCreationDate(), entry.isActive(), entry.getMagicWord(),groupName));
		  		  
		  if (groupName != null && !isValidGroupName(groupName))			
		  	groupList.add(groupName);				
	  }		
	}

	public boolean containMagicWord(String word)
	{
		for (int i=0; i<rowDataList.size();++i)
		{
			MagicWordData data = (MagicWordData)rowDataList.get(i);
			if (data.fWord.equals(word))
				return true;
		}

		return false;
	}

	public Vector populatedDataFromTable()
	{
		MagicWords magicWordsInfo = new MagicWords(new LoggerToConsole());
		for (int row=0; row < getRowCount(); row++)
		{					
			magicWordsInfo.add(dataMapping(row));
		}
		return magicWordsInfo.getAllMagicWords();				
	}

	private MagicWordEntry dataMapping(int row)
	{
		MagicWordData data = (MagicWordData)rowDataList.elementAt(row);

		MagicWordEntry entry = new MagicWordEntry(data.fWord, data.fGroup);
		entry.setActive(data.fActived.booleanValue());
		entry.setCreationDate(data.fDate);
	
		return entry;
	}
	
	private boolean isValidGroupName(String groupName)
	{
		return groupList.contains(groupName);
	}
	
	public Vector getGroupList()
	{
		return groupList;
	}

	public static DateFormat getStoredDateFormat()
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		df.setLenient(false);
		return df;
	}

	public static String getToday()
	{
		DateFormat df = getStoredDateFormat();
		return df.format(new Date());
	}
	
	private int statusConfirmation()
	{
		Object[] options = new String[] {"Yes", "No"};
		String msg = "You are about to inactive the magicword. \nAre you sure?";
		String title = "Inactive Magicword";
			
		return MagicWordsDlg.displayOptionsDialog((Component) null, msg, title, options); 		
	}

	public int getRowCount() 
	{
	  return rowDataList==null ? 0 : rowDataList.size(); 
	}

	public int getColumnCount() 
	{ 
	  return MagicWordColumnInfo.m_columns.length; 
	} 

	public String getColumnName(int column) 
	{ 
	  return MagicWordColumnInfo.m_columns[column].fTitle; 
	}
 
	public boolean isCellEditable(int nRow, int nCol) 
	{
		if (nCol == MagicWordColumnInfo.COL_STATUS)
			return true;

		return false;
	}

	public Object getValueAt(int nRow, int nCol) 
	{
		if (nRow < 0 || nRow>=getRowCount())
			return "";
		
		MagicWordData row = (MagicWordData)rowDataList.elementAt(nRow);
		switch (nCol) 
		{
			case MagicWordColumnInfo.COL_DATE		: return row.fDate;
			case MagicWordColumnInfo.COL_STATUS		: return row.fActived;
			case MagicWordColumnInfo.COL_WORD		: return row.fWord;
			case MagicWordColumnInfo.COL_GROUPNAME	: return row.fGroup;
		}
		return "";
	}
	
	public void setValueAt(Object value, int nRow, int nCol) 
	{
		if (nRow < 0 || nRow >= getRowCount())
		  return;
		  
		MagicWordData row = (MagicWordData)rowDataList.elementAt(nRow);
		String svalue = value.toString();

		switch (nCol) 
		{
		  case MagicWordColumnInfo.COL_DATE: 
			row.fDate = svalue; 
			break;
		  case MagicWordColumnInfo.COL_STATUS:			
			if (row.fActived.booleanValue())
			{
				if (statusConfirmation()!=0) 
					break;
			}		  		  			  	  					
			row.fActived = new Boolean(svalue);
			break;
		  case MagicWordColumnInfo.COL_WORD:
			row.fWord = svalue;
			break;
		  case MagicWordColumnInfo.COL_GROUPNAME:
			row.fGroup = svalue; 
			break;		
		}
	}

	public void insert(int row, String word, String group) 
  	{
		if (row < 0)
			row = 0;
	  
		if (row > rowDataList.size())
			row = rowDataList.size();
	
		if (containMagicWord(word))
			return;	

		if (isValidGroupName(group))
			groupList.add(group);

		rowDataList.insertElementAt(new MagicWordData(getToday(), false,word, group), row);
	}
	  
	public boolean update(int row, String word, String group)
	{
		boolean success=true;
		if (row < 0)
		  row = 0;
		  
		if (row > rowDataList.size())
		  row = rowDataList.size();		  
	
		MagicWordData selectedData = (MagicWordData) rowDataList.get(row);
		if (!selectedData.fWord.equals(word))
			success = false;

		if (group != null && group.length()>=1)
			selectedData.fGroup = group;
		
		rowDataList.setElementAt(selectedData, row);

		if (!isValidGroupName(group))
			groupList.add(group);	
			
		return success;							
	}

	public boolean delete(int row) 
	{
		if (row < 0 || row >= rowDataList.size())
		  return false;
		  
		rowDataList.remove(row);
		  return true;
	}	  
	
	class MagicWordData
	{
		String fDate;
		Boolean fActived;
		String fWord;
		String fGroup;
		
		public MagicWordData(String date, boolean actived, String word, String group)
		{
			fDate = date;
			fActived = new Boolean(actived);
			fWord = word;
			fGroup = group;
		}		
	}
	
	Vector rowDataList;
	Date   date;
	Vector groupList;
}


