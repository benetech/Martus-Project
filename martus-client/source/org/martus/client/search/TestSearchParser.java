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
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.util.TestCaseEnhanced;

public class TestSearchParser extends TestCaseEnhanced
{
    public TestSearchParser(String name)
	{
        super(name);
    }
    
    public void setUp()
    {
		englishParser = SearchParser.createEnglishParser();
    }
    
    public void testTokenizeEmpty()
    {
    	assertEquals("not empty?", 0, englishParser.tokenize("").size());
    }
    
    public void testTokenizeTrailingSpaces()
    {
    	String[]  words = {"red", "green", "blue", };
    	String toTokenize = "";
    	for(int i=0; i < words.length; ++i)
    		toTokenize += words[i] + "   ";
    	verifyTokenized(words, englishParser.tokenize(toTokenize));
    }

    public void testTokenizeLeadingSpaces()
    {
    	String[]  words = {"red", "green", "blue", };
    	String toTokenize = "";
    	for(int i=0; i < words.length; ++i)
    		toTokenize += "   " + words[i];
    	verifyTokenized(words, englishParser.tokenize(toTokenize));
    }
    
    public void testTokenizedQuotedString()
    {
    	String[] tokens = {"this", "\"here or there\"", "that", };
    	String toTokenize = "";
    	for(int i=0; i < tokens.length; ++i)
    		toTokenize += tokens[i] + " ";
    	verifyTokenized(tokens, englishParser.tokenize(toTokenize));
    	
    }
    
    public void testTokenizedSpecificFieldSimple()
    {
    	String fieldValue = ":field:value";
    	TokenList tokens = englishParser.tokenize(fieldValue);
    	assertEquals(1, tokens.size());
    	assertEquals(fieldValue, tokens.get(0));
    	
    }

    public void testTokenizedQuoted()
    {
    	String fieldQuotedValue = "\"quoted :value\"";
    	TokenList tokens = englishParser.tokenize(fieldQuotedValue);
    	assertEquals(1, tokens.size());
    	assertEquals(fieldQuotedValue, tokens.get(0));
    	
    }

	private void verifyTokenized(String[] words, TokenList result)
	{
		assertEquals(3, result.size());
    	for(int i=0; i < words.length; ++i)
    		assertEquals(words[i], result.get(i));
	}
    

    public void testParseEmpty()
    {
    	SearchTreeNode rootNode = englishParser.parseJustAmazonValueForTesting("");
    	assertEquals("not empty?", "", rootNode.getValue());
    }
    
    public void testFieldButNoString()
    {
		FieldSpec field = FieldSpec.createStandardField("field", new FieldTypeNormal());
    	SearchTreeNode rootNode = englishParser.parse(field, "=", "");
    	assertEquals("didn't remember field?", field, rootNode.getField());
    	assertEquals("didn't remember op?", MartusField.EQUAL, rootNode.getComparisonOperator());
    }
    
    public void testSimpleSearch()
	{
		SearchTreeNode rootNode = englishParser.parseJustAmazonValueForTesting("blah");
		assertNotNull("Null root", rootNode);
		assertEquals(SearchTreeNode.VALUE, rootNode.getOperation());
	}
	
	public void testLowerCase()
	{
		SearchTreeNode rootNode = englishParser.parseJustAmazonValueForTesting("this OR that");
		assertNotNull("Null root", rootNode);
		assertEquals(SearchTreeNode.OR, rootNode.getOperation());
	}

	public void testSimpleOr()
	{
		SearchTreeNode rootNode = englishParser.parseJustAmazonValueForTesting("this or that");
		assertNotNull("Null root", rootNode);
		assertEquals(SearchTreeNode.OR, rootNode.getOperation());

		SearchTreeNode left = rootNode.getLeft();
		assertNotNull("Null left", left);
		assertEquals("this", left.getValue());

		SearchTreeNode right = rootNode.getRight();
		assertNotNull("Null right", right);
		assertEquals("that", right.getValue());
	}

