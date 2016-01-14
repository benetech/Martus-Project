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

def get_first_non_blank_line(input)
	while(true)
		line = input.gets
		if(!line || !line.strip.empty?)
			break
		end
	end
	return line
end

def gets_stripped(input)
	line = input.gets
	if(!line)
		return nil
	end
	return line.strip
end

class TranslationFileHeader
	attr_reader :version
	attr_reader :as_of_date
	attr_reader :team
	
	def initialize(msgstr)
		lines = msgstr.split("\\n")
		lines.each do | line |
			if /^Project-Id-Version/.match(line) 
				@version = after_colon(line)
			elsif /^POT-Creation-Date/.match(line)
				@as_of_date = after_colon(line)
			elsif /^Language-Team/.match(line) 
				@team = after_colon(line)
			else
				puts "Unknown header line: >#{line}<"
			end
		end
	end
	
	def after_colon(line)
		colon = line.index(':')
		return line[colon+1..-1]
	end
end

class TranslationEntry
	attr_reader :msgid
	attr_reader :msgstr
	attr_reader :hex
	attr_reader :context
	
	def initialize
		@msgid = ''
		@msgstr = ''
		@context = ''
	end
	
	def append(mode, text)
		case mode
			when :msgctxt then
				@context << text
			when :msgid then
				@msgid << text
			when :msgstr then
				@msgstr << text
			else
				raise "Unknown mode: #{mode} (#{text})"
		end
	end
	
	def set_hex(hex)
		@hex = hex
	end
	
	def set_fuzzy
		@fuzzy = true
	end
	
	def fuzzy?
		return @fuzzy
	end
end

class TranslationFileContents
	attr_reader :language_code
	attr_reader :header
	attr_reader :entries
	
	def initialize(language_code)
		@language_code = language_code
		@entries = []
	end
	
	def setHeader(header)
		@header = header
	end
	
	def addEntry(entry)
		if entry.msgid.empty?
			@header ||= TranslationFileHeader.new(entry.msgstr)
		else
			@entries << entry
		end
	end
end

class PoEntryReader
	def PoEntryReader.read(input)
		entry = nil
		mode = nil
		line = get_first_non_blank_line(input)
		while line
			entry ||= TranslationEntry.new
			
			if line.empty?
				break
			end

			if /^#/.match(line)
				PoEntryReader.process_comment(entry, line)
			else
				if line.match(/^msgid /)
					mode = :msgid
				elsif line.match(/^msgstr /)
					mode = :msgstr
				elsif line.match(/^msgctxt /)
					mode = :msgctxt
				end
				
				text = extract_text(line)
				entry.append(mode, text)
			end

			line = gets_stripped(input)
		end
		
		return entry
	end
	
	def PoEntryReader.extract_text(line)
		first = line.index('"')
		last = line.rindex('"')
		if !first || !last
			puts line
		end
		text = line[first+1..last-1]
		return text
	end
	
	def PoEntryReader.process_comment(entry, line)
		if line.match /^#:/ 
			hex = (/\s*(\w\w\w\w)\s*/.match line)
			if(hex)
				entry.set_hex(hex[1])
			end
		elsif line.match /^#,\s+fuzzy/
			entry.set_fuzzy
		else
			puts "Unknown comment: #{line}"
		end
	end
end

class PoReader
	def PoReader.read(language_code, input)
		contents = TranslationFileContents.new(language_code)
		
		while true
			entry = PoEntryReader.read(input)
			if !entry
				break
			end
			contents.addEntry(entry)
		end
		
		return contents
	end	
end

class MtfHeaderWriter
	def MtfHeaderWriter.write(output, contents)
		header = contents.header
		output.print "\uFEFF"
		output.puts "# Martus Client Translation File"
		output.puts "# Language code:  #{contents.language_code}"
		output.puts "# Language name:  #{header.team}"
		output.puts "# Translated by:"
		output.puts "# Exported date:  #{header.as_of_date}"
		output.puts "# Client version: #{header.version}"
		output.puts "# Client build:"
		output.puts "# *** This MTF file was converted from a PO file"
		output.puts
	end
end

class MtfEntryWriter
	def MtfEntryWriter.write(output, entry)
		hex = entry.hex
		if hex
			hex_stuff = "-#{entry.hex}-"
		else
			hex_stuff = ''
		end
		before_equals = "#{hex_stuff}#{entry.context}"
		filler = '_' * (entry.context.length + 5)
		output.puts "##{filler}=#{entry.msgid}"
		translated = entry.msgstr
		has_angle_brackets = /^<(.*?)>$/.match translated
		if(entry.fuzzy? && !has_angle_brackets)
			translated = "<#{translated}>"
		end
		output.puts "#{before_equals}=#{translated}"
		output.puts 
	end
end

class MtfWriter
	def MtfWriter.write(output, contents)
		MtfHeaderWriter.write(output, contents)
		contents.entries.each do | entry |
			MtfEntryWriter.write(output, entry)
		end
	end
end

language_code = 'es'
contents = PoReader.read(language_code, File.open("/home/kevins/Downloads/Martus-#{language_code}.po"))
MtfWriter.write(STDOUT, contents)
