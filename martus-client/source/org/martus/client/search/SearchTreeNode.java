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

package org.martus.client.search;

import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;

public class SearchTreeNode
{
	public final static int VALUE = 0;
	public final static int OR = 1;
	public final static int AND = 2;
	
	private final static String GREATER_EQUAL_STRING = ">=";
	private final static String LESS_EQUAL_STRING = "<=";
	private final static String GREATER_STRING = ">";
	private final static String LESS_STRING = "<";
	private final static String EQUAL_STRING = "=";
	private final static String NOT_EQUAL_STRING = "!=";

	public SearchTreeNode(String justValueForTesting)
	{
		this(FieldSpec.createStandardField("", new FieldTypeNormal()), "", justValueForTesting);
	}
	
	public SearchTreeNode(FieldSpec fieldToSearch, String compareOperator, String value)
	{
		nodeOp = VALUE;
		field = fieldToSearch;
		compareOp = convertComparisonOpStringToValue(compareOperator);
		nodeValue = withoutQuotes(value);
	}
	
	public static String getComparisonOp(String value)
	{
		for(int i=0; i < comparisonOpsLongestFirst.length; ++i)
		{
			if(value.startsWith(comparisonOpsLongestFirst[i]))
				return comparisonOpsLongestFirst[i];
		}
		
		return "";
	}
	
	private static int convertComparisonOpStringToValue(String op)
	{
		if(op.equals(GREATER_STRING))
			return MartusField.GREATER;
		if(op.equals(GREATER_EQUAL_STRING))
			return MartusField.GREATER_EQUAL;
		if(op.equals(LESS_STRING))
			return MartusField.LESS;
		if(op.equals(LESS_EQUAL_STRING))
			return MartusField.LESS_EQUAL;
		if(op.equals(EQUAL_STRING))
			return MartusField.EQUAL;
		if(op.equals(NOT_EQUAL_STRING))
			return MartusField.NOT_EQUAL;
		
		return MartusField.CONTAINS;
	}

	private String withoutQuotes(String rawValue)
	{
		if(rawValue.startsWith("\""))
			rawValue = rawValue.substring(1, rawValue.length() - 1);
		return rawValue;
	}
	
	public SearchTreeNode(int op, SearchTreeNode left, SearchTreeNode right)
	{
		nodeOp = op;
		nodeLeft = left;
		nodeRight = right;
	}
	
	public FieldSpec getField()
	{
		return field;
	}

	public String getValue()
	{
		return nodeValue;
	}
	
	public int getComparisonOperator()
	{
		return compareOp;
	}

	public int getOperation()
	{
		return nodeOp;
	}

	public SearchTreeNode getLeft()
	{
		return nodeLeft;
	}

	public SearchTreeNode getRight()
	{
		return nodeRight;
	}

	private final static String[] comparisonOpsLongestFirst = 
	{
		NOT_EQUAL_STRING, 
		GREATER_EQUAL_STRING, 
		LESS_EQUAL_STRING, 
		GREATER_STRING, 
		LESS_STRING, 
		EQUAL_STRING,
	};
	
	private String nodeValue;
	private FieldSpec field;
	private int nodeOp;
	private int compareOp;
	private SearchTreeNode nodeLeft;
	private SearchTreeNode nodeRight;

}
