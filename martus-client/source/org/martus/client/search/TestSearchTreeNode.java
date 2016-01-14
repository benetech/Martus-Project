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
import org.martus.util.TestCaseEnhanced;


public class TestSearchTreeNode extends TestCaseEnhanced
{
    public TestSearchTreeNode(String name)
	{
        super(name);
    }

    public void testValueNode()
    {
		SearchTreeNode node = new SearchTreeNode("text");
		assertEquals("text", node.getValue());
		assertEquals(SearchTreeNode.VALUE, node.getOperation());

		node = new SearchTreeNode(" nostripping ");
		assertEquals(" nostripping ", node.getValue());
    }
    
    public void testQuotedValues()
    {
		FieldSpec field = FieldSpec.createStandardField("field", new FieldTypeNormal());
    	String phrase = "search for this";
    	
    	SearchTreeNode withoutField = new SearchTreeNode("\"" + phrase + "\"");
    	assertEquals("without field parsed wrong?", phrase, withoutField.getValue());
    	
    	SearchTreeNode withField = new SearchTreeNode(field, "", "\"" + phrase + "\"");
    	assertEquals("with field parsed wrong?", phrase, withField.getValue());
    	
    	String thisThat = "this:that";
    	SearchTreeNode quotedColon = new SearchTreeNode("\"" + thisThat + "\"");
    	assertEquals("split quoted string?", thisThat, quotedColon.getValue());
    }
    
    public void testComparisons()
    {
		FieldSpec field = FieldSpec.createStandardField("name", new FieldTypeNormal());
    	String basicValue = "stuff";

    	assertEquals("thought it was a comparison op?", "", SearchTreeNode.getComparisonOp("one"));
    	SearchTreeNode noOp = new SearchTreeNode(field, "", basicValue);
    	assertEquals("wrong default op?", MartusField.CONTAINS, noOp.getComparisonOperator());
    	
    	String[] comparisonOps = {"=", "!=", ">", ">=", "<", "<="};
    	int[] comparisonOpValues = {
    		MartusField.EQUAL,
    		MartusField.NOT_EQUAL,
    		MartusField.GREATER, 
    		MartusField.GREATER_EQUAL,
    		MartusField.LESS, 
    		MartusField.LESS_EQUAL,
    	};
    	for(int i=0; i < comparisonOps.length; ++i)
    	{
    		SearchTreeNode node = new SearchTreeNode(field, comparisonOps[i], basicValue);
    		assertEquals("wrong compare op value?", comparisonOpValues[i], node.getComparisonOperator());
    		assertEquals("didn't strip op?", basicValue, node.getValue());
    	}
    }
    
    public void testOpNode()
    {
		verifyOpNodeCreation(SearchTreeNode.OR, "or");
		verifyOpNodeCreation(SearchTreeNode.AND, "and");
	}

	private void verifyOpNodeCreation(int op, String opName)
	{
		SearchTreeNode left = new SearchTreeNode("left");
		SearchTreeNode right = new SearchTreeNode("right");
		SearchTreeNode node = new SearchTreeNode(op, left, right);
		assertEquals(op, node.getOperation());
		assertNull(opName + " didn't clear value?", node.getValue());

		assertNotNull("Left", node.getLeft());
		assertNotNull("Right", node.getRight());

		assertEquals("Left", "left", node.getLeft().getValue());
		assertEquals("Right", "right", node.getRight().getValue());
	}

}
