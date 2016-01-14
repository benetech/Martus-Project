name = 'martus-client-iso'

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']
  release_build_number = $BUILD_NUMBER

  cd_setup_exe = _(:target, :temp, 'MartusClientCDSetup.exe')
  iso_name = "MartusClientCD-#{project.version}-#{input_build_number}-#{release_build_number}.iso"
	iso_file = _(:target, iso_name)
  iso_dir = _(:target, :temp, 'iso')
	volume_name = "Martus-#{project.version}-#{input_build_number}-#{release_build_number}"

  attic_dir = $attic_dir
  signed_jar_file = File.join(attic_dir, "martus-client-signed-#{input_build_number}.jar")

  martus_jar_file = _(:target, :temp, 'martus.jar')
  
  task martus_jar_file => signed_jar_file do
    FileUtils::cp(signed_jar_file, martus_jar_file)
	end
	
	def add_file(from, to)
	  puts "add_file: #{from} to #{to}"
	  FileUtils::cp(from, to)
	end
	
	def add_files(pattern, dir)
    puts "add_files: #{pattern} to #{dir}"
	  if ! File.directory? dir
	    raise "Can only copy multiple files to a directory, not to #{dir}"
	  end
	  Dir.glob(pattern).each do | file |
	    add_file(file, dir)
	  end
	end

	def add_artifact(dir, artifact)
	  puts "add_artifact #{artifact} to #{dir}"
	  add_file(artifact.to_s, dir)
	end
	
  def add_artifact_as(dir, artifact, new_name)
    dest = File.join(dir, new_name)
    FileUtils::cp(artifact.to_s, dest)
  end
  
	def add_artifacts(dir, artifacts)
	  puts "add_artifacts #{artifacts} to #{dir}"
	  artifacts.each do | artifact |
	    add_artifact(dir, artifact)
	  end
	end
		
	file iso_dir => martus_jar_file do
    puts "Creating ISO tree in #{iso_dir}"
    FileUtils::rm_rf(iso_dir)
    puts "-iso directory removed"
    FileUtils::mkdir(iso_dir)
    puts "-iso directory created"
    
    puts "-adding jar, autorun, icon, and windows installer"
    add_file(martus_jar_file, iso_dir)
    add_file(_('martus-build', 'BuildFiles', 'ProgramFiles', 'autorun.inf'), iso_dir)
    add_file(_('martus-build', 'BuildFiles', 'ProgramFiles', 'app.ico'), iso_dir)
    add_artifacts(iso_dir, [cd_setup_exe])

    puts "-adding dmg"
    dmg_filename = "MartusClient-#{project.version}-#{input_build_number}-#{release_build_number}.dmg"
    dmg = _(:target, dmg_filename)
    add_file(dmg, File.join(iso_dir, "MartusClient-#{project.version}.dmg"))
    
    #NOTE: For now at least, don't include Linux zip
    
    puts "-adding LibExt"
    lib_dir = File.join(iso_dir, 'LibExt')
    FileUtils.mkdir(lib_dir)
    add_artifacts(lib_dir, third_party_client_jars) 
  
    puts "-adding verify"
    verify_dir = File.join(iso_dir, 'verify')
    FileUtils.mkdir(verify_dir)
    add_files(_('martus-jar-verifier', '*.bat'), verify_dir)
    add_files(_('martus-jar-verifier', '*.txt'), verify_dir)
    add_files(_('martus-jar-verifier', "readme_verify*.txt"), verify_dir)
  
    puts "-adding Documents directory"
    docs_dir = File.join(iso_dir, 'Documents')
    FileUtils.mkdir(docs_dir)
    add_file(_('martus-build', 'BuildFiles', 'Documents', 'license.txt'), docs_dir)
    add_file(_('martus-build', 'BuildFiles', 'Documents', 'gpl.txt'), docs_dir)
    add_files(_('martus-build', 'BuildFiles', 'Documents', "client", 'README*.txt'), docs_dir)
    add_files(_('martus-build', 'BuildFiles', 'Documents', "client", '*.pdf'), docs_dir)

    puts "-adding thirdparty docs"
    thirdpartydocs_dir = File.join(docs_dir, 'ThirdParty')
    FileUtils.mkdir(thirdpartydocs_dir)
    add_artifacts(thirdpartydocs_dir, third_party_client_licenses)
    
    puts "-adding SourceFiles"
    source_dir = File.join(iso_dir, 'SourceFiles')
    source_zip = File.join(attic_dir, "martus-client-sources-#{$client_version}.zip")
    FileUtils.mkdir(source_dir)
    add_artifacts(source_dir, third_party_client_source)  
    add_file(source_zip, File.join(source_dir, "MartusClientSources-#{project.version}.zip"))
	end
	
	file iso_file => [cd_setup_exe, iso_dir] do
    puts "Creating ISO"
    options = '-J -r -T -hide-joliet-trans-tbl -l'
    volume = "-V #{volume_name}"
    output = "-o #{iso_file}"
    `mkisofs #{options} #{volume} #{output} #{iso_dir}`
  
    create_sha_files(iso_file)
	end
	
	build(iso_file)
	
	clean do
	  FileUtils.rm_rf(iso_dir)
	  FileUtils.rm_f(iso_file)
	end
	
end
