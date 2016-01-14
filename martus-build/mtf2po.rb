#! /usr/env ruby
=begin
The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2013, Beneficent
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
=end


def process_file(input, output)
	while(!input.eof?)
		line = input.gets.strip
		if line.index('#_') == 0
			english = line
			translated = input.gets.strip
			process_entry(output, english, translated)
		end
	end
end

def process_entry(output, english, translated)
	filler, english_text = get_stuff_before_and_after_equals(english)
	hash_and_context, translated_text = get_stuff_before_and_after_equals(translated)
	if hash_and_context.index('-') == 0
		hash = hash_and_context[1,4]
		context = hash_and_context[6..-1]
	else
		hash = ''
		context = hash_and_context 
	end	

	english_text.gsub!(/\\\//, "\\\\\\/") # why are 3 pairs of \ needed here?
	translated_text.gsub!(/\\\//, "\\\\\\/")

	english_text.gsub!(/\"/, "\\\"")
	translated_text.gsub!(/\"/, "\\\"")
	
	output.puts
	output.puts "#: #{hash}"
	if(english_text.index("\\n"))
		output.puts "#. Do NOT translate the \\n because they represent newlines."
	end
	if(english_text.index("Benetech"))
		output.puts "#. Do NOT translate the word Benetech."
	end
	if(english_text.index("Martus"))
		output.puts "#. Do NOT translate the word Martus."
	end
	if(english_text.index("Tor"))
		output.puts "#. Do NOT translate the word Tor."
	end
	if(english_text =~ /#.*#/)
		output.puts "#. Do not translate words that are surrounded by #'s, but you may move " + 
		"them around as grammatically appropriate. " +
		"Example: #TotalNumberOfFilesInBackup#, #Titles#, #FieldLabel#, etc. " +
		"as these words will be replaced when the program runs with " +
		"a particular value. " +
		"For Example. #TotalNumberOfFilesInBackup# = '5' " +
		"#Titles# = 'A list of bulletin titles' "
	end
	if(english_text =~ /\(\..*\)/)
		output.puts "#. For file filters like 'Martus Report Format (.mrf), " +
		"The descriptive names should be translated, but the (.mrf) must not be translated."
	end
	if(context == "field:VirtualKeyboardKeys")
		output.puts "#. Keep the english alphabet, but include any " + 
		"non-english characters at the end of the english alphabet/numbers/special " + 
		"characters (e.g. attach entire Thai alphabet at the end of the line)."
	end
	if(context == "field:translationVersion")
		output.puts "#. Do not translate the numbers."
	end
	if(context == "field:ErrorCustomFields")
		output.puts "#. Do not translate the numbers."
	end
	if(context.index("CreateCustomFieldsHelp"))
		output.puts "#. You can translate tags into foreign characters (but without punctuation or spaces)."
		output.puts "#. Check the User Guide section 10b to see if the text has already been translated and use the same translation for consistency."
	end
	if(context.index("CreateCustomFieldsHelp1") || context.index("CreateCustomFieldsHelp2"))
		output.puts "#. Leave standard field tags in English, but put translation in parentheses after " + 
		"english : e.g.  'author' (translation-of-author from mtf, e.g. autor in spanish), " +
		"so users know what they refer to."
	end
	if(context.index("CreateCustomFieldsHelp2"))
		output.puts "#. Leave field types in English (e.g. BOOLEAN, DATE), " + 
		"but put translation in parentheses after english, so users know what they refer to."
		output.puts "#. Change the \"ddd\" in \"<DefaultValue>ddd</DefaultValue>\" to whatever letter the translation of \"default\" begins with."
	end
	if(context.index("CreateCustomFieldsHelp3"))
		output.puts "#. Leave field types in English in examples (e.g. BOOLEAN, DATE)"
		output.puts "#. do not translate words between angle brackets in the XML for custom fields, such as: " +
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
		"<ReusableChoices code= , </ReusableChoices>, label= , <Choice code= ."
	end
	if(english_text.empty?)
		english_text = ' '
		translated_text = ' '
	end
	untranslated = (/^<(.*?)>$/.match translated_text)
	if(untranslated)
		if(untranslated[1] == english_text)
			translated_text = ""
		else
			output.puts "#. This English string has changed, so this translation need to be updated and then marked non-fuzzy."
			output.puts "#, fuzzy"
			translated_text = untranslated[1]
		end
	end
	
	if $pot
		translated_text = "" 
	end

	# Transifex fails if original starts with newline and translated doesn't	
	while english_text[0,2] == "\\n" && translated_text[0,2] != "\\n"
		translated_text = "\\n" + translated_text
	end
	
	# Transifex fails if original doesn't start with newline but translated does	
	while translated_text[0,2] == "\\n" && english_text[0,2] != "\\n"
		translated_text = translated_text[2..-1]
	end

	if $language == 'th' && english_text.index("WARNING:  ") == 0
		puts english_text
		puts translated_text
	end
	# Transifex fails if original ends with newline and translated doesn't	
	while english_text[-2,2] == "\\n" && translated_text[-2,2] != "\\n"
		translated_text = translated_text + "\\n"
	end
	
	# Transifex fails if original doesn't end with newline but translated does	
	while translated_text[-2,2] == "\\n" && english_text[-2,2] != "\\n"
	puts "Removing trailing newline"
		translated_text = translated_text[0..-3]
	end

	# Special case for a poor Arabic translation
	if translated_text == "\\n" && english_text[0,2] != "\\n"
		translated_text = " \\n"
	end

	output.puts "msgctxt \"#{context}\""
	output.puts "msgid \"\""
	output.puts "\"#{english_text}\""
	output.puts "msgstr \"\""
	output.puts "\"#{translated_text}\""
end

def get_stuff_before_and_after_equals(text)
	equals = text.index('=')
	before = text[0, equals]
	after = text[equals+1..-1]
	return [before, after]
end

def write_quoted(output, text)
	output.puts "\"#{text}\""
end

def process_header(input)
	while(!input.eof?)
		line = input.gets.strip
		if(line.empty?)
			return
		end
		
		if(line.index("# Language name:") == 0)
			$language_name = extract_after_colon(line)
		elsif (line.index("# Client version") == 0)
			$version = extract_after_colon(line)
		end
	end
	
end

def extract_after_colon(line)
	colon = line.index(':')
	return line[colon+1..-1].strip
end

def write_header(output)
	output.puts "msgid \"\""
	output.puts "msgstr \"\""
	write_quoted output, "Project-Id-Version: Martus #{$version}\\n"
	write_quoted output, "Report-Msgid-Bugs-To: martus@benetech.org\\n"
	write_quoted output, "POT-Creation-Date: #{Time.now}\\n"
	if(!$pot)
		#write_quoted output, "PO-Revision-Date: #{Time.now}\\n"
		#write_quoted output, "Last-Translator: Jeremy <jeremyy@miradi.org>\\n"
		write_quoted output, "Language-Team: #{$language_name}\\n"
	end
	write_quoted output, "MIME-Version: 1.0\\n"
	write_quoted output, "Content-Type: text/plain; charset=UTF-8\\n"
	write_quoted output, "Content-Transfer-Encoding: 8bit\\n"
	#write_quoted output, "Plural-Forms: nplurals=2; plural=(n != 1);\\n"
	output.puts
	output.puts
end

def convert(language, out)
	mtf_filename = "Martus-#{language}.mtf"
	File.open(mtf_filename) do | input |
		process_header(input)
		write_header(out)
		process_file(input, out)
	end
end

def create_pot_from(language)
	puts("Creating .pot")
	$pot = true
	File.open("Martus-5.1.pot", "w") do | out |
		convert(language, out)
	end
	$pot = false
end

def convert_po_for(language)
	$language = language
	mtf = "Martus-#{language}.mtf"
	po = "Martus-5.1-#{language}.po"
	if File.exists? mtf
		puts("Converting #{language}")
		File.open(po, "w") do | out |
			convert(language, out)
		end
	end
end

create_pot_from('en')

# Transifex doesn't need a PO for each language, so don't bother creating them
#languages = ['ar', 'arm', 'bur', 'es', 'fa', 'fr', 'km', 'ne', 'nl', 'ru', 'sq', 'th'] 
#languages.each do | language |
#	convert_po_for(language)
#end
