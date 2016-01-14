/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.core.templates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;

import org.martus.client.core.templates.FormTemplateManager.InvalidTemplateNameException;
import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;

public class TestFormTemplateManager extends TestCaseEnhanced
{
	public TestFormTemplateManager(String name)
	{
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		
		security = MockMartusSecurity.createClient();
	}
	
	public void testCreateFails() throws Exception
	{
		File badDirectory = createTempFile();
		try
		{
			FormTemplateManager.createOrOpen(security, badDirectory);
			fail("Should have thrown for not a directory");
		}
		catch(FileNotFoundException ignoreExpected)
		{
		}
		finally
		{
			badDirectory.delete();
		}
	}
	
	public void testCreateNewDirectory() throws Exception
	{
		File tempDirectory = createTempDirectory();
		try
		{
			File templateDirectory = new File(tempDirectory, "templates");
			FormTemplateManager manager = FormTemplateManager.createOrOpen(security, templateDirectory);
			
			Set<String> names = manager.getAvailableTemplateNames();
			assertEquals(1, names.size());
			String onlyTemplateName = names.iterator().next();
			assertEquals(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME, onlyTemplateName);
			
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
	}

	public void testSaveAndLoad() throws Exception
	{
		File tempDirectory = createTempDirectory();
		try
		{
			File templateDirectory = new File(tempDirectory, "templates");
			FormTemplateManager manager = FormTemplateManager.createOrOpen(security, templateDirectory);
			
			assertEquals(1, manager.getAvailableTemplateNames().size());

			String title = "t1";
			FormTemplate template = createFormTemplate(title, "d1");
			manager.putTemplate(template);
			assertEquals(2, manager.getAvailableTemplateNames().size());
			FormTemplate got = manager.getTemplate(title);
			assertEquals(template.getDescription(), got.getDescription());
			
			String filename = FormTemplateManager.getTemplateFilename(title);
			File templateFile = new File(templateDirectory, filename);
			FileOutputStream out = new FileOutputStream(templateFile, true);
			out.write(5);
			out.close();
			
			PrintStream dest = MartusLogger.getDestination();
			MartusLogger.disableLogging();
			manager = FormTemplateManager.createOrOpen(security, templateDirectory);
			assertEquals(1, manager.getAvailableTemplateNames().size());
			MartusLogger.setDestination(dest);
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
	}
	
	public void testDefaultTemplate() throws Exception
	{
		File tempDirectory = createTempDirectory();
		try
		{
			File templateDirectory = new File(tempDirectory, "templates");
			FormTemplateManager manager = FormTemplateManager.createOrOpen(security, templateDirectory);
			
			FormTemplate defaultTemplate = manager.getTemplate("");
			assertEquals("", defaultTemplate.getTitle());
			assertEquals("", defaultTemplate.getDescription());
			FieldSpecCollection top = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
			assertEquals(top.toXml(), defaultTemplate.getTopSectionXml());
			FieldSpecCollection bottom = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
			assertEquals(bottom.toXml(), defaultTemplate.getBottomSectionXml());

			String title = "";
			FormTemplate template = createFormTemplate(title, "d1");
			try
			{
				manager.putTemplate(template);
				fail("Should have thrown trying to put template with no title");
			}
			catch(InvalidTemplateNameException ignoreExpected)
			{
			}
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
	}
	
	public void testCurrentTemplate() throws Exception
	{
		File tempDirectory = createTempDirectory();
		try
		{
			File templateDirectory = new File(tempDirectory, "templates");
			FormTemplateManager manager = FormTemplateManager.createOrOpen(security, templateDirectory);
			assertEquals("", manager.getCurrentFormTemplate().getTitle());
			
			String title = "other";
			FormTemplate template = createFormTemplate(title, "");
			manager.putTemplate(template);
			assertEquals("", manager.getCurrentFormTemplate().getTitle());
			manager.setCurrentFormTemplate(title);
			assertEquals(title, manager.getCurrentFormTemplateNameProperty().getValue());
			
			FormTemplateManager otherManager = FormTemplateManager.createOrOpen(security, templateDirectory);
			assertEquals(title, otherManager.getCurrentFormTemplateNameProperty().getValue());
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
	}
	
	public void testSaveLoadState() throws Exception
	{
		File tempDirectory = createTempDirectory();
		try
		{
			File templateDirectory = new File(tempDirectory, "templates");
			FormTemplateManager manager = FormTemplateManager.createOrOpen(security, templateDirectory);
			
			manager.setCurrentFormTemplate("");
			manager = FormTemplateManager.createOrOpen(security, templateDirectory);

			String title = "other";
			FormTemplate template = createFormTemplate(title, "");
			manager.putTemplate(template);
			assertEquals("", manager.getCurrentFormTemplate().getTitle());
			manager.setCurrentFormTemplate(title);
			manager = FormTemplateManager.createOrOpen(security, templateDirectory);
			assertEquals(title, manager.getCurrentFormTemplateNameProperty().getValue());
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
		
	}
	
	public void testDelete() throws Exception
	{
		File tempDirectory = createTempDirectory();
		try
		{
			File templateDirectory = new File(tempDirectory, "templates");
			FormTemplateManager manager = FormTemplateManager.createOrOpen(security, templateDirectory);
			
			String title = "Title";
			FormTemplate template = createFormTemplate(title, "Description");
			manager.putTemplate(template);
			manager.setCurrentFormTemplate(title);
			
			try
			{
				manager.deleteTemplate(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME);
				fail("Should have thrown trying to delete the default template");
			}
			catch(InvalidTemplateNameException ignoreExpected)
			{
			}

			try
			{
				manager.deleteTemplate("Does not exist");
				fail("Should have thrown trying to delete a non-existent template");
			}
			catch(FileNotFoundException ignoreExpected)
			{
			}
			
			assertEquals(2, manager.getAvailableTemplateNames().size());
			manager.deleteTemplate(title);
			assertEquals(1, manager.getAvailableTemplateNames().size());
			String currentTemplateTitle = manager.getCurrentFormTemplate().getTitle();
			assertEquals(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME, currentTemplateTitle);
			
			manager = FormTemplateManager.createOrOpen(security, templateDirectory);
			assertEquals(1, manager.getAvailableTemplateNames().size());
			
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
	}
	
	private FormTemplate createFormTemplate(String title, String description) throws Exception
	{
		FieldSpecCollection top = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection bottom = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		FormTemplate template = new FormTemplate(title, description, top, bottom);
		return template;
	}
	
	public MockMartusSecurity security;
}

