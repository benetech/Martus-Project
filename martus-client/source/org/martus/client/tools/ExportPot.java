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
package org.martus.client.tools;

import java.io.File;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.SortedSet;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiSession;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.common.MiniLocalization;
import org.martus.common.i18n.TranslationEntry;


public class ExportPot
{
	public static void main(String[] args) throws Exception
	{
		if(args.length == 0)
		{
			System.err.println("Usage: ExportPot <filename.pot>");
			System.exit(1);
		}
			
		PrintStream out = new PrintStream(new File(args[0]), "UTF-8");
		new ExportPot().exportPot(out);
		out.flush();
		out.close();
		
		System.out.println("Done.");
	}

	public ExportPot() throws Exception
	{
		File translationsDirectory = MartusApp.getTranslationsDirectory();
		String[] allEnglishStrings = UiSession.getAllEnglishStrings();
		localization = new MartusLocalization(translationsDirectory, allEnglishStrings);
	}
	
	public void exportPot(PrintStream out) throws Exception
	{
		exportHeader(out);
		exportEntries(out);
	}

	private void exportHeader(PrintStream out)
	{
		LocalDate now = LocalDate.now();
		String today = DateTimeFormatter.ISO_LOCAL_DATE.format(now);
		out.println("msgid \"\"");
		out.println("msgstr \"\"");
		printlnQuoted(out, "Project-Id-Version: Martus " + UiConstants.versionLabel + "\\n");
		printlnQuoted(out, "Report-Msgid-Bugs-To: martus@benetech.org\\n");
		printlnQuoted(out, "POT-Creation-Date: " + today + "\\n");
		printlnQuoted(out, "MIME-Version: 1.0\\n");
		printlnQuoted(out, "Content-Type: text/plain; charset=UTF-8\\n");
		printlnQuoted(out, "Content-Transfer-Encoding: 8bit\\n");
		//#write_quoted output, "Plural-Forms: nplurals=2; plural=(n != 1);\\n"
		out.println();
		out.println();
		
	}

	private void printlnQuoted(PrintStream out, String string)
	{
		out.println('"' + string + '"');
	}

	public void exportEntries(PrintStream out)
	{
		SortedSet<String> strings = localization.getAllKeysSorted();
		for (String key : strings)
		{
			String english = localization.getLabel(MiniLocalization.ENGLISH, key);
			String hex = localization.getHashOfEnglish(key);
			
			TranslationEntry entry = new TranslationEntry();
			entry.append(TranslationEntry.MSGID, english);
			entry.append(TranslationEntry.MSGCTXT, key);
			entry.setHex(hex);
			
			exportEntry(out, entry);
		}
	}

	public void exportEntry(PrintStream out, TranslationEntry entry)
	{
		String english_text = entry.getMsgid();
		english_text = english_text.replace("\\", "\\\\");
		english_text = english_text.replace("\n", "\\n");
		english_text = english_text.replace("\"", "\\\"");

		out.println("#: " + entry.getHex());
		exportNotesToTranslators(out, entry);
		out.print("msgctxt ");
		printlnQuoted(out, entry.getContext());
		out.print("msgid ");
		printlnQuoted(out, "");
		out.print("msgstr ");
		printlnQuoted(out, english_text);
		out.println();
	}
	
