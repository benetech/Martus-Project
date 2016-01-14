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
package org.martus.common.test;

import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeDropdown;
import org.martus.common.fieldspec.FieldTypeGrid;
import org.martus.common.fieldspec.FieldTypeLanguage;
import org.martus.common.fieldspec.FieldTypeMessage;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FieldTypeUnknown;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.TestCustomDropDownFieldSpec;
import org.martus.util.TestCaseEnhanced;


public class TestGridFieldSpec extends TestCaseEnhanced
{

	public TestGridFieldSpec(String name)
	{
		super(name);
	}
	
	public void testGridXmlFieldSpecLoaderLegacy() throws Exception
	{
		FieldSpec.XmlFieldSpecLoader loader = new FieldSpec.XmlFieldSpecLoader();
		loader.parse(SAMPLE_GRID_FIELD_XML_LEGACY);
		GridFieldSpec spec = (GridFieldSpec)loader.getFieldSpec();
		assertEquals(2, spec.getColumnCount());
		assertContains(SAMPLE_GRID_HEADER_LABEL_1, spec.getAllColumnLabels());
		assertContains(SAMPLE_GRID_HEADER_LABEL_2, spec.getAllColumnLabels());
		assertEquals(SAMPLE_GRID_FIELD_XML, spec.toString());
	}
	
	public void testGridXmlFieldSpecLoaderNormal() throws Exception
	{
		FieldSpec.XmlFieldSpecLoader loader = new FieldSpec.XmlFieldSpecLoader();
		loader.parse(SAMPLE_GRID_FIELD_XML);
		GridFieldSpec spec = (GridFieldSpec)loader.getFieldSpec();
		assertEquals(2, spec.getColumnCount());
		assertContains(SAMPLE_GRID_HEADER_LABEL_1, spec.getAllColumnLabels());
		assertContains(SAMPLE_GRID_HEADER_LABEL_2, spec.getAllColumnLabels());
		assertEquals(SAMPLE_GRID_FIELD_XML, spec.toString());
	}

	public void testGridXmlFieldSpecLoaderDropdown() throws Exception
	{
		FieldSpec.XmlFieldSpecLoader loader = new FieldSpec.XmlFieldSpecLoader();
		loader.parse(SAMPLE_GRID_FIELD_XML_DROPDOWN);
		GridFieldSpec spec = (GridFieldSpec)loader.getFieldSpec();
		assertEquals(3, spec.getColumnCount());
		assertContains(SAMPLE_GRID_HEADER_LABEL_1, spec.getAllColumnLabels());
		assertContains(SAMPLE_GRID_HEADER_LABEL_2, spec.getAllColumnLabels());
		assertContains(TestCustomDropDownFieldSpec.SAMPLE_DROPDOWN_LABEL, spec.getAllColumnLabels());
		assertEquals(SAMPLE_GRID_FIELD_XML_DROPDOWN, spec.toString());
	}

	public void testAddColumn() throws Exception
	{
		GridFieldSpec spec = new GridFieldSpec();

		String labelStringColumn = "TYPE_NORMAL";
		FieldSpec stringSpec = FieldSpec.createFieldSpec(labelStringColumn, new FieldTypeNormal());
		spec.addColumn(stringSpec);
		assertEquals(labelStringColumn, spec.getColumnLabel(0));
		assertEquals(new FieldTypeNormal(), spec.getColumnType(0));
		assertEquals(labelStringColumn, spec.getFieldSpec(0).getLabel());

		FieldSpec.XmlFieldSpecLoader loader = new FieldSpec.XmlFieldSpecLoader();
		loader.parse(TestCustomDropDownFieldSpec.SAMPLE_DROPDOWN_FIELD_XML);
		DropDownFieldSpec dropdownSpecToAdd = (DropDownFieldSpec)loader.getFieldSpec();
		spec.addColumn(dropdownSpecToAdd);
		assertEquals(TestCustomDropDownFieldSpec.SAMPLE_DROPDOWN_LABEL, spec.getColumnLabel(1));
		assertEquals(new FieldTypeDropdown(), spec.getColumnType(1));

		DropDownFieldSpec dropdownSpecRetrieved = (DropDownFieldSpec)spec.getFieldSpec(1);
		assertEquals(3, dropdownSpecRetrieved.getCount());
		assertEquals("", dropdownSpecRetrieved.getValue(0));
		assertEquals(TestCustomDropDownFieldSpec.SAMPLE_DROPDOWN_CHOICE1, dropdownSpecRetrieved.getValue(1));
		assertEquals(TestCustomDropDownFieldSpec.SAMPLE_DROPDOWN_CHOICE2, dropdownSpecRetrieved.getValue(2));


		String labelBooleanColumn = "TYPE_BOOLEAN";
		FieldSpec booleanSpec = FieldSpec.createFieldSpec(labelBooleanColumn, new FieldTypeBoolean());
		spec.addColumn(booleanSpec);
		assertEquals(labelBooleanColumn, spec.getColumnLabel(2));
		assertEquals(new FieldTypeBoolean(), spec.getColumnType(2));
		assertEquals(labelBooleanColumn, spec.getFieldSpec(2).getLabel());
		
		String labelDateColumn = "TYPE_DATE";
		FieldSpec dateSpec = FieldSpec.createFieldSpec(labelDateColumn, new FieldTypeDate());
		spec.addColumn(dateSpec);
		assertEquals(labelDateColumn, spec.getColumnLabel(3));
		assertEquals(new FieldTypeDate(), spec.getColumnType(3));
		assertEquals(labelDateColumn, spec.getFieldSpec(3).getLabel());

		String labelDateRangeColumn = "TYPE_DATERANGE";
		FieldSpec dateRangeSpec = FieldSpec.createFieldSpec(labelDateRangeColumn, new FieldTypeDateRange());
		spec.addColumn(dateRangeSpec);
		assertEquals(labelDateRangeColumn, spec.getColumnLabel(4));
		assertEquals(new FieldTypeDateRange(), spec.getColumnType(4));
		assertEquals(labelDateRangeColumn, spec.getFieldSpec(4).getLabel());
	}
	