	public void testSimpleAnd()
	{
		SearchTreeNode rootNode = englishParser.parseJustAmazonValueForTesting(" tweedledee  and  tweedledum ");
		assertNotNull("Null root", rootNode);
		assertEquals(SearchTreeNode.AND, rootNode.getOperation());

		SearchTreeNode left = rootNode.getLeft();
		assertNotNull("Null left", left);
		assertEquals("tweedledee", left.getValue());

		SearchTreeNode right = rootNode.getRight();
		assertNotNull("Null right", right);
		assertEquals("tweedledum", right.getValue());
	}

	public void testAndBeforeOr()
	{
		// (a AND b) OR c
		SearchTreeNode abc = englishParser.parseJustAmazonValueForTesting("a and b or c");
		assertNotNull("Null root", abc);
		assertEquals("rootNode", SearchTreeNode.OR, abc.getOperation());
		
		SearchTreeNode ab = abc.getLeft();
		assertNotNull("root Null left", ab);
		assertEquals("ab", SearchTreeNode.AND, ab.getOperation());
		assertEquals("a", ab.getLeft().getValue());
		assertEquals("b", ab.getRight().getValue());

		assertEquals("c", abc.getRight().getValue());

	}
	
	public void testOrBeforeAnd()
	{
		// (a OR b) AND c
		SearchTreeNode abc = englishParser.parseJustAmazonValueForTesting("a or b and c");
		assertNotNull("Null root", abc);
		assertEquals("rootNode", SearchTreeNode.AND, abc.getOperation());
		
		SearchTreeNode ab = abc.getLeft();
		assertNotNull("root Null left", ab);
		assertEquals("ab", SearchTreeNode.OR, ab.getOperation());
		assertEquals("a", ab.getLeft().getValue());
		assertEquals("b", ab.getRight().getValue());

		assertEquals("c", abc.getRight().getValue());

	}
	
	public void testMultipleWords()
	{
		SearchTreeNode ab = englishParser.parseJustAmazonValueForTesting("a b");
		assertEquals("rootNode", SearchTreeNode.AND, ab.getOperation());
		assertEquals("a", ab.getLeft().getValue());
		assertEquals("b", ab.getRight().getValue());
	}
	
	public void testQuoted()
	{
		String quotedValue = "testing quoted";
		SearchTreeNode quoted = englishParser.parseJustAmazonValueForTesting("\"" + quotedValue + "\"");
		assertEquals(quotedValue, quoted.getValue());
	}
	
	public void testSpecificField()
	{
		SearchTreeNode all = englishParser.parseJustAmazonValueForTesting("testing");
		assertEquals("not searching all fields?", "", all.getField().getTag());
		
		FieldSpec field = FieldSpec.createStandardField("name", new FieldTypeNormal());
		SearchTreeNode name = englishParser.parse(field, "", "smith");
		assertEquals("not searching name?", "name", name.getField().getTag());
		assertEquals("smith", name.getValue());

		String greenEggs = "green eggs and ham";
		SearchTreeNode phrase = englishParser.parse(field, "", "\"" + greenEggs + "\"");
		assertEquals("not searching name?", "name", phrase.getField().getTag());
		assertEquals("green eggs and ham", phrase.getValue());
	}
	
	public void testAmazonStyleSearching()
	{
		FieldSpec field = FieldSpec.createStandardField("field", new FieldTypeNormal());
		String plain = "plain";
		String quoted = "quoted";
		SearchTreeNode or = englishParser.parse(field, ">", plain + " or \"" + quoted + "\"");
		assertEquals("didn't see the or?", SearchTreeNode.OR, or.getOperation());
		
		SearchTreeNode left = or.getLeft();
		assertEquals("left part not a value?", SearchTreeNode.VALUE, left.getOperation());
		assertEquals("left part wrong value?", plain, left.getValue());
		assertEquals("left part wrong field?", field, left.getField());
		assertEquals("left part wrong op?", MartusField.GREATER, left.getComparisonOperator());

		SearchTreeNode right = or.getRight();
		assertEquals("right part not a value?", SearchTreeNode.VALUE, right.getOperation());
		assertEquals("right part wrong value?", quoted, right.getValue());
		assertEquals("right part wrong field?", field, right.getField());
		assertEquals("right part wrong op?", MartusField.GREATER, right.getComparisonOperator());
	}
	