	private void exportNotesToTranslators(PrintStream out, TranslationEntry entry)
	{
		String english_text = entry.getMsgid();
		english_text = english_text.replaceAll("\n", "\\\\n");
		
		if(english_text.contains("\\n"))
			out.println("#. Do NOT translate the \\n because they represent newlines.");
		if(english_text.contains("Benetech"))
			out.println("#. Do NOT translate the word Benetech.");
		if(english_text.contains("Martus"))
			out.println("#. Do NOT translate the word Martus.");
		if(english_text.contains("Tor"))
			out.println("#. Do NOT translate the word Tor.");
		if(english_text.matches(".*?#.*?#.*"))
			out.println("#. Do not translate words that are surrounded by #'s, but you may move " + 
					"them around as grammatically appropriate. " +
					"Example: #TotalNumberOfFilesInBackup#, #Titles#, #FieldLabel#, etc. " +
					"as these words will be replaced when the program runs with " +
					"a particular value. " +
					"For Example. #TotalNumberOfFilesInBackup# = '5' " +
					"#Titles# = 'A list of bulletin titles' ");
		if(english_text.matches(".*?\\(\\..*\\).*"))
			out.println("#. For file filters like 'Martus Report Format (.mrf), " +
					"The descriptive names should be translated, but the (.mrf) must not be translated.");
		if(entry.getContext().equals("field:VirtualKeyboardKeys"))
			out.println("#. Keep the english alphabet, but include any " + 
					"non-english characters at the end of the english alphabet/numbers/special " + 
					"characters (e.g. attach entire Thai alphabet at the end of the line).");
		if(entry.getContext().equals("field:translationVersion"))
			out.println("#. Do not translate the numbers.");
		if(entry.getContext().equals("field:ErrorCustomFields"))
			out.println("#. Do not translate the numbers.");
		if(entry.getContext().contains("CreateCustomFieldsHelp"))
		{
			out.println("#. You can translate tags into foreign characters (but without punctuation or spaces).");
			out.println("#. Check the User Guide section 10b to see if the text has already been translated and use the same translation for consistency.");
		}
		if(entry.getContext().contains("CreateCustomFieldsHelp1") || entry.getContext().contains("CreateCustomFieldsHelp2"))
			out.println("#. Leave standard field tags in English, but put translation in parentheses after " + 
					"english : e.g.  'author' (translation-of-author from mtf, e.g. autor in spanish), " +
					"so users know what they refer to.");
		if(entry.getContext().contains("CreateCustomFieldsHelp2"))
		{
			out.println("#. Leave field types in English (e.g. BOOLEAN, DATE), " + 
					"but put translation in parentheses after english, so users know what they refer to.");
			out.println("#. Change the \"ddd\" in \"<DefaultValue>ddd</DefaultValue>\" to whatever letter the translation of \"default\" begins with.");
		}
		if(entry.getContext().contains("CreateCustomFieldsHelp3"))
		{
			out.println("#. Leave field types in English in examples (e.g. BOOLEAN, DATE)");
			out.println("#. do not translate words between angle brackets in the XML for custom fields, such as: " +
					"<Field type='SECTION'>, <Field type='STRING'>, <Field type='BOOLEAN'>, <Field type='DATE'>, " + 
					"<Field type='DATERANGE'>, <Field type='DROPDOWN'>, <Field type='MULTILINE'>  " +
					"<Field type='LANGUAGE'>, <Field type='MESSAGE'>, <Field type='GRID'>,  " +
					"</Field>, <Tag>, </Tag>, <Label>, </Label>,  <Message>, </Message>  " +
					"<Choices>, </Choices>, <Choice>, </Choice>, <DataSource>, </DataSource> " +
					"<GridFieldTag>, </GridFieldTag>, <GridColumnLabel>, </GridColumnLabel>  " +
					"<GridSpecDetails>, </GridSpecDetails>, <Column>, </Column>,  " +
					"<Column type='STRING'>, <Column type='BOOLEAN'>, <Column type='DATE'>, " +
					"<Column type='DATERANGE'>, <Column type='DROPDOWN'>  " +
					"<KeepWithPrevious/>, <RequiredField/>, <DefaultValue>, </DefaultValue>, " +
					"<MinimumDate>, </MinimumDate>, <MaximumDate>, </MaximumDate>, <MaximumDate/>. " +
					"For Reusable choices sections, translate anything within single quotes '...', but not  " +
					"<UseReusableChoices code= , </UseReusableChoices> " +
					"<ReusableChoices code= , </ReusableChoices>, label= , <Choice code= .");
		}
	}

	private MtfAwareLocalization localization;
}