	public void testAddColumnWithDisallowedTypes()
	{
		assertFalse("language column allowed?", GridFieldSpec.isValidGridColumnType(new FieldTypeLanguage()));
		assertFalse("multiline column allowed?", GridFieldSpec.isValidGridColumnType(new FieldTypeMultiline()));
		assertFalse("grid column allowed?", GridFieldSpec.isValidGridColumnType(new FieldTypeGrid()));
		assertFalse("message column allowed?", GridFieldSpec.isValidGridColumnType(new FieldTypeMessage()));
		assertFalse("unknown column allowed?", GridFieldSpec.isValidGridColumnType(new FieldTypeUnknown()));
	}

	public static final String SAMPLE_GRID_HEADER_LABEL_1 = "label1";
	public static final String SAMPLE_GRID_HEADER_LABEL_2 = "label2";
	public static final String SAMPLE_GRID_FIELD_XML_LEGACY = "<Field type='GRID'>\n" +
			"<Tag>custom</Tag>\n" +
			"<Label>me</Label>\n" +
			"<GridSpecDetails>\n<Column><Label>" +
			SAMPLE_GRID_HEADER_LABEL_1 +
			"</Label></Column>\n<Column><Label>" +
			SAMPLE_GRID_HEADER_LABEL_2 +
			"</Label></Column>\n</GridSpecDetails>\n</Field>\n";

	public static final String SAMPLE_GRID_FIELD_XML = "<Field type='GRID'>\n" +
	"<Tag>custom</Tag>\n" +
	"<Label>me</Label>\n" +
	"<GridSpecDetails>\n<Column type='STRING'>\n" +
	"<Tag></Tag>\n" +
	"<Label>"+SAMPLE_GRID_HEADER_LABEL_1+"</Label>\n" +
	"</Column>\n" +
	"<Column type='STRING'>\n" +
	"<Tag></Tag>\n" +
	"<Label>"+SAMPLE_GRID_HEADER_LABEL_2+"</Label>\n" +
	"</Column>\n" +
	"</GridSpecDetails>\n" +
	"</Field>\n";
	
	public static final String SAMPLE_DROPDOWN_CHOICE1 = "choice #1";
	public static final String SAMPLE_DROPDOWN_CHOICE2 = "choice #2";
	public static final String SAMPLE_DROPDOWN_LABEL = "Dropdown Label";
	public static final String SAMPLE_DROPDOWN_FIELD_XML = "<Column type='DROPDOWN'>\n" +
	"<Tag>custom</Tag>\n" +
	"<Label>"+SAMPLE_DROPDOWN_LABEL+"</Label>\n" +
	"<Choices>\n" +
	"<Choice>" +
	"</Choice>\n" +
	"<Choice>" +
	SAMPLE_DROPDOWN_CHOICE1 +
	"</Choice>\n" +
	"<Choice>" +
	SAMPLE_DROPDOWN_CHOICE2 +
	"</Choice>\n" +
	"</Choices>\n" +
	"</Column>\n";

	public static final String SAMPLE_GRID_FIELD_XML_DROPDOWN = "<Field type='GRID'>\n" +
	"<Tag>custom with dropdowns</Tag>\n" +
	"<Label>dropdowns</Label>\n" +
	"<GridSpecDetails>\n" +
	"<Column type='STRING'>\n" +
	"<Tag></Tag>\n" +
	"<Label>"+SAMPLE_GRID_HEADER_LABEL_1+"</Label>\n" +
	"</Column>\n" +
	"<Column type='STRING'>\n" +
	"<Tag></Tag>\n" +
	"<Label>"+SAMPLE_GRID_HEADER_LABEL_2+"</Label>\n" +
	"</Column>\n" +
	SAMPLE_DROPDOWN_FIELD_XML +
	"</GridSpecDetails>\n" +
	"</Field>\n";
	

}
