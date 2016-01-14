repositories.remote << 'http://www.ibiblio.org/maven2/'
repositories.remote << 'http://repo1.maven.org/maven2/'
repositories.remote << 'http://download.java.net/maven/2'

$BUILD_NUMBER = ENV['BUILD_NUMBER'] || 'TEST'
$JAVAC_VERSION = '8'

on_windows = (RUBY_PLATFORM.index('mswin') || RUBY_PLATFORM.index("mingw"))
unicode = true
use_wine = (!on_windows && unicode)


$nsis_script_dir = "Win32_NSIS#{unicode ? '_Unicode' : ''}"

$full_nsis_dir = ENV['NSIS_HOME']
if !$full_nsis_dir
	puts "ERROR: NSIS_HOME must be defined"
	exit(1)
end

if !File.directory? $full_nsis_dir
	puts "ERROR: NSIS_HOME must exist: #{$full_nsis_dir}"
	exit(1)
end

nsis_exe = 'makensis'

if use_wine
  DRIVE = "drive_"
  drive_at = $full_nsis_dir.index(DRIVE)
  if !drive_at
    puts "ERROR: NSIS_HOME must contain #{DRIVE}: #{$full_nsis_dir}"
    exit(1)
  end
  drive_letter_at = drive_at + DRIVE.length
  drive = $full_nsis_dir[drive_letter_at, 1]
  relative = $full_nsis_dir[drive_letter_at+1..-1]
  wine_nsis_home = File.join("#{drive}:", relative)
  nsis_exe_path = File.join(wine_nsis_home, nsis_exe)
  $nsis_command = "wine \"#{nsis_exe_path}\" /V3 /NOCD"
else
  $nsis_command = "#{nsis_exe} -V2"
end

$client_version = ENV['INPUT_BUILD_NUMBER'] || 'NNN'

$attic_dir = ENV['ATTIC_DIR']
if !$attic_dir
	puts "ERROR: ATTIC_DIR must be set"
	exit(1)
end

puts "BUILD_NUMBER: #{$BUILD_NUMBER}"


$ORCHID_VERSION = '1.0.0'

def build_spec(group, name, type, version)
	return "#{group}:#{name}:#{type}:#{version}"
end

def build_junit_spec(type)
	return build_spec('junit', 'junit', type, '4.11')
end

def build_xmlrpc_commons_logging_spec(type)
	return build_spec('commons-logging', 'commons-logging', type, '1.1')
end

def build_xmlrpc_ws_commons_util_spec(type)
	return build_spec('ws-commons-util', 'ws-commons-util', type, '1.0.2')
end

def build_xmlrpc_spec(name, type)
	return build_spec('xmlrpc', name, type, '3.1.3')
end

def build_icu4j_spec(type)
	return build_spec('com.ibm.icu', 'icu4j', type, '3.4.4')
end

def build_javarosa_spec(type)
	return build_spec('javarosa', 'javarosa', type, '1.0')
end

def build_kxml_spec(type)
	return build_spec('kxml', 'kxml', type, '2.3')
end

def build_layouts_spec(type)
	return build_spec('com.jhlabs', 'layouts', type, '2006-08-10')
end

def build_velocity_spec(type)
	return build_spec('velocity', 'velocity', type, '1.4')
end

def build_velocity_dep_spec(type)
	return build_spec('velocity', 'velocity-dep', type, '1.4')
end

def build_jetty_spec(type)
	return build_spec('jetty', 'jetty', type, '4.2.27')
end

def build_javax_servlet_spec(type)
	return build_spec('jetty', 'javax.servlet', type, '5.1.12')
end

def build_lucene_spec(type)
	return build_spec('lucene', 'lucene', type, '1.3-rc1')
end

def build_persiancalendar_spec(type)
	return build_spec('com.ghasemkiani', 'persiancalendar', type, '2.1')
end

def build_bcprov_spec(type)
	return build_spec('bouncycastle', 'bcprov-jdk15on', type, '148')
end

