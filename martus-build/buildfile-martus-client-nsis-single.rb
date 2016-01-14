name = 'martus-client-nsis-single'

require "#{File.dirname(__FILE__)}/buildfile-martus-client-nsis-common"

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
	project.version = ENV['RELEASE_IDENTIFIER']
	input_build_number = ENV['INPUT_BUILD_NUMBER']
	release_build_number = $BUILD_NUMBER

	exe_name = "MartusSetupSingle.exe"
	exe_path = _(:target, exe_name)
	destination = _(:target, "MartusClientSetup-#{project.version}-#{input_build_number}-#{release_build_number}.exe")
  
	task :configure_nsis do
		from = get_nsis_contrib_language_directory(File.dirname(__FILE__))
		copy_files_to_nsis(from)
	end

	nsis_zip_file = project('martus-client-nsis-zip').package(:zip)

	file exe_path => nsis_zip_file do
		puts "Building NSIS Single installer"
		run_nsis_task(nsis_zip_file.to_s, 'NSIS_Martus_Single.nsi', exe_name)
		FileUtils.mv exe_path, destination
		create_sha_files(destination)
	end
	
	build(:configure_nsis, exe_path) do
		artifact(MARTUS_SINGLE_SETUP_EXE_SPEC).from(destination)
		puts "Created artifact #{MARTUS_SINGLE_SETUP_EXE_SPEC}"
	end
	
end
