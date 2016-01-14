#! /usr/env ruby
=begin
The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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

require 'csv'
require 'cgi' # for HTML/XML escaping

$filename = ARGV[0]
$code_length = ARGV[1].to_i

def get_code(value)
  stripped = value.gsub(/\W/, '')
  return stripped[0,$code_length]
end

def get_list_code(header)
  level = header.match(/\w*/).to_s
  return header[0,1] + level[1..-1]
end

def xml_escape(s)
  result = s.dup
  result.gsub!("&", "&amp;")
  result.gsub!("<", "&lt;")
  result.gsub!(">", "&gt;")
  result.gsub!("'", "&apos;")
  result.gsub!("\"", "&quot;")
  return result
end

$collisions = 0
$outer_entries = {}
$inner_entries = {}

CSV.open($filename, "r").each do | row |
  if(!$header)
    $header = row
    $outer_level = $header[0]
    $inner_level = $header[1]
    next
  end
  outer_value = row[0]
  inner_value = row[1]
  outer_code = get_code(outer_value)
  if $outer_entries[outer_code] && ($outer_entries[outer_code] != outer_value)
    puts "<!-- DUPLICATE OUTER CODE: #{outer_code}"
  end
  $outer_entries[outer_code] = outer_value
  
  inner_code = get_code(inner_value)
  inner_combined_code = "#{outer_code}.#{inner_code}"
  if $inner_entries[inner_combined_code]
    puts "<!-- DUPLICATE COMBINED CODE: #{inner_combined_code} -->"
    $collisions += 1
    inner_combined_code = "#{inner_combined_code}#{$collisions+1}"
  end
  $inner_entries[inner_combined_code] = inner_value
end

puts "<ReusableChoices code='#{get_list_code($outer_level)}Choices' label='#{xml_escape($outer_level)}'>"
$outer_entries.keys.sort.each do | outer_code |
  puts "<Choice code='#{outer_code}' label='#{xml_escape($outer_entries[outer_code])}'></Choice>"
end
puts "</ReusableChoices>"

puts "<ReusableChoices code='#{get_list_code($inner_level)}Choices' label='#{xml_escape($inner_level)}'>"
$inner_entries.keys.sort.each do | inner_code |
  puts "<Choice code='#{inner_code}' label='#{xml_escape($inner_entries[inner_code])}'></Choice>"
end
puts "</ReusableChoices>"