def build_mail_spec(type)
	return build_spec('javax.mail', 'mail', type, '1.4.3')
end

def build_rhino_spec(type)
	return build_spec('org.mozilla.rhino', 'js', type, '2006-03-08')
end

def build_logi_spec(type)
	return build_spec('org.logi', 'logi', type, '1.1.2')
end

def build_jortho_spec(type)
  return build_spec('de.inetsoftware', 'jortho', type, '0.5')
end

def build_jfreechart_spec(type)
  return build_spec('org.jfree', 'jfreechart', type, '1.0.14')
end

def build_jcommon_spec(type)
  return build_spec('org.jfree', 'jcommon', type, '1.0.17')
end

def build_orchid_spec(type)
  return build_spec('com.subgraph.orchid', 'orchid', type, $ORCHID_VERSION)
end

# LibExt, from public repository

# LibExt, not in public repository
BCPROV_SPEC = build_bcprov_spec('jar')
BCPROV_SOURCE_SPEC = build_bcprov_spec('sources')
BCPROV_LICENSE_SPEC = build_bcprov_spec('license')
JUNIT_SPEC = build_junit_spec('jar')
JUNIT_SOURCE_SPEC = build_junit_spec('sources')
JUNIT_LICENSE_SPEC = build_junit_spec('license')

# Common, not in public repository
LOGI_LICENSE_SPEC = build_logi_spec('license')
ORCHID_SPEC = build_orchid_spec('jar') 
ORCHID_SOURCE_SPEC = build_orchid_spec('sources') 
ORCHID_LICENSE_SPEC = build_orchid_spec('license') 

# Common, from public repository
PERSIANCALENDAR_SPEC = build_persiancalendar_spec('jar')
PERSIANCALENDAR_SOURCE_SPEC = build_persiancalendar_spec('sources')
PERSIANCALENDAR_LICENSE_SPEC = build_persiancalendar_spec('license')
VELOCITY_SPEC = build_velocity_spec('jar')
VELOCITY_SOURCE_SPEC = build_velocity_spec('sources')
VELOCITY_LICENSE_SPEC = build_velocity_spec('license')
VELOCITY_DEP_SPEC = build_velocity_dep_spec('jar')
VELOCITY_DEP_SOURCE_SPEC = build_velocity_dep_spec('sources')
VELOCITY_DEP_LICENSE_SPEC = build_velocity_dep_spec('license')
XMLRPC_COMMON_SPEC = build_xmlrpc_spec('xmlrpc-common', 'jar')
XMLRPC_CLIENT_SPEC = build_xmlrpc_spec('xmlrpc-client', 'jar')
XMLRPC_SERVER_SPEC = build_xmlrpc_spec('xmlrpc-server', 'jar')
XMLRPC_COMMONS_LOGGING_SPEC = build_xmlrpc_commons_logging_spec('jar')
XMLRPC_WS_COMMONS_UTIL_SPEC = build_xmlrpc_ws_commons_util_spec('jar')
XMLRPC_SOURCE_SPEC = build_xmlrpc_spec('apache-xmlrpc', 'sources')
XMLRPC_LICENSE_SPEC = build_xmlrpc_spec('LICENSE.txt', 'license')
ICU4J_SPEC = build_icu4j_spec('jar')
ICU4J_SOURCE_SPEC = build_icu4j_spec('sources')
ICU4J_LICENSE_SPEC = build_icu4j_spec('license')
JAVAROSA_SPEC = build_javarosa_spec('jar')
JAVAROSA_SOURCE_SPEC = build_javarosa_spec('sources')
JAVAROSA_LICENSE_SPEC = build_javarosa_spec('license')
KXML_SPEC = build_kxml_spec('jar')

