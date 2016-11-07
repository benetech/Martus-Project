/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

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
package org.martus.common.bulletin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.javarosa.core.model.FormDef;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.util.XFormUtils;
import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class FormTemplateFromXFormsLoader extends AbtractXFormsLoader
{
	private FormTemplateFromXFormsLoader()
	{
	}

	public static boolean isXFormsXml(String possibleXFormsXml)
	{
		try
		{
			return new FormTemplateFromXFormsLoader().startsWithXFormsElement(possibleXFormsXml);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return false;
		}
	}
	
	public static FormTemplate createNewBulletinFromXFormsFormTemplate(String xFormsModel) throws Exception
	{
		return new FormTemplateFromXFormsLoader().createFormTemplate(xFormsModel);
	}
	
	private boolean startsWithXFormsElement(String possibleXFormsXml) throws Exception
	{
		if (possibleXFormsXml == null)
			return false;
		
		if (possibleXFormsXml.isEmpty())
			return false;

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(new InputSource(new ByteArrayInputStream(possibleXFormsXml.getBytes(StandardCharsets.UTF_8))));
		Element documentElement = document.getDocumentElement();
		documentElement.normalize();
		NamedNodeMap attributes = documentElement.getAttributes();
		Node xmlnsAttributeValue = attributes.getNamedItem("xmlns");
		
		if (xmlnsAttributeValue != null && xmlnsAttributeValue.getNodeValue() != null && xmlnsAttributeValue.getNodeValue().equals(XFORMS_XMLNS_URL))
			return true;

		String rootElementName = documentElement.getNodeName();
		if (rootElementName.equals(XFORMS_ROOT_ELEMENT_NAME))
			return true;
		
		return false;
	}
		
	private FormTemplate createFormTemplate(String xFormsModelXmlAsString) throws Exception
	{
		System.out.println(xFormsModelXmlAsString);
        initializeJavaRosa();    	

        InputStream xFormsModelInputStream = new ByteArrayInputStream(xFormsModelXmlAsString.getBytes(StandardCharsets.UTF_8));
        FormDef formDef = XFormUtils.getFormFromInputStream(xFormsModelInputStream);
        FormEntryModel formEntryModel = new FormEntryModel(formDef);
        FormEntryController formEntryController = new FormEntryController(formEntryModel);
        
		FieldSpecCollection fieldSpecsFromXForms = createFieldSpecsFromXForms(formEntryController);
		
		FieldSpecCollection allFields = new FieldSpecCollection();
		FieldSpecCollection defaultTopFieldSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		allFields.addAll(defaultTopFieldSpecs);
		allFields.addAll(fieldSpecsFromXForms);
		
		FieldSpecCollection bottom = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		FormTemplate importedFormTemplate = new FormTemplate(formDef.getTitle(), "This form template was generated from an xforms", allFields, bottom);
		
		return importedFormTemplate;
	}
	
	private static final String XFORMS_ROOT_ELEMENT_NAME = "xforms_model";
	private static final String XFORMS_XMLNS_URL = "http://www.w3.org/2002/xforms";
}
