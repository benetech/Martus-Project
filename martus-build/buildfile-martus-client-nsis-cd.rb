name = 'martus-client-nsis-cd'

require "#{File.dirname(__FILE__)}/buildfile-martus-client-nsis-common"

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']
  release_build_number = $BUILD_NUMBER

  temp_dir = _(:target, :temp)
	exe_name = 'MartusClientCDSetup.exe'
	exe_path = File.join(temp_dir, exe_name)

	task :configure_nsis do
		from = get_nsis_contrib_language_directory(File.dirname(__FILE__))
		copy_files_to_nsis(from)
	end

  nsis_zip_file = project('martus-client-nsis-zip').package(:zip)

  file exe_path => nsis_zip_file do
    puts "Building NSIS CD installer"
    FileUtils.mkdir_p temp_dir
		run_nsis_task(nsis_zip_file.to_s, 'NSIS_Martus.nsi', exe_name)
		FileUtils.mv(_(:target, exe_name), exe_path)
	end
	
	build(exe_path)

  artifact(MARTUS_SINGLE_SETUP_EXE_SPEC).from(exe_path)
end