# Client, from public repository
JORTHO_SPEC = build_jortho_spec('jar')
JORTHO_SOURCE_SPEC = build_jortho_spec('sources')
JORTHO_LICENSE_SPEC = build_jortho_spec('license')
JORTHO_ENGLISH_SPEC = build_jortho_spec('dict-en')
JORTHO_SPANISH_SPEC = build_jortho_spec('dict-es')
JFREECHART_SPEC = build_jfreechart_spec('jar')
JFREECHART_SOURCE_SPEC = build_jfreechart_spec('sources')
JFREECHART_LICENSE_SPEC = build_jfreechart_spec('license')
JCOMMON_SPEC = build_jcommon_spec('jar')
JCOMMON_SOURCE_SPEC = build_jcommon_spec('sources')
JCOMMON_LICENSE_SPEC = build_jcommon_spec('license')
LAYOUTS_SPEC = build_layouts_spec('jar')
LAYOUTS_SOURCE_SPEC = build_layouts_spec('sources')
LAYOUTS_LICENSE_SPEC = build_layouts_spec('license')
RHINO_SPEC = build_rhino_spec('jar')
RHINO_SOURCE_SPEC = build_rhino_spec('sources')
RHINO_LICENSE_SPEC = build_rhino_spec('license')

# Server, from public repository
JETTY_SPEC = build_jetty_spec('jar')
JETTY_SOURCE_SPEC = build_jetty_spec('sources')
JETTY_LICENSE_SPEC = build_jetty_spec('license')
JAVAX_SERVLET_SPEC = build_javax_servlet_spec('jar')
JAVAX_SERVLET_LICENSE_SPEC = build_javax_servlet_spec('license')
LUCENE_SPEC = build_lucene_spec('jar')
LUCENE_SOURCE_SPEC = build_lucene_spec('sources')
LUCENE_LICENSE_SPEC = build_lucene_spec('license')
MAIL_SPEC = build_mail_spec('jar')
MAIL_LICENSE_SPEC = build_mail_spec('license')
BLUEPRINT_CORE_SPEC = build_spec('com.orienttechnologies.orient', 'blueprints-core', 'jar', '2.5.0')
COMMONS_BEANUTILS_SPEC = build_spec('com.orienttechnologies.orient', 'commons-beanutils', 'jar', '1.7.0')
COMMONS_BEANUTILS_CORE_SPEC = build_spec('com.orienttechnologies.orient', 'commons-beanutils-core', 'jar', '1.8.0')
COMMONS_COLLECTIONS_SPEC = build_spec('com.orienttechnologies.orient', 'commons-collections', 'jar', '3.2.1')
COMMONS_CONFIGURATION_SPEC = build_spec('com.orienttechnologies.orient', 'commons-configuration', 'jar', '1.6')
COMMONS_DIGESTER_SPEC = build_spec('com.orienttechnologies.orient', 'commons-digester', 'jar', '1.8')
COMMONS_LANG_SPEC = build_spec('com.orienttechnologies.orient', 'commons-lang', 'jar', '2.4')
CONCURRENT_LINKED_HASHMAP_LRU_SPEC = build_spec('com.orienttechnologies.orient', 'concurrentlinkedhashmap-lru', 'jar', '1.4')
JNA_SPEC = build_spec('com.orienttechnologies.orient', 'jna', 'jar', '4.0.0')
JNA_PLATFORM_SPEC = build_spec('com.orienttechnologies.orient', 'jna-platform', 'jar', '4.0.0')
ORIENT_COMMONS_SPEC = build_spec('com.orienttechnologies.orient', 'orient-commons', 'jar', '1.7.4')
ORIENTDB_CORE_SPEC = build_spec('com.orienttechnologies.orient', 'orientdb-core', 'jar', '1.7.4')
ORIENTDB_GRAPHDB_SPEC = build_spec('com.orienttechnologies.orient', 'orientdb-graphdb', 'jar', '1.7.4')
ORIENTDB_NATIVEOS_SPEC = build_spec('com.orienttechnologies.orient', 'orientdb-nativeos', 'jar', '1.7.4')
ORIENTDB_SOURCE_SPEC = build_spec('com.orienttechnologies.orient', 'orientdb', 'source', '1.7.4')
ORIENTDB_LICENSE_SPEC = build_spec('com.orienttechnologies.orient', 'orientdb', 'license', '1.7.4')