	public void testDropdownValuesWithSpaces()
	{
		String valueWithSpaces = "Item with spaces";
		ChoiceItem[] choices = {new ChoiceItem(valueWithSpaces, valueWithSpaces)};
		FieldSpec field = new DropDownFieldSpec(choices);
		SearchTreeNode result = englishParser.parse(field, "=", valueWithSpaces);
		assertEquals("didn't ignore spaces?", valueWithSpaces, result.getValue());
	}
	
/*	
 * This test won't be valid until we support parens
 * 
 * public void testReallyComplex()
	{
		// (a and b) OR (c AND (d and e) OR f)
		SearchTreeNode rootNode = englishParser.parse("(a b) or (c (d e) or f)");
		assertNotNull("Null root", rootNode);
		assertEquals("rootNode", SearchTreeNode.OR, rootNode.getOperation());

		SearchTreeNode ab = rootNode.getLeft();
		assertNotNull("ab Null", ab);
		assertEquals("ab", SearchTreeNode.AND, ab.getOperation());
		assertEquals("a", ab.getLeft().getValue());
		assertEquals("b", ab.getRight().getValue());

		SearchTreeNode cdef = rootNode.getRight();
		assertNotNull("cdef Null", cdef);
		assertEquals("cdef", SearchTreeNode.OR, cdef.getOperation());
		assertEquals("f", cdef.getRight().getValue());

		SearchTreeNode cde = cdef.getLeft();
		assertNotNull("cde Null", cde);
		assertEquals("cde", SearchTreeNode.AND, cde.getOperation());
		assertEquals("c", cde.getLeft().getValue());

		SearchTreeNode de = cde.getRight();
		assertNotNull("de Null", de);
		assertEquals("de", SearchTreeNode.AND, de.getOperation());
		assertEquals("d", de.getLeft().getValue());
		assertEquals("e", de.getRight().getValue());
	}
*/
	public void testSpanish()
	{
		// (a OR b) AND c
		SearchParser parser = new SearchParser("y", "o");
		SearchTreeNode abc = parser.parseJustAmazonValueForTesting("a o b y c");
		assertNotNull("Null root", abc);
		assertEquals("rootNode", SearchTreeNode.AND, abc.getOperation());

		SearchTreeNode ab = abc.getLeft();
		assertNotNull("root Null left", ab);
		assertEquals("ab", SearchTreeNode.OR, ab.getOperation());
		assertEquals("a", ab.getLeft().getValue());
		assertEquals("b", ab.getRight().getValue());

		assertEquals("c", abc.getRight().getValue());
	}
	
	public void testEnglishAndAndOrAlwaysWork()
	{
		SearchParser parser = new SearchParser("y", "o");
		SearchTreeNode o = parser.parseJustAmazonValueForTesting("a o b");
		assertEquals("'o' not OR?", SearchTreeNode.OR, o.getOperation());
		SearchTreeNode y = parser.parseJustAmazonValueForTesting("a y b");
		assertEquals("'y' not AND?", SearchTreeNode.AND, y.getOperation());
		SearchTreeNode or = parser.parseJustAmazonValueForTesting("a or b");
		assertEquals("'or' not OR?", SearchTreeNode.OR, or.getOperation());
		SearchTreeNode and = parser.parseJustAmazonValueForTesting("a and b");
		assertEquals("'and' not OR?", SearchTreeNode.AND, and.getOperation());
		
	}
	
	SearchParser englishParser;
}
