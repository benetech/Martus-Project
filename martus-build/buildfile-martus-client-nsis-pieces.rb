name = "martus-client-nsis-pieces"

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']
  release_build_number = $BUILD_NUMBER

  setup_artifact = project('martus-client-nsis-single').artifact(MARTUS_SINGLE_SETUP_EXE_SPEC)
  
  temp_dir = File.join(_(:target, :temp), 'chunks')
  full_version = "#{project.version}-#{input_build_number}-#{release_build_number}"
  base_name = "MartusClientSetupMultiPart-#{full_version}"
  zip_file = _(:target, "#{base_name}.zip")
  original_exe_file = setup_artifact.to_s
  renamed_exe_file = _(:target, :temp, "#{base_name}.exe")
  original_merger_file = _('martus-build', 'BuildFiles', 'MartusSetupLauncher', 'Release', 'MartusSetupBuilder-[Version_Number].exe')
  renamed_merger_file = File.join(temp_dir, "#{base_name}.exe")

  puts "Setting up build dependency: #{setup_artifact.to_s}"
  build(setup_artifact.to_s) do
    FileUtils.mkdir_p(temp_dir)
    FileUtils.rm_f(Dir.glob(File.join(temp_dir, "*")))
    puts "After deleting, there are #{Dir.glob(File.join(temp_dir, '*')).size} files in #{temp_dir}"
    FileUtils.cp original_exe_file, renamed_exe_file
    
    #NOTE: filesplit won't compile with modern Linux C++, 
    # so we will use GNU split, which is compatible, except for file naming
    # which we can fix in post-processing (below)
    command = "split --numeric-suffixes --suffix-length=3 --bytes=5M #{renamed_exe_file} #{temp_dir}/"
    puts command
    result = `#{command}` 
    puts "#{command}\n#{result}"
    if $CHILD_STATUS != 0
      raise "Failed in split #{$CHILD_STATUS}"
    end
    
    parts = Dir.glob(File.join(temp_dir, '*')).sort
    number = 1
    print "Post-processing #{parts.size} chunks"
    parts.each do | part |
      new_number = sprintf('%03d', number)
      number += 1
      new_name = "MartusClient-#{full_version}_#{new_number}.cnk"
      chunk = File.join(temp_dir, new_name)
      FileUtils.mv part, chunk
      create_sha_files(chunk)
      print "."
    end
    puts "Done"
    
    puts "Copying #{original_merger_file} to #{renamed_merger_file}"
    FileUtils.cp original_merger_file, renamed_merger_file
    create_sha_files(renamed_merger_file)
  end

  package(:zip, :file=>zip_file).tap do | p | 
    puts "Creating chunks zip: #{zip_file}"
    p.include(File.join(temp_dir, '*'))
  end
end