MARTUS_SINGLE_SETUP_EXE_SPEC = build_spec('org.martus', 'martus_single_setup', 'exe', 'JustOneVersion')
MARTUS_UPGRADE_SETUP_EXE_SPEC = build_spec('org.martus', 'martus_upgrade_setup', 'exe', 'JustOneVersion')
DMG_SPEC = build_spec('org.martus', 'martus_client_dmg', 'dmg', $client_version)

def create_layout_with_source_as_source(base)
	layout = Layout.new
	layout[:root] = "#{base}"
	layout[:source, :main, :java] = "#{base}/source"
	layout[:source, :test, :java] = "#{base}/source"
	layout[:target] = "#{base}/target"
	layout[:target, :main, :classes] = "#{base}/target/main/classes"
	layout[:target, :test, :classes] = "#{base}/target/test/classes"
	return layout
end

def update_packaged_zip(package)
	package.enhance do | task |
		task.enhance do
			yield package.name
		end
	end
end

def unzip_file (file, destination)
	Zip::ZipFile.open(file) do |zip_file|
		zip_file.each do |f|
			f_path=File.join(destination, f.name)
			FileUtils.mkdir_p(File.dirname(f_path))
			if File.exist?(f_path) && !File.directory?(f_path)
				raise "Can't overwrite #{f_path}"
			end
			if(! f.directory?)
				puts "unzip_file #{file}: #{f} (#{f.name}, #{f_path})"
				zip_file.extract(f.name, f_path)
			end 
		end
	end
end

def unzip_one_entry(artifact, entry, destination)
  Zip::ZipFile.open(artifact.to_s) do |zip_file|
    zip_file.each do |f|
      if entry == f.name
        f_path=File.join(destination, f.name)
        FileUtils.mkdir_p(File.dirname(f_path))
        if File.exist?(f_path) && !File.directory?(f_path)
          raise "Can't overwrite #{f_path}"
        end
        zip_file.extract(f, f_path) 
      end
    end
  end
end


def extract_artifact_entry_task(artifact_spec, entry)
	return extract_zip_entry_task(artifact(artifact_spec).to_s, entry)
end

def extract_zip_entry_task(zip_file, entry_to_extract)
	target_dir = _('target', 'temp')
	extracted_file = File.join(target_dir, entry_to_extract)
	unzip_task = unzip(target_dir=>zip_file).include(entry_to_extract)
	return file extracted_file=>unzip_task
end

def create_sha_files(filepath)
  create_sha1(filepath)
  create_sha2(filepath)
end

def create_sha1(filepath)
  return create_sha(filepath, 'sha1sum', '.sha1')
end

def create_server_sha1(filepath)
  return create_sha(filepath, 'sha1sum', '.sha')
end

def create_sha2(filepath)
  return create_sha(filepath, 'sha256sum', '.sha2')
end

def create_sha(filepath, digester, extension)
  full_sha_path = "#{filepath}#{extension}"
  dir = File::dirname filepath
  base = File::basename filepath
  working_dir = Dir::getwd
  Dir::chdir dir
  
  cmd = "#{digester} #{base} > #{full_sha_path}"
  puts cmd
	output = `#{cmd} 2>&1`
	result = $?
  Dir::chdir working_dir

  if result != 0
		raise "Error generating #{extension} of #{filepath}\n#{output}"
	end
	
	return full_sha_path
end

def today_as_iso_date
  return Time.now.strftime('%F')  # %F is ISO date YYYY-MM-DD
end

def today_without_dashes
  return today_as_iso_date.gsub(/-/, '')
end

def third_party_client_jars
	jars = []
	jars << artifact(RHINO_SPEC)
	jars << artifact(LAYOUTS_SPEC)
	jars << artifact(BCPROV_SPEC)
	jars << artifact(JUNIT_SPEC)
	jars << artifact(ICU4J_SPEC)
	jars << artifact(PERSIANCALENDAR_SPEC)
	jars << artifact(VELOCITY_DEP_SPEC)
	jars << artifact(XMLRPC_COMMONS_LOGGING_SPEC)
	jars << artifact(XMLRPC_WS_COMMONS_UTIL_SPEC)
	jars << artifact(XMLRPC_COMMON_SPEC)
	jars << artifact(XMLRPC_CLIENT_SPEC)
	# NOTE: JOrtho is being included in martus.jar, so exclude it here,
	# but keep it in the licenses and source code sections
	#jars << artifact(JORTHO_SPEC)
	jars << artifact(JFREECHART_SPEC)
	jars << artifact(JCOMMON_SPEC)
	jars << artifact(ORCHID_SPEC)
	jars << artifact(JAVAROSA_SPEC)
	jars << artifact(KXML_SPEC)
	return jars
end

def third_party_client_licenses
	licenses = []
	licenses << artifact(BCPROV_LICENSE_SPEC)
	licenses << artifact(JUNIT_LICENSE_SPEC)
	licenses << artifact(PERSIANCALENDAR_LICENSE_SPEC)
	licenses << artifact(LOGI_LICENSE_SPEC)
	licenses << artifact(VELOCITY_LICENSE_SPEC)
	licenses << artifact(VELOCITY_DEP_LICENSE_SPEC)
	licenses << artifact(XMLRPC_LICENSE_SPEC)
	licenses << artifact(ICU4J_LICENSE_SPEC)
	licenses << artifact(LAYOUTS_LICENSE_SPEC)
	licenses << artifact(RHINO_LICENSE_SPEC)
	licenses << artifact(JORTHO_LICENSE_SPEC)
	licenses << artifact(JFREECHART_LICENSE_SPEC)
	licenses << artifact(JCOMMON_LICENSE_SPEC)
	licenses << artifact(ORCHID_LICENSE_SPEC)
	licenses << artifact(JAVAROSA_LICENSE_SPEC)
	return licenses
end

def third_party_client_source
  sources = []
  sources << artifact(BCPROV_SOURCE_SPEC)
  sources << artifact(JUNIT_SOURCE_SPEC)
  sources << artifact(PERSIANCALENDAR_SOURCE_SPEC)
  sources << artifact(VELOCITY_SOURCE_SPEC)
# TODO: Find velocity-dep source code
#	sources << artifact(VELOCITY_DEP_SOURCE_SPEC)
  sources << artifact(XMLRPC_SOURCE_SPEC)
# TODO: Find ICU4J source code
#	sources << artifact(ICU4J_SOURCE_SPEC)
  sources << artifact(LAYOUTS_SOURCE_SPEC)
  sources << artifact(RHINO_SOURCE_SPEC)
  sources << artifact(JORTHO_SOURCE_SPEC)
	sources << artifact(JFREECHART_SOURCE_SPEC)
	sources << artifact(JCOMMON_SOURCE_SPEC)
	sources << artifact(ORCHID_SOURCE_SPEC)
	sources << artifact(JAVAROSA_SOURCE_SPEC)
	return sources
end

def include_artifacts(target, artifacts, path)
	artifacts.each do | artifact |
		target.include(artifact, :path=>path)
	end
end

def include_artifact(target, artifact, path, name)
  target.include(artifact, :path=>path, :as=>name)
end

def fix_newlines(files)
	Dir.glob(files).each do | file |
		`unix2dos #{file}`
	end
end

task nil do
end

task :always

define 'martus-build' do
	build do
		create_combined_license
	
		fix_newlines(_('BuildFiles', '*.txt'))
		fix_newlines(_('BuildFiles', 'Documents', '*.txt'))
		fix_newlines(project('martus-jar-verifier').path_to('*.txt'))
	end

	#TODO: Set up a task that depends on: client exe, client iso+sha,
	# client chunks, client mac dmg, client linux zip, mlp files, 
	# server jar, mspa zip, and any other products
end
	
